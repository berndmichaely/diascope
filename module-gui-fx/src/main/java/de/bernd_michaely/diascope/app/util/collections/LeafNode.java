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

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Leaf nodes of a BinaryTree.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public final class LeafNode<L> extends Node
{
	private @Nullable L value;

	LeafNode(L value)
	{
		this.value = value;
	}

	@Override
	public @Nullable
	L getValue()
	{
		return value;
	}

	public void setValue(@Nullable L value)
	{
		this.value = value;
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof LeafNode other)
		{
			return Objects.equals(this.getValue(), other.getValue());
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(getValue());
	}
}
