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

import javafx.collections.ObservableList;

/**
 * Interface describing a list with individually selectable items.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 * @param <E> the list element type
 * @see SelectableListFactory
 */
public interface SelectableList<E> extends Selectable, ObservableList<E>
{
	/**
	 * Starts a series of selections. The call of this method must be paired with
	 * a call to {@link #endSelectionChange()}, ideally inside a
	 * try-finally-block. Selection changes may be nested.
	 *
	 * @see SelectionChangeListener
	 */
	void beginSelectionChange();

	/**
	 * Ends a series of selections.
	 *
	 * @see #beginSelectionChange()
	 * @see SelectionChangeListener
	 * @throws IllegalStateException if this method is not called properly paired
	 *                               with {@link #beginSelectionChange()}
	 */
	void endSelectionChange();

	/**
	 * Returns the number of selected items.
	 *
	 * @return the number of selected items
	 */
	int getNumSelected();

	/**
	 * Adds a given selection listener. Selection changes of index ranges will be
	 * combined into a single change event.
	 *
	 * @param selectionChangeListener the given selection listener
	 * @throws NullPointerException if the given listener is null
	 */
	default void addSelectionListener(SelectionChangeListener<? super E> selectionChangeListener)
	{
		addSelectionListener(selectionChangeListener, false);
	}

	/**
	 * Adds a given selection listener.
	 *
	 * @param selectionChangeListener   the given selection listener
	 * @param requestSingleChangeEvents if true, a change event will be generated
	 *                                  for each single selection change, e.g.
	 *                                  range selections will not be optimized to
	 *                                  a single [from,to] range change
	 * @throws NullPointerException if the given listener is null
	 */
	void addSelectionListener(SelectionChangeListener<? super E> selectionChangeListener,
		boolean requestSingleChangeEvents);

	/**
	 * Removes a given selection listener.
	 *
	 * @param selectionChangeListener the given selection listener
	 * @return true, iff the listener was present before
	 * @throws NullPointerException if the given listener is null
	 */
	boolean removeSelectionListener(SelectionChangeListener<? super E> selectionChangeListener);
}
