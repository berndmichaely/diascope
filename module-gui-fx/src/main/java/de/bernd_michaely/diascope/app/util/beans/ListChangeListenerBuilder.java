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

import java.util.function.Consumer;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;

/// Builder to create a ListChangeListener.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListChangeListenerBuilder<T>
{
	private Consumer<Change<? extends T>> onAdd, onRemove, onUpdate, onPermutate;

	public ListChangeListenerBuilder()
	{
		this.onAdd = change ->
		{
			throw new IllegalStateException("Unexpected add in ListChangeListener of: " +
				change.getList().getClass().getName());
		};
		this.onRemove = change ->
		{
			throw new IllegalStateException("Unexpected remove in ListChangeListener of: " +
				change.getList().getClass().getName());
		};
		this.onUpdate = change ->
		{
			throw new IllegalStateException("Unexpected update in ListChangeListener of: " +
				change.getList().getClass().getName());
		};
		this.onPermutate = change ->
		{
			throw new IllegalStateException("Unexpected permutate in ListChangeListener of: " +
				change.getList().getClass().getName());
		};
	}

	public ListChangeListenerBuilder<T> onAdd(Consumer<Change<? extends T>> onAdd)
	{
		this.onAdd = onAdd;
		return this;
	}

	public ListChangeListenerBuilder<T> onRemove(Consumer<Change<? extends T>> onRemove)
	{
		this.onRemove = onRemove;
		return this;
	}

	public ListChangeListenerBuilder<T> onUpdate(Consumer<Change<? extends T>> onUpdate)
	{
		this.onUpdate = onUpdate;
		return this;
	}

	public ListChangeListenerBuilder<T> onPermutate(Consumer<Change<? extends T>> onPermutate)
	{
		this.onPermutate = onPermutate;
		return this;
	}

	public ListChangeListener<T> build()
	{
		return (Change<? extends T> change) ->
		{
			while (change.next())
			{
				if (change.wasPermutated() && onPermutate != null)
				{
					onPermutate.accept(change);
				}
				else if (change.wasUpdated() && onUpdate != null)
				{
					onUpdate.accept(change);
				}
				else
				{
					if (change.wasRemoved() && onRemove != null)
					{
						onRemove.accept(change);
					}
					if (change.wasAdded() && onAdd != null)
					{
						onAdd.accept(change);
					}
				}
			}
		};
	}
}
