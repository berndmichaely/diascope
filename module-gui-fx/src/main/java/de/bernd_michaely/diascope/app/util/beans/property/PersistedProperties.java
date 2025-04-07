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
package de.bernd_michaely.diascope.app.util.beans.property;

import de.bernd_michaely.diascope.app.PreferencesUtil;
import de.bernd_michaely.diascope.app.stage.PreferencesKeys;
import java.lang.System.Logger;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;

/// Factory class to create properties which are persisted by preferences.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class PersistedProperties
{
	private static final Logger logger = System.getLogger(PersistedProperties.class.getName());

	public static BooleanProperty newPersistedBooleanProperty(
		PreferencesKeys preferencesKey, Class<?> c, boolean defaultValue)
	{
		final var preferences = PreferencesUtil.nodeForPackage(c);
		final String key = preferencesKey.getKey();
		final var property = new SimpleBooleanProperty(preferences.getBoolean(key, defaultValue));
		property.addListener(onChange(newValue -> preferences.putBoolean(key, newValue)));
		return property;
	}

	public static DoubleProperty newPersistedDoubleProperty(
		PreferencesKeys preferencesKey, Class<?> c, double defaultValue)
	{
		final var preferences = PreferencesUtil.nodeForPackage(c);
		final String key = preferencesKey.getKey();
		final var property = new SimpleDoubleProperty(preferences.getDouble(key, defaultValue));
		property.addListener(onChange(newValue -> preferences.putDouble(key, newValue.doubleValue())));
		return property;
	}

	public static <E extends Enum<E>> ObjectProperty<E> newPersistedEnumProperty(
		PreferencesKeys preferencesKey, Class<?> c, E defaultValue)
	{
		final var preferences = PreferencesUtil.nodeForPackage(c);
		final String key = preferencesKey.getKey();
		final Class<E> enumClass = defaultValue.getDeclaringClass();
		E value;
		final String enumConstantName = preferences.get(key, defaultValue.name());
		try
		{
			value = Enum.valueOf(enumClass, enumConstantName);
		}
		catch (IllegalArgumentException ex)
		{
			value = defaultValue;
			logger.log(WARNING, () ->
				"PersistedEnumProperty: invalid persisted Enum<%s> constant name »%s« (using default value »%s« instead)"
					.formatted(enumClass.getName(), enumConstantName, defaultValue));
		}
		final var property = new SimpleObjectProperty<E>(value);
		property.addListener(onChange(newValue ->
			preferences.put(key, newValue != null ? newValue.name() : defaultValue.name())));
		return property;
	}

	public static <T> ObjectProperty<T> newPersistedObjectProperty(
		PreferencesKeys preferencesKey, Class<?> c, String defaultValue, Function<String, T> factory)
	{
		final var preferences = PreferencesUtil.nodeForPackage(c);
		final String key = preferencesKey.getKey();
		final var property = new SimpleObjectProperty<T>(factory.apply(preferences.get(key, defaultValue)));
		property.addListener(onChange(newValue -> preferences.put(key, Objects.toString(newValue, defaultValue))));
		return property;
	}
}
