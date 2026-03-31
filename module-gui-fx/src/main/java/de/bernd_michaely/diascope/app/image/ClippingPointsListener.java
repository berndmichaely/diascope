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
package de.bernd_michaely.diascope.app.image;

import java.util.List;
import java.util.function.Function;

import static de.bernd_michaely.diascope.app.image.Border.*;

/// Class to calculate splitter shape clipping points.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ClippingPointsListener implements Runnable
{
	private static final Double ZERO = 0.0;
	private final Viewport viewport;
	private final List<ImageLayer> unmodifiableLayers;
	private final Function<ImageLayer, SplitDivider> dividerByImageLayer;
	private final Function<ImageLayer, ImageLayerShapeSplit> shapeByImageLayer;
	private final Double[] pointsRect;

	ClippingPointsListener(Viewport viewport, List<ImageLayer> unmodifiableLayers,
		Function<ImageLayer, SplitDivider> dividerByImageLayer,
		Function<ImageLayer, ImageLayerShapeSplit> shapeByImageLayer)
	{
		this.viewport = viewport;
		this.unmodifiableLayers = unmodifiableLayers;
		this.dividerByImageLayer = dividerByImageLayer;
		this.shapeByImageLayer = shapeByImageLayer;
		this.pointsRect = new Double[8];
		pointsRect[0] = ZERO;
		pointsRect[1] = ZERO;
		pointsRect[3] = ZERO;
		pointsRect[6] = ZERO;
	}

	@Override
	public void run()
	{
		final int n = unmodifiableLayers.size();
		if (n > 1)
		{
			for (int i = 0; i < n; i++)
			{
				final var layer = unmodifiableLayers.get(i);
				final var layerNext = unmodifiableLayers.get((i + 1) % n);
				final var divider = dividerByImageLayer.apply(layer);
				final var dividerNext = dividerByImageLayer.apply(layerNext);
				var corner = divider.getBorder();
				final var cornerNext = dividerNext.getBorder();
				final int numIntermediateCorners = numberOfCornerPointsBetween(
					corner, divider.getAngle(), cornerNext, dividerNext.getAngle());
				final int numPoints = 2 * (3 + numIntermediateCorners);
				final Double[] points = new Double[numPoints];
				int index = 0;
				final var splitCenter = viewport.getSplitCenter();
				points[index++] = splitCenter.xProperty().getValue();
				points[index++] = splitCenter.yProperty().getValue();
				points[index++] = divider.getBorderIntersectionX();
				points[index++] = divider.getBorderIntersectionY();
				for (int k = 0; k < numIntermediateCorners; k++, corner = corner.next())
				{
					points[index++] = switch (corner)
					{
						case TOP, RIGHT -> viewport.widthProperty().getValue();
						case BOTTOM, LEFT -> ZERO;
					};
					points[index++] = switch (corner)
					{
						case RIGHT, BOTTOM -> viewport.heightProperty().getValue();
						case LEFT, TOP -> ZERO;
					};
				}
				points[index++] = dividerNext.getBorderIntersectionX();
				points[index++] = dividerNext.getBorderIntersectionY();
				shapeByImageLayer.apply(layer).setPolygonPoints(points);
			}
		}
		else if (n == 1)
		{
			final double width = viewport.widthProperty().get();
			final double height = viewport.heightProperty().get();
			pointsRect[2] = width;
			pointsRect[4] = width;
			pointsRect[5] = height;
			pointsRect[7] = height;
			shapeByImageLayer.apply(unmodifiableLayers.getFirst()).setPolygonPoints(pointsRect);
		}
	}
}
