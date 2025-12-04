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
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Collections.unmodifiableList;

/// Inner nodes of a BinaryTree.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public final class InnerNode<I> extends TreeNode
{
	private @Nullable I value;
	private final List<@Nullable TreeNode> nodes;
	private final List<@Nullable TreeNode> nodesUnmodifiable;

	/// Creates a new inner node instance with a fixed number of subnodes.
	///
	/// @param numSubNodes the number of possible subnodes.
	/// @param value a value associated with the inner node (may be {@code null})
	///
	InnerNode(int numSubNodes, @Nullable I value)
	{
		this.value = value;
		this.nodes = new ArrayList<>(numSubNodes);
		for (int i = 0; i < numSubNodes; i++)
		{
			nodes.add(null);
		}
		this.nodesUnmodifiable = unmodifiableList(nodes);
	}

	/// Creates a new binary tree node.
	///
	/// @param firstNode first binary tree node
	/// @param secondNode second binary tree node
	///
	static <V> InnerNode<V> createBinaryNode(TreeNode firstNode, TreeNode secondNode)
	{
		final var node = new InnerNode<V>(2, null);
		node.setSubNode(0, firstNode);
		node.setSubNode(1, secondNode);
		return node;
	}

	public int getSize()
	{
		return nodes.size();
	}

	@Override
	public @Nullable
	I getValue()
	{
		return value;
	}

	public void setValue(@Nullable I value)
	{
		this.value = value;
	}

	@Nullable
	TreeNode getSubNode(int index)
	{
		return nodes.get(index);
	}

	List<@Nullable TreeNode> getSubNodes()
	{
		return nodesUnmodifiable;
	}

	@Nullable
	TreeNode setSubNode(int index, @Nullable TreeNode node)
	{
		final TreeNode oldValue = nodes.set(index, node);
		if (node != null)
		{
			node.setParentNode(this);
		}
		return oldValue;
	}

	@Override
	public String toString()
	{
		final var s = new StringBuilder();
		s.append(getClass().getSimpleName())
			.append("[<")
			.append(Objects.toString(getValue(), STRING_EMPTY))
			.append(">:");
		for (int i = 0; i < getSubNodes().size(); i++)
		{
			if (i > 0)
			{
				s.append("|");
			}
			s.append(Objects.toString(getSubNode(i), STRING_EMPTY));
		}
		s.append("]");
		return s.toString();
	}
}
