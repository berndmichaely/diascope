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

	ClippingPointsListener(Viewport viewport, List<ImageLayer> unmodifiableLayers)
	{
		this.viewport = viewport;
		this.unmodifiableLayers = unmodifiableLayers;
	}

	@Override
	public void run()
	{
		if (viewport.isMultiLayerMode())
		{
			final int n = unmodifiableLayers.size();
			for (int i = 0; i < n; i++)
			{
				final var layer = unmodifiableLayers.get(i);
				final var divider = layer.getDivider();
				final var corner = divider.getBorder();
				final var layerNext = unmodifiableLayers.get(i == n - 1 ? 0 : i + 1);
				final var dividerNext = layerNext.getDivider();
				final var cornerNext = dividerNext.getBorder();
				final int numIntermediateCorners = numberOfCornerPointsBetween(
					corner, divider.getAngle(), cornerNext, dividerNext.getAngle());
				final int numPoints = 2 * (3 + numIntermediateCorners);
				final Double[] points = new Double[numPoints];
				int index = 0;
				points[index++] = viewport.getSplitCenter().xProperty().getValue();
				points[index++] = viewport.getSplitCenter().yProperty().getValue();
				points[index++] = divider.getBorderIntersectionX();
				points[index++] = divider.getBorderIntersectionY();
				var c = corner;
				for (int k = 0; k < numIntermediateCorners; k++, c = c.next())
				{
					points[index++] = switch (c)
					{
						case TOP, RIGHT ->
							viewport.widthProperty().getValue();
						case BOTTOM, LEFT ->
							ZERO;
					};
					points[index++] = switch (c)
					{
						case RIGHT, BOTTOM ->
							viewport.heightProperty().getValue();
						case LEFT, TOP ->
							ZERO;
					};
				}
				points[index++] = dividerNext.getBorderIntersectionX();
				points[index++] = dividerNext.getBorderIntersectionY();
				layer.setShapePoints(points);
			}
		}
	}
}
