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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.common.desktop.fx.collections.selection.TransformationListUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for ListSelectionHandlerImplTest class.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ListSelectionHandlerImplTest
{
  @Test
  public void recapCommonFxBehaviour()
  {
    final ListProperty<Integer> listProperty = new SimpleListProperty<>();
    assertDoesNotThrow(() -> listProperty.set(null));
    assertEquals(0, listProperty.sizeProperty().get());
    assertEquals(0, listProperty.getSize());
    assertEquals(0, listProperty.size());
    assertTrue(listProperty.emptyProperty().get());
    assertTrue(listProperty.isEmpty());
    assertNull(listProperty.get());
    assertThrows(UnsupportedOperationException.class, () -> listProperty.add(7));
    listProperty.set(FXCollections.observableArrayList());
    assertDoesNotThrow(() -> listProperty.add(7));
  }

  @Test
  public void testTransformationChain()
  {
    System.out.println("testTransformationChain");
    final var list1 = SelectableListFactory.<Integer>selectableList();
    final var listSelectionHandler = SelectableListFactory.listSelectionHandler(list1);
    assertEquals(list1, listSelectionHandler.getHeadList());
    // check invalid transformation chain:
    final var list2 = SelectableListFactory.<Integer>selectableList();
    assertThrows(IllegalArgumentException.class, () ->
      SelectableListFactory.listSelectionHandler(list1,
        new FilteredList<>(list2, i -> i % 2 == 1)));
    assertThrows(IllegalArgumentException.class, () ->
      SelectableListFactory.listSelectionHandler(list1,
        new SortedList<>(new FilteredList<>(list2, i -> i % 2 == 1), Integer::compare)));
  }

  @Test
  public void testListSelectionHandlerImpl()
  {
    System.out.println("testListSelectionHandlerImpl");
    final var srcList = SelectableListFactory.<Integer>selectableList();
    final var listSelectionHandler = SelectableListFactory.listSelectionHandler(srcList);
    assertFalse(listSelectionHandler.isTransformed());
    assertEquals(srcList, listSelectionHandler.getHeadList());
    assertTrue(srcList.isEmpty());
    assertTrue(listSelectionHandler.emptyProperty().get());
    assertTrue(listSelectionHandler.isNoneSelected());
    assertTrue(listSelectionHandler.isAllSelected());
    int counter = 0;
    assertEquals(counter, listSelectionHandler.getNumSelected());

    for (int i = 10; i >= 0; i--)
    {
      srcList.add(i);
    }
    for (int i = 9; i >= 1; i -= 2)
    {
      srcList.setSelected(i, true);
      assertEquals(++counter, listSelectionHandler.getNumSelected());
      assertFalse(listSelectionHandler.isNoneSelected());
      assertFalse(listSelectionHandler.isAllSelected());
    }
    for (int i = 0; i < srcList.size(); i++)
    {
      assertEquals(i, listSelectionHandler.getSourceIndex(i));
    }
    System.out.println("Source   list : " + srcList);
    System.out.println("Filtered list : " + listSelectionHandler);
  }

  @Test
  public void testFactoryMethodCollection()
  {
    System.out.println("testFactoryMethodCollection");
    final var srcList = SelectableListFactory.<Integer>selectableList();
    for (int i = 10; i >= 5; i--)
    {
      srcList.add(i);
    }
    assertEquals(srcList,
      SelectableListFactory.selectableList(List.of(10, 9, 8, 7, 6, 5)));
  }

  @Test
  public void testListSelectionHandlerImplFiltered()
  {
    System.out.println("testListSelectionHandlerImplFiltered");
    final var srcList = SelectableListFactory.<Integer>selectableList();
    final var filteredList = new FilteredList<>(srcList, i -> i % 2 == 1);
    final var listSelectionHandler = SelectableListFactory.listSelectionHandler(srcList, filteredList);
    assertTrue(listSelectionHandler.isTransformed());
    assertEquals(filteredList, listSelectionHandler.getHeadList());
    assertTrue(listSelectionHandler.isNoneSelected());
    assertTrue(listSelectionHandler.isAllSelected());
    int counter = 0;
    assertEquals(counter, listSelectionHandler.getNumSelected());

    for (int i = 10; i >= 0; i--)
    {
      srcList.add(i);
    }
    // [10,9,8,7,6,5,4,3,2,1,0] -> filter -> [9,7,5,3,1]

    assertEquals(11, listSelectionHandler.getSourceList().size());
    assertEquals(5, listSelectionHandler.getHeadList().size());
    assertTrue(listSelectionHandler.isNoneSelected());
    assertFalse(listSelectionHandler.isAllSelected());

    for (int i = 9; i >= 1; i -= 2)
    {
      srcList.setSelected(i, true);
      assertEquals(++counter, listSelectionHandler.getNumSelected());
      assertFalse(listSelectionHandler.isNoneSelected());
      assertEquals(counter == 5, listSelectionHandler.isAllSelected());
    }
    for (int i = 0; i < listSelectionHandler.getHeadList().size(); i++)
    {
      assertEquals(9 - 2 * i, listSelectionHandler.get(i));
      assertEquals(9 - 2 * i, listSelectionHandler.getHeadList().get(i));
      assertEquals(1 + 2 * i, listSelectionHandler.getSourceIndex(i));
      assertEquals(i, getViewIndexFor(srcList, filteredList, listSelectionHandler.getSourceIndex(i)));
    }
    System.out.println("Source   list : " + srcList);
    System.out.println("Filtered list : " + listSelectionHandler);
  }

  @Test
  public void testListSelectionHandlerImplFilteredSorted()
  {
    System.out.println("testListSelectionHandlerImplFilteredSorted");
    final var srcList = SelectableListFactory.<Integer>selectableList();
    final var sortedList = new SortedList<>(
      new FilteredList<>(srcList, i -> i % 2 == 1), Integer::compare);
    final var listSelectionHandler = SelectableListFactory.listSelectionHandler(srcList, sortedList);
    assertTrue(listSelectionHandler.isTransformed());
    assertEquals(sortedList, listSelectionHandler.getHeadList());
    assertTrue(listSelectionHandler.isNoneSelected());
    assertTrue(listSelectionHandler.isAllSelected());
    int counter = 0;
    assertEquals(counter, listSelectionHandler.getNumSelected());

    for (int i = 10; i >= 0; i--)
    {
      srcList.add(i);
    }
    // [10,9,8,7,6,5,4,3,2,1,0] -> filter -> [9,7,5,3,1] -> sort -> [1,3,5,7,9]

    assertEquals(11, listSelectionHandler.getSourceList().size());
    assertEquals(5, listSelectionHandler.getHeadList().size());
    assertTrue(listSelectionHandler.isNoneSelected());
    assertFalse(listSelectionHandler.isAllSelected());

    for (int i = 9; i >= 1; i -= 2)
    {
      srcList.setSelected(i, true);
      assertEquals(++counter, listSelectionHandler.getNumSelected());
      assertFalse(listSelectionHandler.isNoneSelected());
      assertEquals(counter == 5, listSelectionHandler.isAllSelected());
    }
    for (int i = 0; i < listSelectionHandler.getHeadList().size(); i++)
    {
      assertEquals(1 + 2 * i, listSelectionHandler.get(i));
      assertEquals(1 + 2 * i, listSelectionHandler.getHeadList().get(i));
      assertEquals(9 - 2 * i, listSelectionHandler.getSourceIndex(i));
      assertEquals(i, getViewIndexFor(srcList, sortedList, listSelectionHandler.getSourceIndex(i)));
    }
    System.out.println("Source   list : " + srcList);
    System.out.println("Filtered list : " + listSelectionHandler);
  }

  @Test
  public void testSelect()
  {
    System.out.println("testSelect");
    final var sourceList = SelectableListFactory.<Integer>selectableList();
    final var transformList = new SortedList<>(
      new FilteredList<>(sourceList, i -> i % 2 == 1), Integer::compare);
    final var lshSource = SelectableListFactory.listSelectionHandler(sourceList);
    final var lshTransform = SelectableListFactory.listSelectionHandler(sourceList, transformList);

    for (int i = 10; i >= 0; i--)
    {
      sourceList.add(i);
    }
    // [10,9,8,7,6,5,4,3,2,1,0] -> filter -> [9,7,5,3,1] -> sort -> [1,3,5,7,9]

    assertTrue(lshSource.isNoneSelected());
    assertFalse(lshSource.isAllSelected());
    assertEquals(0, lshSource.getNumSelected());
    assertTrue(lshTransform.isNoneSelected());
    assertFalse(lshTransform.isAllSelected());
    assertEquals(0, lshTransform.getNumSelected());

    lshTransform.selectAll();

    for (int i = 0; i < sourceList.size(); i += 2)
    {
      assertFalse(lshSource.isSelected(i));
    }
    for (int i = 1; i < sourceList.size(); i += 2)
    {
      assertTrue(lshSource.isSelected(i));
    }

    assertFalse(lshSource.isNoneSelected());
    assertFalse(lshSource.isAllSelected());
    assertEquals(5, lshSource.getNumSelected());
    assertFalse(lshTransform.isNoneSelected());
    assertTrue(lshTransform.isAllSelected());
    assertEquals(5, lshTransform.getNumSelected());

    lshSource.invertSelection();

    assertFalse(lshSource.isNoneSelected());
    assertFalse(lshSource.isAllSelected());
    assertEquals(6, lshSource.getNumSelected());
    assertTrue(lshTransform.isNoneSelected());
    assertFalse(lshTransform.isAllSelected());
    assertEquals(0, lshTransform.getNumSelected());

    for (int i = 0; i < sourceList.size(); i += 2)
    {
      assertTrue(lshSource.isSelected(i));
    }
    for (int i = 1; i < sourceList.size(); i += 2)
    {
      assertFalse(lshSource.isSelected(i));
    }

    lshTransform.selectAll();
    assertTrue(lshSource.isAllSelected());
    assertTrue(lshTransform.isAllSelected());

    lshTransform.invertSelection();

    assertFalse(lshSource.isNoneSelected());
    assertFalse(lshSource.isAllSelected());
    assertTrue(lshTransform.isNoneSelected());
    assertFalse(lshTransform.isAllSelected());

    lshSource.invertSelection();
    lshTransform.invertSelection();

    assertTrue(lshSource.isNoneSelected());
    assertFalse(lshSource.isAllSelected());
    assertTrue(lshTransform.isNoneSelected());
    assertFalse(lshTransform.isAllSelected());

    System.out.println("Source   list : " + sourceList);
    System.out.println("Filtered list : " + lshTransform);
  }

  @Test
  public void testStream()
  {
    System.out.println("testStream");
    final var sourceList = SelectableListFactory.<Integer>selectableList();
    final var transformList = new SortedList<>(
      new FilteredList<>(sourceList, i -> i % 2 == 1), Integer::compare);
    final var lshSource = SelectableListFactory.listSelectionHandler(sourceList);
    final var lshTransform = SelectableListFactory.listSelectionHandler(sourceList, transformList);

    for (int i = 10; i >= 0; i--)
    {
      sourceList.add(i);
    }
    // [10,9,8,7,6,5,4,3,2,1,0] -> filter -> [9,7,5,3,1] -> sort -> [1,3,5,7,9]

    // select indices [2,3,4,7]:
    sourceList.setSelected(7, true);
    sourceList.selectRange(2, 5, Selectable.Action.SELECTION_SET);

    assertEquals(List.of(8, 7, 6, 3), lshSource.streamSelected().toList());
    assertEquals(List.of(10, 9, 5, 4, 2, 1, 0), lshSource.streamUnselected().toList());
    assertEquals(List.of(3, 7), lshTransform.streamSelected().toList());
    assertEquals(List.of(1, 5, 9), lshTransform.streamUnselected().toList());
  }

  @Test
  public void testStreamNull()
  {
    System.out.println("testStreamNull");
    final var sourceList = SelectableListFactory.<String>selectableList();
    final var transformList = new SortedList<>(sourceList, Comparator.nullsLast(String::compareTo));
    final var lshSource = SelectableListFactory.listSelectionHandler(sourceList);
    final var lshTransform = SelectableListFactory.listSelectionHandler(sourceList, transformList);
    sourceList.addAll("one", null, "two", null, "three");
    final var listExpected = new ArrayList<String>();
    listExpected.add("one");
    listExpected.add(null);
    listExpected.add("two");
    listExpected.add(null);
    listExpected.add("three");
    assertEquals(listExpected, sourceList.stream().toList());
    assertEquals(listExpected, lshSource.streamUnselected().toList());
    assertEquals(List.of(), lshSource.streamSelected().toList());
    sourceList.selectAll();
    assertEquals(List.of(), lshSource.streamUnselected().toList());
    assertEquals(listExpected, lshSource.streamSelected().toList());
    // sorted:
    sourceList.selectNone();
    listExpected.clear();
    listExpected.add("one");
    listExpected.add("three");
    listExpected.add("two");
    listExpected.add(null);
    listExpected.add(null);
    assertEquals(listExpected, transformList.stream().toList());
    assertEquals(listExpected, lshTransform.streamUnselected().toList());
    assertEquals(List.of(), lshTransform.streamSelected().toList());
    sourceList.selectAll();
    assertEquals(List.of(), lshTransform.streamUnselected().toList());
    assertEquals(listExpected, lshTransform.streamSelected().toList());
  }
}
