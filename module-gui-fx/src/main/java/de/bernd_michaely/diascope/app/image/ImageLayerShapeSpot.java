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

import de.bernd_michaely.diascope.app.image.ImageLayer.Type;
import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.MultiImageView.Mode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/// Class to describe an ImageLayer selection shape for SPOT mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayerShapeSpot extends ImageLayerShapeBase
{
	private static final double SPOT_RADIUS_MIN = Font.getDefault().getSize();
	private static final double SPOT_RADIUS_DEFAULT = SPOT_RADIUS_MIN * 10;
	private static final double SPOT_RADIUS_MAX = 1_000_000;
	private final Ellipse ellipse;
	private final SpotCenter spotCenter;
	private final DoubleProperty centerX = new SimpleDoubleProperty();
	private final DoubleProperty centerY = new SimpleDoubleProperty();
	private final DoubleProperty radius = new SimpleDoubleProperty(SPOT_RADIUS_DEFAULT);
	private final ChangeListener<@Nullable Mode> spotInitListener;
	private final BooleanProperty mouseInShape = new SimpleBooleanProperty();
	private final BooleanProperty mouseInSpot = new SimpleBooleanProperty();
	private double mx, my, dx, dy;

	private static class MouseEventAdapter implements Consumer<MouseEvent>
	{
		private @Nullable Consumer<MouseEvent> delegate;

		private MouseEventAdapter()
		{
		}

		@Override
		public void accept(MouseEvent event)
		{
			if (delegate != null)
			{
				delegate.accept(event);
			}
		}
	}

	private ImageLayerShapeSpot(Viewport viewport,
		@Nullable Consumer<MouseEvent> onMouseDragInit,
		@Nullable Consumer<MouseEvent> onMouseDragged)
	{
		super(true, onMouseDragInit, onMouseDragged);
		this.ellipse = new Ellipse(SPOT_RADIUS_MAX, SPOT_RADIUS_DEFAULT);
		this.spotCenter = new SpotCenter(viewport.widthProperty(), viewport.heightProperty());
		spotCenter.xProperty().bind(centerX);
		spotCenter.yProperty().bind(centerY);
		spotCenter.hoverProperty().bind(mouseInSpot);
		ellipse.centerXProperty().bind(centerX);
		ellipse.centerYProperty().bind(centerY);
		ellipse.radiusXProperty().bind(radius.multiply(1.5));
		ellipse.radiusYProperty().bind(radius);
		ellipse.setOnMouseEntered(_ -> mouseInShape.set(true));
		ellipse.setOnMouseExited(_ -> mouseInShape.set(false));
		spotCenter.getShape().setOnMouseEntered(_ -> mouseInSpot.set(true));
		spotCenter.getShape().setOnMouseExited(_ -> mouseInSpot.set(false));
		spotCenter.enabledProperty().bind(mouseInShape.or(mouseInSpot).and(
			viewport.multiLayerModeProperty()).and(viewport.modeProperty().isEqualTo(SPOT)));
		this.spotInitListener = onChange(newValue ->
		{
			if (newValue == Mode.SPOT)
			{
				centerX.set(viewport.widthProperty().get() / 2.0);
				centerY.set(viewport.heightProperty().get() / 2.0);
				removeSpotInitListener(viewport);
			}
		});
		viewport.modeProperty().addListener(spotInitListener);
		viewport.widthProperty().addListener(onChange((oldWidth, newWidth) ->
		{
			final double w = newWidth.doubleValue();
			final double x = centerX.get() * w / oldWidth.doubleValue();
			centerX.set(x);
		}));
		viewport.heightProperty().addListener(onChange((oldHeight, newHeight) ->
		{
			final double h = newHeight.doubleValue();
			final double y = centerY.get() * h / oldHeight.doubleValue();
			centerY.set(y);
		}));
	}

	static ImageLayerShapeSpot createInstance(Viewport viewport)
	{
		final MouseEventAdapter adapterDragInit = new MouseEventAdapter();
		final MouseEventAdapter adapterDragged = new MouseEventAdapter();
		final var imageLayerShapeSpot = new ImageLayerShapeSpot(
			viewport, adapterDragInit, adapterDragged);
		adapterDragInit.delegate = imageLayerShapeSpot::onMouseDragInit;
		adapterDragged.delegate = imageLayerShapeSpot::onMouseDragged;
		imageLayerShapeSpot._postInit();
		return imageLayerShapeSpot;
	}

	private void onMouseDragInit(MouseEvent event)
	{
		mx = centerX.get();
		my = centerY.get();
		dx = mx - event.getX();
		dy = my - event.getY();
	}

	private void onMouseDragged(MouseEvent event)
	{
		try
		{
			if (event.isControlDown())
			{
				final double x = event.getX() - mx;
				final double y = event.getY() - my;
				radius.set(max(SPOT_RADIUS_MIN, sqrt(x * x + y * y)));
			}
			else
			{
				centerX.set(dx + event.getX());
				centerY.set(dy + event.getY());
			}
		}
		finally
		{
			event.consume();
		}
	}

	private void removeSpotInitListener(@UnderInitialization ImageLayerShapeSpot this,
		Viewport viewport)
	{
		if (viewport != null && spotInitListener != null)
		{
			viewport.modeProperty().removeListener(spotInitListener);
		}
	}

	void bindClipToShape(Ellipse clip)
	{
		clip.centerXProperty().bind(ellipse.centerXProperty());
		clip.centerYProperty().bind(ellipse.centerYProperty());
		clip.radiusXProperty().bind(ellipse.radiusXProperty());
		clip.radiusYProperty().bind(ellipse.radiusYProperty());
	}

	SpotCenter getSpotCenter()
	{
		return spotCenter;
	}

	@Override
	Type getType()
	{
		return Type.SPOT;
	}

	@Override
	Ellipse getShape()
	{
		return ellipse;
	}
}
