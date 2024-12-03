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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.util.Optional;

import static java.lang.System.Logger.Level.*;
import static java.util.function.Predicate.not;

/**
 * Class to retrieve the application version.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ApplicationVersion
{
	private static final Logger logger = System.getLogger(ApplicationVersion.class.getName());
	private static final String FILENAME_SEMANTIC_VERSION = "semantic_version.txt";
	private static Optional<SemanticVersion> instance = Optional.empty();

	private static void setInstance(String version)
	{
		try
		{
			instance = Optional.of(new SemanticVersion(version));
		}
		catch (IllegalArgumentException ex)
		{
			instance = Optional.empty();
		}
	}

	/**
	 * Retrieves the application version.
	 *
	 * @return the application version
	 */
	static Optional<SemanticVersion> getInstance()
	{
		if (instance.isEmpty())
		{
			try (var stream = ApplicationVersion.class.getResourceAsStream(FILENAME_SEMANTIC_VERSION))
			{
				if (stream != null)
				{
					try (var reader = new BufferedReader(new InputStreamReader(stream)))
					{
						reader.lines().filter(not(String::isBlank)).findFirst()
							.ifPresentOrElse(ApplicationVersion::setInstance,
								() -> logger.log(WARNING, "Can't find a valid application semantic version."));
					}
				}
				else
				{
					logger.log(WARNING, "Can't find resource file with application semantic version.");
				}
			}
			catch (IOException ex)
			{
				logger.log(WARNING, "Error accessing resource file with application semantic version.");
			}
		}
		return instance;
	}
}
