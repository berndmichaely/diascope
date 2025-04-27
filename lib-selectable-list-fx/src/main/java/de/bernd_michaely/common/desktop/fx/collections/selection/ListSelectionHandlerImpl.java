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
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.transformation.TransformationList;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.equal;

/**
 * Default implementation of the ListSelectionHandler interface.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 * @param <E> the list element type
 */
class ListSelectionHandlerImpl<E> implements ListSelectionHandler<E>
{
  private final ReadOnlyListWrapper<E> sourceListProperty;
  private final ReadOnlyListWrapper<E> transformationListProperty;
  private final ReadOnlyObjectWrapper<ReadOnlyListProperty<E>> headList;
  private final ReadOnlyIntegerWrapper numSelectedProperty;
  private final ReadOnlyBooleanWrapper noneSelectedProperty, allSelectedProperty;

  /**
   * Creates an instance for a given source list and transformation list.
   *
   * @param sourceList         the given source list
   * @param transformationList a transformation list (or a chain thereof) to be
   *                           applied to the source list (may be null)
   * @throws NullPointerException     if sourceList is null
   * @throws IllegalArgumentException if transformationList is not null and
   *                                  sourceList is not in the transformation
   *                                  chain
   */
  private ListSelectionHandlerImpl(SelectableList<E> sourceList,
    @Nullable TransformationList<E, E> transformationList)
  {
    this.sourceListProperty = new ReadOnlyListWrapper<>(
      requireNonNull(sourceList, getClass().getName() + " : source list is null"));
    if (transformationList != null && !transformationList.isInTransformationChain(sourceList))
    {
      throw new IllegalArgumentException(getClass().getName() +
        " : source list is not in transformation chain");
    }
    this.transformationListProperty = new ReadOnlyListWrapper<>();
    initTransformationList(this.transformationListProperty, transformationList);
    this.headList = new ReadOnlyObjectWrapper<>(transformationList != null ?
      this.transformationListProperty : this.sourceListProperty);
    this.numSelectedProperty = new ReadOnlyIntegerWrapper();
    this.noneSelectedProperty = new ReadOnlyBooleanWrapper();
    noneSelectedProperty.bind(equal(numSelectedProperty, 0));
    this.allSelectedProperty = new ReadOnlyBooleanWrapper();
    // post init:
//		getSourceList().addSelectionListener(this::handleSelectionChange, isTransformed());
//		allSelectedProperty.bind(equal(numSelectedProperty, getHeadList().sizeProperty()));
  }

  /**
   * Creates an instance for a given source list and transformation list.
   *
   * @param sourceList         the given source list
   * @param transformationList a transformation list (or a chain thereof) to be
   *                           applied to the source list (may be null)
   * @throws NullPointerException     if sourceList is null
   * @throws IllegalArgumentException if transformationList is not null and
   *                                  sourceList is not in the transformation
   *                                  chain
   */
  static <E> ListSelectionHandler<E> createInstance(SelectableList<E> sourceList,
    @Nullable TransformationList<E, E> transformationList)
  {
    final var lsh = new ListSelectionHandlerImpl<E>(sourceList, transformationList);
    // post init:
    lsh.getSourceList().addSelectionListener(lsh::handleSelectionChange, lsh.isTransformed());
    lsh.allSelectedProperty.bind(equal(lsh.numSelectedProperty, lsh.getHeadList().sizeProperty()));
    return lsh;
  }

  @Override
  public SelectableList<E> getSourceList()
  {
    return (SelectableList<E>) sourceListProperty.get();
  }

  @Override
  @SuppressWarnings(
    {
      "unchecked", // unchecked OK because set final in constructor
      "nullness"   // JavaFX ListProperties can handle null values
    })
  public @Nullable
  TransformationList<E, E> getTransformationList()
  {
    return (TransformationList<E, E>) transformationListProperty.get();
  }

  @SuppressWarnings("nullness") // JavaFX ListProperties can handle null values
  private static <E> void initTransformationList(ReadOnlyListWrapper<E> transformationListProperty,
    @Nullable TransformationList<E, E> transformationList)
  {
    transformationListProperty.set(transformationList);
  }

  @Override
  public ReadOnlyObjectProperty<ReadOnlyListProperty<E>> headListProperty()
  {
    return headList.getReadOnlyProperty();
  }

  private void handleSelectionChange(SelectionChange selectionChange)
  {
    if (isTransformed())
    {
      final int sourceIndex = TransformationListUtil.getViewIndexFor(
        getSourceList(), getTransformationList(), selectionChange.getFrom());
      if (sourceIndex >= 0)
      {
        final var selectionChangeType = selectionChange.getSelectionChangeType();
        if (selectionChangeType != null)
        {
          switch (selectionChangeType)
          {
            case SINGLE_INCREMENT ->
              numSelectedProperty.set(numSelectedProperty.get() + 1);
            case SINGLE_DECREMENT ->
              numSelectedProperty.set(numSelectedProperty.get() - 1);
            default -> numSelectedProperty.set((int) streamSelected().count());
          }
        }
      }
    }
    else
    {
      numSelectedProperty.set(getSourceList().getNumSelected());
    }
  }

  @Override
  public ReadOnlyBooleanProperty allSelectedProperty()
  {
    return allSelectedProperty.getReadOnlyProperty();
  }

  @Override
  public ReadOnlyBooleanProperty noneSelectedProperty()
  {
    return noneSelectedProperty.getReadOnlyProperty();
  }

  @Override
  public ReadOnlyIntegerProperty numSelectedProperty()
  {
    return numSelectedProperty.getReadOnlyProperty();
  }

  @Override
  public ReadOnlyIntegerProperty sizeProperty()
  {
    return getHeadList().sizeProperty();
  }

  @Override
  public ReadOnlyBooleanProperty emptyProperty()
  {
    return getHeadList().emptyProperty();
  }

  @Override
  public void selectRange(int from, int to, Action action)
  {
    IntStream.range(from, to)
      .map(this::getSourceIndex)
      .forEach(index -> getSourceList().select(index, action));
  }

  @Override
  public int getSourceIndex(int index)
  {
    final TransformationList<E, E> transformationList = getTransformationList();
    return transformationList != null ?
      transformationList.getSourceIndexFor(getSourceList(), index) : index;
  }

  @Override
  public E get(int index)
  {
    return getHeadList().get(index);
  }

  /**
   * Returns true, iff the item at the given index is selected.
   *
   * @param index the given index, including any transformations
   * @return true, iff the item at the given index is selected
   */
  @Override
  public boolean isSelected(int index)
  {
    return getSourceList().isSelected(getSourceIndex(index));
  }

  @Override
  public Stream<E> streamSelected()
  {
    return streamFiltered(this::isSelected);
  }

  @Override
  public Stream<E> streamUnselected()
  {
    final IntPredicate selected = this::isSelected;
    return streamFiltered(selected.negate());
  }

  private Stream<E> streamFiltered(IntPredicate predicate)
  {
    return IntStream.range(0, size()).filter(predicate).mapToObj(this::get);
  }

  /**
   * {@inheritDoc}
   *
   * @return a String representation of the items of the head list with their
   *         selection status
   */
  @Override
  public String toString()
  {
    final SelectableList<E> list;
    if (isTransformed())
    {
      list = SelectableListFactory.selectableList();
      for (int i = 0; i < size(); i++)
      {
        list.add(get(i));
        if (isSelected(i))
        {
          list.setSelected(i, true);
        }
      }
    }
    else
    {
      list = getSourceList();
    }
    return list.toString();
  }
}
