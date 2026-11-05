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

import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.control.TextFactory;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.dialog.InfoPane.*;
import static de.bernd_michaely.diascope.app.dialog.InfoPaneSystemInfo.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;

/**
 * Class to construct the content of the info about dialog.
 *
 * @author Bernd Michaely
 */
public final class PaneInfoAbout implements InfoPane
{
	private final String title;
	private final Region textNode;
	private final Region contentNode;
	private final ReadOnlyBooleanWrapper readOnlyBooleanWrapper = new ReadOnlyBooleanWrapper(false);

	public PaneInfoAbout(String title, @Nullable Image iconStage)
	{
		this.title = title;
		final var infoPaneSystemInfo = new InfoPaneSystemInfo();
		final var tabSystemInfo = new Tab(infoPaneSystemInfo.getTitle(), infoPaneSystemInfo.getDisplay());
		final var tabDevelopmentInfo = new Tab("Development Info", createTable(getDevelopmentInfo()));
		final var tabPane = new TabPane(tabSystemInfo);
		readOnlyBooleanWrapper.getReadOnlyProperty().addListener(onChange(visible ->
		{
			final var tabs = tabPane.getTabs();
			if (visible)
			{
				tabs.addLast(tabDevelopmentInfo);
			}
			else
			{
				tabs.remove(tabDevelopmentInfo);
			}
		}));
		readOnlyBooleanWrapper.bind(ApplicationConfiguration.getState().developmentModeProperty());
		tabPane.setTabClosingPolicy(UNAVAILABLE);
//		final var pane = new BorderPane(tabPane);
		final var pane = new BorderPane(tabPane);
		BorderPane.setMargin(tabPane, DEFAULT_INSETS);
		this.contentNode = pane;
		final double sizeText = (DEFAULT_FONT_SIZE * 32 / 13);
		final var textFactory = new TextFactory(COLOR_HEADER, sizeText);
		this.textNode = textFactory.createTextNode(this.title);
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public Region getDisplay()
	{
		return this.contentNode;
	}

	public Region getTextNode()
	{
		return textNode;
	}
}
