/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/// Class to describe image layer local viewport bounds.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ViewportBoundsLocal implements ViewportBounds
{
	private final DoubleProperty x = new SimpleDoubleProperty();
	private final DoubleProperty y = new SimpleDoubleProperty();
	private final DoubleProperty width = new SimpleDoubleProperty();
	private final DoubleProperty height = new SimpleDoubleProperty();
	private final DoubleProperty scrollPosX = new SimpleDoubleProperty();
	private final DoubleProperty scrollPosY = new SimpleDoubleProperty();

	@Override
	public DoubleProperty xProperty()
	{
		return x;
	}

	@Override
	public DoubleProperty yProperty()
	{
		return y;
	}

	@Override
	public DoubleProperty widthProperty()
	{
		return width;
	}

	@Override
	public DoubleProperty heightProperty()
	{
		return height;
	}

	@Override
	public DoubleProperty scrollPosXProperty()
	{
		return scrollPosX;
	}

	@Override
	public DoubleProperty scrollPosYProperty()
	{
		return scrollPosY;
	}
}
