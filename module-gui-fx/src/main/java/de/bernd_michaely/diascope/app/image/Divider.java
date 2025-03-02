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

import java.util.function.DoubleConsumer;
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
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.Bindings.normalizeAngle;
import static de.bernd_michaely.diascope.app.image.Bindings.tan;
import static de.bernd_michaely.diascope.app.image.Border.BOTTOM;
import static de.bernd_michaely.diascope.app.image.Border.LEFT;
import static de.bernd_michaely.diascope.app.image.Border.RIGHT;
import static de.bernd_michaely.diascope.app.image.Border.TOP;
import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;
import static javafx.beans.binding.Bindings.when;

/**
 * Class to handle image layer dividers.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class Divider
{
	private static final double PI_2 = Math.PI * 0.5;
	private static final double PI_3_2 = Math.PI * 1.5;
	private final DoubleProperty angle;
	private final ReadOnlyDoubleWrapper angleNorm;
	private final ReadOnlyDoubleProperty angleNormalized;
	private final ReadOnlyObjectWrapper<Border> border;
	private final ReadOnlyDoubleWrapper borderIntersectionX, borderIntersectionY;
	private final Line line;
	private @Nullable DoubleConsumer onRotate;

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
		this.line = new Line();
		line.setStroke(Color.TRANSPARENT);
		line.setStrokeWidth(Math.round(Font.getDefault().getSize() / 2.5));
		line.setCursor(Cursor.MOVE);
		line.startXProperty().bind(splitCenterX);
		line.startYProperty().bind(splitCenterY);
		line.endXProperty().bind(borderIntersectionX);
		line.endYProperty().bind(borderIntersectionY);
		line.setOnMouseDragged(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				final double dx = event.getX() - splitCenterX.get();
				final double dy = event.getY() - splitCenterY.get();
				event.consume();
				if (onRotate != null)
				{
					onRotate.accept(toDegrees(
						dx == 0.0 ? (dy > 0.0 ? PI_2 : (dy < 0 ? PI_3_2 : 0.0)) :
							(dx > 0.0 ? atan(dy / dx) : PI - atan(dy / -dx))));
				}
				else
				{
					throw new IllegalStateException("Divider callback not initialized");
				}
			}
		});
	}

	void setOnRotate(DoubleConsumer onRotate)
	{
		this.onRotate = onRotate;
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

	Line getLine()
	{
		return line;
	}
}
