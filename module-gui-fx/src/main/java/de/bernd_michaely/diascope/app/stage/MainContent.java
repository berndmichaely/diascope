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

import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.PreferencesUtil;
import de.bernd_michaely.diascope.app.icons.Icons;
import de.bernd_michaely.diascope.app.image.ImageDescriptor;
import de.bernd_michaely.diascope.app.image.MultiImageView;
import de.bernd_michaely.diascope.app.image.MultiImageView.ZoomMode;
import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader;
import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.TaskParameters;
import de.bernd_michaely.diascope.app.util.scene.SceneStylesheetUtil;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.PREF_KEY_IMAGE_SPLIT_POS;
import static de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.RequestType.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.when;
import static javafx.geometry.Pos.*;

/**
 * Content area of main window.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class MainContent
{
	private static final Logger logger = System.getLogger(MainContent.class.getName());
	private static final int INDEX_NO_SELECTION = -1;
	private static final String MSG_FMT_LOADING_IMG = "%s ← loading…";
	private static final Preferences preferences = PreferencesUtil.nodeForPackage(MainWindow.class);
	private final BorderPane borderPane;
	private final SplitPane splitPane;
	private final MultiImageView multiImageView;
	private final BorderPane imageContainer;
	private final BorderPane statusLine;
	private final Label labelStatus;
	private final Label labelStatusIndex;
	private final ProgressControl progressControl;
	private final ListView<ImageGroupDescriptor> listView;
	private final ReadOnlyListWrapper<ImageGroupDescriptor> listViewProperty;
	private final ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty;
	private final ImageDirectoryReader imageDirectoryReader;
	private final ChangeListener<@Nullable Path> pathChangeListener;
	private final ImageLoader imageLoader = new ImageLoader();
	private final Cursor cursorDefault;
	private @MonotonicNonNull Paint paintDefaultText;
	private int indexSelectedPrevious = INDEX_NO_SELECTION;
	private @Nullable Stage stageFullScreen;
	private @Nullable Region detachedComponent;
	private final ToolBar toolBarImage;
	private final BooleanProperty toolBarFullscreenProperty;

	MainContent(ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty,
		ReadOnlyBooleanProperty propertyShowStatusLine)
	{
		this.selectedPathProperty = selectedPathProperty;
		this.listView = new ListView<>();
		this.listViewProperty = new ReadOnlyListWrapper<>(listView.getItems());
		this.listView.setCellFactory(ImageListCell::new);
		this.labelStatus = new Label();
		this.statusLine = new BorderPane(labelStatus);
		this.statusLine.setPrefHeight(new ProgressBar(0).getHeight());
		BorderPane.setAlignment(labelStatus, CENTER_LEFT);
		this.labelStatusIndex = new Label();
		this.statusLine.setRight(labelStatusIndex);
		this.multiImageView = new MultiImageView();
		multiImageView.addLayer();
		multiImageView.getRegion().setOnScroll((ScrollEvent event) ->
		{
			final double deltaY = event.getDeltaY();
			final boolean forward = deltaY < 0.0;
			final boolean backward = deltaY > 0.0;
			if (forward || backward)
			{
				final var items = listView.getItems();
				final int n = items.size();
				if (n > 0)
				{
					final var selectionModel = listView.getSelectionModel();
					final int selectedIndex = selectionModel.getSelectedIndex();
					if (forward && selectedIndex < n - 1)
					{
						selectionModel.selectNext();
						if (selectedIndex < n - 2) // workaround
						{
							listView.scrollTo(selectionModel.getSelectedIndex());
						}
					}
					else if (backward && selectedIndex > 0)
					{
						selectionModel.selectPrevious();
						listView.scrollTo(selectionModel.getSelectedIndex());
					}
				}
			}
		});
		this.imageContainer = new BorderPane(multiImageView.getRegion());
		this.splitPane = new SplitPane(this.imageContainer, this.listView);
		SplitPane.setResizableWithParent(this.listView, false);
		this.borderPane = new BorderPane();
		// ToolBar:
		final var buttonLayerAdd = new Button();
		final Image iconLayerAdd = Icons.LayerAdd.getIconImage();
		if (iconLayerAdd != null)
		{
			buttonLayerAdd.setGraphic(new ImageView(iconLayerAdd));
		}
		else
		{
			buttonLayerAdd.setText("+");
		}
		buttonLayerAdd.setTooltip(new Tooltip("Add a new image view"));
		buttonLayerAdd.setOnAction(event -> multiImageView.addLayer());
		final var buttonLayerRemove = new Button();
		final Image iconLayerRemove = Icons.LayerRemove.getIconImage();
		if (iconLayerRemove != null)
		{
			buttonLayerRemove.setGraphic(new ImageView(iconLayerRemove));
		}
		else
		{
			buttonLayerRemove.setText("-");
		}
		buttonLayerRemove.setTooltip(new Tooltip("Remove selected image view"));
		buttonLayerRemove.disableProperty().bind(multiImageView.numberOfLayersProperty().lessThan(2));
		buttonLayerRemove.setOnAction(event -> multiImageView.removeLayer());
		final var buttonZoomFitWindow = new Button();
		final Image iconZoomFitWindow = Icons.ZoomFitWindow.getIconImage();
		if (iconZoomFitWindow != null)
		{
			buttonZoomFitWindow.setGraphic(new ImageView(iconZoomFitWindow));
		}
		else
		{
			buttonZoomFitWindow.setText("Fit");
		}
		buttonZoomFitWindow.setTooltip(new Tooltip("Zoom image to fit window"));
		buttonZoomFitWindow.setOnAction(event -> multiImageView.zoomModeProperty().set(ZoomMode.FIT));
		final var buttonZoomFillWindow = new Button();
		final Image iconZoomFillWindow = Icons.ZoomFillWindow.getIconImage();
		if (iconZoomFillWindow != null)
		{
			buttonZoomFillWindow.setGraphic(new ImageView(iconZoomFillWindow));
		}
		else
		{
			buttonZoomFillWindow.setText("Fill");
		}
		buttonZoomFillWindow.setTooltip(new Tooltip("Zoom image to fill window"));
		buttonZoomFillWindow.setOnAction(event -> multiImageView.zoomModeProperty().set(ZoomMode.FILL));
		final var buttonZoom100 = new Button();
		final Image iconZoom100 = Icons.Zoom100.getIconImage();
		if (iconZoom100 != null)
		{
			buttonZoom100.setGraphic(new ImageView(iconZoom100));
		}
		else
		{
			buttonZoom100.setText("100%");
		}
		buttonZoom100.setTooltip(new Tooltip("Zoom image to 100%"));
		final var sliderZoom = new Slider(0.01, 4, 1);
		buttonZoom100.setOnAction(event ->
		{
			multiImageView.zoomModeProperty().set(ZoomMode.FIXED);
			sliderZoom.setValue(1.0);
		});
		sliderZoom.setTooltip(new Tooltip("Zoom factor"));
		sliderZoom.valueProperty().addListener(onChange(value ->
		{
			multiImageView.zoomModeProperty().set(ZoomMode.FIXED);
			multiImageView.zoomFixedProperty().set(value.doubleValue());
		}));
		sliderZoom.setTooltip(new Tooltip("Set zoom factor"));
		sliderZoom.setOnMouseClicked(event ->
		{
			if (event.getClickCount() == 2)
			{
				multiImageView.zoomModeProperty().set(ZoomMode.FIXED);
				multiImageView.zoomFixedProperty().set(sliderZoom.getValue());
			}
		});
		final var labelZoom = new Label("100.0%");
		labelZoom.setTooltip(new Tooltip("Current zoom factor"));
		labelZoom.textProperty().bind(when(multiImageView.isSingleSelectedProperty())
			.then(multiImageView.zoomFactorProperty().multiply(100.0).asString("%.1f%% "))
			.otherwise(""));
		final var labelZoomWidth = new Label("8888.8% ");
		labelZoomWidth.setVisible(false);
		final var stackPaneZoom = new StackPane(labelZoomWidth, labelZoom);
		stackPaneZoom.setAlignment(CENTER_RIGHT);
//		stackPaneZoom.setBackground(Background.fill(Color.ROSYBROWN));
		final var sliderRotation = new Slider(0, 360, 0);
		sliderRotation.setTooltip(new Tooltip("Set image rotation"));
		final var labelRotation = new Label("0°");
		labelRotation.setTooltip(new Tooltip("Current image rotation"));
		labelRotation.textProperty().bind(when(multiImageView.isSingleSelectedProperty())
			.then(multiImageView.rotateProperty().asString("%.0f° "))
			.otherwise(""));
		final var labelRotationWidth = new Label(" 360° ");
		labelRotationWidth.setVisible(false);
		final var stackPaneRotation = new StackPane(labelRotationWidth, labelRotation);
		stackPaneRotation.setAlignment(CENTER_RIGHT);
//		stackPaneRotation.setBackground(Background.fill(Color.DARKKHAKI));
		final var buttonMirrorX = new ToggleButton();
		final Image iconMirrorX = Icons.MirrorX.getIconImage();
		if (iconMirrorX != null)
		{
			buttonMirrorX.setGraphic(new ImageView(iconMirrorX));
		}
		else
		{
			buttonMirrorX.setText("Mirr. horiz.");
		}
		buttonMirrorX.setTooltip(new Tooltip("Mirror image horizontally"));
		multiImageView.mirrorXProperty().bind(buttonMirrorX.selectedProperty());
		final var buttonMirrorY = new ToggleButton();
		final Image iconMirrorY = Icons.MirrorY.getIconImage();
		if (iconMirrorY != null)
		{
			buttonMirrorY.setGraphic(new ImageView(iconMirrorY));
		}
		else
		{
			buttonMirrorY.setText("Mirr. vert.");
		}
		buttonMirrorY.setTooltip(new Tooltip("Mirror image vertically"));
		multiImageView.mirrorYProperty().bind(buttonMirrorY.selectedProperty());
		this.toolBarImage = new ToolBar(
			buttonLayerAdd, buttonLayerRemove,
			buttonZoomFitWindow, buttonZoomFillWindow, buttonZoom100,
			sliderZoom, stackPaneZoom, sliderRotation, stackPaneRotation,
			buttonMirrorX, buttonMirrorY);
		toolBarImage.disableProperty().bind(not(multiImageView.isSingleSelectedProperty()));
		this.toolBarFullscreenProperty = new SimpleBooleanProperty();
		this.multiImageView.rotateProperty().bind(sliderRotation.valueProperty());
		this.borderPane.setCenter(this.splitPane);
		this.borderPane.setTop(toolBarImage);
		this.progressControl = new ProgressControl(this.statusLine);
		final var statusLines = new VBox();
		this.borderPane.setBottom(statusLines);
		final var statusLineDevelopment = new Label("Command line arguments passed: " +
			ApplicationConfiguration.getState().commandLineArguments().toString());
		ApplicationConfiguration.getState().developmentModeProperty().addListener(onChange(newValue ->
		{
			if (newValue)
			{
				statusLines.getChildren().add(statusLineDevelopment);
			}
			else
			{
				statusLines.getChildren().remove(statusLineDevelopment);
			}
		}));
		this.imageDirectoryReader = new ImageDirectoryReader(listView.getItems(), this.progressControl);
		this.pathChangeListener = onChange(imageDirectoryReader::accept);
		selectedPathProperty.addListener(this.pathChangeListener);
		propertyShowStatusLine.addListener(onChange(newValue ->
		{
			if (newValue)
			{
				statusLines.getChildren().add(0, statusLine);
			}
			else
			{
				statusLines.getChildren().remove(statusLine);
			}
		}));
		cursorDefault = borderPane.getCursor();
	}

	private void updateSelectedIndex(Number selectedIndex)
	{
		final int index = selectedIndex.intValue();
		final var items = listView.getItems();
		final int n = items.size();
		if (index >= 0)
		{
			labelStatusIndex.setText("[" + (index + 1) + "/" + n + "]");
			borderPane.setCursor(Cursor.WAIT);
			final var selectedItem = items.get(selectedIndex.intValue());
			final Path pathSelected = selectedItem.getPath();
			labelStatus.setText(MSG_FMT_LOADING_IMG.formatted(pathSelected));
			labelStatus.setTextFill(Color.SKYBLUE);
			// read ahead:
			final boolean backward = index < this.indexSelectedPrevious;
			final boolean forward = index > this.indexSelectedPrevious;
			ImageGroupDescriptor igdReadAhead = null;
			final int maxIndex = n - 1;
			if (index < maxIndex && (forward || backward && index == 0))
			{
				igdReadAhead = items.get(index + 1);
			}
			else if (index > 0 && (backward || forward && index == maxIndex))
			{
				igdReadAhead = items.get(index - 1);
			}
			this.indexSelectedPrevious = index;
			imageLoader.accept(new TaskParameters(pathSelected, IMMEDIATE));
			logger.log(TRACE, "Request immediate of »%s«".formatted(pathSelected));
			final Path pathReadAhead = igdReadAhead != null ? igdReadAhead.getPath() : null;
			if (pathReadAhead != null)
			{
				logger.log(TRACE, "Request read ahead of »%s«".formatted(pathReadAhead));
				imageLoader.accept(new TaskParameters(pathReadAhead, READ_AHEAD));
			}
		}
		else
		{
			labelStatusIndex.setText("[–/–]");
			labelStatus.setText("");
			imageLoader.accept(new TaskParameters());
		}
	}

	/**
	 * Display the given image.
	 *
	 * @param taskResult the given image, may be null to clear the display
	 */
	private void setImageDescriptor(ImageLoader.TaskResult taskResult)
	{
		final var image = taskResult.image();
		final var path = taskResult.path();
		multiImageView.setImageDescriptor(image != null && path != null ?
			new ImageDescriptor(image, path) : null);
	}

	private Paint getDefaultTextPaint()
	{
		if (paintDefaultText == null)
		{
			paintDefaultText = labelStatus.getTextFill();
		}
		return paintDefaultText;
	}

	ReadOnlyIntegerProperty selectedIndexProperty()
	{
		return listView.getSelectionModel().selectedIndexProperty();
	}

	ReadOnlyListProperty<ImageGroupDescriptor> getListViewProperty()
	{
		return listViewProperty.getReadOnlyProperty();
	}

	void selectFirst()
	{
		listView.getSelectionModel().selectFirst();
	}

	void selectPrevious()
	{
		listView.getSelectionModel().selectPrevious();
	}

	void selectselectNext()
	{
		listView.getSelectionModel().selectNext();
	}

	void selectselectLast()
	{
		listView.getSelectionModel().selectLast();
	}

	void postVisibleInit()
	{
		getDefaultTextPaint();
		this.imageLoader.setOnResult(taskResult -> Platform.runLater(() ->
		{
			final Path path = taskResult.path();
			final String strPath = path != null ? path.toString() : "";
			final boolean finished = taskResult.state();
			if (finished)
			{
				labelStatus.setTextFill(getDefaultTextPaint());
				labelStatus.setText(strPath);
				getRegion().setCursor(cursorDefault);
			}
			else
			{
				labelStatus.setText(MSG_FMT_LOADING_IMG.formatted(strPath));
				labelStatus.setTextFill(Color.SKYBLUE);
			}
			setImageDescriptor(taskResult);
		}));
		this.listView.getSelectionModel().selectedIndexProperty()
			.addListener(onChange(this::updateSelectedIndex));
		this.progressControl.setOnProgressZero(() ->
		{
			final int n = listView.getItems().size();
			if (n > 1)
			{
				listView.getSelectionModel().selectFirst(); // seems not to work if n==1
			}
			else if (n == 1) // workaround
			{
				updateSelectedIndex(0);
			}
			else
			{
				updateSelectedIndex(INDEX_NO_SELECTION);
			}
		});
		final var dividers = this.splitPane.getDividers();
		if (!dividers.isEmpty())
		{
			final var divider = dividers.get(0);
			divider.setPosition(preferences.getDouble(PREF_KEY_IMAGE_SPLIT_POS.getKey(), 200));
			divider.positionProperty().addListener(onChange(position ->
				preferences.putDouble(PREF_KEY_IMAGE_SPLIT_POS.getKey(), position.doubleValue())));
		}
		else
		{
			logger.log(WARNING, "No image split pane available");
		}
		multiImageView.getRegion().setOnMouseClicked(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
			{
				if (isFullScreen())
				{
					closeFullScreen();
				}
				else
				{
					setFullScreen(event.isShiftDown());
				}
				event.consume();
			}
		});
	}

	void onApplicationClose()
	{
		try (imageDirectoryReader)
		{
			selectedPathProperty.removeListener(pathChangeListener);
		}
	}

	/**
	 * Returns the main component to be included in surrounding environment.
	 *
	 * @return the main component
	 */
	Region getRegion()
	{
		return borderPane;
	}

	/**
	 * Detach the fullscreen component.
	 *
	 * @param fullPane true for detaching the full image pane, false for image
	 *                 only
	 * @return the detached component
	 */
	private Region detachFullscreenComponent(boolean fullPane)
	{
		final Region result;
		if (detachedComponent == null)
		{
			if (fullPane)
			{
				if (borderPane.getChildren().remove(splitPane))
				{
					result = splitPane;
				}
				else
				{
					throw new IllegalStateException("Error removing splitPane from borderPane");
				}
			}
			else
			{
				if (imageContainer.getChildren().remove(multiImageView.getRegion()))
				{
					result = multiImageView.getRegion();
				}
				else
				{
					throw new IllegalStateException("Error removing paneImage from splitPane");
				}
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

	private @Nullable ChangeListener<Boolean> toolBarFullscreenListener;

	private boolean isFullScreen()
	{
		return this.stageFullScreen != null;
	}

	private void closeFullScreen()
	{
		final Stage stFullScreen = stageFullScreen;
		if (stFullScreen != null)
		{
			final ChangeListener<Boolean> listener = toolBarFullscreenListener;
			if (listener != null)
			{
				toolBarFullscreenProperty.set(false);
				toolBarFullscreenProperty.removeListener(listener);
			}
			stFullScreen.close();
			reAttachFullscreenComponent();
			multiImageView.scrollBarsEnabledProperty().set(false);
			stageFullScreen = null;
		}
	}

	void setFullScreen(boolean fullPane)
	{
		if (!isFullScreen())
		{
			final var stage = new Stage();
			stage.setOnCloseRequest(_ -> closeFullScreen());
			stageFullScreen = stage;
			multiImageView.scrollBarsEnabledProperty().set(false);
			final var root = detachFullscreenComponent(fullPane);
			final var rootPane = new BorderPane(root);
			final var scene = new Scene(rootPane);
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
			scene.addEventFilter(KeyEvent.KEY_PRESSED, event ->
			{
				switch (event.getCode())
				{
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
