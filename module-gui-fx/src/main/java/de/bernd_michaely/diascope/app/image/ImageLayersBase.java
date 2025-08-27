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
import java.lang.System.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.ObservableList;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static de.bernd_michaely.diascope.app.util.beans.binding.ListBindings.chainedObservableDoubleValues;
import static java.lang.System.Logger.Level.*;
import static javafx.collections.FXCollections.unmodifiableObservableList;

/// Base class to handle image layers.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
abstract sealed class ImageLayersBase permits ImageLayers, ImageLayersSpot
{
	private static final Logger logger = System.getLogger(ImageLayersBase.class.getName());
	final ReadOnlyDoubleWrapper layersMaxWidth, layersMaxHeight;
	final SelectableList<ImageLayer> layers;
	final ObservableList<ImageLayer> unmodifiableLayers;
	final LayerSelectionModel layerSelectionModel;

	ImageLayersBase()
	{
		this.layersMaxWidth = new ReadOnlyDoubleWrapper();
		this.layersMaxHeight = new ReadOnlyDoubleWrapper();
		this.layers = SelectableListFactory.selectableList();
		this.unmodifiableLayers = unmodifiableObservableList(layers);
		this.layerSelectionModel = new LayerSelectionModel(layers);
		// viewport.layersMax[Width|Height]Property bindings:
		layersMaxWidth.bind(chainedObservableDoubleValues(unmodifiableLayers,
			ImageLayer::layerWidthProperty, Bindings::max, 0.0));
		layersMaxHeight.bind(chainedObservableDoubleValues(unmodifiableLayers,
			ImageLayer::layerHeightProperty, Bindings::max, 0.0));
		layersMaxWidth.addListener(onChange(newValue ->
			logger.log(TRACE, () -> "→ %s → maxWidth = %.1f".formatted(
				getClass().getSimpleName(), newValue.doubleValue()))));
		layersMaxHeight.addListener(onChange(newValue ->
			logger.log(TRACE, () -> "→ %s → maxHeight = %.1f".formatted(
				getClass().getSimpleName(), newValue.doubleValue()))));
	}
}
