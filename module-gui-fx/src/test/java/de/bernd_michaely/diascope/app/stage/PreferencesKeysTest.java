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
package de.bernd_michaely.diascope.app.stage;

import java.util.TreeSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PreferencesKeys.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class PreferencesKeysTest
{
	@Test
	public void testPreferencesKeys()
	{
		System.out.println("Test PreferencesKeys …");
		final var setKeys = new TreeSet<String>();
		for (var prefKey : PreferencesKeys.values())
		{
			System.out.println("Test %s".formatted(prefKey));
			assertTrue(prefKey.name().startsWith(PreferencesKeys.PREFIX_KEYS));
			final String key = prefKey.getKey();
			assertNotNull(key);
			assertFalse(key.isBlank());
			assertTrue(setKeys.add(key)); // uniqueness
		}
	}
}
