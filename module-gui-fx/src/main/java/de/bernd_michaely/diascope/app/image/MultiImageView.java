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
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.Bindings.C;
import static de.bernd_michaely.diascope.app.image.ZoomMode.FIT;
import static java.lang.System.Logger.Level.*;

/// Facade of a component to display multiple images.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class MultiImageView
{
	private static final Logger logger = System.getLogger(MultiImageView.class.getName());
	private final Viewport viewport;
	private final ImageLayers imageLayers;
	private final BooleanProperty scrollBarsEnabled;
	private final ReadOnlyIntegerWrapper numLayers;
	private final ReadOnlyBooleanWrapper spotModeAvailable;

	/// Enum to describe the multi image mode.
	///
	public enum Mode
	{
		SPLIT, SPOT;

		/// Returns the default multi image mode.
		///
		/// @return currently returns the SPLIT mode.
		public static Mode getDefaultMode()
		{
			return SPLIT;
		}
	}

	public MultiImageView()
	{
		this.numLayers = new ReadOnlyIntegerWrapper();
		this.viewport = new Viewport(numLayers.getReadOnlyProperty());
		this.imageLayers = new ImageLayers(viewport);
		numLayers.bind(imageLayers.getLayerSelectionModel().sizeProperty());
		this.scrollBarsEnabled = new SimpleBooleanProperty();
		viewport.getScrollBars().enabledProperty().bind(
			scrollBarsEnabled.and(imageLayers.getImageTransforms().zoomModeProperty().isNotEqualTo(FIT)));
		this.spotModeAvailable = new ReadOnlyBooleanWrapper();
		spotModeAvailable.bind(viewport.multiLayerModeProperty()
			.and(imageLayers.getLayerSelectionModel().singleLayerSelected()));
	}

	/// Returns the main component to be included in surrounding environment.
	///
	/// @return the main component
	///
	public Region getRegion()
	{
		return viewport.getPaneViewport();
	}

	public ImageTransforms getImageTransforms()
	{
		return imageLayers.getImageTransforms();
	}

	public LayerSelectionModel getLayerSelectionModel()
	{
		return imageLayers.getLayerSelectionModel();
	}

	/// Centers the split center in the viewport and
	/// re-initializes the divider angles.
	public void resetDividers()
	{
		viewport.getSplitCenter().center();
		imageLayers.getDividerRotationControl().initializeDividerAngles();
	}

	/// Returns the maximum possible number of layers.
	/// This number depends on the minimum divider angle gap.
	///
	/// @return the maximum possible number of layers
	///
	public int getMaximumNumberOfLayers()
	{
		return (int) (C / imageLayers.getDividerRotationControl().getDividerMinGap());
	}

	public void addLayer()
	{
		final Optional<ImageLayer> singleSelectedLayer = imageLayers.getSingleSelectedLayer();
		final var layers = imageLayers.getLayers();
		imageLayers.createImageLayer(singleSelectedLayer.isPresent() ?
			layers.indexOf(singleSelectedLayer.get()) + 1 : layers.size());
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
	public void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		logger.log(TRACE, () -> getClass().getName() + "::setImageDescriptor »" + imageDescriptor + "«");
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

	/// Property to indicate the multi image mode.
	///
	/// @return property to indicate the multi image mode
	///
	public ObjectProperty<Mode> modeProperty()
	{
		return viewport.modeProperty();
	}

	/// Property to indicate the multi image mode.
	///
	/// @return property to indicate the multi image mode
	///
	public ReadOnlyObjectProperty<Mode> modeOrDefaultProperty()
	{
		return viewport.modeOrDefaultProperty();
	}

	/// Returns the multi image mode or the default mode, if null.
	///
	/// @return the multi image mode
	///
	public Mode getModeOrDefault()
	{
		return modeOrDefaultProperty().get();
	}

	/// Sets the multi image mode.
	///
	/// @param mode the given mode
	///
	public void setMode(Mode mode)
	{
		modeProperty().set(mode);
	}

	public ReadOnlyBooleanProperty spotModeAvailableProperty()
	{
		return spotModeAvailable.getReadOnlyProperty();
	}
}
