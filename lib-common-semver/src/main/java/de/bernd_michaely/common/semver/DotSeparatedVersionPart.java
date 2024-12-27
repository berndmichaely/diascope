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

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Base class to handle a dot separated semantic version part.
 */
sealed public abstract class DotSeparatedVersionPart permits PreRelease, Build
{
	private static final String DELIMITER = "\\.";
	private static final Identifier[] NULL = new Identifier[0];
	private final String versionPart;
	private Identifier[] identifiers = NULL;
	private @MonotonicNonNull List<Identifier> listIdentifiers;

	DotSeparatedVersionPart()
	{
		this("");
	}

	DotSeparatedVersionPart(String build)
	{
		this.versionPart = build;
	}

	public boolean isBlank()
	{
		return versionPart.isBlank();
	}

	String getVersionPart()
	{
		return versionPart;
	}

	private Identifier[] getIdentifiers()
	{
		if (identifiers == NULL)
		{
			identifiers = versionPart.isBlank() ? new Identifier[0] :
				Arrays.stream(versionPart.split(DELIMITER)).map(Identifier::new).toArray(Identifier[]::new);
		}
		return identifiers;
	}

	/**
	 * Returns an unmodifiable list of the identifiers of this PreRelease.
	 *
	 * @return an unmodifiable list of identifiers. For an empty PreRelease, an
	 *         empty list is returned (<em>not</em> a one element list containing
	 *         an empty Identifier).
	 */
	public List<Identifier> getListIdentifiers()
	{
		if (listIdentifiers == null)
		{
			listIdentifiers = List.of(getIdentifiers());
		}
		return listIdentifiers;
	}

	int compareTo(DotSeparatedVersionPart other)
	{
		final boolean thisIsEmpty = this.versionPart.isEmpty();
		final boolean otherIsEmpty = other.versionPart.isEmpty();
		if (thisIsEmpty || otherIsEmpty)
		{
			return thisIsEmpty ? (otherIsEmpty ? 0 : 1) : -1;
		}
		else
		{
			return Arrays.compare(this.getIdentifiers(), other.getIdentifiers());
		}
	}

	/**
	 * Returns the original constructor parameter.
	 *
	 * @return the original constructor parameter
	 */
	@Override
	public String toString()
	{
		return versionPart;
	}
}
