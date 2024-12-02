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

import de.bernd_michaely.diascope.app.stage.ImageGroupDataUpdater.Result;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import javafx.scene.image.Image;

/**
 * Object to retrieve image metadata in a worker thread.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageGroupDataUpdater implements Callable<Result>
{
	private final Path pathImageFile;
	private final int mainListIndex;

	record Result(ImageMetadata imageMetadata, int mainListIndex)
		{
	}

	ImageGroupDataUpdater(Path imageFile, int mainListIndex)
	{
		this.pathImageFile = imageFile;
		this.mainListIndex = mainListIndex;
	}

	@Override
	public Result call() throws Exception
	{
		try (final var inputStream = new BufferedInputStream(Files.newInputStream(pathImageFile)))
		{
			final Image thumbnail = new Image(inputStream, 200, 200, true, true);
			return new Result(new ImageMetadata(thumbnail), mainListIndex);
		}
	}
}
