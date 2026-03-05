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

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Class to describe an ImageLayer selection shape for SPOT BASE mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
@Deprecated
final class ImageLayerShapeSpotBase extends AbstractImageLayerShape
{
	private final ReadOnlyObjectWrapper<Paint> strokeSelectedPaint;

	private ImageLayerShapeSpotBase()
	{
		super(false, null, null);
		this.strokeSelectedPaint = new ReadOnlyObjectWrapper<>(Color.TRANSPARENT);
	}

	static ImageLayerShapeSpotBase createInstance()
	{
		return new ImageLayerShapeSpotBase();
	}

	@Override
	ReadOnlyObjectProperty<@Nullable Shape> clipProperty()
	{
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	@Override
	ObservableObjectValue<Paint> getStrokeSelectedPaint()
	{
		return strokeSelectedPaint.getReadOnlyProperty();
	}

	@Override
	double getStrokeWidthSelected()
	{
		return 0.0;
	}
}
