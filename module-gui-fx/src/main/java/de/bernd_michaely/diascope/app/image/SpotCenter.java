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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;

import static javafx.beans.binding.Bindings.when;

/// Class to handle the spot center shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class SpotCenter
{
	private static final Color COLOR_DEFAULT_SPOT = Color.LIGHTCORAL;
	private static final Color COLOR_HOVER_SPOT = Color.RED;
	private final DoubleProperty spotCenterX = new SimpleDoubleProperty();
	private final DoubleProperty spotCenterY = new SimpleDoubleProperty();
	private final BooleanProperty hover = new SimpleBooleanProperty();
	private final List<? extends Shape> shapes;
	private final Group group = new Group();
	private final double diameter, radius;

	SpotCenter(ReadOnlyDoubleProperty viewportWidth, ReadOnlyDoubleProperty viewportHeight)
	{
		this.diameter = Font.getDefault().getSize();
		this.radius = diameter / 2.0;
		final Line lineCrossLeftAsc = new Line();
		final Line lineCrossRightAsc = new Line();
		this.shapes = List.of(lineCrossLeftAsc, lineCrossRightAsc);
		group.getChildren().addAll(shapes);
		shapes.forEach(line ->
		{
			line.strokeProperty().bind(when(hover).then(COLOR_HOVER_SPOT).otherwise(COLOR_DEFAULT_SPOT));
			line.setStrokeLineCap(StrokeLineCap.ROUND);
			line.setStrokeWidth(5);
			line.setOpacity(0.8);
		});
		lineCrossLeftAsc.startXProperty().bind(spotCenterX.subtract(radius));
		lineCrossLeftAsc.startYProperty().bind(spotCenterY.subtract(radius));
		lineCrossLeftAsc.endXProperty().bind(spotCenterX.add(radius));
		lineCrossLeftAsc.endYProperty().bind(spotCenterY.add(radius));
		lineCrossRightAsc.startXProperty().bind(spotCenterX.add(radius));
		lineCrossRightAsc.startYProperty().bind(spotCenterY.subtract(radius));
		lineCrossRightAsc.endXProperty().bind(spotCenterX.subtract(radius));
		lineCrossRightAsc.endYProperty().bind(spotCenterY.add(radius));
	}

	BooleanProperty hoverProperty()
	{
		return hover;
	}

	BooleanProperty enabledProperty()
	{
		return group.visibleProperty();
	}

	DoubleProperty xProperty()
	{
		return spotCenterX;
	}

	DoubleProperty yProperty()
	{
		return spotCenterY;
	}

	Group getShape()
	{
		return group;
	}
}
