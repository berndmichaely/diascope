/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.icons;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Icons Test. Check availability of icon resources.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class IconsTest
{
	/**
	 * Test of getIconImage method, of class Icons.
	 */
	@Test
	public void testGetIconImage()
	{
		System.out.println("=== Check availability of icon resources ===");
		for (Icons icon : Icons.values())
		{
			for (var isColorMode : Set.of(false, true))
			{
				final String resourceName = icon.getResourceName(isColorMode);
				System.out.println("· %s".formatted(resourceName));
				assertThat(icon.getIconImage(isColorMode, 7, 8))
					.withFailMessage("Icon resource »%s« not found!", resourceName)
					.isNotNull();
			}
		}
	}
}
