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
package de.bernd_michaely.diascope.app.image;

import java.nio.file.Path;
import javafx.scene.image.Image;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Object to describe an image. As a wrapper, it has a unique object identity
 * even if two instances encapsulate the same image instance, so it is suitable
 * to be used with an ObservableList.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ImageDescriptor
{
	private final Image image;
	private final Path path;

	public ImageDescriptor(Image image, Path path)
	{
		this.image = image;
		this.path = path;
	}

	/**
	 * Returns the given image.
	 *
	 * @return the given image
	 */
	public Image getImage()
	{
		return image;
	}

	/**
	 * Returns the image path.
	 *
	 * @return the image path
	 */
	public Path getPath()
	{
		return path;
	}

	public String getTitle()
	{
		return path != null ? path.toString() : "";
	}

	@Override
	public final boolean equals(@Nullable Object object)
	{
		return super.equals(object);
	}

	@Override
	public final int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public String toString()
	{
		return getTitle();
	}
}
