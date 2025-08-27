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

	ImageLayersSpot(Viewport viewport, ImageTransforms imageTransforms)
	{
		layerSelectionHandler = (imageLayer, _) ->
			layers.selectAll(i -> layers.get(i) == imageLayer ? SELECTION_TOGGLE : SELECTION_UNSET);
		final var baseLayer = ImageLayer.createInstance(BASE, viewport, layerSelectionHandler);
		final var spotLayer = ImageLayer.createInstance(SPOT, viewport, layerSelectionHandler);
		layers.addAll(baseLayer, spotLayer);
		viewport.addSpotBaseLayer(baseLayer);
		viewport.addSpotLayer(spotLayer);
		layers.forEach(l -> l.getImageTransforms().bindProperties(imageTransforms));
	}
}
