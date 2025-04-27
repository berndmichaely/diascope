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

/**
 * This module provides a list data structure which encapsulates list items
 * with a selection state.
 * <p>
 * The list type is based on {@link javafx.collections.ObservableList} and
 * provides a selection change listening mechanism.
 * </p>
 * <p>
 * The {@link de.bernd_michaely.common.desktop.fx.collections.selection.ListSelectionHandler}
 * type provides properties indicating the number of selected list items and
 * all/nothing selected info. This works also through an optional
 * transformation chain.
 * </p>
 */
module de.bernd_michaely.common.selectable.list.fx
{
	requires transitive javafx.base;
	requires org.checkerframework.checker.qual;

	exports de.bernd_michaely.common.desktop.fx.collections.selection;
}
