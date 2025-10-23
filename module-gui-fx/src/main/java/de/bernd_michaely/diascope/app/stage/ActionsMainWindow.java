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

import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.icons.Icons;
import de.bernd_michaely.diascope.app.util.action.Action;
import de.bernd_michaely.diascope.app.util.action.ActionItemDescriptor;
import de.bernd_michaely.diascope.app.util.action.CheckedAction;
import de.bernd_michaely.diascope.app.util.action.TriggerAction;
import java.util.List;
import java.util.stream.Stream;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import static de.bernd_michaely.diascope.app.ApplicationConfiguration.LaunchType.*;
import static de.bernd_michaely.diascope.app.stage.ActionsImageControl.*;
import static de.bernd_michaely.diascope.app.util.action.Action.SEPARATOR;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

/// Class to define Actions for the main window.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ActionsMainWindow
{
	// menuFile
	final TriggerAction actionOpen;
	final TriggerAction actionClose;
	final TriggerAction actionExit;
	// menuView
	final CheckedAction actionFullScreen;
	final CheckedAction actionShowSidePane;
	// menuNavigation
	final TriggerAction actionShowFirst;
	final TriggerAction actionShowPrev;
	final TriggerAction actionShowNext;
	final TriggerAction actionShowLast;
	// menuOptions
	final CheckedAction actionShowHeaderBar;
	final CheckedAction actionShowToolBar;
	final CheckedAction actionShowStatusLine;
	final CheckedAction actionDevelopmentMode;
	// menuHelp
	final TriggerAction actionSysEnv;
	final TriggerAction actionInfoAbout;
	// result
	private final MenuBar menuBar;
	private final ToolBar toolBar;

	ActionsMainWindow()
	{
		final var state = ApplicationConfiguration.getState();
		final boolean isDevelopmentMode = state.launchType() == DEVELOPMENT;
		this.actionOpen = new TriggerAction(new ActionItemDescriptor(
			Icons.FileOpen, "Open", "Open directory …", null));
		this.actionClose = new TriggerAction(new ActionItemDescriptor(
			null, "Close", "Close directory", null));
		this.actionExit = new TriggerAction(new ActionItemDescriptor(
			null, "Exit", "Exit", "Exit application"));
		// MenuBar
		final var menuFile = new Menu("File");
		final var menuView = new Menu("View");
		final var menuNavigation = new Menu("Navigation");
		final var menuOptions = new Menu("Options");
		final var menuHelp = new Menu("Help");
		this.menuBar = new MenuBar(menuFile, menuView, menuNavigation, menuOptions, menuHelp);
		// menuFile
		final List<MenuItem> menuItemsOpen = actionOpen.createMenuItems();
		menuItemsOpen.getFirst().setAccelerator(new KeyCharacterCombination("o", CONTROL_DOWN));
		menuFile.getItems().addAll(menuItemsOpen);
		menuFile.getItems().addAll(actionClose.createMenuItems());
		menuFile.getItems().addAll(new SeparatorMenuItem());
		menuFile.getItems().addAll(actionExit.createMenuItems());
		// menuView
		this.actionFullScreen = new CheckedAction(ACTION_ITEM_DESCRIPTOR_FULLSCREEN);
		final List<CheckMenuItem> menuItemsFullScreen = actionFullScreen.createMenuItems();
		menuItemsFullScreen.getFirst().setAccelerator(new KeyCodeCombination(KeyCode.F11));
		this.actionShowSidePane = new CheckedAction(new ActionItemDescriptor(
			Icons.ShowSidePane, "[←]", "Show side pane", "Show/Hide side pane"));
		menuView.getItems().addAll(menuItemsFullScreen);
		menuView.getItems().addAll(actionShowSidePane.createMenuItems());
		// menuNavigation
		this.actionShowFirst = new TriggerAction(new ActionItemDescriptor(
			Icons.ViewShowFirst, "<<", "Select First", null));
		this.actionShowPrev = new TriggerAction(new ActionItemDescriptor(
			Icons.ViewShowPrev, "<", "Select Previous", null));
		this.actionShowNext = new TriggerAction(new ActionItemDescriptor(
			Icons.ViewShowNext, ">", "Select Next", null));
		this.actionShowLast = new TriggerAction(new ActionItemDescriptor(
			Icons.ViewShowLast, ">>", "Select Last", null));
		menuNavigation.getItems().addAll(actionShowFirst.createMenuItems());
		menuNavigation.getItems().addAll(actionShowPrev.createMenuItems());
		menuNavigation.getItems().addAll(actionShowNext.createMenuItems());
		menuNavigation.getItems().addAll(actionShowLast.createMenuItems());
		// menuOptions
		if (isDevelopmentMode)
		{
			this.actionDevelopmentMode = new CheckedAction(new ActionItemDescriptor("Development mode"));
			final var selectedProperty = actionDevelopmentMode.selectedProperty();
			state.developmentModeProperty().bind(selectedProperty);
			menuOptions.getItems().addAll(actionDevelopmentMode.createMenuItems());
		}
		else
		{
			this.actionDevelopmentMode = new CheckedAction();
		}
		this.actionShowHeaderBar = new CheckedAction(new ActionItemDescriptor("Show header bar"));
		menuOptions.getItems().addAll(actionShowHeaderBar.createMenuItems());
		this.actionShowToolBar = new CheckedAction(new ActionItemDescriptor("Show tool bar"));
		menuOptions.getItems().addAll(actionShowToolBar.createMenuItems());
		this.actionShowStatusLine = new CheckedAction(new ActionItemDescriptor("Show status line"));
		menuOptions.getItems().addAll(actionShowStatusLine.createMenuItems());
		// menuHelp
		this.actionSysEnv = new TriggerAction(new ActionItemDescriptor("System Environment"));
		this.actionInfoAbout = new TriggerAction(new ActionItemDescriptor("About"));
		menuHelp.getItems().addAll(actionSysEnv.createMenuItems());
		menuHelp.getItems().addAll(actionInfoAbout.createMenuItems());
		// ToolBar
		this.toolBar = new ToolBar(
			List.of(
				actionShowSidePane,
				actionOpen,
				SEPARATOR,
				actionFullScreen,
				SEPARATOR,
				actionShowFirst, actionShowPrev, actionShowNext, actionShowLast
			).stream().map(Action::createToolBarButtons).flatMap(List::stream).toArray(Control[]::new));
		final List<Button> buttonsExit = actionExit.createToolBarButtons();
		final List<Control> controlsExit = Stream.concat(
			Stream.of(Action.createToolBarSeparator()),
			buttonsExit.stream()).toList();
		toolBar.getItems().stream()
			.filter(node -> node instanceof Button).map(node -> (Button) node)
			.findAny().ifPresent(button ->
				buttonsExit.getFirst().prefHeightProperty().bind(button.heightProperty()));
		state.developmentModeProperty().addListener(onChange(isDevel ->
		{
			if (isDevel)
			{
				toolBar.getItems().addAll(controlsExit);
			}
			else
			{
				toolBar.getItems().removeAll(controlsExit);
			}
		}));
	}

	MenuBar getMenuBar()
	{
		return menuBar;
	}

	ToolBar getToolBar()
	{
		return toolBar;
	}
}
