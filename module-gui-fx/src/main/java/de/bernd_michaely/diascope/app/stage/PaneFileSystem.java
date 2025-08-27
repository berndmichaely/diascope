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

import de.bernd_michaely.common.filesystem.view.base.Configuration;
import de.bernd_michaely.common.filesystem.view.base.UserNodeConfiguration;
import de.bernd_michaely.common.filesystem.view.fx.FileSystemTreeView;
import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.stage.GlobalConstants.PATH_USER_HOME;
import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.PREF_KEY_SELECTED_PATH;
import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.PREF_KEY_SHOW_HIDDEN_DIRS;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static de.bernd_michaely.diascope.app.util.beans.property.PersistedProperties.newPersistedBooleanProperty;
import static de.bernd_michaely.diascope.app.util.beans.property.PersistedProperties.newPersistedObjectProperty;
import static java.lang.System.Logger.Level.*;

/// Main FileSystem pane.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class PaneFileSystem
{
	private static final Logger logger = System.getLogger(PaneFileSystem.class.getName());
	private final FileSystemTreeView fileSystemTreeView;
	private final BooleanProperty showingHiddenDirsProperty;
	private final BorderPane paneFstv;
	private final Tab tabFstv;
	private final ObjectProperty<@Nullable Path> selectedPathPersistedProperty;

	public PaneFileSystem()
	{
		this.showingHiddenDirsProperty = newPersistedBooleanProperty(
			PREF_KEY_SHOW_HIDDEN_DIRS, getClass(), false);
		this.fileSystemTreeView = FileSystemTreeView.createInstance(
			Configuration.builder().setUserNodeConfiguration(new UserNodeConfiguration()
			{
				@Override
				public boolean isCreatingNodeForDirectory(Path directory)
				{
					try
					{
						return showingHiddenDirsProperty.get() || !Files.isHidden(directory);
					}
					catch (IOException ex)
					{
						logger.log(WARNING, "UserNodeConfiguration::isCreatingNodeForDirectory", ex);
						return false;
					}
				}

				@Override
				public UserNodeConfiguration getUserNodeConfigurationFor(Path path)
				{
					return this;
				}
			}).build());
		final var menuItemUpdate = new MenuItem("Update");
		final var menuItemShowHidden = new CheckMenuItem("Show hidden directories");
		menuItemShowHidden.selectedProperty().bindBidirectional(showingHiddenDirsProperty);
		menuItemUpdate.setOnAction(e -> fileSystemTreeView.updateTree());
		final var menuFstv = new Menu("View");
		menuFstv.getItems().addAll(menuItemUpdate, menuItemShowHidden);
		this.paneFstv = new BorderPane(fileSystemTreeView.getComponent());
		paneFstv.setTop(new VBox(new MenuBar(menuFstv)));
		this.tabFstv = new Tab("Filesystem", paneFstv);
		tabFstv.setClosable(false);
		showingHiddenDirsProperty.addListener(onChange(fileSystemTreeView::updateTree));
		this.selectedPathPersistedProperty = newPersistedObjectProperty(
			PREF_KEY_SELECTED_PATH, getClass(), PATH_USER_HOME.toString(), Paths::get);
		ApplicationConfiguration.getState().initialPath().ifPresentOrElse(str ->
		{
			if (!str.isBlank())
			{
				final Path dir = Path.of(str);
				final Path path = Files.isDirectory(dir) ? dir : PATH_USER_HOME;
				logger.log(INFO, () -> "Path to open at launch: »%s«"
					.formatted(path != null ? path.toString() : ""));
				fileSystemTreeView.expandPath(path, true, true);
			}
		}, () ->
		{
			fileSystemTreeView.expandPath(selectedPathPersistedProperty.get(), true, true);
		});
		selectedPathPersistedProperty.bind(fileSystemTreeView.selectedPathProperty());
	}

	FileSystemTreeView getFileSystemTreeView()
	{
		return fileSystemTreeView;
	}

	Tab getTabFstv()
	{
		return tabFstv;
	}

	ObjectProperty<@Nullable Path> selectedPathPersistedProperty()
	{
		return selectedPathPersistedProperty;
	}
}
