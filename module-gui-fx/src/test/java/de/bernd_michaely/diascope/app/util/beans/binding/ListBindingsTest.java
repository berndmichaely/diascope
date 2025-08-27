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

import de.bernd_michaely.diascope.app.util.beans.binding.ListBindings.ChainedObservableDoubleValues;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.util.beans.binding.ListBindings.*;
import static javafx.collections.FXCollections.observableArrayList;
import static org.junit.jupiter.api.Assertions.*;

/// ListBindings Test.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListBindingsTest
{
	private static class Item implements Comparable<Item>
	{
		private final DoubleProperty property;

		private Item(double value)
		{
			this.property = new SimpleDoubleProperty(value);
		}

		@Override
		public int compareTo(Item other)
		{
			return Double.compare(this.getValue(), other.getValue());
		}

		@Override
		public boolean equals(Object object)
		{
			if (object instanceof Item other)
			{
				return compareTo(other) == 0;
			}
			else
			{
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			return property.intValue();
		}

		private DoubleProperty valueProperty()
		{
			return property;
		}

		private double getValue()
		{
			return property.doubleValue();
		}

		@Override
		public String toString()
		{
			return "%.1f".formatted(getValue());
		}
	}

	private void printList(ObservableList<Item> list)
	{
		System.out.println("· input list: %s".formatted(list));
	}

	private void check(double expected, double actual)
	{
		System.out.println("  → expected: %.1f → actual: %.1f".formatted(expected, actual));
		assertEquals(expected, actual);
	}

	@Test
	public void test_identity_selector()
	{
		System.out.println("test_identity_selector");
		final ObservableList<DoubleProperty> list = observableArrayList();
		final var result = chainedObservableDoubleValues(list, Bindings::add, 0);
		check(0, result.get());
		list.add(new SimpleDoubleProperty(1));
		check(1, result.get());
		list.removeFirst();
		check(0, result.get());
		list.add(new SimpleDoubleProperty(2));
		check(2, result.get());
		list.clear();
		check(0, result.get());
	}

	@Test
	public void test_1_operand()
	{
		System.out.println("test_1_operand");
		final ObservableList<Item> list = observableArrayList();
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::add, 0);
		check(0, result.get());
		list.add(new Item(1));
		check(1, result.get());
		list.removeFirst();
		check(0, result.get());
		list.add(new Item(2));
		check(2, result.get());
		list.clear();
		check(0, result.get());
	}

	@Test
	public void test_operand_positionss()
	{
		System.out.println("test_2_operands");
		final ObservableList<Item> list = observableArrayList();
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::add, 0.0);
		check(0, result.get());
		//init:
		list.add(new Item(2));
		check(2, result.get());
		list.add(new Item(1));
		check(3, result.get());
		// add/remove @ first/middle/last:
		list.add(0, new Item(3));
		check(6, result.get());
		list.remove(0);
		check(3, result.get());
		list.add(1, new Item(3));
		check(6, result.get());
		list.remove(1);
		check(3, result.get());
		list.add(2, new Item(3));
		check(6, result.get());
		list.remove(2);
		check(3, result.get());
		// update @ first/middle/last:
		list.add(0, new Item(3));
		check(6, result.get());
		list.set(0, new Item(4));
		check(7, result.get());
		list.set(1, new Item(5));
		check(10, result.get());
		list.set(2, new Item(6));
		check(15, result.get());
		// permutate:
		list.sort(null);
		check(15, result.get());
	}

	@Test
	public void test_3_operands_add()
	{
		System.out.println("test_3_operands_add");
		final ObservableList<Item> list = observableArrayList();
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::add, -1);
		check(-1, result.get());
		list.add(new Item(1));
		check(1, result.get());
		list.add(new Item(2));
		check(3, result.get());
		list.add(new Item(3));
		check(6, result.get());
		list.set(0, new Item(4));
		check(9, result.get());
		list.set(1, new Item(5));
		check(12, result.get());
		list.set(2, new Item(6));
		check(15, result.get());
		list.remove(0);
		check(11, result.get());
		list.remove(0);
		check(6, result.get());
		list.remove(0);
		check(-1, result.get());
	}

	@Test
	public void test_3_operands_max()
	{
		System.out.println("test_3_operands_max");
		final ObservableList<Item> list = observableArrayList();
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::max, -1);
		check(-1, result.get());
		list.add(new Item(1));
		check(1, result.get());
		list.add(new Item(2));
		check(2, result.get());
		list.add(new Item(3));
		check(3, result.get());
		list.set(0, new Item(4));
		check(4, result.get());
		list.set(1, new Item(3));
		check(4, result.get());
		list.set(2, new Item(5));
		check(5, result.get());
		list.remove(2);
		check(4, result.get());
		list.add(new Item(6));
		check(6, result.get());
		list.remove(1);
		check(6, result.get());
		list.remove(1);
		check(4, result.get());
		list.set(0, new Item(0));
		check(0, result.get());
		list.remove(0);
		check(-1, result.get());
	}

	@Test
	public void testChainedObservableDoubleValues_add_remove_non_empty()
	{
		System.out.println("testChainedObservableDoubleValues_add_remove_non_empty");
		final ObservableList<Item> list = observableArrayList();
		double expected = 0;
		for (int i = 0; i < 16; i++)
		{
			final double value = 1 << i;
			list.add(new Item(value));
			expected += value;
		}
		printList(list);
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::add, -1);
		check(expected, result.get());
		for (int i = 15; i >= 0; i -= 2)
		{
			final double value = 1 << i;
			list.remove(i);
			expected -= value;
		}
		printList(list);
		list.clear();
		check(-1, result.get());
		printList(list);
	}

	@Test
	public void testChainedObservableDoubleValues_add_remove_empty()
	{
		System.out.println("testChainedObservableDoubleValues_add_remove_empty");
		final ObservableList<Item> list = observableArrayList();
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::add, -1);
		check(-1, result.get());
		double expected = 0;
		for (int i = 0; i < 16; i++)
		{
			final double value = 1 << i;
			list.add(new Item(value));
			expected += value;
		}
		printList(list);
		check(expected, result.get());
		for (int i = 14; i >= 0; i -= 2)
		{
			final double value = 1 << i;
			list.remove(i);
			expected -= value;
		}
		printList(list);
		list.clear();
		check(-1, result.get());
		printList(list);
	}

	@Test
	public void testChainedObservableDoubleValues_set()
	{
		System.out.println("testChainedObservableDoubleValues_set");
		final ObservableList<Item> list = observableArrayList();
		double expected = 0;
		for (int i = 0; i < 16; i++)
		{
			final double value = 1 << i;
			list.add(new Item(value));
			expected += value;
		}
		printList(list);
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::add, -1);
		check(expected, result.get());
		list.getFirst().valueProperty().set(5.7);
		expected += 4.7;
		check(expected, result.get());
	}

	@Test
	public void testChainedObservableDoubleValues_permutate()
	{
		System.out.println("testChainedObservableDoubleValues_permutate");
		final ObservableList<Item> list = observableArrayList();
		double expected = 0;
		for (int i = 15; i >= 0; i--)
		{
			final int value = 1 << i;
			list.add(new Item(value));
			expected += value;
		}
		printList(list);
		final var result = chainedObservableDoubleValues(list, Item::valueProperty, Bindings::add, -1);
		check(expected, result.get());
		// permutate:
		list.subList(1, 12).sort(null);
		printList(list);
		// some changes after permutate:
		final List<Item> list2 = new ArrayList<>();
		for (int i = 0; i < list.size(); i++)
		{
			list2.add(new Item(list.get(i).getValue() - 0.5));
		}
		for (int i = 0; i < 16; i++)
		{
			final double oldValue = list.remove(i).getValue();
			final Item newValue = list2.get(i);
			System.out.println("→ update item [%d] after permutation from %.1f to %.1f:"
				.formatted(i, oldValue, newValue.getValue()));
			list.add(i, newValue);
			printList(list);
			expected -= 0.5;
			check(expected, result.get());
		}
	}

	@Test
	public void test_prefix_sum()
	{
		System.out.println("test_prefix_sum");
		final ObservableList<DoubleProperty> list = observableArrayList();
		final var result = new ChainedObservableDoubleValues<DoubleProperty>(list, p -> p, Bindings::add, 0.0);
		final int num = 100;
		// add 0..99
		for (int i = 0; i < num; i++)
		{
			list.add(new SimpleDoubleProperty(i));
			result.getIntermediateResult(i);
		}
		double sum = 0.0;
		for (int i = 0; i < num; i++, sum += i)
		{
			System.out.print("· %2d : ".formatted(i));
			check(sum, result.getIntermediateResult(i));
		}
		sum = result.getfinalResult();
		check((num - 1) * num / 2, sum);
		// add 99..0
		double sum_imr = 0.0;
		for (int i = 0; i < num; i++)
		{
			final var property = list.get(i);
			sum_imr += num;
			double delta = num - i;
			sum += delta;
			property.set(property.get() + delta);
			System.out.print("· %2d : ".formatted(i));
			check(sum_imr, result.getIntermediateResult(i));
			System.out.print("→ %2d : ".formatted(i));
			check(sum, result.getfinalResult());
		}
		check(num * num, sum);
		// sub 0..99
		sum_imr = 0.0;
		for (int i = 0; i < num; i++)
		{
			final var property = list.get(i);
			property.set(property.get() - i);
			sum_imr += num - i;
			sum -= i;
			System.out.print("· %2d : ".formatted(i));
			check(sum_imr, result.getIntermediateResult(i));
			System.out.print("→ %2d : ".formatted(i));
			check(sum, result.getfinalResult());
		}
		check(num * (num + 1) / 2, sum);
		// remove 0..99
		for (int i = 0; i < num; i++)
		{
			final int delta = num - i;
			sum -= delta;
			System.out.print("· %2d : ".formatted(i));
			check(delta, list.removeFirst().get());
			System.out.print("→ %2d : ".formatted(i));
			check(sum, result.getfinalResult());
		}
		check(0.0, result.getfinalResult());
	}
}
