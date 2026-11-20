/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;

/// Class to describe the translation of image layers for global and local viewports.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class LayerTranslation
{
	private final ScrollBars scrollBars;
	private final DoubleProperty focusPointX = new SimpleDoubleProperty(0.5);
	private final DoubleProperty focusPointY = new SimpleDoubleProperty(0.5);
	private final DoubleProperty layersMaxWidth = new SimpleDoubleProperty();
	private final DoubleProperty layersMaxHeight = new SimpleDoubleProperty();
	private final ReadOnlyBooleanWrapper scrollingEnabledHorizontal = new ReadOnlyBooleanWrapper();
	private final ReadOnlyBooleanWrapper scrollingEnabledVertical = new ReadOnlyBooleanWrapper();
	private final ReadOnlyDoubleWrapper scrollRangeMaxWidth = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper scrollRangeMaxHeight = new ReadOnlyDoubleWrapper();
	private double mouseScrollStartX, mouseScrollStartY;

	LayerTranslation(ScrollableViewportBounds viewportBounds)
	{
		this.scrollBars = new ScrollBars(
			viewportBounds.widthProperty(), viewportBounds.heightProperty());
		scrollingEnabledHorizontal.bind(
			layersMaxWidth.greaterThan(viewportBounds.widthProperty()));
		scrollBars.horizontalVisibleProperty().bind(
			scrollBars.enabledProperty().and(scrollingEnabledHorizontal));
		scrollingEnabledVertical.bind(
			layersMaxHeight.greaterThan(viewportBounds.heightProperty()));
		scrollBars.verticalVisibleProperty().bind(
			scrollBars.enabledProperty().and(scrollingEnabledVertical));
		scrollRangeMaxWidth.bind(layersMaxWidth.subtract(viewportBounds.widthProperty()));
		scrollRangeMaxHeight.bind(layersMaxHeight.subtract(viewportBounds.heightProperty()));
		viewportBounds.scrollPosXProperty().bind(
			scrollBars.valueHProperty().multiply(scrollRangeMaxWidth.getReadOnlyProperty()));
		viewportBounds.scrollPosYProperty().bind(
			scrollBars.valueVProperty().multiply(scrollRangeMaxHeight.getReadOnlyProperty()));
	}

	ScrollBars getScrollBars()
	{
		return scrollBars;
	}

	DoubleProperty focusPointXProperty()
	{
		return focusPointX;
	}

	DoubleProperty focusPointYProperty()
	{
		return focusPointY;
	}

	DoubleProperty layersMaxWidthProperty()
	{
		return layersMaxWidth;
	}

	DoubleProperty layersMaxHeightProperty()
	{
		return layersMaxHeight;
	}

	ReadOnlyBooleanProperty scrollingEnabledHorizontalProperty()
	{
		return scrollingEnabledHorizontal.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty scrollingEnabledVerticalProperty()
	{
		return scrollingEnabledVertical.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollRangeMaxWidthProperty()
	{
		return scrollRangeMaxWidth.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollRangeMaxHeightProperty()
	{
		return scrollRangeMaxHeight.getReadOnlyProperty();
	}

	void saveScrollPos()
	{
		mouseScrollStartX = scrollBars.valueHProperty().doubleValue();
		mouseScrollStartY = scrollBars.valueVProperty().doubleValue();
	}

	double getMouseScrollStartX()
	{
		return mouseScrollStartX;
	}

	double getMouseScrollStartY()
	{
		return mouseScrollStartY;
	}
}
