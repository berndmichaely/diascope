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
import java.util.prefs.Preferences;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.bernd_michaely.diascope.app.stage.StageBounds.InitType.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Math.clamp;

/**
 * Class to handle the main window bounds.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class StageBounds
{
	private static final double UNINITIALIZED = -100_000;
	private static final double INITIAL_WINDOW_SIZE = 0.7;
	private final Stage stage;
	private final PrefKeys prefKeys;
	private final Preferences preferences;
	private final ApplicationConfiguration.State state;
	private final double screenWidth;
	private final double screenHeight;
	private final double widthPref, heightPref, xPref, yPref;
	private double width, height, x, y;

	enum InitType
	{
		GIVEN_GEOMETRY
		{
			@Override
			String getLogMessage()
			{
				return "Setting main window bounds";
			}
		}, FIRST_START
		{
			@Override
			String getLogMessage()
			{
				return "Initializing main window bounds";
			}
		}, RESTORE_PREFERENCES
		{
			@Override
			String getLogMessage()
			{
				return "Restoring main window bounds";
			}
		};

		abstract String getLogMessage();
	}
	private final InitType initType;

	record PrefKeys(
		PreferencesKeys width, PreferencesKeys height,
		PreferencesKeys x, PreferencesKeys y, PreferencesKeys maximize)
	{
	}

	StageBounds(Stage stage, PrefKeys prefKeys)
	{
		this.stage = stage;
		this.prefKeys = prefKeys;
		this.preferences = PreferencesUtil.nodeForPackage(StageBounds.class);
		final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		this.screenWidth = screenBounds.getWidth();
		this.screenHeight = screenBounds.getHeight();
		this.state = ApplicationConfiguration.getState();
		this.widthPref = preferences.getDouble(prefKeys.width().getKey(), UNINITIALIZED);
		this.heightPref = preferences.getDouble(prefKeys.height().getKey(), UNINITIALIZED);
		this.xPref = preferences.getDouble(prefKeys.x().getKey(), UNINITIALIZED);
		this.yPref = preferences.getDouble(prefKeys.y().getKey(), UNINITIALIZED);
		final boolean needsInit =
			widthPref == UNINITIALIZED || heightPref == UNINITIALIZED ||
			xPref == UNINITIALIZED || yPref == UNINITIALIZED;
		this.initType = state.geometry().isPresent() ? GIVEN_GEOMETRY :
			(needsInit ? FIRST_START : RESTORE_PREFERENCES);
	}

	void initialize()
	{
		initValues();
		initListener();
	}

	private void initValues()
	{
		switch (initType)
		{
			case GIVEN_GEOMETRY ->
			{
				getGeometry();
				checkBounds();
			}
			case FIRST_START ->
			{
				final double f = INITIAL_WINDOW_SIZE;
				final double w = screenWidth;
				final double h = screenHeight;
				width = w * f;
				height = h * f;
				center();
			}
			case RESTORE_PREFERENCES ->
			{
				width = widthPref;
				height = heightPref;
				x = xPref;
				y = yPref;
				checkBounds();
			}
			default -> throw new AssertionError(StageBounds.class.getName() + ": Invalid InitType");
		}
		setBounds();
		if (state.geometry().isEmpty())
		{
			stage.setMaximized(preferences.getBoolean(prefKeys.maximize().getKey(), false));
		}
	}

	private void getGeometry()
	{
		final var geometry = state.geometry();
		geometry.ifPresent(g ->
		{
			width = g.width();
			height = g.height();
			if (g.position())
			{
				x = g.fromRight() ? screenWidth - width - g.x() : g.x();
				y = g.fromBottom() ? screenHeight - height - g.y() : g.y();
			}
			else
			{
				center();
			}
		});
	}

	private void center()
	{
		x = (screenWidth - width) / 2;
		y = (screenHeight - height) / 2;
	}

	private void checkBounds()
	{
		width = clamp(width, 100, screenWidth);
		height = clamp(height, 100, screenHeight);
		x = clamp(x, 0, screenWidth - 50);
		y = clamp(y, 0, screenHeight - 50);
	}

	private void setBounds()
	{
		stage.setX(x);
		stage.setY(y);
		stage.setWidth(width);
		stage.setHeight(height);
	}

	private void initListener()
	{
		// Note to the following listeners:
		// the test of (!stage.isMaximized()) doesn't work with all window managers
		stage.xProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(prefKeys.x().getKey(), stage.getX());
			}
		}));
		stage.yProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(prefKeys.y().getKey(), stage.getY());
			}
		}));
		stage.widthProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(prefKeys.width().getKey(), stage.getWidth());
			}
		}));
		stage.heightProperty().addListener(onChange(() ->
		{
			if (!stage.isMaximized())
			{
				preferences.putDouble(prefKeys.height().getKey(), stage.getHeight());
			}
		}));
		stage.maximizedProperty().addListener(onChange(maximized ->
			preferences.putBoolean(prefKeys.maximize().getKey(), maximized)));
	}

	String getLogMessage()
	{
		return "%s → [%.0fx%.0f+%.0f+%.0f]".formatted(
			initType.getLogMessage(), width, height, x, y);
	}

	@Override
	public String toString()
	{
		return getClass().getName() + " → " + getLogMessage();
	}
}
