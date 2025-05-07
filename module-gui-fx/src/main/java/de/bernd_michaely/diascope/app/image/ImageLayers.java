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

import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableList;
import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableListFactory;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;

import static de.bernd_michaely.common.desktop.fx.collections.selection.Selectable.Action.*;
import static de.bernd_michaely.diascope.app.image.Border.*;
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
	private final SelectableList<ImageLayer> layers;
	private final LayerSelectionModel layerSelectionModel;
	private final DividerRotationControl dividerRotationControl;
	private final ImageTransforms imageTransforms;
	private final ChangeListener<Number> listenerClippingPoints;
	private final BiConsumer<ImageLayer, Boolean> layerSelectionHandler;

	ImageLayers(Viewport viewport)
	{
		this.viewport = viewport;
		this.layers = SelectableListFactory.selectableList();
		this.layerSelectionModel = new LayerSelectionModel(layers);
		this.dividerRotationControl = new DividerRotationControl(layers);
		this.imageTransforms = new ImageTransforms();
		this.layerSelectionHandler = (imageLayer, multiSelect) ->
		{
			for (int i = 0; i < layers.size(); i++)
			{
				if (layers.get(i) == imageLayer)
				{
					layers.select(i, SELECTION_TOGGLE);
				}
				else if (!multiSelect)
				{
					layers.select(i, SELECTION_UNSET);
				}
			}
		};
		this.listenerClippingPoints = onChange(() ->
		{
			if (viewport.isClippingEnabled())
			{
				final int n = layers.size();
				for (int i = 0; i < n; i++)
				{
					final var layer = layers.get(i);
					final var divider = layer.getDivider();
					final var corner = divider.getBorder();
					final var layerNext = layers.get(i == n - 1 ? 0 : i + 1);
					final var dividerNext = layerNext.getDivider();
					final var cornerNext = dividerNext.getBorder();
					final int numIntermediateCorners = numberOfCornerPointsBetween(
						corner, divider.getAngle(), cornerNext, dividerNext.getAngle());
					final int numPoints = 2 * (3 + numIntermediateCorners);
					final Double[] points = new Double[numPoints];
					int index = 0;
					points[index++] = viewport.getSplitCenter().xProperty().getValue();
					points[index++] = viewport.getSplitCenter().yProperty().getValue();
					points[index++] = divider.getBorderIntersectionX();
					points[index++] = divider.getBorderIntersectionY();
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
					points[index++] = dividerNext.getBorderIntersectionX();
					points[index++] = dividerNext.getBorderIntersectionY();
					layer.setShapePoints(points);
				}
			}
		});
	}

	SelectableList<ImageLayer> getLayers()
	{
		return layers;
	}

	BiConsumer<ImageLayer, Boolean> getLayerSelectionHandler()
	{
		return layerSelectionHandler;
	}

	LayerSelectionModel getLayerSelectionModel()
	{
		return layerSelectionModel;
	}

	ReadOnlyObjectProperty<Optional<ImageLayer>> singleSelectedLayerProperty()
	{
		return layerSelectionModel.singleSelectedLayerProperty();
	}

	Optional<ImageLayer> getSingleSelectedLayer()
	{
		return singleSelectedLayerProperty().get();
	}

	ImageTransforms getImageTransforms()
	{
		return imageTransforms;
	}

	DividerRotationControl getDividerRotationControl()
	{
		return dividerRotationControl;
	}

	ImageLayer createImageLayer(int index)
	{
		if (layerSelectionHandler == null)
		{
			throw new IllegalStateException(getClass().getName() +
				"::createImageLayer : layerSelectionHandler not initialized!");
		}
		final var imageLayer = ImageLayer.createInstance(viewport, layerSelectionHandler, dividerRotationControl);
		layers.add(index, imageLayer);
		viewport.addLayer(index, imageLayer);
		// imageLayer.getImageLayerShape().unselectedVisibleProperty().bind(viewport.dividersVisibleProperty());
		updateScrollRangeBindings();
		final int numLayers = layers.size();
		if (numLayers == 2)
		{
			viewport.multiLayerModeProperty().set(true);
			viewport.widthProperty().addListener(listenerClippingPoints);
			viewport.heightProperty().addListener(listenerClippingPoints);
			viewport.getSplitCenter().xProperty().addListener(listenerClippingPoints);
			viewport.getSplitCenter().yProperty().addListener(listenerClippingPoints);
		}
		imageLayer.getDivider().angleProperty().addListener(listenerClippingPoints);
		dividerRotationControl.initializeDividerAngles();
		imageLayer.getImageTransforms().bindProperties(getImageTransforms());
		return imageLayer;
	}

	/// Removes the given layers.
	///
	/// @param imageLayers the layers to remove
	/// @return true, iff anything has been changed
	///
	boolean removeLayers(List<ImageLayer> imageLayers)
	{
		boolean anyRemoved = false;
		for (var imageLayer : imageLayers)
		{
			final boolean removed = layers.remove(imageLayer);
			anyRemoved |= removed;
			if (removed)
			{
				imageLayer.getImageLayerShape().unselectedVisibleProperty().unbind();
				imageLayer.getDivider().angleProperty().unbind();
				if (layers.size() == 1)
				{
					viewport.multiLayerModeProperty().set(false);
					viewport.widthProperty().removeListener(listenerClippingPoints);
					viewport.heightProperty().removeListener(listenerClippingPoints);
				}
				viewport.removeLayer(imageLayer);
			}
		}
		if (anyRemoved)
		{
			updateScrollRangeBindings();
			dividerRotationControl.initializeDividerAngles();
		}
		return anyRemoved;
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
}
