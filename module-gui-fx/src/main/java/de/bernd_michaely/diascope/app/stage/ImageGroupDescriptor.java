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
package de.bernd_michaely.diascope.app.stage;

import java.lang.System.Logger;
import java.nio.file.Path;
import javafx.scene.image.Image;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Object to describe an image group, that is all data and metadata which
 * describe a single image. The term image refers to the content, that is
 * several image files may belong to the group, e.g. RAW + JPEG files of the
 * same image.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageGroupDescriptor
{
	private static final Logger logger = System.getLogger(ImageGroupDescriptor.class.getName());
	private final Path path;
	private String title = "";
	private @Nullable Image thumbnail;

	ImageGroupDescriptor(Path path)
	{
		this.path = path;
		final Path fileName = path.getFileName();
		title = fileName != null ? fileName.toString() : "";
	}

	@Nullable
	Image getThumbnail()
	{
		return this.thumbnail;
	}

	void setThumbnail(@Nullable Image thumbnail)
	{
		this.thumbnail = thumbnail;
	}

	Path getPath()
	{
		return path;
	}

	@Override
	public String toString()
	{
		return title;
	}
}
