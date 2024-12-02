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
package de.bernd_michaely.diascope.app.version;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.version.SemanticVersion.parseSemanticVersion;
import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SemanticVersion Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SemanticVersionTest
{
	@Test
	public void test_parse()
	{
		System.out.println("test_parse");
		final SemanticVersion version = parseSemanticVersion("1.2.3-rc.1+b17").get();
		assertEquals(1, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getPatch());
		assertEquals("rc.1", version.getPreRelease().toString());
		assertEquals("b17", version.getBuild());
		assertEquals(new SemanticVersion(1, 2, 3, "rc.1", "b17"), version);
		assertFalse(parseSemanticVersion("-1.0.0").isPresent());
		assertFalse(parseSemanticVersion("1.x.y").isPresent());
		assertEquals(new SemanticVersion(), parseSemanticVersion("0.0.0").get());
		assertEquals(new SemanticVersion(0, 0, 0, "", ""), parseSemanticVersion("0.0.0").get());
	}

	@Test
	public void test_equals()
	{
		System.out.println("test_equals");
		final Optional<SemanticVersion> optional1 = parseSemanticVersion("1.2.3+x");
		final Optional<SemanticVersion> optional2 = parseSemanticVersion("1.2.3+y");
		final Optional<SemanticVersion> optional3 = parseSemanticVersion("1.1.1-rc.1");

		final SemanticVersion v1 = optional1.get();
		final SemanticVersion v2 = optional2.get();
		final SemanticVersion v3 = optional3.get();

		assertTrue(optional1.isPresent());
		assertTrue(optional2.isPresent());
		assertTrue(optional3.isPresent());
		assertTrue(v1.equals(v2));
		assertFalse(parseSemanticVersion("2.1.1-rc.1").get().equals(v3));
		assertFalse(parseSemanticVersion("1.2.1-rc.1").get().equals(v3));
		assertFalse(parseSemanticVersion("1.1.2-rc.1").get().equals(v3));
		assertFalse(parseSemanticVersion("1.1.1-rc.2").get().equals(v3));
	}

	@Test
	public void test_compareTo()
	{
		System.out.println("test_compareTo");
		final List<SemanticVersion> listAscending = List.of(
			"0.0.0-alpha",
			"0.0.0",
			"0.9.9",
			"1.0.0-alpha",
			"1.0.0-alpha.1",
			"1.0.0-alpha.beta",
			"1.0.0-beta",
			"1.0.0-beta.2",
			"1.0.0-beta.11",
			"1.0.0-rc.1",
			"1.0.0",
			"2.0.0",
			"2.1.0",
			"2.1.1",
			"9.9.9",
			"9.9.10",
			"9.10.9",
			"10.9.9",
			"10.10.10"
		).stream()
			.map(SemanticVersion::parseSemanticVersion)
			.map(Optional::get)
			.toList();
		for (int i = 1; i < listAscending.size(); i++)
		{
			final SemanticVersion v1 = listAscending.get(i - 1);
			final SemanticVersion v2 = listAscending.get(i);
			System.out.println("· comparing »%s« < »%s«".formatted(v1, v2));
			assertTrue(v1.compareTo(v2) < 0);
			assertTrue(v2.compareTo(v1) > 0);
		}
		assertIterableEquals(listAscending,
			listAscending.stream().collect(toCollection(TreeSet::new)));
	}

	private void _test_toString(String version)
	{
		assertEquals(version, parseSemanticVersion(version).get().toString());
	}

	@Test
	public void test_toString()
	{
		System.out.println("test_toString");
		_test_toString("0.0.0");
		_test_toString("1.0.0-rc.1");
		_test_toString("1.0.0+r17");
		_test_toString("1.0.0-rc.1+r17");
		_test_toString("1.0.0");
		_test_toString("2.3.4-rc.1+b17");
	}
}
