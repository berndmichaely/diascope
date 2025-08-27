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
package de.bernd_michaely.diascope.app.util.beans.binding;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.util.beans.binding.ListBindings.chainedObservableDoubleValues;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ListBindings Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ListBindingsTest
{
	private static class Item
	{
		private final DoubleProperty value;
		private final DoubleProperty result;

		private Item(double value)
		{
			this.value = new SimpleDoubleProperty(value);
			this.result = new SimpleDoubleProperty();
		}

		private DoubleProperty getValue()
		{
			return value;
		}

		private DoubleProperty getResult()
		{
			return result;
		}

		@Override
		public String toString()
		{
			return "[%.1f ~ (%.1f)]".formatted(value.get(), result.get());
		}
	}

	private void printList(ObservableList<Item> list, boolean verbose)
	{
		if (verbose)
		{
			System.out.println("· input list: [\n%s]".formatted(list.stream()
				.map(Item::toString).collect(joining(",\n"))));
		}
		else
		{
			System.out.println("· input list: [%s]".formatted(list.stream()
				.map(Item::getValue).map(DoubleProperty::get)
				.map(d -> "%.1f".formatted(d))
				.collect(joining(", "))));
		}
	}

	private void check(double expected, double actual)
	{
		System.out.println("expected: %.1f → actual: %.1f".formatted(expected, actual));
		assertEquals(expected, actual);
	}

	@Test
	public void testChainedObservableDoubleValues_non_empty()
	{
		System.out.println("testChainedObservableDoubleValues_non_empty");
		final ObservableList<Item> list = FXCollections.observableArrayList();
		double expected = 0;
		for (int i = 0; i < 16; i++)
		{
			final double value = 1 << i;
			list.add(new Item(value));
			expected += value;
		}
		printList(list, false);
		final var result = chainedObservableDoubleValues(
			list, Item::getValue, Item::getResult, Bindings::add, -1);
		check(expected, result.get());
		for (int i = 15; i >= 0; i -= 2)
		{
			final double value = 1 << i;
			list.remove(i);
			expected -= value;
		}
		printList(list, true);
		list.clear();
		check(-1, result.get());
		printList(list, true);
	}

	@Test
	public void testChainedObservableDoubleValues_empty()
	{
		System.out.println("testChainedObservableDoubleValues_empty");
		final ObservableList<Item> list = FXCollections.observableArrayList();
		final var result = chainedObservableDoubleValues(
			list, Item::getValue, Item::getResult, Bindings::add, -1);
		check(-1, result.get());
		double expected = 0;
		for (int i = 0; i < 16; i++)
		{
			final double value = 1 << i;
			list.add(new Item(value));
			expected += value;
		}
		printList(list, true);
		check(expected, result.get());
		for (int i = 14; i >= 0; i -= 2)
		{
			final double value = 1 << i;
			list.remove(i);
			expected -= value;
		}
		printList(list, true);
		list.clear();
		check(-1, result.get());
		printList(list, true);
	}
}
