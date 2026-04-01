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

import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;

import static javafx.beans.binding.Bindings.when;

/// Class to handle image layer grid dividers.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class GridDivider extends AbstractDivider implements AutoCloseable
{
	private final ViewportBoundsLocal viewportBoundsLocal;
	private final EnumProperties<Orientation> orientation;

	enum Orientation
	{
		VERTICAL, HORIZONTAL
	}

	GridDivider(ViewportBoundsLocal viewportBoundsLocal)
	{
		this.viewportBoundsLocal = viewportBoundsLocal;
		this.orientation = EnumProperties.createInstance(Orientation.values()[0]);
		// TODO
		startXProperty().bind(
			when(orientation.isValueProperty(Orientation.VERTICAL))
				.then(viewportBoundsLocal.widthProperty())
				.otherwise(viewportBoundsLocal.heightProperty()));
	}

	EnumProperties<Orientation> orientationProperties()
	{
		return orientation;
	}

	Orientation getOrientation()
	{
		return orientation.getValueOrDefault();
	}

	void setOrientation(Orientation orientation)
	{
		this.orientation.setRawValue(orientation);
	}

	@Override
	public void close()
	{
		// TODO
	}
}
