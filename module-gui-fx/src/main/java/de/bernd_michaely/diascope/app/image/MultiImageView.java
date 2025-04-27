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

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.Bindings.C;
import static de.bernd_michaely.diascope.app.image.ZoomMode.FIT;
import static java.util.stream.Collectors.toUnmodifiableList;

/// Facade of a component to display multiple images.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class MultiImageView
{
	private final ImageLayers imageLayers;
	private final Viewport viewport;
	private final BooleanProperty scrollBarsEnabled;

	public MultiImageView()
	{
		this.viewport = new Viewport();
		this.imageLayers = new ImageLayers(viewport);
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

	public void selectAll()
	{
		imageLayers.getLayersProperty().selectAll();
	}

	public void selectNone()
	{
		imageLayers.getLayersProperty().selectNone();
	}

	public void invertSelection()
	{
		imageLayers.getLayersProperty().invertSelection();
	}

	/// Centers the split center in the viewport and
	/// re-initializes the divider angles.
	public void resetDividers()
	{
		viewport.getSplitCenter().center();
		imageLayers.getDividerRotationControl().initializeDividerAngles();
	}

	private ObservableList<ImageLayer> getLayers()
	{
		return imageLayers.getLayers();
	}

	public int getMaximumNumberOfLayers()
	{
		return (int) (C / imageLayers.getDividerRotationControl().getDividerMinGap());
	}

	public void addLayer()
	{
		final Optional<ImageLayer> singleSelectedLayer = imageLayers.getSingleSelectedLayer();
		final var layers = getLayers();
		addLayer(singleSelectedLayer.isPresent() ?
			layers.indexOf(singleSelectedLayer.get()) + 1 : layers.size());
	}

	private void addLayer(int index)
	{
		final var imageLayer = imageLayers.createImageLayer(index);
		imageLayers.getLayerSelectionHandler().accept(imageLayer, false);
	}

	public void removeSelectedLayers()
	{
		final var layers = getLayers();
		final boolean anyRemoved = imageLayers.removeLayers(layers.stream()
			.filter(ImageLayer::isSelected).collect(toUnmodifiableList()));
		if (anyRemoved && layers.size() == 1)
		{
			imageLayers.getLayerSelectionHandler().accept(layers.getFirst(), false);
		}
	}

	public ReadOnlyIntegerProperty numberOfLayersProperty()
	{
		return imageLayers.numberOfLayersProperty();
	}

	public ReadOnlyIntegerProperty numberOfSelectedLayersProperty()
	{
		return imageLayers.getLayersProperty().numSelectedProperty();
	}

	/**
	 * Display the given image.
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	public void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		imageLayers.getSingleSelectedLayer().ifPresent(imageLayer ->
			imageLayer.setImageDescriptor(imageDescriptor));
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
