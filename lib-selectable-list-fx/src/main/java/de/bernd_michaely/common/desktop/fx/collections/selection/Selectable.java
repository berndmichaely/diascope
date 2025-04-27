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

import static de.bernd_michaely.common.desktop.fx.collections.selection.Selectable.Action.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Interface to describe selection related actions of objects containing a list
 * of selectable items.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public interface Selectable
{
  /**
   * Enum to describe selection related actions.
   */
  enum Action
  {
    /**
     * Set selection status to true.
     */
    SELECTION_SET,
    /**
     * Set selection status to false.
     */
    SELECTION_UNSET,
    /**
     * Invert selection status.
     */
    SELECTION_TOGGLE
  }

  /**
   * Return the number of list items.
   *
   * @return the number of list items
   */
  int size();

  /**
   * Returns the source index of an index transformation for the given index.
   *
   * @param index the given index
   * @return the source index of an index transformation for the given index.
   *         The default implementation assumes the identity transformation,
   *         that is it returns the given index.
   */
  default int getSourceIndex(int index)
  {
    return index;
  }

  /**
   * Returns true, iff the item at the given index is selected.
   *
   * @param index the given index
   * @return true, iff the item at the given index is selected
   */
  boolean isSelected(int index);

  /**
   * Select the list items in the range <code>[from,&nbsp;to[</code> according
   * to the given action. The indices work like:
   * <pre><code>for (int i = from; i &lt; to; i++)</code></pre>
   *
   * @param from   the beginning of the index range (including)
   * @param to     the end of the index range (excluding)
   * @param action the selection action to apply, <code>null</code> is a no-op
   */
  void selectRange(int from, int to, Action action);

  /**
   * Select the list items in the closed range between <code>from</code> and
   * <code>to</code> with possibly <code>to&nbsp;&lt;&nbsp;from</code>. The
   * indices work like:
   * <pre><code>for (int i = min(from, to); i &le; max(from, to); i++)</code></pre>
   *
   * @param from   the beginning of the index range (including)
   * @param to     the end of the index range (including)
   * @param action the selection action to apply
   */
  default void selectRangeBidirectional(int from, int to, Action action)
  {
    selectRange(min(from, to), max(from, to) + 1, action);
  }

  /**
   * Select the list items in the closed range between <code>from</code> and
   * <code>to</code> with possibly <code>to&nbsp;&lt;&nbsp;from</code>. The
   * indices work like:
   * <pre><code>for (int i = min(from, to); i &le; max(from, to); i++)</code></pre>
   *
   * @param from  the beginning of the index range (including)
   * @param to    the end of the index range (including)
   * @param value the given value
   */
  default void selectRangeBidirectional(int from, int to, boolean value)
  {
    selectRangeBidirectional(from, to, value ? SELECTION_SET : SELECTION_UNSET);
  }

  /**
   * Set the selection status of item at index to value.
   *
   * @param index the given index
   * @param value the given value
   */
  default void setSelected(int index, boolean value)
  {
    selectRange(index, index + 1, value ? SELECTION_SET : SELECTION_UNSET);
  }

  /**
   * Set the selection status of item at index according to the given action.
   *
   * @param index  the given index
   * @param action the given action
   */
  default void select(int index, Action action)
  {
    selectRange(index, index + 1, action);
  }

  /**
   * Set the selection status of all list items according to the given action.
   *
   * @param action the given action
   */
  default void selectAll(Action action)
  {
    selectRange(0, size(), action);
  }

  /**
   * Set all items selected.
   */
  default void selectAll()
  {
    selectRange(0, size(), SELECTION_SET);
  }

  /**
   * Set all items unselected.
   */
  default void selectNone()
  {
    selectRange(0, size(), SELECTION_UNSET);
  }

  /**
   * Invert the selection status of all items.
   */
  default void invertSelection()
  {
    selectRange(0, size(), SELECTION_TOGGLE);
  }
}
