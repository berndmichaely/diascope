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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import static de.bernd_michaely.diascope.app.image.Bindings.normalizeAngle;
import static de.bernd_michaely.diascope.app.image.Bindings.tan;
import static de.bernd_michaely.diascope.app.image.Border.*;
import static java.lang.Math.ceil;
import static javafx.beans.binding.Bindings.when;

/**
 * Class to handle image layer dividers.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class Divider
{
	private static final Color COLOR_DEFAULT = ImageLayerShape.COLOR_UNSELECTED;
	private static final Color COLOR_HOVER = Color.LIGHTCORAL;
	private final DoubleProperty angle;
	private final ReadOnlyDoubleWrapper angleNorm;
	private final ReadOnlyDoubleProperty angleNormalized;
	private final ReadOnlyObjectWrapper<Border> border;
	private final ReadOnlyDoubleWrapper borderIntersectionX, borderIntersectionY;
	private final Line lineShape, lineEvent;
	private final MouseDragState mouseDragState;

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
		lineShape.setStroke(COLOR_DEFAULT);
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
		lineEvent.setOnMouseEntered(_ -> lineShape.setStroke(COLOR_HOVER));
		lineEvent.setOnMouseExited(_ -> lineShape.setStroke(COLOR_DEFAULT));
		this.mouseDragState = new MouseDragState(splitCenterX, splitCenterY);
		mouseDragState.setListenersFor(lineEvent);
	}

	MouseDragState getMouseDragState()
	{
		return mouseDragState;
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

	Line getLineShape()
	{
		return lineShape;
	}

	Line getLineEvent()
	{
		return lineEvent;
	}

	void clear()
	{
		angleProperty().unbind();
		mouseDragState.removeListenersFor(lineEvent);
		mouseDragState.setOnRotate(null);
	}
}
