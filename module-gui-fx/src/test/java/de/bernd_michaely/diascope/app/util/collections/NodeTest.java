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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Node classes Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class NodeTest
{
	@Test
	public void test_node_classes()
	{
		System.out.println("· test node classes");
		final var root = new InnerNode<>(4, "inner-1");
		final var innerNode2 = new InnerNode<>(2, "inner-2");
		final var leafNode1 = new LeafNode<Integer>(7);
		final var leafNode2 = new LeafNode<Integer>(8);
		final var leafNode3 = new LeafNode<Integer>(9);
		final var leafNode4 = new LeafNode<Integer>(8);
		root.setSubNode(0, leafNode1);
		innerNode2.setSubNode(0, leafNode3);
		root.setSubNode(1, innerNode2);
		root.setSubNode(3, leafNode2);
		new SimpleTreeFormatter(root).getLines().forEach(System.out::println);
		assertTrue(root.isRootNode());
		assertEquals("inner-1", root.getValue());
		assertEquals("inner-2", innerNode2.getValue());
		assertEquals(7, leafNode1.getValue());
		assertNotEquals(leafNode2, leafNode3);
		assertEquals(leafNode2, leafNode4);
		assertEquals(root, leafNode1.getParentNode());
		assertEquals(root, innerNode2.getParentNode());
		assertEquals(root, leafNode2.getParentNode());
		assertEquals(innerNode2, leafNode3.getParentNode());
		assertEquals(
			"InnerNode[<inner-1>:LeafNode(7)|InnerNode[<inner-2>:LeafNode(9)|%1$s]|%1$s|LeafNode(8)]"
				.formatted(Node.STRING_EMPTY), root.toString());
	}
}
