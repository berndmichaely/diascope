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

import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class representing a list cell for an image group list.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageListCell extends ListCell<ImageGroupDescriptor>
{
	private final ListView<ImageGroupDescriptor> listView;
	private final ImageView imageView;
	private final Label labelName;
	final BorderPane borderPane;

	ImageListCell(ListView<ImageGroupDescriptor> listView)
	{
		this.listView = listView;
		labelName = new Label();
		labelName.setBorder(Border.EMPTY);
		imageView = new ImageView();
		imageView.setPreserveRatio(true);
		borderPane = new BorderPane(imageView);
		borderPane.setBorder(Border.EMPTY);
		borderPane.setBottom(labelName);
	}

	@SuppressWarnings("argument")
	private void setThumbnail(@Nullable Image thumbnail)
	{
		imageView.setImage(thumbnail);
	}

	private class InsetsBinding extends DoubleBinding
	{
		@SuppressWarnings("method.invocation")
		private InsetsBinding()
		{
			bind(widthProperty(), listView.widthProperty(),
				insetsProperty(), listView.insetsProperty());
		}

		@Override
		protected double computeValue()
		{
			final var insets = getInsets();
			final var insetsList = listView.getInsets();
			return getWidth() - insets.getLeft() - insets.getRight() -
				insetsList.getLeft() - insetsList.getRight() - 1;
		}
	}

	@Override
	protected void updateItem(ImageGroupDescriptor item, boolean empty)
	{
		super.updateItem(item, empty);
		final DoubleBinding listCellSize = new InsetsBinding();
		imageView.fitWidthProperty().bind(listCellSize);
		imageView.fitHeightProperty().bind(listCellSize);
		labelName.prefWidthProperty().bind(listCellSize);
		labelName.maxWidthProperty().bind(listCellSize);
		setGraphic(borderPane);
		if (empty || item == null)
		{
			labelName.setText("");
			setThumbnail(null);
		}
		else
		{
			setThumbnail(item.getThumbnail());
			final String itemName = item.toString();
			labelName.setText(itemName);
			labelName.setTooltip(new Tooltip(itemName));
		}
	}
}
