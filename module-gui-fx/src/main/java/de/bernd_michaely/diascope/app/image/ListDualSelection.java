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
package de.bernd_michaely.diascope.app.image;

import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableList;
import de.bernd_michaely.diascope.app.util.beans.ListChangeListenerBuilder;
import java.util.Optional;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import static de.bernd_michaely.common.desktop.fx.collections.selection.SelectionChangeListener.SelectionChange.SelectionChangeType.*;

/// Class to handle a dual selection state of a SelectableList.
/// Dual selection has a specific meaning in this context:
///
///   * Dual selection becomes true, if:
///     1. a single element is selected and then
///     2. a second element becomes selected.
///   * If the list contains exactly 2 elements, then dual selection is always true.
///
/// In the latter case, if exactly one element is selected, then
///
///   * the unselected list element is the first selected and
///   * the selected list element is the second selected.
///
/// and otherwise
///
///	  * the first list element is the first selected and
///   * the second list element is the second selected
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ListDualSelection<T>
{
	private final ReadOnlyBooleanWrapper singleItemSelected;
	private final ReadOnlyBooleanWrapper dualItemsSelected;
	private final ReadOnlyObjectWrapper<Optional<T>> singleSelectionItem;
	private final ReadOnlyObjectWrapper<Optional<T>> dualSelectionFirstItem;
	private final ReadOnlyObjectWrapper<Optional<T>> dualSelectionSecondItem;

	ListDualSelection(SelectableList<T> list)
	{
		this.singleItemSelected = new ReadOnlyBooleanWrapper();
		this.singleSelectionItem = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.dualItemsSelected = new ReadOnlyBooleanWrapper();
		this.dualSelectionFirstItem = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.dualSelectionSecondItem = new ReadOnlyObjectWrapper<>(Optional.empty());
		final Runnable checkSingleSelection = () ->
		{
			final int numSelected = list.getNumSelected();
			final boolean isSingleSelected = numSelected == 1;
			singleItemSelected.set(isSingleSelected);
			if (isSingleSelected)
			{
				final int n = list.size();
				boolean selected = false;
				for (int i = 0; i < n && !selected; i++)
				{
					selected = list.isSelected(i);
					if (selected)
					{
						singleSelectionItem.set(Optional.ofNullable(list.get(i)));
					}
				}
			}
			else
			{
				singleSelectionItem.set(Optional.empty());
			}
		};
		final Runnable checkDualSelection = () ->
		{
			if (list.size() == 2)
			{
				dualItemsSelected.set(true);
				if (list.getNumSelected() == 1)
				{
					singleItemSelected.set(true);
					singleSelectionItem.set(Optional.ofNullable(
						list.isSelected(0) ? list.getFirst() : list.getLast()));
					dualSelectionSecondItem.set(singleSelectionItem.get());
					final var firstItem = singleSelectionItem.get().get();
					dualSelectionFirstItem.set(list.stream()
						.filter(item -> item != firstItem)
						.findAny());
				}
				else
				{
					dualSelectionFirstItem.set(Optional.ofNullable(list.getFirst()));
					dualSelectionSecondItem.set(Optional.ofNullable(list.getLast()));
				}
			}
		};
		list.addListener(new ListChangeListenerBuilder<>()
			.onAdd(_ -> checkDualSelection.run())
			.onRemove(change ->
			{
				checkSingleSelection.run();
				final int n = list.size();
				if (n < 2)
				{
					dualItemsSelected.set(false);
					dualSelectionFirstItem.set(Optional.empty());
					dualSelectionSecondItem.set(Optional.empty());
				}
				if (n == 2)
				{
					checkDualSelection.run();
				}
				else // n > 2
				{
					if (dualItemsSelected.get())
					{
						final boolean containsFirst = list.contains(dualSelectionFirstItem.get());
						final boolean containsSecond = list.contains(dualSelectionSecondItem.get());
						if (!(containsFirst && containsSecond))
						{
							dualItemsSelected.set(false);
							dualSelectionFirstItem.set(Optional.empty());
							dualSelectionSecondItem.set(Optional.empty());
						}
					}
				}
			})
			.build());
		list.addSelectionListener(change ->
		{
			final boolean wasSingleSelected = singleItemSelected.get();
			final Optional<T> optionalOldSingleSelectedItem = singleSelectionItem.get();
			final int n = list.size();
			checkSingleSelection.run();
			final boolean isDualSelected = wasSingleSelected && change.getSelectionChangeType() == SINGLE_INCREMENT;
			final boolean isDualItem = n == 2;
			final boolean isDualMode = isDualSelected || isDualItem;
			dualItemsSelected.set(isDualMode);
			if (isDualMode)
			{
				if (isDualSelected)
				{
					dualSelectionFirstItem.set(optionalOldSingleSelectedItem);
					final var firstItem = optionalOldSingleSelectedItem.get();
					boolean found = false;
					for (int i = 0; i < n && !found; i++)
					{
						final T item = list.get(i);
						found = list.isSelected(i) && item != firstItem;
						if (found)
						{
							dualSelectionSecondItem.set(Optional.ofNullable(item));
						}
					}
					if (!found)
					{
						dualSelectionSecondItem.set(Optional.empty());
					}
				}
				else // n == 2
				{
					checkDualSelection.run();
				}
			}
			else
			{
				dualSelectionFirstItem.set(Optional.empty());
				dualSelectionSecondItem.set(Optional.empty());
			}
		});
	}

	ReadOnlyBooleanProperty singleItemSelectedProperty()
	{
		return singleItemSelected.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty dualItemsSelectedProperty()
	{
		return dualItemsSelected.getReadOnlyProperty();
	}

	ReadOnlyObjectProperty<Optional<T>> singleSelectionItemProperty()
	{
		return singleSelectionItem.getReadOnlyProperty();
	}

	ReadOnlyObjectProperty<Optional<T>> dualSelectionFirstItemProperty()
	{
		return dualSelectionFirstItem.getReadOnlyProperty();
	}

	ReadOnlyObjectProperty<Optional<T>> dualSelectionSecondItemProperty()
	{
		return dualSelectionSecondItem.getReadOnlyProperty();
	}
}
