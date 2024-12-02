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
import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader;
import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.TaskParameters;
import de.bernd_michaely.diascope.app.util.scene.SceneStylesheetUtil;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
	private final BorderPane borderPane = new BorderPane();
	private final SplitPane splitPane;
	private final ImageView imageView = new ImageView();
	private final BorderPane paneImage = new BorderPane(imageView);
	private final BorderPane imageContainer = new BorderPane(paneImage);
	private final BorderPane statusLine;
	private final Label labelStatus = new Label();
	private final Label labelStatusIndex = new Label();
	private final ProgressControl progressControl;
	private final ListView<ImageGroupDescriptor> listView;
	private @MonotonicNonNull ListBinding<ImageGroupDescriptor> listBinding;
	private final ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty;
	private final ImageDirectoryReader imageDirectoryReader;
	private final ChangeListener<@Nullable Path> pathChangeListener;
	private final ImageLoader imageLoader = new ImageLoader();
	private final Cursor cursorDefault;
	private @MonotonicNonNull Paint paintDefaultText;
	private int indexSelectedPrevious = INDEX_NO_SELECTION;
	private @Nullable Stage stageFullscreen;
	private @Nullable Region detachedComponent;

	MainContent(ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty,
		ReadOnlyBooleanProperty propertyShowStatusLine)
	{
		this.selectedPathProperty = selectedPathProperty;
		this.listView = new ListView<>();
		this.listView.setCellFactory(ImageListCell::new);
		this.statusLine = new BorderPane(labelStatus);
		this.statusLine.setPrefHeight(new ProgressBar(0).getHeight());
		BorderPane.setAlignment(labelStatus, CENTER_LEFT);
		this.statusLine.setRight(labelStatusIndex);
		paneImage.setBackground(Background.fill(Color.BLACK));
		paneImage.setMinSize(0, 0);
		this.imageView.fitWidthProperty().bind(paneImage.widthProperty());
		this.imageView.fitHeightProperty().bind(paneImage.heightProperty());
		paneImage.setOnScroll((ScrollEvent event) ->
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
		this.imageView.setPreserveRatio(true);
		this.splitPane = new SplitPane(this.imageContainer, this.listView);
		SplitPane.setResizableWithParent(this.listView, false);
		this.borderPane.setCenter(this.splitPane);
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
	 * @param image the given image, may be null to clear the display
	 */
	@SuppressWarnings("argument")
	private void setImage(@Nullable Image image)
	{
		imageView.setImage(image);
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

	@SuppressWarnings("method.invocation")
	ListBinding<ImageGroupDescriptor> getListBinding()
	{
		if (listBinding == null)
		{
			listBinding = new ListBinding<ImageGroupDescriptor>()
			{
				{
					bind(listView.getItems());
				}

				@Override
				protected ObservableList<ImageGroupDescriptor> computeValue()
				{
					return listView.getItems();
				}
			};
		}
		return listBinding;
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
			setImage(taskResult.image());
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
		this.paneImage.setOnMouseClicked(event ->
		{
			if (event.getClickCount() > 1)
			{
				if (isFullscreen())
				{
					closeFullscreen();
				}
				else
				{
					setFullscreen(event.isShiftDown());
				}
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
				if (imageContainer.getChildren().remove(paneImage))
				{
					result = paneImage;
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
			else if (root == paneImage)
			{
				imageContainer.setCenter(paneImage);
			}
			detachedComponent = null;
		}
	}

	boolean isFullscreen()
	{
		return this.stageFullscreen != null;
	}

	private void closeFullscreen()
	{
		if (stageFullscreen != null)
		{
			stageFullscreen.close();
			reAttachFullscreenComponent();
			stageFullscreen = null;
		}
	}

	void setFullscreen(boolean fullPane)
	{
		if (!isFullscreen())
		{
			final var stage = new Stage();
			stage.setOnCloseRequest(_ -> closeFullscreen());
			stageFullscreen = stage;
			final var root = detachFullscreenComponent(fullPane);
			final var rootPane = new BorderPane(root);
			final var scene = new Scene(rootPane);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, event ->
			{
				if (event.getCode().equals(KeyCode.ESCAPE) || event.getCode().equals(KeyCode.F11))
				{
					closeFullscreen();
					event.consume();
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
