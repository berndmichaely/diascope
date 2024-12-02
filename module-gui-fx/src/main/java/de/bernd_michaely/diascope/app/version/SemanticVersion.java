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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
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
	private final String build;

	public SemanticVersion()
	{
		this(0, 0, 0, new PreRelease(), "");
	}

	SemanticVersion(int major, int minor, int patch, String preRelease, String build)
	{
		this(major, minor, patch, new PreRelease(preRelease), build);
	}

	private SemanticVersion(int major, int minor, int patch, PreRelease preRelease, String build)
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.preRelease = preRelease != null ? preRelease : new PreRelease();
		this.build = build != null ? build : "";
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
	public String getBuild()
	{
		return build;
	}

	/**
	 * Returns a semantic versioning regex pattern.
	 *
	 * @return a semantic versioning regex pattern
	 * @see #REGEX_SEMANTIC_VERSION
	 */
	static Pattern getRegExSemanticVersion()
	{
		if (REGEX_SEMANTIC_VERSION == null)
		{
			REGEX_SEMANTIC_VERSION = Pattern.compile(STR_REGEX_SEMANTIC_VERSION);
		}
		return REGEX_SEMANTIC_VERSION;
	}

	/**
	 * Factory method. Based on {@link #STR_REGEX_SEMANTIC_VERSION}.
	 *
	 * @param semanticVersion a semantic versioning String
	 * @return a new optional instance. May be empty, if semanticVersion is
	 *         invalid
	 */
	public static Optional<SemanticVersion> parseSemanticVersion(String semanticVersion)
	{
		SemanticVersion result = null;
		if (semanticVersion != null)
		{
			final var matcher = getRegExSemanticVersion().matcher(semanticVersion);
			if (matcher.matches())
			{
				try
				{
					result = new SemanticVersion(
						parseInt(requireNonNullElse(matcher.group(1), "")),
						parseInt(requireNonNullElse(matcher.group(2), "")),
						parseInt(requireNonNullElse(matcher.group(3), "")),
						new PreRelease(requireNonNullElse(matcher.group(4), "")),
						requireNonNullElse(matcher.group(5), ""));
				}
				catch (NumberFormatException ex)
				{
					result = null;
				}
			}
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Class to handle a semantic version pre-release.
	 */
	public static class PreRelease implements Comparable<PreRelease>
	{
		private final String preRelease;

		private static class Identifier implements Comparable<Identifier>
		{
			private final String part;
			private boolean isNumeric;
			private int num;

			private Identifier(String part)
			{
				this.part = part;
				try
				{
					num = parseInt(part);
					isNumeric = true;
				}
				catch (NumberFormatException ex)
				{
					isNumeric = false;
				}
			}

			@Override
			public int compareTo(Identifier other)
			{
				if (this.isNumeric && other.isNumeric)
				{
					return Integer.compare(this.num, other.num);
				}
				else if (!this.isNumeric && !other.isNumeric)
				{
					return this.part.compareTo(other.part);
				}
				else
				{
					return this.isNumeric ? -1 : 1;
				}
			}

			@Override
			public boolean equals(@Nullable Object object)
			{
				if (object instanceof Identifier other)
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
				return part.hashCode();
			}
		}

		private PreRelease()
		{
			this("");
		}

		private PreRelease(String preRelease)
		{
			this.preRelease = preRelease == null || preRelease.isBlank() ? "" : preRelease;
		}

		@Override
		public int compareTo(PreRelease other)
		{
			final boolean thisIsEmpty = this.preRelease.isEmpty();
			final boolean otherIsEmpty = other.preRelease.isEmpty();
			if (thisIsEmpty || otherIsEmpty)
			{
				return thisIsEmpty ? (otherIsEmpty ? 0 : 1) : -1;
			}
			else
			{
				final String delimiter = "\\.";
				return Arrays.compare(this.preRelease.split(delimiter), other.preRelease.split(delimiter),
					Comparator.comparing(Identifier::new));
			}
		}

		@Override
		public boolean equals(@Nullable Object object)
		{
			if (object instanceof PreRelease other)
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
			return preRelease.hashCode();
		}

		@Override
		public String toString()
		{
			return preRelease;
		}
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
			preRelease.toString().isBlank() ? "" : " pre-release »" + preRelease + "«",
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
			preRelease.toString().isBlank() ? "" : "-" + preRelease,
			build.isBlank() ? "" : "+" + build);
	}
}
