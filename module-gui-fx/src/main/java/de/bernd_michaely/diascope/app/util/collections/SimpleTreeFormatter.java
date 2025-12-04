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

import static de.bernd_michaely.diascope.app.util.collections.Node.STRING_EMPTY;

/// Simple tree formatter.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class SimpleTreeFormatter
{
	private final List<String> lines;

	SimpleTreeFormatter(@Nullable Node node)
	{
		lines = addNode(node);
	}

	private static String getNodeValue(@Nullable Node node)
	{
		if (node != null)
		{
			final InnerNode parentNode = node.getParentNode();
			return "[" +
				Objects.toString(node.getValue()) + "←(" +
				Objects.toString(parentNode != null ? parentNode.getValue() : null) + ")]";
		}
		else
		{
			return "[" + STRING_EMPTY + "]";
		}
	}

	private static String formatLine(int numSpaces, String line)
	{
		final StringBuilder s = new StringBuilder(line.length() + numSpaces);
		s.repeat(" ", numSpaces);
		s.append(line);
		return s.toString();
	}

	private static List<String> addNode(@Nullable Node node)
	{
		final List<String> result = new ArrayList<>();
		switch (node)
		{
			case InnerNode innerNode ->
			{
				final String value = getNodeValue(node);
				final int len = value.length() + 3;
				final int n = innerNode.getSize();
				final int n_2 = n / 2;
				for (int i = 0; i < n; i++)
				{
					if (i == n_2)
					{
						result.add("i" + value);
					}
					final List<String> list = addNode(innerNode.getSubNode(i));
					list.stream().map(line -> formatLine(len, line)).forEach(result::add);
				}
			}
			case LeafNode _ -> result.add("L" + getNodeValue(node));
			case null, default -> result.add("[" + STRING_EMPTY + "]");
		}
		return result;
	}

	List<String> getLines()
	{
		return lines;
	}
}
