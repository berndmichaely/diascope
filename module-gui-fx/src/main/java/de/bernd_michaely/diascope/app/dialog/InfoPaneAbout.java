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
package de.bernd_michaely.diascope.app.dialog;

import de.bernd_michaely.diascope.app.control.HyperlinkNode;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * InfoPane for info about dialog.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class InfoPaneAbout implements InfoPane
{
	private static final String URL_APPLICATION = "https://bernd-michaely.de";

	@Override
	public String getTitle()
	{
		return "Info about";
	}

	private Separator createSeparator()
	{
		final Separator separator = new Separator(Orientation.HORIZONTAL);
		final double w = DEFAULT_INSET_SIZE * 8;
		final double h = DEFAULT_INSET_SIZE * 1.4;
		separator.setMinSize(w, h);
		separator.setPrefSize(w, h);
		separator.setMaxSize(w, h);
		return separator;
	}

	@Override
	public Region getDisplay()
	{
		final Font fontHeader = Font.font("", FontWeight.BOLD, DEFAULT_FONT_SIZE * 14 / 13);
		String subTitle = "Version based on JavaFX";
		final VBox vBox = new VBox(DEFAULT_FONT_SIZE * 8 / 13);
		final Label labelSubTitle = new Label(subTitle);
		labelSubTitle.setFont(fontHeader);
		vBox.getChildren().addAll(
			labelSubTitle,
			createSeparator(),
			new Label("An application for browsing"),
			new Label("– images –"),
			new Label("– metadata of images –"),
			new Label("and much more …"),
			createSeparator(),
			new Label("Application by"),
			HyperlinkNode.createNode(URL_APPLICATION));
		vBox.setAlignment(Pos.CENTER);
		vBox.setFillWidth(true);
		return vBox;
	}
}
