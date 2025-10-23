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
import de.bernd_michaely.diascope.app.image.ImageDescriptor;
import de.bernd_michaely.diascope.app.image.MultiImageView;
import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader;
import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.TaskParameters;
import de.bernd_michaely.diascope.app.util.action.CheckedAction;
import java.lang.System.Logger;
import java.nio.file.Path;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.RequestType.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static java.lang.System.Logger.Level.*;
import static javafx.geometry.Pos.*;

/// Content area of main window.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class MainContent
{
	private static final Logger logger = System.getLogger(MainContent.class.getName());
	private static final int INDEX_NO_SELECTION = -1;
	private static final String MSG_FMT_LOADING_IMG = "%s ← loading…";
	private static final Color COLOR_LABEL_LOADING_IMG = Color.SKYBLUE;
	private final BorderPane outerPane;
	private final MultiImageView multiImageView;
	private final BorderPane statusLine;
	private final Label labelStatus;
	private final Label labelStatusIndex;
	private final ProgressControl progressControl;
	private final ListView<ImageGroupDescriptor> listView;
	private final ReadOnlyListWrapper<ImageGroupDescriptor> listViewProperty;
	private @Nullable ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty;
	private final ImageDirectoryReader imageDirectoryReader;
	private final ChangeListener<@Nullable Path> pathChangeListener;
	private final ImageLoader imageLoader = new ImageLoader();
	private final Cursor cursorDefault;
	private @MonotonicNonNull Paint paintDefaultText;
	private int indexSelectedPrevious = INDEX_NO_SELECTION;
	private final BooleanProperty showStatusLineProperty;
	private final MainContentComponents components;

	public MainContent()
	{
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
		this.progressControl = new ProgressControl(this.statusLine);
		final var statusLines = new VBox();
		this.components = new MainContentComponents(multiImageView, listView);
		this.outerPane = new BorderPane(components.getRegion());
		this.outerPane.setBottom(statusLines);
		final var state = ApplicationConfiguration.getState();
		final var statusLineDevelopment = new Label(
			"Command line arguments passed: " + state.commandLineArguments().toString());
		state.developmentModeProperty().addListener(onChange(newValue ->
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
		this.showStatusLineProperty = new SimpleBooleanProperty();
		this.showStatusLineProperty.addListener(onChange(newValue ->
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
		cursorDefault = outerPane.getCursor();
	}

	void bindShowStatusLineProperty(ReadOnlyBooleanProperty showStatusLinePersistedProperty)
	{
		this.showStatusLineProperty.bind(showStatusLinePersistedProperty);
	}

	void setSelectedPathProperty(ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty)
	{
		this.selectedPathProperty = selectedPathProperty;
		selectedPathProperty.addListener(pathChangeListener);
	}

	private void removeSelectedPathProperty()
	{
		if (selectedPathProperty != null)
		{
			selectedPathProperty.removeListener(pathChangeListener);
			selectedPathProperty = null;
		}
	}

	private void updateSelectedIndex(Number selectedIndex)
	{
		final int index = selectedIndex.intValue();
		final var items = listView.getItems();
		final int n = items.size();
		if (index >= 0)
		{
			labelStatusIndex.setText("[" + (index + 1) + "/" + n + "]");
			getRegion().setCursor(Cursor.WAIT);
			final var selectedItem = items.get(selectedIndex.intValue());
			final Path pathSelected = selectedItem.getPath();
			getDefaultTextPaint();
			labelStatus.setText(MSG_FMT_LOADING_IMG.formatted(pathSelected));
			labelStatus.setTextFill(COLOR_LABEL_LOADING_IMG);
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
				labelStatus.setTextFill(COLOR_LABEL_LOADING_IMG);
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
		multiImageView.getRegion().setOnMouseClicked(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 &&
				(!multiImageView.isMultiLayerMode() || event.isShiftDown()))
			{
				getActionFullScreen().toggle();
				event.consume();
			}
		});
	}

	void setFullScreenIcon(Image iconStage)
	{
		components.setFullScreenIcon(iconStage);
	}

	void onApplicationClose()
	{
		try (imageDirectoryReader)
		{
			removeSelectedPathProperty();
		}
	}

	/**
	 * Returns the main component to be included in surrounding environment.
	 *
	 * @return the main component
	 */
	Region getRegion()
	{
		return outerPane;
	}

	CheckedAction getActionFullScreen()
	{
		return components.getActionFullScreen();
	}
}
