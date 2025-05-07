/*
 * Copyright 2025 Bernd Michaely (info@bernd-michaely.de).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bernd_michaely.common.desktop.fx.collections.selection;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * Adds selection related properties.
 *
 * @since 2.1
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public interface SelectableProperties extends Selectable
{
	/**
	 * Property indicating the number of selected items.
	 *
	 * @return a property indicating the number of selected items
	 */
	ReadOnlyIntegerProperty numSelectedProperty();

	/**
	 * Returns the number of selected items.
	 *
	 * @return the number of selected items
	 */
	default int getNumSelected()
	{
		return numSelectedProperty().get();
	}

	/**
	 * Returns a property which evaluates to true, iff there are no selected
	 * items.
	 *
	 * @return true, iff there are no selected items
	 */
	ReadOnlyBooleanProperty noneSelectedProperty();

	/**
	 * Returns true, iff there are no selected items.
	 *
	 * @return true, iff there are no selected items
	 */
	default boolean isNoneSelected()
	{
		return noneSelectedProperty().get();
	}

	/**
	 * Returns a property which evaluates to true, iff there are no unselected
	 * items.
	 *
	 * @return true, iff there are no unselected items
	 */
	ReadOnlyBooleanProperty allSelectedProperty();

	/**
	 * Returns true, iff there are no unselected items.
	 *
	 * @return true, iff there are no unselected items
	 */
	default boolean isAllSelected()
	{
		return allSelectedProperty().get();
	}

	/**
	 * Property indicating the list size.
	 *
	 * @return a property indicating the list size
	 */
	ReadOnlyIntegerProperty sizeProperty();

	/**
	 * Returns the value of the size property.
	 *
	 * @return the value of the size property
	 */
	default int getSize()
	{
		return sizeProperty().get();
	}

	@Override
	default int size()
	{
		return getSize();
	}

	/**
	 * Property to indicate an empty list.
	 *
	 * @return a property to indicate an empty list
	 */
	ReadOnlyBooleanProperty emptyProperty();

	/**
	 * Returns the value of the empty property.
	 *
	 * @return the value of the empty property
	 */
	default boolean isEmpty()
	{
		return emptyProperty().get();
	}
}
