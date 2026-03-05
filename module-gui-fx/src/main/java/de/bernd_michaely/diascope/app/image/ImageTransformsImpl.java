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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Implementation of the `ImageTransforms` interface providing
/// bindings between instances.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageTransformsImpl implements ImageTransforms, AutoCloseable
{
	// control properties:
	private final EnumProperties<ZoomMode> zoomModeProperties;
	private final DoubleProperty zoomFixed;
	private final DoubleProperty rotate;
	private final BooleanProperty mirrorX, mirrorY;
	// calculated properties:
	private final ReadOnlyDoubleWrapper resultingZoomFactor;
	private @Nullable ReadOnlyDoubleWrapper otherResultingZoomFactor;

	ImageTransformsImpl()
	{
		this.zoomModeProperties = EnumProperties.createInstance(ZoomMode.getDefault());
		this.zoomFixed = new SimpleDoubleProperty(1.0);
		this.rotate = new SimpleDoubleProperty(0.0);
		this.mirrorX = new SimpleBooleanProperty();
		this.mirrorY = new SimpleBooleanProperty();
		this.resultingZoomFactor = new ReadOnlyDoubleWrapper();
	}

	/// Bind this image control transforms to the other transforms.
	///
	/// @param other the other transforms
	/// @see #unbindControlProperties()
	/// @see #adjustControlProperties(ImageTransformsImpl)
	///
	void bindControlProperties(ImageTransformsImpl other)
	{
		this.zoomModeProperties.rawValueProperty().bind(other.zoomModeProperties.rawValueProperty());
		this.zoomFixed.bind(other.zoomFixed);
		this.rotate.bind(other.rotate);
		this.mirrorX.bind(other.mirrorX);
		this.mirrorY.bind(other.mirrorY);
	}

	/// Adjust other image control transforms to this transforms.
	///
	/// @param other the other transforms
	/// @see #bindControlProperties(ImageTransformsImpl)
	/// @see #unbindControlProperties()
	///
	void adjustControlProperties(ImageTransformsImpl other)
	{
		other.zoomModeProperties.rawValueProperty().set(this.zoomModeProperties.rawValueProperty().get());
		other.zoomFixed.set(this.zoomFixed.get());
		other.rotate.set(this.rotate.get());
		other.mirrorX.set(this.mirrorX.get());
		other.mirrorY.set(this.mirrorY.get());
	}

	/// Unbind this transforms from the other transforms.
	///
	/// @see #bindControlProperties(ImageTransformsImpl)
	/// @see #adjustControlProperties(ImageTransformsImpl)
	///
	void unbindControlProperties()
	{
		this.zoomModeProperties.rawValueProperty().unbind();
		this.zoomFixed.unbind();
		this.rotate.unbind();
		this.mirrorX.unbind();
		this.mirrorY.unbind();
	}

	/// Bind this image control transforms to the other transforms.
	///
	/// @param other the other transforms
	/// @see #unbindCalculatedProperties()
	///
	void bindCalculatedProperties(ImageTransformsImpl other)
	{
		if (otherResultingZoomFactor == null)
		{
			otherResultingZoomFactor = other.resultingZoomFactorProperty();
			otherResultingZoomFactor.bind(this.resultingZoomFactor);
		}
	}

	/// Unbind this transforms from the other transforms.
	///
	/// @see #bindCalculatedProperties(ImageTransformsImpl)
	///
	void unbindCalculatedProperties()
	{
		if (otherResultingZoomFactor != null)
		{
			otherResultingZoomFactor.unbind();
			otherResultingZoomFactor = null;
		}
	}

	/// Binds all properties.
	///
	/// @see #bindControlProperties(ImageTransformsImpl)
	/// @see #bindCalculatedProperties(ImageTransformsImpl)
	///
	void bindAllProperties(ImageTransformsImpl other)
	{
		bindControlProperties(other);
		bindCalculatedProperties(other);
	}

	/// Unbinds all properties.
	///
	/// @see #unbindControlProperties()
	/// @see #unbindCalculatedProperties()
	///
	void unbindAllProperties()
	{
		unbindCalculatedProperties();
		unbindControlProperties();
	}

	ReadOnlyDoubleWrapper resultingZoomFactorProperty()
	{
		return resultingZoomFactor;
	}

	@Override
	public ReadOnlyDoubleProperty zoomFactorProperty()
	{
		return resultingZoomFactorProperty().getReadOnlyProperty();
	}

	EnumProperties<ZoomMode> zoomModeProperties()
	{
		return zoomModeProperties;
	}

	@Override
	public ReadOnlyObjectProperty<ZoomMode> zoomModeOrDefaultProperty()
	{
		return zoomModeProperties().valueOrDefaultProperty();
	}

	@Override
	public ObjectProperty<@Nullable ZoomMode> zoomModeRawValueProperty()
	{
		return zoomModeProperties().rawValueProperty();
	}

	@Override
	public DoubleProperty zoomFixedProperty()
	{
		return zoomFixed;
	}

	@Override
	public DoubleProperty rotateProperty()
	{
		return rotate;
	}

	@Override
	public BooleanProperty mirrorXProperty()
	{
		return mirrorX;
	}

	@Override
	public BooleanProperty mirrorYProperty()
	{
		return mirrorY;
	}

	/// {@inheritDoc}
	///
	/// This implementation unbinds all properties.
	///
	@Override
	public void close()
	{
		unbindAllProperties();
	}
}
