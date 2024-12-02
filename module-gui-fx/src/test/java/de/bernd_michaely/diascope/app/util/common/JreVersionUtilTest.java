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
package de.bernd_michaely.diascope.app.util.common;

import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.util.common.JreVersionUtil.getJreVersionInfo;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JreVersionUtil Test class.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class JreVersionUtilTest
{
	private boolean compareVersionStrings(String original, String expected)
	{
		final String formatted = getJreVersionInfo(Runtime.Version.parse(original));
		final boolean result = formatted.equals(expected);
		if (result)
		{
			System.out.println(format("%20s --> %s", original, expected));
		}
		else
		{
			System.out.println(format("%s failed with: %s", original, formatted));
		}
		return result;
	}

	/**
	 * Test of getJreVersionInfo method, of class JreVersionUtil.
	 */
	@Test
	public void testGetJreVersionInfo()
	{
		System.out.println("getJreVersionInfo");
		assertTrue(compareVersionStrings("10", "10"));
		assertTrue(compareVersionStrings("11.7", "11.7"));
		assertTrue(compareVersionStrings("12.0.4", "12.0.4"));
		assertTrue(compareVersionStrings("13.0.0.2", "13.0.0.2"));
		assertTrue(compareVersionStrings("10+181", "10 build 181"));
		assertTrue(compareVersionStrings("10-ea73", "10 pre-release ea73"));
		assertTrue(compareVersionStrings("10.1.2-ea73+62", "10.1.2 pre-release ea73 build 62"));
		assertTrue(compareVersionStrings("10.1.3-ea73+64-xyz", "10.1.3 pre-release ea73 build 64 (xyz)"));
	}
}
