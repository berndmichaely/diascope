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
package de.bernd_michaely.diascope;

import de.bernd_michaely.common.cli.parser.CommandLineArguments;
import de.bernd_michaely.common.cli.parser.InvalidCommandLineParametersException;
import de.bernd_michaely.common.cli.parser.OptionDefinitionException;
import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.ApplicationConfiguration.Geometry;
import de.bernd_michaely.diascope.app.DiascopeLauncher;
import de.bernd_michaely.diascope.app.PreferencesUtil;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.System.Logger.Level.*;
import static java.util.Objects.requireNonNullElse;

/// Main launcher of the Diascope application.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class Launcher
{
	private static final Logger logger = System.getLogger(Launcher.class.getName());
	private static final String REGEX_GEOMETRY = "(\\d{3,4})[xX](\\d{3,4})(([+-])(\\d{1,4})([+-])(\\d{1,4}))?";
	private static final String OPT_HELP = "help";
	private static final String OPT_DEVELOPMENT = "development";
	private static final String OPT_EXPERIMENTAL = "experimental";
	private static final String OPT_EXPORT_PREFERENCES = "show-preferences";
	private static final String OPT_CLEAR_PREFERENCES = "clear-preferences";
	private static final String OPT_INITIAL_PATH = "open";
	private static final String OPT_GEOMETRY = "geometry";
	private static boolean helpMode, developmentMode, experimentalMode, exportPrefsMode, clearPrefsMode;
	private static @Nullable String initialPath;
	private static @Nullable String strGeometry;
	private static @Nullable Geometry geometry;

	public static void main(String... args)
	{
		final var commandLineArguments = new CommandLineArguments((String longOption, @Nullable String param) ->
		{
			switch (longOption)
			{
				case OPT_HELP -> helpMode = true;
				case OPT_DEVELOPMENT -> developmentMode = true;
				case OPT_EXPERIMENTAL -> experimentalMode = true;
				case OPT_EXPORT_PREFERENCES -> exportPrefsMode = true;
				case OPT_CLEAR_PREFERENCES -> clearPrefsMode = true;
				case OPT_INITIAL_PATH -> initialPath = requireNonNullElse(param, "");
				case OPT_GEOMETRY -> strGeometry = param;
				default -> throw new AssertionError(
						"Invalid CommandLineArguments long option »%s«".formatted(longOption));
			}
		});
		try
		{
			commandLineArguments
				.addFlagOption(OPT_HELP, 'h', "print this help to stdout")
				.addFlagOption(OPT_DEVELOPMENT, 'd', "start application in development mode")
				.addFlagOption(OPT_EXPORT_PREFERENCES, 'p', "export preferences to stdout")
				.addFlagOption(OPT_CLEAR_PREFERENCES, 'c', "clear all preferences of this application")
				.addParameterOption(OPT_INITIAL_PATH, 'o', false,
					"path to open initially (no path parameter to open nothing)")
				.addFlagOption(OPT_EXPERIMENTAL, 'E', "enable experimental options")
				.addParameterOption(OPT_GEOMETRY, 'g', REGEX_GEOMETRY, true,
					"main window geometry, e.g. 800x600-200+100");
		}
		catch (OptionDefinitionException ex)
		{
			logger.log(ERROR, "Invalid command line option definition", ex);
		}
		List<String> commandLineArgs = null;
		try
		{
			commandLineArgs = commandLineArguments.parse(args);
		}
		catch (InvalidCommandLineParametersException ex)
		{
			commandLineArguments.printFormattedDescription(System.err, true);
			System.exit(1);
		}
		if (strGeometry != null)
		{
			final var matcher = commandLineArguments.getMatcher(OPT_GEOMETRY);
			if (matcher != null)
			{
				final String group1 = matcher.group(1);
				final String group2 = matcher.group(2);
				if (group1 != null && group2 != null)
				{
					final String group3 = matcher.group(3);
					if (group3 == null || group3.isBlank())
					{
						geometry = new Geometry(group1, group2);
					}
					else
					{
						final String group4 = matcher.group(4);
						final String group5 = matcher.group(5);
						final String group6 = matcher.group(6);
						final String group7 = matcher.group(7);
						if (group4 != null && group5 != null && group6 != null && group7 != null)
						{
							geometry = new Geometry(group1, group2, group4, group5, group6, group7);
						}
					}
				}
			}
		}
		ApplicationConfiguration.initInstance(Optional.ofNullable(initialPath), commandLineArgs,
			developmentMode, experimentalMode, Optional.ofNullable(geometry));
		if (helpMode)
		{
			final PrintStream ps = System.out;
			final var appName = new StringBuilder(ApplicationConfiguration.getApplicationName());
			ApplicationConfiguration.getState().version().ifPresent(
				version -> appName.append(" ").append(version));
			List.of(
				appName,
				""
			).forEach(ps::println);
			commandLineArguments.printFormattedDescription(ps, true);
		}
		else if (exportPrefsMode || clearPrefsMode)
		{
			if (exportPrefsMode)
			{
				PreferencesUtil.exportPreferences(System.out);
			}
			if (clearPrefsMode)
			{
				PreferencesUtil.clearPreferences(System.out);
			}
		}
		else
		{
			new DiascopeLauncher().run();
		}
	}
}
