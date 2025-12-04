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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Iterator for tree nodes.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class TreeNodeIterator implements Iterator<Node>
{
	private final Deque<Node> stack = new ArrayDeque<>();

	TreeNodeIterator(@Nullable Node root)
	{
		if (root != null)
		{
			stack.push(root);
		}
	}

	@Override
	public boolean hasNext()
	{
		return !stack.isEmpty();
	}

	@Override
	public Node next()
	{
		final Node next = stack.pop();
		if (next instanceof InnerNode<?> innerNode)
		{
			innerNode.getSubNodes().reversed().forEach(subNode ->
			{
				if (subNode != null)
				{
					stack.push(subNode);
				}
			});
		}
		return next;
	}
}
