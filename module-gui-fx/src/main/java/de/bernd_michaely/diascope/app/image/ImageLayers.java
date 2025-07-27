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
	private final DividerRotationControl dividerRotationControl;
	private final ChangeListener<Number> clippingPointsListener;

	ImageLayers(Viewport viewport, ImageTransforms imageTransforms)
	{
		super(viewport, imageTransforms);
		this.dividerRotationControl = new DividerRotationControl(unmodifiableLayers);
		this.clippingPointsListener = onChange(new ClippingPointsListener(viewport, unmodifiableLayers));
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
		layers.addListener(new ListChangeListenerBuilder<ImageLayer>()
			.onAdd(change ->
			{
				final var list = change.getList();
				for (int i = change.getFrom(); i < change.getTo(); i++)
				{
					final var imageLayer = list.get(i);
					imageLayer.getDivider().angleProperty().addListener(clippingPointsListener);
					viewport.addSplitLayer(i, imageLayer);
				}
				dividerRotationControl.initializeDividerAngles();
			})
			.onRemove(change ->
			{
				for (var imageLayer : change.getRemoved())
				{
					viewport.removeLayer(imageLayer);
					imageLayer.getDivider().angleProperty().removeListener(clippingPointsListener);
					imageLayer.clear();
				}
				dividerRotationControl.initializeDividerAngles();
			}).build());
		// listener for imageTransforms:
		layers.addListener(new ListChangeListenerBuilder<ImageLayer>()
			.onAdd(change ->
			{
				final var list = change.getList();
				for (int i = change.getFrom(); i < change.getTo(); i++)
				{
					final var imageLayer = list.get(i);
					imageLayer.getImageTransforms().bindProperties(imageTransforms);
					if (list.isEmpty())
					{
						imageTransforms.zoomFactorWrapperProperty().unbind();
					}
					else
					{
						imageTransforms.zoomFactorWrapperProperty().bind(
							list.getFirst().getImageTransforms().zoomFactorWrapperProperty());
					}
				}
			})
			.onRemove(change ->
			{
				final var list = change.getList();
				for (var imageLayer : change.getRemoved())
				{
					imageLayer.getImageTransforms().unbindProperties(imageTransforms);
					if (list.isEmpty())
					{
						imageTransforms.zoomFactorWrapperProperty().unbind();
					}
					else
					{
						imageTransforms.zoomFactorWrapperProperty().bind(
							list.getFirst().getImageTransforms().zoomFactorWrapperProperty());
					}
				}
			}).build());
	}

	DividerRotationControl getDividerRotationControl()
	{
		return dividerRotationControl;
	}

	ImageLayer createImageLayer(int index)
	{
		final var imageLayer = ImageLayer.createInstance(
			viewport, this::selectImageLayer, dividerRotationControl);
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
