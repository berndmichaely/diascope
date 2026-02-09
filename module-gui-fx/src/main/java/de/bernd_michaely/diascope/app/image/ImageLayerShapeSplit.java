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

import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/// Class to describe an ImageLayer selection shape for SPLIT mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayerShapeSplit extends ImageLayerShapeBaseStroke
{
	private final Polygon polygon = new Polygon();
	private final Rectangle rectangle = new Rectangle();
	private final ReadOnlyObjectProperty<Mode> modeProperty;

	private ImageLayerShapeSplit(ReadOnlyObjectProperty<Mode> modeProperty)
	{
		super(false, null, null);
		this.modeProperty = modeProperty;
	}

	static ImageLayerShapeSplit createInstance(ReadOnlyObjectProperty<Mode> modeProperty)
	{
		final var imageLayerShape = new ImageLayerShapeSplit(modeProperty);
		imageLayerShape._postInit();
		return imageLayerShape;
	}

	void setShapePoints(Double... points)
	{
		polygon.getPoints().setAll(points);
	}

	void clearPoints()
	{
		polygon.getPoints().clear();
	}

	@Override
	Shape getShape()
	{
		return modeProperty.get().equals(Mode.SPLIT) ? polygon : rectangle;
	}
}
