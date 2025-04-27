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

import java.util.List;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.TransformationList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for SelectableListFactory.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SelectableListFactoryTest
{
  /**
   * Test of selectableList method, of class SelectableListFactory.
   */
  @Test
  public void testSelectableList()
  {
    System.out.println("selectableList()");
    assertNotNull(SelectableListFactory.selectableList());
  }

  /**
   * Test of selectableList method, of class SelectableListFactory.
   */
  @Test
  public void testSelectableList_Collection()
  {
    System.out.println("selectableList(null)");
    assertThrows(NullPointerException.class,
      () -> SelectableListFactory.selectableList(null));

    System.out.println("selectableList(src)");
    final List<? extends Number> src = List.of(3, 2, 1);
    final SelectableList<? extends Number> selectableList = SelectableListFactory.selectableList(src);
    assertNotNull(selectableList);
    assertEquals(3, selectableList.size());
    assertEquals(2, selectableList.get(1));
  }

  /**
   * Test of listSelectionHandler method, of class SelectableListFactory.
   */
  @Test
  public void testListSelectionHandler_SelectableList()
  {
    System.out.println("listSelectionHandler(null)");
    assertThrows(NullPointerException.class,
      () -> SelectableListFactory.listSelectionHandler(null));

    System.out.println("listSelectionHandler(src)");
    final List<Integer> list = List.of(1, 2, 3);
    final SelectableList<Integer> src = SelectableListFactory.selectableList(list);
    assertNotNull(SelectableListFactory.listSelectionHandler(src));
  }

  /**
   * Test of listSelectionHandler method, of class SelectableListFactory.
   */
  @Test
  public void testListSelectionHandler_SelectableList_TransformationList()
  {
    final List<Integer> list = List.of(1, 2, 3);
    final SelectableList<Integer> src = SelectableListFactory.selectableList(list);
    final TransformationList<Integer, Integer> transformationList =
      new FilteredList<>(src);
    System.out.println("listSelectionHandler(null, null)");
    assertThrows(NullPointerException.class,
      () -> SelectableListFactory.listSelectionHandler(null, null));

    System.out.println("listSelectionHandler(null, transformationList)");
    assertThrows(NullPointerException.class,
      () -> SelectableListFactory.listSelectionHandler(null, transformationList));

    System.out.println("listSelectionHandler(src, null)");
    assertNotNull(SelectableListFactory.listSelectionHandler(src, null));

    System.out.println("listSelectionHandler(src, transformationList)");
    assertNotNull(SelectableListFactory.listSelectionHandler(src, transformationList));
  }
}
