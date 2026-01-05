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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.unmodifiableObservableList;

/// Class to handle viewport scrollbars.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ScrollBars
{
	private final ScrollBar scrollBarH, scrollBarV;
	private final ObservableList<Node> scrollBars;
	private final BooleanProperty enabled = new SimpleBooleanProperty();

	ScrollBars(ReadOnlyDoubleProperty viewportWidth, ReadOnlyDoubleProperty viewportHeight)
	{
		this.scrollBarH = new ScrollBar();
		initScrollBar(scrollBarH);
		scrollBarH.setOrientation(Orientation.HORIZONTAL);
		this.scrollBarV = new ScrollBar();
		initScrollBar(scrollBarV);
		scrollBarV.setOrientation(Orientation.VERTICAL);
		scrollBarH.visibleProperty().bind(enabled);
		scrollBarV.visibleProperty().bind(enabled);
		this.scrollBars = unmodifiableObservableList(observableArrayList(scrollBarH, scrollBarV));
		// anchor:
		final Runnable adjustWidth = () ->
		{
			final double w = viewportWidth.get() - scrollBarV.getWidth();
			scrollBarH.setPrefWidth(w);
			scrollBarV.relocate(w, 0);
		};
		final Runnable adjustHeight = () ->
		{
			final double h = viewportHeight.get() - scrollBarH.getHeight();
			scrollBarV.setPrefHeight(h);
			scrollBarH.relocate(0, h);
		};
		scrollBarH.heightProperty().addListener(onChange(adjustHeight));
		scrollBarV.widthProperty().addListener(onChange(adjustWidth));
		viewportWidth.addListener(onChange(adjustWidth));
		viewportHeight.addListener(onChange(adjustHeight));
	}

	private static void initScrollBar(ScrollBar scrollBar)
	{
		scrollBar.setMin(0.0);
		scrollBar.setMax(1.0);
		scrollBar.setValue(0.5);
		scrollBar.setUnitIncrement(0.05);
		scrollBar.setBlockIncrement(0.2);
		scrollBar.setOpacity(0.85);
	}

	DoubleProperty valueHProperty()
	{
		return scrollBarH.valueProperty();
	}

	DoubleProperty valueVProperty()
	{
		return scrollBarV.valueProperty();
	}

	BooleanProperty enabledProperty()
	{
		return enabled;
	}

	BooleanProperty horizontalVisibleProperty()
	{
		return scrollBarH.visibleProperty();
	}

	BooleanProperty verticalVisibleProperty()
	{
		return scrollBarV.visibleProperty();
	}

	/// Returns an unmodifiable list of scrollbars.
	///
	/// @return an unmodifiable list of scrollbars
	///
	ObservableList<Node> getScrollBars()
	{
		return scrollBars;
	}
}
