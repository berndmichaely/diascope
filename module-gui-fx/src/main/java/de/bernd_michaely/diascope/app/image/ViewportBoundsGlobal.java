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

import de.bernd_michaely.diascope.app.util.beans.property.ConstDoubleProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

/// Class to describe viewport global viewport bounds.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ViewportBoundsGlobal implements ScrollableViewportBounds
{
	private final ReadOnlyDoubleProperty x = new ConstDoubleProperty(0d);
	private final ReadOnlyDoubleProperty y = new ConstDoubleProperty(0d);
	private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper();
	private final DoubleProperty scrollPosX = new SimpleDoubleProperty();
	private final DoubleProperty scrollPosY = new SimpleDoubleProperty();

	ViewportBoundsGlobal(ObservableDoubleValue width, ObservableDoubleValue height)
	{
		this.width.bind(width);
		this.height.bind(height);
	}

	@Override
	public ReadOnlyDoubleProperty xProperty()
	{
		return x;
	}

	@Override
	public ReadOnlyDoubleProperty yProperty()
	{
		return y;
	}

	@Override
	public ReadOnlyDoubleProperty widthProperty()
	{
		return width.getReadOnlyProperty();
	}

	@Override
	public ReadOnlyDoubleProperty heightProperty()
	{
		return height.getReadOnlyProperty();
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
