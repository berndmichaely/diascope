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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/// Properties for external control of image transforms.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ImageTransforms
{
	private final ObjectProperty<ZoomMode> zoomMode;
	private final DoubleProperty zoomFixed;
	private final ReadOnlyDoubleWrapper zoomFactor;
	private final DoubleProperty rotate;
	private final BooleanProperty mirrorX, mirrorY;

	ImageTransforms()
	{
		this.zoomMode = new SimpleObjectProperty<>(ZoomMode.getDefault());
		this.zoomFixed = new SimpleDoubleProperty(1.0);
		this.zoomFactor = new ReadOnlyDoubleWrapper();
		this.rotate = new SimpleDoubleProperty(0.0);
		this.mirrorX = new SimpleBooleanProperty();
		this.mirrorY = new SimpleBooleanProperty();
	}

	/// Bind this transforms to other transforms.
	///
	/// @param other the other transforms
	///
	void bindProperties(ImageTransforms other)
	{
		this.zoomMode.bind(other.zoomMode);
		this.zoomFixed.bind(other.zoomFixed);
//		other.zoomFactorWrapperProperty().bind(this.zoomFactor);
		this.rotate.bind(other.rotate);
		this.mirrorX.bind(other.mirrorX);
		this.mirrorY.bind(other.mirrorY);
	}

	/// Unbind this transforms from other transforms.
	///
	/// @param other the other transforms
	///
	void unbindProperties(ImageTransforms other)
	{
		this.zoomMode.unbind();
		this.zoomFixed.unbind();
//		other.zoomFactorWrapperProperty().unbind();
		this.rotate.unbind();
		this.mirrorX.unbind();
		this.mirrorY.unbind();
	}

	public ObjectProperty<ZoomMode> zoomModeProperty()
	{
		return zoomMode;
	}

	public DoubleProperty zoomFixedProperty()
	{
		return zoomFixed;
	}

	ReadOnlyDoubleWrapper zoomFactorWrapperProperty()
	{
		return zoomFactor;
	}

	public ReadOnlyDoubleProperty zoomFactorProperty()
	{
		return zoomFactor.getReadOnlyProperty();
	}

	public DoubleProperty rotateProperty()
	{
		return rotate;
	}

	public BooleanProperty mirrorXProperty()
	{
		return mirrorX;
	}

	public BooleanProperty mirrorYProperty()
	{
		return mirrorY;
	}
}
