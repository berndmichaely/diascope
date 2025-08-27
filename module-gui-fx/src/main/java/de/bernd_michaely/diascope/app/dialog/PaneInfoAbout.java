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

import de.bernd_michaely.diascope.app.control.TextFactory;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.Nullable;

import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;

/**
 * Class to construct the content of the info about dialog.
 *
 * @author Bernd Michaely
 */
public class PaneInfoAbout implements InfoPane
{
	private final BorderPane pane;
	private final String title;

	public PaneInfoAbout(String title, @Nullable Image iconStage)
	{
		this.title = title;
//		final InfoPaneAbout infoPaneAbout = new InfoPaneAbout();
		final InfoPaneSystemInfo infoPaneSystemInfo = new InfoPaneSystemInfo();
		final TabPane tabPane = new TabPane(
			//			new Tab(infoPaneAbout.getTitle(), infoPaneAbout.getDisplay()),
			new Tab(infoPaneSystemInfo.getTitle(), infoPaneSystemInfo.getDisplay()));
		tabPane.setTabClosingPolicy(UNAVAILABLE);
		this.pane = new BorderPane(tabPane);
		final double sizeText = (DEFAULT_FONT_SIZE * 32 / 13);
		final TextFactory textFactory = new TextFactory(Color.STEELBLUE.brighter(), sizeText);
		final Node textNode = textFactory.createTextNode(this.title);
		if (iconStage != null)
		{
			final var imageView = new ImageView(iconStage);
			final double sizeImage = sizeText * 1.2;
			imageView.setFitWidth(sizeImage);
			imageView.setFitHeight(sizeImage);
			final var paneTop = new HBox(sizeImage * 0.5, imageView, textNode);
			this.pane.setTop(paneTop);
		}
		else
		{
			this.pane.setTop(textNode);
		}
		final double vgap = DEFAULT_FONT_SIZE * 8 / 13;
		final double hgap = DEFAULT_FONT_SIZE * 8;
		BorderPane.setMargin(pane.getTop(), new Insets(vgap, hgap, vgap, hgap));
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public Region getDisplay()
	{
		return this.pane;
	}
}
