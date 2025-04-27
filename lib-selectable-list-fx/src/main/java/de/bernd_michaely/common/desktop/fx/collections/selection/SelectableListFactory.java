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

import java.util.Collection;
import javafx.collections.transformation.TransformationList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class with factory methods for creation of instances of SelectableLists and
 * related types.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SelectableListFactory
{
  /**
   * Private constructor, this class contains static factory methods only.
   */
  private SelectableListFactory()
  {
  }

  /**
   * Returns a SelectableList implementation.
   *
   * @param <E> the list element type
   * @return a SelectableList instance
   */
  public static <E> SelectableList<E> selectableList()
  {
    return new SelectableListImpl<>();
  }

  /**
   * Returns a SelectableList implementation.
   *
   * @param <E> the list element type
   * @param src a given collection the list is initially filled with
   * @return a SelectableList instance
   * @throws NullPointerException if src is null
   */
  public static <E> SelectableList<E> selectableList(Collection<? extends E> src)
  {
    final SelectableList<E> selectableList = new SelectableListImpl<>();
    selectableList.addAll(src);
    return selectableList;
  }

  /**
   * Returns a SelectableListSelectionHandler implementation.
   *
   * @param <E>        the list element type
   * @param sourceList a given list the returned instance is based on
   * @return a SelectableListSelectionHandler instance
   * @throws NullPointerException if sourceList is null
   */
  public static <E> ListSelectionHandler<E> listSelectionHandler(SelectableList<E> sourceList)
  {
    return listSelectionHandler(sourceList, null);
  }

  /**
   * Returns a SelectableListSelectionHandler implementation.
   *
   * @param <E>                the list element type
   * @param sourceList         a given list the returned instance is based on
   * @param transformationList a transformation list (or a chain thereof) to be
   *                           applied to the source list
   * @return a ListSelectionHandler instance
   * @throws NullPointerException if sourceList is null
   */
  public static <E> ListSelectionHandler<E> listSelectionHandler(SelectableList<E> sourceList,
    @Nullable TransformationList<E, E> transformationList)
  {
    return ListSelectionHandlerImpl.createInstance(sourceList, transformationList);
  }
}
