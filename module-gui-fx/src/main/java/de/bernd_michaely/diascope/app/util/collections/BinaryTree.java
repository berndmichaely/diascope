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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.collections.InnerNode.createBinaryTreeNode;

/// Generic class to describe a binary tree structure.
///
/// This is a {@code  Collection<TreeNode>}.
/// Note, that {@code add} and {@code remove} methods are not supported.
///
/// @see #append(Object)
/// @see #append(List)
/// @see #append(Object, Object)
/// @see #insertItemAt(Object, Object)
/// @see #insertItemAt(Object, Object, Object)
/// @see #removeItem(Object)
///
/// @param <I> data type associated with inner nodes
/// @param <L> data type associated with leaf nodes
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class BinaryTree<I, L> extends AbstractCollection<TreeNode>
{
	private @Nullable TreeNode root;
	private int size;

	/// Returns the root node.
	///
	/// @return the root node
	///
	@Nullable
	TreeNode getRoot()
	{
		return root;
	}

	@Override
	public Iterator<TreeNode> iterator()
	{
		return new TreeNodeIterator(getRoot());
	}

	@Override
	public int size()
	{
		return size;
	}

	/// Finds an inner node by its value.
	///
	/// @param value the value to search for
	/// @return the inner node, if found, or {@code null}
	///
	public @Nullable
	InnerNode findInnerNode(I value)
	{
		final Iterator<TreeNode> iter = iterator();
		while (iter.hasNext())
		{
			if (iter.next() instanceof InnerNode innerNode && Objects.equals(innerNode.getValue(), value))
			{
				return innerNode;
			}
		}
		return null;
	}

	/// Finds a leaf node by its value.
	///
	/// @param value the value to search for
	/// @return the leaf node, if found, or {@code null}
	///
	public @Nullable
	LeafNode findLeafNode(L value)
	{
		final Iterator<TreeNode> iter = iterator();
		while (iter.hasNext())
		{
			if (iter.next() instanceof LeafNode leafNode && Objects.equals(leafNode.getValue(), value))
			{
				return leafNode;
			}
		}
		return null;
	}

	/// Returns true, iff the tree contains an inner node with the given value.
	///
	/// @return true, iff the tree contains an inner node with the given value
	///
	public boolean containsInnerValue(I item)
	{
		return findInnerNode(item) != null;
	}

	/// Returns true, iff the tree contains a leaf node with the given value.
	///
	/// @return true, iff the tree contains a leaf node with the given value
	///
	public boolean containsLeaf(L item)
	{
		return findLeafNode(item) != null;
	}

	/// Appends a leaf node for the given item.
	///
	/// @param item the given item
	///
	public void append(L item)
	{
		append(item, null);
	}

	/// Appends leaf nodes for the given items.
	///
	/// @param item the given list of items
	///
	public void append(List<L> items)
	{
		items.forEach(this::append);
	}

	/// Appends a leaf node for the given item and
	/// sets a value for the generated inner parent node.
	///
	/// @param item the given item
	/// @param value the associated inner node value
	///
	public void append(L item, @Nullable I value)
	{
		if (root == null)
		{
			if (value == null)
			{
				root = new LeafNode<>(item);
				size = 1;
			}
			else
			{
				throw new IllegalArgumentException(
					"%s::append : node is first leaf in tree and cannot have an associated value"
						.formatted(getClass().getName()));
			}
		}
		else
		{
			// find last leaf:
			LeafNode lastLeaf = null;
			final Iterator<TreeNode> iterator = iterator();
			while (iterator.hasNext())
			{
				if (iterator.next() instanceof LeafNode nextLeaf)
				{
					lastLeaf = nextLeaf;
				}
			}
			if (lastLeaf != null)
			{
				insertLeafNode(item, value, lastLeaf);
			}
			else
			{
				throw new IllegalStateException(
					"::append : no last leaf found".formatted(getClass().getName()));
			}
		}
	}

	/// Inserts a new leaf node after the leaf node given by the insertion point.
	///
	/// @param item the given leaf node value
	/// @param insertionPoint the insertion point indicated by its leaf value
	/// @return true, iff the insertion point was found and a new node was inserted
	///
	public boolean insertItemAt(L item, L insertionPoint)
	{
		return insertItemAt(item, null, insertionPoint);
	}

	/// Inserts a new leaf node after the leaf node given by the insertion point.
	/// Also sets a value for the generated inner parent node.
	///
	/// @param item the given leaf node value
	/// @param value the associated inner node value
	/// @param insertionPoint the insertion point indicated by its leaf value
	/// @return true, iff the insertion point was found and a new node was inserted
	///
	public boolean insertItemAt(L item, @Nullable I value, L insertionPoint)
	{
		final LeafNode insertionNode = findLeafNode(insertionPoint);
		if (insertionNode != null)
		{
			insertLeafNode(item, value, insertionNode);
			return true;
		}
		else
		{
			return false;
		}
	}

	private void insertLeafNode(L item, @Nullable I value, LeafNode insertionNode)
	{
		final InnerNode parentNode = insertionNode.getParentNode();
		final InnerNode<I> newInnerNode = createBinaryTreeNode(insertionNode, new LeafNode<>(item));
		newInnerNode.setValue(value);
		if (parentNode != null)
		{
			int childIndex = parentNode.getSize() - 1;
			while (childIndex >= 0 && insertionNode != parentNode.getSubNode(childIndex))
			{
				childIndex--;
			}
			parentNode.setSubNode(childIndex, newInnerNode);
		}
		else
		{
			root = newInnerNode;
		}
		size++;
	}

	/// Removes the leaf node indicated by the given value.
	///
	/// @param item the given leaf value
	/// @return true, if the leaf node was found and removed,
	///					false, if the leaf was not found and nothing was changed
	///
	public boolean removeItem(L item)
	{
		final LeafNode leafNode = findLeafNode(item);
		if (leafNode != null)
		{
			final InnerNode pn = leafNode.getParentNode();
			if (pn != null)
			{
				final InnerNode ppn = pn.getParentNode();
				final TreeNode otherChild = pn.getSubNode(leafNode == pn.getSubNode(1) ? 0 : 1);
				if (ppn != null)
				{
					ppn.setSubNode(pn == ppn.getSubNode(1) ? 1 : 0, otherChild);
				}
				else
				{
					root = otherChild;
				}
				size--;
			}
			else
			{
				clear();
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void clear()
	{
		root = null;
		size = 0;
	}

	void formatted(Consumer<String> consumer)
	{
		new SimpleTreeFormatter(getRoot()).getLines().forEach(consumer);
	}

	@Override
	public String toString()
	{
		return "%s:{%s}".formatted(getClass().getSimpleName(),
			Objects.toString(getRoot(), "·"));
	}
}
