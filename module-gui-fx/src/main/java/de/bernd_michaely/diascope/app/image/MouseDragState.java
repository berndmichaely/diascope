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

import de.bernd_michaely.diascope.app.image.DividerRotationControl.RotationType;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static de.bernd_michaely.diascope.app.image.DividerRotationControl.RotationType.*;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

/// Class to handle mouse drag events.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class MouseDragState
{
	private final ReadOnlyDoubleProperty originX, originY;
	private @MonotonicNonNull Runnable onRotate;
	private boolean dragStart = true;
	private boolean dragRelease = false;
	private double rotationAngle;
	private RotationType rotationType = NEUTRAL;

	MouseDragState(ReadOnlyDoubleProperty originX, ReadOnlyDoubleProperty originY)
	{
		this.originX = originX;
		this.originY = originY;
	}

	void setListenersFor(Node node)
	{
		node.setOnMouseDragged(this::handleMouseDragged);
		node.setOnMouseReleased(this::handleMouseReleased);
	}

	private void fireListenerEvent()
	{
		if (onRotate != null)
		{
			onRotate.run();
		}
		else
		{
			throw new IllegalStateException("Divider callback not initialized");
		}
	}

	private boolean handleMouseEvent(MouseEvent event)
	{
		if (event.getButton().equals(MouseButton.PRIMARY))
		{
			final double dx = event.getX() - originX.get();
			final double dy = event.getY() - originY.get();
			final boolean shiftDown = event.isShiftDown();
			final boolean controlDown = event.isControlDown();
			final boolean altDown = event.isAltDown();
			var type = NEUTRAL;
			if (!altDown)
			{
				if (!shiftDown && !controlDown)
				{
					type = WHEEL;
				}
				else if (!shiftDown && controlDown)
				{
					type = SINGLE;
				}
				else if (shiftDown && !controlDown)
				{
					type = FOLD;
				}
			}
			event.consume();
			this.rotationType = type;
			final double oldValue = this.rotationAngle;
			final double newValue = toDegrees(atan2(dy, dx));
			this.rotationAngle = newValue;
			return oldValue != newValue;
		}
		else
		{
			return false;
		}
	}

	void setOnRotate(Runnable onRotate)
	{
		this.onRotate = onRotate;
	}

	void handleMouseDragged(MouseEvent event)
	{
		try
		{
			if (handleMouseEvent(event) || dragStart)
			{
				fireListenerEvent();
			}
		}
		finally
		{
			dragStart = false;
		}
	}

	void handleMouseReleased(MouseEvent event)
	{
		try
		{
			handleMouseEvent(event);
			dragRelease = true;
			fireListenerEvent();
		}
		finally
		{
			dragStart = true;
			dragRelease = false;
			rotationType = NEUTRAL;
			rotationAngle = 0.0;
		}
	}

	double getRotationAngle()
	{
		return rotationAngle;
	}

	RotationType getRotationType()
	{
		return rotationType;
	}

	boolean isDragStart()
	{
		return dragStart;
	}

	boolean isDragRelease()
	{
		return dragRelease;
	}
}
