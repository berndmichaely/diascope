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
package de.bernd_michaely.common.semver;

import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SemanticVersion Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SemanticVersionTest
{
	private static final List<String> LIST_SORTED_STRICTLY_ASCENDING = List.of(
		"0.0.0-0",
		"0.0.0-0.0",
		"0.0.0-0.9",
		"0.0.0-0.10",
		"0.0.0-0.a",
		"0.0.0-1.0",
		"0.0.0--",
		"0.0.0-0-",
		"0.0.0-0-0",
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
	);

	private static List<SemanticVersion> getVersionListAscending()
	{
		return LIST_SORTED_STRICTLY_ASCENDING.stream().map(SemanticVersion::new).collect(toUnmodifiableList());
	}

	@Test
	public void test_getSupportedVersion()
	{
		assertDoesNotThrow(() -> System.out.println(
			"SemanticVersion::getSupportedVersion : " + SemanticVersion.getSupportedVersion()));
	}

	@Test
	public void test_parse()
	{
		System.out.println("test_parse");
		final SemanticVersion version = new SemanticVersion("1.2.3-rc.1+b17");
		assertEquals(1, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getPatch());
		assertEquals("rc.1", version.getPreRelease().toString());
		assertFalse(version.getPreRelease().isBlank());
		assertFalse(version.getBuild().isBlank());
		final SemanticVersion version0 = new SemanticVersion("0.0.0");
		assertTrue(version0.getPreRelease().isBlank());
		assertTrue(new SemanticVersion().getBuild().isBlank());
		assertEquals("b17", version.getBuild().toString());
		assertEquals(new SemanticVersion(1, 2, 3,
			new PreRelease("rc.1"), new Build("b17")), version);
		assertDoesNotThrow(() -> new SemanticVersion("1.0.0-a00"));
		assertDoesNotThrow(() -> new SemanticVersion("1.0.0-a0a"));
		assertDoesNotThrow(() -> new SemanticVersion("1.0.0-0a0"));
		final SemanticVersion sv1 = new SemanticVersion("1.0.0-Hello-World");
		assertDoesNotThrow(() -> sv1);
		assertEquals(1, sv1.getPreRelease().getListIdentifiers().size());
		assertEquals("Hello-World", sv1.getPreRelease().getListIdentifiers().getFirst().toString());
		final SemanticVersion sv2 = new SemanticVersion("1.0.0+r17-rc.1");
		assertDoesNotThrow(() -> sv2);
		assertTrue(sv2.getPreRelease().getListIdentifiers().isEmpty());
		assertEquals("r17-rc.1", sv2.getBuild().toString());
		final SemanticVersion sv3 = new SemanticVersion("1.0.0+-");
		assertDoesNotThrow(() -> sv3);
		assertTrue(sv3.getPreRelease().getListIdentifiers().isEmpty());
		assertEquals("-", sv3.getBuild().toString());
		final SemanticVersion sv4 = new SemanticVersion("1.0.0--");
		assertDoesNotThrow(() -> sv4);
		assertEquals(1, sv4.getPreRelease().getListIdentifiers().size());
		assertEquals("-", sv4.getPreRelease().getListIdentifiers().getFirst().toString());
		final SemanticVersion sv5 = new SemanticVersion("1.0.0-0-0");
		assertDoesNotThrow(() -> sv5);
		assertEquals(1, sv5.getPreRelease().getListIdentifiers().size());
		assertEquals("0-0", sv5.getPreRelease().getListIdentifiers().getFirst().toString());
		assertEquals(2, new SemanticVersion("1.0.0-0.0").getPreRelease().getListIdentifiers().size());
		assertEquals(new SemanticVersion(), new SemanticVersion("0.0.0-0"));
		assertEquals(new SemanticVersion(0, 0, 0, new PreRelease(),
			new Build()), version0);
		final SemanticVersion sv6 = new SemanticVersion("1.0.0+001");
		assertDoesNotThrow(() -> sv6);
		final Build build6 = sv6.getBuild();
		assertFalse(build6.isBlank());
		assertEquals(1, build6.getListIdentifiers().size());
		final Identifier id6 = build6.getListIdentifiers().get(0);
		assertTrue(id6.isNumeric());
		assertEquals(1, id6.getNumber());
		assertEquals("001", id6.toString());
	}

	@Test
	public void test_invalid_version_strings()
	{
		List.of(
			"",
			"1",
			"1.",
			"1.0",
			"1.0.",
			"-1.0.0",
			"1.x.y",
			"1.0.0- 0",
			"1.0.0 -0",
			"1.0.0-00",
			"1.0.0-01",
			"1.0.0-a.00",
			"1.0.0-a.01",
			"x1.0.0",
			"1.0.0y",
			"1.0.0-.",
			"1.0.0+.",
			"1.0.0-a..b",
			"1.0.0+a..b"
		).forEach(version ->
			assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(version)));
	}

	private void test_Identifiers(List<Identifier> identifiers)
	{
		assertEquals(3, identifiers.size());
		assertTrue(identifiers.get(0).isNumeric());
		assertEquals(5, identifiers.get(0).getNumber());
		assertEquals("5", identifiers.get(0).toString());
		assertFalse(identifiers.get(1).isNumeric());
		assertTrue(identifiers.get(1).getNumber() < 0);
		assertEquals("b", identifiers.get(1).toString());
		assertTrue(identifiers.get(2).isNumeric());
		assertEquals(7, identifiers.get(2).getNumber());
		assertEquals("7", identifiers.get(2).toString());
	}

	@Test
	public void test_PreRelease()
	{
		test_Identifiers(new SemanticVersion("1.2.3-5.b.7").getPreRelease().getListIdentifiers());
	}

	@Test
	public void test_Build()
	{
		test_Identifiers(new SemanticVersion("1.2.3+5.b.7").getBuild().getListIdentifiers());
	}

	@Test
	public void test_equals()
	{
		System.out.println("test_equals");
		final String str1 = "1.2.3+x";
		final SemanticVersion v1 = new SemanticVersion(str1);
		final SemanticVersion v2 = new SemanticVersion("1.2.3+y");
		final SemanticVersion v3 = new SemanticVersion("1.1.1-rc.1");
		assertTrue(v1.equals(v2));
		assertFalse(v1.equals(str1));
		assertFalse(v1.equals(null));
		assertFalse(new SemanticVersion("2.1.1-rc.1").equals(v3));
		assertFalse(new SemanticVersion("1.2.1-rc.1").equals(v3));
		assertFalse(new SemanticVersion("1.1.2-rc.1").equals(v3));
		assertFalse(new SemanticVersion("1.1.1-rc.2").equals(v3));
	}

	@Test
	public void test_compareTo()
	{
		System.out.println("test_compareTo");
		final List<SemanticVersion> listAscending = getVersionListAscending();
		final int maxLength = listAscending.stream()
			.map(SemanticVersion::toString).mapToInt(String::length)
			.max().getAsInt();
		for (int i = 1; i < listAscending.size(); i++)
		{
			final SemanticVersion v1 = listAscending.get(i - 1);
			final SemanticVersion v2 = listAscending.get(i);
			System.out.println(("· %" + maxLength + "s < %s").formatted(v1, v2));
			assertTrue(v1.compareTo(v2) < 0);
			assertTrue(v2.compareTo(v1) > 0);
		}
		assertIterableEquals(listAscending,
			listAscending.stream().collect(toCollection(TreeSet::new)));
		final var sv1 = new SemanticVersion("0.0.0-1+1");
		assertFalse(sv1.getPreRelease().isBlank());
		assertFalse(sv1.getBuild().isBlank());
		assertEquals(sv1.getPreRelease().toString(), sv1.getBuild().toString());
		assertFalse(sv1.getPreRelease().equals(sv1.getBuild()));
		assertFalse(sv1.getBuild().equals(sv1.getPreRelease()));
	}

	@Test
	public void test_hashCode()
	{
		System.out.println("test_hashCode");
		final var sv1 = new SemanticVersion("1.0.0-x+aaa");
		final var sv2 = new SemanticVersion("1.0.0-x+bbb");
		assertEquals(sv1, sv2);
		assertEquals(sv1.hashCode(), sv2.hashCode());
		// just list hashcodes:
		final List<SemanticVersion> listAscending = getVersionListAscending();
		final int maxLength = listAscending.stream()
			.map(SemanticVersion::toString).mapToInt(String::length)
			.max().getAsInt();
		listAscending.stream()
			.map(sv -> ("· %" + maxLength + "s : # %+11d").formatted(sv, sv.hashCode()))
			.forEach(System.out::println);
	}

	private void _test_toString(String version)
	{
		assertEquals(version, new SemanticVersion(version).toString());
	}

	@Test
	public void test_toString()
	{
		System.out.println("test_toString");
		_test_toString("1.0.0-rc.1");
		_test_toString("1.0.0+r17");
		_test_toString("1.0.0-rc.1+r17");
		_test_toString("1.0.0");
		_test_toString("2.3.4-rc.1+b17");
		LIST_SORTED_STRICTLY_ASCENDING.forEach(version ->
			assertEquals(version, new SemanticVersion(version).toString()));
	}

	@Test
	public void test_getDescription()
	{
		System.out.println("test_getDescription");
		final List<SemanticVersion> listAscending = getVersionListAscending();
		final int maxLength = listAscending.stream()
			.map(SemanticVersion::toString).mapToInt(String::length)
			.max().getAsInt();
		listAscending.stream()
			.map(sv -> ("· %" + maxLength + "s -> %s").formatted(sv, sv.getDescription()))
			.forEach(System.out::println);
	}
}
