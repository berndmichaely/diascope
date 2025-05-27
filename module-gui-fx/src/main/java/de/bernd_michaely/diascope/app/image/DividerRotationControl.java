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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.Bindings.*;
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
	private static final double C2 = 2 * C;
	private final DoubleProperty dividerMinGapProperty;
	private final List<ImageLayer> layers;
	private @Nullable Divider divider;
	private @Nullable Runnable dividerDragCycle;

	enum RotationType
	{
		NEUTRAL, WHEEL, SINGLE, FOLD
	}

	DividerRotationControl(List<ImageLayer> layers)
	{
		this.layers = layers;
		this.dividerMinGapProperty = new SimpleDoubleProperty(DEFAULT_DIVIDER_MIN_GAP);
	}

	DoubleProperty dividerMinGapProperty()
	{
		return dividerMinGapProperty;
	}

	double getDividerMinGap()
	{
		return dividerMinGapProperty().get();
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
	/// the following in the range `]0°..720°[`.
	/// All angles are strictly increasing.
	///
	void normalizeDividerAngles()
	{
		if (!layers.isEmpty())
		{
			final double minGap = getDividerMinGap();
			final double angle = layers.getFirst().getDivider().getAngle();
			final double da = normalizeAngle(angle) - angle;
			double anglePrev = 0.0;
			for (int i = 0; i < layers.size(); i++)
			{
				final var d = layers.get(i).getDivider();
				double an = d.getAngle() + da;
				final double aMin = anglePrev + minGap;
				if (i > 0)
				{
					if (an < aMin)
					{
						final int index = i;
						final double oldValue = an;
						an = aMin;
						final double newValue = an;
						logger.log(WARNING, () -> "Correcting angle #%d from %f → %f"
							.formatted(index, oldValue, newValue));
					}
					if (an >= anglePrev + C)
					{
						final int index = i;
						final double oldValue = an;
						an = max(aMin, an - C);
						final double newValue = an;
						logger.log(WARNING, () -> "Correcting angle #%d from %f ↓ %f"
							.formatted(index, oldValue, newValue));
					}
				}
				d.setAngle(an);
				anglePrev = an;
			}
			if (anglePrev >= C2)
			{
				final double a = anglePrev;
				logger.log(WARNING, () -> "Last divider angle is %f".formatted(a));
			}
			logger.log(TRACE, () -> "normalized divider angles → %s".formatted(
				layers.stream().map(ImageLayer::getDivider).map(Divider::getAngle).toList()));
		}
	}

	@Override
	public void accept(Divider divider)
	{
		final var mouseDragState = divider.getMouseDragState();
		final boolean dividerChanged = this.divider != null && this.divider != divider;
		if (dividerChanged)
		{
			logger.log(WARNING, () -> "Divider changed during mouse drag cycle.");
		}
		if (mouseDragState.isDragStart())
		{
			logger.log(TRACE, () -> ">>> start drag cycle");
			this.divider = divider;
			this.dividerDragCycle = new DividerDragCycle(layers, divider, getDividerMinGap());
		}
		if (mouseDragState.isDragRelease() || dividerChanged)
		{
			logger.log(TRACE, () -> "<<< release drag cycle");
			this.dividerDragCycle = null;
			this.divider = null;
			normalizeDividerAngles();
		}
		else
		{
			if (dividerDragCycle != null)
			{
				dividerDragCycle.run();
			}
		}
	}
}
