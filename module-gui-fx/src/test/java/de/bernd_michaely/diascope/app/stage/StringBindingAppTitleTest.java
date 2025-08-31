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

import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.stage.GlobalConstants.PATH_USER_HOME;
import static de.bernd_michaely.diascope.app.stage.StringBindingAppTitle.POSTFIX_DEVELOPMENT_MODE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StringBindingAppTitle.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class StringBindingAppTitleTest
{
	@Test
	public void testFactoryMethod()
	{
		assertThrows(NullPointerException.class,
			() -> StringBindingAppTitle.create(
				new ReadOnlyObjectWrapper<Path>().getReadOnlyProperty(), null));
		assertThrows(NullPointerException.class,
			() -> StringBindingAppTitle.create(null, new SimpleBooleanProperty()));
	}

	private String mode(String value, boolean isDevelopmentMode)
	{
		return isDevelopmentMode ? value + POSTFIX_DEVELOPMENT_MODE : value;
	}

	private void testCalculatedValue(boolean devMode) throws URISyntaxException
	{
		final String title = ApplicationConfiguration.getApplicationName();
		final String titlePrefix = title + " - ";
		final var pathProperty = new ReadOnlyObjectWrapper<Path>();
		final var developmentModeProperty = new SimpleBooleanProperty(devMode);
		final StringBinding stringBinding = StringBindingAppTitle.create(
			pathProperty.getReadOnlyProperty(), developmentModeProperty);
		System.out.println(mode("Test null path", devMode));
		assertNull(pathProperty.get());
		assertEquals(mode(title, devMode), stringBinding.get());
		System.out.println(mode("Test home path", devMode));
		pathProperty.set(PATH_USER_HOME);
		assertEquals(mode(titlePrefix + "~", devMode), stringBinding.get());
		System.out.println(mode("Test home subdir", devMode));
		pathProperty.set(Path.of(PATH_USER_HOME.toString(), "subdir"));
		assertEquals(mode(titlePrefix + "~" + File.separator + "subdir", devMode), stringBinding.get());
		System.out.println(mode("Test path outside home dir", devMode));
		pathProperty.set(Path.of(new URI("file:///tmp")));
		assertEquals(mode(titlePrefix + File.separator + "tmp", devMode), stringBinding.get());
	}

	/**
	 * Test of computed value of StringBinding.
	 */
	@Test
	public void testComputeValue() throws URISyntaxException
	{
		testCalculatedValue(false);
	}

	/**
	 * Test of computed value of StringBinding.
	 */
	@Test
	public void testComputeValueDevelopmentMode() throws URISyntaxException
	{
		testCalculatedValue(true);
	}
}
