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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

/// The Diascope MainWindow class.
///
/// @author The Diascope MainWindow class.
///
public class MainWindow
{
	private static final Logger logger = System.getLogger(MainWindow.class.getName());
	private static final String FILE_RES_ICON_STAGE = "diascope.png";
	private static final String FILE_RES_ICON_STAGE_2 = "diascope_t.png";
	private static final String FILE_RES_ICON_STAGE_FULLSCREEN = "diascope_fullscreen2.png";
	private final @MonotonicNonNull Image iconStage;
	private final @MonotonicNonNull Image iconStageFullScreen;
	private static final Preferences preferences = PreferencesUtil.nodeForPackage(MainWindow.class);
	private @MonotonicNonNull PaneFileSystem paneFileSystem;
	private final StringProperty titleProperty = new SimpleStringProperty();
	private final ActionsMainWindow actions = new ActionsMainWindow();
	private final MenuBar menuBar = actions.getMenuBar();
	private final ToolBar toolBar = actions.getToolBar();
	private final ResizableDialog dialogSystemEnvironment =
		new ResizableDialog(CLOSEABLE_DIALOG, NONE, false);
	private final ResizableDialog dialogInfoAbout =
		new ResizableDialog(CLOSEABLE_DIALOG, SCALING, true);
	private final BooleanProperty sidePaneVisibleProperty;
	private final BooleanProperty sidePaneVisiblePersistedProperty;
	private final DoubleProperty mainSplitPosPersistedProperty;
	private final BooleanProperty showHeaderBarPersistedProperty;
	private final BooleanProperty showToolBarPersistedProperty;
	private final BooleanProperty showStatusLinePersistedProperty;
	private final SplitPane splitPane;
	private final BorderPane rootPane, toolBarPane;
	private final TabPane tabPane;
	private @MonotonicNonNull MainContent mainContent;

	public MainWindow()
	{
		this.splitPane = new SplitPane();
		this.toolBarPane = new BorderPane(splitPane);
		this.rootPane = new BorderPane(toolBarPane);
		this.tabPane = new TabPane();
		// sidePane
		this.sidePaneVisibleProperty = new SimpleBooleanProperty();
		this.sidePaneVisiblePersistedProperty = newPersistedBooleanProperty(
			PREF_KEY_FSTV_VISIBLE, getClass(), true);
		actions.actionShowSidePane.selectedProperty().bindBidirectional(this.sidePaneVisiblePersistedProperty);
		this.mainSplitPosPersistedProperty = newPersistedDoubleProperty(
			PREF_KEY_SPLIT_POS_MAIN, getClass(), 1.0 / 3.0);
		// HeaderBar
		this.showHeaderBarPersistedProperty = newPersistedBooleanProperty(
			PREF_KEY_SHOW_HEADERBAR, getClass(), false);
		// ToolBar
		this.showToolBarPersistedProperty = newPersistedBooleanProperty(
			PREF_KEY_SHOW_TOOLBAR, getClass(), true);
		// StatusLine
		this.showStatusLinePersistedProperty = newPersistedBooleanProperty(
			PREF_KEY_SHOW_STATUS_LINE, getClass(), true);
		actions.actionShowStatusLine.selectedProperty().bindBidirectional(showStatusLinePersistedProperty);
		// dialogs
		dialogSystemEnvironment.setTitle("System Environment");
		dialogInfoAbout.setTitle("Info About");
		this.iconStage = createResourceImage(FILE_RES_ICON_STAGE);
		this.iconStageFullScreen = createResourceImage(FILE_RES_ICON_STAGE_FULLSCREEN);
	}

	public void setMainContent(MainContent mainContent)
	{
		if (this.mainContent == null && mainContent != null)
		{
			this.mainContent = mainContent;
			actions.actionFullScreen.selectedProperty().bindBidirectional(
				mainContent.getActionFullScreen().selectedProperty());
			mainContent.bindShowStatusLineProperty(actions.actionShowStatusLine.selectedProperty());
			this.splitPane.getItems().add(mainContent.getRegion());
			actions.actionShowFirst.setOnAction(_ -> mainContent.selectFirst());
			actions.actionShowFirst.disableProperty().bind(
				mainContent.selectedIndexProperty().isEqualTo(0).or(
					mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
			actions.actionShowPrev.setOnAction(_ -> mainContent.selectPrevious());
			actions.actionShowPrev.disableProperty().bind(
				mainContent.selectedIndexProperty().isEqualTo(0).or(
					mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
			actions.actionShowNext.setOnAction(_ -> mainContent.selectselectNext());
			actions.actionShowNext.disableProperty().bind(
				mainContent.selectedIndexProperty().isEqualTo(
					mainContent.getListViewProperty().sizeProperty().subtract(1)).or(
					mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
			actions.actionShowLast.setOnAction(_ -> mainContent.selectselectLast());
			actions.actionShowLast.disableProperty().bind(
				mainContent.selectedIndexProperty().isEqualTo(
					mainContent.getListViewProperty().sizeProperty().subtract(1)).or(
					mainContent.getListViewProperty().sizeProperty().lessThanOrEqualTo(1)));
			// ToolBar
			final BooleanProperty showToolBarProperty = actions.actionShowToolBar.selectedProperty();
			showToolBarProperty.addListener(onChange(enabled ->
			{
				if (enabled)
				{
					toolBarPane.setTop(toolBar);
				}
				else
				{
					toolBarPane.getChildren().remove(toolBar);
				}
			}));
			showToolBarProperty.bindBidirectional(showToolBarPersistedProperty);
			if (iconStageFullScreen != null)
			{
				mainContent.setFullScreenIcon(iconStageFullScreen);
			}
		}
	}

	public void setFileSystemView(PaneFileSystem paneFileSystem)
	{
		if (this.paneFileSystem == null)
		{
			this.paneFileSystem = paneFileSystem;
			final var fstv = paneFileSystem.getFileSystemTreeView();
			actions.actionClose.setOnAction(e -> fstv.clearSelection());
			actions.actionClose.disableProperty().bind(not(fstv.pathSelectedProperty()));
			getMainContent().setSelectedPathProperty(fstv.selectedPathProperty());
			titleProperty.bind(StringBindingAppTitle.create(fstv.selectedPathProperty(),
				ApplicationConfiguration.getState().developmentModeProperty()));
			tabPane.getTabs().add(paneFileSystem.getTabFstv());
		}
		else
		{
			throw new IllegalStateException("PaneFileSystem initialized twice");
		}
	}

	private MainContent getMainContent()
	{
		if (mainContent != null)
		{
			return mainContent;
		}
		else
		{
			throw new IllegalStateException("MainContent not initialized!");
		}
	}

	private @Nullable
	FileSystemTreeView getFileSystemTreeView()
	{
		return paneFileSystem != null ? paneFileSystem.getFileSystemTreeView() : null;
	}

	/// Performs initialization of the main window.
	///
	/// @param stage the primary stage
	///
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
		actions.actionExit.setOnAction(_ ->
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
		actions.actionOpen.setOnAction(actionOpen);
		actions.actionSysEnv.setOnAction(actionSysEnv);
		actions.actionInfoAbout.setOnAction(actionInfoAbout);
		final var scene = new Scene(rootPane);
		SceneStylesheetUtil.setStylesheet(scene);
		final var stageBounds = new StageBounds(stage, new StageBounds.PrefKeys(
			PREF_KEY_WIDTH, PREF_KEY_HEIGHT, PREF_KEY_X, PREF_KEY_Y, PREF_KEY_MAXIMIZE));
		stageBounds.initialize();
		logger.log(TRACE, () -> stageBounds.getLogMessage());
		stage.setOnCloseRequest(event -> onApplicationClose());
		if (state.launchType() == UNIT_TEST)
		{
			Platform.runLater(Platform::exit);
		}
		else
		{
			stage.setScene(scene);
			// HeaderBar
			final BooleanProperty showHeaderBarProperty = actions.actionShowHeaderBar.selectedProperty();
			showHeaderBarProperty.bindBidirectional(showHeaderBarPersistedProperty);
			rootPane.setTop(showHeaderBarProperty.get() ? createHeaderBar(stage) : menuBar);
			showHeaderBarProperty.addListener(onChange(_ ->
			{
				final var dialog = new Alert(AlertType.INFORMATION,
					"The change will take effect after restarting the application.",
					ButtonType.OK);
				dialog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
				dialog.showAndWait();
			}));
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
			actions.actionDevelopmentMode.setSelected(true);
			getMainContent().postVisibleInit();
		}
	}

	@SuppressWarnings("deprecation")
	private HeaderBar createHeaderBar(Stage stage)
	{
		stage.initStyle(StageStyle.EXTENDED);
		final var headerBar = new HeaderBar();
		menuBar.setBorder(Border.EMPTY);
		HeaderBar.setAlignment(menuBar, Pos.CENTER_LEFT);
		HeaderBar.setMargin(menuBar, new Insets(5));
		final var paneLeading = new BorderPane(menuBar);
		headerBar.setLeading(paneLeading);
		final Label title = new Label();
		title.textProperty().bind(titleProperty);
		headerBar.setCenter(title);
		final var icon = createResourceImage(FILE_RES_ICON_STAGE_2);
		if (icon != null)
		{
			final var imageView = new ImageView(icon);
			imageView.setPreserveRatio(true);
			menuBar.heightProperty().addListener(onChange(newValue ->
			{
				final double height = newValue.doubleValue();
				imageView.fitWidthProperty().set(height);
				imageView.fitHeightProperty().set(height);
			}));
			paneLeading.setLeft(imageView);
			BorderPane.setAlignment(imageView, Pos.CENTER);
			BorderPane.setMargin(imageView, new Insets(0, 5, 0, 5));
		}
		return headerBar;
	}

	private static @Nullable
	Image createResourceImage(String fileName)
	{
		Image icon = null;
		try (var inputStream = MainWindow.class.getResourceAsStream(fileName))
		{
			if (inputStream != null)
			{
				icon = new Image(inputStream);
			}
		}
		catch (IOException ex)
		{
			logger.log(TRACE, "Can't find stage icon »%s«".formatted(fileName), ex);
			icon = null;
		}
		if (icon == null)
		{
			logger.log(WARNING, "Can't find stage icon (ignoring…)");
		}
		return icon;
	}

	private void onApplicationClose()
	{
		logger.log(TRACE, "Closing main window …");
		if (paneFileSystem != null)
		{
			paneFileSystem.selectedPathPersistedProperty().unbind();
		}
		getMainContent().onApplicationClose();
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
}
