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

import de.bernd_michaely.diascope.app.util.beans.ListChangeListenerBuilder;
import de.bernd_michaely.diascope.app.util.collections.BinaryNode;
import de.bernd_michaely.diascope.app.util.collections.BinaryTree;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.beans.value.ChangeListener;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.common.desktop.fx.collections.selection.Selectable.Action.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.beans.binding.Bindings.createBooleanBinding;

/// Class to handle the image layers for split mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayersDefault extends ImageLayersBase
{
	final LayerSelectionModel layerSelectionModel;
	private final Viewport viewport;
	private final SplitDividerRotationControl splitDividerRotationControl;
	private final GridDividerDragControl gridDividerDragControl;
	private final ChangeListener<Number> clippingPointsListener;

	ImageLayersDefault(Viewport viewport)
	{
		this.viewport = viewport;
		final BinaryTree<GridDivider, ImageLayer> gridTree = new BinaryTree<>();
		final Map<ImageLayer, SplitDivider> splitDividers = new IdentityHashMap<>();
		final Map<ImageLayer, ImageLayerShapeSplit> imageLayerShapes = new IdentityHashMap<>();
		final Function<ImageLayer, @Nullable GridDivider> gridDividerByImageLayer = imageLayer ->
		{
			final var leafNode = gridTree.findLeafNode(imageLayer);
			return (leafNode != null && leafNode.getParentNode() instanceof BinaryNode<?> binaryNode) ?
				(GridDivider) binaryNode.getValue() : null;
		};
		final Function<ImageLayer, SplitDivider> splitDividerByImageLayer = imageLayer ->
		{
			final var splitDivider = splitDividers.get(imageLayer);
			if (splitDivider != null)
			{
				return splitDivider;
			}
			else
			{
				throw new IllegalStateException(getClass().getName() + ": Invalid SplitDivider");
			}
		};
		final Function<ImageLayer, ImageLayerShapeSplit> shapeByImageLayer = imageLayer ->
		{
			final var imageLayerShape = imageLayerShapes.get(imageLayer);
			if (imageLayerShape != null)
			{
				return imageLayerShape;
			}
			else
			{
				throw new IllegalStateException(getClass().getName() + ": Invalid ImageLayerShape");
			}
		};
		this.layerSelectionModel = new LayerSelectionModel(layers, shapeByImageLayer);
		this.splitDividerRotationControl = new SplitDividerRotationControl(unmodifiableLayers, splitDividerByImageLayer);
		this.gridDividerDragControl = new GridDividerDragControl(gridTree,
			viewport.widthProperty(), viewport.heightProperty());
		this.clippingPointsListener = onChange(new ClippingPointsListener(
			this.viewport, unmodifiableLayers, splitDividerByImageLayer, shapeByImageLayer));
		viewport.multiLayerModeProperty().addListener(onChange(enabled ->
		{
			if (enabled)
			{
				viewport.widthProperty().addListener(clippingPointsListener);
				viewport.heightProperty().addListener(clippingPointsListener);
				viewport.getSplitCenter().xProperty().addListener(clippingPointsListener);
				viewport.getSplitCenter().yProperty().addListener(clippingPointsListener);
			}
			else
			{
				viewport.widthProperty().removeListener(clippingPointsListener);
				viewport.heightProperty().removeListener(clippingPointsListener);
				viewport.getSplitCenter().xProperty().removeListener(clippingPointsListener);
				viewport.getSplitCenter().yProperty().removeListener(clippingPointsListener);
			}
		}));
		unmodifiableLayers.addListener(new ListChangeListenerBuilder<ImageLayer>()
			.onAdd(change ->
			{
				final var list = change.getList();
				for (int i = change.getFrom(); i < change.getTo(); i++)
				{
					final var imageLayer = list.get(i);
					if (imageLayer != null)
					{
						// create GridDivider:
						final GridDivider gridDivider;
						if (gridTree.isEmpty())
						{
							gridDivider = null;
							gridTree.append(imageLayer);
						}
						else
						{
							gridDivider = new GridDivider(imageLayer.getViewportBoundsLocal());
							final ImageLayer insertionPoint = list.get(i > 0 ? i - 1 : 0);
							if (!gridTree.insertItemAt(imageLayer, gridDivider, insertionPoint, i > 0))
							{
								throw new IllegalStateException(getClass().getName() +
									" : grid insertion point not found");
							}
						}
						// create SplitDivider:
						final var splitDivider = new SplitDivider(viewport);
						splitDividers.put(imageLayer, splitDivider);
						splitDivider.angleProperty().addListener(clippingPointsListener);
						splitDivider.getMouseDragState().setOnRotate(
							() -> splitDividerRotationControl.accept(splitDivider));
						// create ImageLayerShape:
						final var imageLayerShape = new ImageLayerShapeSplit(viewport.modeProperties());
						imageLayerShape.setLayerSelectionHandler(
							multiSelect -> selectImageLayer(imageLayer, multiSelect));
						imageLayerShapes.put(imageLayer, imageLayerShape);
						imageLayer.clipProperty().bind(imageLayerShape.clipProperty());
						imageLayerShape.bindViewportBounds(imageLayer.getViewportBoundsLocal());
						final var dualProperty = layerSelectionModel.dualSelectedLayerSecondProperty();
						imageLayerShape.dualSpotSelectedProperty().bind(
							createBooleanBinding(() ->
								dualProperty.get().map(l -> l == imageLayer && layerSelectionModel.getSize() > 2).orElse(false),
								dualProperty));
						// add components to viewport:
						viewport.addImageLayer(i, imageLayer, gridDivider, splitDivider, imageLayerShape);
					}
				}
				gridDividerDragControl.initializeDividerPositions();
				splitDividerRotationControl.initializeDividerAngles();
			})
			.onRemove(change ->
			{
				for (var imageLayer : change.getRemoved())
				{
					final var leafNode = gridTree.findLeafNode(imageLayer);
					if (leafNode != null)
					{
						final var gridDivider = gridDividerByImageLayer.apply(imageLayer);
						try (gridDivider)
						{
							gridTree.removeNode(leafNode);
						}
					}
					else
					{
						throw new IllegalStateException(getClass().getName() +
							" : on remove layer : Invalid GridDivider");
					}
					final var splitDivider = splitDividers.remove(imageLayer);
					try (imageLayer; splitDivider)
					{
						viewport.removeLayer(imageLayer);
						if (splitDivider != null)
						{
							splitDivider.angleProperty().removeListener(clippingPointsListener);
						}
						else
						{
							throw new IllegalStateException(getClass().getName() +
								" : on remove layer : no SplitDivider associated with this ImageLayer");
						}
					}
				}
				gridDividerDragControl.initializeDividerPositions();
				splitDividerRotationControl.initializeDividerAngles();
			}).build());
	}

	SplitDividerRotationControl getSplitDividerRotationControl()
	{
		return splitDividerRotationControl;
	}

	GridDividerDragControl getGridDividerDragControl()
	{
		return gridDividerDragControl;
	}

	ImageLayer createImageLayer(int index)
	{
		final var imageLayer = new ImageLayer(viewport);
		layers.add(index < 0 ? layers.size() : index, imageLayer);
		selectImageLayer(imageLayer);
		return imageLayer;
	}

	private void selectImageLayer(ImageLayer imageLayer)
	{
		selectImageLayer(imageLayer, false);
	}

	private void selectImageLayer(ImageLayer imageLayer, boolean multiSelect)
	{
		if (multiSelect)
		{
			layers.select(layers.indexOf(imageLayer), SELECTION_TOGGLE);
		}
		else
		{
			layers.selectAll(i -> layers.get(i) == imageLayer ? SELECTION_TOGGLE : SELECTION_UNSET);
		}
	}

	private boolean isImageLayerSelected(ImageLayer imageLayer)
	{
		final int index = unmodifiableLayers.indexOf(imageLayer);
		return index >= 0 ? layerSelectionModel.isSelected(index) : false;
	}

	/// Removes the selected layers.
	///
	/// @return true, iff anything has been changed
	///
	boolean removeSelectedLayers()
	{
		return _removeLayersIf(this::isImageLayerSelected);
	}

	/// Removes all but one layer.
	/// A selected layer will be retained preferably.
	///
	/// @return true, iff anything has been changed
	///
	boolean removeAllLayersButOne()
	{
		if (layers.size() > 1)
		{
			final var imageLayerRetained = layers.stream()
				.filter(this::isImageLayerSelected).findAny().orElse(layers.getFirst());
			return _removeLayersIf(l -> l != imageLayerRetained);
		}
		else
		{
			return false;
		}
	}

	/// Removes all layers which meet the condition.
	///
	/// @param condition the condition to delete a layer
	/// @return true, iff anything has been changed
	///
	private boolean _removeLayersIf(Predicate<ImageLayer> condition)
	{
		final boolean anyRemoved = layers.removeIf(condition);
		if (anyRemoved)
		{
			if (layers.size() == 1)
			{
				selectImageLayer(layers.getFirst());
			}
		}
		return anyRemoved;
	}
}
