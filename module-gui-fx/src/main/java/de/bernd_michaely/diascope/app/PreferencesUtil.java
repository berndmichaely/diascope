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

import de.bernd_michaely.diascope.app.version.SemanticVersion;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.System.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static de.bernd_michaely.diascope.app.ApplicationConfiguration.LaunchType.*;
import static java.lang.System.Logger.Level.*;

/**
 * Facade for storing preference data.
 * <strong>Important:</strong>
 * Always use {@link #nodeForPackage(Class)} of this class, never use
 * {@link Preferences#userNodeForPackage(Class)} directly!
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class PreferencesUtil
{
	private static final Logger logger = System.getLogger(PreferencesUtil.class.getName());

	static Class<?> getAppRootPackageClass()
	{
		return PreferencesUtil.class;
	}

	/**
	 * Returns the postfix to be appended to the base package name. This postfix
	 * may depend on miscellaneous factors including:
	 * <ul>
	 * <li>{@link de.bernd_michaely.diascope.app.ApplicationConfiguration.LaunchType LaunchType}</li>
	 * <li>{@link SemanticVersion#getPreRelease()}</li>
	 * </ul>
	 *
	 * @return the postfix to be appended to the base package name
	 */
	static String getAppRootPostfix()
	{
		final var state = ApplicationConfiguration.getState();
		final var launchType = state.launchType();
		final StringBuilder s = new StringBuilder("_");
		if (launchType == NORMAL)
		{
			final var semanticVersion = state.version().orElse(new SemanticVersion());
			s.append("v").append(semanticVersion.getMajor());
			final String preRelease = semanticVersion.getPreRelease().toString();
			if (!preRelease.isBlank())
			{
				s.append("_").append(preRelease.replace('.', '-'));
			}
		}
		else
		{
			s.append(launchType.name().toLowerCase());
		}
		return s.toString();
	}

	/**
	 * Returns a preferences node for the given class to store preference data.
	 * Different sub trees may be used dependent on the launch type and
	 * prerelease.
	 * <strong>Important:</strong> Always use this method instead of using
	 * {@link Preferences#userNodeForPackage(Class)} directly!
	 *
	 * @param c the class to determine the associated package
	 * @return a preferences node
	 */
	public static Preferences nodeForPackage(Class<?> c)
	{
		final Preferences preferencesAppRoot = Preferences.userNodeForPackage(getAppRootPackageClass());
		final String rootPath = preferencesAppRoot.absolutePath();
		final String prefPath = Preferences.userNodeForPackage(c).absolutePath();
		if (!prefPath.startsWith(rootPath))
		{
			throw new IllegalStateException(PreferencesUtil.class.getName() +
				"::nodeForPackage(Class<?> c) : invalid package outside »" + rootPath + "«");
		}
		final Preferences parentAppRoot = preferencesAppRoot.parent();
		if (parentAppRoot != null)
		{
			final Preferences node = parentAppRoot.node(
				preferencesAppRoot.name() + getAppRootPostfix() + prefPath.substring(rootPath.length()));
			logger.log(TRACE, () -> "Accessing " + node);
			return node;
		}
		else
		{
			throw new IllegalStateException(PreferencesUtil.class.getName() +
				"::nodeForPackage(Class<?> c) : invalid app root »" + preferencesAppRoot + "«");
		}
	}

	public static void exportPreferences(OutputStream outputStream)
	{
		try
		{
			nodeForPackage(getAppRootPackageClass()).exportSubtree(outputStream);
		}
		catch (IOException | BackingStoreException | IllegalStateException ex)
		{
			logger.log(ERROR, ex);
		}
	}

	public static void clearPreferences(OutputStream outputStream)
	{
		try
		{
			nodeForPackage(getAppRootPackageClass()).removeNode();
			try (var w = new BufferedWriter(new OutputStreamWriter(outputStream)))
			{
				w.write("Preferences cleared.\n");
			}
		}
		catch (IOException | BackingStoreException | IllegalStateException ex)
		{
			logger.log(ERROR, ex);
		}
	}
}
