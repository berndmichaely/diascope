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

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.common.desktop.fx.collections.selection.TransformationListUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for TransformationListUtil class.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class TransformationListUtilTest
{
	@Test
	public void test_FilteredList_getViewIndex()
	{
		System.out.println("test_FilteredList_getViewIndex");
		final var sourceList = FXCollections.observableArrayList(6, 5, 4, 3, 2, 1, 0);
		final var filteredList = new FilteredList<>(sourceList, i -> i % 2 == 1);
		System.out.println("Source    list : " + sourceList);
		System.out.println("Filtered  list : " + filteredList);
		assertEquals(0, filteredList.getViewIndex(1));
		assertEquals(1, filteredList.getViewIndex(3));
		assertEquals(2, filteredList.getViewIndex(5));
		assertTrue(filteredList.getViewIndex(0) < 0);
		assertTrue(filteredList.getViewIndex(2) < 0);
		assertTrue(filteredList.getViewIndex(4) < 0);
		assertTrue(filteredList.getViewIndex(6) < 0);
		for (int i = 7; i < 100; i++)
		{
			final int k = i;
			assertThrows(IndexOutOfBoundsException.class, () -> filteredList.getViewIndex(k));
		}
	}

	@Test
	public void test_SortedList_getViewIndex()
	{
		System.out.println("test_SortedList_getViewIndex");
		final var sourceList = FXCollections.observableArrayList(4, 1, 3, 2);
		final var sortedList = new SortedList<>(sourceList, Integer::compare);
		System.out.println("Source    list : " + sourceList);
		System.out.println("Sorted    list : " + sortedList);
		assertEquals(3, sortedList.getViewIndex(0));
		assertEquals(0, sortedList.getViewIndex(1));
		assertEquals(2, sortedList.getViewIndex(2));
		assertEquals(1, sortedList.getViewIndex(3));
		for (int i = 0; i < 10; i++)
		{
			try
			{
				System.out.println("sortedList.getViewIndex(" + i + ") = " + sortedList.getViewIndex(i));
			}
			catch (IndexOutOfBoundsException ex)
			{
				System.out.println("JavaFX bug : " + ex.getMessage());
			}
		}
		// the following tests fail in OpenJavaFX 16:
//		assertTrue(sortedList.getViewIndex(4) < 0);
//		assertTrue(sortedList.getViewIndex(5) < 0);
//		assertTrue(sortedList.getViewIndex(6) < 0);
//		assertTrue(sortedList.getViewIndex(7) < 0);
//		assertTrue(sortedList.getViewIndex(8) < 0);
//		assertTrue(sortedList.getViewIndex(9) < 0);
		// -> Java bug database -> internal review ID (2021-07-25) : 9071017
	}

	@Test
	public void testSrcToTransformIndex0()
	{
		System.out.println("testSrcToTransformIndex0");
		final var transformList = new FilteredList<>(FXCollections.observableArrayList(3, 2, 1, 0));
		System.out.println("Source    list : " + transformList.getSource());
		assertEquals(0, getViewIndexFor(transformList, transformList, 0));
		assertEquals(1, getViewIndexFor(transformList, transformList, 1));
		assertEquals(2, getViewIndexFor(transformList, transformList, 2));
		assertEquals(3, getViewIndexFor(transformList, transformList, 3));
		assertTrue(getViewIndexFor(transformList, transformList, 4) < 0);
		assertTrue(getViewIndexFor(transformList, transformList, 1000) < 0);
		assertTrue(getViewIndexFor(transformList, transformList, -1) < 0);
		assertTrue(getViewIndexFor(transformList, null, -1) < 0);
		assertTrue(getViewIndexFor(null, transformList, -1) < 0);
		assertTrue(getViewIndexFor(null, null, -1) < 0);
		// same with convenience method:
		assertEquals(0, getViewIndexFor(transformList, 0));
		assertEquals(1, getViewIndexFor(transformList, 1));
		assertEquals(2, getViewIndexFor(transformList, 2));
		assertEquals(3, getViewIndexFor(transformList, 3));
		assertTrue(getViewIndexFor(transformList, 4) < 0);
		assertTrue(getViewIndexFor(transformList, 1000) < 0);
		assertTrue(getViewIndexFor(transformList, -1) < 0);
		assertTrue(getViewIndexFor(null, -1) < 0);
	}

	@Test
	public void testSrcToTransformIndex1_1()
	{
		System.out.println("testSrcToTransformIndex1_1");
		final var sourceList = FXCollections.<Integer>observableArrayList();
		final var transformList = new FilteredList<>(sourceList, i -> i % 2 == 1);
		for (int i = 10; i >= 0; i--)
		{
			sourceList.add(i);
		}
		System.out.println("Source    list : " + sourceList);
		System.out.println("Transform list : " + transformList);
		// [10,9,8,7,6,5,4,3,2,1,0] -> filter -> [9,7,5,3,1]
		assertTrue(getViewIndexFor(sourceList, transformList, 0) < 0);
		assertEquals(0, getViewIndexFor(sourceList, transformList, 1));
		assertTrue(getViewIndexFor(sourceList, transformList, 2) < 0);
		assertEquals(1, getViewIndexFor(sourceList, transformList, 3));
		assertTrue(getViewIndexFor(sourceList, transformList, 4) < 0);
		assertEquals(2, getViewIndexFor(sourceList, transformList, 5));
		assertTrue(getViewIndexFor(sourceList, transformList, 6) < 0);
		assertEquals(3, getViewIndexFor(sourceList, transformList, 7));
		assertTrue(getViewIndexFor(sourceList, transformList, 8) < 0);
		assertEquals(4, getViewIndexFor(sourceList, transformList, 9));
		assertTrue(getViewIndexFor(sourceList, transformList, 10) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, 11) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, 1000) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, -1) < 0);
		// same with convenience method:
		assertTrue(getViewIndexFor(transformList, 0) < 0);
		assertEquals(0, getViewIndexFor(transformList, 1));
		assertTrue(getViewIndexFor(transformList, 2) < 0);
		assertEquals(1, getViewIndexFor(transformList, 3));
		assertTrue(getViewIndexFor(transformList, 4) < 0);
		assertEquals(2, getViewIndexFor(transformList, 5));
		assertTrue(getViewIndexFor(transformList, 6) < 0);
		assertEquals(3, getViewIndexFor(transformList, 7));
		assertTrue(getViewIndexFor(transformList, 8) < 0);
		assertEquals(4, getViewIndexFor(transformList, 9));
		assertTrue(getViewIndexFor(transformList, 10) < 0);
		assertTrue(getViewIndexFor(transformList, 11) < 0);
		assertTrue(getViewIndexFor(transformList, 1000) < 0);
		assertTrue(getViewIndexFor(transformList, -1) < 0);
	}

	@Test
	public void testSrcToTransformIndex1_2()
	{
		System.out.println("testSrcToTransformIndex1_2");
		final var sourceList = FXCollections.<Integer>observableArrayList();
		final var transformList = new SortedList<>(sourceList, Integer::compare);
		for (int i = 5; i >= 0; i--)
		{
			sourceList.add(i);
		}
		System.out.println("Source    list : " + sourceList);
		System.out.println("Transform list : " + transformList);
		// [5,4,3,2,1,0] -> filter -> [0,1,2,3,4,5]
		assertEquals(5, getViewIndexFor(sourceList, transformList, 0));
		assertEquals(4, getViewIndexFor(sourceList, transformList, 1));
		assertEquals(3, getViewIndexFor(sourceList, transformList, 2));
		assertEquals(2, getViewIndexFor(sourceList, transformList, 3));
		assertEquals(1, getViewIndexFor(sourceList, transformList, 4));
		assertEquals(0, getViewIndexFor(sourceList, transformList, 5));
		assertTrue(getViewIndexFor(sourceList, transformList, 6) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, 1000) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, -1) < 0);
		// same with convenience method:
		assertEquals(5, getViewIndexFor(transformList, 0));
		assertEquals(4, getViewIndexFor(transformList, 1));
		assertEquals(3, getViewIndexFor(transformList, 2));
		assertEquals(2, getViewIndexFor(transformList, 3));
		assertEquals(1, getViewIndexFor(transformList, 4));
		assertEquals(0, getViewIndexFor(transformList, 5));
		assertTrue(getViewIndexFor(transformList, 6) < 0);
		assertTrue(getViewIndexFor(transformList, 1000) < 0);
		assertTrue(getViewIndexFor(transformList, -1) < 0);
	}

	@Test
	public void testSrcToTransformIndex2()
	{
		System.out.println("testSrcToTransformIndex2");
		final var sourceList = FXCollections.<Integer>observableArrayList();
		final var transformList = new SortedList<>(
			new FilteredList<>(sourceList, i -> i % 2 == 1), Integer::compare);
		for (int i = 10; i >= 0; i--)
		{
			sourceList.add(i);
		}
		System.out.println("Source    list : " + sourceList);
		System.out.println("Transform list : " + transformList);
		// [10,9,8,7,6,5,4,3,2,1,0] -> filter -> [9,7,5,3,1] -> sort -> [1,3,5,7,9]
		assertTrue(getViewIndexFor(sourceList, transformList, 0) < 0);
		assertEquals(4, getViewIndexFor(sourceList, transformList, 1));
		assertTrue(getViewIndexFor(sourceList, transformList, 2) < 0);
		assertEquals(3, getViewIndexFor(sourceList, transformList, 3));
		assertTrue(getViewIndexFor(sourceList, transformList, 4) < 0);
		assertEquals(2, getViewIndexFor(sourceList, transformList, 5));
		assertTrue(getViewIndexFor(sourceList, transformList, 6) < 0);
		assertEquals(1, getViewIndexFor(sourceList, transformList, 7));
		assertTrue(getViewIndexFor(sourceList, transformList, 8) < 0);
		assertEquals(0, getViewIndexFor(sourceList, transformList, 9));
		assertTrue(getViewIndexFor(sourceList, transformList, 10) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, 11) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, 1000) < 0);
		assertTrue(getViewIndexFor(sourceList, transformList, -1) < 0);
	}
}
