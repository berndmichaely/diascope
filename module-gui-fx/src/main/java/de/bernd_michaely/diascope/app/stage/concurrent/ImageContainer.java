package de.bernd_michaely.diascope.app.stage.concurrent;

import de.bernd_michaely.diascope.app.stage.concurrent.ImageLoader.RequestType;
import java.nio.file.Path;
import java.util.Objects;
import javafx.scene.image.Image;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Container for concurrent loading of images.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
record ImageContainer(Path path, RequestType requestType, @Nullable Image image, boolean loaded)
{
	ImageContainer(Path path, RequestType requestType)
	{
		this(path, requestType, null, false);
	}

	public Path key()
	{
		return path();
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof ImageContainer other)
		{
			return Objects.equals(this.key(), other.key());
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(key());
	}
}
