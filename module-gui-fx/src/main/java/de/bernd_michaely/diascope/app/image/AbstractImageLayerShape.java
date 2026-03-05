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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.checkerframework.checker.nullness.qual.Nullable;

import static javafx.beans.binding.Bindings.when;

/// Base class to describe an ImageLayer selection shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
abstract sealed class AbstractImageLayerShape implements ImageLayerShape
	permits ImageLayerShapeSpotBase, AbstractImageLayerShapeStroke, ImageLayerShapeSpot
{
	private final BooleanProperty selected;
	private final BooleanProperty unselectedVisible;
	private @Nullable Consumer<Boolean> layerSelectionHandler;
	private final @Nullable Consumer<MouseEvent> onMouseDragInit;
	private final @Nullable Consumer<MouseEvent> onMouseDragged;
	private final ReadOnlyBooleanWrapper mouseDragged = new ReadOnlyBooleanWrapper();

	AbstractImageLayerShape(boolean unselectedVisible,
		@Nullable Consumer<MouseEvent> onMouseDragInit,
		@Nullable Consumer<MouseEvent> onMouseDragged)
	{
		this.selected = new SimpleBooleanProperty();
		this.unselectedVisible = new SimpleBooleanProperty(unselectedVisible);
		this.onMouseDragInit = onMouseDragInit;
		this.onMouseDragged = onMouseDragged;
	}

	void initShape(Shape shape)
	{
		shape.setFill(Color.TRANSPARENT);
		shape.setStrokeLineCap(StrokeLineCap.ROUND);
		shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
		shape.setStrokeType(StrokeType.INSIDE);
		shape.strokeProperty().bind(when(selected).then(getStrokeSelectedPaint()).otherwise(
			when(unselectedVisible).then(COLOR_UNSELECTED).otherwise(Color.TRANSPARENT)));
		shape.strokeWidthProperty().bind(when(selected).then(getStrokeWidthSelected()).otherwise(
			when(unselectedVisible).then(STROKE_WIDTH_UNSELECTED).otherwise(0.0)));
		shape.setOnMouseDragged(event ->
		{
			if (!mouseDragged.get())
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
					mouseDragged.set(true);
				}
			}
			if (onMouseDragged != null)
			{
				onMouseDragged.accept(event);
			}
		});
		shape.setOnMouseReleased(event ->
		{
			try
			{
				final boolean isSelectClick = !mouseDragged.get() &&
					event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1 &&
					!event.isShiftDown() && !event.isAltDown();
				if (isSelectClick && layerSelectionHandler != null)
				{
					layerSelectionHandler.accept(event.isControlDown());
				}
			}
			finally
			{
				mouseDragged.set(false);
				if (this instanceof ImageLayerShapeSpot)
				{
					event.consume();
				}
			}
		});
	}

	@Override
	public void setLayerSelectionHandler(Consumer<Boolean> layerSelectionHandler)
	{
		this.layerSelectionHandler = layerSelectionHandler;
	}

	@Override
	public BooleanProperty selectedProperty()
	{
		return selected;
	}

	ReadOnlyBooleanProperty mouseDraggedProperty()
	{
		return mouseDragged.getReadOnlyProperty();
	}

	abstract ObservableObjectValue<Paint> getStrokeSelectedPaint();

	abstract double getStrokeWidthSelected();

	abstract ReadOnlyObjectProperty<@Nullable Shape> clipProperty();
}
