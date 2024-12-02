/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.stage.concurrent;

import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A cache to hold LoadedImage items identified by a Path. This cache is not
 * thread safe on its own.
 */
class ImageCache
{
	private static @MonotonicNonNull ImageCache instance;
	/**
	 * The maximum capacity of the cache.
	 */
	private static final int CAPACITY = 5;
	private final Deque<ImageContainer> queue = new LinkedList<>();

	private ImageCache()
	{
	}

	static ImageCache getInstance()
	{
		if (instance == null)
		{
			instance = new ImageCache();
		}
		return instance;
	}

	/**
	 * Adds a new item to the head of the queue, replacing an earlier item with
	 * the same path, if present, and limits the size of the cache to the maximum
	 * capacity.
	 *
	 * @param imageContainer the item to add
	 */
	void put(ImageContainer item)
	{
		queue.removeLastOccurrence(item);
		queue.addFirst(item);
		while (queue.size() > CAPACITY)
		{
			queue.removeLast();
		}
	}

	/**
	 * Finds an item by path.
	 *
	 * @param path the path to search for
	 * @return an optional ImageContainer
	 */
	Optional<ImageContainer> find(@Nullable Path path)
	{
		return queue.stream().filter(item -> item.path().equals(path)).findFirst();
	}

	/**
	 * Removes all cached items.
	 */
	void clear()
	{
		queue.clear();
	}
}
