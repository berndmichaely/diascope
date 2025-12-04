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

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ListContentConcatenation Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ListContentConcatenationTest
{
	private static ObservableList<String> createList(char from, char to)
	{
		final ObservableList<String> result = FXCollections.observableArrayList();
		for (char c = from; c <= to; c++)
		{
			result.add("" + c);
		}
		return result;
	}

	@Test
	public void test_source_lists_not_observable()
	{
		System.out.println("test_source_lists_not_observable");
		final var src1 = createList('a', 'e');
		final var src2 = createList('g', 'i');
		final var src3 = createList('m', 'p');
		try (var lcc = new ListContentConcatenation<String>(List.of(src1, src2, src3)))
		{
			assertFalse(lcc.isSourceListsObservable());
			final List<String> result = lcc.getTargetList();
			assertEquals(List.of("a", "b", "c", "d", "e", "g", "h", "i", "m", "n", "o", "p"), result);
			src1.remove("c");
			assertEquals(List.of("a", "b", "d", "e", "g", "h", "i", "m", "n", "o", "p"), result);
			src1.add("f");
			assertEquals(List.of("a", "b", "d", "e", "f", "g", "h", "i", "m", "n", "o", "p"), result);
			src2.remove("i");
			assertEquals(List.of("a", "b", "d", "e", "f", "g", "h", "m", "n", "o", "p"), result);
			src3.add(0, "l");
			assertEquals(List.of("a", "b", "d", "e", "f", "g", "h", "l", "m", "n", "o", "p"), result);
			src3.add("x");
			assertEquals(List.of("a", "b", "d", "e", "f", "g", "h", "l", "m", "n", "o", "p", "x"), result);
			src3.add(5, "q");
			assertEquals(List.of("a", "b", "d", "e", "f", "g", "h", "l", "m", "n", "o", "p", "q", "x"), result);
		}
	}

	@Test
	public void test_target()
	{
		System.out.println("test_target");
		final var src1 = createList('a', 'e');
		final List<String> targetList = new ArrayList<>(List.of("Test", "TargetList", "not", "empty"));
		try (var lcc = new ListContentConcatenation<String>(List.of(src1), targetList))
		{
			assertFalse(lcc.isSourceListsObservable());
			assertEquals(List.of("a", "b", "c", "d", "e"), targetList);
		}
	}

	@Test
	public void test_source_lists_observable()
	{
		System.out.println("test_source_lists_observable");
		final var src1 = createList('a', 'e');
		final var src2 = createList('g', 'i');
		final var src3 = createList('m', 'p');
		final ObservableList<ObservableList<String>> observableLists =
			FXCollections.observableArrayList();
		final List<String> result;
		try (var lcc = new ListContentConcatenation<String>(observableLists))
		{
			assertTrue(lcc.isSourceListsObservable());
			result = lcc.getTargetList();
			assertEquals(List.of(), result);
			observableLists.add(src1);
			assertEquals(List.of("a", "b", "c", "d", "e"), result);
			observableLists.add(src3);
			assertEquals(List.of("a", "b", "c", "d", "e", "m", "n", "o", "p"), result);
			observableLists.add(1, src2);
			assertEquals(List.of("a", "b", "c", "d", "e", "g", "h", "i", "m", "n", "o", "p"), result);
			observableLists.remove(0);
			assertEquals(List.of("g", "h", "i", "m", "n", "o", "p"), result);
			observableLists.remove(1);
			assertEquals(List.of("g", "h", "i"), result);
			observableLists.remove(0);
			assertEquals(List.of(), result);
			observableLists.addAll(List.of(src1, src2, src3));
			assertEquals(List.of("a", "b", "c", "d", "e", "g", "h", "i", "m", "n", "o", "p"), result);
			observableLists.clear();
			assertEquals(List.of(), result);
			observableLists.addAll(List.of(src1, src1));
			assertEquals(List.of("a", "b", "c", "d", "e", "a", "b", "c", "d", "e"), result);
			observableLists.remove(0);
			assertEquals(List.of("a", "b", "c", "d", "e"), result);
			observableLists.remove(0);
			assertEquals(List.of(), result);
			observableLists.addAll(List.of(src1, src1));
			assertEquals(List.of("a", "b", "c", "d", "e", "a", "b", "c", "d", "e"), result);
			observableLists.set(0, src2);
			assertEquals(List.of("g", "h", "i", "a", "b", "c", "d", "e"), result);
		}
		// check listener cleanup:
		observableLists.getFirst().removeFirst();
		assertEquals(List.of("g", "h", "i", "a", "b", "c", "d", "e"), result);
		observableLists.clear();
		assertEquals(List.of("g", "h", "i", "a", "b", "c", "d", "e"), result);
	}
}
