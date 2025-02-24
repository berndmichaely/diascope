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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;

/// Class to handle split center shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class SplitCenter
{
	private final ReadOnlyDoubleWrapper splitCenterX, splitCenterY;
	private final ReadOnlyDoubleWrapper splitCenterDx, splitCenterDy;
	private final Circle shapeSplitCenter;
	private boolean posInitialized;

	SplitCenter(ReadOnlyDoubleProperty viewportWidth, ReadOnlyDoubleProperty viewportHeight)
	{
		this.splitCenterX = new ReadOnlyDoubleWrapper();
		this.splitCenterY = new ReadOnlyDoubleWrapper();
		this.splitCenterDx = new ReadOnlyDoubleWrapper();
		this.splitCenterDy = new ReadOnlyDoubleWrapper();
		splitCenterDx.bind(viewportWidth.subtract(splitCenterX));
		splitCenterDy.bind(viewportHeight.subtract(splitCenterY));
		final double radius = Font.getDefault().getSize() / 2.0;
		this.shapeSplitCenter = new Circle();
		shapeSplitCenter.setRadius(radius);
		shapeSplitCenter.setFill(Color.WHITESMOKE);
		shapeSplitCenter.setOpacity(0.8);
		shapeSplitCenter.setCursor(Cursor.MOVE);
		shapeSplitCenter.centerXProperty().bind(splitCenterX);
		shapeSplitCenter.centerYProperty().bind(splitCenterY);
		shapeSplitCenter.setOnMouseDragged(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				splitCenterX.set(event.getX());
				splitCenterY.set(event.getY());
				event.consume();
			}
		});
		shapeSplitCenter.visibleProperty().addListener(onChange(visible ->
		{
			if (!posInitialized && visible)
			{
				// center:
				splitCenterX.set(viewportWidth.doubleValue() / 2.0);
				splitCenterY.set(viewportHeight.doubleValue() / 2.0);
				posInitialized = true;
			}
		}));
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

	Shape getShape()
	{
		return shapeSplitCenter;
	}
}
