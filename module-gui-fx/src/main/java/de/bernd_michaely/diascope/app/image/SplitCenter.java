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

import java.util.Collection;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Math.clamp;

/// Class to handle the split center shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class SplitCenter
{
	private static final Color COLOR_DEFAULT_SPLIT = Color.WHITESMOKE;
	private static final Color COLOR_HOVER_SPLIT = Color.MEDIUMSPRINGGREEN;
	private final ReadOnlyDoubleWrapper splitCenterX, splitCenterY;
	private final ReadOnlyDoubleWrapper splitCenterDx, splitCenterDy;
	private final Circle shapeSplitCenter;
	private final Collection<Node> shapes;
	private final double diameter, radius;
	private final Runnable center;
	private boolean positionInitialized;

	SplitCenter(ReadOnlyDoubleProperty viewportWidth, ReadOnlyDoubleProperty viewportHeight)
	{
		this.splitCenterX = new ReadOnlyDoubleWrapper();
		this.splitCenterY = new ReadOnlyDoubleWrapper();
		this.splitCenterDx = new ReadOnlyDoubleWrapper();
		this.splitCenterDy = new ReadOnlyDoubleWrapper();
		splitCenterDx.bind(viewportWidth.subtract(splitCenterX));
		splitCenterDy.bind(viewportHeight.subtract(splitCenterY));
		this.diameter = Font.getDefault().getSize();
		this.radius = diameter / 2.0;
		this.shapeSplitCenter = new Circle(radius);
		shapeSplitCenter.setFill(COLOR_DEFAULT_SPLIT);
		shapeSplitCenter.setOpacity(0.8);
		shapeSplitCenter.setCursor(Cursor.MOVE);
		shapeSplitCenter.centerXProperty().bind(splitCenterX);
		shapeSplitCenter.centerYProperty().bind(splitCenterY);
		shapeSplitCenter.setOnMouseEntered(_ -> shapeSplitCenter.setFill(COLOR_HOVER_SPLIT));
		shapeSplitCenter.setOnMouseExited(_ -> shapeSplitCenter.setFill(COLOR_DEFAULT_SPLIT));
		this.shapes = List.of(shapeSplitCenter);
		viewportWidth.addListener(onChange((oldWidth, newWidth) ->
		{
			final double w = newWidth.doubleValue();
			final double ow = oldWidth.doubleValue();
			final double x = ow != 0 ? splitCenterX.get() * w / ow : 0;
			splitCenterX.set(normX(diameter, radius, w, x));
		}));
		viewportHeight.addListener(onChange((oldHeight, newHeight) ->
		{
			final double h = newHeight.doubleValue();
			final double oh = oldHeight.doubleValue();
			final double y = oh != 0 ? splitCenterY.get() * h / oh : 0;
			splitCenterY.set(normY(diameter, radius, h, y));
		}));
		shapeSplitCenter.setOnMouseDragged(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				splitCenterX.set(normX(diameter, radius, viewportWidth.get(), event.getX()));
				splitCenterY.set(normY(diameter, radius, viewportHeight.get(), event.getY()));
				event.consume();
			}
		});
		this.center = () ->
		{
			splitCenterX.set(viewportWidth.get() / 2.0);
			splitCenterY.set(viewportHeight.get() / 2.0);
		};
		shapeSplitCenter.visibleProperty().addListener(onChange(visible ->
		{
			if (!positionInitialized && visible)
			{
				center.run();
				positionInitialized = true;
			}
		}));
	}

	static double normX(double diameter, double radius, double width, double x)
	{
		return width < diameter ? width / 2.0 : clamp(x, radius, width - radius);
	}

	static double normY(double diameter, double radius, double height, double y)
	{
		return height < diameter ? height / 2.0 : clamp(y, radius, height - radius);
	}

	/// Centers the split center in the viewport.
	void center()
	{
		this.center.run();
	}

	BooleanProperty enabledProperty()
	{
		return shapeSplitCenter.visibleProperty();
	}

	ReadOnlyDoubleProperty xProperty()
	{
		return splitCenterX;
	}

	ReadOnlyDoubleProperty yProperty()
	{
		return splitCenterY;
	}

	ReadOnlyDoubleProperty dxProperty()
	{
		return splitCenterDx;
	}

	ReadOnlyDoubleProperty dyProperty()
	{
		return splitCenterDy;
	}

	Collection<Node> getShapes()
	{
		return shapes;
	}
}
