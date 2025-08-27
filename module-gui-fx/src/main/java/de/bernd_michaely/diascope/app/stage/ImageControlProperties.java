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
package de.bernd_michaely.diascope.app.stage;

import de.bernd_michaely.diascope.app.image.MultiImageView;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static javafx.beans.binding.Bindings.not;

/// Properties common to image toolbar, image area context menu and
/// key event hanlders.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageControlProperties
{
	private final FullScreen fullScreen;
	private final MainContentProperties mainContentProperties;
	private final ReadOnlyBooleanWrapper addLayerDisableProperty;
	private final ReadOnlyBooleanWrapper removeLayerDisableProperty;

	ImageControlProperties(MultiImageView multiImageView,
		FullScreen fullScreen, MainContentProperties mainContentProperties)
	{
		this.fullScreen = fullScreen;
		this.mainContentProperties = mainContentProperties;
		final var layerSelectionModel = multiImageView.getLayerSelectionModel();
		final var numLayers = layerSelectionModel.sizeProperty();
		final var numSelectedLayers = layerSelectionModel.numSelectedProperty();
		this.addLayerDisableProperty = new ReadOnlyBooleanWrapper();
		addLayerDisableProperty.bind(
			numLayers.greaterThanOrEqualTo(multiImageView.maximumNumberOfLayersProperty()).or(
				multiImageView.modeProperty().isEqualTo(SPOT)));
		this.removeLayerDisableProperty = new ReadOnlyBooleanWrapper();
		removeLayerDisableProperty.bind(
			not(numLayers.greaterThan(1)
				.and(numSelectedLayers.greaterThan(0)
					.and(numSelectedLayers.lessThan(numLayers))))
				.or(multiImageView.modeProperty().isEqualTo(SPOT)));
	}

	MainContentProperties getMainContentProperties()
	{
		return mainContentProperties;
	}

	ReadOnlyBooleanProperty addLayerDisableProperty()
	{
		return addLayerDisableProperty.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty removeLayerDisableProperty()
	{
		return removeLayerDisableProperty.getReadOnlyProperty();
	}

	FullScreen getFullScreen()
	{
		return fullScreen;
	}
}
