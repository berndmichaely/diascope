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

import de.bernd_michaely.diascope.app.image.ImageLayer.Type;
import javafx.scene.shape.Polygon;

import static de.bernd_michaely.diascope.app.image.ImageLayer.Type.*;

/// Class to describe an ImageLayer selection shape for SPLIT mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayerShapeSplit extends ImageLayerShapeBase
{
	private final Polygon polygon = new Polygon();

	private ImageLayerShapeSplit()
	{
		super(false, null, null);
	}

	static ImageLayerShapeSplit createInstance()
	{
		final var imageLayerShape = new ImageLayerShapeSplit();
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
	Type getType()
	{
		return SPLIT;
	}

	@Override
	Polygon getShape()
	{
		return polygon;
	}
}
