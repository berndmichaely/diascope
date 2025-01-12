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
import java.util.Arrays;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.Bindings.normalizeAngle;
import static de.bernd_michaely.diascope.app.image.Bindings.tan;
import static de.bernd_michaely.diascope.app.image.Border.*;
import static de.bernd_michaely.diascope.app.image.MultiImageView.ZoomMode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.min;
import static javafx.beans.binding.Bindings.negate;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.when;
import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

/**
 * Class to describe ImageView transformations.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageLayer
{
	private final AnchorPane paneLayer = new AnchorPane();
	private final ImageView imageView = new ImageView();
	private final Rectangle imageRotated = new Rectangle();
	private final Polygon clippingShape = new Polygon();
	private final Polygon selectionShape = new Polygon();
	private final DoubleProperty aspectRatio;
	private final ReadOnlyDoubleWrapper imageWidth, imageHeight;
	private final ReadOnlyDoubleWrapper imageWidthRotated, imageHeightRotated;
	private final ReadOnlyDoubleWrapper imageWidthTransformed, imageHeightTransformed;
	private final DoubleProperty maxToPreviousWidth, maxToPreviousHeight;
	private final DoubleProperty zoomFitWidth, zoomFitHeight, zoomFit;
	private final DoubleProperty zoomFill;
	private final DoubleProperty zoomFixed;
	private final ObjectProperty<ZoomMode> zoomMode;
	private final ReadOnlyDoubleWrapper zoomFactor;
	private final ReadOnlyBooleanWrapper imageIsNull;
	private final BooleanProperty zoomModeIsFit;
	private final BooleanProperty zoomModeIsFill;
	private final BooleanProperty scrollBarsDisabled;
	private final ReadOnlyBooleanWrapper scrollBarEnabledHorizontal, scrollBarEnabledVertical;
	private final ReadOnlyBooleanWrapper scrollBarVisibleHorizontal, scrollBarVisibleVertical;
	private final DoubleProperty focusPointX, focusPointY;
	private final DoubleProperty dividerAngle;
	private final ReadOnlyDoubleWrapper dividerAngleNorm;
	private final ReadOnlyDoubleProperty angleNormalized;
	private final ReadOnlyObjectWrapper<Border> dividerBorder;
	private final ReadOnlyDoubleWrapper dividerBorderX, dividerBorderY;
	private final Scale scale;
	private final Rotate rotate;
	private final Translate translateScroll;
	private final BooleanProperty selected;
	private String imageTitle = "";

	ImageLayer(Viewport viewport)
	{
		this.selected = new SimpleBooleanProperty();
		paneLayer.setMinSize(0, 0);
		paneLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		selectionShape.setFill(new Color(0.0, 0.0, 0.0, 0.0));
//		selectionShape.setStroke(Color.CORNFLOWERBLUE);
		selectionShape.strokeProperty().bind(when(selected)
			.then(Color.CORNFLOWERBLUE).otherwise(Color.ALICEBLUE));
		selectionShape.setStrokeWidth(3);
		selectionShape.setStrokeLineCap(StrokeLineCap.ROUND);
		selectionShape.setStrokeLineJoin(StrokeLineJoin.ROUND);
		selectionShape.setStrokeType(StrokeType.INSIDE);
//		selectionShape.visibleProperty().bind(selected);
		paneLayer.getChildren().addAll(selectionShape, imageView);
		setTopAnchor(imageView, 0.0);
		setLeftAnchor(imageView, 0.0);
		setRightAnchor(imageView, 0.0);
		setBottomAnchor(imageView, 0.0);
		this.aspectRatio = new SimpleDoubleProperty(1.0);
		this.imageWidth = new ReadOnlyDoubleWrapper();
		this.imageHeight = new ReadOnlyDoubleWrapper();
		this.imageWidthRotated = new ReadOnlyDoubleWrapper();
		this.imageHeightRotated = new ReadOnlyDoubleWrapper();
		this.imageWidthTransformed = new ReadOnlyDoubleWrapper();
		this.imageHeightTransformed = new ReadOnlyDoubleWrapper();
		this.maxToPreviousWidth = new SimpleDoubleProperty();
		this.maxToPreviousHeight = new SimpleDoubleProperty();
		this.zoomFitWidth = new SimpleDoubleProperty();
		this.zoomFitHeight = new SimpleDoubleProperty();
		this.zoomFit = new SimpleDoubleProperty();
		this.zoomFill = new SimpleDoubleProperty();
		this.zoomFixed = new SimpleDoubleProperty();
		this.zoomFactor = new ReadOnlyDoubleWrapper();
		this.imageIsNull = new ReadOnlyBooleanWrapper();
		this.dividerAngle = new SimpleDoubleProperty(0.0);
		this.dividerBorder = new ReadOnlyObjectWrapper<>(Border.RIGHT);
		this.dividerBorderX = new ReadOnlyDoubleWrapper();
		this.dividerBorderY = new ReadOnlyDoubleWrapper();
		this.dividerAngleNorm = new ReadOnlyDoubleWrapper();

		imageIsNull.bind(isNull(imageView.imageProperty()));
		this.zoomMode = new SimpleObjectProperty<>();
		zoomMode.set(ZoomMode.getDefault());
		this.zoomModeIsFit = new SimpleBooleanProperty();
		zoomModeIsFit.bind(zoomMode.isEqualTo(FIT));
		this.zoomModeIsFill = new SimpleBooleanProperty();
		zoomModeIsFill.bind(zoomMode.isEqualTo(FILL));
		this.scrollBarsDisabled = new SimpleBooleanProperty();
		scrollBarsDisabled.bind(imageIsNull.or(zoomModeIsFit).or(viewport.scrollBarsDisabledProperty()));
		imageRotated.boundsInParentProperty().addListener(onChange(bounds ->
		{
			imageWidthRotated.set(bounds.getWidth());
			imageHeightRotated.set(bounds.getHeight());
		}));
		aspectRatio.bind(imageWidthRotated.divide(imageHeightRotated));
		zoomFitWidth.bind(viewport.widthProperty().divide(imageWidthRotated));
		zoomFitHeight.bind(viewport.heightProperty().divide(imageHeightRotated));
		zoomFit.bind(min(zoomFitWidth, zoomFitHeight));
		zoomFill.bind(max(zoomFitWidth, zoomFitHeight));
		zoomFactor.bind(
			when(imageIsNull).then(0.0)
				.otherwise(when(zoomModeIsFit).then(zoomFit)
					.otherwise(when(zoomModeIsFill).then(zoomFill)
						.otherwise(zoomFixed))));
		imageRotated.rotateProperty().bind(viewport.rotateProperty());
		zoomFixed.bind(viewport.zoomFixedProperty());
		zoomMode.bind(viewport.zoomModeProperty());
		this.scale = new Scale();
		scale.xProperty().bind(zoomFactor);
		scale.yProperty().bind(zoomFactor);
		imageWidthTransformed.bind(imageWidthRotated.multiply(zoomFactor));
		imageHeightTransformed.bind(imageHeightRotated.multiply(zoomFactor));
		this.scrollBarEnabledHorizontal = new ReadOnlyBooleanWrapper();
		scrollBarEnabledHorizontal.bind(
			imageWidthTransformed.getReadOnlyProperty().greaterThan(viewport.widthProperty()));
		this.scrollBarVisibleHorizontal = new ReadOnlyBooleanWrapper();
		scrollBarVisibleHorizontal.bind(
			not(viewport.scrollBarsDisabledProperty()).and(scrollBarEnabledHorizontal));
		this.scrollBarEnabledVertical = new ReadOnlyBooleanWrapper();
		scrollBarEnabledVertical.bind(
			imageHeightTransformed.getReadOnlyProperty().greaterThan(viewport.heightProperty()));
		this.scrollBarVisibleVertical = new ReadOnlyBooleanWrapper();
		scrollBarVisibleVertical.bind(
			not(viewport.scrollBarsDisabledProperty()).and(scrollBarEnabledVertical));
		this.rotate = new Rotate();
		rotate.angleProperty().bind(imageRotated.rotateProperty());
		final var mirror = new Scale();
		mirror.xProperty().bind(when(viewport.mirrorXProperty()).then(-1.0).otherwise(1.0));
		mirror.pivotXProperty().bind(imageWidth.divide(2.0));
		mirror.yProperty().bind(when(viewport.mirrorYProperty()).then(-1.0).otherwise(1.0));
		mirror.pivotYProperty().bind(imageHeight.divide(2.0));
		final var translateCenter = new Translate();
		translateCenter.xProperty().bind(imageWidth.divide(-2.0));
		translateCenter.yProperty().bind(imageHeight.divide(-2.0));
		final var translateBack = new Translate();
		translateBack.xProperty().bind(imageWidthRotated.divide(2.0));
		translateBack.yProperty().bind(imageHeightRotated.divide(2.0));
		this.focusPointX = new SimpleDoubleProperty();
		focusPointX.bind(viewport.focusPointX());
		this.focusPointY = new SimpleDoubleProperty();
		focusPointY.bind(viewport.focusPointY());
		this.translateScroll = new Translate();
		translateScroll.xProperty().bind(
			when(scrollBarEnabledHorizontal)
				.then(negate(viewport.scrollPosXProperty()))
				.otherwise(viewport.widthProperty().subtract(imageWidthTransformed).divide(2.0)));
		translateScroll.yProperty().bind(
			when(scrollBarEnabledVertical)
				.then(negate(viewport.scrollPosYProperty()))
				.otherwise(viewport.heightProperty().subtract(imageHeightTransformed).divide(2.0)));
		imageView.getTransforms().addAll(
			translateScroll, scale, translateBack, rotate, translateCenter, mirror);
		dividerAngleNorm.bind(normalizeAngle(dividerAngle));
		this.angleNormalized = dividerAngleNorm.getReadOnlyProperty();
		dividerBorder.bind(
			when(angleNormalized.lessThanOrEqualTo(viewport.getCornerAngles().get(RIGHT)))
				.then(RIGHT).otherwise(
				when(angleNormalized.lessThanOrEqualTo(viewport.getCornerAngles().get(BOTTOM)))
					.then(BOTTOM).otherwise(
					when(angleNormalized.lessThanOrEqualTo(viewport.getCornerAngles().get(LEFT)))
						.then(LEFT).otherwise(
						when(angleNormalized.lessThanOrEqualTo(viewport.getCornerAngles().get(TOP)))
							.then(TOP).otherwise(RIGHT)))));
		dividerBorderX.bind(
			when(dividerBorder.isEqualTo(RIGHT))
				.then(viewport.widthProperty()).otherwise(
				when(dividerBorder.isEqualTo(BOTTOM))
					.then(tan(angleNormalized.subtract(90.0))
						.multiply(viewport.splitCenterDyProperty())
						.add(viewport.splitCenterXProperty())).otherwise(
					when(dividerBorder.isEqualTo(LEFT))
						.then(0.0).otherwise( // TOP
						tan(angleNormalized.add(90.0))
							.multiply(viewport.splitCenterYProperty())
							.add(viewport.splitCenterXProperty())))));
		dividerBorderY.bind(
			when(dividerBorder.isEqualTo(RIGHT))
				.then(tan(angleNormalized)
					.multiply(viewport.splitCenterDxProperty())
					.add(viewport.splitCenterYProperty())).otherwise(
				when(dividerBorder.isEqualTo(BOTTOM))
					.then(viewport.heightProperty()).otherwise(
					when(dividerBorder.isEqualTo(LEFT))
						.then(tan(angleNormalized.subtract(180.0))
							.multiply(viewport.splitCenterXProperty())
							.add(viewport.splitCenterYProperty()))
						.otherwise(0.0)))); // TOP
		viewport.multiLayerModeProperty().addListener(onChange(enabled ->
		{
			if (enabled)
			{
				setNullableClip(clippingShape);
			}
			else
			{
				setNullableClip(null);
				clippingShape.getPoints().clear();
				selectionShape.getPoints().clear();
			}
		}));
	}

	ObjectProperty<ZoomMode> zoomModeProperty()
	{
		return zoomMode;
	}

	DoubleProperty zoomFixedProperty()
	{
		return zoomFixed;
	}

	DoubleProperty rotateProperty()
	{
		return imageRotated.rotateProperty();
	}

	ReadOnlyDoubleProperty zoomFactorProperty()
	{
		return zoomFactor.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty layerWidthProperty()
	{
		return imageWidthTransformed.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty layerHeightProperty()
	{
		return imageHeightTransformed.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty imageIsNullProperty()
	{
		return imageIsNull.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty scrollBarEnabledHorizontalProperty()
	{
		return scrollBarVisibleHorizontal.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty scrollBarEnabledVerticalProperty()
	{
		return scrollBarVisibleVertical.getReadOnlyProperty();
	}

	DoubleProperty maxToPreviousWidthProperty()
	{
		return maxToPreviousWidth;
	}

	DoubleProperty maxToPreviousHeightProperty()
	{
		return maxToPreviousHeight;
	}

	ImageView getImageView()
	{
		return imageView;
	}

	AnchorPane getPaneLayer()
	{
		return paneLayer;
	}

	/**
	 * Set the image to display.
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		final var image = imageDescriptor != null ? imageDescriptor.getImage() : null;
		setNullableImage(image);
		imageTitle = imageDescriptor != null ? imageDescriptor.getTitle() : "";
		final double width = image != null ? Double.max(image.getWidth(), 0) : 0;
		final double height = image != null ? Double.max(image.getHeight(), 0) : 0;
		imageWidth.set(width);
		imageHeight.set(height);
		imageRotated.setWidth(width);
		imageRotated.setHeight(height);
	}

	@SuppressWarnings("argument")
	private void setNullableImage(@Nullable Image image)
	{
		imageView.setImage(image);
	}

	@SuppressWarnings("argument")
	private void setNullableClip(@UnderInitialization ImageLayer this,
		@Nullable Node clip)
	{
//		imageView.setClip(clip);
		paneLayer.setClip(clip);
	}

	BooleanProperty selectedProperty()
	{
		return selected;
	}

	DoubleProperty dividerAngleProperty()
	{
		return dividerAngle;
	}

	Border getDividerBorder()
	{
		return dividerBorder.getReadOnlyProperty().get();
	}

	Double getDividerBorderX()
	{
		return dividerBorderX.getValue();
	}

	Double getDividerBorderY()
	{
		return dividerBorderY.getValue();
	}

	void setShapePoints(Double... points)
	{
		System.out.println("SET SHAPE POINTS: " + Arrays.deepToString(points));
		clippingShape.getPoints().setAll(points);
		selectionShape.getPoints().setAll(points);
	}

	boolean isSelected()
	{
		return selected.get();
	}
}
