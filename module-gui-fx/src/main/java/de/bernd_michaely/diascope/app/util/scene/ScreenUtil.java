/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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

import javafx.stage.Screen;

import static java.lang.Math.round;

/// Utility for Screen properties.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ScreenUtil
{
	private static final int SCALED_WIDTH, SCALED_HEIGHT;
	private static final int SCALED_WIDTH_SMALL, SCALED_HEIGHT_SMALL;
	private static final int BASE_SIZE = 24;
//	private static final float FACTOR_SIZE_SMALL = 2f / 3f;
	private static final float FACTOR_SIZE_SMALL = 0.75f;

	static
	{
		final var screen = Screen.getPrimary();
		SCALED_WIDTH = (int) round(BASE_SIZE * screen.getOutputScaleX());
		SCALED_HEIGHT = (int) round(BASE_SIZE * screen.getOutputScaleY());
		SCALED_WIDTH_SMALL = round(SCALED_WIDTH * FACTOR_SIZE_SMALL);
		SCALED_HEIGHT_SMALL = round(SCALED_HEIGHT * FACTOR_SIZE_SMALL);

	}

	/// Returns the resulting icon width.
	/// That is it returns the base icon width scaled by the primary screens
	/// output scale factor.
	///
	/// @param small false to request a default toolbar icon size,
	///							 true for smaller menu item icons
	/// @return the resulting icon width
	///
	public static int getScaledWidth(boolean small)
	{
		return small ? SCALED_WIDTH_SMALL : SCALED_WIDTH;
	}

	/// Returns the resulting icon height.
	/// That is it returns the base icon height scaled by the primary screens
	/// output scale factor.
	///
	/// @param small false to request a default toolbar icon size,
	///							 true for smaller menu item icons
	/// @return the resulting icon height
	///
	public static int getScaledHeight(boolean small)
	{
		return small ? SCALED_HEIGHT_SMALL : SCALED_HEIGHT;
	}
}
