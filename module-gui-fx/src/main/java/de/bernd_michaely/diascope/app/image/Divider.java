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
import javafx.scene.shape.Line;

import static de.bernd_michaely.diascope.app.image.Bindings.normalizeAngle;
import static de.bernd_michaely.diascope.app.image.Bindings.tan;
import static de.bernd_michaely.diascope.app.image.Border.*;
import static javafx.beans.binding.Bindings.when;

/// Class to handle image layer split dividers.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class Divider extends DividerBase implements AutoCloseable
{
	private final DoubleProperty angle = new SimpleDoubleProperty();
	private final ReadOnlyDoubleWrapper angleNorm = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleProperty angleNormalized = angleNorm.getReadOnlyProperty();
	private final ReadOnlyObjectWrapper<Border> border = new ReadOnlyObjectWrapper<>();
	private final ReadOnlyDoubleWrapper borderIntersectionX = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper borderIntersectionY = new ReadOnlyDoubleWrapper();
	private final MouseDragState mouseDragState;

	Divider(CornerAngles cornerAngles,
		ReadOnlyDoubleProperty viewportWidth,
		ReadOnlyDoubleProperty viewportHeight,
		ReadOnlyDoubleProperty splitCenterX,
		ReadOnlyDoubleProperty splitCenterY,
		ReadOnlyDoubleProperty splitCenterDx,
		ReadOnlyDoubleProperty splitCenterDy)
	{
		angleNorm.bind(normalizeAngle(angle));
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
		this.mouseDragState = new MouseDragState(splitCenterX, splitCenterY);
	}

	static Divider createInstance(Viewport viewport)
	{
		final var splitCenter = viewport.getSplitCenter();
		final var divider = new Divider(viewport.getCornerAngles(),
			viewport.widthProperty(), viewport.heightProperty(),
			splitCenter.xProperty(), splitCenter.yProperty(),
			splitCenter.dxProperty(), splitCenter.dyProperty());
		final Line lineShape = divider.getLineShape();
		lineShape.startXProperty().bind(splitCenter.xProperty());
		lineShape.startYProperty().bind(splitCenter.yProperty());
		lineShape.endXProperty().bind(divider.borderIntersectionX);
		lineShape.endYProperty().bind(divider.borderIntersectionY);
		final Line lineEvent = divider.getLineEvent();
		lineEvent.startXProperty().bind(lineShape.startXProperty());
		lineEvent.startYProperty().bind(lineShape.startYProperty());
		lineEvent.endXProperty().bind(lineShape.endXProperty());
		lineEvent.endYProperty().bind(lineShape.endYProperty());
		divider.mouseDragState.setListenersFor(lineEvent);
		return divider;
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

	@Override
	public void close()
	{
		angleProperty().unbind();
		mouseDragState.removeListenersFor(getLineEvent());
		mouseDragState.setOnRotate(null);
	}
}
