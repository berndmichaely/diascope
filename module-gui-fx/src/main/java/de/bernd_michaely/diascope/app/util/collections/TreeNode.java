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

import java.lang.ref.WeakReference;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Base class for tree nodes.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public abstract sealed class TreeNode permits InnerNode, LeafNode
{
	static final String STRING_EMPTY = "·";
	private @Nullable WeakReference<InnerNode> parentNode;

	abstract @Nullable
	Object getValue();

	TreeNode()
	{
	}

	/// Returns the parent node, if this node is not the root.
	///
	/// *Implementation note:*
	/// Parent nodes are held via weak references.
	///
	/// @return the parent node or {@code null}
	public @Nullable
	InnerNode getParentNode()
	{
		return parentNode != null ? parentNode.get() : null;
	}

	void setParentNode(@Nullable InnerNode parentNode)
	{
		this.parentNode = parentNode != null ? new WeakReference<>(parentNode) : null;
	}

	/// Returns true, iff this node is the root node.
	///
	/// @return true, iff this node is the root node
	///
	public boolean isRootNode()
	{
		return parentNode == null;
	}

	@Override
	public String toString()
	{
		return "%s(%s)".formatted(
			getClass().getSimpleName(), Objects.toString(getValue(), ""));
	}
}
