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

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.value.ObservableBooleanValue;

import static javafx.beans.binding.Bindings.when;

/// ViewportBounds implementation to switch between a global and a
/// local (image layer) viewport.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ViewportBoundsSwitch implements ViewportBounds
{
	private final ReadOnlyDoubleWrapper x = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper y = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper scrollPosX = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper scrollPosY = new ReadOnlyDoubleWrapper();

	/// Switch between a global and a local viewport.
	///
	/// @param isLocal true to bind to the given local ViewportBounds,
	///                false to bind to the given global ViewportBounds
	/// @param global the given global ViewportBounds
	/// @param local the given local ViewportBounds
	///
	ViewportBoundsSwitch(ObservableBooleanValue isLocal,
		ViewportBoundsGlobal global, ViewportBoundsLocal local)
	{
		x.bind(when(isLocal).then(local.xProperty()).otherwise(global.xProperty()));
		y.bind(when(isLocal).then(local.yProperty()).otherwise(global.yProperty()));
		width.bind(when(isLocal).then(local.widthProperty()).otherwise(global.widthProperty()));
		height.bind(when(isLocal).then(local.heightProperty()).otherwise(global.heightProperty()));
		scrollPosX.bind(when(isLocal).then(local.scrollPosXProperty()).otherwise(global.scrollPosXProperty()));
		scrollPosY.bind(when(isLocal).then(local.scrollPosYProperty()).otherwise(global.scrollPosYProperty()));
	}

	@Override
	public ReadOnlyDoubleProperty xProperty()
	{
		return x.getReadOnlyProperty();
	}

	@Override
	public ReadOnlyDoubleProperty yProperty()
	{
		return y.getReadOnlyProperty();
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
	public ReadOnlyDoubleProperty scrollPosXProperty()
	{
		return scrollPosX.getReadOnlyProperty();
	}

	@Override
	public ReadOnlyDoubleProperty scrollPosYProperty()
	{
		return scrollPosY.getReadOnlyProperty();
	}
}
