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
package de.bernd_michaely.diascope.app.util.common;

import static java.util.stream.Collectors.joining;

/**
 * Utility class to create a human readable description of the JRE10+ in use.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class JreVersionUtil
{
	/**
	 * Returns a descriptive JRE version string retrieved from a system property.
	 *
	 * @return a formatted string indicating the JRE version in use
	 */
	public static String getJreVersionInfo()
	{
		return getJreVersionInfo(Runtime.version());
	}

	/**
	 * Package locale method for implementation and unit testing.
	 *
	 * @param runtimeVersion the runtime version to consider
	 * @return the formatted string indicating the JRE version in use
	 */
	static String getJreVersionInfo(Runtime.Version runtimeVersion)
	{
		final StringBuilder s = new StringBuilder(runtimeVersion.version().stream()
			.map(Number::toString).collect(joining(".")));
		runtimeVersion.pre().ifPresent(num -> s.append(" pre-release ").append(num));
		runtimeVersion.build().ifPresent(num -> s.append(" build ").append(num));
		runtimeVersion.optional().ifPresent(opt -> s.append(" (").append(opt).append(")"));
		return s.toString();
	}
}
