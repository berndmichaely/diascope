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
import java.util.List;
import java.util.function.Consumer;

import static de.bernd_michaely.diascope.app.image.Bindings.*;
import static de.bernd_michaely.diascope.app.image.Divider.RotationType.*;
import static java.lang.Math.clamp;
import static java.lang.Math.max;
import static java.lang.System.Logger.Level.*;

/// Class to handle divider rorations.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class DividerRotationControl implements Consumer<Divider>
{
	private static final Logger logger = System.getLogger(DividerRotationControl.class.getName());
	private static final double DEFAULT_DIVIDER_MIN_GAP = 10.0;
	private final List<ImageLayer> layers;
	private double angleMin;
	private double angleMax;

	DividerRotationControl(List<ImageLayer> layers)
	{
		this.layers = layers;
	}

	double getDividerMinGap()
	{
		return DEFAULT_DIVIDER_MIN_GAP;
	}

	private int getDividerIndex(Divider divider)
	{
		for (int i = 0; i < layers.size(); i++)
		{
			if (layers.get(i).getDivider() == divider)
			{
				return i;
			}
		}
		return -1;
	}

	/// Initializes all dividers to aequidistant angles.
	void initializeDividerAngles()
	{
		final int numLayers = layers.size();
		if (numLayers > 0)
		{
			final double da = C / numLayers;
			double a = 90.0;
			for (int i = 0; i < numLayers; i++, a += da)
			{
				layers.get(i).getDivider().setAngle(a);
			}
		}
	}

	/// Normalizes all divider angles.
	/// The first divider will be in the range `[0°..360°[`,
	/// the following in the range `[0°..720°[`.
	/// All angles are strictly increasing.
	///
	void normalizeDividerAngles()
	{
		if (!layers.isEmpty())
		{
			final var first = layers.getFirst();
			final double angle = first.getDivider().getAngle();
			final double da = normalizeAngle(angle) - angle;
			if (da != 0.0)
			{
				for (var layer : layers)
				{
					final var divider = layer.getDivider();
					divider.setAngle(divider.getAngle() + da);
				}
			}
		}
	}

	@Override
	public void accept(Divider divider)
	{
		final var mouseDragState = divider.getMouseDragState();
		final var rotationType = mouseDragState.getRotationType();
		final boolean isDragStart = mouseDragState.isDragStart();
		final double angle = divider.getAngle();
		final double rotationAngle = mouseDragState.getRotationAngle();
		switch (rotationType)
		{
			case ALL_SYNCHRONOUS ->
			{
				final double da = rotationAngle - angle;
				for (var layer : layers)
				{
					final var d = layer.getDivider();
					d.setAngle(d.getAngle() + da);
				}
			}
			case SINGLE_ONLY ->
			{
				if (isDragStart)
				{
					// initialize angleMin, angleMax:
					final int n = layers.size();
					final int index = getDividerIndex(divider);
					if (n > 1 && index >= 0)
					{
						final int indexLast = n - 1;
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
						final double gap = getDividerMinGap();
						angleMin = anglePrev + gap;
						angleMax = max(angleMin, (angleNext - gap));
						//  max()  ^^^ due to possible rounding errors
					}
				}
				// rangeLow = angleMin - (C - (angleMax - angleMin)) / 2
				final double rangeLow = (angleMax + angleMin - C) / 2;
				final double rangeHigh = rangeLow + C;
				double a = rotationAngle;
				while (a < rangeLow)
				{
					a += C;
				}
				while (a >= rangeHigh)
				{
					a -= C;
				}
				divider.setAngle(clamp(a, angleMin, angleMax));
			}
			case SINGLE_ADJUST_OTHERS ->
			{
				// TODO : implement SINGLE_ADJUST_OTHERS RotationType
				if (isDragStart)
				{
					logger.log(TRACE, () ->
						getClass().getName() + ": RotationType »" + rotationType + "« not implemented yet…");
				}
			}
			case RELEASED ->
			{
				normalizeDividerAngles();
			}
			default -> throw new AssertionError("Invalid Divider.RotationType: " + rotationType);
		}
	}
}
