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
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ImageLayer.Type.*;
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
	private static final double DEFAULT_SPOT_RADIUS = 100.0;
	private final Type type;
	private final Shape shape;
	private final BooleanProperty selected = new SimpleBooleanProperty();
	private final BooleanProperty unselectedVisible = new SimpleBooleanProperty();
	private boolean mouseDragged;
	private @Nullable Consumer<Boolean> layerSelectionHandler;
	private final DoubleProperty centerX = new SimpleDoubleProperty(DEFAULT_SPOT_RADIUS);
	private final DoubleProperty centerY = new SimpleDoubleProperty(DEFAULT_SPOT_RADIUS);
	private double dx, dy;

	ImageLayerShape(Type type)
	{
		this.type = type;
		this.shape = switch (type)
		{
			case SPLIT, BASE ->
				new Polygon();
			case SPOT ->
				new Circle(DEFAULT_SPOT_RADIUS);
		};
		if (shape instanceof Circle circle)
		{
			circle.centerXProperty().bind(centerX);
			circle.centerYProperty().bind(centerY);
		}
		shape.setOnMouseDragged(event ->
		{
			if (!mouseDragged)
			{
				dx = centerX.get() - event.getX();
				dy = centerY.get() - event.getY();
				mouseDragged = true;
			}
			if (type == SPOT)
			{
				centerX.set(dx + event.getX());
				centerY.set(dy + event.getY());
			}
		});
		shape.setOnMouseReleased(event ->
		{
			try
			{
				if (!mouseDragged && event.getButton().equals(MouseButton.PRIMARY) &&
					event.getClickCount() == 1 && !event.isShiftDown() && !event.isAltDown())
				{
					if (layerSelectionHandler != null)
					{
						layerSelectionHandler.accept(event.isControlDown());
					}
				}
			}
			finally
			{
				mouseDragged = false;
			}
		});
		shape.setFill(Color.TRANSPARENT);
		shape.setStrokeLineCap(StrokeLineCap.ROUND);
		shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
		shape.setStrokeType(StrokeType.INSIDE);
		shape.strokeProperty().bind(when(selected).then(COLOR_SELECTED).otherwise(
			when(unselectedVisible).then(COLOR_UNSELECTED).otherwise(Color.TRANSPARENT)));
		shape.strokeWidthProperty().bind(when(selected).then(STROKE_WIDTH_SELECTED).otherwise(
			when(unselectedVisible).then(STROKE_WIDTH_UNSELECTED).otherwise(0.0)));
	}

	Type getType()
	{
		return type;
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
		if (shape instanceof Polygon polygon)
		{
			polygon.getPoints().setAll(points);
		}
	}

	void clearPoints()
	{
		if (shape instanceof Polygon polygon)
		{
			polygon.getPoints().clear();
		}
	}

	void bindShape(Circle clip)
	{
		if (shape instanceof Circle circle)
		{
			clip.centerXProperty().bind(circle.centerXProperty());
			clip.centerYProperty().bind(circle.centerYProperty());
			clip.radiusProperty().bind(circle.radiusProperty());
		}
	}

	Shape getShape()
	{
		return shape;
	}
}
