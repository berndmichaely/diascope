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
 * Class to describe dot separated version identifier parts.
 */
public class Identifier implements Comparable<Identifier>
{
	private static final int NO_NUMBER = -1;
	private final String part;
	private boolean isNumeric;
	private int number;

	Identifier(String part)
	{
		this.part = part;
		try
		{
			number = Integer.parseInt(part);
			isNumeric = true;
		}
		catch (NumberFormatException ex)
		{
			number = NO_NUMBER;
			isNumeric = false;
		}
	}

	/**
	 * Returns true, if the identifier is numeric.
	 *
	 * @return true, if the identifier is numeric
	 */
	public boolean isNumeric()
	{
		return isNumeric;
	}

	/**
	 * If the identifier is numeric, returns it as a number. Otherwise the return
	 * value is meaningless.
	 *
	 * @return the identifier as number, if it is numeric, otherwise a negative
	 *         number
	 * @see #isNumeric()
	 */
	public int getNumber()
	{
		return number;
	}

	@Override
	public int compareTo(Identifier other)
	{
		if (this.isNumeric && other.isNumeric)
		{
			return Integer.compare(this.number, other.number);
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

	/**
	 * Returns the identifier as String.
	 *
	 * @return the identifier as String
	 */
	@Override
	public String toString()
	{
		return part;
	}
}
