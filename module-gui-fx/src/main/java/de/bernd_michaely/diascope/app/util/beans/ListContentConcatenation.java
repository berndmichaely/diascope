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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Binding to concatenate the content of a (possibly observable) List of
/// observable lists into a single flat target list.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListContentConcatenation<T> implements AutoCloseable
{
	private final List<ObservableList<T>> sourceLists;
	private final @Nullable ObservableList<ObservableList<T>> observableLists;
	private final List<T> targetList;
	private final ListChangeListener<T> listenerSrcList;
	private final @Nullable ListChangeListener<ObservableList<T>> listenerLists;

	/// Creates a new instance taking a list of observable source lists.
	/// A new target list will be created.
	///
	/// @param sourceLists a list of observable source lists. If the list is
	///   observable itself, it will also be watched for changes, that is,
	///   adding and removing of whole lists to/from {@code sourceLists} will be
	///   reflected in the target list content.
	///   Otherwise, {@code sourceLists} should not be changed anymore
	///   after creating this instance.
	///
	public ListContentConcatenation(List<ObservableList<T>> sourceLists)
	{
		this(sourceLists, null);
	}

	/// Creates a new instance taking a list of observable source lists.
	///
	/// @param sourceLists a list of observable source lists. If the list is
	///   observable itself, it will also be watched for changes, that is,
	///   adding and removing of whole lists to/from {@code sourceLists} will be
	///   reflected in the target list content.
	///   Otherwise, {@code sourceLists} should not be changed anymore
	///   after creating this instance.
	/// @param targetList if not {@code null}, the given list will be used,
	///   otherwise, a new one will be created. Note, that any content of a given
	///   list will be replaced.
	///
	public ListContentConcatenation(List<ObservableList<T>> sourceLists, @Nullable List<T> targetList)
	{
		this.sourceLists = sourceLists;
		this.observableLists = sourceLists instanceof ObservableList ?
			(ObservableList<ObservableList<T>>) sourceLists : null;
		this.targetList = targetList != null ? targetList : new ArrayList<>();
		this.targetList.clear();
		this.listenerSrcList = new ListChangeListenerBuilder<T>()
			.onAdd(change -> this.targetList.addAll(
				getPrefixSize(change.getList()) + change.getFrom(), change.getAddedSubList()))
			.onRemove(change ->
			{
				final int from = getPrefixSize(change.getList()) + change.getFrom();
				final int to = from + change.getRemovedSize();
				this.targetList.subList(from, to).clear();
			})
			.build();
		sourceLists.forEach(this.targetList::addAll);
		sourceLists.forEach(list -> list.addListener(listenerSrcList));
		if (observableLists != null)
		{
			this.listenerLists = new ListChangeListenerBuilder<ObservableList<T>>()
				.onAdd(change ->
				{
					change.getAddedSubList().forEach(list ->
					{
						this.targetList.addAll(getPrefixSize(list), list);
						list.addListener(listenerSrcList);
					});
				})
				.onRemove(change ->
				{
					change.getRemoved().forEach(list ->
					{
						list.removeListener(listenerSrcList);
						final int from = getPrefixSize(change.getFrom());
						final int to = from + list.size();
						this.targetList.subList(from, to).clear();
					});
				})
				.build();
			observableLists.addListener(listenerLists);
		}
		else
		{
			this.listenerLists = null;
		}
	}

	private int getPrefixSize(@UnderInitialization ListContentConcatenation<T> this,
		List list)
	{
		int size = 0;
		if (sourceLists != null)
		{
			final int index = sourceLists.indexOf(list);
			if (index >= 0)
			{
				size = getPrefixSize(index);
			}
			else
			{
				throw new IllegalStateException(
					getClass().getName() + "::getPrefixSize : list not found");
			}
		}
		return size;
	}

	private int getPrefixSize(@UnderInitialization ListContentConcatenation<T> this,
		int index)
	{
		return sourceLists != null ? sourceLists.stream().limit(index).mapToInt(List::size).sum() : 0;
	}

	/// Returns true, iff the list of observable source lists is observable itself.
	///
	/// @return true, iff the list of observable source lists is observable itself
	///
	public boolean isSourceListsObservable()
	{
		return observableLists != null;
	}

	/// Returns the list of observable source lists.
	///
	/// @return the list of observable source lists
	///
	public List<ObservableList<T>> getSourceLists()
	{
		return sourceLists;
	}

	/// Returns the target list.
	///
	/// @return the target list
	///
	public List<T> getTargetList()
	{
		return targetList;
	}

	/// {@inheritDoc}
	///
	/// This implementation removes listeners.
	///
	@Override
	public void close()
	{
		if (observableLists != null && listenerLists != null)
		{
			observableLists.removeListener(listenerLists);
		}
		sourceLists.forEach(list -> list.removeListener(listenerSrcList));
	}
}
