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

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNullElse;

/**
 * Utility for semantic versioning.
 *
 * @see <a href="https://semver.org">semver.org</a>
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SemanticVersion implements Comparable<SemanticVersion>
{
	private static @MonotonicNonNull SemanticVersion VERSION_SEMVER;

	/**
	 * Official regular expression for semantic versioning.
	 *
	 * @see <a href="https://semver.org">semver.org</a>
	 */
	public static final String STR_REGEX_SEMANTIC_VERSION =
		"^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
	private static @MonotonicNonNull Pattern REGEX_SEMANTIC_VERSION;

	private final int major;
	private final int minor;
	private final int patch;
	private final PreRelease preRelease;
	private final Build build;

	/**
	 * Returns a default instance with the smallest possible semantic version.
	 */
	public SemanticVersion()
	{
		this("0.0.0-0");
	}

	/**
	 * Creates a new instance.
	 *
	 * @param semanticVersion a String containing a semantic version
	 * @throws IllegalArgumentException if the given semantic version String is
	 *                                  invalid (including null)
	 */
	public SemanticVersion(String semanticVersion)
	{
		final var matcher = getRegExSemanticVersion().matcher(requireNonNullElse(semanticVersion, ""));
		if (matcher.matches())
		{
			try
			{
				this.major = parseInt(requireNonNullElse(matcher.group(1), ""));
				this.minor = parseInt(requireNonNullElse(matcher.group(2), ""));
				this.patch = parseInt(requireNonNullElse(matcher.group(3), ""));
				this.preRelease = new PreRelease(requireNonNullElse(matcher.group(4), ""));
				this.build = new Build(requireNonNullElse(matcher.group(5), ""));
			}
			catch (NumberFormatException ex)
			{
				throw new IllegalStateException(ex);
			}
		}
		else
		{
			throw new IllegalArgumentException(
				"»" + semanticVersion + "« is not a valid Semantic Version String");
		}
	}

	SemanticVersion(int major, int minor, int patch, PreRelease preRelease, Build build)
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.preRelease = preRelease != null ? preRelease : new PreRelease();
		this.build = build != null ? build : new Build();
	}

	/**
	 * Returns the supported version of the semantic version standard.
	 *
	 * @return the supported version of the semantic version standard
	 */
	public static SemanticVersion getSupportedVersion()
	{
		if (VERSION_SEMVER == null)
		{
			VERSION_SEMVER = new SemanticVersion("2.0.0");
		}
		return VERSION_SEMVER;
	}

	/**
	 * Returns the major version.
	 *
	 * @return the major version
	 */
	public int getMajor()
	{
		return major;
	}

	/**
	 * Returns the minor version.
	 *
	 * @return the minor version
	 */
	public int getMinor()
	{
		return minor;
	}

	/**
	 * Returns the patch version.
	 *
	 * @return the patch version
	 */
	public int getPatch()
	{
		return patch;
	}

	/**
	 * Returns the pre-release version.
	 *
	 * @return the pre-release version
	 */
	public PreRelease getPreRelease()
	{
		return preRelease;
	}

	/**
	 * Returns the build version.
	 *
	 * @return the build version
	 */
	public Build getBuild()
	{
		return build;
	}

	/**
	 * Returns a semantic versioning regex pattern.
	 *
	 * @return a semantic versioning regex pattern
	 * @see #REGEX_SEMANTIC_VERSION
	 */
	private static Pattern getRegExSemanticVersion()
	{
		if (REGEX_SEMANTIC_VERSION == null)
		{
			REGEX_SEMANTIC_VERSION = Pattern.compile(STR_REGEX_SEMANTIC_VERSION);
		}
		return REGEX_SEMANTIC_VERSION;
	}

	private static final Comparator<SemanticVersion> semanticVersionComparator =
		Comparator.comparingInt(SemanticVersion::getMajor)
			.thenComparingInt(SemanticVersion::getMinor)
			.thenComparingInt(SemanticVersion::getPatch)
			.thenComparing(SemanticVersion::getPreRelease);

	public static final Comparator<SemanticVersion> getComparator()
	{
		return semanticVersionComparator;
	}

	@Override
	public int compareTo(SemanticVersion other)
	{
		return semanticVersionComparator.compare(this, other);
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof SemanticVersion other)
		{
			return this.compareTo(other) == 0;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(major, minor, patch, preRelease);
	}

	/**
	 * Returns a more verbose string than the canonical form.
	 *
	 * @return a more verbose string than the canonical form
	 */
	public String getDescription()
	{
		return "%d.%d.%d%s%s".formatted(major, minor, patch,
			preRelease.isBlank() ? "" : " pre-release »" + preRelease + "«",
			build.isBlank() ? "" : " build »" + build + "«");
	}

	/**
	 * Returns the semantic version in its canonical form.
	 *
	 * @return the canonical form of the semantic version
	 * @see #getDescription()
	 */
	@Override
	public String toString()
	{
		return "%d.%d.%d%s%s".formatted(major, minor, patch,
			preRelease.isBlank() ? "" : "-" + preRelease,
			build.isBlank() ? "" : "+" + build);
	}
}
