/*
 * Copyright 2021 Bernd Michaely (info@bernd-michaely.de).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bernd_michaely.common.desktop.fx.collections.selection;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for SelectableListImpl class.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SelectableListImplTest
{
	/**
	 * Test selection listener related functionality of class SelectableListImpl.
	 */
	@Test
	public void testSelectableListImpl()
	{
		System.out.println("test SelectableListImpl");

		final SelectableList<Integer> list = SelectableListFactory.selectableList();
		assertEquals(0, list.size());
		assertEquals(0, list.getNumSelected());

		System.out.println("test SelectionChangeListener");

		final var listener = new SelectionChangeListener<Integer>()
		{
			private final List<SelectionChange<? extends Integer>> changes = new ArrayList<>();

			private void reset()
			{
				changes.clear();
			}

			@Override
			public void onSelectionChanged(SelectionChange<? extends Integer> change)
			{
				changes.add(change);
			}
		};
		assertThrows(NullPointerException.class, () -> list.addSelectionListener(null));
		list.addSelectionListener(listener);
		for (int i = 10; i >= 0; i--)
		{
			list.add(i);
		}
		for (int i = 9; i >= 1; i -= 2)
		{
			list.setSelected(i, true);
		}
		assertEquals(5, listener.changes.size());
		for (int i = 0; i < listener.changes.size(); i++)
		{
			final var change = listener.changes.get(i);
			final int index = 9 - 2 * i;
			assertFalse(change.isEmptyRange());
			assertTrue(change.getSelectionChangeType().isSingleChange());
			assertEquals(index, change.getFrom());
			assertEquals(index, change.getTo());
			assertTrue(change.toString().contains("" + index));
			assertEquals(list, change.getList());
		}
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertEquals(list.isSelected(i), i % 2 == 1);
		}

		System.out.println("test empty selection range");
		listener.reset();
		assertEquals(0, listener.changes.size());
		list.selectRange(0, 10, null);
		assertEquals(0, listener.changes.size());
		list.selectRange(0, 0, Selectable.Action.SELECTION_SET);

		System.out.println("test remove");
		listener.reset();
		list.remove(2, 10);
		assertEquals(3, list.size());
		assertEquals(1, list.getNumSelected());

		assertEquals(10, list.get(0));
		assertEquals(false, list.isSelected(0));
		assertEquals(9, list.get(1));
		assertEquals(true, list.isSelected(1));
		assertEquals(0, list.get(2));
		assertEquals(false, list.isSelected(2));

		assertEquals(4, listener.changes.size());

		for (int i = 0; i < listener.changes.size(); i++)
		{
			final var change = listener.changes.get(i);
			final int index = 2;
			assertFalse(change.isEmptyRange());
			assertEquals(index, change.getFrom());
			assertEquals(index, change.getTo());
			assertEquals(list, change.getList());
		}

		System.out.println("test clear");
		list.clear();
		assertEquals(0, list.size());
		assertEquals(0, list.getNumSelected());

		System.out.println("test removeSelectionListener");
		assertTrue(list.removeSelectionListener(listener));
	}

	/**
	 * Test of beginSelectionChange and endSelectionChange methods of class
	 * SelectableListImpl.
	 */
	@Test
	public void testBeginEndSelectionChange()
	{
		System.out.println("test [begin|end]SelectionChange");

		final SelectableList<Integer> list = SelectableListFactory.selectableList();
		assertEquals(0, list.size());
		assertEquals(0, list.getNumSelected());

		System.out.println("test SelectionChangeListener");

		final var listener = new SelectionChangeListener<Integer>()
		{
			private final List<SelectionChangeListener.SelectionChange<? extends Integer>> changes = new ArrayList<>();

			private void reset()
			{
				changes.clear();
			}

			@Override
			public void onSelectionChanged(SelectionChangeListener.SelectionChange<? extends Integer> change)
			{
				changes.add(change);
			}
		};
		assertThrows(NullPointerException.class, () -> list.addSelectionListener(null));
		list.addSelectionListener(listener);
		assertEquals(0, list.size());
		assertEquals(0, list.getNumSelected());
		list.beginSelectionChange();
		try
		{
			for (int i = 10; i >= 0; i--)
			{
				list.add(i);
			}
			for (int i = 9; i >= 1; i -= 2)
			{
				list.setSelected(i, true);
			}
		}
		finally
		{
			list.endSelectionChange();
		}
		assertEquals(1, listener.changes.size());
		final var change = listener.changes.get(0);
		System.out.println(change);

		assertEquals(11, list.size());
		assertEquals(5, list.getNumSelected());
		assertFalse(change.isEmptyRange());
		assertEquals(1, change.getFrom());
		assertEquals(9, change.getTo());
		assertEquals(list, change.getList());
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertEquals(list.isSelected(i), i % 2 == 1);
		}

		System.out.println("test remove");
		listener.reset();
		list.beginSelectionChange();
		try
		{
			list.selectRangeBidirectional(2, 10, false);
		}
		finally
		{
			list.endSelectionChange();
		}
		list.remove(2, 10);
		assertEquals(3, list.size());

		assertEquals(10, list.get(0));
		assertEquals(false, list.isSelected(0));
		assertEquals(9, list.get(1));
		assertEquals(true, list.isSelected(1));
		assertEquals(0, list.get(2));
		assertEquals(false, list.isSelected(2));

		assertEquals(1, listener.changes.size());
		assertEquals(3, listener.changes.get(0).getFrom());
		assertEquals(9, listener.changes.get(0).getTo());
		assertEquals(1, list.getNumSelected());

		System.out.println("test removeSelectionListener");
		assertTrue(list.removeSelectionListener(listener));
	}

	/**
	 * Test range selection methods of class SelectableListImpl.
	 */
	@Test
	public void testSelectRange()
	{
		System.out.println("test selectRange");

		final SelectableList<Integer> list = SelectableListFactory.selectableList();
		assertEquals(0, list.size());
		assertEquals(0, list.getNumSelected());

		System.out.println("test SelectionChangeListener");

		final var listener = new SelectionChangeListener<Integer>()
		{
			private final List<SelectionChangeListener.SelectionChange<? extends Integer>> changes = new ArrayList<>();

			private void reset()
			{
				changes.clear();
			}

			@Override
			public void onSelectionChanged(SelectionChangeListener.SelectionChange<? extends Integer> change)
			{
				changes.add(change);
			}
		};
		assertThrows(NullPointerException.class, () -> list.addSelectionListener(null));
		list.addSelectionListener(listener);
		for (int i = 10; i >= 0; i--)
		{
			list.add(i);
		}
		for (int i = 9; i >= 1; i -= 2)
		{
			list.setSelected(i, true);
		}
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertEquals(list.isSelected(i), i % 2 == 1);
		}

		System.out.println("test invertSelection");
		listener.reset();
		list.invertSelection();
		assertEquals(1, listener.changes.size());
		final var changeInvertSelection = listener.changes.get(0);
		assertFalse(changeInvertSelection.isEmptyRange());
		assertEquals(0, changeInvertSelection.getFrom());
		assertEquals(10, changeInvertSelection.getTo());
		assertEquals(list, changeInvertSelection.getList());
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertEquals(list.isSelected(i), i % 2 == 0);
		}

		System.out.println("test selectNone");

		// invert back
		list.invertSelection();
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertEquals(list.isSelected(i), i % 2 == 1);
		}
		listener.reset();
		list.selectNone();
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertFalse(list.isSelected(i));
		}
		assertEquals(1, listener.changes.size());
		final var changeSelectNone = listener.changes.get(0);
		assertFalse(changeSelectNone.isEmptyRange());
		assertEquals(1, changeSelectNone.getFrom());
		assertEquals(9, changeSelectNone.getTo());
		assertEquals(list, changeSelectNone.getList());

		System.out.println("test selectAll");

		listener.reset();
		list.selectAll();
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertTrue(list.isSelected(i));
		}
		assertEquals(1, listener.changes.size());
		final var changeSelectAll = listener.changes.get(0);
		assertFalse(changeSelectAll.isEmptyRange());
		assertEquals(0, changeSelectAll.getFrom());
		assertEquals(10, changeSelectAll.getTo());
		assertEquals(list, changeSelectAll.getList());

		System.out.println("test selectRange");

		listener.reset();
		list.selectRange(5, 8, Selectable.Action.SELECTION_TOGGLE);
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertEquals(i < 5 || i > 7, list.isSelected(i));
		}
		assertEquals(1, listener.changes.size());
		final var changeSelectRange = listener.changes.get(0);
		assertFalse(changeSelectRange.isEmptyRange());
		assertEquals(5, changeSelectRange.getFrom());
		assertEquals(7, changeSelectRange.getTo());
		assertEquals(list, changeSelectRange.getList());

		System.out.println("test selectRangeBidirectional");

		listener.reset();
		list.selectRangeBidirectional(9, 3, false);
		for (int i = 0; i <= 10; i++)
		{
			assertEquals(10 - i, list.get(i));
			assertEquals(i < 3 || i > 9, list.isSelected(i));
		}
		assertEquals(1, listener.changes.size());
		final var changeSelectRangeBidirectional = listener.changes.get(0);
		assertFalse(changeSelectRangeBidirectional.isEmptyRange());
		assertEquals(3, changeSelectRangeBidirectional.getFrom());
		assertEquals(9, changeSelectRangeBidirectional.getTo());
		assertEquals(list, changeSelectRangeBidirectional.getList());
	}

	@Test
	public void testRemoveSelected()
	{
		System.out.println("test removeSelected");

		final var items = List.of("a", "b", "c", "d", "e", "f", "g");
		final int n = items.size();
		final int n_1 = n - 1;
		final SelectableList<String> list = SelectableListFactory.selectableList(items);
		assertEquals(n, list.size());
		assertEquals(0, list.getNumSelected());
		list.selectAll();
		list.setSelected(list.size() - 1, false);
		assertEquals(n_1, list.getNumSelected());
		assertEquals(items.getLast(), list.removeLast());
		assertEquals(n_1, list.getNumSelected());

		list.setAll(items);
		assertEquals(n, list.size());
		assertEquals(0, list.getNumSelected());
		list.selectAll();
		assertEquals(n, list.getNumSelected());
		assertEquals(items.getLast(), list.removeLast());
		assertEquals(n_1, list.getNumSelected());
	}
}
