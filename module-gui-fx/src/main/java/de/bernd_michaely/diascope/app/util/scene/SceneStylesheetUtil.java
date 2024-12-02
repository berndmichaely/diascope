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
package de.bernd_michaely.diascope.app.util.scene;

import java.lang.System.Logger;
import java.net.URL;
import javafx.scene.Scene;

import static java.lang.System.Logger.Level.*;

/**
 * Utility to set a global stylesheet.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SceneStylesheetUtil
{
	private static final Logger logger = System.getLogger(SceneStylesheetUtil.class.getName());
	private static final String STYLE_SHEET = "dark.css";

	/**
	 * Set global stylesheet for the given scene.
	 *
	 * @param scene the given scene
	 * @return true, iff successful
	 */
	public static boolean setStylesheet(Scene scene)
	{
		final URL resource = SceneStylesheetUtil.class.getResource(STYLE_SHEET);
		if (resource != null)
		{
			logger.log(TRACE, "Add stylesheet resource: »" + resource + "«");
			scene.getStylesheets().add(resource.toExternalForm());
			return true;
		}
		else
		{
			logger.log(WARNING, "Stylesheet resource »" + STYLE_SHEET + "« not found, ignoring…");
			return false;
		}
	}
}
