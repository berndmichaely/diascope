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
import java.util.function.Consumer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
	private final ContextMenu contextMenu;
	private final EventHandler<KeyEvent> keyEventHandler;

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
		this.toolBarImage = new ToolBarImage(multiImageView);
		this.contextMenu = createContextMenu(properties, fullScreen, multiImageView, toolBarImage);
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
		this.keyEventHandler = event ->
		{
			final Consumer<BooleanProperty> propertyToggle = prop ->
			{
				prop.set(!prop.get());
				event.consume();
			};
			switch (event.getCode())
			{
				case L -> propertyToggle.accept(properties.thumbnailsVisibleProperty());
				case S -> propertyToggle.accept(properties.scrollBarsVisibleProperty());
				case T -> propertyToggle.accept(properties.toolBarVisibleProperty());
				case F11 -> propertyToggle.accept(fullScreen.enabledProperty());
				case ESCAPE ->
				{
					fullScreen.enabledProperty().set(false);
					event.consume();
				}
				case DIGIT1 -> toolBarImage.setZoomMode(ORIGINAL);
				case DIGIT2 -> toolBarImage.setZoomMode(FIT);
				case DIGIT3 -> toolBarImage.setZoomMode(FILL);
			}
		};
		paneListView.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
		paneToolBar.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);

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

	private static ContextMenu createContextMenu(MainContentProperties properties,
		FullScreen fullScreen, MultiImageView multiImageView, ToolBarImage toolBarImage)
	{
		final BooleanBinding notMultiLayerMode = not(multiImageView.multiLayerModeProperty());
		final var selectionModel = multiImageView.getLayerSelectionModel();
		// ---
		final var menuItemToolbar = new CheckMenuItem("Show/Hide Toolbar");
		menuItemToolbar.selectedProperty().bindBidirectional(properties.toolBarVisibleProperty());
		final var menuItemThumbnails = new CheckMenuItem("Show/Hide Thumbnails");
		menuItemThumbnails.selectedProperty().bindBidirectional(properties.thumbnailsVisibleProperty());
		// ---
		final var menuItemZoom100 = new RadioMenuItem("Zoom to 100%");
		menuItemZoom100.selectedProperty().bindBidirectional(toolBarImage.getZoom100SelectedProperty());
		final var menuItemFit = new RadioMenuItem("Zoom to fit window");
		menuItemFit.selectedProperty().bindBidirectional(toolBarImage.getZoomFitWindowSelectedProperty());
		final var menuItemFill = new RadioMenuItem("Zoom to fill window");
		menuItemFill.selectedProperty().bindBidirectional(toolBarImage.getZoomFillWindowSelectedProperty());
		toolBarImage.setZoomMode(multiImageView.getImageTransforms().zoomModeProperty().get());
		// ---
		final var menuItemLayerAdd = new MenuItem("Add Layer");
		menuItemLayerAdd.onActionProperty().bind(toolBarImage.getButtonLayerAdd().onActionProperty());
		menuItemLayerAdd.disableProperty().bind(toolBarImage.getButtonLayerAdd().disableProperty());
		final var menuItemLayerRemove = new MenuItem("Remove Layer");
		menuItemLayerRemove.onActionProperty().bind(toolBarImage.getButtonLayerRemove().onActionProperty());
		menuItemLayerRemove.disableProperty().bind(toolBarImage.getButtonLayerRemove().disableProperty());
		// ---
		final var menuItemModeSplit = new RadioMenuItem("Split Mode");
		menuItemModeSplit.selectedProperty().bindBidirectional(toolBarImage.getModeSplitSelectedProperty());
		final var menuItemModeSpot = new RadioMenuItem("Spot Mode");
		menuItemModeSpot.selectedProperty().bindBidirectional(toolBarImage.getModeSpotSelectedProperty());
		menuItemModeSpot.disableProperty().bind(not(multiImageView.spotModeAvailableProperty()));
		// ---
		final var menuItemDivider = new CheckMenuItem("Show/Hide Dividers");
		menuItemDivider.selectedProperty().bindBidirectional(properties.dividersVisibleProperty());
		menuItemDivider.disableProperty().bind(notMultiLayerMode);
		final var menuItemScrollbars = new CheckMenuItem("Show/Hide Scrollbars");
		menuItemScrollbars.selectedProperty().bindBidirectional(properties.scrollBarsVisibleProperty());
		// ---
		final var menuItemSelectAll = new MenuItem("Select all layers");
		menuItemSelectAll.disableProperty().bind(notMultiLayerMode.or(selectionModel.allSelectedProperty()));
		menuItemSelectAll.setOnAction(_ -> selectionModel.selectAll());
		final var menuItemSelectNone = new MenuItem("Unselect all layers");
		menuItemSelectNone.disableProperty().bind(notMultiLayerMode.or(selectionModel.noneSelectedProperty()));
		menuItemSelectNone.setOnAction(_ -> selectionModel.selectNone());
		final var menuItemToggleLayerSelection = new MenuItem("Toggle layers selection");
		menuItemToggleLayerSelection.disableProperty().bind(notMultiLayerMode);
		menuItemToggleLayerSelection.setOnAction(_ -> selectionModel.invertSelection());
		final var menuItemResetAngles = new MenuItem("Reset dividers");
		menuItemResetAngles.disableProperty().bind(notMultiLayerMode);
		menuItemResetAngles.setOnAction(_ -> multiImageView.resetDividers());
		// ---
		final var menuItemFullScreen = new MenuItem();
		menuItemFullScreen.textProperty().bind(when(fullScreen.enabledProperty())
			.then("Exit Fullscreen").otherwise("Enter Fullscreen"));
		menuItemFullScreen.setOnAction(_ -> fullScreen.toggle());
		// ->
		return new ContextMenu(
			menuItemToolbar, menuItemThumbnails,
			new SeparatorMenuItem(),
			menuItemZoom100, menuItemFit, menuItemFill,
			new SeparatorMenuItem(),
			menuItemLayerAdd, menuItemLayerRemove,
			new SeparatorMenuItem(),
			menuItemModeSplit, menuItemModeSpot,
			new SeparatorMenuItem(),
			menuItemDivider, menuItemScrollbars,
			new SeparatorMenuItem(),
			menuItemToggleLayerSelection, menuItemSelectAll, menuItemSelectNone,
			menuItemResetAngles,
			new SeparatorMenuItem(),
			menuItemFullScreen);
	}

	private void addKeyEventFilter(Node node)
	{
		node.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
	}

	BooleanProperty fullScreenProperty()
	{
		return fullScreen.enabledProperty();
	}

	Region getRegion()
	{
		return paneOuter;
	}
}
