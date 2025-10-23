/*
 * Copyright (C) 2025 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.util.math;

import static java.lang.Double.*;

/**
 * Utility class for operators.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class Operators
{
	/**
	 * Calculates the maximum of two double values clamped to the range
	 * {@code [0.0 .. Double.MAX_VALUE]}. {@code NaN} is mapped to {@code 0.0}.
	 *
	 * @param d1 first argument
	 * @param d2 second argument
	 * @return the maximum of the arguments clamped to the range
	 *         {@code [0.0 .. Double.MAX_VALUE]}
	 */
	public static double max_positive(double d1, double d2)
	{
		if (d1 == POSITIVE_INFINITY || d2 == POSITIVE_INFINITY)
		{
			return MAX_VALUE;
		}
		else if (isNaN(d1))
		{
			return isNaN(d2) ? 0.0 : max(0.0, d2);
		}
		else if (isNaN(d2))
		{
			return max(0.0, d1);
		}
		else
		{
			return max(0.0, max(d1, d2));
		}
	}
}
