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
import java.util.List;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.image.Bindings.C;
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
	public void test_ordering()
	{
		System.out.println("Test Border ordering");
		assertIterableEquals(List.of(RIGHT, BOTTOM, LEFT, TOP), List.of(Border.values()));
	}

	@Test
	public void test_next()
	{
		System.out.println("Test Border::next");
		final int maxLength = Arrays.stream(Border.values())
			.map(Border::name).mapToInt(String::length).max().orElse(0);
		final int n = Border.values().length;
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
			assertEquals(1, (n + expected.ordinal() - border.ordinal()) % n);
			final Border actual = border.next();
			System.out.format("%" + maxLength + "s → %s%n", border, actual);
			assertEquals(expected, actual);
		}
	}

	private void _test_numberOfCornerPointsBetween(int expected,
		Border border, int next1, int next2)
	{
		Border border1 = border;
		double angle1 = 10.0;
		for (int i = 0; i < next1; i++)
		{
			border1 = border1.next();
			angle1 += 85.0;
		}
		Border border2 = border;
		double angle2 = 20.0;
		for (int i = 0; i < next2; i++)
		{
			border2 = border2.next();
			angle2 += 85.0;
		}
		while (angle2 < angle1)
		{
			angle2 += C;
		}
		System.out.println("· %s (%7.3f°) → %s (%7.3f°) = %d"
			.formatted(border1, angle1, border2, angle2, expected));
		final int num = numberOfCornerPointsBetween(border1, angle1, border2, angle2);
		assertEquals(expected, num);
	}

	@Test
	public void test_numberOfCornerPointsBetween()
	{
		System.out.println("Check number of  corner  points  between borders:");
		for (var border : Border.values())
		{
			_test_numberOfCornerPointsBetween(0, border, 0, 0);
			_test_numberOfCornerPointsBetween(1, border, 0, 1);
			_test_numberOfCornerPointsBetween(2, border, 0, 2);
			_test_numberOfCornerPointsBetween(3, border, 0, 3);
			_test_numberOfCornerPointsBetween(4, border, 0, 4);
			_test_numberOfCornerPointsBetween(3, border, 1, 0);
			_test_numberOfCornerPointsBetween(2, border, 2, 0);
			_test_numberOfCornerPointsBetween(1, border, 3, 0);
			_test_numberOfCornerPointsBetween(0, border, 4, 0);
		}
	}
}
