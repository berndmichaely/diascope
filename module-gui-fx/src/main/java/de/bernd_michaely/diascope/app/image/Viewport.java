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
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Math.clamp;
import static javafx.beans.binding.Bindings.isNotNull;
import static javafx.beans.binding.Bindings.when;

/// Class to describe the viewport of a MultiImageView containing all images.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class Viewport
{
	private final Pane paneImageLayers = new Pane();
	private final Pane paneDividerLines = new Pane();
	private final Pane paneSpotLayers = new Pane();
	private final Pane paneTopLayer = new Pane();
	private final StackPane stackPane = new StackPane(
		paneImageLayers, paneDividerLines, paneTopLayer);
	private final BorderPane paneViewport = new BorderPane(stackPane);
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
	private final List<Node> cacheChildrenTopLayer = new ArrayList<>();
	private @Nullable ImageLayer spotBaseLayer, spotLayer;
	private @MonotonicNonNull LayerSelectionModel layerSelectionModel;

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
		paneSpotLayers.setBackground(Background.EMPTY);
		modeProperty.addListener(onChange((oldMode, newMode) ->
		{
			if (oldMode == SPOT)
			{
				final var stackItems = stackPane.getChildren();
				if (!stackItems.contains(paneDividerLines))
				{
					stackItems.remove(paneSpotLayers);
					stackItems.add(0, paneImageLayers);
					stackItems.add(1, paneDividerLines);
					final var nodes = paneTopLayer.getChildren();
					if (spotLayer != null)
					{
						nodes.remove(spotLayer.getImageLayerShape().getShape());
					}
					nodes.addAll(0, this.cacheChildrenTopLayer);
					this.cacheChildrenTopLayer.clear();
				}
				if (spotBaseLayer != null)
				{
					spotBaseLayer.setImageDescriptor(null);
				}
				if (spotLayer != null)
				{
					spotLayer.setImageDescriptor(null);
				}
			}
			else if (newMode == SPOT)
			{
				final var stackItems = stackPane.getChildren();
				if (!stackItems.contains(paneSpotLayers))
				{
					stackItems.removeAll(paneImageLayers, paneDividerLines);
					stackItems.add(0, paneSpotLayers);
					final int numLayers = numLayersProperty.get();
					final var nodes = paneTopLayer.getChildren();
					final var subList = nodes.subList(0, 2 * numLayers + 1);
					this.cacheChildrenTopLayer.clear();
					this.cacheChildrenTopLayer.addAll(subList);
					subList.clear();
					if (spotLayer != null)
					{
						nodes.add(0, spotLayer.getImageLayerShape().getShape());
					}
				}
				if (layerSelectionModel != null && layerSelectionModel.dualLayerSelected().get())
				{
					final var l0 = spotBaseLayer;
					if (l0 != null)
					{
						layerSelectionModel.dualSelectedLayerFirstProperty().get().ifPresentOrElse(
							imageLayer -> l0.setImageDescriptor(imageLayer.getImageDescriptor()),
							() -> l0.setImageDescriptor(null));
					}
					final var l1 = spotLayer;
					if (l1 != null)
					{
						layerSelectionModel.dualSelectedLayerSecondProperty().get().ifPresentOrElse(
							imageLayer -> l1.setImageDescriptor(imageLayer.getImageDescriptor()),
							() -> l1.setImageDescriptor(null));
					}
				}
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
				final double x = clamp(mouseScrollStartX - dx, 0.0, 1.0);
				final double y = clamp(mouseScrollStartY - dy, 0.0, 1.0);
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

	void setLayerSelectionModel(LayerSelectionModel layerSelectionModel)
	{
		this.layerSelectionModel = layerSelectionModel;
	}

	void addSplitLayer(int index, ImageLayer imageLayer)
	{
		final int numLayers = numLayersProperty.get();
		final var nodes = modeProperty.get() == SPOT ?
			this.cacheChildrenTopLayer : this.paneTopLayer.getChildren();
		final var divider = imageLayer.getDivider();
		final var lineEvent = divider.getLineEvent();
		final var lineShape = divider.getLineShape();
		final var dividersEnabledProperty = dividersEnabled.getReadOnlyProperty();
		lineEvent.visibleProperty().bind(dividersEnabledProperty);
		lineShape.visibleProperty().bind(dividersEnabledProperty);
		nodes.add(index, imageLayer.getImageLayerShape().getShape());
		nodes.add(index + numLayers, lineEvent);
		paneDividerLines.getChildren().add(index, lineShape);
		paneImageLayers.getChildren().add(index, imageLayer.getRegion());
	}

	void addSpotBaseLayer(ImageLayer imageLayer)
	{
		spotBaseLayer = imageLayer;
		paneSpotLayers.getChildren().add(0, imageLayer.getRegion());
	}

	void addSpotLayer(ImageLayer imageLayer)
	{
		spotLayer = imageLayer;
		paneSpotLayers.getChildren().add(imageLayer.getRegion());
	}

	void removeLayer(ImageLayer imageLayer)
	{
		switch (imageLayer.getImageLayerShape().getType())
		{
			case SPLIT ->
			{
				final var nodes = modeProperty.get() == SPOT ?
					this.cacheChildrenTopLayer : this.paneTopLayer.getChildren();
				final var divider = imageLayer.getDivider();
				final var lineEvent = divider.getLineEvent();
				final var lineShape = divider.getLineShape();
				nodes.remove(lineEvent);
				paneDividerLines.getChildren().remove(lineShape);
				nodes.remove(imageLayer.getImageLayerShape().getShape());
				paneImageLayers.getChildren().remove(imageLayer.getRegion());
			}
			case BASE ->
			{
				paneSpotLayers.getChildren().remove(imageLayer.getRegion());
			}
			case SPOT ->
			{
				paneSpotLayers.getChildren().remove(imageLayer.getRegion());
				final var nodes = modeProperty.get() == SPOT ?
					this.cacheChildrenTopLayer : this.paneTopLayer.getChildren();
				nodes.remove(imageLayer.getImageLayerShape().getShape());
			}
			default -> throw new AssertionError(getClass().getName() +
					"::removeLayer: Invalid image layer type");
		}
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

	/// The maximum of widths of all layers.
	///
	/// @return a property holding the maximum of widths of all layers
	///
	DoubleProperty layersMaxWidthProperty()
	{
		return layersMaxWidth;
	}

	/// The maximum of heights of all layers.
	///
	/// @return a property holding the maximum of heights of all layers
	///
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
