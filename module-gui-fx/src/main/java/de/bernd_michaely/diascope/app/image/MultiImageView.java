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

import java.lang.System.Logger;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

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
public class MultiImageView
{
	private static final Logger logger = System.getLogger(MultiImageView.class.getName());
	private final Viewport viewport;
	private final ImageTransforms imageTransforms;
	private final ImageLayers imageLayersSplit;
	private final ImageLayersSpot imageLayersSpot;
	private final BooleanProperty scrollBarsEnabled;
	private final ReadOnlyIntegerWrapper numLayers;
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
			return SPLIT;
		}

		public boolean isSpotMode()
		{
			return this == SPOT;
		}
	}

	public MultiImageView()
	{
		this.numLayers = new ReadOnlyIntegerWrapper();
		this.viewport = new Viewport(numLayers.getReadOnlyProperty());
		this.imageTransforms = new ImageTransforms();
		this.imageLayersSplit = new ImageLayers(viewport, imageTransforms);
		final var layerSelectionModel = imageLayersSplit.layerSelectionModel;
		viewport.setLayerSelectionModel(layerSelectionModel);
		this.imageLayersSpot = new ImageLayersSpot(viewport, imageTransforms);
		imageLayersSpot.layerSelectionModel.setSelected(1, true);
		this.maximumNumberOfLayers = new ReadOnlyIntegerWrapper(
			(int) (C / imageLayersSplit.getDividerRotationControl().getDividerMinGap()));
		viewport.layersMaxWidthProperty().bind(when(viewport.spotProperty())
			.then(imageLayersSpot.layersMaxWidth).otherwise(imageLayersSplit.layersMaxWidth));
		viewport.layersMaxHeightProperty().bind(when(viewport.spotProperty())
			.then(imageLayersSpot.layersMaxHeight).otherwise(imageLayersSplit.layersMaxHeight));
		viewport.modeProperty().addListener(onChange(mode ->
		{
			if (mode == null || mode == SINGLE)
			{
				imageLayersSplit.removeAllLayersButOne();
				imageLayersSplit.layers.setSelected(0, true);
			}
		}));
		numLayers.bind(layerSelectionModel.sizeProperty());
		this.scrollBarsEnabled = new SimpleBooleanProperty();
		viewport.getScrollBars().enabledProperty().bind(
			scrollBarsEnabled.and(imageTransforms.zoomModeProperty().isNotEqualTo(FIT)));
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
		return this.imageTransforms;
	}

	public LayerSelectionModel getLayerSelectionModel()
	{
		return imageLayersSplit.layerSelectionModel;
	}

	/// Resets the multi image layer controls:
	///
	///   * In SPLIT mode, centers the split center in the viewport and re-initializes the divider angles.
	///   * In SPOT mode, centers the spot in the viewport.
	///
	public void resetControls()
	{
		final var mode = getMode();
		switch (mode)
		{
			case SPLIT ->
			{
				viewport.getSplitCenter().center();
				imageLayersSplit.getDividerRotationControl().initializeDividerAngles();
			}
			case SPOT ->
			{
				imageLayersSpot.reset();
			}
			case SINGLE ->
			{
			}
			default -> throw new AssertionError("Invalid mode: " + mode);
		}
	}

	/// Adds a new layer.
	public void addLayer()
	{
		final var layers = imageLayersSplit.layers;
		if (layers.size() == 1 && getMode() == getInitialMode())
		{
			setMode(getDefaultMultiImageMode());
		}
		final Optional<ImageLayer> singleSelectedLayer =
			imageLayersSplit.layerSelectionModel.singleSelectedLayerProperty().get();
		imageLayersSplit.createImageLayer(singleSelectedLayer.isPresent() ?
			layers.indexOf(singleSelectedLayer.get()) + 1 : layers.size());
	}

	/// Returns a property indicating the number of layers.
	///
	/// @return a property indicating the number of layers
	///
	public ReadOnlyIntegerProperty numLayersProperty()
	{
		return numLayers.getReadOnlyProperty();
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
		return imageLayersSplit.removeSelectedLayers();
	}

	/**
	 * Display the given image. If a single layer is selected, the image is
	 * displayed on that layer, otherwise, it is ignored. (If there is only one
	 * layer, it is implicitly selected.)
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	public void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		logger.log(TRACE, () -> getClass().getName() + "::setImageDescriptor »" + imageDescriptor + "«");
		if (!viewport.isSpotMode())
		{
			imageLayersSplit.layerSelectionModel.singleSelectedLayerProperty().get().ifPresent(
				imageLayer -> imageLayer.setImageDescriptor(imageDescriptor));
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

	/// Property to indicate the multi image mode.
	///
	/// @return property to indicate the multi image mode
	///
	public ObjectProperty<Mode> modeProperty()
	{
		return viewport.modeProperty();
	}

	/// Returns the multi image mode.
	///
	/// @return the multi image mode
	///
	public Mode getMode()
	{
		return modeProperty().get();
	}

	/// Sets the multi image mode.
	///
	/// @param mode the given mode
	///
	public void setMode(Mode mode)
	{
		modeProperty().set(mode);
	}

	public ReadOnlyBooleanProperty spotModeDisabledProperty()
	{
		return spotModeDisabled.getReadOnlyProperty();
	}
}
