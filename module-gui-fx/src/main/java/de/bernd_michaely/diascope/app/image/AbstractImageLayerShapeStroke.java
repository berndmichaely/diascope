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

import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import org.checkerframework.checker.nullness.qual.Nullable;

import static javafx.beans.binding.Bindings.when;

/// Base class to describe an ImageLayer selection shape.
/// Adds defaults for stroke.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
abstract sealed class AbstractImageLayerShapeStroke extends AbstractImageLayerShape
	permits ImageLayerShapeSplit
{
	private static final double STROKE_WIDTH_SELECTED = 4 * STROKE_WIDTH_UNSELECTED;
	private final BooleanProperty dualSpotSelected;
	private final ReadOnlyObjectWrapper<Paint> strokeSelectedPaint;

	AbstractImageLayerShapeStroke(boolean unselectedVisible,
		@Nullable Consumer<MouseEvent> onMouseDragInit,
		@Nullable Consumer<MouseEvent> onMouseDragged)
	{
		super(unselectedVisible, onMouseDragInit, onMouseDragged);
		this.dualSpotSelected = new SimpleBooleanProperty();
		this.strokeSelectedPaint = new ReadOnlyObjectWrapper<>();
		strokeSelectedPaint.bind(when(dualSpotSelected)
			.then(COLORS_SELECTED.getLast())
			.otherwise(COLORS_SELECTED.getFirst()));
	}

	BooleanProperty dualSpotSelectedProperty()
	{
		return dualSpotSelected;
	}

	@Override
	ObservableObjectValue<Paint> getStrokeSelectedPaint()
	{
		return strokeSelectedPaint.getReadOnlyProperty();
	}

	@Override
	double getStrokeWidthSelected()
	{
		return STROKE_WIDTH_SELECTED;
	}
}
