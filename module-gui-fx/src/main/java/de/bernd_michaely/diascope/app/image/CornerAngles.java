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

import java.util.EnumMap;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

import static de.bernd_michaely.diascope.app.image.Bindings.arctan;
import static de.bernd_michaely.diascope.app.image.Border.BOTTOM;
import static de.bernd_michaely.diascope.app.image.Border.LEFT;
import static de.bernd_michaely.diascope.app.image.Border.RIGHT;
import static de.bernd_michaely.diascope.app.image.Border.TOP;

/// Class to handle viewport corner angles.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class CornerAngles
{
	private final EnumMap<Border, ReadOnlyDoubleWrapper> cornerAngles;

	/// Creates a new instance.
	///
	/// @param x the x coordinate of the split center point
	/// @param y the y coordinate of the split center point
	/// @param dx delta x to the viewport width
	/// @param dy delta y to the viewport height
	///
	CornerAngles(
		ReadOnlyDoubleProperty x, ReadOnlyDoubleProperty y,
		ReadOnlyDoubleProperty dx, ReadOnlyDoubleProperty dy)
	{
		this.cornerAngles = new EnumMap<>(Border.class);
		for (var border : Border.values())
		{
			final var property = new ReadOnlyDoubleWrapper();
			cornerAngles.put(border, property);
			switch (border)
			{
				case RIGHT ->
					property.bind(arctan(dy.divide(dx)));
				case BOTTOM ->
					property.bind(arctan(x.divide(dy)).add(90.0));
				case LEFT ->
					property.bind(arctan(y.divide(x)).add(180.0));
				case TOP ->
					property.bind(arctan(dx.divide(y)).add(270.0));
				default ->
					throw new IllegalStateException("Invalid border: " + border);
			}
		}
	}

	/// Returns a property indicating the angle of the split center point to the
	/// given viewport corner.
	///
	/// @param  border the given corner indicated by the associated border
	/// @return a property indicating the angle of the split center point to the
	///         given viewport corner
	/// @see    Border
	///
	ReadOnlyDoubleProperty get(Border border)
	{
		final var prop = cornerAngles.get(border);
		if (prop == null)
		{
			throw new IllegalStateException(
				"Corner angle not initialized for Border: " + border);
		}
		return prop.getReadOnlyProperty();
	}
}
