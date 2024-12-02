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
package de.bernd_michaely.diascope.app.stage;

import de.bernd_michaely.common.filesystem.view.base.Configuration;
import de.bernd_michaely.common.filesystem.view.base.UserNodeConfiguration;
import de.bernd_michaely.common.filesystem.view.fx.FileSystemTreeView;
import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.PreferencesUtil;
import de.bernd_michaely.diascope.app.dialog.PaneInfoAbout;
import de.bernd_michaely.diascope.app.dialog.PaneInfoSysEnv;
import de.bernd_michaely.diascope.app.dialog.ResizableDialog;
import de.bernd_michaely.diascope.app.icons.Icons;
import de.bernd_michaely.diascope.app.util.scene.SceneStylesheetUtil;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.ApplicationConfiguration.LaunchType.*;
import static de.bernd_michaely.diascope.app.control.ScaleBox.SpaceGainingMode.*;
import static de.bernd_michaely.diascope.app.dialog.ResizableDialog.DialogType.*;
import static de.bernd_michaely.diascope.app.stage.GlobalConstants.*;
import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.*;
import static de.bernd_michaely.diascope.app.stage.StringBindingAppTitle.TITLE_APPLICATION;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static java.lang.System.Logger.Level.*;
import static java.util.Objects.requireNonNullElse;
import static javafx.beans.binding.Bindings.not;

/**
 * The Diascope MainWindow class.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class MainWindow
{
	private static final Logger logger = System.getLogger(MainWindow.class.getName());
	private static final double INITIAL_WINDOW_SIZE = 2.0 / 3.0;
	private static final Preferences preferences = PreferencesUtil.nodeForPackage(MainWindow.class);
	private final FileSystemTreeView fileSystemTreeView;
	private final ResizableDialog dialogSystemEnvironment =
		new ResizableDialog(CLOSEABLE_DIALOG, NONE, false);
	private final ResizableDialog dialogInfoAbout =
		new ResizableDialog(CLOSEABLE_DIALOG, SCALING, true);
	private final BooleanProperty sidePaneVisibleProperty = new SimpleBooleanProperty();
	private final ChangeListener<Number> changeListenerSplitDividerFstv = onChange(splitPos ->
		preferences.putDouble(PREF_KEY_MAIN_SPLIT_POS.getKey(), splitPos.doubleValue()));
	private boolean showingHiddenDirs;
	private final CheckMenuItem menuItemShowStatusLine;
	private final MainContent mainContent;

	private static final ChangeListener<@Nullable Path> changeListenerPathPref = onChange(
		path -> preferences.put(PREF_KEY_SELECTED_PATH.getKey(),
			requireNonNullElse(path, PATH_USER_HOME).toString()));

	public MainWindow()
	{
		this.fileSystemTreeView = FileSystemTreeView.createInstance(
			Configuration.builder().setUserNodeConfiguration(new UserNodeConfiguration()
			{
				@Override
				public boolean isCreatingNodeForDirectory(Path directory)
				{
					try
					{
						return showingHiddenDirs || !Files.isHidden(directory);
					}
					catch (IOException ex)
					{
						logger.log(WARNING, "" + ex);
						return false;
					}
				}

				@Override
				public UserNodeConfiguration getUserNodeConfigurationFor(Path path)
				{
					return this;
				}
			}).build());
		this.menuItemShowStatusLine = new CheckMenuItem("Show status line");
		this.mainContent = new MainContent(
			this.fileSystemTreeView.selectedPathProperty(),
			this.menuItemShowStatusLine.selectedProperty());
		this.menuItemShowStatusLine.setSelected(
			preferences.getBoolean(PREF_KEY_SHOW_STATUS_LINE.getKey(), true));
		this.menuItemShowStatusLine.setOnAction(event ->
			preferences.putBoolean(PREF_KEY_SHOW_STATUS_LINE.getKey(),
				this.menuItemShowStatusLine.isSelected()));
	}

	/**
	 * Performs initialization of the main window.
	 *
	 * @param stage the primary stage
	 */
	public void initialize(Stage stage)
	{
		final var state = ApplicationConfiguration.getState();
		final BooleanProperty developmentModeProperty = state.developmentModeProperty();
		final var launchType = state.launchType();
		final boolean isDevelopmentMode = launchType == DEVELOPMENT;
		final boolean isTestMode = launchType == UNIT_TEST;
		dialogSystemEnvironment.setTitle("System Environment");
		dialogInfoAbout.setTitle("Info About");
		this.showingHiddenDirs = preferences.getBoolean(PREF_KEY_SHOW_HIDDEN_DIRS.getKey(), false);
		final var menuItemShowHidden = new CheckMenuItem("Show hidden directories");
		menuItemShowHidden.setSelected(this.showingHiddenDirs);
		final var paneFstv = new BorderPane(fileSystemTreeView.getComponent());
		final var tabFstv = new Tab("Filesystem", paneFstv);
		tabFstv.setClosable(false);
		final var tabPane = new TabPane(tabFstv);
		final var menuItemUpdate = new MenuItem("Update");
		menuItemUpdate.setOnAction(e -> fileSystemTreeView.updateTree());
		menuItemShowHidden.selectedProperty().addListener(onChange(() ->
		{
			this.showingHiddenDirs = !this.showingHiddenDirs;
			preferences.putBoolean(PREF_KEY_SHOW_HIDDEN_DIRS.getKey(), this.showingHiddenDirs);
			fileSystemTreeView.updateTree();
		}));
		final var menuFstv = new Menu("View");
		menuFstv.getItems().addAll(menuItemUpdate, menuItemShowHidden);
		paneFstv.setTop(new VBox(new MenuBar(menuFstv)));
		final var splitPane = new SplitPane(mainContent.getRegion());
		// *** Main Menu and ToolBar ***
		final var borderPane = new BorderPane(splitPane);
		// Actions:
		final EventHandler<ActionEvent> actionOpen = e ->
		{
			final var directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File(getSelectedPath()));
			final File result = directoryChooser.showDialog(stage);
			if (result != null)
			{
				fileSystemTreeView.expandPath(result.toPath(), true, true);
			}
		};
		final EventHandler<ActionEvent> actionClose = e -> fileSystemTreeView.clearSelection();
		final EventHandler<ActionEvent> actionExit = e ->
		{
			try
			{
				onApplicationClose();
			}
			finally
			{
				Platform.exit();
			}
		};
		final EventHandler<ActionEvent> actionSysEnv = e ->
			dialogSystemEnvironment.show(stage, new PaneInfoSysEnv().getDisplay());
		final EventHandler<ActionEvent> actionInfoAbout = e ->
			dialogInfoAbout.show(stage, new PaneInfoAbout(TITLE_APPLICATION).getDisplay());
		// Icons:
		final Image iconFstv = Icons.ShowSidePane.getIconImage();
		final Image iconFileOpen = Icons.FileOpen.getIconImage();
		final Image iconViewFullscreen = Icons.ViewFullscreen.getIconImage();
		final Image iconViewFullscreenPane = Icons.ViewFullscreenPane.getIconImage();
		final Image iconViewShowFirst = Icons.ViewShowFirst.getIconImage();
		final Image iconViewShowPrev = Icons.ViewShowPrev.getIconImage();
		final Image iconViewShowNext = Icons.ViewShowNext.getIconImage();
		final Image iconViewShowLast = Icons.ViewShowLast.getIconImage();
		// Menu("File")
		final var menuFile = new Menu("File");
		final var menuItemDirOpen = new MenuItem("Open directory");
		menuItemDirOpen.setOnAction(actionOpen);
		menuItemDirOpen.setAccelerator(new KeyCharacterCombination("o", KeyCombination.CONTROL_DOWN));
		final var buttonDirOpen = new Button();
		buttonDirOpen.setOnAction(menuItemDirOpen.getOnAction());
		buttonDirOpen.setTooltip(new Tooltip(menuItemDirOpen.getText()));
		if (iconFileOpen != null)
		{
			buttonDirOpen.setGraphic(new ImageView(iconFileOpen));
		}
		final var menuItemClose = new MenuItem("Close directory");
		menuItemClose.setOnAction(actionClose);
		menuItemClose.disableProperty().bind(not(fileSystemTreeView.pathSelectedProperty()));
		final var menuItemExit = new MenuItem("Exit");
		menuItemExit.setOnAction(actionExit);
		menuFile.getItems().addAll(
			menuItemDirOpen, menuItemClose, new SeparatorMenuItem(), menuItemExit);
		// Menu("View")
		final var menuView = new Menu("View");
		final var menuItemShowSidePane = new CheckMenuItem("Show side pane");
		if (iconFstv != null)
		{
			menuItemShowSidePane.setGraphic(new ImageView(iconFstv));
		}
		menuItemShowSidePane.selectedProperty().bindBidirectional(this.sidePaneVisibleProperty);
		final var menuItemFullscreen = new MenuItem("Fullscreen");
		menuItemFullscreen.setOnAction(_ -> mainContent.setFullscreen(false));
		menuItemFullscreen.disableProperty().bind(getListBinding().emptyProperty());
		menuItemFullscreen.setAccelerator(new KeyCodeCombination(KeyCode.F11));
		final Button buttonViewFullscreen = new Button();
		if (iconViewFullscreen != null)
		{
			menuItemFullscreen.setGraphic(new ImageView(iconViewFullscreen));
			buttonViewFullscreen.setGraphic(new ImageView(iconViewFullscreen));
		}
		else
		{
			buttonViewFullscreen.setText("FullScreen");
		}
		adaptActionState(menuItemFullscreen, buttonViewFullscreen, "Show image in fullscreen mode");
		final var menuItemFullscreenPane = new MenuItem("Fullscreen Pane");
		menuItemFullscreenPane.setOnAction(_ -> mainContent.setFullscreen(true));
		menuItemFullscreenPane.disableProperty().bind(getListBinding().emptyProperty());
		menuItemFullscreenPane.setAccelerator(new KeyCodeCombination(KeyCode.F11, KeyCombination.SHIFT_DOWN));
		final Button buttonViewFullscreenPane = new Button();
		if (iconViewFullscreenPane != null)
		{
			menuItemFullscreenPane.setGraphic(new ImageView(iconViewFullscreenPane));
			buttonViewFullscreenPane.setGraphic(new ImageView(iconViewFullscreenPane));
		}
		else
		{
			buttonViewFullscreenPane.setText("FullScr.Pane");
		}
		adaptActionState(menuItemFullscreenPane, buttonViewFullscreenPane, "Show image pane in fullscreen mode");
		menuView.getItems().addAll(menuItemFullscreen, menuItemFullscreenPane,
			new SeparatorMenuItem(), menuItemShowSidePane);
		// Menu("Navigation")
		final var menuNavigation = new Menu("Navigation");
		final var menuItemShowFirst = new MenuItem("Select First");
		menuItemShowFirst.setOnAction(_ -> mainContent.selectFirst());
		menuItemShowFirst.disableProperty().bind(
			selectedIndexProperty().isEqualTo(0).or(
				getListBinding().sizeProperty().lessThanOrEqualTo(1)));
		final var menuItemShowPrev = new MenuItem("Select Previous");
		menuItemShowPrev.setOnAction(_ -> mainContent.selectPrevious());
		menuItemShowPrev.disableProperty().bind(
			selectedIndexProperty().isEqualTo(0).or(
				getListBinding().sizeProperty().lessThanOrEqualTo(1)));
		final var menuItemShowNext = new MenuItem("Select Next");
		menuItemShowNext.setOnAction(_ -> mainContent.selectselectNext());
		menuItemShowNext.disableProperty().bind(
			selectedIndexProperty().isEqualTo(getListBinding().sizeProperty().subtract(1)).or(
				getListBinding().sizeProperty().lessThanOrEqualTo(1)));
		final var menuItemShowLast = new MenuItem("Select Last");
		menuItemShowLast.setOnAction(_ -> mainContent.selectselectLast());
		menuItemShowLast.disableProperty().bind(
			selectedIndexProperty().isEqualTo(getListBinding().sizeProperty().subtract(1)).or(
				getListBinding().sizeProperty().lessThanOrEqualTo(1)));
		final var buttonItemShowFirst = new Button();
		final var buttonItemShowPrev = new Button();
		final var buttonItemShowNext = new Button();
		final var buttonItemShowLast = new Button();
		if (iconViewShowFirst != null && iconViewShowPrev != null &&
			iconViewShowNext != null && iconViewShowLast != null)
		{
			menuItemShowFirst.setGraphic(new ImageView(iconViewShowFirst));
			menuItemShowPrev.setGraphic(new ImageView(iconViewShowPrev));
			menuItemShowNext.setGraphic(new ImageView(iconViewShowNext));
			menuItemShowLast.setGraphic(new ImageView(iconViewShowLast));
			buttonItemShowFirst.setGraphic(new ImageView(iconViewShowFirst));
			buttonItemShowPrev.setGraphic(new ImageView(iconViewShowPrev));
			buttonItemShowNext.setGraphic(new ImageView(iconViewShowNext));
			buttonItemShowLast.setGraphic(new ImageView(iconViewShowLast));
		}
		else
		{
			buttonItemShowFirst.setText(menuItemShowFirst.getText());
			buttonItemShowPrev.setText(menuItemShowPrev.getText());
			buttonItemShowNext.setText(menuItemShowNext.getText());
			buttonItemShowLast.setText(menuItemShowLast.getText());
		}
		adaptActionState(menuItemShowFirst, buttonItemShowFirst);
		adaptActionState(menuItemShowPrev, buttonItemShowPrev);
		adaptActionState(menuItemShowNext, buttonItemShowNext);
		adaptActionState(menuItemShowLast, buttonItemShowLast);
		menuNavigation.getItems().addAll(
			menuItemShowFirst, menuItemShowPrev, menuItemShowNext, menuItemShowLast);
		// Menu("Options")
		final var menuOptions = new Menu("Options");
		menuOptions.getItems().add(this.menuItemShowStatusLine);
		final CheckMenuItem menuItemDevelopmentMode;
		if (isDevelopmentMode)
		{
			menuItemDevelopmentMode = new CheckMenuItem("Development mode");
			developmentModeProperty.bind(menuItemDevelopmentMode.selectedProperty());
			menuOptions.getItems().add(menuItemDevelopmentMode);
		}
		else
		{
			menuItemDevelopmentMode = null;
		}
		final var menuHelp = new Menu("Help");
		final var menuItemSysEnv = new MenuItem("System Environment");
		menuItemSysEnv.setOnAction(actionSysEnv);
		final var menuItemInfoAbout = new MenuItem("About");
		menuItemInfoAbout.setOnAction(actionInfoAbout);
		menuHelp.getItems().addAll(menuItemSysEnv, menuItemInfoAbout);
		// ToolBar items:
		final var toggleButtonSidePane = new ToggleButton();
		if (iconFstv != null)
		{
			toggleButtonSidePane.setGraphic(new ImageView(iconFstv));
		}
		else
		{
			toggleButtonSidePane.setText("Sidepane");
		}
		toggleButtonSidePane.setTooltip(new Tooltip("Show/Hide side pane"));
		toggleButtonSidePane.selectedProperty().bindBidirectional(this.sidePaneVisibleProperty);
		// Menu and ToolBar:
		final var menuBar = new MenuBar(menuFile, menuView, menuNavigation, menuOptions, menuHelp);
		final var toolBar = new ToolBar(toggleButtonSidePane, buttonDirOpen,
			new Separator(),
			buttonViewFullscreen, buttonViewFullscreenPane,
			new Separator(),
			buttonItemShowFirst, buttonItemShowPrev, buttonItemShowNext, buttonItemShowLast
		);
		final var buttonExit = new Button("Exit");
		final var buttonExitSeparator = new Separator();
		buttonExit.prefHeightProperty().bind(toggleButtonSidePane.heightProperty());
		buttonExit.setTooltip(new Tooltip("Exit application"));
		buttonExit.setOnAction(actionExit);
		developmentModeProperty.addListener(onChange(isDevel ->
		{
			if (isDevel)
			{
				toolBar.getItems().addAll(buttonExitSeparator, buttonExit);
			}
			else
			{
				toolBar.getItems().removeAll(buttonExitSeparator, buttonExit);
			}
		}));
		borderPane.setTop(new VBox(menuBar, toolBar));
		// *** end Menu and ToolBar ***
		final var scene = new Scene(borderPane);
		SceneStylesheetUtil.setStylesheet(scene);
		initStageBounds(stage);
		// Note to the following listeners:
		// the test of (!stage.isMaximized()) doesn't work with all window managers
		stage.xProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_X.getKey(), stage.getX());
			}
		}));
		stage.yProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_Y.getKey(), stage.getY());
			}
		}));
		stage.widthProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_WIDTH.getKey(), stage.getWidth());
			}
		}));
		stage.heightProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_HEIGHT.getKey(), stage.getHeight());
			}
		}));
		stage.maximizedProperty().addListener(onChange(maximized ->
			preferences.putBoolean(PREF_KEY_MAXIMIZE.getKey(), maximized)));
		stage.titleProperty().bind(StringBindingAppTitle.create(
			fileSystemTreeView.selectedPathProperty(), developmentModeProperty));
		stage.setOnCloseRequest(event -> onApplicationClose());
		if (isTestMode)
		{
			Platform.runLater(Platform::exit);
		}
		else
		{
			stage.setScene(scene);
			stage.show();
			this.sidePaneVisibleProperty.addListener(onChange(visible ->
			{
				final ObservableList<Node> items = splitPane.getItems();
				final ObservableList<SplitPane.Divider> dividers = splitPane.getDividers();
				if (visible)
				{
					if (items.size() == 1)
					{
						items.add(0, tabPane);
						final SplitPane.Divider divider = dividers.get(0);
						divider.setPosition(preferences.getDouble(PREF_KEY_MAIN_SPLIT_POS.getKey(), 1.0 / 3.0));
						divider.positionProperty().addListener(changeListenerSplitDividerFstv);
					}
					else
					{
						logger.log(WARNING, "Inconsistent state when setting FileSystemTreeView visible");
					}
				}
				else
				{
					if (items.size() == 2)
					{
						final SplitPane.Divider divider = dividers.get(0);
						divider.positionProperty().removeListener(changeListenerSplitDividerFstv);
						preferences.putDouble(PREF_KEY_MAIN_SPLIT_POS.getKey(), divider.getPosition());
						items.remove(tabPane);
					}
					else
					{
						logger.log(WARNING, "Inconsistent state when setting FileSystemTreeView invisible");
					}
				}
				preferences.putBoolean(PREF_KEY_FSTV_VISIBLE.getKey(), visible);
			}));
			this.sidePaneVisibleProperty.setValue(
				preferences.getBoolean(PREF_KEY_FSTV_VISIBLE.getKey(), true));
			final String selectedPath = getSelectedPath();
			if (!selectedPath.isBlank())
			{
				fileSystemTreeView.expandPath(Paths.get(selectedPath), true, true);
			}
			fileSystemTreeView.selectedPathProperty().addListener(changeListenerPathPref);
			if (menuItemDevelopmentMode != null)
			{
				menuItemDevelopmentMode.selectedProperty().set(true);
			}
			this.mainContent.postVisibleInit();
		}
	}

	private ReadOnlyIntegerProperty selectedIndexProperty()
	{
		return mainContent.selectedIndexProperty();
	}

	private ListBinding<ImageGroupDescriptor> getListBinding()
	{
		return mainContent.getListBinding();
	}

	private void adaptActionState(MenuItem from, Button to)
	{
		adaptActionState(from, to, from.getText());
	}

	private void adaptActionState(MenuItem from, Button to, String textTooltip)
	{
		to.setOnAction(from.getOnAction());
		to.disableProperty().bind(from.disableProperty());
		to.setTooltip(new Tooltip(textTooltip));
	}

	private void onApplicationClose()
	{
		logger.log(TRACE, "Closing main window …");
		try (fileSystemTreeView)
		{
			fileSystemTreeView.selectedPathProperty().removeListener(changeListenerPathPref);
			mainContent.onApplicationClose();
		}
		catch (IOException ex)
		{
			logger.log(WARNING, ex);
		}
	}

	private static String getSelectedPath()
	{
		return preferences.get(PREF_KEY_SELECTED_PATH.getKey(), PATH_USER_HOME.toString());
	}

	private static void initStageBounds(Stage stage)
	{
		final double widthPref = preferences.getDouble(PREF_KEY_WIDTH.getKey(), Integer.MIN_VALUE);
		final double heightPref = preferences.getDouble(PREF_KEY_HEIGHT.getKey(), Integer.MIN_VALUE);
		final boolean needsInit = widthPref < 0 || heightPref < 0;
		if (needsInit)
		{
			logger.log(TRACE, "Initializing main window bounds…");
			final Rectangle2D bounds = Screen.getPrimary().getBounds();
			final double f = INITIAL_WINDOW_SIZE;
			final double w = bounds.getWidth();
			final double h = bounds.getHeight();
			final double wf = w * f;
			final double hf = h * f;
			stage.setX((w - wf) / 2);
			stage.setY((h - hf) / 2);
			stage.setWidth(wf);
			stage.setHeight(hf);
			preferences.putDouble(PREF_KEY_X.getKey(), stage.getX());
			preferences.putDouble(PREF_KEY_Y.getKey(), stage.getY());
			preferences.putDouble(PREF_KEY_HEIGHT.getKey(), stage.getHeight());
			preferences.putDouble(PREF_KEY_HEIGHT.getKey(), stage.getHeight());
		}
		else
		{
			stage.setX(preferences.getDouble(PREF_KEY_X.getKey(), 0));
			stage.setY(preferences.getDouble(PREF_KEY_Y.getKey(), 0));
			stage.setWidth(widthPref);
			stage.setHeight(heightPref);
			stage.setMaximized(preferences.getBoolean(PREF_KEY_MAXIMIZE.getKey(), false));
		}
	}
}
