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
package de.bernd_michaely.diascope.app;

import de.bernd_michaely.common.semver.SemanticVersion;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.ApplicationConfiguration.LaunchType.*;

/**
 * Global application configuration. A singleton class.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ApplicationConfiguration
{
	private static final String STR_APP_NAME = "Diascope";
	private static @MonotonicNonNull ApplicationConfiguration instance;
	private final State state;

	public static String getApplicationName()
	{
		return STR_APP_NAME;
	}

	/**
	 * The application starting mode.
	 */
	public enum LaunchType
	{
		NORMAL, DEVELOPMENT, UNIT_TEST
	}

	public record Geometry(int width, int height, boolean position,
		boolean fromRight, int x, boolean fromBottom, int y)
	{
		public Geometry(int width, int height)
		{
			this(width, height, false, false, -1, false, -1);
		}

		public Geometry(String width, String height)
		{
			this(Integer.parseInt(width), Integer.parseInt(height));
		}

		public Geometry(String width, String height,
			String fromRight, String x, String fromBottom, String y)
		{
			this(Integer.parseInt(width), Integer.parseInt(height), true,
				"-".equals(fromRight), Integer.parseInt(x), "-".equals(fromBottom), Integer.parseInt(y));
		}
	}

	public record State(
		Optional<SemanticVersion> version,
		LaunchType launchType,
		List<String> commandLineArguments,
		Optional<String> initialPath,
		BooleanProperty developmentModeProperty,
		boolean experimentalMode,
		Optional<Geometry> geometry)
	{
		public boolean isStartedInDevelopmentMode()
		{
			return launchType() == LaunchType.DEVELOPMENT;
		}

		public String[] getCommandLineArgs()
		{
			return commandLineArguments().toArray(String[]::new);
		}
	}

	private ApplicationConfiguration(State state)
	{
		this.state = state;
	}

	public static State getState()
	{
		if (instance != null)
		{
			return instance.state;
		}
		else
		{
			throw new IllegalStateException(ApplicationConfiguration.class.getName() +
				"::initInstance not called!");

		}
	}

	public static void initInstance(Optional<String> initialPath,
		List<String> commandLineArguments, boolean developmentMode,
		boolean experimentalMode, Optional<Geometry> geometry)
	{
		if (instance == null)
		{
			instance = new ApplicationConfiguration(
				new State(ApplicationVersion.getInstance(),
					developmentMode ? DEVELOPMENT : NORMAL,
					commandLineArguments,
					initialPath,
					new SimpleBooleanProperty(developmentMode),
					experimentalMode,
					geometry));
		}
		else
		{
			throw new IllegalStateException(ApplicationConfiguration.class.getName() +
				"::initInstance called twice!");
		}
	}

	static void initInstanceForUnitTests()
	{
		initInstanceForUnitTests(null);
	}

	static void initInstanceForUnitTests(@Nullable List<String> commandLineArguments)
	{
		if (instance == null)
		{
			instance = new ApplicationConfiguration(
				new State(ApplicationVersion.getInstance(),
					UNIT_TEST,
					commandLineArguments != null ? commandLineArguments : List.of(),
					Optional.empty(),
					new SimpleBooleanProperty(),
					false, Optional.empty()));
		}
		else
		{
			throw new IllegalStateException(ApplicationConfiguration.class.getName() +
				"::initInstance called twice!");
		}
	}
}
