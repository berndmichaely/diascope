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

import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;
import java.lang.System.Logger;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.layout.Region;

import static de.bernd_michaely.diascope.app.image.Bindings.C;
import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static de.bernd_michaely.diascope.app.image.ZoomMode.FIT;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.when;

/// Facade of a component to display multiple images.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class MultiImageView implements AutoCloseable
{
	private static final Logger logger = System.getLogger(MultiImageView.class.getName());
	private final Viewport viewport;
	private final ImageTransformsSwitch<ImageLayer> imageTransformsSwitch;
	private final ImageLayers imageLayers;
	private final ImageLayersSpot spotImageLayers;
	private final BooleanProperty scrollBarsEnabled;
	private final ReadOnlyIntegerWrapper maximumNumberOfLayers;
	private final ReadOnlyBooleanWrapper spotModeDisabled;

	/// Enum to describe the multi image mode.
	///
	public enum Mode
	{
		SINGLE, GRID, SPLIT, SPOT;

		public static Mode getInitialMode()
		{
			return SINGLE;
		}

		public static Mode getDefaultMultiImageMode()
		{
//			return GRID;
			return SPLIT;
		}
	}

	public MultiImageView()
	{
		this.viewport = new Viewport();
		this.imageLayers = new ImageLayers(viewport);
		final var layerSelectionModel = imageLayers.layerSelectionModel;
		viewport.setLayerSelectionModel(layerSelectionModel);
		this.spotImageLayers = new ImageLayersSpot(viewport);
		this.imageTransformsSwitch = new ImageTransformsSwitch<>(
			viewport.modeProperties(), layerSelectionModel.singleSelectedLayerProperty(),
			imageLayers.unmodifiableLayers, spotImageLayers.unmodifiableLayers);
		this.maximumNumberOfLayers = new ReadOnlyIntegerWrapper(
			(int) (C / imageLayers.getSplitDividerRotationControl().getDividerMinGap()));
		viewport.layersMaxWidthProperty().bind(when(viewport.modeProperties().isValueProperty(SPOT))
			.then(spotImageLayers.layersMaxWidth).otherwise(imageLayers.layersMaxWidth));
		viewport.layersMaxHeightProperty().bind(when(viewport.modeProperties().isValueProperty(SPOT))
			.then(spotImageLayers.layersMaxHeight).otherwise(imageLayers.layersMaxHeight));
		viewport.modeProperties().isValueProperty(SINGLE).addListener(onChange(isSingleMode ->
		{
			if (isSingleMode)
			{
				imageLayers.removeAllLayersButOne();
				imageLayers.layers.setSelected(0, true);
			}
		}));
		this.scrollBarsEnabled = new SimpleBooleanProperty();
		viewport.scrollBarsEnabledProperty().bind(
			scrollBarsEnabled.and(imageTransformsSwitch.getFacadeImageTransforms()
				.zoomModeOrDefaultProperty().isNotEqualTo(FIT)));
		this.spotModeDisabled = new ReadOnlyBooleanWrapper();
		spotModeDisabled.bind(not(layerSelectionModel.dualLayerSelected()));
	}

	/// Returns the main component to be included in surrounding environment.
	///
	/// @return the main component
	///
	public Region getRegion()
	{
		return viewport.getRegion();
	}

	public ImageTransforms getImageTransforms()
	{
		return imageTransformsSwitch.getFacadeImageTransforms();
	}

	public LayerSelectionModel getLayerSelectionModel()
	{
		return imageLayers.layerSelectionModel;
	}

	/// Resets the multi image layer controls by setting a default layout,
	/// depending on the mode.
	///
	public void resetControls()
	{
		final var mode = getMode();
		switch (mode)
		{
			case GRID ->
			{
				imageLayers.getGridDividerDragControl().initializeDividerPositions();
			}
			case SPLIT ->
			{
				viewport.getSplitCenter().center();
				imageLayers.getSplitDividerRotationControl().initializeDividerAngles();
			}
			case SPOT ->
			{
				spotImageLayers.reset();
			}
			case SINGLE ->
			{
			}
		}
	}

	/// Adds a new layer.
	public void addLayer()
	{
		final var layers = imageLayers.layers;
		if (layers.size() == 1 && getMode() == getInitialMode())
		{
			setMode(getDefaultMultiImageMode());
		}
		imageLayers.createImageLayer(
			imageLayers.layerSelectionModel.singleSelectedLayerProperty().get()
				.map(layer -> layers.indexOf(layer) + 1).orElse(layers.size()));
	}

	/// Returns a property indicating the number of layers.
	///
	/// @return a property indicating the number of layers
	///
	public ReadOnlyIntegerProperty numLayersProperty()
	{
		return imageLayers.layerSelectionModel.sizeProperty();
	}

	/// Returns the number of layers.
	///
	/// @return the number of layers
	///
	public int getNumLayers()
	{
		return numLayersProperty().get();
	}

	/// Returns a property indicating the maximum possible number of layers.
	/// This number depends on the minimum divider angle gap.
	///
	/// @return a property indicating the maximum possible number of layers
	///
	public ReadOnlyIntegerProperty maximumNumberOfLayersProperty()
	{
		return maximumNumberOfLayers.getReadOnlyProperty();
	}

	/// Returns the maximum possible number of layers.
	/// This number depends on the minimum divider angle gap.
	///
	/// @return the maximum possible number of layers
	///
	public int getMaximumNumberOfLayers()
	{
		return maximumNumberOfLayersProperty().get();
	}

	/// Removes the selected layers.
	///
	/// @return true, iff anything has been changed
	///
	public boolean removeSelectedLayers()
	{
		return imageLayers.removeSelectedLayers();
	}

	/**
	 * Display the given image. If a single layer is selected, the image is
	 * displayed on that layer, otherwise, it is ignored. (If there is only one
	 * layer, it is implicitly selected.)
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	public void setImageDescriptor(Optional<ImageDescriptor> imageDescriptor)
	{
		logger.log(TRACE, () -> getClass().getName() + "::setImageDescriptor »" + imageDescriptor + "«");
		if (!viewport.modeProperties().isValue(SPOT))
		{
			imageLayers.layerSelectionModel.singleSelectedLayerProperty().get().ifPresent(
				imageLayer -> imageLayer.setImageDescriptor(imageDescriptor));
		}
	}

	public BooleanProperty scrollBarsEnabledProperty()
	{
		return scrollBarsEnabled;
	}

	/// Returns true, iff the multi image mode is actually not SINGLE.
	/// Note, that the current number of layers may be 1 nonetheless.
	///
	/// @see Mode#SINGLE
	///
	public ObservableBooleanValue multiLayerModeProperty()
	{
		return viewport.multiLayerModeProperty();
	}

	/// Returns true, iff the multi image mode is actually not SINGLE.
	/// Note, that the current number of layers may be 1 nonetheless.
	///
	/// @see #multiLayerModeProperty()
	///
	public boolean isMultiLayerMode()
	{
		return multiLayerModeProperty().get();
	}

	public BooleanProperty dividersVisibleProperty()
	{
		return viewport.dividersVisibleProperty();
	}

	/// Property to indicate the multi image mode.
	///
	/// @return property to indicate the multi image mode
	///
	public EnumProperties<Mode> modeProperties()
	{
		return viewport.modeProperties();
	}

	/// Returns the multi image mode.
	///
	/// @return the multi image mode
	///
	public Mode getMode()
	{
		return modeProperties().getValueOrDefault();
	}

	/// Sets the multi image mode.
	///
	/// @param mode the given mode
	///
	public void setMode(Mode mode)
	{
		modeProperties().setRawValue(mode);
	}

	public ReadOnlyBooleanProperty spotModeDisabledProperty()
	{
		return spotModeDisabled.getReadOnlyProperty();
	}

	/// {@inheritDoc}
	///
	/// This implementation unbinds all properties.
	///
	@Override
	public void close()
	{
		try (viewport; imageTransformsSwitch; imageLayers)
		{
		}
	}
}
