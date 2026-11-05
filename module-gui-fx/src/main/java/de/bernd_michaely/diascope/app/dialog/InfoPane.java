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

import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Interface to describe a single info pane usable as a Tab or dialog window
 * content.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
sealed interface InfoPane permits InfoPaneSystemInfo, PaneInfoAbout
{
	double DEFAULT_FONT_SIZE = Font.getDefault().getSize();
	double DEFAULT_INSET_SIZE = DEFAULT_FONT_SIZE * 15 / 13;
	Insets DEFAULT_INSETS = new Insets(DEFAULT_FONT_SIZE * 3);
	String STR_URL_APPLICATION = "https://github.com/berndmichaely/diascope";
	String STR_URL_DEVELOPER = "https://bernd-michaely.de";
	String MSG_UNKNOWN = "<unknown>";
	Color COLOR_HEADER = Color.STEELBLUE.brighter();
	Color COLOR_HYPERLINK = Color.LIGHTSKYBLUE;

//	private static final Color COLOR_YES = Color.MEDIUMSPRINGGREEN;
//	private static final Color COLOR_YES = Color.PALEGREEN;
//	private static final Color COLOR_YES = Color.LAWNGREEN;
//	private static final Color COLOR_YES = Color.LIME;
//	private static final Color COLOR_YES = Color.SPRINGGREEN;
	Color COLOR_YES = Color.LIGHTGREEN;

//	private static final Color COLOR_NO = Color.LIGHTCORAL;
//	private static final Color COLOR_NO = Color.ORANGERED;
//	private static final Color COLOR_NO = Color.TOMATO;
	Color COLOR_NO = Color.DARKORANGE;

	/**
	 * Returns a title to display as a Tab or Window title.
	 *
	 * @return a title
	 */
	String getTitle();

	/**
	 * Returns the containing node of the display.
	 *
	 * @return the display node
	 */
	Region getDisplay();
}
