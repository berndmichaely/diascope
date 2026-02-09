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
import de.bernd_michaely.diascope.app.util.collections.BinaryTree;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import javafx.beans.value.ChangeListener;

import static de.bernd_michaely.common.desktop.fx.collections.selection.Selectable.Action.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;

/// Class to handle the image layers for split mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayers extends ImageLayersBase
{
	private final Viewport viewport;
	private final BinaryTree<GridDivider, ImageLayer> gridTree;
	private final DividerRotationControl dividerRotationControl;
	private final DividerDragControl dividerDragControl;
	private final ChangeListener<Number> clippingPointsListener;

	ImageLayers(Viewport viewport, Map<ImageLayer, SplitDivider> splitDividers)
	{
		this.viewport = viewport;
		final Function<ImageLayer, SplitDivider> dividerByImageLayer = imageLayer ->
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
		this.dividerRotationControl = new DividerRotationControl(unmodifiableLayers, dividerByImageLayer);
		this.gridTree = new BinaryTree<>();
		this.dividerDragControl = new DividerDragControl(gridTree,
			viewport.widthProperty(), viewport.heightProperty());
		this.clippingPointsListener = onChange(new ClippingPointsListener(
			this.viewport, unmodifiableLayers, dividerByImageLayer));
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
		// listener for imageLayer:
		unmodifiableLayers.addListener(new ListChangeListenerBuilder<ImageLayer>()
			.onAdd(change ->
			{
				final var list = change.getList();
				final boolean wasEmpty = change.getAddedSize() == list.size();
				for (int i = change.getFrom(); i < change.getTo(); i++)
				{
					final var imageLayer = list.get(i);
					if (imageLayer != null)
					{
						final var splitDivider = new SplitDivider(viewport);
						splitDividers.put(imageLayer, splitDivider);
						viewport.addImageLayer(i, imageLayer);
						viewport.addSplitDivider(i, splitDivider);
						splitDivider.angleProperty().addListener(clippingPointsListener);
						splitDivider.getMouseDragState().setOnRotate(
							() -> dividerRotationControl.accept(splitDivider));
					}
					if (wasEmpty && i == 0)
					{
						gridTree.append(imageLayer);
					}
					else
					{
						final ImageLayer layerPrev = list.get(i > 0 ? i - 1 : 0);
						if (!gridTree.insertItemAt(imageLayer, new GridDivider(), layerPrev, i > 0))
						{
							throw new IllegalStateException(getClass().getName() +
								" : grid insertion point not found");
						}
					}
				}
				dividerRotationControl.initializeDividerAngles();
				dividerDragControl.initializeDividerPositions();
			})
			.onRemove(change ->
			{
				for (var imageLayer : change.getRemoved())
				{
					if (imageLayer != null)
					{
						viewport.removeLayer(imageLayer);
						try (var splitDivider = splitDividers.get(imageLayer))
						{
							if (splitDivider != null)
							{
								viewport.removeSplitDivider(splitDivider);
								splitDivider.angleProperty().removeListener(clippingPointsListener);
							}
							else
							{
								throw new IllegalStateException(getClass().getName() +
									" : on remove layer : no divider associated with this ImageLayer");
							}
						}
						imageLayer.close();
					}
				}
				change.getRemoved().forEach(splitDividers::remove);
				dividerRotationControl.initializeDividerAngles();
			}).build());
		// listener for imageTransforms:
//		final Consumer<List<? extends ImageLayer>> bindZoomFactor = list ->
//		{
//			if (list.isEmpty())
//			{
//				facadeImageTransforms.zoomFactorWrapperProperty().unbind();
//			}
//			else
//			{
//				facadeImageTransforms.zoomFactorWrapperProperty().bind(
//					list.getFirst().getImageTransforms().zoomFactorProperty());
//			}
//		};
//		unmodifiableLayers.addListener(new ListChangeListenerBuilder<ImageLayer>()
//			.onAdd(change ->
//			{
//				if (viewport.modeProperties().isValue(GRID))
//				{
//				}
//				else
//				{
//					change.getAddedSubList().forEach(l ->
//						l.getImageTransforms().bindProperties(facadeImageTransforms));
//					bindZoomFactor.accept(change.getList());
//				}
//			})
//			.onRemove(change ->
//			{
//				if (viewport.modeProperties().isValue(GRID))
//				{
//				}
//				else
//				{
//					change.getRemoved().forEach(l ->
//						l.getImageTransforms().unbindProperties(facadeImageTransforms));
//					bindZoomFactor.accept(change.getList());
//				}
//			}).build());
	}

	DividerRotationControl getDividerRotationControl()
	{
		return dividerRotationControl;
	}

	DividerDragControl getDividerDragControl()
	{
		return dividerDragControl;
	}

	ImageLayer createImageLayer(int index)
	{
		final var imageLayer = ImageLayer.createGridSplitLayer(viewport, this::selectImageLayer);
		layers.add(index, imageLayer);
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

	/// Removes the selected layers.
	///
	/// @return true, iff anything has been changed
	///
	boolean removeSelectedLayers()
	{
		return _removeLayersIf(ImageLayer::isSelected);
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
				.filter(ImageLayer::isSelected).findAny().orElse(layers.getFirst());
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
