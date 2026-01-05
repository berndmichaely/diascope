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

import java.util.EnumMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.equal;
import static javafx.beans.binding.Bindings.isNotNull;
import static javafx.beans.binding.Bindings.when;

/// Factory class to create properties related to enums.
/// Properties include:
///   * the raw enum value
///   * a raw-or-default value, which returns the default value instead of a `null` raw value
///   * characteristic boolean values for each enum constant to indicate,
///     whether the current value equals the related enum constant
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class EnumProperties<V extends Enum<V>>
{
	private final Enum<V> defaultValue;
	private final ObjectProperty<@Nullable Enum<V>> rawValueProperty;
	private final ReadOnlyObjectWrapper<Enum<V>> valueOrDefaultProperty;
	private @MonotonicNonNull EnumMap<V, ReadOnlyBooleanProperty> map;

	/// Creates a new instance with the given default value.
	///
	/// @param defaultValue the given default value
	/// @throws NullPointerException if the default value is `null`
	///
	private EnumProperties(V defaultValue)
	{
		this.defaultValue = requireNonNull(defaultValue);
		this.rawValueProperty = new SimpleObjectProperty<>();
		this.valueOrDefaultProperty = new ReadOnlyObjectWrapper<>();
	}

	@SuppressWarnings("argument")
	private void init()
	{
		valueOrDefaultProperty.bind(
			when(isNotNull(rawValueProperty)).then(rawValueProperty).otherwise(defaultValue));
	}

	/// Creates a new instance with the given default value.
	///
	/// @param defaultValue a default value, which must not be `null`
	/// @return a new instance
	/// @throws NullPointerException if the default value is `null`
	///
	public static <E extends Enum<E>> EnumProperties<E> createInstance(E defaultValue)
	{
		final var enumProperties = new EnumProperties<E>(defaultValue);
		enumProperties.init();
		return enumProperties;
	}

	public Enum<V> getDefaultValue()
	{
		return defaultValue;
	}

	public ObjectProperty<@Nullable Enum<V>> rawValueProperty()
	{
		return rawValueProperty;
	}

	public @Nullable
	Enum<V> getRawValue()
	{
		return rawValueProperty().get();
	}

	public void setRawValue(@Nullable Enum<V> value)
	{
		rawValueProperty().set(value);
	}

	public ReadOnlyObjectProperty<Enum<V>> valueOrDefaultProperty()
	{
		return valueOrDefaultProperty.getReadOnlyProperty();
	}

	public Enum<V> getValueOrDefault()
	{
		return valueOrDefaultProperty().get();
	}

	public ReadOnlyBooleanProperty isValueProperty(V enumValue)
	{
		if (map == null)
		{
			map = new EnumMap<>(defaultValue.getDeclaringClass());
		}
		return map.computeIfAbsent(enumValue, key ->
		{
			final var readOnlyBooleanWrapper = new ReadOnlyBooleanWrapper();
			readOnlyBooleanWrapper.bind(equal(valueOrDefaultProperty, key));
			return readOnlyBooleanWrapper.getReadOnlyProperty();
		});
	}

	public boolean isValue(V value)
	{
		return isValueProperty(value).get();
	}

	@Override
	public String toString()
	{
		return "%s: »%s« [raw: »%s«]".formatted(
			getDefaultValue().getDeclaringClass().getName(), getValueOrDefault(), "" + getRawValue());
	}
}
