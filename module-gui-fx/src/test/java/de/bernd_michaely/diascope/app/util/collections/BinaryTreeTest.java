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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BinaryTree Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class BinaryTreeTest
{
	private List<Node> toList(BinaryTree binaryTree)
	{
		final List<Node> result = new ArrayList<>();
		_toList(result, binaryTree.getRoot());
		return result;
	}

	private void _toList(List<Node> result, Node node)
	{
		if (node != null)
		{
			result.add(node);
			if (node instanceof InnerNode innerNode)
			{
				_toList(result, innerNode.getSubNode(0));
				_toList(result, innerNode.getSubNode(1));
			}
		}
	}

	@Test
	public void test_add_leaves()
	{
		System.out.println(">>> test_add_leaves");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		System.out.println();
		System.out.println("→ " + binaryTree);
		assertEquals("BinaryTree:{·}", binaryTree.toString());
		final List<String> listTest = List.of("one", "two", "three", "four", "five");
		for (int i = 0; i < listTest.size(); i++)
		{
			if (i == 0)
			{
				binaryTree.append(listTest.get(i));
			}
			else
			{
				binaryTree.append(listTest.get(i), i);
			}
		}
		System.out.println();
		System.out.println("→ " + binaryTree);
		assertEquals(
			"BinaryTree:{InnerNode[<1>:LeafNode(one)|InnerNode[<2>:LeafNode(two)|InnerNode[<3>:LeafNode(three)|InnerNode[<4>:LeafNode(four)|LeafNode(five)]]]]}",
			binaryTree.toString());
		assertEquals(listTest.size(), binaryTree.size());
		assertIterableEquals(toList(binaryTree), binaryTree);
		System.out.println();
		binaryTree.formatted(System.out::println);
		final boolean inserted = binaryTree.insertItemAt("six", 789, "three");
		assertTrue(inserted);
		System.out.println();
		System.out.println("→ " + binaryTree);
		assertEquals(
			"BinaryTree:{InnerNode[<1>:LeafNode(one)|InnerNode[<2>:LeafNode(two)|InnerNode[<3>:InnerNode[<789>:LeafNode(three)|LeafNode(six)]|InnerNode[<4>:LeafNode(four)|LeafNode(five)]]]]}",
			binaryTree.toString());
		System.out.println();
		binaryTree.formatted(System.out::println);
		binaryTree.clear();
		assertEquals(0, binaryTree.size());
		assertEquals("BinaryTree:{·}", binaryTree.toString());
	}

	@Test
	public void test_add_inner()
	{
		System.out.println(">>> test_add_inner");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		binaryTree.append("one");
		assertFalse(binaryTree.containsInnerValue(7));
		binaryTree.append("two", 7);
		assertTrue(binaryTree.containsInnerValue(7));
		binaryTree.removeItem("one");
		assertFalse(binaryTree.containsInnerValue(7));
	}

	@Test
	public void test_remove_0()
	{
		System.out.println(">>> test_remove_0");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		assertFalse(binaryTree.removeItem(""));
		assertEquals(0, binaryTree.size());
		assertEquals("BinaryTree:{·}", binaryTree.toString());
	}

	@Test
	public void test_remove_1()
	{
		System.out.println(">>> test_remove_1");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		binaryTree.append("one");
		assertEquals(1, binaryTree.size());
		assertTrue(binaryTree.containsLeaf("one"));
		binaryTree.formatted(System.out::println);
		assertTrue(binaryTree.removeItem("one"));
		assertEquals(0, binaryTree.size());
		assertFalse(binaryTree.containsLeaf("one"));
		System.out.println("→");
		binaryTree.formatted(System.out::println);
		assertEquals("BinaryTree:{·}", binaryTree.toString());
	}

	@Test
	public void test_remove_2_0()
	{
		System.out.println(">>> test_remove_2_0");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		binaryTree.append("one");
		binaryTree.append("two");
		assertEquals(2, binaryTree.size());
		assertTrue(binaryTree.containsLeaf("one"));
		assertTrue(binaryTree.containsLeaf("two"));
		binaryTree.formatted(System.out::println);
		assertTrue(binaryTree.removeItem("one"));
		assertEquals(1, binaryTree.size());
		assertFalse(binaryTree.containsLeaf("one"));
		assertTrue(binaryTree.containsLeaf("two"));
		System.out.println("→");
		binaryTree.formatted(System.out::println);
		assertEquals("BinaryTree:{LeafNode(two)}", binaryTree.toString());
	}

	@Test
	public void test_remove_2_1()
	{
		System.out.println(">>> test_remove_2_1");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		binaryTree.append("one");
		binaryTree.append("two");
		assertEquals(2, binaryTree.size());
		assertTrue(binaryTree.containsLeaf("one"));
		assertTrue(binaryTree.containsLeaf("two"));
		binaryTree.formatted(System.out::println);
		assertTrue(binaryTree.removeItem("two"));
		assertEquals(1, binaryTree.size());
		assertTrue(binaryTree.containsLeaf("one"));
		assertFalse(binaryTree.containsLeaf("two"));
		System.out.println("→");
		binaryTree.formatted(System.out::println);
		assertEquals("BinaryTree:{LeafNode(one)}", binaryTree.toString());
	}

	@Test
	public void test_remove_3_0()
	{
		System.out.println(">>> test_remove_3_0");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		binaryTree.append(List.of("one", "two", "three"));
		assertEquals(3, binaryTree.size());
		assertTrue(binaryTree.containsLeaf("one"));
		assertTrue(binaryTree.containsLeaf("two"));
		assertTrue(binaryTree.containsLeaf("three"));
		binaryTree.formatted(System.out::println);
		assertTrue(binaryTree.removeItem("one"));
		assertEquals(2, binaryTree.size());
		assertFalse(binaryTree.containsLeaf("one"));
		assertTrue(binaryTree.containsLeaf("two"));
		assertTrue(binaryTree.containsLeaf("three"));
		System.out.println("→");
		binaryTree.formatted(System.out::println);
		assertEquals(
			"BinaryTree:{InnerNode[<%1$s>:LeafNode(two)|LeafNode(three)]}".formatted(Node.STRING_EMPTY),
			binaryTree.toString());
	}

	@Test
	public void test_remove_3_1()
	{
		System.out.println(">>> test_remove_3_1");
		final var binaryTree = new BinaryTree<Integer, String>();
		assertEquals(0, binaryTree.size());
		binaryTree.append(List.of("one", "two", "three"));
		assertEquals(3, binaryTree.size());
		assertTrue(binaryTree.containsLeaf("one"));
		assertTrue(binaryTree.containsLeaf("two"));
		assertTrue(binaryTree.containsLeaf("three"));
		binaryTree.formatted(System.out::println);
		assertTrue(binaryTree.removeItem("two"));
		assertEquals(2, binaryTree.size());
		assertTrue(binaryTree.containsLeaf("one"));
		assertFalse(binaryTree.containsLeaf("two"));
		assertTrue(binaryTree.containsLeaf("three"));
		System.out.println("→");
		binaryTree.formatted(System.out::println);
		assertEquals(
			"BinaryTree:{InnerNode[<%1$s>:LeafNode(one)|LeafNode(three)]}".formatted(Node.STRING_EMPTY),
			binaryTree.toString());
	}
}
