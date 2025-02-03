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
import de.bernd_michaely.diascope.app.util.scene.SceneStylesheetUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.T;

/**
 * Class to handle a FullScreen window.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class FullScreen
{
	private final BorderPane borderPane;
	private final SplitPane splitPane;
	private final MultiImageView multiImageView;
	private final BorderPane imageContainer;
	private final ToolBar toolBarImage;
	private final Region listView;
	private final BooleanProperty toolBarFullscreenProperty;
	private @Nullable ChangeListener<Boolean> toolBarFullscreenListener;
	private final BooleanProperty thumbnailsFullscreenProperty;
	private @Nullable ChangeListener<Boolean> thumbnailsFullscreenListener;
	private @Nullable Stage stageFullScreen;
	private @Nullable Region detachedComponent;

	FullScreen(BorderPane borderPane, MultiImageView multiImageView)
	{
		this.borderPane = borderPane;
		this.multiImageView = multiImageView;
		this.toolBarFullscreenProperty = new SimpleBooleanProperty();
		this.thumbnailsFullscreenProperty = new SimpleBooleanProperty();
		this.splitPane = borderPane.getChildrenUnmodifiable().stream()
			.filter(node -> node instanceof SplitPane)
			.map(node -> (SplitPane) node)
			.findFirst().orElseThrow(IllegalStateException::new);
		this.toolBarImage = borderPane.getChildrenUnmodifiable().stream()
			.filter(node -> node instanceof ToolBar)
			.map(node -> (ToolBar) node)
			.findFirst().orElseThrow(IllegalStateException::new);
		final var region = multiImageView.getRegion();
		this.imageContainer = splitPane.getItems().stream()
			.filter(node -> node instanceof BorderPane)
			.map(node -> (BorderPane) node)
			.filter(pane -> pane.getChildrenUnmodifiable().contains(region))
			.findFirst().orElseThrow(IllegalStateException::new);
		this.listView = splitPane.getItems().stream()
			.filter(node -> node instanceof ListView)
			.map(node -> (ListView) node)
			.findFirst().orElseThrow(IllegalStateException::new);
	}

	/**
	 * Detach the fullscreen component.
	 *
	 * @return the detached component
	 */
	private Region detachFullscreenComponent()
	{
		final Region result;
		if (detachedComponent == null)
		{
			if (imageContainer.getChildren().remove(multiImageView.getRegion()))
			{
				result = multiImageView.getRegion();
			}
			else
			{
				throw new IllegalStateException("Error removing paneImage from splitPane");
			}
			result.setBackground(Background.fill(Color.BLACK));
			detachedComponent = result;
		}
		else
		{
			result = detachedComponent;
		}
		return result;
	}

	/**
	 * Re-attach the fullscreen component.
	 *
	 * @return the detached component
	 */
	private void reAttachFullscreenComponent()
	{
		final var root = detachedComponent;
		if (root != null)
		{
			if (root == splitPane)
			{
				borderPane.setCenter(splitPane);
			}
			else if (root == multiImageView.getRegion())
			{
				imageContainer.setCenter(multiImageView.getRegion());
			}
			detachedComponent = null;
		}
	}

	boolean isFullScreen()
	{
		return this.stageFullScreen != null;
	}

	void closeFullScreen()
	{
		final Stage stFullScreen = stageFullScreen;
		if (stFullScreen != null)
		{
			final ChangeListener<Boolean> listenertoolBar = toolBarFullscreenListener;
			if (listenertoolBar != null)
			{
				toolBarFullscreenProperty.set(false);
				toolBarFullscreenProperty.removeListener(listenertoolBar);
			}
			final ChangeListener<Boolean> listenerThumbnails = thumbnailsFullscreenListener;
			if (listenerThumbnails != null)
			{
				thumbnailsFullscreenProperty.set(false);
				thumbnailsFullscreenProperty.removeListener(listenerThumbnails);
			}
			stFullScreen.close();
			reAttachFullscreenComponent();
			multiImageView.scrollBarsEnabledProperty().set(false);
			stageFullScreen = null;
		}
	}

	void setFullScreen()
	{
		if (!isFullScreen())
		{
			final var stage = new Stage();
			stage.setOnCloseRequest(_ -> closeFullScreen());
			stageFullScreen = stage;
			multiImageView.scrollBarsEnabledProperty().set(false);
			final var root = detachFullscreenComponent();
			final var rootPane = new BorderPane(root);
			final var scene = new Scene(rootPane);
			scene.setOnContextMenuRequested(contextMenuEvent ->
			{
				final var menuItemToolbar = new CheckMenuItem("Show/Hide Toolbar");
				menuItemToolbar.selectedProperty().bindBidirectional(toolBarFullscreenProperty);
				final var menuItemThumbnails = new CheckMenuItem("Show/Hide Thumbnails");
				menuItemThumbnails.selectedProperty().bindBidirectional(thumbnailsFullscreenProperty);
				final var menuItemScrollbars = new CheckMenuItem("Show/Hide Scrollbars");
				menuItemScrollbars.selectedProperty().bindBidirectional(multiImageView.scrollBarsEnabledProperty());
				final var menuItemExit = new MenuItem("Exit Fullscreen");
				menuItemExit.setOnAction(_ -> closeFullScreen());
				final var contextMenu = new ContextMenu(
					menuItemToolbar, menuItemThumbnails, menuItemScrollbars, new SeparatorMenuItem(), menuItemExit);
				contextMenu.show(stage, contextMenuEvent.getSceneX(), contextMenuEvent.getSceneY());
			});
			toolBarFullscreenListener = onChange(isFullscreen ->
			{
				if (isFullscreen)
				{
					borderPane.getChildren().remove(toolBarImage);
					rootPane.setTop(toolBarImage);
				}
				else
				{
					rootPane.getChildren().remove(toolBarImage);
					borderPane.setTop(toolBarImage);
				}
			});
			toolBarFullscreenProperty.addListener(toolBarFullscreenListener);
			thumbnailsFullscreenListener = onChange(isFullscreen ->
			{
				if (isFullscreen)
				{
					splitPane.getItems().remove(listView);
					rootPane.setRight(listView);
				}
				else
				{
					splitPane.getItems().remove(listView);
					borderPane.setRight(listView);
				}
			});
			thumbnailsFullscreenProperty.addListener(thumbnailsFullscreenListener);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, event ->
			{
				switch (event.getCode())
				{
					case L ->
					{
						thumbnailsFullscreenProperty.set(!thumbnailsFullscreenProperty.get());
						event.consume();
					}
					case S ->
					{
						multiImageView.scrollBarsEnabledProperty().set(
							!multiImageView.scrollBarsEnabledProperty().get());
						event.consume();
					}
					case T ->
					{
						toolBarFullscreenProperty.set(!toolBarFullscreenProperty.get());
						event.consume();
					}
					case ESCAPE, F11 ->
					{
						closeFullScreen();
						event.consume();
					}
				}
			});
			SceneStylesheetUtil.setStylesheet(scene);
			stage.setScene(scene);
			stage.setFullScreen(true);
			stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
			stage.setFullScreenExitHint("");
			stage.showAndWait();
		}
	}
}
