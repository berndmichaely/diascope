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
import java.util.function.Consumer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.Bindings.*;
import static de.bernd_michaely.diascope.app.image.DividerRotationControl.RotationType.*;

/// Class to handle divider rorations.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class DividerRotationControl implements Consumer<Divider>
{
	private static final double DEFAULT_DIVIDER_MIN_GAP = 10.0;
	private final DoubleProperty dividerMinGapProperty;
	private final List<ImageLayer> layers;
	private @Nullable Divider divider;
	private @Nullable DividerDragCycle dragCycle;

	enum RotationType
	{
		NEUTRAL, WHEEL, SINGLE, FOLD, RELEASED
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
		if (mouseDragState.isDragStart())
		{
			this.divider = divider;
			this.dragCycle = new DividerDragCycle(layers, divider, getDividerMinGap());
		}
		else if (this.divider != divider)
		{
			throw new IllegalStateException("Divider changed during drag cycle.");
		}
		switch (rotationType)
		{
			case RELEASED ->
			{
				this.dragCycle = null;
				this.divider = null;
				normalizeDividerAngles();
			}
			default ->
			{
				if (dragCycle != null)
				{
					dragCycle.drag(rotationType);
				}
				else
				{
					throw new IllegalStateException("DragCycle is null during drag operation.");
				}
			}
		}
	}
}
