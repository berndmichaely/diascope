/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import static java.lang.Double.max;
import static java.lang.Double.min;

/// Class to describe the viewport of a MultiImageView containing all images.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class Viewport
{
	private final Pane paneImageLayers = new Pane();
	private final Pane paneTopLayer = new Pane();
	private final StackPane paneViewport = new StackPane(paneImageLayers, paneTopLayer);
	private final int numFixedEventControls;
	private final ScrollBars scrollBars;
	private final SplitCenter splitCenter;
	private final CornerAngles cornerAngles;
	private final ReadOnlyBooleanWrapper multiLayerMode;
	private final DoubleProperty focusPointX, focusPointY;
	private final DoubleProperty layersMaxWidth, layersMaxHeight;
	private final ReadOnlyBooleanWrapper scrollBarEnabledHorizontal, scrollBarEnabledVertical;
	private final ReadOnlyDoubleWrapper scrollRangeMaxWidth, scrollRangeMaxHeight;
	private final ReadOnlyDoubleWrapper scrollPosX, scrollPosY;
	private final BooleanProperty dividersVisible;
	private final ReadOnlyBooleanWrapper dividersEnabled;
	private double mouseDragStartX, mouseDragStartY;
	private double mouseScrollStartX, mouseScrollStartY;

	Viewport(ReadOnlyBooleanWrapper multiLayerMode)
	{
		this.multiLayerMode = multiLayerMode;
		this.dividersVisible = new SimpleBooleanProperty();
		this.dividersEnabled = new ReadOnlyBooleanWrapper();
		dividersEnabled.bind(multiLayerMode.getReadOnlyProperty().and(dividersVisible));
		this.focusPointX = new SimpleDoubleProperty(0.5);
		this.focusPointY = new SimpleDoubleProperty(0.5);
		this.layersMaxWidth = new SimpleDoubleProperty();
		this.layersMaxHeight = new SimpleDoubleProperty();
		this.scrollRangeMaxWidth = new ReadOnlyDoubleWrapper();
		this.scrollRangeMaxHeight = new ReadOnlyDoubleWrapper();
		this.scrollPosX = new ReadOnlyDoubleWrapper();
		this.scrollPosY = new ReadOnlyDoubleWrapper();
		this.scrollBars = new ScrollBars(paneViewport.widthProperty(), paneViewport.heightProperty());
		this.splitCenter = new SplitCenter(paneViewport.widthProperty(), paneViewport.heightProperty());
		splitCenter.enabledProperty().bind(dividersEnabled.getReadOnlyProperty());
		paneTopLayer.setBackground(Background.EMPTY);
		paneTopLayer.getChildren().addAll(scrollBars.getControls());
		paneTopLayer.getChildren().add(splitCenter.getShape());
		this.numFixedEventControls = paneTopLayer.getChildren().size();
		paneViewport.setBackground(Background.fill(Color.BLACK));
		paneViewport.setMinSize(0, 0);
		paneViewport.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.scrollBarEnabledHorizontal = new ReadOnlyBooleanWrapper();
		scrollBarEnabledHorizontal.bind(
			layersMaxWidth.greaterThan(paneViewport.widthProperty()));
		scrollBars.horizontalVisibleProperty().bind(
			scrollBars.enabledProperty().and(scrollBarEnabledHorizontal));
		this.scrollBarEnabledVertical = new ReadOnlyBooleanWrapper();
		scrollBarEnabledVertical.bind(
			layersMaxHeight.greaterThan(paneViewport.heightProperty()));
		scrollBars.verticalVisibleProperty().bind(
			scrollBars.enabledProperty().and(scrollBarEnabledVertical));
		scrollRangeMaxWidth.bind(layersMaxWidth.subtract(paneViewport.widthProperty()));
		scrollRangeMaxHeight.bind(layersMaxHeight.subtract(paneViewport.heightProperty()));
		scrollPosX.bind(scrollBars.valueHProperty().multiply(scrollRangeMaxWidth));
		scrollPosY.bind(scrollBars.valueVProperty().multiply(scrollRangeMaxHeight));
		this.cornerAngles = new CornerAngles(
			splitCenter.xProperty(), splitCenter.yProperty(),
			splitCenter.dxProperty(), splitCenter.dyProperty());
		paneViewport.setOnMousePressed(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				mouseDragStartX = event.getX();
				mouseDragStartY = event.getY();
				mouseScrollStartX = scrollBars.valueHProperty().doubleValue();
				mouseScrollStartY = scrollBars.valueVProperty().doubleValue();
			}
		});
		paneViewport.setOnMouseDragged(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				final double px = scrollRangeMaxWidth.doubleValue();
				final double py = scrollRangeMaxHeight.doubleValue();
				final double dx = (event.getX() - mouseDragStartX) / px;
				final double dy = (event.getY() - mouseDragStartY) / py;
				final double x = min(max(0.0, mouseScrollStartX - dx), 1.0);
				final double y = min(max(0.0, mouseScrollStartY - dy), 1.0);
				scrollBars.valueHProperty().setValue(x);
				scrollBars.valueVProperty().setValue(y);
			}
		});
	}

	void addLayer(int index, ImageLayer imageLayer)
	{
		final var childrenLayerEvent = paneTopLayer.getChildren();
		paneImageLayers.getChildren().add(index, imageLayer.getRegion());
		childrenLayerEvent.add(index, imageLayer.getImageLayerShape().getShape());
		final Line lineEvent = imageLayer.getDivider().getLineEvent();
		final Line lineShape = imageLayer.getDivider().getLineShape();
		lineEvent.visibleProperty().bind(dividersEnabled.getReadOnlyProperty());
		lineShape.visibleProperty().bind(dividersEnabled.getReadOnlyProperty());
		paneImageLayers.getChildren().add(lineShape);
		childrenLayerEvent.add(childrenLayerEvent.size() - numFixedEventControls, lineEvent);
	}

	void removeLayer(ImageLayer imageLayer)
	{
		final Divider divider = imageLayer.getDivider();
		paneImageLayers.getChildren().remove(divider.getLineShape());
		paneTopLayer.getChildren().remove(divider.getLineEvent());
		paneTopLayer.getChildren().remove(imageLayer.getImageLayerShape().getShape());
		paneImageLayers.getChildren().remove(imageLayer.getRegion());
	}

	SplitCenter getSplitCenter()
	{
		return splitCenter;
	}

	ReadOnlyBooleanProperty multiLayerModeProperty()
	{
		return multiLayerMode.getReadOnlyProperty();
	}

	boolean isMultiLayerMode()
	{
		return multiLayerModeProperty().get();
	}

	CornerAngles getCornerAngles()
	{
		return cornerAngles;
	}

	ScrollBars getScrollBars()
	{
		return scrollBars;
	}

	DoubleProperty focusPointX()
	{
		return focusPointX;
	}

	DoubleProperty focusPointY()
	{
		return focusPointY;
	}

	ReadOnlyDoubleProperty widthProperty()
	{
		return getPaneViewport().widthProperty();
	}

	ReadOnlyDoubleProperty heightProperty()
	{
		return getPaneViewport().heightProperty();
	}

	/**
	 * The maximum of widths of all layers.
	 *
	 * @return a property holding the maximum of widths of all layers
	 */
	DoubleProperty layersMaxWidthProperty()
	{
		return layersMaxWidth;
	}

	/**
	 * The maximum of heights of all layers.
	 *
	 * @return a property holding the maximum of heights of all layers
	 */
	DoubleProperty layersMaxHeightProperty()
	{
		return layersMaxHeight;
	}

	ReadOnlyBooleanProperty scrollBarEnabledHorizontalProperty()
	{
		return scrollBarEnabledHorizontal.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty scrollBarEnabledVerticalProperty()
	{
		return scrollBarEnabledVertical.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollRangeMaxWidthProperty()
	{
		return scrollRangeMaxWidth.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollRangeMaxHeightProperty()
	{
		return scrollRangeMaxHeight.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollPosXProperty()
	{
		return scrollPosX.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollPosYProperty()
	{
		return scrollPosY.getReadOnlyProperty();
	}

	BooleanProperty dividersVisibleProperty()
	{
		return dividersVisible;
	}

	Pane getPaneViewport()
	{
		return paneViewport;
	}
}
