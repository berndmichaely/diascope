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
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import static de.bernd_michaely.diascope.app.image.ImageLayerShape.COLOR_UNSELECTED;
import static de.bernd_michaely.diascope.app.image.ImageLayerShape.STROKE_WIDTH_UNSELECTED;
import static java.lang.Math.ceil;

/// Base class for image layer dividers.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
abstract sealed class AbstractDivider permits SplitDivider, GridDivider
{
	private static final Paint COLOR_DEFAULT = COLOR_UNSELECTED;
	private static final Paint COLOR_HOVER = Color.LIGHTCORAL;
	private final Line lineShape = new Line();
	private final Line lineEvent = new Line();

	AbstractDivider()
	{
		lineShape.setStroke(COLOR_DEFAULT);
		lineShape.setStrokeWidth(ceil(Font.getDefault().getSize() / 10) * STROKE_WIDTH_UNSELECTED);
		lineEvent.setStroke(Color.TRANSPARENT);
		lineEvent.setStrokeWidth(lineShape.getStrokeWidth() * 4);
		lineEvent.visibleProperty().bind(lineShape.visibleProperty());
		lineEvent.setCursor(Cursor.HAND);
		lineEvent.setOnMouseEntered(_ -> lineShape.setStroke(COLOR_HOVER));
		lineEvent.setOnMouseExited(_ -> lineShape.setStroke(COLOR_DEFAULT));
		lineEvent.startXProperty().bind(lineShape.startXProperty());
		lineEvent.startYProperty().bind(lineShape.startYProperty());
		lineEvent.endXProperty().bind(lineShape.endXProperty());
		lineEvent.endYProperty().bind(lineShape.endYProperty());
	}

	Line getLineShape()
	{
		return lineShape;
	}

	Line getLineEvent()
	{
		return lineEvent;
	}

	DoubleProperty startXProperty()
	{
		return lineShape.startXProperty();
	}

	DoubleProperty startYProperty()
	{
		return lineShape.startYProperty();
	}

	DoubleProperty endXProperty()
	{
		return lineShape.endXProperty();
	}

	DoubleProperty endYProperty()
	{
		return lineShape.endYProperty();
	}

	BooleanProperty visibleProperty()
	{
		return getLineShape().visibleProperty();
	}
}
