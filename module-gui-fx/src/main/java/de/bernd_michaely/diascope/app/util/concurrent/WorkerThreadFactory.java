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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread factory for demon worker threads
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class WorkerThreadFactory
{
	public static final int TIMEOUT_SECONDS = 10;

	private WorkerThreadFactory()
	{
	}

	private static class ThreadFactoryImpl implements ThreadFactory
	{
		private final String namePrefix;
		private final AtomicLong counterThread;

		private ThreadFactoryImpl(String namePrefix, long numStart)
		{
			this.namePrefix = namePrefix;
			this.counterThread = new AtomicLong(numStart);
		}

		@Override
		public Thread newThread(Runnable runnable)
		{
			final var threadName = namePrefix + "-" + counterThread.getAndIncrement();
			final var thread = new Thread(runnable, threadName);
			thread.setDaemon(true);
			return thread;
		}
	}

	public static ThreadFactory createInstance(String namePrefix)
	{
		return createInstance(namePrefix, 1);
	}

	public static ThreadFactory createInstance(String namePrefix, long numStart)
	{
		return new ThreadFactoryImpl(namePrefix, numStart);
	}
}
