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

import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.util.concurrent.ConciseTaskScheduler.*;
import static java.util.concurrent.TimeUnit.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ConciseTaskScheduler.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ConciseTaskSchedulerTest
{
	private ScheduledFuture<?> scheduledFuture;
	private volatile int numCalls;
	private volatile int numTasks;

	private static class TestValue implements Runnable
	{
		private final int value;

		private TestValue(int value)
		{
			this.value = value;
		}

		@Override
		public void run()
		{
		}

		@Override
		public String toString()
		{
			return "" + value;
		}
	}

	private void _test_constructor(ConciseTaskScheduler taskScheduler) throws TimeoutException
	{
		try (taskScheduler)
		{
			assertFalse(taskScheduler.isClosed());
			assertEquals(DEFAULT_SCHEDULING_TIME, taskScheduler.getSchedulingTime());
		}
		assertTrue(taskScheduler.isClosed());
	}

	@Test
	public void testConstructor() throws TimeoutException
	{
		assertThrows(NullPointerException.class, () -> new ConciseTaskScheduler(1, null));
		final Consumer<Deque<Runnable>> consumer = deque ->
		{
		};
		_test_constructor(new ConciseTaskScheduler(consumer));
		_test_constructor(new ConciseTaskScheduler(0, consumer));
	}

	@Test
	public void testTransferFast() throws TimeoutException
	{
		final int delay = 1;
		final int schedulingTime = 10 * delay;
		testTransfer(1000, delay, schedulingTime);
	}

	@Test
	public void testTransferSlow() throws TimeoutException
	{
		final int delay = 20;
		final int schedulingTime = delay / 4;
		testTransfer(50, delay, schedulingTime);
	}

	@Test
	public void testTransferSlow2() throws TimeoutException
	{
		final int delay = 20;
		final int schedulingTime = delay - 1;
		testTransfer(50, delay, schedulingTime);
	}

	@Test
	public void testTransferSlow3() throws TimeoutException
	{
		final int delay = 20;
		final int schedulingTime = delay + 1;
		testTransfer(50, delay, schedulingTime);
	}

	private void testTransfer(int numLoops, int delay, int schedulingTime)
	{
		System.out.println("start testing transfer…");
		final var threadFactory = WorkerThreadFactory.createInstance(getClass().getSimpleName());
		final var executorService = Executors.newScheduledThreadPool(1, threadFactory);
		final AtomicInteger counter = new AtomicInteger(1);
		final var countDownLatch = new CountDownLatch(1);
		final Consumer<Deque<Runnable>> consumer = tasks ->
		{
			final int n = tasks.size();
			System.out.println(" → transfer task list " + tasks + " of size " + n);
			numCalls++;
			numTasks += n;
		};
		final var taskScheduler = new ConciseTaskScheduler(schedulingTime, consumer);
		try (taskScheduler)
		{
			scheduledFuture = executorService.scheduleAtFixedRate(() ->
			{
				final int n = counter.getAndIncrement();
				System.out.println("scheduler call #" + n);
				if (n <= numLoops)
				{
					assertTrue(taskScheduler.submit(new TestValue(n)));
					if (n == numLoops)
					{
						scheduledFuture.cancel(false);
						countDownLatch.countDown();
					}
				}
			}, delay, delay, MILLISECONDS);
			boolean finishedByTimeout = false;
			boolean isFinished = false;
			while (!isFinished)
			{
				try
				{
					finishedByTimeout = !countDownLatch.await(numLoops * delay + 5000, MILLISECONDS);
					isFinished = true;
				}
				catch (InterruptedException ex)
				{
					System.err.println(ex);
				}
			}
			assertFalse(finishedByTimeout, "finished by timeout");
		}
		finally
		{
			System.out.println("…end testing transfer.");
		}
		assertTrue(taskScheduler.isClosed());
		assertFalse(taskScheduler.submit(new TestValue(0)));
		assertEquals(numLoops, numTasks);
		final int sTime = taskScheduler.getSchedulingTime();
		assertEquals(schedulingTime, sTime);
		final int numCallsScheduler = numLoops * delay / sTime;
		System.out.println("numCalls: %d (%d)".formatted(numCalls, numCallsScheduler));
	}
}
