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

/**
 * Utility class for {@link ImageTransformsSwitch}.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
final class TransformParams extends TransformParamsBase
{
	private static final ZoomMode DEFAULT_ZOOM_MODE = ZoomMode.getDefault();
	ZoomMode zoomMode = DEFAULT_ZOOM_MODE;

	static TransformParams createFrom(ImageTransforms imageTransforms)
	{
		final var newInstance = new TransformParams();
		newInstance.readFrom(imageTransforms);
		return newInstance;
	}

	@Override
	void readFrom(ImageTransforms imageTransforms)
	{
		super.readFrom(imageTransforms);
		zoomMode = imageTransforms.zoomModeOrDefaultProperty().get();
	}

	@Override
	void writeTo(ImageTransforms imageTransforms)
	{
		super.writeTo(imageTransforms);
		imageTransforms.zoomModeRawValueProperty().set(zoomMode);
	}

	static void initDefaultsOf(ImageTransforms imageTransforms)
	{
		TransformParamsBase._initDefaultsOf(imageTransforms);
		imageTransforms.zoomModeRawValueProperty().set(DEFAULT_ZOOM_MODE);
	}

	@Override
	public String toString()
	{
		return super.toString().replaceFirst("]$", "_ZoomMode:%s]".formatted(zoomMode.name()));
	}
}
