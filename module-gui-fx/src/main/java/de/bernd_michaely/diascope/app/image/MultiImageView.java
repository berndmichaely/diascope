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

import java.lang.System.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.or;
import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

/**
 * Facade of a component to display multiple images.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class MultiImageView
{
	private static final Double ZERO = 0.0;
	private static final int NULL_INDEX = -1;
	private final ObservableList<ImageLayer> layers;
	private final ImageLayer nullLayer;
	private final ReadOnlyListWrapper<ImageLayer> layersProperty;
	private final ReadOnlyIntegerWrapper selectedSingleIndex;
	private final ReadOnlyBooleanWrapper isSingleSelected;
	private final ReadOnlyObjectWrapper<ImageLayer> selectedLayer;
	private final ReadOnlyDoubleWrapper zoomFactor;
	private final Viewport viewport;
	private final BooleanProperty scrollBarsEnabled;
	private final ChangeListener<Number> listenerClippingPoints;

	public enum ZoomMode
	{
		FIT, FILL, FIXED;

		static ZoomMode getDefault()
		{
			return FIT;
		}
	}

	private static class NullImageLayer extends ImageLayer
	{
		private static final Logger logger = System.getLogger(NullImageLayer.class.getName());

		private NullImageLayer(Viewport viewport)
		{
			super(viewport);
		}

		@Override
		final void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
		{
			logger.log(WARNING, "Trying to set image on %s : »%s«".formatted(getClass().getName(),
				imageDescriptor != null ? imageDescriptor.getPath() : "(null)"));
		}
	}

	public MultiImageView()
	{
		this.layers = FXCollections.observableArrayList();
		this.layersProperty = new ReadOnlyListWrapper<>(layers);
		this.scrollBarsEnabled = new SimpleBooleanProperty();
		this.viewport = new Viewport(not(scrollBarsEnabled), layersProperty.getReadOnlyProperty());
		this.nullLayer = new NullImageLayer(viewport);
		this.selectedSingleIndex = new ReadOnlyIntegerWrapper(NULL_INDEX);
		this.isSingleSelected = new ReadOnlyBooleanWrapper();
		this.selectedLayer = new ReadOnlyObjectWrapper<>(nullLayer);
		this.zoomFactor = new ReadOnlyDoubleWrapper();
		this.listenerClippingPoints = onChange(() ->
		{
			if (viewport.isClippingEnabled())
			{
				final int n = layers.size();
				for (int i = 0; i < n; i++)
				{
					final var layer = layers.get(i);
					final var layerNext = layers.get(i == n - 1 ? 0 : i + 1);
					final var corner = layer.getDividerBorder();
					final var cornerNext = layerNext.getDividerBorder();
					final int numIntermediateCorners = Border.numberOfCornerPointsBetween(corner, cornerNext);
					final int numPoints = 2 * (3 + numIntermediateCorners);
					final Double[] points = new Double[numPoints];
					int index = 0;
					points[index++] = viewport.splitCenterXProperty().getValue();
					points[index++] = viewport.splitCenterYProperty().getValue();
					points[index++] = layer.getDividerBorderX();
					points[index++] = layer.getDividerBorderY();
					var c = corner;
					for (int k = 0; k < numIntermediateCorners; k++, c = c.next())
					{
						points[index++] = switch (c)
						{
							case TOP, RIGHT ->
								viewport.widthProperty().getValue();
							case BOTTOM, LEFT ->
								ZERO;
							default ->
								throw new IllegalStateException("Invalid border: " + c);
						};
						points[index++] = switch (c)
						{
							case RIGHT, BOTTOM ->
								viewport.heightProperty().getValue();
							case LEFT, TOP ->
								ZERO;
							default ->
								throw new IllegalStateException("Invalid border: " + c);
						};
					}
					points[index++] = layerNext.getDividerBorderX();
					points[index++] = layerNext.getDividerBorderY();
					layer.setShapePoints(points);
				}
			}
		});
	}

	/**
	 * Returns the main component to be included in surrounding environment.
	 *
	 * @return the main component
	 */
	public Pane getRegion()
	{
		return viewport.getViewportPane();
	}

	public void addLayer()
	{
		addLayer(layers.size());
	}

	private void addLayer(int index)
	{
		final boolean wasEmpty = layers.isEmpty();
		final ImageLayer layer = createImageLayer(index);
		if (wasEmpty)
		{
			selectedLayer.set(layer);
			toggleLayerSelection(layer, false);
		}
	}

	private ImageLayer createImageLayer(int index)
	{
		final var imageLayer = new ImageLayer(viewport);
		layers.add(index, imageLayer);
		final AnchorPane paneLayer = imageLayer.getPaneLayer();
		setTopAnchor(paneLayer, 0.0);
		setLeftAnchor(paneLayer, 0.0);
		setRightAnchor(paneLayer, 0.0);
		setBottomAnchor(paneLayer, 0.0);
		viewport.getViewportPane().getChildren().add(index, paneLayer);
		updateScrollRangeBindings();
		updateScrollbarVisibilityBindings();
		paneLayer.setOnMouseClicked(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1 &&
				event.isControlDown())
			{
				layers.stream()
					.filter(layer -> layer.getPaneLayer() == paneLayer)
					.findFirst().ifPresent(layer -> toggleLayerSelection(layer, event.isShiftDown()));
			}
		});
		final int numLayers = layers.size();
		if (numLayers == 2)
		{
			viewport.widthProperty().addListener(listenerClippingPoints);
			viewport.heightProperty().addListener(listenerClippingPoints);
		}
		imageLayer.dividerAngleProperty().addListener(listenerClippingPoints);
		final double da = 360.0 / numLayers;
		for (int i = 0; i < numLayers; i++)
		{
			layers.get(i).dividerAngleProperty().set(i * da);
		}
		return imageLayer;
	}

	public void removeLayer()
	{
		removeLayer(selectedSingleIndexProperty().get());
	}

	private void removeLayer(int index)
	{
		final ImageLayer removed = layers.remove(index);
		removed.dividerAngleProperty().unbind();
		if (layers.size() == 1)
		{
			viewport.widthProperty().removeListener(listenerClippingPoints);
			viewport.heightProperty().removeListener(listenerClippingPoints);
		}
		viewport.getViewportPane().getChildren().remove(removed.getPaneLayer());
		updateScrollRangeBindings();
		updateScrollbarVisibilityBindings();
	}

	private void updateScrollbarVisibilityBindings()
	{
		if (layers.isEmpty())
		{
			viewport.getScrollBarH().visibleProperty().unbind();
			viewport.getScrollBarV().visibleProperty().unbind();
		}
		else
		{
			final ImageLayer firstLayer = layers.getFirst();
			ObservableBooleanValue propSbvH = firstLayer.scrollBarEnabledHorizontalProperty();
			ObservableBooleanValue propSbvV = firstLayer.scrollBarEnabledVerticalProperty();
			for (int i = 1; i < layers.size(); i++)
			{
				final ImageLayer layer = layers.get(i);
				propSbvH = or(propSbvH, layer.scrollBarEnabledHorizontalProperty());
				propSbvV = or(propSbvV, layer.scrollBarEnabledVerticalProperty());
			}
			viewport.getScrollBarH().visibleProperty().bind(propSbvH);
			viewport.getScrollBarV().visibleProperty().bind(propSbvV);
		}
	}

	private void updateScrollRangeBindings()
	{
		if (layers.isEmpty())
		{
			viewport.layersMaxWidthProperty().unbind();
			viewport.layersMaxWidthProperty().set(0.0);
			viewport.layersMaxHeightProperty().unbind();
			viewport.layersMaxHeightProperty().set(0.0);
		}
		else
		{
			final ImageLayer first = layers.getFirst();
			first.maxToPreviousWidthProperty().bind(max(first.layerWidthProperty(), 0.0));
			first.maxToPreviousHeightProperty().bind(max(first.layerHeightProperty(), 0.0));
			for (int i = 1; i < layers.size(); i++)
			{
				final ImageLayer lPrev = layers.get(i - 1);
				final ImageLayer lNext = layers.get(i);
				lNext.maxToPreviousWidthProperty().bind(
					max(lPrev.maxToPreviousWidthProperty(), lNext.layerWidthProperty()));
				lNext.maxToPreviousHeightProperty().bind(
					max(lPrev.maxToPreviousHeightProperty(), lNext.layerHeightProperty()));
			}
			viewport.layersMaxWidthProperty().bind(layers.getLast().maxToPreviousWidthProperty());
			viewport.layersMaxHeightProperty().bind(layers.getLast().maxToPreviousHeightProperty());
		}
	}

	private void toggleLayerSelection(ImageLayer layer, boolean multiSelect)
	{
		final int index = layers.indexOf(layer);
		if (index >= 0)
		{
			final BooleanProperty selectedProperty = layer.selectedProperty();
			selectedProperty.set(!selectedProperty.get());
			if (!multiSelect)
			{
				layers.stream()
					.filter(l -> l != layer)
					.forEach(l -> l.selectedProperty().set(false));
			}
			final int numSelected = (int) layers.stream().filter(ImageLayer::isSelected).count();
			isSingleSelected.set(numSelected == 1);
			if (isSingleSelected.get())
			{
				selectedSingleIndex.set(index);
				selectedLayer.set(layer);
				zoomFactor.bind(selectedLayer.get().zoomFactorProperty());
			}
			else
			{
				selectedSingleIndex.set(NULL_INDEX);
				selectedLayer.set(nullLayer);
			}
		}
	}

	public ReadOnlyIntegerProperty numberOfLayersProperty()
	{
		return layersProperty.sizeProperty();
	}

	public ReadOnlyIntegerProperty selectedSingleIndexProperty()
	{
		return selectedSingleIndex.getReadOnlyProperty();
	}

	public ReadOnlyBooleanProperty isSingleSelectedProperty()
	{
		return isSingleSelected.getReadOnlyProperty();
	}

	/**
	 * Display the given image.
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	public void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		final var imageLayer = selectedLayer.get();
		if (imageLayer != nullLayer)
		{
			imageLayer.setImageDescriptor(imageDescriptor);
		}
	}

	public DoubleProperty rotateProperty()
	{
		return viewport.rotateProperty();
	}

	public BooleanProperty scrollBarsEnabledProperty()
	{
		return scrollBarsEnabled;
	}

	public DoubleProperty zoomFixedProperty()
	{
		return viewport.zoomFixedProperty();
	}

	public ObjectProperty<ZoomMode> zoomModeProperty()
	{
		return viewport.zoomModeProperty();
	}

	public ReadOnlyDoubleProperty zoomFactorProperty()
	{
		return zoomFactor.getReadOnlyProperty();
	}

	public BooleanProperty mirrorXProperty()
	{
		return viewport.mirrorXProperty();
	}

	public BooleanProperty mirrorYProperty()
	{
		return viewport.mirrorYProperty();
	}
}
