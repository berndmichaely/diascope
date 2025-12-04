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
public final class InnerNode<I> extends Node
{
	private @Nullable I value;
	private final List<@Nullable Node> nodes;
	private final List<@Nullable Node> nodesUnmodifiable;

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
	Node getSubNode(int index)
	{
		return nodes.get(index);
	}

	List<@Nullable Node> getSubNodes()
	{
		return nodesUnmodifiable;
	}

	@Nullable
	Node setSubNode(int index, @Nullable Node node)
	{
		final Node oldValue = nodes.set(index, node);
		if (node != null)
		{
			node.setParentNode(this);
		}
		return oldValue;
	}

	@Override
	public String toString()
	{
		return "%s[<%s>:%s|%s]".formatted(getClass().getSimpleName(),
			Objects.toString(getValue(), STRING_EMPTY),
			Objects.toString(getSubNode(0), STRING_EMPTY),
			Objects.toString(getSubNode(1), STRING_EMPTY));
	}
}
