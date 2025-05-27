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

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.initialization.qual.UnderInitialization;

import static de.bernd_michaely.diascope.app.image.Bindings.C;
import static java.lang.Math.clamp;
import static java.lang.Math.max;
import static java.lang.System.Logger.Level.*;
import static java.util.Collections.unmodifiableList;

/// Class to handle a divider drag cycle.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class DividerDragCycle implements Runnable
{
	private static final Logger logger = System.getLogger(DividerDragCycle.class.getName());
	private final List<ImageLayer> layers;
	private final int n;
	private final int indexLast;
	private final Divider divider;
	private final double dividerMinGap;
	private final List<Double> startAngles;
	private final int indexDivider;
	private final FoldState foldStateFwd, foldStateBwd;
	private double dividerStartAngle;
	private double singleAngleMin;
	private double singleAngleMax;
	private boolean isForward;

	private class FoldState
	{
		private final boolean forward;
		private final double angleLow;
		private final double rangeLow;
		private final double rangeHigh;
		private final double angleHigh;

		private FoldState(boolean forward)
		{
			if (n < 2 || indexDivider < 0 || indexDivider >= n)
			{
				throw new IllegalStateException("Invalid precondition for FoldState");
			}
			this.forward = forward;
			final double dividerCumulativeGap = indexLast * dividerMinGap;
			if (forward)
			{
				if (indexDivider > 0)
				{
					angleLow = startAngles.get(indexDivider - 1);
					angleHigh = angleLow + C;
				}
				else
				{
					angleHigh = startAngles.get(indexLast);
					angleLow = angleHigh - C;
				}
				rangeLow = angleLow + dividerMinGap;
				rangeHigh = angleHigh - dividerCumulativeGap;
			}
			else // backward
			{
				if (indexDivider < indexLast)
				{
					angleHigh = startAngles.get(indexDivider + 1);
					angleLow = angleHigh - C;
				}
				else
				{
					angleLow = startAngles.get(0);
					angleHigh = angleLow + C;
				}
				rangeLow = angleLow + dividerCumulativeGap;
				rangeHigh = angleHigh - dividerMinGap;
			}
		}

		@Override
		public String toString()
		{
			return "%s [[ %f [ %f / %f ] %f ] %s]".formatted(
				getClass().getSimpleName(), angleLow, rangeLow, rangeHigh, angleHigh,
				forward ? "→ FWD" : "← BWD");
		}
	}

	DividerDragCycle(List<ImageLayer> layers, Divider divider, double dividerMinGap)
	{
		this.layers = layers;
		this.n = layers.size();
		this.indexLast = n - 1;
		this.divider = divider;
		this.dividerMinGap = dividerMinGap;
		final List<Double> angles = new ArrayList<>(n);
		int index = -1;
		for (int i = 0; i < n; i++)
		{
			final var d = layers.get(i).getDivider();
			final double angle = d.angleProperty().get();
			angles.add(i, angle);
			if (d == divider)
			{
				dividerStartAngle = angle;
				index = i;
			}
		}
		if (index >= 0)
		{
			indexDivider = index;
		}
		else
		{
			throw new IllegalStateException("Invalid divider in DividerDragCycle.");
		}
		this.startAngles = unmodifiableList(angles);
		this.foldStateFwd = new FoldState(true);
		this.foldStateBwd = new FoldState(false);
		logger.log(TRACE, () -> "· " + foldStateFwd);
		logger.log(TRACE, () -> "· " + foldStateBwd);
		initSingleRotation(dividerMinGap);
	}

	private void initSingleRotation(
		@UnderInitialization(DividerDragCycle.class) DividerDragCycle this,
		double dividerMinGap)
	{
		final double angle = dividerStartAngle;
		final int index = indexDivider;
		if (n > 1 && index >= 0)
		{
			final int indexPrev = index > 0 ? index - 1 : indexLast;
			final int indexNext = index < indexLast ? index + 1 : 0;
			double anglePrev = layers.get(indexPrev).getDivider().getAngle();
			while (anglePrev > angle)
			{
				anglePrev -= C;
			}
			double angleNext = layers.get(indexNext).getDivider().getAngle();
			while (angleNext < angle)
			{
				angleNext += C;
			}
			singleAngleMin = anglePrev + dividerMinGap;
			singleAngleMax = max(singleAngleMin, angleNext - dividerMinGap);
			//         max() ^^^ due to possible rounding errors
		}
		else
		{
			singleAngleMin = 0.0;
			singleAngleMax = 0.0;
		}
	}

	/// Normalizes the given angle to be within the range
	/// `[rangeLow..(rangeLow+360°)[`.
	///
	/// *Implementation note:* assuming angle is already close to range.
	///
	/// @param angle    the given angle
	/// @param rangeLow the lower range bound
	/// @return the normalized angle
	///
	static double normalizeAngleToRange(double angle, double rangeLow)
	{
		final double rangeHigh = rangeLow + C;
		double a = angle;
		while (a < rangeLow)
		{
			a += C;
		}
		while (a >= rangeHigh)
		{
			a -= C;
		}
		return a;
	}

	@Override
	public void run()
	{
		final var mouseDragState = divider.getMouseDragState();
		final var rotationType = mouseDragState.getRotationType();
		final double rotationAngle = mouseDragState.getRotationAngle();
		switch (rotationType)
		{
			case NEUTRAL ->
			{
				for (int i = 0; i < n; i++)
				{
					layers.get(i).getDivider().angleProperty().setValue(startAngles.get(i));
				}
			}
			case WHEEL ->
			{
				final double da = rotationAngle - dividerStartAngle;
				for (int i = 0; i < n; i++)
				{
					final var d = layers.get(i).getDivider();
					d.setAngle(startAngles.get(i) + da);
				}
			}
			case SINGLE ->
			{
				for (int i = 0; i < n; i++)
				{
					final var d = layers.get(i).getDivider();
					if (d == divider)
					{
						// normalize the rotation angle to the middle of the invalid range:
						// rangeLow == singleAngleMin - (C - (singleAngleMax - singleAngleMin)) / 2
						final double rangeLow = (singleAngleMax + singleAngleMin - C) / 2;
						final double normalized = normalizeAngleToRange(rotationAngle, rangeLow);
						// then limit the rotation angle to the valid range:
						divider.setAngle(clamp(normalized, singleAngleMin, singleAngleMax));
					}
					else
					{
						d.angleProperty().setValue(startAngles.get(i));
					}
				}
			}
			case FOLD ->
			{
				isForward = normalizeAngleToRange(
					rotationAngle, (isForward ? foldStateFwd : foldStateBwd).angleLow) >= dividerStartAngle;
				final var foldState = isForward ? foldStateFwd : foldStateBwd;
				final double normalized = normalizeAngleToRange(rotationAngle, foldState.angleLow);
				final double ra = clamp(normalized, foldState.rangeLow, foldState.rangeHigh);
				divider.setAngle(ra);
				int index = indexDivider;
				for (int i = n - 2; i >= 0; i--)
				{
					index = isForward ? (index < indexLast ? index + 1 : 0) : (index > 0 ? index - 1 : indexLast);
					final double startAngle = startAngles.get(index);
					if (i > 0)
					{
						final double a, th, tl, dh, dl, p;
						if (isForward)
						{
							th = normalizeAngleToRange(foldState.angleHigh - i * dividerMinGap, foldState.angleLow);
							tl = startAngle;
							dh = foldState.rangeHigh;
							dl = dividerStartAngle;
							p = (ra - dl) / (dh - dl);
							a = tl + p * normalizeAngleToRange((th - tl), 0.0);
						}
						else
						{
							th = foldState.angleLow + i * dividerMinGap;
							tl = startAngle;
							dh = foldState.rangeLow;
							dl = dividerStartAngle;
							p = (ra - dl) / (dh - dl);
							a = tl - p * normalizeAngleToRange(tl - th, 0.0);
						}
						layers.get(index).getDivider().setAngle(a);
					}
					else
					{
						layers.get(index).getDivider().setAngle(startAngle);
					}
				}
			}
			default -> throw new AssertionError("Invalid RotationType »%s«".formatted(rotationType));
		}
	}
}
