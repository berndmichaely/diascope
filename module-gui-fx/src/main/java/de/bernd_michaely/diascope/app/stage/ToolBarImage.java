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
import de.bernd_michaely.diascope.app.image.ZoomMode;
import de.bernd_michaely.diascope.app.util.action.Action;
import java.util.List;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

import static de.bernd_michaely.diascope.app.util.action.Action.SEPARATOR;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.geometry.Pos.CENTER_RIGHT;

/// ToolBar for image area.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ToolBarImage
{
	private final ToolBar toolBar;
	private final ContextMenu contextMenu;

	ToolBarImage(ImageControlActions actions, MultiImageView multiImageView)
	{
		final EventHandler<ScrollEvent> sliderScrollEventHandler = event ->
		{
			if (event.getSource() instanceof Slider slider)
			{
				final double deltaY = event.getDeltaY();
				final boolean forward = deltaY < 0.0;
				final boolean backward = deltaY > 0.0;
				if (forward)
				{
					slider.increment();
				}
				else if (backward)
				{
					slider.decrement();
				}
			}
		};
		final var sliderZoom = new Slider(0.05, 4, 1);
		sliderZoom.setBlockIncrement(0.05);
		sliderZoom.setTooltip(new Tooltip("Zoom factor"));
		sliderZoom.valueProperty().addListener(onChange(value ->
		{
			actions.actionZoom.setSelectedId(ZoomMode.FIXED);
			multiImageView.getImageTransforms().zoomFixedProperty().set(value.doubleValue());
		}));
		sliderZoom.setTooltip(new Tooltip("""
      Set zoom factor

      Double-Click to set slider to the actual zoom factor"""));
		sliderZoom.setOnMouseClicked(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 &&
				multiImageView.getImageTransforms().zoomModeProperty().get() != ZoomMode.FIXED)
			{
				sliderZoom.setValue(multiImageView.getImageTransforms().zoomFactorProperty().get());
			}
		});
		sliderZoom.setOnScroll(sliderScrollEventHandler);
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
		final var sliderRotation = new Slider(-180, 180, 0);
		sliderRotation.setBlockIncrement(30);
		sliderRotation.setTooltip(new Tooltip("""
      Set image rotation

      Double-Click slider knob to reset to 0"""));
		sliderRotation.setOnMouseClicked(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
			{
				sliderRotation.setValue(0);
			}
		});
		sliderRotation.setOnScroll(sliderScrollEventHandler);
		final var labelRotation = new Label("0°");
		labelRotation.setTooltip(new Tooltip("Current image rotation"));
		labelRotation.textProperty().bind(
			multiImageView.getImageTransforms().rotateProperty().asString("%.0f° "));
		final var labelRotationWidth = new Label(" -180° ");
		labelRotationWidth.setVisible(false);
		final var stackPaneRotation = new StackPane(labelRotationWidth, labelRotation);
		stackPaneRotation.setAlignment(CENTER_RIGHT);
//		stackPaneRotation.setBackground(Background.fill(Color.DARKKHAKI));
		multiImageView.getImageTransforms().rotateProperty().bind(sliderRotation.valueProperty());
		// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
		// create ToolBar:
		// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
		this.toolBar = new ToolBar();
		toolBar.getItems().addAll(actions.actionThumbnails.createToolBarButtons());
		toolBar.getItems().add(Action.createToolBarSeparator());
		final boolean isShapeClipSupported = Platform.isSupported(ConditionalFeature.SHAPE_CLIP);
		if (isShapeClipSupported)
		{
			toolBar.getItems().addAll(actions.actionLayerAdd.createToolBarButtons());
			toolBar.getItems().addAll(actions.actionLayerRemove.createToolBarButtons());
			toolBar.getItems().addAll(actions.actionShowDividers.createToolBarButtons());
			// TODO : icons
//			toolBar.getItems().add(Action.createToolBarSeparator());
//			toolBar.getItems().addAll(actions.actionSelectAll.createToolBarButtons());
//			toolBar.getItems().addAll(actions.actionSelectNone.createToolBarButtons());
//			toolBar.getItems().addAll(actions.actionSelectToggle.createToolBarButtons());
			toolBar.getItems().add(Action.createToolBarSeparator());
			toolBar.getItems().addAll(actions.actionMode.createToolBarButtons());
			toolBar.getItems().add(Action.createToolBarSeparator());
		}
		toolBar.getItems().addAll(actions.actionZoom.createToolBarButtons());
		toolBar.getItems().addAll(stackPaneZoom, sliderZoom);
		toolBar.getItems().add(Action.createToolBarSeparator());
		toolBar.getItems().addAll(sliderRotation, stackPaneRotation);
		toolBar.getItems().add(new Separator());
		toolBar.getItems().addAll(actions.actionMirrorX.createToolBarButtons());
		toolBar.getItems().addAll(actions.actionMirrorY.createToolBarButtons());
		// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
		// create ContextMenu:
		// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
		this.contextMenu = new ContextMenu(
			List.of(
				actions.actionToolbar,
				actions.actionThumbnails,
				actions.actionScrollbars,
				SEPARATOR,
				actions.actionLayerAdd,
				actions.actionLayerRemove,
				actions.actionShowDividers,
				SEPARATOR,
				actions.actionMode,
				SEPARATOR,
				actions.actionZoom,
				SEPARATOR,
				actions.actionSelectAll,
				actions.actionSelectNone,
				actions.actionSelectToggle,
				SEPARATOR,
				actions.actionMirrorX,
				actions.actionMirrorY,
				SEPARATOR,
				actions.actionResetControls,
				actions.actionFullScreen
			).stream().map(Action::createMenuItems).flatMap(List::stream).toArray(MenuItem[]::new));
	}

	ToolBar getToolBar()
	{
		return toolBar;
	}

	ContextMenu getContextMenu()
	{
		return contextMenu;
	}
}
