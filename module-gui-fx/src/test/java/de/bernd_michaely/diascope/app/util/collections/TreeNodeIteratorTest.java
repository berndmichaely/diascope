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
package de.bernd_michaely.diascope.app.util.collections;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TreeNodeIterator Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class TreeNodeIteratorTest
{
	@Test
	public void test_empty()
	{
		System.out.println("· test_empty");
		final var treeNodeIterator = new TreeNodeIterator(null);
		assertFalse(treeNodeIterator.hasNext());
		assertThrows(NoSuchElementException.class, () -> treeNodeIterator.next());
	}

	@Test
	public void test_non_empty_1()
	{
		System.out.println("· test_non_empty_1");
		final var treeNodeIterator = new TreeNodeIterator(new LeafNode<>(7));
		assertTrue(treeNodeIterator.hasNext());
		assertEquals(7, treeNodeIterator.next().getValue());
		assertThrows(UnsupportedOperationException.class, () -> treeNodeIterator.remove());
		assertFalse(treeNodeIterator.hasNext());
		assertThrows(NoSuchElementException.class, () -> treeNodeIterator.next());
	}

	@Test
	public void test_non_empty_2()
	{
		System.out.println("· test_non_empty_2");
		final var root = new InnerNode<>(4, "inner-1");
		root.setSubNode(0, new LeafNode<>(7));
		final var innerNode2 = new InnerNode<>(2, "inner-2");
		innerNode2.setSubNode(0, new LeafNode<>(9));
		root.setSubNode(1, innerNode2);
		root.setSubNode(3, new LeafNode<>(8));
		new SimpleTreeFormatter(root).getLines().forEach(System.out::println);
		final var treeNodeIterator = new TreeNodeIterator(root);
		assertTrue(treeNodeIterator.hasNext());
		assertEquals("inner-1", treeNodeIterator.next().getValue());
		assertTrue(treeNodeIterator.hasNext());
		assertEquals(7, treeNodeIterator.next().getValue());
		assertTrue(treeNodeIterator.hasNext());
		assertEquals("inner-2", treeNodeIterator.next().getValue());
		assertTrue(treeNodeIterator.hasNext());
		assertEquals(9, treeNodeIterator.next().getValue());
		assertTrue(treeNodeIterator.hasNext());
		assertEquals(8, treeNodeIterator.next().getValue());
		assertFalse(treeNodeIterator.hasNext());
		assertThrows(NoSuchElementException.class, () -> treeNodeIterator.next());
	}
}
