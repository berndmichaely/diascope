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

import java.util.function.BiConsumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static de.bernd_michaely.diascope.app.image.Border.BOTTOM;
import static de.bernd_michaely.diascope.app.image.Border.LEFT;
import static de.bernd_michaely.diascope.app.image.Border.RIGHT;
import static de.bernd_michaely.diascope.app.image.Border.TOP;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.beans.binding.Bindings.max;

/// Class to handle the image layer viewport layer.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageLayers
{
	private static final Double ZERO = 0.0;
	private final Viewport viewport;
	private final ObservableList<ImageLayer> layers;
	private final ReadOnlyListWrapper<ImageLayer> layersProperty;
	private final ImageTransforms imageTransforms;
	private final ChangeListener<Number> listenerClippingPoints;
	private final BooleanProperty dividersVisible;
	private @MonotonicNonNull BiConsumer<ImageLayer, Boolean> layerSelectionHandler;

	ImageLayers(Viewport viewport)
	{
		this.viewport = viewport;
		this.layers = FXCollections.observableArrayList();
		this.layersProperty = new ReadOnlyListWrapper<>(layers);
		this.imageTransforms = new ImageTransforms();
		this.dividersVisible = new SimpleBooleanProperty();
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
						};
						points[index++] = switch (c)
						{
							case RIGHT, BOTTOM ->
								viewport.heightProperty().getValue();
							case LEFT, TOP ->
								ZERO;
						};
					}
					points[index++] = layerNext.getDivider().getBorderIntersectionX();
					points[index++] = layerNext.getDivider().getBorderIntersectionY();
					layer.setShapePoints(points);
				}
			}
		});
	}

	void setLayerSelectionHandler(BiConsumer<ImageLayer, Boolean> layerSelectionHandler)
	{
		this.layerSelectionHandler = layerSelectionHandler;
	}

	ObservableList<ImageLayer> getLayers()
	{
		return layers;
	}

	ReadOnlyListProperty<ImageLayer> layersProperty()
	{
		return layersProperty.getReadOnlyProperty();
	}

	ImageTransforms getImageTransforms()
	{
		return imageTransforms;
	}

	ReadOnlyIntegerProperty numberOfLayersProperty()
	{
		return layersProperty().sizeProperty();
	}

	ImageLayer createImageLayer(int index)
	{
		if (layerSelectionHandler == null)
		{
			throw new IllegalStateException(getClass().getName() +
				"::createImageLayer : layerSelectionHandler not initialized!");
		}
		final var imageLayer = ImageLayer.createInstance(viewport, layerSelectionHandler);
		getLayers().add(index, imageLayer);
		viewport.addLayer(index, imageLayer);
		imageLayer.getImageLayerShape().unselectedVisibleProperty().bind(dividersVisibleProperty());
		updateScrollRangeBindings();
		final int numLayers = getLayers().size();
		if (numLayers == 2)
		{
			viewport.multiLayerModeProperty().set(true);
			viewport.widthProperty().addListener(listenerClippingPoints);
			viewport.heightProperty().addListener(listenerClippingPoints);
			viewport.splitCenterXProperty().addListener(listenerClippingPoints);
			viewport.splitCenterYProperty().addListener(listenerClippingPoints);
		}
		imageLayer.getDivider().angleProperty().addListener(listenerClippingPoints);
		updateDividerDefaultAngles();
		imageLayer.getImageTransforms().bindProperties(getImageTransforms());
		return imageLayer;
	}

	void removeLayer(ImageLayer imageLayer)
	{
		if (getLayers().remove(imageLayer))
		{
			imageLayer.getImageLayerShape().unselectedVisibleProperty().unbind();
			imageLayer.getDivider().angleProperty().unbind();
			if (getLayers().size() == 1)
			{
				viewport.multiLayerModeProperty().set(false);
				viewport.widthProperty().removeListener(listenerClippingPoints);
				viewport.heightProperty().removeListener(listenerClippingPoints);
			}
			viewport.removeLayer(imageLayer);
			updateScrollRangeBindings();
			updateDividerDefaultAngles();
		}
	}

	private void updateDividerDefaultAngles()
	{
		final int numLayers = getLayers().size();
		if (numLayers > 0)
		{
			final double da = 360.0 / numLayers;
			for (int i = 0; i < numLayers; i++)
			{
				getLayers().get(i).getDivider().setAngle(i * da);
			}
		}
	}

	private void updateScrollRangeBindings()
	{
		if (getLayers().isEmpty())
		{
			viewport.layersMaxWidthProperty().unbind();
			viewport.layersMaxWidthProperty().set(0.0);
			viewport.layersMaxHeightProperty().unbind();
			viewport.layersMaxHeightProperty().set(0.0);
		}
		else
		{
			final ImageLayer first = getLayers().getFirst();
			first.maxToPreviousWidthProperty().bind(max(first.layerWidthProperty(), 0.0));
			first.maxToPreviousHeightProperty().bind(max(first.layerHeightProperty(), 0.0));
			for (int i = 1; i < getLayers().size(); i++)
			{
				final ImageLayer lPrev = getLayers().get(i - 1);
				final ImageLayer lNext = getLayers().get(i);
				lNext.maxToPreviousWidthProperty().bind(
					max(lPrev.maxToPreviousWidthProperty(), lNext.layerWidthProperty()));
				lNext.maxToPreviousHeightProperty().bind(
					max(lPrev.maxToPreviousHeightProperty(), lNext.layerHeightProperty()));
			}
			viewport.layersMaxWidthProperty().bind(getLayers().getLast().maxToPreviousWidthProperty());
			viewport.layersMaxHeightProperty().bind(getLayers().getLast().maxToPreviousHeightProperty());
		}
	}

	BooleanProperty dividersVisibleProperty()
	{
		return dividersVisible;
	}
}
