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
import de.bernd_michaely.diascope.app.util.beans.ListChangeListenerBuilder;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static javafx.beans.binding.Bindings.max;

/// Base class to handle image layers.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
abstract sealed class ImageLayersBase permits ImageLayers, ImageLayersSpot
{
	final Viewport viewport;
	final SelectableList<ImageLayer> layers;
	final List<ImageLayer> unmodifiableLayers;
	final LayerSelectionModel layerSelectionModel;
	final ImageTransforms imageTransforms;

	ImageLayersBase(Viewport viewport)
	{
		this.viewport = viewport;
		this.layers = SelectableListFactory.selectableList();
		this.unmodifiableLayers = unmodifiableList(layers);
		this.layerSelectionModel = new LayerSelectionModel(layers);
		this.imageTransforms = new ImageTransforms();
		// viewport.layersMax[Width|Height]Property bindings:
		layers.addListener(new ListChangeListenerBuilder<ImageLayer>()
			.onAdd(change ->
			{
				final var list = change.getList();
				final int n = list.size();
				final int from = change.getFrom();
				final int to = change.getTo();
				final int end = to < n ? to + 1 : to;
				for (int i = from; i < end; i++)
				{
					if (i == 0)
					{
						final ImageLayer first = list.getFirst();
						first.maxToPreviousWidthProperty().bind(first.layerWidthProperty());
						first.maxToPreviousHeightProperty().bind(first.layerHeightProperty());
					}
					else
					{
						final ImageLayer lPrev = list.get(i - 1);
						final ImageLayer lNext = list.get(i);
						lNext.maxToPreviousWidthProperty().bind(
							max(lPrev.maxToPreviousWidthProperty(), lNext.layerWidthProperty()));
						lNext.maxToPreviousHeightProperty().bind(
							max(lPrev.maxToPreviousHeightProperty(), lNext.layerHeightProperty()));
					}
				}
				if (to == n)
				{
					viewport.layersMaxWidthProperty().bind(layers.getLast().maxToPreviousWidthProperty());
					viewport.layersMaxHeightProperty().bind(layers.getLast().maxToPreviousHeightProperty());
				}
			})
			.onRemove(change ->
			{
				final var list = change.getList();
				final boolean isEmpty = list.isEmpty();
				if (change.getTo() < list.size())
				{
					final int from = change.getFrom();
					if (from == 0)
					{
						final ImageLayer first = list.getFirst();
						first.maxToPreviousWidthProperty().bind(first.layerWidthProperty());
						first.maxToPreviousHeightProperty().bind(first.layerHeightProperty());
					}
					else
					{
						final ImageLayer lPrev = list.get(from - 1);
						final ImageLayer lNext = list.get(from);
						lNext.maxToPreviousWidthProperty().bind(
							max(lPrev.maxToPreviousWidthProperty(), lNext.layerWidthProperty()));
						lNext.maxToPreviousHeightProperty().bind(
							max(lPrev.maxToPreviousHeightProperty(), lNext.layerHeightProperty()));
					}
				}
				else if (!isEmpty)
				{
					viewport.layersMaxWidthProperty().bind(layers.getLast().maxToPreviousWidthProperty());
					viewport.layersMaxHeightProperty().bind(layers.getLast().maxToPreviousHeightProperty());
				}
				change.getRemoved().forEach(imageLayer ->
				{
					imageLayer.maxToPreviousWidthProperty().unbind();
					imageLayer.maxToPreviousHeightProperty().unbind();
				});
				if (isEmpty)
				{
					viewport.layersMaxWidthProperty().unbind();
					viewport.layersMaxWidthProperty().set(0.0);
					viewport.layersMaxHeightProperty().unbind();
					viewport.layersMaxHeightProperty().set(0.0);
				}
			}).build());
	}
}
