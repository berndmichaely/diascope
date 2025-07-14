/*
 * Copyright 2021 Bernd Michaely (info@bernd-michaely.de).
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import javafx.collections.ModifiableObservableListBase;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Default implementation of the SelectableList interface. This package local
 * implementation should be instantiated by the {@link CommonFXCollections}
 * factory class only.
 *
 * @param <E> the list element type
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class SelectableListImpl<E> extends ModifiableObservableListBase<E>
	implements SelectableList<E>, RandomAccess
{
	/**
	 * Class to encapsulate a list item with a selection state.
	 *
	 * @param <E> the list element type
	 */
	private static class SelectableItem<E>
	{
		private E item;
		private boolean selected;

		private SelectableItem(E item)
		{
			this.item = item;
		}

		@Override
		public boolean equals(@Nullable Object object)
		{
			if (object instanceof SelectableItem other)
			{
				return Objects.equals(this.item, other.item);
			}
			else
			{
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(item);
		}

//		private static final char CHAR_SELECTED = '✓';
//		private static final char CHAR_UNSELECTED = '✕';
		private static final char CHAR_SELECTED = '+';
		private static final char CHAR_UNSELECTED = '-';

		@Override
		public String toString()
		{
			return "»" + item + "«" + (selected ? CHAR_SELECTED : CHAR_UNSELECTED);
		}
	}

	private final List<SelectableItem<E>> delegate;
	private @MonotonicNonNull SelectionControl<E> selectionControl;

	SelectableListImpl()
	{
		this.delegate = new ArrayList<>();
	}

	private SelectionControl<E> getSelectionControl()
	{
		if (selectionControl == null)
		{
			selectionControl = new SelectionControl<>(new WeakReference<>(this));
		}
		return selectionControl;
	}

	@Override
	public void beginSelectionChange()
	{
		getSelectionControl().beginSelectionChange();
	}

	@Override
	public void endSelectionChange()
	{
		getSelectionControl().endSelectionChange();
	}

	@Override
	public E get(int index)
	{
		return delegate.get(index).item;
	}

	@Override
	protected void doAdd(int index, E item)
	{
		delegate.add(index, new SelectableItem<>(item));
	}

	@Override
	protected E doRemove(int index)
	{
		final SelectableItem<E> selectableItem = delegate.get(index);
		if (selectableItem.selected)
		{
			getSelectionControl().decrementSelectionCounter(index);
		}
		return delegate.remove(index).item;
	}

	@Override
	protected E doSet(int index, E item)
	{
		final SelectableItem<E> selectableItem = delegate.get(index);
		final E oldValue = selectableItem.item;
		selectableItem.item = item;
		return oldValue;
	}

	@Override
	public int size()
	{
		return delegate.size();
	}

	@Override
	public boolean isSelected(int index)
	{
		return delegate.get(index).selected;
	}

	@Override
	public void selectRange(int from, int to, Action action)
	{
		if (from < to && action != null)
		{
			beginSelectionChange();
			try
			{
				for (int i = from; i < to; i++)
				{
					final SelectableItem<E> item = delegate.get(i);
					final boolean oldValue = item.selected;
					final boolean newValue;
					switch (action)
					{
						case SELECTION_SET -> newValue = true;
						case SELECTION_UNSET -> newValue = false;
						case SELECTION_TOGGLE -> newValue = !oldValue;
						default -> throw new IllegalStateException("" + action);
					}
					if (oldValue != newValue)
					{
						item.selected = newValue;
						if (newValue)
						{
							getSelectionControl().incrementSelectionCounter(i);
						}
						else
						{
							getSelectionControl().decrementSelectionCounter(i);
						}
					}
				}
			}
			finally
			{
				endSelectionChange();
			}
		}
	}

	@Override
	public int getNumSelected()
	{
		return getSelectionControl().getSelectionCounter();
	}

	@Override
	public void addSelectionListener(SelectionChangeListener<? super E> selectionChangeListener,
		boolean requestSingleChangeEvents)
	{
		getSelectionControl().addSelectionListener(selectionChangeListener, requestSingleChangeEvents);
	}

	@Override
	public boolean removeSelectionListener(SelectionChangeListener<? super E> selectionChangeListener)
	{
		return getSelectionControl().removeSelectionListener(selectionChangeListener);
	}

	@Override
	public String toString()
	{
		return delegate.toString();
	}
}
