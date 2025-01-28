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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

import static javafx.beans.binding.Bindings.when;

/// Class to describe an ImageLayer selection shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageLayerShape
{
	private final Polygon polygon = new Polygon();
	private final BooleanProperty selected = new SimpleBooleanProperty();

	public ImageLayerShape()
	{
		polygon.setFill(Color.TRANSPARENT);
		polygon.setStrokeLineCap(StrokeLineCap.ROUND);
		polygon.setStrokeLineJoin(StrokeLineJoin.ROUND);
		polygon.setStrokeType(StrokeType.INSIDE);
		polygon.strokeProperty().bind(
			when(selected).then(Color.CORNFLOWERBLUE).otherwise(Color.ALICEBLUE));
		polygon.strokeWidthProperty().bind(when(selected).then(4).otherwise(1));
	}

	BooleanProperty selectedProperty()
	{
		return selected;
	}

	BooleanProperty visibleProperty()
	{
		return getShape().visibleProperty();
	}

	void setShapePoints(Double... points)
	{
		polygon.getPoints().setAll(points);
	}

	void clearPoints()
	{
		polygon.getPoints().clear();
	}

	Shape getShape()
	{
		return polygon;
	}
}
