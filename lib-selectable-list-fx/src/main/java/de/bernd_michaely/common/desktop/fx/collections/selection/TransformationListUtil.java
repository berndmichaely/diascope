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
import javafx.collections.transformation.TransformationList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility for index calculations on TransformationList chains.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class TransformationListUtil
{
  private static final int INDEX_OUT_OF_BOUNDS = -1;
  private static final String MSG_OUT_OF_TRANSFORM_CHAIN =
    "Source list is not in transformation chain!";

  /**
   * Maps the given index of the transformation lists direct source list to an
   * index in the transformation list. Returns a negative value, if the source
   * index is not mapped. This method is also a workaround for undefined
   * index-out-of-bounds behavior of
   * {@link TransformationList#getViewIndex(int)}.
   *
   * @since 2.0
   * @see #getViewIndexFor(ObservableList, TransformationList, int)
   * @param transformationList the given transformation list
   * @param index              the given source list index
   * @return the corresponding transformation list index
   * @throws IllegalArgumentException if the source list is not in the
   *                                  transformation chain
   */
  public static int getViewIndexFor(@Nullable TransformationList<?, ?> transformationList, int index)
  {
    final @Nullable ObservableList<?> sourceList = transformationList != null ?
      transformationList.getSource() : null;
    return getViewIndexFor(sourceList, transformationList, index);
  }

  /**
   * Maps the given index of the source list to an index in the transformation
   * list. Returns a negative value, if the source index is not mapped. This
   * method is also a workaround for undefined index-out-of-bounds behavior of
   * {@link TransformationList#getViewIndex(int)}.
   *
   * @see
   * <a href="https://bugs.openjdk.java.net/browse/JDK-8271865">https://bugs.openjdk.java.net/browse/JDK-8271865</a>
   *
   * @param sourceList         the given source list
   * @param transformationList the given transformation list
   * @param index              the given source list index
   * @return the corresponding transformation list index
   * @throws IllegalArgumentException if the source list is not in the
   *                                  transformation chain
   */
  public static int getViewIndexFor(@Nullable ObservableList<?> sourceList,
    @Nullable TransformationList<?, ?> transformationList, int index)
  {
    if (sourceList == null || index < 0 || index >= sourceList.size())
    {
      return INDEX_OUT_OF_BOUNDS;
    }
    if (transformationList == null || transformationList == sourceList)
    {
      return index; // no transformation => return identity
    }
    try
    {
      return _getViewIndexFor(sourceList, transformationList, index);
    }
    catch (IndexOutOfBoundsException ex)
    {
      return INDEX_OUT_OF_BOUNDS;
    }
  }

  private static int _getViewIndexFor(ObservableList<?> sourceList,
    TransformationList<?, ?> transformationList, int index)
  {
    final ObservableList<?> immediateSource = transformationList.getSource();
    if (immediateSource == sourceList)
    {
      return transformationList.getViewIndex(index);
    }
    else if (immediateSource instanceof TransformationList tlInner)
    {
      final int indexInner = _getViewIndexFor(sourceList, tlInner, index);
      if (indexInner >= 0 && indexInner < immediateSource.size())
      {
        return transformationList.getViewIndex(indexInner);
      }
      else
      {
        return INDEX_OUT_OF_BOUNDS;
      }
    }
    else
    {
      throw new IllegalArgumentException(MSG_OUT_OF_TRANSFORM_CHAIN);
    }
  }
}
