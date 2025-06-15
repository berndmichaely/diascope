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

import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import java.lang.System.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.isNotNull;
import static javafx.beans.binding.Bindings.when;

/// Class to describe the viewport of a MultiImageView containing all images.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class Viewport
{
	private static final Logger logger = System.getLogger(Viewport.class.getName());
	private final Pane paneImageLayers = new Pane();
	private final Pane paneDividerLines = new Pane();
	private final Pane paneTopLayer = new Pane();
	private final StackPane paneSplitMode = new StackPane(
		paneImageLayers, paneDividerLines, paneTopLayer);
	private final BorderPane paneViewport = new BorderPane(paneSplitMode);
	private final Pane paneSpotMode = new Pane();
	private final ReadOnlyIntegerProperty numLayersProperty;
	private final ObjectProperty<@Nullable Mode> modeProperty;
	private final ReadOnlyObjectWrapper<Mode> modeOrDefaultProperty;
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

	Viewport(ReadOnlyIntegerProperty numLayersProperty)
	{
		this.numLayersProperty = numLayersProperty;
		this.modeProperty = new SimpleObjectProperty<>();
		this.modeOrDefaultProperty = new ReadOnlyObjectWrapper<>();
		_initmodeProperties(modeOrDefaultProperty, modeProperty);
		this.multiLayerMode = new ReadOnlyBooleanWrapper();
		multiLayerMode.bind(numLayersProperty.greaterThanOrEqualTo(2));
		this.dividersVisible = new SimpleBooleanProperty();
		this.dividersEnabled = new ReadOnlyBooleanWrapper();
		dividersEnabled.bind(multiLayerMode.getReadOnlyProperty()
			.and(dividersVisible).and(modeProperty.isEqualTo(SPLIT)));
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
		paneDividerLines.setBackground(Background.EMPTY);
		paneSpotMode.setBackground(Background.EMPTY);
		modeOrDefaultProperty.addListener(onChange(mode ->
		{
			logger.log(TRACE, () -> "Switching to mode " + mode);
			switch (mode)
			{
				case SPLIT ->
				{
					paneViewport.getChildren().remove(paneSpotMode);
					paneViewport.setCenter(paneSplitMode);
				}
				case SPOT ->
				{
					paneViewport.getChildren().remove(paneSplitMode);
					paneViewport.setCenter(paneSpotMode);
				}
				default -> throw new AssertionError(
						"Invalid " + Mode.class.getName() + " : " + mode);
			}
		}));
		paneTopLayer.getChildren().addAll(scrollBars.getControls());
		paneTopLayer.getChildren().add(splitCenter.getShape());
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

	@SuppressWarnings("argument")
	private static void _initmodeProperties(ReadOnlyObjectWrapper<Mode> modeOrDefaultProperty,
		ObjectProperty<@Nullable Mode> modeProperty)
	{
		modeOrDefaultProperty.bind(
			when(isNotNull(modeProperty)).then(modeProperty).otherwise(getDefaultMode()));
	}

	void addLayer(int index, ImageLayer imageLayer)
	{
		final int n = numLayersProperty.get();
		final var childrenLayerEvent = paneTopLayer.getChildren();
		final var divider = imageLayer.getDivider();
		final var lineEvent = divider.getLineEvent();
		final var lineShape = divider.getLineShape();
		final var dividersEnabledProperty = dividersEnabled.getReadOnlyProperty();
		lineEvent.visibleProperty().bind(dividersEnabledProperty);
		lineShape.visibleProperty().bind(dividersEnabledProperty);
		childrenLayerEvent.add(index, imageLayer.getImageLayerShape().getShape());
		childrenLayerEvent.add(n + index, lineEvent);
		paneDividerLines.getChildren().add(index, lineShape);
		paneImageLayers.getChildren().add(index, imageLayer.getRegion());
	}

	void removeLayer(ImageLayer imageLayer)
	{
		final var childrenLayerEvent = paneTopLayer.getChildren();
		final var divider = imageLayer.getDivider();
		final var lineEvent = divider.getLineEvent();
		final var lineShape = divider.getLineShape();
		childrenLayerEvent.remove(lineEvent);
		paneDividerLines.getChildren().remove(lineShape);
		childrenLayerEvent.remove(imageLayer.getImageLayerShape().getShape());
		paneImageLayers.getChildren().remove(imageLayer.getRegion());
	}

	SplitCenter getSplitCenter()
	{
		return splitCenter;
	}

	ObjectProperty<@Nullable Mode> modeProperty()
	{
		return modeProperty;
	}

	ReadOnlyObjectProperty<Mode> modeOrDefaultProperty()
	{
		return modeOrDefaultProperty.getReadOnlyProperty();
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
