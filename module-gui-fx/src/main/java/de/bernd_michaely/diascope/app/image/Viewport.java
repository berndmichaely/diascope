/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
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
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

/**
 * Class to describe the viewport of a MultiImageView containing all images.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class Viewport
{
	private final AnchorPane paneLayers;
	private final ScrollBar scrollBarH, scrollBarV;
	private final ReadOnlyBooleanProperty scrollBarsDisabled;
	private final ReadOnlyDoubleWrapper viewportWidthScroll, viewportHeightScroll;
	private final DoubleProperty focusPointX, focusPointY;

	Viewport(ReadOnlyBooleanProperty scrollBarsDisabled)
	{
		this.focusPointX = new SimpleDoubleProperty(0.5);
		this.focusPointY = new SimpleDoubleProperty(0.5);
		this.scrollBarH = new ScrollBar();
		initScrollBar(scrollBarH);
		scrollBarH.setOrientation(Orientation.HORIZONTAL);
//		scrollBarH.visibleProperty().addListener(onChange(becomesVisible ->
//		{
//			if (becomesVisible)
//			{
//				scrollBarH.setValue(focusPointX.doubleValue());
//			}
//		}));
		this.scrollBarV = new ScrollBar();
		initScrollBar(scrollBarV);
		scrollBarV.setOrientation(Orientation.VERTICAL);
//		scrollBarV.visibleProperty().addListener(onChange(becomesVisible ->
//		{
//			if (becomesVisible)
//			{
//				scrollBarV.setValue(focusPointY.doubleValue());
//			}
//		}));
		this.scrollBarsDisabled = scrollBarsDisabled;
		this.paneLayers = new AnchorPane();
		paneLayers.setBackground(Background.fill(Color.BLACK));
		paneLayers.setMinSize(0, 0);
		paneLayers.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		paneLayers.getChildren().addAll(scrollBarH, scrollBarV);
		setLeftAnchor(scrollBarH, 0.0);
		scrollBarV.widthProperty().addListener(onChange(w -> setRightAnchor(scrollBarH, w.doubleValue())));
		setBottomAnchor(scrollBarH, 0.0);
		setTopAnchor(scrollBarV, 0.0);
		setRightAnchor(scrollBarV, 0.0);
		scrollBarH.heightProperty().addListener(onChange(h -> setBottomAnchor(scrollBarV, h.doubleValue())));
		this.viewportWidthScroll = new ReadOnlyDoubleWrapper();
		viewportWidthScroll.bind(paneLayers.widthProperty().subtract(scrollBarV.widthProperty()));
		this.viewportHeightScroll = new ReadOnlyDoubleWrapper();
		viewportHeightScroll.bind(paneLayers.heightProperty().subtract(scrollBarH.heightProperty()));
	}

	private static void initScrollBar(ScrollBar scrollBar)
	{
		scrollBar.setMin(0.0);
		scrollBar.setMax(1.0);
		scrollBar.setValue(0.5);
		scrollBar.setUnitIncrement(0.05);
		scrollBar.setBlockIncrement(0.2);
	}

	ScrollBar getScrollBarH()
	{
		return scrollBarH;
	}

	ScrollBar getScrollBarV()
	{
		return scrollBarV;
	}

	ReadOnlyDoubleProperty viewportWidthScrollProperty()
	{
		return viewportWidthScroll.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty viewportHeightScrollProperty()
	{
		return viewportHeightScroll.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty scrollBarsDisabledProperty()
	{
		return scrollBarsDisabled;
	}

	DoubleProperty focusPointX()
	{
		return focusPointX;
	}

	DoubleProperty focusPointY()
	{
		return focusPointY;
	}

	ReadOnlyDoubleProperty widthProperty()
	{
		return paneLayers.widthProperty();
	}

	ReadOnlyDoubleProperty heightProperty()
	{
		return paneLayers.heightProperty();
	}

	Pane getPaneLayers()
	{
		return paneLayers;
	}
}
