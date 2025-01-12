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
package de.bernd_michaely.diascope.app.icons;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import javafx.scene.image.Image;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.System.Logger.Level.*;

/**
 * Utility class to access icons in resources. Implementation using batik.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public enum Icons
{
	// File:
	FileOpen,
	// Edit:
	SelectAll, SelectInvert, SelectNone,
	// View:
	ShowThumbs,
	ViewFullscreen, ViewFullscreenPane, ShowSidePane,
	ZoomFitWindow, ZoomFillWindow, Zoom100,
	MirrorX, MirrorY,
	ViewRotateByExif, ViewDisplayMetaData,
	// Navigation
	ViewShowFirst, ViewShowPrev, ViewShowNext, ViewShowLast,
	// Layer
	LayerAdd, LayerRemove;

	private static final Logger logger = System.getLogger(Icons.class.getName());

	public @Nullable
	Image getIconImage()
	{
		final String resourceName = "action" + name() + ".png";
		try (final InputStream resourceStream = getClass().getResourceAsStream(resourceName))
		{
			if (resourceStream != null)
			{
				return new Image(resourceStream);
			}
			else
			{
				logger.log(WARNING, "Resource not found: " + resourceName);
				return null;
			}
		}
		catch (IOException ex)
		{
			logger.log(WARNING, "" + ex);
			return null;
		}
	}
}
