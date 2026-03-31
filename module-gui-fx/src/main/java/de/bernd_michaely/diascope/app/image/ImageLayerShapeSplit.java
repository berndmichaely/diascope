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

import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.checkerframework.checker.nullness.qual.Nullable;

import static javafx.beans.binding.Bindings.when;

/// Class to describe an ImageLayer selection shape for SPLIT mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayerShapeSplit extends AbstractImageLayerShapeStroke
{
	private final Rectangle rectangle = new Rectangle();
	private final Rectangle rectangleClip = new Rectangle();
	private final Polygon polygon = new Polygon();
	private final Polygon polygonClip = new Polygon();
	private final ReadOnlyObjectWrapper<@Nullable Shape> clip;

	ImageLayerShapeSplit(EnumProperties<Mode> modeProperties)
	{
		super(false, null, null);
		this.clip = new ReadOnlyObjectWrapper<>();
		clip.bind(when(modeProperties.isValueProperty(Mode.SPLIT))
			.<@Nullable Shape>then(polygonClip).otherwise(
			when(modeProperties.isValueProperty(Mode.GRID))
				.<@Nullable Shape>then(rectangleClip).otherwise((@Nullable Shape) null)));
		rectangleClip.xProperty().bind(rectangle.xProperty());
		rectangleClip.yProperty().bind(rectangle.yProperty());
		rectangleClip.widthProperty().bind(rectangle.widthProperty());
		rectangleClip.heightProperty().bind(rectangle.heightProperty());
		rectangleClip.arcWidthProperty().bind(rectangle.arcWidthProperty());
		rectangleClip.arcHeightProperty().bind(rectangle.arcHeightProperty());
		initShape(rectangle);
		initShape(polygon);
	}

	ReadOnlyObjectProperty<@Nullable Shape> clipProperty()
	{
		return clip.getReadOnlyProperty();
	}

	void bindViewportBounds(ViewportBoundsLocal viewportBounds)
	{
		rectangle.xProperty().bind(viewportBounds.xProperty());
		rectangle.yProperty().bind(viewportBounds.yProperty());
		rectangle.widthProperty().bind(viewportBounds.widthProperty());
		rectangle.heightProperty().bind(viewportBounds.heightProperty());
	}

	void setPolygonPoints(Double... points)
	{
		polygon.getPoints().setAll(points);
		polygonClip.getPoints().setAll(points);
	}

	void setPolygonPoints(ClippingPointsListener.Points points)
	{
		//polygon.getPoints().setAll(points);
		//polygonClip.getPoints().setAll(points);
		// … doesn't seem to work …
		setPolygonPoints(points.toArray());
	}

	Rectangle getRectangle()
	{
		return rectangle;
	}

	Polygon getPolygon()
	{
		return polygon;
	}
}
