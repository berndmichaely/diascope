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
import de.bernd_michaely.diascope.app.util.action.CheckedAction;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ZoomMode.*;
import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.PREF_KEY_SPLIT_POS_IMAGE;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static de.bernd_michaely.diascope.app.util.beans.property.PersistedProperties.*;

/// Class to handle the conditionally displayed parts of the main content.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class MainContentComponents
{
	private static final double DEFAULT_SPLIT_WIDTH = 0.8;
	private final BorderPane paneOuter;
	private final BorderPane paneContent;
	private final BorderPane paneToolBar;
	private final BorderPane paneListView;
	private @Nullable SplitPane splitPane;
	private final DoubleProperty splitPosProperty;
	private final ToolBarImage toolBarImage;
	private final FullScreen fullScreen;
	private final MainContentProperties properties;
	private final MainContentProperties persistedProperties;
	private final ImageControlProperties imageControlProperties;
	private final ImageControlActions actions;

	MainContentComponents(MultiImageView multiImageView, ListView<ImageGroupDescriptor> listView)
	{
		this.paneListView = new BorderPane(multiImageView.getRegion());
		this.paneToolBar = new BorderPane(paneListView);
		this.paneContent = new BorderPane(paneToolBar);
		this.paneOuter = new BorderPane(paneContent);
//		this.outerPane = new BorderPane(createFullScreenDummy());
		this.splitPosProperty = newPersistedDoubleProperty(
			PREF_KEY_SPLIT_POS_IMAGE, getClass(), DEFAULT_SPLIT_WIDTH);
		this.fullScreen = new FullScreen(() ->
		{
			paneOuter.getChildren().remove(paneContent);
//			paneOuter.setCenter(createFullScreenDummy());
			return paneContent;
		}, () -> paneOuter.setCenter(paneContent));
		this.properties = new MainContentProperties();
		this.imageControlProperties = new ImageControlProperties(multiImageView, fullScreen, properties);
		this.actions = new ImageControlActions(multiImageView, imageControlProperties);
		this.toolBarImage = new ToolBarImage(actions, multiImageView);
		final var contextMenu = toolBarImage.getContextMenu();
		paneContent.setOnContextMenuRequested(contextMenuEvent ->
		{
			if (contextMenu.isShowing())
			{
				contextMenu.hide();
			}
			else
			{
				contextMenu.show(paneContent, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
			}
		});
		final EventHandler<KeyEvent> keyEventHandler = event ->
		{
			final Consumer<BooleanProperty> propertyToggle = prop ->
			{
				prop.set(!prop.get());
				event.consume();
			};
			final Consumer<ZoomMode> zoomModeToggle = zoomMode ->
			{
				final var actionZoom = actions.actionZoom;
				actionZoom.setSelectedId(
					actionZoom.getSelectedId() != zoomMode ? zoomMode : actionZoom.getUnselectedId());
				event.consume();
			};
			switch (event.getCode())
			{
				case L -> propertyToggle.accept(actions.actionThumbnails.selectedProperty());
				case S -> propertyToggle.accept(actions.actionScrollbars.selectedProperty());
				case T -> propertyToggle.accept(actions.actionToolbar.selectedProperty());
				case H -> propertyToggle.accept(actions.actionMirrorX.selectedProperty());
				case V -> propertyToggle.accept(actions.actionMirrorY.selectedProperty());
				case F11 -> propertyToggle.accept(actions.actionFullScreen.selectedProperty());
				case ESCAPE ->
				{
					fullScreen.enabledProperty().set(false);
					event.consume();
				}
				case DIGIT1 -> zoomModeToggle.accept(ORIGINAL);
				case DIGIT2 -> zoomModeToggle.accept(FIT);
				case DIGIT3 -> zoomModeToggle.accept(FILL);
			}
		};
		paneOuter.setOnKeyPressed(keyEventHandler);
		paneListView.setOnKeyPressed(keyEventHandler);
		paneToolBar.setOnKeyPressed(keyEventHandler);
		// window and fullscreen modes properties bindings:
		properties.toolBarVisibleProperty().addListener(onChange(visible ->
		{
			final var toolBar = toolBarImage.getToolBar();
			if (visible)
			{
				paneToolBar.setTop(toolBar);
			}
			else
			{
				paneToolBar.getChildren().remove(toolBar);
				if (!properties.thumbnailsVisibleProperty().get())
				{
					multiImageView.getRegion().requestFocus();
				}
			}
		}));
		SplitPane.setResizableWithParent(multiImageView.getRegion(), false);
		SplitPane.setResizableWithParent(listView, false);
		properties.thumbnailsVisibleProperty().addListener(onChange(visible ->
		{
			final var imgView = multiImageView.getRegion();
			if (visible)
			{
				paneListView.getChildren().remove(imgView);
				final var sp = new SplitPane(imgView, listView);
				splitPane = sp;
				paneListView.setCenter(sp);
				sp.getDividers().getFirst().positionProperty().bindBidirectional(splitPosProperty);
			}
			else
			{
				if (splitPane != null)
				{
					final var sp = splitPane;
					sp.getDividers().getFirst().positionProperty().unbindBidirectional(splitPosProperty);
					sp.getItems().clear();
					paneListView.getChildren().remove(sp);
					paneListView.setCenter(imgView);
					splitPane = null;
					if (!properties.toolBarVisibleProperty().get())
					{
						multiImageView.getRegion().requestFocus();
					}
				}
			}
		}));
		properties.dividersVisibleProperty().bindBidirectional(multiImageView.dividersVisibleProperty());
		properties.scrollBarsVisibleProperty().bindBidirectional(multiImageView.scrollBarsEnabledProperty());
		this.persistedProperties = MainContentProperties.newPersistedProperties(fullScreen.enabledProperty());
		this.properties.bindBidirectional(this.persistedProperties);
	}

	private static Node createFullScreenDummy()
	{
		final var label = new Label("FullScreen");
		final double unit = Font.getDefault().getSize();
		label.setFont(Font.font(unit * 5));
		label.setBorder(Border.stroke(Color.FLORALWHITE));
		return label;
	}

	CheckedAction getActionFullScreen()
	{
		return actions.actionFullScreen;
	}

	Region getRegion()
	{
		return paneOuter;
	}
}
