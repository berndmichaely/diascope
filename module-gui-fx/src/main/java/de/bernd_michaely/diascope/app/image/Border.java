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
package de.bernd_michaely.diascope.app.image;

/// Enumeration of borders and corners of a rectangular area.
/// Borders are ordered in ascending order of angles.
/// Corners are associated to a pair of a border and its following border.
/// E.g. (for a square):
///
///   - ordinal 0: border RIGHT   (0°), corner RIGHT-BOTTOM (45°)
///   - ordinal 1: border BOTTOM (90°), corner BOTTOM-LEFT (135°)
///   - ordinal 2: border LEFT  (180°), corner LEFT-TOP    (225°)
///   - ordinal 3: border TOP   (270°), corner TOP-RIGHT   (315°)
///
enum Border
{
	RIGHT, BOTTOM, LEFT, TOP;

	Border previous()
	{
		final int o = ordinal();
		final int n = values().length;
		return values()[o > 0 ? o - 1 : n - 1];
	}

	/// Get the next Border in ascending order of angles.
	///
	/// @return the next Border in ascending order of angles
	///
	Border next()
	{
		final int o = ordinal();
		final int n = values().length;
		return values()[o == n - 1 ? 0 : o + 1];
	}

	/// Returns the number of corner points between two given borders
	/// in ascending order of angles.
	///
	/// @param border1 the first given Border
	/// @param border2 the second given Border
	/// @return the number of corner points between the given borders
	///
	static int numberOfCornerPointsBetween(Border border1, Border border2)
	{
		final int i1 = border1.ordinal();
		final int i2 = border2.ordinal();
		final int n = values().length;
		return i1 <= i2 ? i2 - i1 : n + i2 - i1;
	}
}
