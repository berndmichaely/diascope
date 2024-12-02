/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.common.cli.parser;

import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.common.cli.parser.AnsiColorEscapeCodes.*;
import static java.util.Objects.requireNonNullElse;

/**
 * Utility class providing a few conceptual default string coloring methods
 * (meant for command line application help messages) and a global (static)
 * on/off switch for colored output.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class AnsiColorEscapeCodesUtil
{
	private static boolean coloringEnabled = false;

	private AnsiColorEscapeCodesUtil()
	{
	}

	/**
	 * Returns true, iff coloring is globally enabled. Coloring is enabled by
	 * default.
	 *
	 * @return true, iff coloring is globally enabled
	 */
	public static boolean isColoringEnabled()
	{
		return coloringEnabled;
	}

	/**
	 * Set coloring globally enabled. Coloring is enabled by default.
	 *
	 * @param enabled true to enable
	 */
	public static void setColoringEnabled(boolean enabled)
	{
		coloringEnabled = enabled;
	}

	/**
	 * Returns a colored version of the provided string, if coloring is enabled,
	 * otherwise the unchanged provided string. If the provided string is null,
	 * the empty string is returned.
	 *
	 * @param s       the provided string
	 * @param colorFg the foreground color (null for no coloring)
	 * @param bright  true for bright color
	 * @return a colored version of the provided string
	 */
	public static String formatAsColored(@Nullable String s,
		AnsiColorEscapeCodes colorFg, boolean bright)
	{
		return requireNonNullElse(
			isColoringEnabled() ? formatAsAnsiColored(s, colorFg, null, bright) : s, "");
	}

	/**
	 * Returns a default color formatting for headers.
	 *
	 * @param s the provided string
	 * @return a default color formatting for headers
	 */
	public static String formatAsHeader(String s)
	{
		return formatAsColored(s, YELLOW, false);
	}

	/**
	 * Returns a default color formatting for abstracts.
	 *
	 * @param s the provided string
	 * @return a default color formatting for abstracts
	 */
	public static String formatAsAbstract(String s)
	{
		return formatAsColored(s, BLACK, true);
	}

	/**
	 * Returns a default color formatting for options.
	 *
	 * @param s the provided string
	 * @return a default color formatting for options
	 */
	public static String formatAsOption(String s)
	{
		return formatAsColored(s, BLUE, true);
	}

	/**
	 * Returns a default color formatting for examples.
	 *
	 * @param s the provided string
	 * @return a default color formatting for examples
	 */
	public static String formatAsExample(String s)
	{
		return formatAsColored(s, GREEN, true);
	}
}
