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

import de.bernd_michaely.diascope.app.util.concurrent.ConciseTaskScheduler;
import de.bernd_michaely.diascope.app.util.concurrent.WorkerThreadFactory;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.concurrent.WorkerThreadFactory.TIMEOUT_SECONDS;
import static java.util.concurrent.TimeUnit.*;

/**
 * Concurrency class for loading image directories.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageDirectoryReader implements Consumer<Path>, AutoCloseable
{
	private static final Logger logger = System.getLogger(ImageDirectoryReader.class.getName());
	private final ObservableList<ImageGroupDescriptor> listItems;
	private final ProgressControl progressControl;
	private final ConciseTaskScheduler platformScheduler;
	private final ExecutorService executorService;
	private volatile @Nullable ImageDirectoryReaderTask taskCurrent;
	private volatile boolean stateNext_Active;
	private volatile @Nullable Path stateNext_Directory;

	ImageDirectoryReader(ObservableList<ImageGroupDescriptor> listItems,
		ProgressControl progressControl)
	{
		this.listItems = listItems;
		this.progressControl = progressControl;
		final Consumer<Deque<Runnable>> consumer = tasks -> Platform.runLater(() ->
		{
			for (Runnable r = tasks.pollFirst(); r != null; r = tasks.pollFirst())
			{
				r.run();
			}
		});
		this.platformScheduler = new ConciseTaskScheduler(consumer);
		this.executorService = Executors.newSingleThreadExecutor(
			WorkerThreadFactory.createInstance(getClass().getName()));
	}

	private void createNewTask(@Nullable Path directory)
	{
		synchronized (this)
		{
			taskCurrent = new ImageDirectoryReaderTask(listItems,
				progressControl, platformScheduler, directory, this::onCurrentTaskFinish);
			executorService.submit(taskCurrent);
		}
	}

	@Override
	public void accept(@Nullable Path directory)
	{
		synchronized (this)
		{
			if (taskCurrent == null)
			{
				createNewTask(directory);
			}
			else
			{
				try
				{
					taskCurrent.cancel();
				}
				finally
				{
					stateNext_Directory = directory;
					stateNext_Active = true;
				}
			}
		}
	}

	private void onCurrentTaskFinish()
	{
		synchronized (this)
		{
			taskCurrent = null;
			if (stateNext_Active)
			{
				try
				{
					createNewTask(stateNext_Directory);
				}
				finally
				{
					stateNext_Active = false;
				}
			}
		}
	}

	@Override
	public void close()
	{
		synchronized (this)
		{
			stateNext_Active = false;
			stateNext_Directory = null;
			if (taskCurrent != null)
			{
				taskCurrent.cancel();
				taskCurrent = null;
			}
			try (platformScheduler)
			{
				executorService.shutdown();
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
			}
		}
	}
}
