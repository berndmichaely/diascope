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

/// Class to handle image layer grid dividers.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class DividerGrid extends DividerBase
{
	private Orientation orientation = Orientation.values()[0];

	enum Orientation
	{
		VERTICAL, HORIZONTAL
	}

	DividerGrid()
	{
	}

	static DividerGrid createInstance(Viewport viewport)
	{
		return new DividerGrid();
	}

	Orientation getOrientation()
	{
		return orientation;
	}
}
