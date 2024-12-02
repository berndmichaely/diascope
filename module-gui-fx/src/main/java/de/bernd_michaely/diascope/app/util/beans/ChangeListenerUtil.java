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
package de.bernd_michaely.diascope.app.util.beans;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Utilities related to the ChangeListener interface.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ChangeListenerUtil
{
	/**
	 * Adapter from Runnable to ChangeListener.
	 *
	 * @param <T>      the type of the observed value
	 * @param runnable Runnable to handle the change
	 * @return a compatible ChangeListener
	 */
	public static <T> ChangeListener<T> onChange(Runnable runnable)
	{
		return (_, _, _) -> runnable.run();
	}

	/**
	 * Adapter to consume the new value.
	 *
	 * @param <T>      the type of the observed value
	 * @param consumer Consumer to handle the new listener value
	 * @return a compatible ChangeListener
	 */
	public static <T> ChangeListener<T> onChange(Consumer<T> consumer)
	{
		return (ObservableValue<? extends T> _, T _, T newValue) ->
			consumer.accept(newValue);
	}

	/**
	 * Adapter to consume the old and new value.
	 *
	 * @param <T>        the type of the observed value
	 * @param biConsumer BiConsumer to handle the old (first) and new (second)
	 *                   listener values
	 * @return a compatible ChangeListener
	 */
	public static <T> ChangeListener<T> onChange(BiConsumer<T, T> biConsumer)
	{
		return (ObservableValue<? extends T> _, T oldValue, T newValue) ->
			biConsumer.accept(oldValue, newValue);
	}
}
