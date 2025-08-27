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
package de.bernd_michaely.diascope.app.image;

import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableList;
import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableListFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// ListDualSelection Test.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListDualSelectionTest
{
	private SelectableList<String> list;
	private ListDualSelection<String> selection;

	@BeforeEach
	public void setUp()
	{
		list = SelectableListFactory.selectableList();
		// NOTE: the tests expect, no null and no duplicate elements are added to list
		selection = new ListDualSelection<>(list);
	}

	@AfterEach
	public void tearDown()
	{
		list = null;
		selection = null;
	}

	private enum Action
	{
		ADD, DEL, SEL
	}

	private void _add(String item, String singleItemSelected, String first, String second)
	{
		list.add(item);
		_check(Action.ADD, -1, false, item, singleItemSelected, first, second);
	}

	private void _del(String item, String singleItemSelected, String first, String second)
	{
		list.remove(item);
		_check(Action.DEL, -1, false, item, singleItemSelected, first, second);
	}

	private void _sel(int index, boolean select, String singleItemSelected, String first, String second)
	{
		list.setSelected(index, select);
		_check(Action.SEL, index, select, null, singleItemSelected, first, second);
	}

	private void _check(Action action, int index, boolean select,
		String item, String singleItem, String first, String second)
	{
		final boolean singleItemSelected = singleItem != null;
		final boolean dualItemsSelected = first != null && second != null;
		final String w0 = singleItem != null ? "»%s«".formatted(singleItem) : "–––";
		final String w1 = first != null ? "»%s«".formatted(first) : "–––";
		final String w2 = second != null ? "»%s«".formatted(second) : "–––";
		if (action != null)
		{
			if (action == Action.SEL)
			{
				System.out.println("→ %s [%d]%s    → %s → { %s / %s } : %s"
					.formatted(action, index, (select ? "+" : "-"), w0, w1, w2, list));
			}
			else
			{
				System.out.println("· %s »%s«     → %s → { %s / %s } : %s"
					.formatted(action, item, w0, w1, w2, list));
			}
		}
		else
		{
			System.out.println("· %s         → %s → { %s / %s } : %s"
				.formatted("–––", w0, w1, w2, list));
		}
		assertEquals(singleItemSelected, selection.singleItemSelectedProperty().getValue());
		assertEquals(singleItemSelected, selection.singleSelectionItemProperty().get().isPresent());
		assertEquals(singleItem, selection.singleSelectionItemProperty().get().orElse(null));
		assertEquals(dualItemsSelected, selection.dualItemsSelectedProperty().getValue());
		assertEquals(dualItemsSelected, selection.dualSelectionFirstItemProperty().get().isPresent());
		assertEquals(dualItemsSelected, selection.dualSelectionSecondItemProperty().get().isPresent());
		assertEquals(first, selection.dualSelectionFirstItemProperty().get().orElse(null));
		assertEquals(second, selection.dualSelectionSecondItemProperty().get().orElse(null));
	}

	@Test
	public void test_Selection_1()
	{
		System.out.println("test_Selection_1");
		//
		_check(null, -1, false, null, null, null, null);
		//
		_add("a", null, null, null);
		//
		_add("b", null, "a", "b");
		//
		_add("c", null, null, null);
	}

	@Test
	public void test_Selection_2()
	{
		System.out.println("test_Selection_2");
		//
		_check(null, -1, false, null, null, null, null);
		//
		_add("a", null, null, null);
		//
		_sel(0, true, "a", null, null);
		//
		_add("b", "a", "b", "a");
		//
		_sel(1, true, null, "a", "b");
		//
		_add("c", null, "a", "b");
		//
		_sel(2, true, null, null, null);
		//
		_sel(0, false, null, null, null);
		//
		_sel(1, false, "c", null, null);
		//
		_sel(1, true, null, "c", "b");
		//
		_del("c", "b", "a", "b");
		//
		_sel(0, true, null, "b", "a");
		//
		_del("a", "b", null, null);
		//
		_del("b", null, null, null);
	}
}
