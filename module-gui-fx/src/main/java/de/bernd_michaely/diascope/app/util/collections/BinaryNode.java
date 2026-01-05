/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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

import org.checkerframework.checker.nullness.qual.Nullable;

/// Inner nodes of a BinaryTree.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public final class BinaryNode<I> extends InnerNode<I>
{
	/// Creates a new binary tree node.
	///
	/// @param firstNode first binary tree node
	/// @param lastNode second binary tree node
	/// @param value the value to associate with this node
	///
	BinaryNode(TreeNode firstNode, TreeNode lastNode, @Nullable I value)
	{
		super(2, value);
		setSubNode(0, firstNode);
		setSubNode(1, lastNode);
	}

	/// {@inheritDoc}
	///
	/// If `node` is `null`, nothing is changed and `null` will be returned.
	///
	@Override
	@Nullable
	TreeNode setSubNode(int index, @Nullable TreeNode node)
	{
		if (node != null)
		{
			final TreeNode oldValue = super.setSubNode(index, node);
			node.setParentNode(this);
			return oldValue;
		}
		else
		{
			return null;
		}
	}

	@Override
	TreeNode getFirst()
	{
		final var node = getSubNodes().getFirst();
		if (node != null)
		{
			return node;
		}
		else
		{
			throw new IllegalStateException(getClass().getName() + ": first node is null");
		}
	}

	@Override
	TreeNode getLast()
	{
		final var node = getSubNodes().getLast();
		if (node != null)
		{
			return node;
		}
		else
		{
			throw new IllegalStateException(getClass().getName() + ": last node is null");
		}
	}
}
