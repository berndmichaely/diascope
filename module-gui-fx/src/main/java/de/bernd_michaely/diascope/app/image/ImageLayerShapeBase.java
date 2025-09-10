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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ImageLayer.Type.*;
import static javafx.beans.binding.Bindings.when;

/// Base class to describe an ImageLayer selection shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
abstract sealed class ImageLayerShapeBase permits ImageLayerShapeSplit, ImageLayerShapeSpot
{
	static final Color COLOR_UNSELECTED = Color.ALICEBLUE;
	static final double STROKE_WIDTH_UNSELECTED = 1.0;
	private static final Color COLOR_SELECTED = Color.CORNFLOWERBLUE;
	private static final double STROKE_WIDTH_SELECTED = 4.0;
	private final BooleanProperty selected;
	private final BooleanProperty unselectedVisible;
	private @Nullable Consumer<Boolean> layerSelectionHandler;
	private final @Nullable Consumer<MouseEvent> onMouseDragInit;
	private final @Nullable Consumer<MouseEvent> onMouseDragged;
	private boolean mouseDragged;

	ImageLayerShapeBase(boolean unselectedVisible,
		@Nullable Consumer<MouseEvent> onMouseDragInit,
		@Nullable Consumer<MouseEvent> onMouseDragged)
	{
		this.selected = new SimpleBooleanProperty();
		this.unselectedVisible = new SimpleBooleanProperty(unselectedVisible);
		this.onMouseDragInit = onMouseDragInit;
		this.onMouseDragged = onMouseDragged;
	}

	void _postInit()
	{
		getShape().setFill(Color.TRANSPARENT);
		getShape().setStrokeLineCap(StrokeLineCap.ROUND);
		getShape().setStrokeLineJoin(StrokeLineJoin.ROUND);
		getShape().setStrokeType(StrokeType.INSIDE);
		getShape().strokeProperty().bind(when(selected).then(COLOR_SELECTED).otherwise(
			when(unselectedVisible).then(COLOR_UNSELECTED).otherwise(Color.TRANSPARENT)));
		getShape().strokeWidthProperty().bind(when(selected).then(STROKE_WIDTH_SELECTED).otherwise(
			when(unselectedVisible).then(STROKE_WIDTH_UNSELECTED).otherwise(0.0)));
		getShape().setOnMouseDragged(event ->
		{
			if (!mouseDragged)
			{
				try
				{
					if (onMouseDragInit != null)
					{
						onMouseDragInit.accept(event);
					}
				}
				finally
				{
					mouseDragged = true;
				}
			}
			if (onMouseDragged != null)
			{
				onMouseDragged.accept(event);
			}
		});
		getShape().setOnMouseReleased(event ->
		{
			try
			{
				final boolean isSelectClick = !mouseDragged &&
					event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1 &&
					!event.isShiftDown() && !event.isAltDown();
				if (isSelectClick && layerSelectionHandler != null)
				{
					layerSelectionHandler.accept(event.isControlDown());
				}
			}
			finally
			{
				mouseDragged = false;
				if (getType() == SPOT)
				{
					event.consume();
				}
			}
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

	abstract Type getType();

	abstract Shape getShape();
}
