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
 * Utility class for {@link ImageTransformsSwitch} to buffer
 * {@link  ImageTransforms} values.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
abstract sealed class TransformParamsBase permits TransformParams, DeltaTransformParams
{
	private static final double DEFAULT_ZOOM_FACTOR_FIXED = 1.0;
	private static final double DEFAULT_ROTATE = 0.0;
	private static final boolean DEFAULT_MIRROR_X = false;
	private static final boolean DEFAULT_MIRROR_Y = false;
	double zoomFactorFixed = DEFAULT_ZOOM_FACTOR_FIXED;
	double rotate = DEFAULT_ROTATE;
	boolean mirrorX = DEFAULT_MIRROR_X;
	boolean mirrorY = DEFAULT_MIRROR_Y;

	void readFrom(ImageTransforms imageTransforms)
	{
		zoomFactorFixed = imageTransforms.zoomFixedProperty().get();
		rotate = imageTransforms.rotateProperty().get();
		mirrorX = imageTransforms.mirrorXProperty().get();
		mirrorY = imageTransforms.mirrorYProperty().get();
	}

	void writeTo(ImageTransforms imageTransforms)
	{
		imageTransforms.zoomFixedProperty().set(zoomFactorFixed);
		imageTransforms.rotateProperty().set(rotate);
		imageTransforms.mirrorXProperty().set(mirrorX);
		imageTransforms.mirrorYProperty().set(mirrorY);
	}

	static void _initDefaultsOf(ImageTransforms imageTransforms)
	{
		imageTransforms.zoomFixedProperty().set(DEFAULT_ZOOM_FACTOR_FIXED);
		imageTransforms.rotateProperty().set(DEFAULT_ROTATE);
		imageTransforms.mirrorXProperty().set(DEFAULT_MIRROR_X);
		imageTransforms.mirrorYProperty().set(DEFAULT_MIRROR_Y);
	}

	@Override
	public String toString()
	{
		return "%s_[ZoomFactor:%f_Rotation:%f_Mirror(%b/%b)]".formatted(
			getClass().getName(), zoomFactorFixed, rotate, mirrorX, mirrorY);
	}
}
