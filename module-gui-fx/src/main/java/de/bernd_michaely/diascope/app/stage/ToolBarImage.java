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
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.beans.binding.Bindings.not;
import static javafx.geometry.Pos.CENTER_RIGHT;

/// ToolBar for image area.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ToolBarImage
{
	private final ToolBar toolBar;
	private final ToggleGroup toggleGroupZoom = new ToggleGroup();

	ToolBarImage(MultiImageView multiImageView)
	{
		final var layerSelectionModel = multiImageView.getLayerSelectionModel();
		final var numLayers = layerSelectionModel.sizeProperty();
		final var numSelectedLayers = layerSelectionModel.numSelectedProperty();
		// add/remove layers
		final var buttonLayerAdd = new Button();
		final Image iconLayerAdd = Icons.LayerAdd.getIconImage();
		if (iconLayerAdd != null)
		{
			buttonLayerAdd.setGraphic(new ImageView(iconLayerAdd));
		}
		else
		{
			buttonLayerAdd.setText("+");
		}
		buttonLayerAdd.setTooltip(new Tooltip("Add a new layer"));
		buttonLayerAdd.disableProperty().bind(
			numLayers.greaterThanOrEqualTo(multiImageView.maximumNumberOfLayersProperty()).or(
				multiImageView.modeOrDefaultProperty().isEqualTo(SPOT)));
		final var buttonLayerRemove = new Button();
		final Image iconLayerRemove = Icons.LayerRemove.getIconImage();
		if (iconLayerRemove != null)
		{
			buttonLayerRemove.setGraphic(new ImageView(iconLayerRemove));
		}
		else
		{
			buttonLayerRemove.setText("-");
		}
		buttonLayerRemove.setTooltip(new Tooltip("Remove selected layers"));
		buttonLayerRemove.disableProperty().bind(
			not(numLayers.greaterThan(1)
				.and(numSelectedLayers.greaterThan(0)
					.and(numSelectedLayers.lessThan(numLayers))))
				.or(multiImageView.modeOrDefaultProperty().isEqualTo(SPOT)));
		buttonLayerRemove.setOnAction(_ -> multiImageView.removeSelectedLayers());
		// multi layer modes
		final var buttonModeSplit = new ToggleButton();
		buttonModeSplit.setText("Split");
		buttonModeSplit.setTooltip(new Tooltip("""
			Set multi image SPLIT mode.

			To enable SPOT mode, open at least 2 layers,
			single-select the base layer and then
			<Ctrl>-select the spot layer."""));
		buttonModeSplit.setUserData(SPLIT);
		final var buttonModeSpot = new ToggleButton();
		buttonModeSpot.setText("Spot");
		buttonModeSpot.setTooltip(new Tooltip("""
			Set multi image SPOT mode.

			To enable SPOT mode, open at least 2 layers,
			single-select the base layer and then
			<Ctrl>-select the spot layer."""));
		buttonModeSpot.setUserData(SPOT);
		buttonModeSpot.disableProperty().bind(not(multiImageView.spotModeAvailableProperty()));
		final var toggleGroupMode = new ToggleGroup();
		toggleGroupMode.getToggles().addAll(buttonModeSplit, buttonModeSpot);
		toggleGroupMode.selectedToggleProperty().addListener(onChange(selectedToggle ->
		{
			if (selectedToggle != null && selectedToggle.getUserData() instanceof MultiImageView.Mode mode)
			{
				multiImageView.setMode(mode);
			}
			else
			{
				multiImageView.setMode(null);
			}
		}));
		buttonLayerAdd.setOnAction(_ ->
		{
			if (toggleGroupMode.getSelectedToggle() == null && multiImageView.getNumLayers() == 1)
			{
				toggleGroupMode.getToggles().stream()
					.filter(toggle -> toggle.getUserData() == getDefaultMode())
					.findAny().ifPresent(toggleGroupMode::selectToggle);
			}
			multiImageView.addLayer();
		});
		// dividers
		final var buttonShowDividers = new ToggleButton();
		final Image iconShowDividers = Icons.ShowDividers.getIconImage();
		if (iconShowDividers != null)
		{
			buttonShowDividers.setGraphic(new ImageView(iconShowDividers));
		}
		else
		{
			buttonShowDividers.setText("X");
		}
		buttonShowDividers.setTooltip(new Tooltip("Show/Hide dividers"));
		buttonShowDividers.disableProperty().bind(not(multiImageView.multiLayerModeProperty()));
		buttonShowDividers.setSelected(true);
		multiImageView.dividersVisibleProperty().bindBidirectional(buttonShowDividers.selectedProperty());
		// zoom modes
		final var buttonZoomFitWindow = new ToggleButton();
		buttonZoomFitWindow.setUserData(ZoomMode.FIT);
		final Image iconZoomFitWindow = Icons.ZoomFitWindow.getIconImage();
		if (iconZoomFitWindow != null)
		{
			buttonZoomFitWindow.setGraphic(new ImageView(iconZoomFitWindow));
		}
		else
		{
			buttonZoomFitWindow.setText("Fit");
		}
		buttonZoomFitWindow.setTooltip(new Tooltip("Zoom image to fit window"));
		final var buttonZoomFillWindow = new ToggleButton();
		buttonZoomFillWindow.setUserData(ZoomMode.FILL);
		final Image iconZoomFillWindow = Icons.ZoomFillWindow.getIconImage();
		if (iconZoomFillWindow != null)
		{
			buttonZoomFillWindow.setGraphic(new ImageView(iconZoomFillWindow));
		}
		else
		{
			buttonZoomFillWindow.setText("Fill");
		}
		buttonZoomFillWindow.setTooltip(new Tooltip("Zoom image to fill window"));
		final var buttonZoom100 = new ToggleButton();
		buttonZoom100.setUserData(ZoomMode.ORIGINAL);
		final Image iconZoom100 = Icons.Zoom100.getIconImage();
		if (iconZoom100 != null)
		{
			buttonZoom100.setGraphic(new ImageView(iconZoom100));
		}
		else
		{
			buttonZoom100.setText("100%");
		}
		buttonZoom100.setTooltip(new Tooltip("Zoom image to 100%"));
		final var sliderZoom = new Slider(0.01, 4, 1);
		toggleGroupZoom.getToggles().addAll(buttonZoomFitWindow, buttonZoomFillWindow, buttonZoom100);
		toggleGroupZoom.selectedToggleProperty().addListener(onChange(selectedToggle ->
		{
			if (selectedToggle != null && selectedToggle.getUserData() instanceof ZoomMode zoomMode)
			{
				multiImageView.getImageTransforms().zoomModeProperty().set(zoomMode);
				if (zoomMode == ZoomMode.ORIGINAL)
				{
					multiImageView.getImageTransforms().zoomFixedProperty().set(1.0);
				}
			}
			else
			{
				multiImageView.getImageTransforms().zoomModeProperty().set(ZoomMode.FIXED);
				multiImageView.getImageTransforms().zoomFixedProperty().set(sliderZoom.getValue());
			}
		}));
		sliderZoom.setTooltip(new Tooltip("Zoom factor"));
		sliderZoom.valueProperty().addListener(onChange(value ->
		{
			unselectToggles(toggleGroupZoom);
			multiImageView.getImageTransforms().zoomModeProperty().set(ZoomMode.FIXED);
			multiImageView.getImageTransforms().zoomFixedProperty().set(value.doubleValue());
		}));
		sliderZoom.setTooltip(new Tooltip("Set zoom factor"));
		sliderZoom.setOnMouseClicked(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 &&
				multiImageView.getImageTransforms().zoomModeProperty().get() != ZoomMode.FIXED)
			{
				sliderZoom.setValue(multiImageView.getImageTransforms().zoomFactorProperty().get());
			}
		});
		final var labelZoom = new Label("100.0%");
		labelZoom.setTooltip(new Tooltip("Current zoom factor"));
		labelZoom.textProperty().bind(
			multiImageView.getImageTransforms().zoomFactorProperty().multiply(100.0).asString("%.1f%% "));
		final var labelZoomWidth = new Label("8888.8% ");
		labelZoomWidth.setVisible(false);
		final var stackPaneZoom = new StackPane(labelZoomWidth, labelZoom);
//		stackPaneZoom.setBackground(Background.fill(Color.ROSYBROWN));
		stackPaneZoom.setAlignment(CENTER_RIGHT);
		// transformations
		final var sliderRotation = new Slider(0, 360, 0);
		sliderRotation.setTooltip(new Tooltip("Set image rotation"));
		final var labelRotation = new Label("0°");
		labelRotation.setTooltip(new Tooltip("Current image rotation"));
		labelRotation.textProperty().bind(
			multiImageView.getImageTransforms().rotateProperty().asString("%.0f° "));
		final var labelRotationWidth = new Label(" 360° ");
		labelRotationWidth.setVisible(false);
		final var stackPaneRotation = new StackPane(labelRotationWidth, labelRotation);
		stackPaneRotation.setAlignment(CENTER_RIGHT);
//		stackPaneRotation.setBackground(Background.fill(Color.DARKKHAKI));
		final var buttonMirrorX = new ToggleButton();
		final Image iconMirrorX = Icons.MirrorX.getIconImage();
		if (iconMirrorX != null)
		{
			buttonMirrorX.setGraphic(new ImageView(iconMirrorX));
		}
		else
		{
			buttonMirrorX.setText("Mirr. horiz.");
		}
		buttonMirrorX.setTooltip(new Tooltip("Mirror image horizontally"));
		multiImageView.getImageTransforms().mirrorXProperty().bind(buttonMirrorX.selectedProperty());
		final var buttonMirrorY = new ToggleButton();
		final Image iconMirrorY = Icons.MirrorY.getIconImage();
		if (iconMirrorY != null)
		{
			buttonMirrorY.setGraphic(new ImageView(iconMirrorY));
		}
		else
		{
			buttonMirrorY.setText("Mirr. vert.");
		}
		buttonMirrorY.setTooltip(new Tooltip("Mirror image vertically"));
		multiImageView.getImageTransforms().mirrorYProperty().bind(buttonMirrorY.selectedProperty());
		multiImageView.getImageTransforms().rotateProperty().bind(sliderRotation.valueProperty());
		this.toolBar = new ToolBar();
		final boolean isShapeClipSupported = Platform.isSupported(ConditionalFeature.SHAPE_CLIP);
		if (isShapeClipSupported)
		{
			toolBar.getItems().addAll(buttonLayerAdd, buttonLayerRemove);
			toolBar.getItems().add(new Separator());
			toolBar.getItems().addAll(buttonModeSplit, buttonModeSpot);
			toolBar.getItems().add(new Separator());
			toolBar.getItems().add(buttonShowDividers);
		}
		toolBar.getItems().addAll(
			buttonZoomFitWindow, buttonZoomFillWindow, buttonZoom100,
			sliderZoom, stackPaneZoom, sliderRotation, stackPaneRotation,
			buttonMirrorX, buttonMirrorY);
	}

	@SuppressWarnings("argument")
	private void unselectToggles(@UnderInitialization ToolBarImage this,
		ToggleGroup toggleGroup)
	{
		toggleGroup.selectToggle(null);
	}

	void setZoomMode(ZoomMode zoomMode)
	{
		toggleGroupZoom.getToggles().stream()
			.filter(toggle -> toggle.getUserData() == zoomMode)
			.findAny()
			.ifPresent(toggle -> toggle.setSelected(true));
	}

	ToolBar getToolBar()
	{
		return toolBar;
	}
}
