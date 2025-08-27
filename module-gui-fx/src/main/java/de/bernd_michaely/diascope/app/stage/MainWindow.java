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

import de.bernd_michaely.common.filesystem.view.fx.FileSystemTreeView;
import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.PreferencesUtil;
import de.bernd_michaely.diascope.app.dialog.PaneInfoAbout;
import de.bernd_michaely.diascope.app.dialog.PaneInfoSysEnv;
import de.bernd_michaely.diascope.app.dialog.ResizableDialog;
import de.bernd_michaely.diascope.app.icons.Icons;
import de.bernd_michaely.diascope.app.util.action.Action;
import de.bernd_michaely.diascope.app.util.action.CheckedAction;
import de.bernd_michaely.diascope.app.util.scene.SceneStylesheetUtil;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.ApplicationConfiguration.LaunchType.*;
import static de.bernd_michaely.diascope.app.ApplicationConfiguration.getApplicationName;
import static de.bernd_michaely.diascope.app.control.ScaleBox.SpaceGainingMode.*;
import static de.bernd_michaely.diascope.app.dialog.ResizableDialog.DialogType.*;
import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static de.bernd_michaely.diascope.app.util.beans.property.PersistedProperties.*;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.not;

/**
 * The Diascope MainWindow class.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class MainWindow
{
	private static final Logger logger = System.getLogger(MainWindow.class.getName());
	private static final String FILE_RES_ICON_STAGE = "diascope.png";
	private final @MonotonicNonNull Image iconStage;
	private static final double INITIAL_WINDOW_SIZE = 2.0 / 3.0;
	private static final Preferences preferences = PreferencesUtil.nodeForPackage(MainWindow.class);
	private @MonotonicNonNull PaneFileSystem paneFileSystem;
	private final StringProperty titleProperty = new SimpleStringProperty();
	private final MenuItem menuItemClose;
	private final ResizableDialog dialogSystemEnvironment =
		new ResizableDialog(CLOSEABLE_DIALOG, NONE, false);
	private final ResizableDialog dialogInfoAbout =
		new ResizableDialog(CLOSEABLE_DIALOG, SCALING, true);
	private final BooleanProperty sidePaneVisibleProperty;
	private final BooleanProperty sidePaneVisiblePersistedProperty;
	private final DoubleProperty mainSplitPosPersistedProperty;
	private final CheckMenuItem menuItemShowStatusLine;
	private final BooleanProperty showStatusLinePersistedProperty;
	private final Menu menuOptions;
	private final MenuItem menuItemDirOpen, menuItemSysEnv, menuItemInfoAbout, menuItemExit;
	private final @Nullable CheckMenuItem menuItemDevelopmentMode;
	private final SplitPane splitPane;
	private final BorderPane borderPane;
	private final TabPane tabPane;
	private final MainContent mainContent;

	public MainWindow()
	{
		this.sidePaneVisibleProperty = new SimpleBooleanProperty();
		this.sidePaneVisiblePersistedProperty = newPersistedBooleanProperty(
			PREF_KEY_FSTV_VISIBLE, getClass(), true);
		this.mainSplitPosPersistedProperty = newPersistedDoubleProperty(
			PREF_KEY_SPLIT_POS_MAIN, getClass(), 1.0 / 3.0);
		this.menuItemShowStatusLine = new CheckMenuItem("Show status line");
		this.showStatusLinePersistedProperty = newPersistedBooleanProperty(
			PREF_KEY_SHOW_STATUS_LINE, getClass(), true);
		this.menuItemShowStatusLine.selectedProperty().bindBidirectional(showStatusLinePersistedProperty);
		this.mainContent = new MainContent(this.menuItemShowStatusLine.selectedProperty());
		final var state = ApplicationConfiguration.getState();
		final BooleanProperty developmentModeProperty = state.developmentModeProperty();
		final var launchType = state.launchType();
		final boolean isDevelopmentMode = DEVELOPMENT.equals(launchType);
		this.menuOptions = new Menu("Options");
		this.menuItemDirOpen = new MenuItem("Open directory …");
		this.menuItemSysEnv = new MenuItem("System Environment");
		this.menuItemInfoAbout = new MenuItem("About");
		if (isDevelopmentMode)
		{
			final var checkMenuItem = new CheckMenuItem("Development mode");
			developmentModeProperty.bind(checkMenuItem.selectedProperty());
			menuOptions.getItems().add(checkMenuItem);
			menuItemDevelopmentMode = checkMenuItem;
		}
		else
		{
			menuItemDevelopmentMode = null;
		}
		this.splitPane = new SplitPane(mainContent.getRegion());
		this.borderPane = new BorderPane(splitPane);
		// Icons:
		final Image iconFstv = Icons.ShowSidePane.getIconImage();
		final Image iconFileOpen = Icons.FileOpen.getIconImage();
		final Image iconViewFullscreen = Icons.ViewFullscreen.getIconImage();
		final Image iconViewShowFirst = Icons.ViewShowFirst.getIconImage();
		final Image iconViewShowPrev = Icons.ViewShowPrev.getIconImage();
		final Image iconViewShowNext = Icons.ViewShowNext.getIconImage();
		final Image iconViewShowLast = Icons.ViewShowLast.getIconImage();
		// Menu("File")
		final var menuFile = new Menu("File");
		menuItemDirOpen.setAccelerator(new KeyCharacterCombination("o", KeyCombination.CONTROL_DOWN));
		final var buttonDirOpen = new Button();
		adaptActionState(menuItemDirOpen, buttonDirOpen, menuItemDirOpen.getText());
		if (iconFileOpen != null)
		{
			buttonDirOpen.setGraphic(new ImageView(iconFileOpen));
		}
		this.menuItemClose = new MenuItem("Close directory");
		this.menuItemExit = new MenuItem("Exit");
		menuFile.getItems().addAll(
			menuItemDirOpen, menuItemClose, new SeparatorMenuItem(), menuItemExit);
		// Menu("View")
		final var menuView = new Menu("View");
		final var menuItemShowSidePane = new CheckMenuItem("Show side pane");
		if (iconFstv != null)
		{
			menuItemShowSidePane.setGraphic(new ImageView(iconFstv));
		}
		menuItemShowSidePane.selectedProperty().bindBidirectional(this.sidePaneVisiblePersistedProperty);
		final CheckedAction actionFullScreen = mainContent.getActionFullScreen();
		final var menuItemFullscreen = actionFullScreen.createMenuItems();
		menuItemFullscreen.getFirst().setAccelerator(new KeyCodeCombination(KeyCode.F11));
		final var buttonViewFullscreen = actionFullScreen.createToolBarButtons();
		menuView.getItems().addAll(menuItemFullscreen);
		menuView.getItems().addAll(menuItemShowSidePane);
		// Menu("Navigation")
		final var menuNavigation = new Menu("Navigation");
		final var menuItemShowFirst = new MenuItem("Select First");
		menuItemShowFirst.setOnAction(_ -> mainContent.selectFirst());
		menuItemShowFirst.disableProperty().bind(
			mainContent.selectedIndexProperty().isEqualTo(0).or(
				mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
		final var menuItemShowPrev = new MenuItem("Select Previous");
		menuItemShowPrev.setOnAction(_ -> mainContent.selectPrevious());
		menuItemShowPrev.disableProperty().bind(
			mainContent.selectedIndexProperty().isEqualTo(0).or(
				mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
		final var menuItemShowNext = new MenuItem("Select Next");
		menuItemShowNext.setOnAction(_ -> mainContent.selectselectNext());
		menuItemShowNext.disableProperty().bind(
			mainContent.selectedIndexProperty().isEqualTo(
				mainContent.getListViewProperty().sizeProperty().subtract(1)).or(
				mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
		final var menuItemShowLast = new MenuItem("Select Last");
		menuItemShowLast.setOnAction(_ -> mainContent.selectselectLast());
		menuItemShowLast.disableProperty().bind(
			mainContent.selectedIndexProperty().isEqualTo(
				mainContent.getListViewProperty().sizeProperty().subtract(1)).or(
				mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
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
		menuOptions.getItems().add(this.menuItemShowStatusLine);
		final var menuHelp = new Menu("Help");
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
		toggleButtonSidePane.selectedProperty().bindBidirectional(this.sidePaneVisiblePersistedProperty);
		// Menu and ToolBar:
		final var menuBar = new MenuBar(menuFile, menuView, menuNavigation, menuOptions, menuHelp);
		final var toolBar = new ToolBar();
		toolBar.getItems().addAll(toggleButtonSidePane, buttonDirOpen);
		toolBar.getItems().add(Action.createToolBarSeparator());
		toolBar.getItems().addAll(buttonViewFullscreen);
		toolBar.getItems().add(Action.createToolBarSeparator());
		toolBar.getItems().addAll(buttonItemShowFirst, buttonItemShowPrev, buttonItemShowNext, buttonItemShowLast);
		final var buttonExit = new Button("Exit");
		final var buttonExitSeparator = new Separator();
		buttonExit.prefHeightProperty().bind(toggleButtonSidePane.heightProperty());
		adaptActionState(menuItemExit, buttonExit, "Exit application");
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
		dialogSystemEnvironment.setTitle("System Environment");
		dialogInfoAbout.setTitle("Info About");
		this.tabPane = new TabPane();
		Image _iconStage = null;
		try (var inputStream = getClass().getResourceAsStream(FILE_RES_ICON_STAGE))
		{
			if (inputStream != null)
			{
				_iconStage = new Image(inputStream);
			}
		}
		catch (IOException ex)
		{
			logger.log(TRACE, "Can't find stage icon »%s«".formatted(FILE_RES_ICON_STAGE), ex);
			_iconStage = null;
		}
		if (_iconStage == null)
		{
			logger.log(WARNING, "Can't find stage icon (ignoring…)");
		}
		this.iconStage = _iconStage;
	}

	public void setFileSystemView(PaneFileSystem paneFileSystem)
	{
		if (this.paneFileSystem == null)
		{
			this.paneFileSystem = paneFileSystem;
			final var fstv = paneFileSystem.getFileSystemTreeView();
			menuItemClose.setOnAction(e -> fstv.clearSelection());
			menuItemClose.disableProperty().bind(not(fstv.pathSelectedProperty()));
			mainContent.setSelectedPathProperty(fstv.selectedPathProperty());
			titleProperty.bind(StringBindingAppTitle.create(fstv.selectedPathProperty(),
				ApplicationConfiguration.getState().developmentModeProperty()));
			tabPane.getTabs().add(paneFileSystem.getTabFstv());
		}
		else
		{
			throw new IllegalStateException("PaneFileSystem initialized twice");
		}
	}

	private @Nullable
	FileSystemTreeView getFileSystemTreeView()
	{
		return paneFileSystem != null ? paneFileSystem.getFileSystemTreeView() : null;
	}

	/**
	 * Performs initialization of the main window.
	 *
	 * @param stage the primary stage
	 */
	public void _start(Stage stage)
	{
		if (iconStage != null)
		{
			stage.getIcons().add(iconStage);
		}
		final var state = ApplicationConfiguration.getState();
		stage.titleProperty().bind(titleProperty);
		// Actions:
		final EventHandler<ActionEvent> actionOpen = e ->
		{
			final var directoryChooser = new DirectoryChooser();
			if (paneFileSystem != null)
			{
				final var selectedPath = paneFileSystem.selectedPathPersistedProperty().get();
				if (selectedPath != null)
				{
					directoryChooser.setTitle("Open directory");
					directoryChooser.setInitialDirectory(selectedPath.toFile());
				}
				final File result = directoryChooser.showDialog(stage);
				if (result != null)
				{
					final var fileSystemTreeView = paneFileSystem.getFileSystemTreeView();
					fileSystemTreeView.expandPath(result.toPath(), true, true);
				}
			}
			else
			{
				throw new IllegalStateException("PaneFileSystem not initialized");
			}
		};
		final EventHandler<ActionEvent> actionSysEnv = e ->
			dialogSystemEnvironment.show(stage, new PaneInfoSysEnv().getDisplay());
		final EventHandler<ActionEvent> actionInfoAbout = e ->
			dialogInfoAbout.show(stage, new PaneInfoAbout(getApplicationName(), null).getDisplay());
		menuItemExit.setOnAction(e ->
		{
			try
			{
				onApplicationClose();
			}
			finally
			{
				Platform.exit();
			}
		});
		menuItemDirOpen.setOnAction(actionOpen);
		menuItemSysEnv.setOnAction(actionSysEnv);
		menuItemInfoAbout.setOnAction(actionInfoAbout);
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
		stage.setOnCloseRequest(event -> onApplicationClose());
		if (state.launchType() == UNIT_TEST)
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
						divider.positionProperty().bindBidirectional(mainSplitPosPersistedProperty);
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
						divider.positionProperty().unbind();
						items.remove(tabPane);
					}
					else
					{
						logger.log(WARNING, "Inconsistent state when setting FileSystemTreeView invisible");
					}
				}
			}));
			sidePaneVisibleProperty.bind(sidePaneVisiblePersistedProperty);
			if (menuItemDevelopmentMode != null)
			{
				menuItemDevelopmentMode.selectedProperty().set(true);
			}
			this.mainContent.postVisibleInit();
		}
	}

	private void adaptActionState(@UnderInitialization MainWindow this,
		MenuItem from, Button to)
	{
		adaptActionState(from, to, from.getText());
	}

	private void adaptActionState(@UnderInitialization MainWindow this,
		MenuItem from, Button to, String textTooltip)
	{
		to.onActionProperty().bindBidirectional(from.onActionProperty());
		to.disableProperty().bind(from.disableProperty());
		to.setTooltip(new Tooltip(textTooltip));
	}

	private void onApplicationClose()
	{
		logger.log(TRACE, "Closing main window …");
		if (paneFileSystem != null)
		{
			paneFileSystem.selectedPathPersistedProperty().unbind();
		}
		mainContent.onApplicationClose();
		try
		{
			final var fileSystemTreeView = getFileSystemTreeView();
			if (fileSystemTreeView != null)
			{
				fileSystemTreeView.close();
			}
		}
		catch (IOException ex)
		{
			logger.log(WARNING, ex);
		}
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
