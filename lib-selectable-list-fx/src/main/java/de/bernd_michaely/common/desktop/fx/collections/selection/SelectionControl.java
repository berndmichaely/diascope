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

import de.bernd_michaely.common.desktop.fx.collections.selection.SelectionChangeListener.SelectionChange;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Internal class to control the current count of selected list items and the
 * corresponding event handling. All {@link SelectableItem}s and the
 * {@link SelectableList} itself keep a reference to one instance of this class
 * to communicate through it. The {@link SelectableList} is accessed through a
 * {@link  WeakReference} to avoid memory leaks. The counting is done in
 * {@code O(1)} time.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class SelectionControl<E>
{
  private final WeakReference<SelectableList<E>> selectableList;
  private final IdentityHashMap<SelectionChangeListener<? super E>, Boolean> selectionChangeListeners;
  private int stackCounter;
  private int selectionCounter;
  private @Nullable SelectionChange<E> selectionChange;

  SelectionControl(WeakReference<SelectableList<E>> selectableList)
  {
    this.selectableList = requireNonNull(selectableList, "SelectableList is null");
    this.selectionChangeListeners = new IdentityHashMap<>();
  }

  int getSelectionCounter()
  {
    return selectionCounter;
  }

  void incrementSelectionCounter(int indexChanged)
  {
    beginSelectionChange();
    try
    {
      selectionCounter++;
      if (selectionChange != null)
      {
        selectionChange.addIndex(indexChanged, true);
      }
      else
      {
        throw new IllegalStateException(getClass().getName() +
          "#incrementSelectionCounter : invalid access to »selectionChange«");
      }
      notifySingleChangeRequestListeners(indexChanged, true);
    }
    finally
    {
      endSelectionChange();
    }
  }

  void decrementSelectionCounter(int indexChanged)
  {
    if (selectionCounter > 0)
    {
      beginSelectionChange();
      try
      {
        selectionCounter--;
        if (selectionChange != null)
        {
          selectionChange.addIndex(indexChanged, false);
        }
        else
        {
          throw new IllegalStateException(getClass().getName() +
            "#decrementSelectionCounter : invalid access to »selectionChange«");
        }
        notifySingleChangeRequestListeners(indexChanged, false);
      }
      finally
      {
        endSelectionChange();
      }
    }
    else
    {
      throw new IllegalStateException(getClass().getName() +
        "#decrementSelectionCounter : invalid call");
    }
  }

  private void notifySingleChangeRequestListeners(int index, boolean selected)
  {
    selectionChangeListeners.keySet().stream()
      .filter(key ->
      {
        @KeyFor("selectionChangeListeners") SelectionChangeListener<? super E> listener = key;
        return selectionChangeListeners.get(listener);
      })
      .forEach(listener ->
      {
        final SelectionChange<E> change = new SelectionChange<>(selectableList);
        change.addIndex(index, selected);
        listener.onSelectionChanged(change);
      });
  }

  void beginSelectionChange()
  {
    if (stackCounter < 0)
    {
      throw new IllegalStateException(getClass().getName() +
        "#beginSelectionChange : invalid stack counter");
    }
    if (++stackCounter == 1)
    {
      selectionChange = new SelectionChange<>(selectableList);
    }
  }

  void endSelectionChange()
  {
    if (--stackCounter >= 0)
    {
      if (stackCounter == 0)
      {
        try
        {
          if (selectionChange != null)
          {
            final @NonNull SelectionChange<E> change = selectionChange;
            selectionChangeListeners.keySet().stream()
              .filter(key ->
              {
                @KeyFor("selectionChangeListeners") SelectionChangeListener<? super E> listener = key;
                return !selectionChangeListeners.get(listener);
              })
              .forEach(listener -> listener.onSelectionChanged(change));
          }
          else
          {
            throw new IllegalStateException("Invalid call of notifyListeners()");
          }
        }
        finally
        {
          selectionChange = null;
        }
      }
    }
    else
    {
      throw new IllegalStateException("Invalid call of endSelectionChange()");
    }
  }

  /**
   * Adds the given listener.
   *
   * @param selectionChangeListener the listener to add
   * @throws NullPointerException if the given listener is null
   */
  void addSelectionListener(SelectionChangeListener<? super E> selectionChangeListener,
    boolean requestSingleChangeEvents)
  {
    selectionChangeListeners.put(requireNonNull(selectionChangeListener), requestSingleChangeEvents);
  }

  /**
   * Removes the given listener.
   *
   * @param selectionChangeListener the listener to remove
   * @return true, iff the listener was present before
   * @throws NullPointerException if the given listener is null
   */
  boolean removeSelectionListener(SelectionChangeListener<? super E> selectionChangeListener)
  {
    return selectionChangeListeners.remove(requireNonNull(selectionChangeListener)) != null;
  }
}
