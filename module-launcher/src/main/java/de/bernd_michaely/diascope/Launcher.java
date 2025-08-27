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
import de.bernd_michaely.diascope.app.DiascopeLauncher;
import de.bernd_michaely.diascope.app.PreferencesUtil;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.System.Logger.Level.*;

/**
 * Main launcher of the Diascope application.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class Launcher
{
	private static final Logger logger = System.getLogger(Launcher.class.getName());
	private static final String OPT_HELP = "help";
	private static final String OPT_DEVELOPMENT = "development";
	private static final String OPT_EXPORT_PREFERENCES = "show-preferences";
	private static final String OPT_CLEAR_PREFERENCES = "clear-preferences";
	private static final String OPT_INITIAL_PATH = "open";
	private static boolean helpMode, developmentMode, exportPrefsMode, clearPrefsMode;
	private static @Nullable String initialPath;

	public static void main(String... args)
	{
		final var commandLineArguments = new CommandLineArguments((String longOption, @Nullable String param) ->
		{
			switch (longOption)
			{
				case OPT_HELP -> helpMode = true;
				case OPT_DEVELOPMENT -> developmentMode = true;
				case OPT_EXPORT_PREFERENCES -> exportPrefsMode = true;
				case OPT_CLEAR_PREFERENCES -> clearPrefsMode = true;
				case OPT_INITIAL_PATH -> initialPath = param;
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
				.addParameterOption(OPT_INITIAL_PATH, 'o', true, "path to open initially");
		}
		catch (OptionDefinitionException ex)
		{
			logger.log(ERROR, "Invalid command line option definition", ex);
		}
		List<String> pathNames = null;
		try
		{
			pathNames = commandLineArguments.parse(args);
		}
		catch (InvalidCommandLineParametersException ex)
		{
			commandLineArguments.printFormattedDescription(System.err, true);
			System.exit(1);
		}
		ApplicationConfiguration.initInstance(pathNames, developmentMode, Optional.ofNullable(initialPath));
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
