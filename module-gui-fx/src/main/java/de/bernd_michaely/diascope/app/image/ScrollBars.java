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
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

/// Class to handle viewport scrollbars.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ScrollBars
{
	private final AnchorPane pane;
	private final ScrollBar scrollBarH, scrollBarV;

	ScrollBars()
	{
		this.scrollBarH = new ScrollBar();
		initScrollBar(scrollBarH);
		scrollBarH.setOrientation(Orientation.HORIZONTAL);
		this.scrollBarV = new ScrollBar();
		initScrollBar(scrollBarV);
		scrollBarV.setOrientation(Orientation.VERTICAL);
		setLeftAnchor(scrollBarH, 0.0);
		scrollBarV.widthProperty().addListener(onChange(w -> setRightAnchor(scrollBarH, w.doubleValue())));
		setBottomAnchor(scrollBarH, 0.0);
		setTopAnchor(scrollBarV, 0.0);
		setRightAnchor(scrollBarV, 0.0);
		scrollBarH.heightProperty().addListener(onChange(h -> setBottomAnchor(scrollBarV, h.doubleValue())));
		this.pane = new AnchorPane(scrollBarH, scrollBarV);
		scrollBarH.visibleProperty().bind(pane.visibleProperty());
		scrollBarV.visibleProperty().bind(pane.visibleProperty());
	}

	private static void initScrollBar(ScrollBar scrollBar)
	{
		scrollBar.setMin(0.0);
		scrollBar.setMax(1.0);
		scrollBar.setValue(0.5);
		scrollBar.setUnitIncrement(0.05);
		scrollBar.setBlockIncrement(0.2);
		scrollBar.setOpacity(0.75);
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
		return getPane().visibleProperty();
	}

	BooleanProperty horizontalVisibleProperty()
	{
		return scrollBarH.visibleProperty();
	}

	BooleanProperty verticalVisibleProperty()
	{
		return scrollBarV.visibleProperty();
	}

	Pane getPane()
	{
		return pane;
	}
}
