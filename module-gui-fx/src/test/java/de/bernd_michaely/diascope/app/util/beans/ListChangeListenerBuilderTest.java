/*
 * Copyright (C) 2025 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.util.beans;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import static javafx.collections.FXCollections.observableArrayList;
import static org.junit.jupiter.api.Assertions.*;

/// ListChangeListenerBuilder Test.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListChangeListenerBuilderTest
{
	private static class ListenerTest<T>
	{
		private int counterAdd, counterRemove, counterUpdate, counterPermutate;

		private void onAdd(Change<? extends T> change)
		{
			counterAdd++;
		}

		private void onRemove(Change<? extends T> change)
		{
			counterRemove++;
		}

		private void onUpdate(Change<? extends T> change)
		{
			counterUpdate++;
		}

		private void onPermutate(Change<? extends T> change)
		{
			counterPermutate++;
		}

		private void reset()
		{
			counterAdd = 0;
			counterRemove = 0;
			counterUpdate = 0;
			counterPermutate = 0;
		}

		private void checkCounters(int counterAdd, int counterRemove, int counterUpdate, int counterPermutate)
		{
			assertEquals(counterAdd, this.counterAdd);
			assertEquals(counterRemove, this.counterRemove);
			assertEquals(counterUpdate, this.counterUpdate);
			assertEquals(counterPermutate, this.counterPermutate);
			reset();
		}
	}

	private void _test_replaceAsUpdate(boolean replaceAsUpdate)
	{
		final var listenerTest = new ListenerTest<String>();
		final ObservableList<String> list = observableArrayList();
		final var listener = new ListChangeListenerBuilder<String>()
			.onAdd(listenerTest::onAdd)
			.onRemove(listenerTest::onRemove)
			.onUpdate(listenerTest::onUpdate)
			.onPermutate(listenerTest::onPermutate)
			.replaceAsUpdate(replaceAsUpdate)
			.build();
		list.addListener(listener);
		listenerTest.checkCounters(0, 0, 0, 0);
		list.addAll("e");
		list.addAll("a");
		list.addAll("d");
		list.addAll("c");
		list.addAll("b");
		listenerTest.checkCounters(5, 0, 0, 0);
		list.remove("a");
		list.remove("b");
		listenerTest.checkCounters(0, 2, 0, 0);
		list.set(0, "a");
		if (replaceAsUpdate)
		{
			listenerTest.checkCounters(0, 0, 1, 0);
		}
		else
		{
			listenerTest.checkCounters(1, 1, 0, 0);
		}
		list.sort(null);
		listenerTest.checkCounters(0, 0, 0, 1);
	}

	@Test
	public void test_no_replaceAsUpdate()
	{
		System.out.println("test_no_replaceAsUpdate");
		_test_replaceAsUpdate(false);
	}

	@Test
	public void test_replaceAsUpdate()
	{
		System.out.println("test_replaceAsUpdate");
		_test_replaceAsUpdate(true);
	}
}
