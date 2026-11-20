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
import de.bernd_michaely.diascope.app.util.beans.ListContentConcatenation;
import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Math.clamp;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.size;

/// Class to describe the viewport of a MultiImageView containing all images.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class Viewport implements AutoCloseable
{
	private final Pane pane = new Pane();
	private final ListContentConcatenation<Node> stackNodes;
	private final EnumProperties<Mode> modeProperties;
	private final SplitCenter splitCenter;
	private final CornerAngles cornerAngles;
	private final ObservableBooleanValue multiLayerMode;
	private final BooleanProperty dividersVisible = new SimpleBooleanProperty();
	private final ReadOnlyBooleanWrapper dividersEnabled = new ReadOnlyBooleanWrapper();
	private @Nullable ImageLayer spotBaseLayer, spotLayer;
	private @MonotonicNonNull LayerSelectionModel<ImageLayer> layerSelectionModel;
	private @MonotonicNonNull List<ImageLayer> unmodifiableLayers;
	private final ViewportBoundsGlobal viewportBounds;
	private final ViewportComponents components;
	private final LayerTranslation layerTranslation;

	private class MouseEventHandler
	{
		private double mouseDragStartX, mouseDragStartY;

		private void onMousePressed(MouseEvent event)
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				mouseDragStartX = event.getX();
				mouseDragStartY = event.getY();
				if (modeProperties.isValue(GRID))
				{
					if (unmodifiableLayers != null)
					{
						unmodifiableLayers.forEach(l -> l.getLayerTranslation().saveScrollPos());
					}
				}
				else
				{
					layerTranslation.saveScrollPos();
				}
			}
		}

		private void mouseDragged(MouseEvent event, LayerTranslation layerTranslation)
		{
			final double px = layerTranslation.scrollRangeMaxWidthProperty().doubleValue();
			final double py = layerTranslation.scrollRangeMaxHeightProperty().doubleValue();
			final double dx = (event.getX() - mouseDragStartX) / px;
			final double dy = (event.getY() - mouseDragStartY) / py;
			final double x = clamp(layerTranslation.getMouseScrollStartX() - dx, 0.0, 1.0);
			final double y = clamp(layerTranslation.getMouseScrollStartY() - dy, 0.0, 1.0);
			final var scrollBars = layerTranslation.getScrollBars();
			scrollBars.valueHProperty().setValue(x);
			scrollBars.valueVProperty().setValue(y);
		}

		private void onMouseDragged(MouseEvent event)
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				if (modeProperties.isValue(GRID))
				{
					if (layerSelectionModel != null && unmodifiableLayers != null)
					{
						for (int i : layerSelectionModel.getListDualSelection().getSelectedIndices())
						{
							mouseDragged(event, unmodifiableLayers.get(i).getLayerTranslation());
						}
					}
				}
				else
				{
					mouseDragged(event, layerTranslation);
				}
			}
		}
	}

	private final MouseEventHandler mouseEventHandler = new MouseEventHandler();

	Viewport()
	{
		this.splitCenter = new SplitCenter(pane.widthProperty(), pane.heightProperty());
		splitCenter.enabledProperty().bind(dividersEnabled.getReadOnlyProperty());
		this.stackNodes = new ListContentConcatenation<>(pane.getChildren());
		this.viewportBounds = new ViewportBoundsGlobal(pane.widthProperty(), pane.heightProperty());
		this.layerTranslation = new LayerTranslation(viewportBounds);
		this.components = new ViewportComponents(stackNodes.getObservableLists(),
			layerTranslation.getScrollBars().getScrollBars(), splitCenter.getShapes());
		final BiConsumer<ReadOnlyObjectProperty<Optional<ImageLayer>>, @Nullable ImageLayer> setImage =
			(property, layer) ->
		{
			if (layer != null)
			{
				layer.setOptionalImageDescriptor(property.get().map(ImageLayer::getImageDescriptor));
			}
		};
		final Consumer<@Nullable ImageLayer> clearImage = layer ->
		{
			if (layer != null)
			{
				layer.setImageDescriptor(null);
			}
		};
		final BiConsumer<Mode, Mode> onModeChange = (oldMode, newMode) ->
		{
			if (oldMode == SPOT)
			{
				clearImage.accept(spotBaseLayer);
				clearImage.accept(spotLayer);
			}
			else if (newMode == SPOT)
			{
				if (layerSelectionModel != null && layerSelectionModel.dualLayerSelected().get())
				{
					setImage.accept(layerSelectionModel.dualSelectedLayerFirstProperty(), spotBaseLayer);
					setImage.accept(layerSelectionModel.dualSelectedLayerSecondProperty(), spotLayer);
				}
			}
		};
		this.modeProperties = EnumProperties.createInstance(getInitialMode(),
			List.of(onChange(components::setListsByMode), onChange(onModeChange)));
		this.multiLayerMode = not(modeProperties.isValueProperty(SINGLE));
		dividersEnabled.bind(dividersVisible
			.and(modeProperties.isValueProperty(SPLIT).or(modeProperties.isValueProperty(GRID)))
			.and(size(components.imageLayers).greaterThanOrEqualTo(2)));
		pane.setBackground(Background.fill(Color.BLACK));
		pane.setMinSize(0, 0);
		pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.cornerAngles = new CornerAngles(
			splitCenter.xProperty(), splitCenter.yProperty(),
			splitCenter.dxProperty(), splitCenter.dyProperty());
		pane.setOnMousePressed(mouseEventHandler::onMousePressed);
		pane.setOnMouseDragged(mouseEventHandler::onMouseDragged);
	}

	void setLayerSelectionModel(LayerSelectionModel<ImageLayer> layerSelectionModel)
	{
		this.layerSelectionModel = layerSelectionModel;
	}

	void setUnmodifiableLayers(ObservableList<ImageLayer> unmodifiableLayers)
	{
		this.unmodifiableLayers = unmodifiableLayers;
	}

	void addImageLayer(int index, ImageLayer imageLayer,
		@Nullable GridDivider gridDivider, SplitDivider splitDivider,
		ImageLayerShape imageLayerShape)
	{
		splitDivider.visibleProperty().bind(dividersEnabled.getReadOnlyProperty());
		components.addGridSplitLayer(index, imageLayer, Map.of(
			imageLayer.getNode(), components.imageLayers,
			splitDivider.getLineEvent(), components.splitEventLines,
			splitDivider.getLineShape(), components.splitShapeLines
		));
		if (gridDivider != null)
		{
			gridDivider.visibleProperty().bind(dividersEnabled.getReadOnlyProperty());
			components.addGridSplitLayer(index - 1, imageLayer, Map.of(
				gridDivider.getLineEvent(), components.gridEventLines,
				gridDivider.getLineShape(), components.gridShapeLines
			));
		}
		switch (imageLayerShape)
		{
			case ImageLayerShapeSplit ils ->
				components.addGridSplitLayer(index, imageLayer, Map.of(
					ils.getRectangle(), components.gridShapes,
					ils.getPolygon(), components.splitShapes
				));
			case ImageLayerShapeSpot ils ->
				components.addGridSplitLayer(index, imageLayer, Map.of(
					ils.getShape(), components.spotShapes,
					ils.getSpotCenter().getShape(), components.spotShapes
				));
		}
	}

	void addSpotBaseLayer(ImageLayer imageLayer)
	{
		spotBaseLayer = imageLayer;
		components.spotLayers.addFirst(imageLayer.getNode());
	}

	void addSpotLayer(ImageLayer imageLayer, ImageLayerShapeSpot imageLayerShapeSpot)
	{
		spotLayer = imageLayer;
		components.spotLayers.addLast(imageLayer.getNode());
		components.spotShapes.addAll(
			imageLayerShapeSpot.getShape(),
			imageLayerShapeSpot.getSpotCenter().getShape());
	}

	void removeLayer(ImageLayer imageLayer)
	{
		components.removeGridSplitLayer(imageLayer);
	}

	SplitCenter getSplitCenter()
	{
		return splitCenter;
	}

	EnumProperties<Mode> modeProperties()
	{
		return modeProperties;
	}

	ObservableBooleanValue multiLayerModeProperty()
	{
		return multiLayerMode;
	}

	CornerAngles getCornerAngles()
	{
		return cornerAngles;
	}

	BooleanProperty scrollBarsEnabledProperty()
	{
		return layerTranslation.getScrollBars().enabledProperty();
	}

	DoubleProperty focusPointX()
	{
		return layerTranslation.focusPointXProperty();
	}

	DoubleProperty focusPointY()
	{
		return layerTranslation.focusPointYProperty();
	}

	ReadOnlyDoubleProperty widthProperty()
	{
		return getRegion().widthProperty();
	}

	ReadOnlyDoubleProperty heightProperty()
	{
		return getRegion().heightProperty();
	}

	/// The maximum of widths of all layers.
	///
	/// @return a property holding the maximum of widths of all layers
	///
	DoubleProperty layersMaxWidthProperty()
	{
		return layerTranslation.layersMaxWidthProperty();
	}

	/// The maximum of heights of all layers.
	///
	/// @return a property holding the maximum of heights of all layers
	///
	DoubleProperty layersMaxHeightProperty()
	{
		return layerTranslation.layersMaxHeightProperty();
	}

	/// True, iff the maximum of all layer widths (depending on Mode) is
	/// greater than the global viewport pane.
	///
	ReadOnlyBooleanProperty scrollingEnabledHorizontalProperty()
	{
		return layerTranslation.scrollingEnabledHorizontalProperty();
	}

	/// True, iff the maximum of all layer heights (depending on Mode) is
	/// greater than the global viewport pane.
	///
	ReadOnlyBooleanProperty scrollingEnabledVerticalProperty()
	{
		return layerTranslation.scrollingEnabledVerticalProperty();
	}

	BooleanProperty dividersVisibleProperty()
	{
		return dividersVisible;
	}

	ViewportBoundsGlobal getViewportBounds()
	{
		return viewportBounds;
	}

	Region getRegion()
	{
		return pane;
	}

	@Override
	public void close()
	{
		try (modeProperties; stackNodes)
		{
		}
	}
}
