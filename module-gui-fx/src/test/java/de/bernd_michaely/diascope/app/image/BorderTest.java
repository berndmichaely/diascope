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
package de.bernd_michaely.diascope.app.image;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.image.Border.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Border.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class BorderTest
{
	@Test
	public void test_next()
	{
		final int maxLength = Arrays.stream(Border.values())
			.map(Border::name).mapToInt(String::length).max().orElse(0);
		for (var border : Border.values())
		{
			final Border expected = switch (border)
			{
				case BOTTOM ->
					LEFT;
				case LEFT ->
					TOP;
				case TOP ->
					RIGHT;
				case RIGHT ->
					BOTTOM;
			};
			final Border actual = border.next();
			System.out.format("%" + maxLength + "s → %s%n", border, actual);
			assertEquals(expected, actual);
		}
	}

	@Test
	public void test_numberOfCornerPointsBetween()
	{
		for (var border : Border.values())
		{
			assertEquals(0, numberOfCornerPointsBetween(border, border));
			assertEquals(1, numberOfCornerPointsBetween(border, border.next()));
			assertEquals(2, numberOfCornerPointsBetween(border, border.next().next()));
			assertEquals(3, numberOfCornerPointsBetween(border, border.next().next().next()));
			assertEquals(0, numberOfCornerPointsBetween(border, border.next().next().next().next()));
			assertEquals(0, numberOfCornerPointsBetween(border, border.next().next().next().next()));
			assertEquals(3, numberOfCornerPointsBetween(border.next(), border));
			assertEquals(2, numberOfCornerPointsBetween(border.next().next(), border));
			assertEquals(1, numberOfCornerPointsBetween(border.next().next().next(), border));
			assertEquals(0, numberOfCornerPointsBetween(border.next().next().next().next(), border));
		}
	}
}
