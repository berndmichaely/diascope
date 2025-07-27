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

import static de.bernd_michaely.common.desktop.fx.collections.selection.Selectable.Action.*;
import static de.bernd_michaely.diascope.app.image.ImageLayer.Type.*;

/// Class to handle the image layers for spot mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayersSpot extends ImageLayersBase
{
	private final BiConsumer<ImageLayer, Boolean> layerSelectionHandler;

	ImageLayersSpot(Viewport viewport)
	{
		super(viewport);
		layerSelectionHandler = (imageLayer, _) ->
			layers.selectAll(i -> layers.get(i) == imageLayer ? SELECTION_TOGGLE : SELECTION_UNSET);
		layers.addAll(
			ImageLayer.createInstance(BASE, viewport, layerSelectionHandler),
			ImageLayer.createInstance(SPOT, viewport, layerSelectionHandler));
		viewport.addSpotBaseLayer(layers.getFirst());
		viewport.addSpotLayer(layers.getLast());
		layers.forEach(imageLayer ->
		{
			imageLayer.getImageTransforms().bindProperties(imageTransforms);
		});
//		imageTransforms.zoomFactorWrapperProperty().bind(
//			layers.getFirst().getImageTransforms().zoomFactorWrapperProperty());
	}

	ImageLayer getBaseLayer()
	{
		return layers.getFirst();
	}

	ImageLayer getSpotLayer()
	{
		return layers.getLast();
	}
}
