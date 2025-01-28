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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/// Properties for external control of image transforms.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ImageTransforms
{
	private final ObjectProperty<ZoomMode> zoomModeProperty;
	private final DoubleProperty zoomFixedProperty;
	private final DoubleProperty rotateProperty;
	private final BooleanProperty mirrorXProperty, mirrorYProperty;

	ImageTransforms()
	{
		this.zoomModeProperty = new SimpleObjectProperty<>(ZoomMode.getDefault());
		this.zoomFixedProperty = new SimpleDoubleProperty(1.0);
		this.rotateProperty = new SimpleDoubleProperty(0.0);
		this.mirrorXProperty = new SimpleBooleanProperty();
		this.mirrorYProperty = new SimpleBooleanProperty();
	}

	/// Bind this transforms to other transforms.
	///
	/// @param other the other transforms
	///
	void bindProperties(ImageTransforms other)
	{
		this.zoomModeProperty.bind(other.zoomModeProperty);
		this.zoomFixedProperty.bind(other.zoomFixedProperty);
		this.rotateProperty.bind(other.rotateProperty);
		this.mirrorXProperty.bind(other.mirrorXProperty);
		this.mirrorYProperty.bind(other.mirrorYProperty);
	}

	/// Unbind this transforms.
	///
	void unbindProperties()
	{
		this.zoomModeProperty.unbind();
		this.zoomFixedProperty.unbind();
		this.rotateProperty.unbind();
		this.mirrorXProperty.unbind();
		this.mirrorYProperty.unbind();
	}

	public ObjectProperty<ZoomMode> zoomModeProperty()
	{
		return zoomModeProperty;
	}

	public DoubleProperty zoomFixedProperty()
	{
		return zoomFixedProperty;
	}

	public DoubleProperty rotateProperty()
	{
		return rotateProperty;
	}

	public BooleanProperty mirrorXProperty()
	{
		return mirrorXProperty;
	}

	public BooleanProperty mirrorYProperty()
	{
		return mirrorYProperty;
	}
}
