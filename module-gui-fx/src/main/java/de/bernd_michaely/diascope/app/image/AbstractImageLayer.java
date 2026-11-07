/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Base class for image layers.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
abstract class AbstractImageLayer implements AutoCloseable
{
	private final ObjectProperty<Optional<ImageDescriptor>> imageDescriptor =
		new SimpleObjectProperty<>(Optional.empty());

	abstract ImageTransforms getImageTransforms();

	ObjectProperty<Optional<ImageDescriptor>> optionalImageDescriptorProperty()
	{
		return imageDescriptor;
	}

	Optional<ImageDescriptor> getOptionalImageDescriptor()
	{
		return optionalImageDescriptorProperty().get();
	}

	@Nullable
	ImageDescriptor getImageDescriptor()
	{
		return getOptionalImageDescriptor().get();
	}

	void setOptionalImageDescriptor(Optional<ImageDescriptor> imageDescriptor)
	{
		optionalImageDescriptorProperty().set(imageDescriptor);
	}

	void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		setOptionalImageDescriptor(Optional.ofNullable(imageDescriptor));
	}

	@Override
	public void close()
	{
	}
}
