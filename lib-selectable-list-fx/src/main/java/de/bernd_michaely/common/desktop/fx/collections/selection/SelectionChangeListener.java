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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

/**
 * Interface that receives notifications of selection changes to a
 * {@link SelectableList}.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 * @param <E> the list element type
 */
@FunctionalInterface
public interface SelectionChangeListener<E>
{
	/**
	 * Contains information about a selection change to a {@link SelectableList}.
	 *
	 * @param <E> the list element type
	 */
	public static class SelectionChange<E>
	{
		/**
		 * Enum to describe details about the kind of selection change.
		 */
		public static enum SelectionChangeType
		{
			/**
			 * A single list item has been changed from unselected to selected state.
			 */
			SINGLE_INCREMENT(true),
			/**
			 * A single list item has been changed from selected to unselected state.
			 */
			SINGLE_DECREMENT(true),
			/**
			 * A complex selection change has happened, changing the selection state
			 * of more than one item.
			 */
			COMPLEX_CHANGE(false);
			private final boolean singleChange;

			/**
			 * Returns true, if the change is a single change.
			 *
			 * @return true, if the change is a single change
			 */
			public boolean isSingleChange()
			{
				return singleChange;
			}

			SelectionChangeType(boolean singleChange)
			{
				this.singleChange = singleChange;
			}
		}

		private final WeakReference<SelectableList<E>> selectableList;
		private int from = -1, to = -1;
		private @MonotonicNonNull SelectionChangeType selectionChangeType;

		/**
		 * Creates an instance for a given list.
		 *
		 * @param selectableList the given list
		 * @throws NullPointerException if selectableList is null
		 */
		SelectionChange(WeakReference<SelectableList<E>> selectableList)
		{
			this.selectableList = requireNonNull(selectableList,
				getClass().getName() + " : SelectableList is null");
		}

		/**
		 * Extends the selection range to include the given index.
		 *
		 * @param index    the given index
		 * @param selected the given selection state
		 * @throws IndexOutOfBoundsException if index is invalid
		 */
		void addIndex(int index, boolean selected)
		{
			if (index < 0)
			{
				throw new IndexOutOfBoundsException(index);
			}
			final var list = getList();
			if (list != null && index >= list.size())
			{
				throw new IndexOutOfBoundsException(index);
			}
			from = from < 0 ? index : min(from, index);
			to = to < 0 ? index : max(to, index);
			if (selectionChangeType == null)
			{
				if (selected)
				{
					selectionChangeType = SelectionChangeType.SINGLE_INCREMENT;
				}
				else
				{
					selectionChangeType = SelectionChangeType.SINGLE_DECREMENT;
				}
			}
			else
			{
				selectionChangeType = SelectionChangeType.COMPLEX_CHANGE;
			}
		}

		/**
		 * Returns the source list.
		 *
		 * @return the source list
		 */
		public @Nullable
		SelectableList<E> getList()
		{
			return selectableList.get();
		}

		/**
		 * Returns the begin of the selection range.
		 *
		 * @return the begin of the selection range
		 */
		public int getFrom()
		{
			return from;
		}

		/**
		 * Returns the end of the selection range.
		 *
		 * @return the end of the selection range
		 */
		public int getTo()
		{
			return to;
		}

		/**
		 * Returns true, iff the selection range is empty.
		 *
		 * @return true, iff the selection range is empty
		 */
		public boolean isEmptyRange()
		{
			return from < 0 || to < 0;
		}

		/**
		 * Returns the type of selection change.
		 *
		 * @return the type of selection change
		 */
		public @Nullable
		SelectionChangeType getSelectionChangeType()
		{
			return selectionChangeType;
		}

		@Override
		public String toString()
		{
			return isEmptyRange() ? getClass().getSimpleName() + "[]" :
				String.format("%s[%d,%d]", getClass().getSimpleName(), getFrom(), getTo());
		}
	}

	/**
	 * Called after a selection change has been made to the
	 * {@link SelectableList}.
	 *
	 * @param change object to describe details about the change
	 */
	void onSelectionChanged(SelectionChange<? extends E> change);
}
