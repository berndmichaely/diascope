/*
 * Copyright (C) 2025 Bernd Michaely (info@bernd-michaely.de)
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
import de.bernd_michaely.diascope.app.PreferencesUtil;
import java.lang.System.Logger;
import java.util.prefs.Preferences;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.*;
import static de.bernd_michaely.diascope.app.stage.StageBounds.InitType.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Math.clamp;
import static java.lang.System.Logger.Level.*;

/**
 * Class to handle the main window bounds.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class StageBounds
{
	private static final Logger logger = System.getLogger(StageBounds.class.getName());
	private static final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
	private static final double SCREEN_HEIGHT = screenBounds.getHeight();
	private static final double SCREEN_WIDTH = screenBounds.getWidth();
	private static final Preferences preferences = PreferencesUtil.nodeForPackage(StageBounds.class);
	private static final double UNINITIALIZED = -100_000;
	private static final double INITIAL_WINDOW_SIZE = 0.7;
	private static double width, height, x, y;

	enum InitType
	{
		GIVEN_GEOMETRY, FIRST_START, RESTORE_PREFERENCES
	}

	static void initStageBounds(Stage stage)
	{
		initValues(stage);
		initListener(stage);
	}

	private static void initValues(Stage stage)
	{
		final var state = ApplicationConfiguration.getState();
		final double widthPref = preferences.getDouble(PREF_KEY_WIDTH.getKey(), UNINITIALIZED);
		final double heightPref = preferences.getDouble(PREF_KEY_HEIGHT.getKey(), UNINITIALIZED);
		final double xPref = preferences.getDouble(PREF_KEY_X.getKey(), UNINITIALIZED);
		final double yPref = preferences.getDouble(PREF_KEY_Y.getKey(), UNINITIALIZED);
		final boolean needsInit =
			widthPref == UNINITIALIZED || heightPref == UNINITIALIZED ||
			xPref == UNINITIALIZED || yPref == UNINITIALIZED;
		final var initType = state.geometry().isPresent() ? GIVEN_GEOMETRY :
			(needsInit ? FIRST_START : RESTORE_PREFERENCES);
		switch (initType)
		{
			case GIVEN_GEOMETRY ->
			{
				logger.log(TRACE, "Setting main window bounds…");
				getGeometry();
				checkBounds();
			}
			case FIRST_START ->
			{
				logger.log(TRACE, "Initializing main window bounds…");
				final double f = INITIAL_WINDOW_SIZE;
				final double w = SCREEN_WIDTH;
				final double h = SCREEN_HEIGHT;
				width = w * f;
				height = h * f;
				center();
			}
			case RESTORE_PREFERENCES ->
			{
				logger.log(TRACE, "Restoring main window bounds…");
				width = widthPref;
				height = heightPref;
				x = xPref;
				y = yPref;
				checkBounds();
			}
			default -> throw new AssertionError(StageBounds.class.getName() + ": Invalid InitType");
		}
		setBounds(stage);
		if (state.geometry().isEmpty())
		{
			stage.setMaximized(preferences.getBoolean(PREF_KEY_MAXIMIZE.getKey(), false));
		}
	}

	private static void getGeometry()
	{
		final var state = ApplicationConfiguration.getState();
		final var geometry = state.geometry();
		geometry.ifPresent(g ->
		{
			width = g.width();
			height = g.height();
			if (g.position())
			{
				x = g.fromRight() ? SCREEN_WIDTH - width - g.x() : g.x();
				y = g.fromBottom() ? SCREEN_HEIGHT - height - g.y() : g.y();
			}
			else
			{
				center();
			}
		});
	}

	private static void center()
	{
		x = (SCREEN_WIDTH - width) / 2;
		y = (SCREEN_HEIGHT - height) / 2;
	}

	private static void checkBounds()
	{
		width = clamp(width, 100, SCREEN_WIDTH);
		height = clamp(height, 100, SCREEN_HEIGHT);
		x = clamp(x, 0, SCREEN_WIDTH - 50);
		y = clamp(y, 0, SCREEN_HEIGHT - 50);
	}

	private static void setBounds(Stage stage)
	{
		stage.setX(x);
		stage.setY(y);
		stage.setWidth(width);
		stage.setHeight(height);
	}

	private static void initListener(Stage stage)
	{
		// Note to the following listeners:
		// the test of (!stage.isMaximized()) doesn't work with all window managers
		stage.xProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_X.getKey(), stage.getX());
			}
		}));
		stage.yProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_Y.getKey(), stage.getY());
			}
		}));
		stage.widthProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_WIDTH.getKey(), stage.getWidth());
			}
		}));
		stage.heightProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(PREF_KEY_HEIGHT.getKey(), stage.getHeight());
			}
		}));
		stage.maximizedProperty().addListener(onChange(maximized ->
			preferences.putBoolean(PREF_KEY_MAXIMIZE.getKey(), maximized)));
	}
}
