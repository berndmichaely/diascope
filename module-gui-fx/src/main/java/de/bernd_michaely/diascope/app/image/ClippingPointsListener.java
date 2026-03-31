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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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

	static class Points extends AbstractCollection<Double>
	{
		private static final int MAX_NUM_POINTS = 3 + Border.values().length;
		private final Double[] coordinates = new Double[2 * MAX_NUM_POINTS];
		private int numPoints;
		private int numCoordinates;
		private int index;

		private class PointsIterator implements Iterator<Double>
		{
			private int counter;

			@Override
			public boolean hasNext()
			{
				return counter < numCoordinates;
			}

			@Override
			public Double next()
			{
				if (hasNext())
				{
					return coordinates[counter++];
				}
				else
				{
					throw new NoSuchElementException();
				}
			}

			private void reset()
			{
				counter = 0;
			}
		}
		private final PointsIterator iterator = new PointsIterator();

		@Override
		public Iterator<Double> iterator()
		{
			return iterator;
		}

		@Override
		public int size()
		{
			return numPoints;
		}

		private void setNumPoints(int numPoints)
		{
			this.numPoints = numPoints;
			numCoordinates = 2 * numPoints;
			index = 0;
			iterator.reset();
		}

		private void setNextCoordinate(Double value)
		{
			coordinates[index++] = value;
		}

		@Override
		public Double[] toArray()
		{
			final Double[] result = new Double[numCoordinates];
			System.arraycopy(coordinates, 0, result, 0, numCoordinates);
			return result;
		}
	}
	private final Points points = new Points();

	ClippingPointsListener(Viewport viewport, List<ImageLayer> unmodifiableLayers,
		Function<ImageLayer, SplitDivider> dividerByImageLayer,
		Function<ImageLayer, ImageLayerShapeSplit> shapeByImageLayer)
	{
		this.viewport = viewport;
		this.unmodifiableLayers = unmodifiableLayers;
		this.dividerByImageLayer = dividerByImageLayer;
		this.shapeByImageLayer = shapeByImageLayer;
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
				points.setNumPoints(3 + numIntermediateCorners);
				final var splitCenter = viewport.getSplitCenter();
				points.setNextCoordinate(splitCenter.xProperty().getValue());
				points.setNextCoordinate(splitCenter.yProperty().getValue());
				points.setNextCoordinate(divider.getBorderIntersectionX());
				points.setNextCoordinate(divider.getBorderIntersectionY());
				for (int k = 0; k < numIntermediateCorners; k++, corner = corner.next())
				{
					points.setNextCoordinate(switch (corner)
					{
						case TOP, RIGHT -> viewport.widthProperty().getValue();
						case BOTTOM, LEFT -> ZERO;
					});
					points.setNextCoordinate(switch (corner)
					{
						case RIGHT, BOTTOM -> viewport.heightProperty().getValue();
						case LEFT, TOP -> ZERO;
					});
				}
				points.setNextCoordinate(dividerNext.getBorderIntersectionX());
				points.setNextCoordinate(dividerNext.getBorderIntersectionY());
				shapeByImageLayer.apply(layer).setPolygonPoints(points);
			}
		}
		else if (n == 1)
		{
			points.setNumPoints(4);
			final double width = viewport.widthProperty().get();
			final double height = viewport.heightProperty().get();
			points.setNextCoordinate(ZERO);
			points.setNextCoordinate(ZERO);
			points.setNextCoordinate(width);
			points.setNextCoordinate(ZERO);
			points.setNextCoordinate(width);
			points.setNextCoordinate(height);
			points.setNextCoordinate(ZERO);
			points.setNextCoordinate(height);
			shapeByImageLayer.apply(unmodifiableLayers.getFirst()).setPolygonPoints(points);
		}
	}
}
