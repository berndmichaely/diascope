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

import de.bernd_michaely.diascope.app.icons.Icons;
import de.bernd_michaely.diascope.app.image.MultiImageView;
import de.bernd_michaely.diascope.app.image.ZoomMode;
import de.bernd_michaely.diascope.app.util.action.ActionItemDescriptor;
import de.bernd_michaely.diascope.app.util.action.CheckedAction;
import de.bernd_michaely.diascope.app.util.action.ToggleAction;
import de.bernd_michaely.diascope.app.util.action.TriggerAction;
import java.util.Map;
import javafx.beans.binding.BooleanBinding;

import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static de.bernd_michaely.diascope.app.image.ZoomMode.*;
import static javafx.beans.binding.Bindings.not;

/// Class to define Actions for image control.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageControlActions
{
	final TriggerAction actionLayerAdd;
	final TriggerAction actionLayerRemove;
	final TriggerAction actionSelectAll;
	final TriggerAction actionSelectNone;
	final TriggerAction actionSelectToggle;
	final TriggerAction actionResetControls;
	final ToggleAction<MultiImageView.Mode> actionMode;
	final ToggleAction<ZoomMode> actionZoom;
	final CheckedAction actionMirrorX;
	final CheckedAction actionMirrorY;
	final CheckedAction actionShowDividers;
	final CheckedAction actionToolbar;
	final CheckedAction actionThumbnails;
	final CheckedAction actionScrollbars;
	final CheckedAction actionFullScreen;

	ImageControlActions(MultiImageView multiImageView, ImageControlProperties imageControlProperties)
	{
		final var properties = imageControlProperties.getMainContentProperties();
		final var selectionModel = multiImageView.getLayerSelectionModel();
		final var emptyProperty = selectionModel.emptyProperty();
		final BooleanBinding notMultiLayerMode = not(multiImageView.multiLayerModeProperty());
		// LayerAdd
		this.actionLayerAdd = new TriggerAction(multiImageView::addLayer,
			new ActionItemDescriptor(Icons.LayerAdd, "+", "Add layer", "Add a new layer"));
		actionLayerAdd.disableProperty().bind(imageControlProperties.addLayerDisableProperty());
		// LayerRemove
		this.actionLayerRemove = new TriggerAction(multiImageView::removeSelectedLayers,
			new ActionItemDescriptor(Icons.LayerRemove, "-", "Remove selected layers", "Remove selected layers"));
		actionLayerRemove.disableProperty().bind(imageControlProperties.removeLayerDisableProperty());
		// multi layer modes
		final String strTooltipModeSplit = """
			Set multi image SPLIT mode.

			To enable SPOT mode, open at least 2 layers,
			single-select the base layer and then
			<Ctrl>-select the spot layer.""";
		final String strTooltipModeSpot = """
			Set multi image SPOT mode.

			To enable SPOT mode, open at least 2 layers,
			single-select the base layer and then
			<Ctrl>-select the spot layer.""";
		this.actionMode = new ToggleAction<>(SINGLE, Map.of(
			SPLIT, new ActionItemDescriptor(Icons.ModeSplit, "Split", "Split Mode", strTooltipModeSplit),
			SPOT, new ActionItemDescriptor(Icons.ModeSpot, "Spot", "Spot Mode", strTooltipModeSpot)));
		actionMode.disableProperty().bind(emptyProperty);
		actionMode.getDisableProperty(SPOT).bind(not(multiImageView.spotModeAvailableProperty()));
		actionMode.selectedIdProperty().bindBidirectional(multiImageView.modeProperty());
		// ShowDividers
		this.actionShowDividers = new CheckedAction(new ActionItemDescriptor(
			Icons.ShowDividers, "\\/", "Show/Hide dividers", "Show/Hide dividers"));
		actionShowDividers.disableProperty().bind(notMultiLayerMode);
		actionShowDividers.selectedProperty().bindBidirectional(properties.dividersVisibleProperty());
		// ZoomMode
		this.actionZoom = new ToggleAction<>(FIXED, Map.of(
			ORIGINAL, new ActionItemDescriptor(Icons.Zoom100, "100%", "Zoom to 100%", "Zoom image to 100%"),
			FIT, new ActionItemDescriptor(Icons.ZoomFitWindow, "Fit", "Zoom to fit window", "Zoom image to fit window"),
			FILL, new ActionItemDescriptor(Icons.ZoomFillWindow, "Fill", "Zoom to fill window", "Zoom image to fill window")));
		actionZoom.disableProperty().bind(emptyProperty);
		actionZoom.selectedIdProperty().bindBidirectional(multiImageView.getImageTransforms().zoomModeProperty());
		// Mirror
		this.actionMirrorX = new CheckedAction(new ActionItemDescriptor(
			Icons.MirrorX, "<––>", "Mirror horizontally", "Mirror image horizontally"));
		actionMirrorX.disableProperty().bind(emptyProperty);
		actionMirrorX.selectedProperty().bindBidirectional(multiImageView.getImageTransforms().mirrorXProperty());
		this.actionMirrorY = new CheckedAction(new ActionItemDescriptor(
			Icons.MirrorY, "^|↓", "Mirror vertically", "Mirror image vertically"));
		actionMirrorY.disableProperty().bind(emptyProperty);
		actionMirrorY.selectedProperty().bindBidirectional(multiImageView.getImageTransforms().mirrorYProperty());
		// ContextMenu specific
		this.actionToolbar = new CheckedAction(new ActionItemDescriptor("Show/Hide Toolbar"));
		actionToolbar.selectedProperty().bindBidirectional(properties.toolBarVisibleProperty());
		this.actionThumbnails = new CheckedAction(new ActionItemDescriptor(
			Icons.ShowThumbs, "Thumbs", "Show/Hide Thumbnails", "Show/Hide Thumbnails"));
		actionThumbnails.selectedProperty().bindBidirectional(properties.thumbnailsVisibleProperty());
		this.actionScrollbars = new CheckedAction(new ActionItemDescriptor("Show/Hide Scrollbars"));
		actionScrollbars.selectedProperty().bindBidirectional(properties.scrollBarsVisibleProperty());
		// Layer Selection
		this.actionSelectAll = new TriggerAction(selectionModel::selectAll, new ActionItemDescriptor(
			Icons.SelectAll, "+++", "Select all layers", "Select all layers"));
		actionSelectAll.disableProperty().bind(notMultiLayerMode.or(selectionModel.allSelectedProperty()));
		this.actionSelectNone = new TriggerAction(selectionModel::selectNone, new ActionItemDescriptor(
			Icons.SelectNone, "---", "Unselect all layers", "Unselect all layers"));
		actionSelectNone.disableProperty().bind(notMultiLayerMode.or(selectionModel.noneSelectedProperty()));
		this.actionSelectToggle = new TriggerAction(selectionModel::invertSelection, new ActionItemDescriptor(
			Icons.SelectInvert, "+/-", "Toggle layers selection", "Toggle layers selection"));
		actionSelectToggle.disableProperty().bind(notMultiLayerMode);
		// misc
		this.actionResetControls = new TriggerAction(multiImageView::resetControls,
			new ActionItemDescriptor("Reset controls"));
		actionResetControls.disableProperty().bind(notMultiLayerMode);
		this.actionFullScreen = new CheckedAction(new ActionItemDescriptor(
			Icons.ViewFullscreen, "[←→]", "FullScreen mode", "Enter fullScreen mode"));
		actionFullScreen.selectedProperty().bindBidirectional(imageControlProperties.getFullScreen().enabledProperty());
		actionFullScreen.disableProperty().bind(emptyProperty);
	}
}
