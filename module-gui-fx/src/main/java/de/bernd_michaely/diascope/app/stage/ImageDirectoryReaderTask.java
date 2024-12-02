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
package de.bernd_michaely.diascope.app.stage;

import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.util.concurrent.ConciseTaskScheduler;
import de.bernd_michaely.diascope.app.util.concurrent.WorkerThreadFactory;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import javafx.collections.ObservableList;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.stage.ProgressControl.*;
import static de.bernd_michaely.diascope.app.util.concurrent.WorkerThreadFactory.TIMEOUT_SECONDS;
import static java.lang.System.Logger.Level.*;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.TimeUnit.*;

/**
 * Task for ImageDirectoryReader.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageDirectoryReaderTask implements Callable<@Nullable Object>
{
	private static final Logger logger = System.getLogger(ImageDirectoryReaderTask.class.getName());
	private final ObservableList<ImageGroupDescriptor> listItems;
	private final ProgressControl progressControl;
	private final ConciseTaskScheduler platformScheduler;
	private final @Nullable Path directory;
	private final Runnable onCurrentTaskFinish;
	private final CompletionService<ImageGroupDataUpdater.Result> completionService;
	private final ExecutorService executorService;
	private volatile boolean cancelled;
	private volatile @Nullable Thread thread;

	static final DirectoryStream.Filter<Path> imageFilter = new DirectoryStream.Filter<Path>()
	{
		private static final Set<String> imageFileNameExtensions =
			Set.of("jpg", "jpeg", "png", "bmp", "gif");

		@Override
		public boolean accept(Path path) throws IOException
		{
			if (path != null)
			{
				if (Files.isRegularFile(path))
				{
					final Path fileName = path.getFileName();
					if (fileName != null)
					{
						final String strFileName = fileName.toString().toLowerCase();
						final String ext = strFileName.substring(strFileName.lastIndexOf(".") + 1);
						return imageFileNameExtensions.contains(ext);
					}
				}
			}
			return false;
		}
	};

	ImageDirectoryReaderTask(ObservableList<ImageGroupDescriptor> listItems,
		ProgressControl progressControl, ConciseTaskScheduler platformScheduler,
		@Nullable Path directory, Runnable onCurrentTaskFinish)
	{
		this.listItems = listItems;
		this.progressControl = progressControl;
		this.platformScheduler = platformScheduler;
		this.directory = directory;
		this.onCurrentTaskFinish = onCurrentTaskFinish;
		final boolean useMultipleCores = ApplicationConfiguration.getState().isStartedInDevelopmentMode();
//		final boolean useMultipleCores = false;
//		final boolean useMultipleCores = true;
		final int poolSizeMin = 1;
		final int poolSize = useMultipleCores ? Math.max(poolSizeMin,
			(int) (Runtime.getRuntime().availableProcessors() * 0.75)) : poolSizeMin;
		logger.log(TRACE, "ThreadPool size: " + poolSize);
		this.executorService = Executors.newFixedThreadPool(
			poolSize, WorkerThreadFactory.createInstance(getClass().getName()));
		this.completionService = new ExecutorCompletionService<>(this.executorService);
	}

	@Override
	public @Nullable
	Object call() throws TimeoutException
	{
		try
		{
			platformScheduler.submit(() ->
			{
				progressControl.accept(PROGRESS_BEGIN);
				listItems.clear();
			});
			readDirectory();
		}
		finally
		{
			executorService.shutdown();
			platformScheduler.submit(() -> progressControl.accept(PROGRESS_END));
			if (onCurrentTaskFinish != null)
			{
				onCurrentTaskFinish.run();
			}
			try
			{
				executorService.awaitTermination(TIMEOUT_SECONDS, SECONDS);
			}
			catch (InterruptedException ex)
			{
				throw new TimeoutException(ex.toString());
			}
		}
		return null;
	}

	private void readDirectory()
	{
		if (directory != null)
		{
			try (final DirectoryStream<Path> paths = Files.newDirectoryStream(directory, imageFilter))
			{
				final SortedSet<Path> entries = new TreeSet<>();
				final Iterator<Path> iterator = paths.iterator();
				while (!cancelled && iterator.hasNext())
				{
					entries.add(iterator.next());
				}
				if (!cancelled)
				{
					readMetadata(unmodifiableList(entries.stream().map(ImageGroupDescriptor::new).toList()));
				}
			}
			catch (IOException ex)
			{
				logger.log(WARNING, ex);
			}
		}
		else
		{
			platformScheduler.submit(progressControl::runOnProgressZero);
		}
	}

	private void readMetadata(List<ImageGroupDescriptor> entries)
	{
		final int n = entries.size();
		platformScheduler.submit(() ->
		{
			listItems.addAll(entries);
			progressControl.runOnProgressZero();
			progressControl.accept(0.0);
		});
		for (int i = 0; i < n; i++)
		{
			completionService.submit(new ImageGroupDataUpdater(entries.get(i).getPath(), i));
		}
		thread = Thread.currentThread();
		final double size = n;
		for (int i = 1; !cancelled && i <= n; i++)
		{
			try
			{
				final ImageGroupDataUpdater.Result result = completionService.take().get();
				final var image = result.imageMetadata().getThumbnail();
				final int index = result.mainListIndex();
				final double progress = i / size;
				platformScheduler.submit(() ->
				{
					final var imageGroupDescriptor = listItems.get(index);
					imageGroupDescriptor.setThumbnail(image);
					listItems.set(index, imageGroupDescriptor);
					progressControl.accept(progress);
				});
			}
			catch (InterruptedException ex)
			{
			}
			catch (ExecutionException ex)
			{
//				cancelled = true;
				logger.log(WARNING, ex);
			}
		}
	}

	void cancel()
	{
		cancelled = true;
		executorService.shutdownNow();
		if (thread != null)
		{
			thread.interrupt();
		}
	}
}
