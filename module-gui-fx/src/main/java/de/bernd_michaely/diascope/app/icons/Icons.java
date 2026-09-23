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

import io.brunoborges.jairosvg.JairoSVG;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.Math.round;
import static java.lang.System.Logger.Level.*;

/**
 * Utility class to access icons in resources. Implementation using batik.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public enum Icons
{
	// File:
	FileOpen, FileClose, FileExit,
	// Edit:
	SelectAll, SelectInvert, SelectNone,
	// View:
	ShowThumbs,
	ViewFullscreen, // ViewFullscreenPane,
	ShowSidePane, ShowToolBar, ShowScrollBars,
	ZoomFitWindow, ZoomFillWindow, Zoom100,
	MirrorX, MirrorY,
	ViewRotateByExif, ViewDisplayMetaData,
	// Navigation
	ViewShowFirst, ViewShowPrev, ViewShowNext, ViewShowLast,
	// Layer
	LayerAdd, LayerRemove, ShowDividers,
	// Multi image modes
	ModeGrid, ModeSplit, ModeSpot,
	// Image context menu
	ResetControls;

	private static final Logger logger = System.getLogger(Icons.class.getName());
	private static final String ICONS_RESOURCE_PACKAGE_BASE = "/de/bernd_michaely/diascope/app/icons";

	private static final int scaledWidth, scaledHeight;
	private static final int scaledWidthSmall, scaledHeightSmall;
	private static final int BASE_SIZE = 24;
//	private static final float FACTOR_SIZE_SMALL = 2f / 3f;
	private static final float FACTOR_SIZE_SMALL = 0.75f;

	static
	{
		final var screen = Screen.getPrimary();
		scaledWidth = (int) round(BASE_SIZE * screen.getOutputScaleX());
		scaledHeight = (int) round(BASE_SIZE * screen.getOutputScaleY());
		scaledWidthSmall = round(scaledWidth * FACTOR_SIZE_SMALL);
		scaledHeightSmall = round(scaledHeight * FACTOR_SIZE_SMALL);
	}

	public static int getScaledWidth()
	{
		return scaledWidth;
	}

	public static int getScaledHeight()
	{
		return scaledHeight;
	}

	public static int getScaledWidthSmall()
	{
		return scaledWidthSmall;
	}

	public static int getScaledHeightSmall()
	{
		return scaledHeightSmall;
	}

	private @Nullable
	Image readImage(final InputStream stream, boolean small)
	{
		final double width = small ? scaledWidthSmall : scaledWidth;
		final double height = small ? scaledHeightSmall : scaledHeight;
		try
		{
			final byte[] bufferPng = JairoSVG.builder()
				.fromStream(stream).outputWidth(width).outputHeight(height).toPng();
			try (final var byteArrayInputStream = new ByteArrayInputStream(bufferPng))
			{
				return new Image(byteArrayInputStream);
			}
		}
		catch (Exception ex)
		{
			logger.log(WARNING, "Error reading SVG from JairoSVG", ex);
			return null;
		}
	}

	/// Get an Image object from resources.
	///
	/// @param colorMode true to request a color icon, false for monochrome icons
	/// @param small false to request a default toolbar icon size,
	///							 true for smaller menu item icons
	///
	public @Nullable
	Image getIconImage(boolean colorMode, boolean small)
	{
		final String nameIconSet = "lucide_" + (colorMode ? "color" : "mono");
		final String resourceName = "%s/%s/action%s.svg".formatted(
			ICONS_RESOURCE_PACKAGE_BASE, nameIconSet, name());
		Image result;
		try (final InputStream stream = getClass().getResourceAsStream(resourceName))
		{
			result = stream != null ? readImage(stream, small) : null;
		}
		catch (IOException ex)
		{
			logger.log(WARNING, () -> "Resource not found: " + resourceName, ex);
			result = null;
		}
		return result;
	}
}
