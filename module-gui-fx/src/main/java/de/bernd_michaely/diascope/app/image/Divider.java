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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static de.bernd_michaely.diascope.app.image.Bindings.normalizeAngle;
import static de.bernd_michaely.diascope.app.image.Bindings.tan;
import static de.bernd_michaely.diascope.app.image.Border.BOTTOM;
import static de.bernd_michaely.diascope.app.image.Border.LEFT;
import static de.bernd_michaely.diascope.app.image.Border.RIGHT;
import static de.bernd_michaely.diascope.app.image.Border.TOP;
import static de.bernd_michaely.diascope.app.image.Divider.RotationType.*;
import static java.lang.Math.atan2;
import static java.lang.Math.ceil;
import static java.lang.Math.toDegrees;
import static javafx.beans.binding.Bindings.when;

/**
 * Class to handle image layer dividers.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class Divider
{
	private final DoubleProperty angle;
	private final ReadOnlyDoubleWrapper angleNorm;
	private final ReadOnlyDoubleProperty angleNormalized;
	private final ReadOnlyObjectWrapper<Border> border;
	private final ReadOnlyDoubleWrapper borderIntersectionX, borderIntersectionY;
	private final Line lineShape, lineEvent;

	enum RotationType
	{
		ALL_SYNCHRONOUS, SINGLE_ONLY, SINGLE_ADJUST_OTHERS, RELEASED
	}

	static class MouseDragState
	{
		private @MonotonicNonNull Runnable onRotate;
		private boolean dragStart = true;
		private double rotationAngle;
		private RotationType rotationType = RELEASED;

		private MouseDragState()
		{
		}

		private void fireEvent()
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

		private void handleMouseDragged(double rotationAngle, RotationType rotationType)
		{
			this.rotationAngle = rotationAngle;
			if (dragStart)
			{
				this.rotationType = rotationType;
			}
			try
			{
				fireEvent();
			}
			finally
			{
				if (dragStart)
				{
					dragStart = false;
				}
			}
		}

		private void handleMouseReleased()
		{
			rotationType = RELEASED;
			dragStart = true;
			rotationAngle = 0.0;
			fireEvent();
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
	}

	private final MouseDragState mouseDragState = new MouseDragState();

	Divider(CornerAngles cornerAngles,
		ReadOnlyDoubleProperty viewportWidth,
		ReadOnlyDoubleProperty viewportHeight,
		ReadOnlyDoubleProperty splitCenterX,
		ReadOnlyDoubleProperty splitCenterY,
		ReadOnlyDoubleProperty splitCenterDx,
		ReadOnlyDoubleProperty splitCenterDy)
	{
		this.angle = new SimpleDoubleProperty(0.0);
		this.border = new ReadOnlyObjectWrapper<>(Border.RIGHT);
		this.borderIntersectionX = new ReadOnlyDoubleWrapper();
		this.borderIntersectionY = new ReadOnlyDoubleWrapper();
		this.angleNorm = new ReadOnlyDoubleWrapper();
		angleNorm.bind(normalizeAngle(angle));
		this.angleNormalized = angleNorm.getReadOnlyProperty();
		border.bind(
			when(angleNormalized.lessThanOrEqualTo(cornerAngles.get(RIGHT)))
				.then(RIGHT).otherwise(
				when(angleNormalized.lessThanOrEqualTo(cornerAngles.get(BOTTOM)))
					.then(BOTTOM).otherwise(
					when(angleNormalized.lessThanOrEqualTo(cornerAngles.get(LEFT)))
						.then(LEFT).otherwise(
						when(angleNormalized.lessThanOrEqualTo(cornerAngles.get(TOP)))
							.then(TOP).otherwise(RIGHT)))));
		borderIntersectionX.bind(
			when(border.isEqualTo(RIGHT))
				.then(viewportWidth).otherwise(
				when(border.isEqualTo(BOTTOM))
					.then(splitCenterX.subtract(
						tan(angleNormalized.subtract(90.0))
							.multiply(splitCenterDy)))
					.otherwise(
						when(border.isEqualTo(LEFT))
							.then(0.0).otherwise( // TOP
							tan(angleNormalized.add(90.0))
								.multiply(splitCenterY)
								.add(splitCenterX)))));
		borderIntersectionY.bind(
			when(border.isEqualTo(RIGHT))
				.then(tan(angleNormalized)
					.multiply(splitCenterDx)
					.add(splitCenterY)).otherwise(
				when(border.isEqualTo(BOTTOM))
					.then(viewportHeight).otherwise(
					when(border.isEqualTo(LEFT))
						.then(splitCenterY.subtract(
							tan(angleNormalized.subtract(180.0))
								.multiply(splitCenterX)))
						.otherwise(0.0)))); // TOP
		final double sizeDefault = Font.getDefault().getSize();
		lineShape = new Line();
		lineShape.setStroke(ImageLayerShape.COLOR_UNSELECTED);
		lineShape.setStrokeWidth(ceil(sizeDefault / 10) * ImageLayerShape.STROKE_WIDTH_UNSELECTED);
		lineShape.startXProperty().bind(splitCenterX);
		lineShape.startYProperty().bind(splitCenterY);
		lineShape.endXProperty().bind(borderIntersectionX);
		lineShape.endYProperty().bind(borderIntersectionY);
		lineEvent = new Line();
		lineEvent.setStroke(Color.TRANSPARENT);
		lineEvent.setStrokeWidth(lineShape.getStrokeWidth() * 4);
		lineEvent.startXProperty().bind(lineShape.startXProperty());
		lineEvent.startYProperty().bind(lineShape.startYProperty());
		lineEvent.endXProperty().bind(lineShape.endXProperty());
		lineEvent.endYProperty().bind(lineShape.endYProperty());
		lineEvent.setCursor(Cursor.MOVE);
		lineEvent.setOnMouseDragged(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				final double dx = event.getX() - splitCenterX.get();
				final double dy = event.getY() - splitCenterY.get();
				final boolean shiftDown = event.isShiftDown();
				final boolean controlDown = event.isControlDown();
				final boolean altDown = event.isAltDown();
				RotationType rotationType = null;
				if (!altDown)
				{
					if (controlDown)
					{
						rotationType = shiftDown ? SINGLE_ADJUST_OTHERS : SINGLE_ONLY;
					}
					else
					{
						if (!shiftDown)
						{
							rotationType = ALL_SYNCHRONOUS;
						}
					}
				}
				event.consume();
				if (rotationType != null)
				{
					final double rotationAngle = toDegrees(atan2(dy, dx));
					mouseDragState.handleMouseDragged(rotationAngle, rotationType);
				}
			}
		});
		lineEvent.setOnMouseReleased(_ -> mouseDragState.handleMouseReleased());
	}

	MouseDragState getMouseDragState()
	{
		return mouseDragState;
	}

	void setOnRotate(Runnable onRotate)
	{
		mouseDragState.onRotate = onRotate;
	}

	DoubleProperty angleProperty()
	{
		return angle;
	}

	double getAngle()
	{
		return angle.get();
	}

	void setAngle(double angle)
	{
		this.angle.set(angle);
	}

	Border getBorder()
	{
		return border.getReadOnlyProperty().get();
	}

	Double getBorderIntersectionX()
	{
		return borderIntersectionX.getValue();
	}

	Double getBorderIntersectionY()
	{
		return borderIntersectionY.getValue();
	}

	List<Line> getLines()
	{
		return List.of(lineShape, lineEvent);
	}
}
