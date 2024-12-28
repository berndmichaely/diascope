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

import de.bernd_michaely.diascope.app.image.MultiImageView.ZoomMode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Double.max;
import static java.lang.Double.min;
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
	private final DoubleProperty rotateProperty;
	private final DoubleProperty zoomFixedProperty;
	private final BooleanProperty mirrorXProperty, mirrorYProperty;
	private final ObjectProperty<ZoomMode> zoomModeProperty;
	private final ScrollBar scrollBarH, scrollBarV;
	private final ObservableBooleanValue scrollBarsDisabled;
	private final DoubleProperty focusPointX, focusPointY;
	private final DoubleProperty layersMaxWidth, layersMaxHeight;
	private final ReadOnlyDoubleWrapper scrollRangeMaxWidth, scrollRangeMaxHeight;
	private final ReadOnlyDoubleWrapper scrollPosX, scrollPosY;
	private double mouseDragStartX, mouseDragStartY;
	private double mouseScrollStartX, mouseScrollStartY;

	Viewport(ObservableBooleanValue scrollBarsDisabled)
	{
		this.rotateProperty = new SimpleDoubleProperty(0.0);
		this.zoomFixedProperty = new SimpleDoubleProperty(1.0);
		this.zoomModeProperty = new SimpleObjectProperty<>(ZoomMode.getDefault());
		this.mirrorXProperty = new SimpleBooleanProperty();
		this.mirrorYProperty = new SimpleBooleanProperty();
		this.focusPointX = new SimpleDoubleProperty(0.5);
		this.focusPointY = new SimpleDoubleProperty(0.5);
		this.layersMaxWidth = new SimpleDoubleProperty();
		this.layersMaxHeight = new SimpleDoubleProperty();
		this.scrollRangeMaxWidth = new ReadOnlyDoubleWrapper();
		this.scrollRangeMaxHeight = new ReadOnlyDoubleWrapper();
		this.scrollPosX = new ReadOnlyDoubleWrapper();
		this.scrollPosY = new ReadOnlyDoubleWrapper();
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
		scrollRangeMaxWidth.bind(layersMaxWidth.subtract(paneLayers.widthProperty()));
		scrollRangeMaxHeight.bind(layersMaxHeight.subtract(paneLayers.heightProperty()));
		scrollPosX.bind(scrollBarH.valueProperty().multiply(scrollRangeMaxWidth));
		scrollPosY.bind(scrollBarV.valueProperty().multiply(scrollRangeMaxHeight));
		paneLayers.setOnMousePressed(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				mouseDragStartX = event.getX();
				mouseDragStartY = event.getY();
				mouseScrollStartX = scrollBarH.getValue();
				mouseScrollStartY = scrollBarV.getValue();
			}
		});
		paneLayers.setOnMouseDragged(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY))
			{
				final double px = scrollRangeMaxWidth.doubleValue();
				final double py = scrollRangeMaxHeight.doubleValue();
				final double dx = (event.getX() - mouseDragStartX) / px;
				final double dy = (event.getY() - mouseDragStartY) / py;
				final double x = min(max(0.0, mouseScrollStartX - dx), 1.0);
				final double y = min(max(0.0, mouseScrollStartY - dy), 1.0);
				scrollBarH.setValue(x);
				scrollBarV.setValue(y);
			}
		});
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

	ScrollBar getScrollBarH()
	{
		return scrollBarH;
	}

	ScrollBar getScrollBarV()
	{
		return scrollBarV;
	}

	ObservableBooleanValue scrollBarsDisabledProperty()
	{
		return scrollBarsDisabled;
	}

	DoubleProperty rotateProperty()
	{
		return rotateProperty;
	}

	DoubleProperty zoomFixedProperty()
	{
		return zoomFixedProperty;
	}

	ObjectProperty<ZoomMode> zoomModeProperty()
	{
		return zoomModeProperty;
	}

	BooleanProperty mirrorXProperty()
	{
		return mirrorXProperty;
	}

	BooleanProperty mirrorYProperty()
	{
		return mirrorYProperty;
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

	DoubleProperty layersMaxWidthProperty()
	{
		return layersMaxWidth;
	}

	DoubleProperty layersMaxHeightProperty()
	{
		return layersMaxHeight;
	}

	ReadOnlyDoubleProperty scrollRangeMaxWidthProperty()
	{
		return scrollRangeMaxWidth.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollRangeMaxHeightProperty()
	{
		return scrollRangeMaxHeight.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollPosXProperty()
	{
		return scrollPosX.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty scrollPosYProperty()
	{
		return scrollPosY.getReadOnlyProperty();
	}

	Pane getPaneLayers()
	{
		return paneLayers;
	}
}
