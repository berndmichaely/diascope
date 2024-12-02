/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.bernd_michaely.diascope.app.stage.concurrent;

import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.TaskParameters;
import de.bernd_michaely.diascope.app.util.concurrent.WorkerThreadFactory;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.RequestType.*;
import static de.bernd_michaely.diascope.app.util.concurrent.WorkerThreadFactory.TIMEOUT_SECONDS;
import static java.lang.System.Logger.Level.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * Concurrency class for loading images.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ImageLoader implements Consumer<TaskParameters>, AutoCloseable
{
	private static final Logger logger = System.getLogger(ImageLoader.class.getName());
	private boolean closed;
	private @MonotonicNonNull Consumer<TaskResult> onResult;
	private @Nullable Path pathCurrent;
	private final EnumMap<RequestType, @Nullable Path> mapPaths;
	private final EnumMap<RequestType, ExecutorService> mapExecutorServices;
	private final EnumMap<RequestType, @Nullable ImageLoaderTask> mapTasks;
	private final ImageCache imageCache;

	public record TaskParameters(@Nullable Path path, RequestType requestType)
		{
		public TaskParameters()
		{
			this(null, IMMEDIATE);
		}
	}

	public enum RequestType
	{
		IMMEDIATE, READ_AHEAD;

		static void forEach(Consumer<RequestType> consumer)
		{
			Arrays.stream(values()).forEach(consumer);
		}
	}

	public record TaskResult(@Nullable Path path, @Nullable Image image, boolean state)
		{
		TaskResult()
		{
			this(null, null, true);
		}

		TaskResult(ImageContainer imageContainer, boolean state)
		{
			this(imageContainer.path(), imageContainer.image(), state);
		}
	}

	public ImageLoader()
	{
		this.mapPaths = new EnumMap<>(RequestType.class);
		this.mapExecutorServices = new EnumMap<>(RequestType.class);
		final var threadFactory = WorkerThreadFactory.createInstance(getClass().getName());
		RequestType.forEach(requestType -> mapExecutorServices.put(
			requestType, Executors.newSingleThreadExecutor(threadFactory)));
		this.mapTasks = new EnumMap<>(RequestType.class);
		this.imageCache = ImageCache.getInstance();
	}

	public void setOnResult(Consumer<TaskResult> onResult)
	{
		this.onResult = onResult;
	}

	@Override
	public void accept(TaskParameters taskParameters)
	{
		synchronized (this)
		{
			if (!closed)
			{
				final var pathRequested = taskParameters.path();
				final var requestType = taskParameters.requestType();
				mapPaths.put(requestType, pathRequested);
				final var imageContainerCached = imageCache.find(pathRequested).orElse(null);
				switch (requestType)
				{
					case IMMEDIATE ->
					{
						if (!Objects.equals(pathCurrent, pathRequested))
						{
							if (pathRequested != null)
							{
								if (imageContainerCached != null)
								{
									if (imageContainerCached.loaded())
									{
										deliverTaskResult(new TaskResult(imageContainerCached, true));
									}
								}
								else if (!mapTasks.containsKey(requestType))
								{
									submitTask(pathRequested, requestType);
								}
							}
							else
							{
								deliverTaskResult(new TaskResult());
							}
						}
					}
					case READ_AHEAD ->
					{
						if (!mapTasks.containsKey(requestType) && pathRequested != null && imageContainerCached == null)
						{
							submitTask(pathRequested, requestType);
						}
					}
					default -> throw new AssertionError(getClass().getName() +
							"::accept : Invalid RequestType: " + requestType);
				}
			}
		}
	}

	private synchronized boolean submitTask(Path path, RequestType requestType)
	{
		try
		{
			final var executorService = mapExecutorServices.get(requestType);
			if (executorService != null)
			{
				final var task = new ImageLoaderTask(path, requestType, this::handleResult);
				executorService.submit(task);
				mapTasks.put(requestType, task);
				imageCache.put(new ImageContainer(path, requestType));
				return true;
			}
			else
			{
				throw new IllegalStateException("ExecutorService is null");
			}
		}
		catch (RejectedExecutionException | IllegalStateException ex)
		{
			logger.log(WARNING, ex);
			return false;
		}
	}

	private record CacheRequestInfo(Optional<Path> pathRequested,
		Optional<ImageContainer> imageContainerCached,
		boolean requestInCache, boolean requestFulfilled)
		{
	}

	private synchronized CacheRequestInfo getCacheRequestInfo(RequestType requestType)
	{
		final var pathRequested = mapPaths.get(requestType);
		final Optional<ImageContainer> imageContainerCached = imageCache.find(pathRequested);
		final boolean requestInCache = imageContainerCached.isPresent();
		final boolean requestFulfilled = requestInCache && imageContainerCached.get().loaded();
		return new CacheRequestInfo(Optional.ofNullable(pathRequested),
			imageContainerCached, requestInCache, requestFulfilled);
	}

	private synchronized void handleResult(ImageContainer imageContainer)
	{
		if (!closed)
		{
			// handle result
			imageCache.put(imageContainer);
			mapTasks.remove(imageContainer.requestType());
			// check IMMEDIATE request
			if (!Objects.equals(pathCurrent, mapPaths.get(IMMEDIATE)))
			{
				final var crInfo = getCacheRequestInfo(IMMEDIATE);
				if (crInfo.pathRequested().isPresent())
				{
					if (crInfo.requestFulfilled())
					{
						deliverTaskResult(new TaskResult(crInfo.imageContainerCached().get(), true));
					}
					else
					{
						if (imageContainer.requestType() == IMMEDIATE)
						{
							deliverTaskResult(new TaskResult(imageContainer, false));
							if (!mapTasks.containsKey(IMMEDIATE) && !crInfo.requestInCache())
							{
								submitTask(crInfo.pathRequested().get(), IMMEDIATE);
							}
						}
					}
				}
				else
				{
					deliverTaskResult(new TaskResult());
				}
			}
			// check READ_AHEAD request
			if (!mapTasks.containsKey(READ_AHEAD))
			{
				final var crInfo = getCacheRequestInfo(READ_AHEAD);
				if (crInfo.pathRequested().isPresent() && !crInfo.requestInCache())
				{
					submitTask(crInfo.pathRequested().get(), READ_AHEAD);
				}
			}
		}
	}

	private synchronized void deliverTaskResult(TaskResult taskResult)
	{
		try
		{
			if (onResult != null)
			{
				onResult.accept(taskResult);
			}
			else
			{
				throw new IllegalStateException(getClass().getName() +
					"::deliverTaskResult : no result handler set");
			}
		}
		finally
		{
			pathCurrent = taskResult.path();
		}
	}

	@Override
	public void close()
	{
		synchronized (this)
		{
			if (!closed)
			{
				closed = true;
				mapExecutorServices.values().forEach(ExecutorService::shutdown);
				mapExecutorServices.values().forEach(executorService ->
				{
					boolean finished = false;
					while (!finished)
					{
						try
						{
							executorService.awaitTermination(TIMEOUT_SECONDS, SECONDS);
							finished = true;
						}
						catch (InterruptedException ex)
						{
						}
					}
				});
			}
		}
	}
}
