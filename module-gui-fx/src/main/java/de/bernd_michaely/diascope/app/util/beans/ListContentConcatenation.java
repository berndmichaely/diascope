/*
 * Copyright (C) 2025 Bernd Michaely (info@bernd-michaely.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.bernd_michaely.diascope.app.util.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Collections.unmodifiableList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableList;
import static javafx.collections.FXCollections.unmodifiableObservableList;

/// Class to concatenate the content of an - optionally also observable - List
/// of observable lists into a single flat target list.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListContentConcatenation<T> implements AutoCloseable
{
	private static @MonotonicNonNull ListContentConcatenation<Void> EMPTY;
	private final List<ObservableList<T>> sourceLists;
	private final ObservableList<ObservableList<T>> observableLists;
	private final List<T> targetList;
	private final List<T> unmodifiableTargetList;
	private final ListChangeListener<T> listenerSrcList;
	private final ListChangeListener<ObservableList<T>> listenerLists;
	private boolean closed;

	/// Returns an unmodifiable empty instance.
	/// Closing this instance has no effect.
	///
	/// @return an unmodifiable empty instance
	///
	public static ListContentConcatenation<Void> emptyListContentConcatenation()
	{
		if (EMPTY == null)
		{
			EMPTY = new ListContentConcatenation<>(List.of(), List.of())
			{
				@Override
				public void close()
				{
				}
			};
		}
		return EMPTY;
	}

	@FunctionalInterface
	interface IntToIntFunction
	{
		int apply(int value);
	}

	/// Same as `ListContentConcatenation(null)`.
	///
	public ListContentConcatenation()
	{
		this(null);
	}

	/// Same as `ListContentConcatenation(null, targetList)`.
	///
	public ListContentConcatenation(@Nullable List<T> targetList)
	{
		this(null, targetList);
	}

	/// Creates a new instance.
	///
	/// @param sourceLists a list of observable source lists. If the list is
	///   observable itself, it will also be watched for changes, that is,
	///   adding and removing of whole lists to/from `sourceLists` will be
	///   reflected in the `targetList` content.
	///   Otherwise, `sourceLists` should not be changed anymore
	///   after creating this instance.
	///   If `null`, an observable list will be created.
	///
	/// @param targetList if not `null`, the given list will be used,
	///   otherwise, a new one will be created. Note, that any content of a given
	///   list will be replaced.
	///
	public ListContentConcatenation(@Nullable List<ObservableList<T>> sourceLists, @Nullable List<T> targetList)
	{
		this.sourceLists = sourceLists != null ? sourceLists : observableArrayList();
		this.observableLists = this.sourceLists instanceof ObservableList ?
			(ObservableList<ObservableList<T>>) this.sourceLists :
			unmodifiableObservableList(observableList(this.sourceLists));
		if (targetList != null)
		{
			this.targetList = targetList;
			if (!this.targetList.isEmpty())
			{
				this.targetList.clear();
			}
		}
		else
		{
			this.targetList = new ArrayList<>();
		}
		this.unmodifiableTargetList = unmodifiableList(this.targetList);
		final IntToIntFunction getPrefixSizeByIndex =
			index -> this.sourceLists.stream().limit(index).mapToInt(List::size).sum();
		final ToIntFunction<List> getPrefixSizeByList =
			list -> getPrefixSizeByIndex.apply(this.sourceLists.indexOf(list));
		this.listenerSrcList = new ListChangeListenerBuilder<T>()
			.onAdd(change -> this.targetList.addAll(
				getPrefixSizeByList.applyAsInt(change.getList()) + change.getFrom(), change.getAddedSubList()))
			.onRemove(change ->
			{
				final int from = getPrefixSizeByList.applyAsInt(change.getList()) + change.getFrom();
				final int to = from + change.getRemovedSize();
				this.targetList.subList(from, to).clear();
			})
			.build();
		this.sourceLists.forEach(this.targetList::addAll);
		this.sourceLists.forEach(list -> list.addListener(listenerSrcList));
		this.listenerLists = new ListChangeListenerBuilder<ObservableList<T>>()
			.onAdd(change ->
			{
				change.getAddedSubList().forEach(list ->
				{
					this.targetList.addAll(getPrefixSizeByList.applyAsInt(list), list);
					list.addListener(listenerSrcList);
				});
			})
			.onRemove(change ->
			{
				change.getRemoved().forEach(list -> list.removeListener(listenerSrcList));
				final int sizeRemoved = change.getRemoved().stream().mapToInt(List::size).sum();
				final int from = getPrefixSizeByIndex.apply(change.getFrom());
				final int to = from + sizeRemoved;
				this.targetList.subList(from, to).clear();
			})
			.build();
		observableLists.addListener(listenerLists);
	}

	/// Returns the initially given list of observable source lists.
	///
	/// @return the initially given list of observable source lists
	///
	public List<ObservableList<T>> getSourceLists()
	{
		return sourceLists;
	}

	/// Returns the initially given list of observable source lists, if it is
	/// observable itself, otherwise an unmodifiable observable wrapper to it.
	///
	/// @return the initially given list of observable source lists, if it is
	///         observable itself, otherwise an unmodifiable observable wrapper
	///         to it
	///
	public ObservableList<ObservableList<T>> getObservableLists()
	{
		return observableLists;
	}

	/// Returns the unmodifiable target list.
	///
	/// @return the unmodifiable target list
	///
	public List<T> getTargetList()
	{
		return unmodifiableTargetList;
	}

	/// Returns true, iff this instance has been closed.
	/// After that, no modifications to the source lists will be tracked anymore.
	///
	/// @return true, iff this instance has been closed
	///
	public boolean isClosed()
	{
		return closed;
	}

	/// {@inheritDoc}
	///
	/// This implementation removes listeners from the source lists.
	///
	@Override
	public void close()
	{
		if (!closed)
		{
			closed = true;
			observableLists.removeListener(listenerLists);
			sourceLists.forEach(list -> list.removeListener(listenerSrcList));
		}
	}
}
