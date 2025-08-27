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

/// Enumerating the available image zoom modes.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public enum ZoomMode
{
	/// The image will be shown in original size.
	ORIGINAL,
	/// The image will be zoomed to fit into the viewport.
	FIT,
	/// The image will be zoomed to fill the viewport.
	FILL,
	/// The image will be zoomed according to a given zoom factor.
	FIXED;

	public static ZoomMode getDefault()
	{
		return FIT;
	}
}
