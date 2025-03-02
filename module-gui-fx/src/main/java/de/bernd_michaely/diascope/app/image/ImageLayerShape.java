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

import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.checkerframework.checker.nullness.qual.Nullable;

import static javafx.beans.binding.Bindings.when;

/// Class to describe an ImageLayer selection shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageLayerShape
{
	private static final Color COLOR_SELECTED = Color.CORNFLOWERBLUE;
	private static final double STROKE_WIDTH_SELECTED = 4.0;
	static final Color COLOR_UNSELECTED = Color.ALICEBLUE;
	static final double STROKE_WIDTH_UNSELECTED = 1.0;
	private final Polygon polygon = new Polygon();
	private final BooleanProperty selected = new SimpleBooleanProperty();
	private final BooleanProperty unselectedVisible = new SimpleBooleanProperty();
	private boolean mouseDragged;
	private @Nullable Consumer<Boolean> layerSelectionHandler;

	ImageLayerShape()
	{
		polygon.setFill(Color.TRANSPARENT);
		polygon.setStrokeLineCap(StrokeLineCap.ROUND);
		polygon.setStrokeLineJoin(StrokeLineJoin.ROUND);
		polygon.setStrokeType(StrokeType.INSIDE);
		polygon.strokeProperty().bind(when(selected).then(COLOR_SELECTED).otherwise(
			when(unselectedVisible).then(COLOR_UNSELECTED).otherwise(Color.TRANSPARENT)));
		polygon.strokeWidthProperty().bind(when(selected).then(STROKE_WIDTH_SELECTED).otherwise(
			when(unselectedVisible).then(STROKE_WIDTH_UNSELECTED).otherwise(0.0)));
		polygon.setOnMouseDragged(event ->
		{
			mouseDragged = true;
		});
		polygon.setOnMouseReleased(event ->
		{
			if (!mouseDragged && event.getButton().equals(MouseButton.PRIMARY) &&
				event.getClickCount() == 1 && !event.isShiftDown() && !event.isAltDown())
			{
				if (layerSelectionHandler != null)
				{
					layerSelectionHandler.accept(event.isControlDown());
				}
			}
			mouseDragged = false;
		});
	}

	void setLayerSelectionHandler(Consumer<Boolean> layerSelectionHandler)
	{
		this.layerSelectionHandler = layerSelectionHandler;
	}

	BooleanProperty selectedProperty()
	{
		return selected;
	}

	BooleanProperty unselectedVisibleProperty()
	{
		return unselectedVisible;
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
