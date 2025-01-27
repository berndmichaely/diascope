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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ImageTransforms.ZoomMode.FIT;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.max;

/**
 * Facade of a component to display multiple images.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class MultiImageView
{
	private static final Double ZERO = 0.0;
	private static final int NULL_INDEX = -1;
	private final ImageTransforms imageTransforms;
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
		this.imageTransforms = new ImageTransforms();
		this.layers = FXCollections.observableArrayList();
		this.layersProperty = new ReadOnlyListWrapper<>(layers);
		this.viewport = new Viewport(layersProperty.getReadOnlyProperty());
		this.scrollBarsEnabled = new SimpleBooleanProperty();
		viewport.getScrollBars().enabledProperty().bind(
			scrollBarsEnabled.and(imageTransforms.zoomModeProperty().isNotEqualTo(FIT)));
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
					final var corner = layer.getDivider().getBorder();
					final var cornerNext = layerNext.getDivider().getBorder();
					final int numIntermediateCorners = Border.numberOfCornerPointsBetween(corner, cornerNext);
					final int numPoints = 2 * (3 + numIntermediateCorners);
					final Double[] points = new Double[numPoints];
					int index = 0;
					points[index++] = viewport.splitCenterXProperty().getValue();
					points[index++] = viewport.splitCenterYProperty().getValue();
					points[index++] = layer.getDivider().getBorderIntersectionX();
					points[index++] = layer.getDivider().getBorderIntersectionY();
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
					points[index++] = layerNext.getDivider().getBorderIntersectionX();
					points[index++] = layerNext.getDivider().getBorderIntersectionY();
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
	public Region getRegion()
	{
		return viewport.getPaneViewport();
	}

	public ImageTransforms getImageTransforms()
	{
		return imageTransforms;
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
		final var imageLayer = ImageLayer.createInstance(viewport, this::toggleLayerSelection);
		layers.add(index, imageLayer);
//		final Pane paneLayer = imageLayer.getPaneLayer();
//		setTopAnchor(paneLayer, 0.0);
//		setLeftAnchor(paneLayer, 0.0);
//		setRightAnchor(paneLayer, 0.0);
//		setBottomAnchor(paneLayer, 0.0);
		viewport.addLayer(index, imageLayer);
		updateScrollRangeBindings();
//		paneLayer.setOnMouseClicked(event ->
//		{
//			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1 &&
//				event.isControlDown())
//			{
//				layers.stream()
//					.filter(layer -> layer.getPaneLayer() == paneLayer)
//					.findFirst().ifPresent(layer -> toggleLayerSelection(layer, event.isShiftDown()));
//			}
//		});
		final int numLayers = layers.size();
		if (numLayers == 2)
		{
			viewport.widthProperty().addListener(listenerClippingPoints);
			viewport.heightProperty().addListener(listenerClippingPoints);
		}
		imageLayer.getDivider().angleProperty().addListener(listenerClippingPoints);
		final double da = 360.0 / numLayers;
		for (int i = 0; i < numLayers; i++)
		{
			layers.get(i).getDivider().setAngle(i * da);
		}
		return imageLayer;
	}

	public void removeLayer()
	{
		removeLayer(selectedSingleIndexProperty().get());
	}

	private void removeLayer(int index)
	{
		if (index >= 0)
		{
			final ImageLayer removed = layers.remove(index);
			removed.getDivider().angleProperty().unbind();
			if (layers.size() == 1)
			{
				viewport.widthProperty().removeListener(listenerClippingPoints);
				viewport.heightProperty().removeListener(listenerClippingPoints);
			}
			viewport.getPaneViewport().getChildren().remove(removed.getRegion());
			updateScrollRangeBindings();
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
			final boolean selected = !layer.isSelected();
			layer.setSelected(selected);
			if (!multiSelect)
			{
				layers.stream().filter(l -> l != layer).forEach(l -> l.setSelected(false));
			}
			final int numSelected = (int) layers.stream().filter(ImageLayer::isSelected).count();
			final boolean singleSelected = numSelected == 1;
			isSingleSelected.set(singleSelected);
			if (singleSelected)
			{
				selectedSingleIndex.set(index);
				selectedLayer.set(layer);
				selectedLayer.get().getImageTransforms().bindProperties(getImageTransforms());
			}
			else
			{
				selectedSingleIndex.set(NULL_INDEX);
				selectedLayer.set(nullLayer);
			}
		}
		System.out.println("toggleLayerSelection: INDEX == " + selectedSingleIndex.getReadOnlyProperty().get());
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

	public boolean isSingleSelected()
	{
		return isSingleSelected.get();
	}

	/**
	 * Display the given image.
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	public void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		if (isSingleSelected())
		{
			selectedLayer.get().setImageDescriptor(imageDescriptor);
		}
	}

	public BooleanProperty scrollBarsEnabledProperty()
	{
		return scrollBarsEnabled;
	}

	public ReadOnlyDoubleProperty zoomFactorProperty()
	{
		return zoomFactor.getReadOnlyProperty();
	}
}
