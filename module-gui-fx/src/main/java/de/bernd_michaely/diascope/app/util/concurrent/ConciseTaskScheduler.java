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
package de.bernd_michaely.diascope.app.util.concurrent;

import java.lang.System.Logger;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.System.Logger.Level.*;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.*;

/**
 * Class implementing a platform task scheduler which collects tasks to pass
 * them in a concise way to avoid to exhaust the platform queue.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public final class ConciseTaskScheduler implements AutoCloseable
{
	/**
	 * Default scheduling time in milliseconds.
	 */
	public static final int DEFAULT_SCHEDULING_TIME = 100;
	private static final Logger logger = System.getLogger(ConciseTaskScheduler.class.getName());
	private final int schedulingTime;
	private final Consumer<Deque<Runnable>> scheduledTasksConsumer;
	private final ScheduledExecutorService scheduledExecutorService;
	private @Nullable ScheduledFuture<?> scheduledFuture;
	private @Nullable Deque<Runnable> scheduledTasks;
	private volatile boolean closed = false;
	private volatile int counterScheduledTaskCreation;

	/**
	 * Creates a new instance. Scheduling time is
	 * {@link #DEFAULT_SCHEDULING_TIME}.
	 *
	 * @param scheduledTasksConsumer consumer for scheduled tasks
	 * @see #ConciseTaskScheduler(int, Consumer)
	 */
	public ConciseTaskScheduler(Consumer<Deque<Runnable>> scheduledTasksConsumer)
	{
		this(DEFAULT_SCHEDULING_TIME, scheduledTasksConsumer);
	}

	/**
	 * Creates a new instance. A simple task consumer implementation might look
	 * like:
	 * <pre>
	 * {@code Consumer<Deque<Runnable>> consumer =
	 *   tasks -> Platform.runLater(() -> tasks.forEach(Runnable::run));}
	 * </pre>
	 *
	 * @param schedulingTime         the scheduling time in milliseconds (a value
	 *                               less or equal to zero will be treated as
	 *                               {@link #DEFAULT_SCHEDULING_TIME})
	 * @param scheduledTasksConsumer consumer for scheduled tasks.
	 */
	public ConciseTaskScheduler(int schedulingTime,
		Consumer<Deque<Runnable>> scheduledTasksConsumer)
	{
		this.schedulingTime = schedulingTime > 0 ? schedulingTime : DEFAULT_SCHEDULING_TIME;
		this.scheduledTasksConsumer = requireNonNull(scheduledTasksConsumer,
			"scheduledTasksConsumer is null");
		this.scheduledExecutorService = Executors.newScheduledThreadPool(0,
			WorkerThreadFactory.createInstance(getClass().getName()));
	}

	/**
	 * Returns true, if this task scheduler is closed.
	 *
	 * @return true, if this task scheduler is closed
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * Returns the scheduling time in milliseconds.
	 *
	 * @return the scheduling time in milliseconds
	 */
	public int getSchedulingTime()
	{
		return schedulingTime;
	}

	/**
	 * Submits a new task. If this task scheduler is closed, the task is ignored.
	 *
	 * @param task the task to submit
	 * @return true, if the task was submitted
	 */
	public boolean submit(Runnable task)
	{
		synchronized (this)
		{
			final boolean result = !closed;
			if (result)
			{
				if (scheduledTasks == null)
				{
					scheduledTasks = new LinkedList<>();
				}
				scheduledTasks.addLast(task);
				if (scheduledFuture == null)
				{
					logger.log(TRACE, () ->
						" → creating new scheduled task #" + ++counterScheduledTaskCreation);
					scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
						this::exchange, 0, schedulingTime, MILLISECONDS);
				}
			}
			else
			{
				logger.log(WARNING, "Ignoring submitted task for closed " + getClass().getName());
			}
			return result;
		}
	}

	private void exchange()
	{
		synchronized (this)
		{
			try
			{
				final var tasks = scheduledTasks;
				scheduledTasks = null;
				if (tasks != null && !tasks.isEmpty())
				{
					if (scheduledTasksConsumer != null)
					{
						scheduledTasksConsumer.accept(tasks);
					}
				}
				else
				{
					if (scheduledFuture != null)
					{
						try
						{
							scheduledFuture.cancel(false);
						}
						finally
						{
							scheduledFuture = null;
						}
					}
				}
			}
			catch (RuntimeException ex)
			{
				// prevent executor service from being canceled
				logger.log(WARNING, getClass().getName() + "::exchange", ex);
			}
		}
	}

	/**
	 * Closes this task scheduler. Remaining tasks are executed, and this method
	 * waits blocking for the termination.
	 *
	 * @throws TimeoutException if closing is interrupted by timeout
	 */
	@Override
	public void close()
	{
		synchronized (this)
		{
			if (!closed)
			{
				try
				{
					if (scheduledFuture != null)
					{
						scheduledFuture.cancel(true);
						scheduledExecutorService.shutdownNow();
					}
				}
				finally
				{
					// process remaining tasks:
					exchange();
					closed = true;
				}
			}
		}
	}
}
