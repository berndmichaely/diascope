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
import java.lang.System.Logger;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.PREF_KEY_IMAGE_SPLIT_POS;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static de.bernd_michaely.diascope.app.util.beans.property.PersistedProperties.*;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.when;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyCode.L;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.T;

/// Class to handle the conditionally displayed parts of the main content.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class MainContentComponents
{
	private static final Logger logger = System.getLogger(MainContentComponents.class.getName());
	private static final double DEFAULT_SPLIT_WIDTH = 0.9;
	private final MultiImageView multiImageView;
	private final ListView<ImageGroupDescriptor> listView;
	private final BorderPane outerPane;
	private final BorderPane contentPane;
	private final BorderPane toolBarPane;
	private final BorderPane listViewPane;
	private final SplitPane splitPane;
	private final DoubleProperty splitPosProperty;
	private final ToolBarImage toolBarImage;
	private final FullScreen fullScreen;
	private final MainContentProperties properties;
	private final MainContentProperties persistedProperties;
	private final ContextMenu contextMenu;

	MainContentComponents(MultiImageView multiImageView, ListView<ImageGroupDescriptor> listView)
	{
		this.multiImageView = multiImageView;
		this.listView = listView;
		this.splitPane = new SplitPane(multiImageView.getRegion());
		this.listViewPane = new BorderPane(splitPane);
		this.toolBarPane = new BorderPane(listViewPane);
		this.contentPane = new BorderPane(toolBarPane);
		this.outerPane = new BorderPane(contentPane);
//		this.outerPane = new BorderPane(createFullScreenDummy());
		this.splitPosProperty = newPersistedDoubleProperty(
			PREF_KEY_IMAGE_SPLIT_POS, getClass(), DEFAULT_SPLIT_WIDTH);
		this.fullScreen = new FullScreen(() ->
		{
			outerPane.getChildren().remove(contentPane);
			outerPane.setCenter(createFullScreenDummy());
			return contentPane;
		}, () -> outerPane.setCenter(contentPane));
		this.properties = new MainContentProperties();
		this.contextMenu = createContextMenu(properties, fullScreen, multiImageView);
		contentPane.setOnContextMenuRequested(contextMenuEvent ->
		{
			if (contextMenu.isShowing())
			{
				contextMenu.hide();
			}
			else
			{
				contextMenu.show(contentPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
			}
		});
		createEventFilter(contentPane, properties, fullScreen);
		this.toolBarImage = new ToolBarImage(multiImageView);
		properties.toolBarVisibleProperty().addListener(onChange(visible ->
		{
			final var toolBar = toolBarImage.getToolBar();
			if (visible)
			{
				toolBarPane.setTop(toolBar);
			}
			else
			{
				toolBarPane.getChildren().remove(toolBar);
			}
		}));
		properties.thumbnailsVisibleProperty().addListener(onChange(visible ->
		{
			if (visible)
			{
				splitPane.getItems().addLast(listView);
				splitPane.getDividers().getFirst().positionProperty().bindBidirectional(splitPosProperty);
				SplitPane.setResizableWithParent(listView, false);
			}
			else
			{
				splitPane.getDividers().getFirst().positionProperty().unbind();
				splitPane.getItems().remove(listView);
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

	private static ContextMenu createContextMenu(MainContentProperties properties,
		FullScreen fullScreen, MultiImageView multiImageView)
	{
		final var menuItemToolbar = new CheckMenuItem("Show/Hide Toolbar");
		menuItemToolbar.selectedProperty().bindBidirectional(properties.toolBarVisibleProperty());
		final var menuItemThumbnails = new CheckMenuItem("Show/Hide Thumbnails");
		menuItemThumbnails.selectedProperty().bindBidirectional(properties.thumbnailsVisibleProperty());
		final var menuItemDivider = new CheckMenuItem("Show/Hide Dividers");
		menuItemDivider.selectedProperty().bindBidirectional(properties.dividersVisibleProperty());
		menuItemDivider.disableProperty().bind(not(multiImageView.multiLayerModeProperty()));
		final var menuItemScrollbars = new CheckMenuItem("Show/Hide Scrollbars");
		menuItemScrollbars.selectedProperty().bindBidirectional(properties.scrollBarsVisibleProperty());
		final var menuItemFullScreen = new MenuItem();
		menuItemFullScreen.textProperty().bind(
			when(fullScreen.enabledProperty())
				.then("Exit Fullscreen").otherwise("Enter Fullscreen"));
		menuItemFullScreen.setOnAction(_ -> fullScreen.toggle());
		return new ContextMenu(
			menuItemToolbar, menuItemThumbnails,
			new SeparatorMenuItem(),
			menuItemDivider, menuItemScrollbars,
			new SeparatorMenuItem(),
			menuItemFullScreen);
	}

	private static void createEventFilter(Node node,
		MainContentProperties properties, FullScreen fullScreen)
	{
		node.addEventFilter(KeyEvent.KEY_PRESSED, event ->
		{
			final Consumer<BooleanProperty> toggleProperty = prop ->
			{
				prop.set(!prop.get());
				event.consume();
			};
			switch (event.getCode())
			{
				case L -> toggleProperty.accept(properties.thumbnailsVisibleProperty());
				case S -> toggleProperty.accept(properties.scrollBarsVisibleProperty());
				case T -> toggleProperty.accept(properties.toolBarVisibleProperty());
				case F11 -> toggleProperty.accept(fullScreen.enabledProperty());
				case ESCAPE ->
				{
					fullScreen.enabledProperty().set(false);
					event.consume();
				}
			};
		});
	}

	BooleanProperty fullScreenProperty()
	{
		return fullScreen.enabledProperty();
	}

	Region getRegion()
	{
		return outerPane;
	}
}
