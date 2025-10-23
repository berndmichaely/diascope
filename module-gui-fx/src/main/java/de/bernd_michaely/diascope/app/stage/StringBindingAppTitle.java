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
package de.bernd_michaely.diascope.app.stage;

import java.io.File;
import java.nio.file.Path;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.ApplicationConfiguration.getApplicationName;
import static de.bernd_michaely.diascope.app.stage.GlobalConstants.PATH_USER_HOME;
import static java.util.Objects.requireNonNull;

/// String binding for the stage titles.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class StringBindingAppTitle extends StringBinding
{
	static final String POSTFIX_DEVELOPMENT_MODE = " (development mode)";
	private static final String STR_HOME = "~";
	private final ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty;
	private final BooleanProperty developmentModeProperty;

	private StringBindingAppTitle(
		ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty,
		BooleanProperty developmentModeProperty)
	{
		this.selectedPathProperty = requireNonNull(
			selectedPathProperty, "selectedPathProperty is null");
		this.developmentModeProperty = requireNonNull(
			developmentModeProperty, "developmentModeProperty is null");
	}

	static StringBinding create(
		ReadOnlyObjectProperty<@Nullable Path> selectedPathProperty,
		BooleanProperty developmentModeProperty)
	{
		final var stringBinding = new StringBindingAppTitle(
			selectedPathProperty, developmentModeProperty);
		stringBinding.bind(selectedPathProperty, developmentModeProperty);
		return stringBinding;
	}

	@Override
	protected String computeValue()
	{
		final StringBuilder title = new StringBuilder(getApplicationName());
		final Path selectedPath = selectedPathProperty.get();
		if (selectedPath != null)
		{
			title.append(" - ");
			final Path pathHome = PATH_USER_HOME;
			if (selectedPath.startsWith(pathHome))
			{
				title.append(STR_HOME);
				final String pathHomeRelative = pathHome.relativize(selectedPath).toString();
				if (!pathHomeRelative.isBlank())
				{
					title.append(File.separator).append(pathHomeRelative);
				}
			}
			else
			{
				title.append(selectedPath);
			}
		}
		if (this.developmentModeProperty.get())
		{
			title.append(POSTFIX_DEVELOPMENT_MODE);
		}
		return title.toString();
	}
}
