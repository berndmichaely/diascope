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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import static de.bernd_michaely.common.desktop.fx.collections.selection.SelectionChangeListener.SelectionChange.SelectionChangeType.*;
import static java.util.Collections.unmodifiableCollection;
import static javafx.beans.binding.Bindings.createBooleanBinding;

/// Class to handle a dual selection state of a SelectableList.
/// Dual selection has a specific meaning in this context. It is true, iff:
///
///   * exactly 2 elements are selected or
///   * the list contains exactly 2 elements.
///
/// In the latter case, if exactly one element is selected, then:
///
///   * the unselected list element is the first selected and
///   * the selected list element is the second selected.
///
/// and if none is selected:
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
	private final Deque<Integer> queueSelected;
	private final Collection<Integer> selectedIndices;

	ListDualSelection(SelectableList<T> list)
	{
		this.singleItemSelected = new ReadOnlyBooleanWrapper();
		this.singleSelectionItem = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.dualItemsSelected = new ReadOnlyBooleanWrapper();
		this.dualSelectionFirstItem = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.dualSelectionSecondItem = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.queueSelected = new ArrayDeque<>();
		this.selectedIndices = unmodifiableCollection(queueSelected);
		singleItemSelected.bind(singleSelectionItem.map(Optional::isPresent));
		dualItemsSelected.bind(createBooleanBinding(
			() -> dualSelectionFirstItem.get().isPresent() && dualSelectionSecondItem.get().isPresent(),
			dualSelectionFirstItem, dualSelectionSecondItem));
		final Runnable checkSelection = () ->
		{
			final int n = list.size();
			final int numSelected = list.getNumSelected();
			// check single selection:
			boolean selected = false;
			if (numSelected == 1)
			{
				for (int i = 0; i < n && !selected; i++)
				{
					selected = list.isSelected(i);
					if (selected)
					{
						singleSelectionItem.set(Optional.ofNullable(list.get(i)));
					}
				}
			}
			if (!selected)
			{
				singleSelectionItem.set(Optional.empty());
			}
			// check dual selection:
			final boolean isNumSelected_2 = numSelected == 2;
			boolean isDualItemsSelected = isNumSelected_2 || n == 2;
			if (isDualItemsSelected)
			{
				if (isNumSelected_2)
				{
					final Integer peekFirst = queueSelected.peekFirst();
					final int indexFirst = peekFirst != null ? peekFirst : -1;
					final Integer peekLast = queueSelected.peekLast();
					final int indexLast = peekLast != null ? peekLast : -1;
					if (indexFirst >= 0 && indexLast >= 0)
					{
						dualSelectionFirstItem.set(Optional.ofNullable(list.get(indexLast)));
						dualSelectionSecondItem.set(Optional.ofNullable(list.get(indexFirst)));
					}
					else
					{
						isDualItemsSelected = false;
					}
				}
				else // numSelected < 2
				{
					final Integer peekFirst = queueSelected.peekFirst();
					final int indexFirst = peekFirst != null ? peekFirst : -1;
					if (indexFirst >= 0 && list.get(indexFirst) == list.getFirst())
					{
						dualSelectionFirstItem.set(Optional.ofNullable(list.getLast()));
						dualSelectionSecondItem.set(Optional.ofNullable(list.getFirst()));
					}
					else
					{
						dualSelectionFirstItem.set(Optional.ofNullable(list.getFirst()));
						dualSelectionSecondItem.set(Optional.ofNullable(list.getLast()));
					}
				}
			}
			if (!isDualItemsSelected)
			{
				dualSelectionFirstItem.set(Optional.empty());
				dualSelectionSecondItem.set(Optional.empty());
			}
		};
		list.addListener(new ListChangeListenerBuilder<T>()
			.onAdd(_ -> checkSelection.run())
			.onRemove(change ->
			{
				final int from = change.getFrom();
				final int to = from + change.getRemovedSize();
				for (int i = from; i < to; i++)
				{
					queueSelected.remove(i);
				}
				checkSelection.run();
			})
			.build());
		list.addSelectionListener(change ->
		{
			final var selectionChangeType = change.getSelectionChangeType();
			if (selectionChangeType != null)
			{
				switch (selectionChangeType)
				{
					case SINGLE_INCREMENT ->
					{
						queueSelected.addFirst(change.getFrom());
					}
					case SINGLE_DECREMENT ->
					{
						queueSelected.remove(change.getFrom());
					}
					case COMPLEX_CHANGE ->
					{
						for (int i = change.getFrom(); i <= change.getTo(); i++)
						{
							if (list.isSelected(i))
							{
								queueSelected.addFirst(i);
							}
							else
							{
								queueSelected.remove(i);
							}
						}
					}
					default -> throw new AssertionError(getClass().getName() +
							": Invalid SelectionChangeType!");
				}
			}
			checkSelection.run();
		});
	}

	Collection<Integer> getSelectedIndices()
	{
		return selectedIndices;
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
