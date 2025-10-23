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
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.clamp;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static javafx.beans.binding.Bindings.when;

/// Class to describe an ImageLayer selection shape for SPOT mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayerShapeSpot extends ImageLayerShapeBase
{
	private static final List<Color> COLORS_SELECTED = List.of(
		Color.CORNFLOWERBLUE, Color.CORAL);
	private static final LinearGradient STROKE_GRADIENT = new LinearGradient(
		0, 0, 1, 0, true, CycleMethod.NO_CYCLE, List.of(
			new Stop(0, COLORS_SELECTED.getFirst()),
			new Stop(1, COLORS_SELECTED.getLast())));
	private static final double STROKE_WIDTH_SELECTED = 5 * STROKE_WIDTH_UNSELECTED;
	private static final double SPOT_RADIUS_MIN = Font.getDefault().getSize();
	private static final double SPOT_RADIUS_DEFAULT = SPOT_RADIUS_MIN * 10;
	private static final double SPOT_RADIUS_MAX = 1e6;
	private static final double ANGLE_STEP = 45.0 / 3;
	private static final double DELTA_CIRCLE = 0.25;
	private static final double SCALE_DOMAIN = 0.4;
	private final ReadOnlyDoubleProperty viewportWidth, viewportHeight;
	private final Ellipse ellipse;
	private final SpotCenter spotCenter;
	private final DoubleProperty centerX = new SimpleDoubleProperty();
	private final DoubleProperty centerY = new SimpleDoubleProperty();
	private final ChangeListener<@Nullable Mode> spotInitListener;
	private final BooleanProperty mouseInShape = new SimpleBooleanProperty();
	private final BooleanProperty mouseInSpot = new SimpleBooleanProperty();
	private final BooleanProperty circleMode = new SimpleBooleanProperty(true);
	private final ObjectBinding<Paint> strokeSelectedPaint;
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
		this.viewportWidth = viewport.widthProperty();
		this.viewportHeight = viewport.heightProperty();
		strokeSelectedPaint = when(circleMode)
			.then((Paint) COLORS_SELECTED.getFirst()).otherwise(STROKE_GRADIENT);
		this.ellipse = new Ellipse(SPOT_RADIUS_DEFAULT, SPOT_RADIUS_DEFAULT);
		this.spotCenter = new SpotCenter(viewport.widthProperty(), viewport.heightProperty());
		spotCenter.xProperty().bind(centerX);
		spotCenter.yProperty().bind(centerY);
		spotCenter.hoverProperty().bind(mouseInSpot);
		ellipse.centerXProperty().bind(centerX);
		ellipse.centerYProperty().bind(centerY);
		ellipse.setOnMouseEntered(_ -> mouseInShape.set(true));
		ellipse.setOnMouseExited(_ -> mouseInShape.set(false));
		spotCenter.getShape().setOnMouseEntered(_ -> mouseInSpot.set(true));
		spotCenter.getShape().setOnMouseExited(_ -> mouseInSpot.set(false));
		spotCenter.enabledProperty().bind(mouseInShape.or(mouseInSpot).and(
			viewport.multiLayerModeProperty()).and(viewport.modeProperty().isEqualTo(Mode.SPOT)));
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

	private Point2D getMousePoint(MouseEvent event)
	{
		return ellipse.localToParent(event.getX(), event.getY());
	}

	private void onMouseDragInit(MouseEvent event)
	{
		final var point = getMousePoint(event);
		mx = centerX.get();
		my = centerY.get();
		dx = mx - point.getX();
		dy = my - point.getY();
	}

	private void onMouseDragged(MouseEvent event)
	{
		try
		{
			final boolean isShift = event.isShiftDown();
			final boolean isControl = event.isControlDown();
			final boolean isAlt = event.isAltDown();
			if (!isAlt)
			{
				final var point = getMousePoint(event);
				final double x = point.getX() - mx;
				final double y = point.getY() - my;
				if (isShift) // set thickness
				{
					final double r = max(SPOT_RADIUS_MIN, sqrt(x * x + y * y)) + STROKE_WIDTH_SELECTED;
					final double rx = ellipse.getRadiusX();
					final double ry = clamp(r, rx, SPOT_RADIUS_MAX);
					double theta = toDegrees(atan2(y, x)) + 90.0;
					if (isControl)
					{
						theta = round(theta / ANGLE_STEP) * ANGLE_STEP;
					}
					ellipse.setRotate(theta);
					circleMode.set(abs(ry - rx) < rx * DELTA_CIRCLE);
					ellipse.setRadiusY(circleMode.get() ? rx : ry);
				}
				else if (!isShift && isControl) // set width
				{
					if (circleMode.get())
					{
						final double r = max(SPOT_RADIUS_MIN, sqrt(x * x + y * y));
						ellipse.setRadiusX(r);
						ellipse.setRadiusY(r);
					}
					else
					{
						final double theta = atan2(y, x);
						final double alpha = toRadians(ellipse.getRotate());
						final double p = (sqrt(x * x + y * y) + STROKE_WIDTH_SELECTED);
						final double r = abs(p * cos(theta - alpha));
						ellipse.setRadiusX(clamp(r, SPOT_RADIUS_MIN, SPOT_RADIUS_MAX));
					}
				}
				else // move
				{
					centerX.set(dx + point.getX());
					centerY.set(dy + point.getY());
				}
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
		clip.rotateProperty().bind(ellipse.rotateProperty());
	}

	SpotCenter getSpotCenter()
	{
		return spotCenter;
	}

	@Override
	ObservableObjectValue<Paint> getStrokeSelectedPaint()
	{
		return strokeSelectedPaint;
	}

	@Override
	double getStrokeWidthSelected()
	{
		return STROKE_WIDTH_SELECTED;
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
