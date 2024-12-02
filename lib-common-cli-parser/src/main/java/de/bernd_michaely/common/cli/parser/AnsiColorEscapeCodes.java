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

/**
 * ANSI escape sequences for 3/4 bit colors.
 *
 * @see <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape
 * code</a>
 */
public enum AnsiColorEscapeCodes
{
	BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE;

	/**
	 * Returns the foreground color code.
	 *
	 * @param bright true for bright color
	 * @return the foreground color code
	 */
	public int getFgCode(boolean bright)
	{
		return ordinal() + (bright ? 90 : 30);
	}

	/**
	 * Returns the background color code.
	 *
	 * @param bright true for bright color
	 * @return the background color code
	 */
	public int getBgCode(boolean bright)
	{
		return ordinal() + (bright ? 100 : 40);
	}

	/**
	 * Returns a color formatted version of the provided string.
	 *
	 * @param s       the provided string
	 * @param colorFg the foreground color (null for no coloring)
	 * @param colorBg the background color (null for no coloring)
	 * @param bright  true for bright color
	 * @return a color formatted version of the provided string (the empty string
	 *         in case of a null value)
	 */
	public static String formatAsAnsiColored(@Nullable String s,
		@Nullable AnsiColorEscapeCodes colorFg, @Nullable AnsiColorEscapeCodes colorBg, boolean bright)
	{
		if (s == null)
		{
			return "";
		}
		if (colorFg == null && colorBg == null)
		{
			return s;
		}
		final String CSI = "\u001B[";
		final String FB = "m";
		final String RESET = CSI + 0 + FB;
		final StringBuilder b = new StringBuilder();
		if (colorFg != null)
		{
			b.append(CSI).append(colorFg.getFgCode(bright)).append(FB);
		}
		if (colorBg != null)
		{
			b.append(CSI).append(colorBg.getBgCode(bright)).append(FB);
		}
		b.append(s).append(RESET);
		return b.toString();
	}
}
