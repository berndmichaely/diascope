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

import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.RequestType;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.System.Logger.Level.*;

/**
 * Task for ImageLoader.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageLoaderTask implements Runnable
{
	private static final System.Logger logger = System.getLogger(ImageLoaderTask.class.getName());
	private final Path path;
	private final RequestType requestType;
	private final Consumer<ImageContainer> resultConsumer;

	ImageLoaderTask(Path path, RequestType requestType, Consumer<ImageContainer> resultConsumer)
	{
		this.path = path;
		this.requestType = requestType;
		this.resultConsumer = resultConsumer;
	}

	private static @Nullable
	Image loadImage(Path path)
	{
		try (final var inputStream = new BufferedInputStream(Files.newInputStream(path)))
		{
			logger.log(TRACE, "Loading image »%s«".formatted(path.toAbsolutePath()));
			return new Image(inputStream);
		}
		catch (IOException ex)
		{
			logger.log(WARNING, ex);
			return null;
		}
	}

	@Override
	public void run()
	{
		resultConsumer.accept(new ImageContainer(path, requestType, loadImage(path), true));
	}
}
