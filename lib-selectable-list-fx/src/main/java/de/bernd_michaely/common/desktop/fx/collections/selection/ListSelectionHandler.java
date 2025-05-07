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

import java.util.stream.Stream;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.transformation.TransformationList;

/**
 * Interface to describe information and actions related to the selection of
 * objects containing a list of selectable items.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 * @param <E> the list element type
 * @see SelectableListFactory
 */
public interface ListSelectionHandler<E> extends SelectableProperties
{
	/**
	 * Stream all selected elements.
	 *
	 * @return a stream of all selected elements
	 */
	Stream<? extends E> streamSelected();

	/**
	 * Stream all unselected elements.
	 *
	 * @return a stream of all unselected elements
	 */
	Stream<? extends E> streamUnselected();

	/**
	 * Returns the given source list.
	 *
	 * @return the given source list
	 */
	SelectableList<E> getSourceList();

	/**
	 * Returns the given transformation list.
	 *
	 * @return the given transformation list
	 */
	TransformationList<E, E> getTransformationList();

	/**
	 * Returns the head of the transformation list or the source list, if no
	 * transformations are in use.
	 *
	 * @return the head of the transformation list
	 */
	ReadOnlyObjectProperty<ReadOnlyListProperty<E>> headListProperty();

	default ReadOnlyListProperty<E> getHeadList()
	{
		return headListProperty().get();
	}

	/**
	 * Returns the head list item at the given index.
	 *
	 * @param index the given index, including any transformations
	 * @return the head list item at the given index
	 */
	E get(int index);

	/**
	 * Returns true, iff a transformation list is set.
	 *
	 * @return true, iff a transformation list is set
	 */
	default boolean isTransformed()
	{
		return getTransformationList() != null;
	}
}
