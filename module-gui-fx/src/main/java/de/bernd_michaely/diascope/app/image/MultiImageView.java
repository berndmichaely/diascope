/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ZoomMode.FIT;

/**
 * Facade of a component to display multiple images.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class MultiImageView
{
	private final ImageLayers imageLayers;
	private final SelectionModel selectionModel;
	private final Viewport viewport;
	private final BooleanProperty scrollBarsEnabled;

	public MultiImageView()
	{
		this.viewport = new Viewport();
		this.imageLayers = new ImageLayers(viewport);
		this.selectionModel = new SelectionModel(imageLayers);
		imageLayers.setLayerSelectionHandler(selectionModel::toggleLayerSelection);
		this.scrollBarsEnabled = new SimpleBooleanProperty();
		viewport.getScrollBars().enabledProperty().bind(
			scrollBarsEnabled.and(imageLayers.getImageTransforms().zoomModeProperty().isNotEqualTo(FIT)));
	}

	/**
	 * Returns the main component to be included in surrounding environment.
	 *
	 * @return the main component
	 */
	public Region getRegion()
	{
		return viewport.getPaneViewport();
	}

	public ImageTransforms getImageTransforms()
	{
		return imageLayers.getImageTransforms();
	}

	private ObservableList<ImageLayer> getLayers()
	{
		return imageLayers.getLayers();
	}

	public void addLayer()
	{
		final var singleSelectedLayer = selectionModel.getSingleSelectedLayer();
		if (singleSelectedLayer != null)
		{
			addLayer(getLayers().indexOf(singleSelectedLayer) + 1);
		}
		else
		{
			addLayer(getLayers().size());
		}
	}

	private void addLayer(int index)
	{
		final ImageLayer layer = imageLayers.createImageLayer(index);
		selectionModel.toggleLayerSelection(layer, false);
	}

	public void removeLayer()
	{
		if (isSingleSelected())
		{
			final var imageLayer = selectionModel.singleSelectedLayerProperty().get();
			if (imageLayer != null)
			{
				imageLayers.removeLayer(imageLayer);
			}
			if (getLayers().size() == 1)
			{
				selectionModel.toggleLayerSelection(getLayers().get(0), false);
			}
		}
	}

	public ReadOnlyIntegerProperty numberOfLayersProperty()
	{
		return imageLayers.layersProperty().sizeProperty();
	}

	public ReadOnlyBooleanProperty isSingleSelectedProperty()
	{
		return selectionModel.singleLayerSelectedProperty();
	}

	public boolean isSingleSelected()
	{
		return isSingleSelectedProperty().get();
	}

	/**
	 * Display the given image.
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	public void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		if (isSingleSelected())
		{
			final var imageLayer = selectionModel.getSingleSelectedLayer();
			if (imageLayer != null)
			{
				imageLayer.setImageDescriptor(imageDescriptor);
			}
		}
	}

	public BooleanProperty scrollBarsEnabledProperty()
	{
		return scrollBarsEnabled;
	}

	public ReadOnlyBooleanProperty multiLayerModeProperty()
	{
		return viewport.multiLayerModeProperty();
	}

	public boolean isMultiLayerMode()
	{
		return multiLayerModeProperty().get();
	}

	public BooleanProperty dividersVisibleProperty()
	{
		return viewport.dividersVisibleProperty();
	}
}
