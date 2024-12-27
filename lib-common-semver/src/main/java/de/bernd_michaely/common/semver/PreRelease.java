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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class to handle a semantic version pre-release.
 */
public final class PreRelease extends DotSeparatedVersionPart implements Comparable<PreRelease>
{
	PreRelease()
	{
		super();
	}

	PreRelease(String preRelease)
	{
		super(preRelease);
	}

	@Override
	public int compareTo(PreRelease other)
	{
		return super.compareTo(other);
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
		return getVersionPart().hashCode();
	}
}
