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

import java.util.function.BiConsumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
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

import static de.bernd_michaely.diascope.app.image.ImageTransforms.ZoomMode.FILL;
import static de.bernd_michaely.diascope.app.image.ImageTransforms.ZoomMode.FIT;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.min;
import static javafx.beans.binding.Bindings.negate;
import static javafx.beans.binding.Bindings.when;

/**
 * Class to describe ImageView transformations.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ImageLayer
{
	private final Pane paneLayer = new Pane();
	private final ImageView imageView = new ImageView();
	private final Rectangle imageRotated = new Rectangle();
	private final Polygon clippingShape = new Polygon();
	private final Polygon selectionShape = new Polygon();
	private final DoubleProperty aspectRatio;
	private final ReadOnlyDoubleWrapper imageWidth, imageHeight;
	private final ReadOnlyDoubleWrapper imageWidthRotated, imageHeightRotated;
	private final ReadOnlyDoubleWrapper imageWidthTransformed, imageHeightTransformed;
	private final DoubleProperty maxToPreviousWidth, maxToPreviousHeight;
	private final ImageTransforms imageTransforms;
	private final DoubleProperty zoomFitWidth, zoomFitHeight, zoomFit;
	private final DoubleProperty zoomFill;
	private final ReadOnlyDoubleWrapper zoomFactor;
	private final ReadOnlyBooleanWrapper imageIsNull;
	private final BooleanProperty zoomModeIsFit;
	private final BooleanProperty zoomModeIsFill;
	private final DoubleProperty focusPointX, focusPointY;
	private final Divider divider;
	private final Scale scale;
	private final Rotate rotate;
	private final Translate translateScroll;
	private final BooleanProperty selected;
	private boolean mouseDragged;
	private String imageTitle = "";

	ImageLayer(Viewport viewport)
	{
		this.selected = new SimpleBooleanProperty();
		selectionShape.setFill(Color.TRANSPARENT);
//		selectionShape.setStroke(Color.CORNFLOWERBLUE);
		selectionShape.strokeProperty().bind(when(selected)
			.then(Color.CORNFLOWERBLUE).otherwise(Color.ALICEBLUE));
		selectionShape.strokeWidthProperty().bind(when(selected).then(4).otherwise(1));
		selectionShape.setStrokeLineCap(StrokeLineCap.ROUND);
		selectionShape.setStrokeLineJoin(StrokeLineJoin.ROUND);
		selectionShape.setStrokeType(StrokeType.INSIDE);
//		selectionShape.visibleProperty().bind(selected);
		paneLayer.getChildren().addAll(imageView, selectionShape);
		paneLayer.setMinSize(0, 0);
		paneLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
//		setTopAnchor(imageView, 0.0);
//		setLeftAnchor(imageView, 0.0);
//		setRightAnchor(imageView, 0.0);
//		setBottomAnchor(imageView, 0.0);
		this.aspectRatio = new SimpleDoubleProperty(1.0);
		this.imageWidth = new ReadOnlyDoubleWrapper();
		this.imageHeight = new ReadOnlyDoubleWrapper();
		this.imageWidthRotated = new ReadOnlyDoubleWrapper();
		this.imageHeightRotated = new ReadOnlyDoubleWrapper();
		this.imageWidthTransformed = new ReadOnlyDoubleWrapper();
		this.imageHeightTransformed = new ReadOnlyDoubleWrapper();
		this.maxToPreviousWidth = new SimpleDoubleProperty();
		this.maxToPreviousHeight = new SimpleDoubleProperty();
		this.imageTransforms = new ImageTransforms();
		this.zoomFitWidth = new SimpleDoubleProperty();
		this.zoomFitHeight = new SimpleDoubleProperty();
		this.zoomFit = new SimpleDoubleProperty();
		this.zoomFill = new SimpleDoubleProperty();
		this.zoomFactor = new ReadOnlyDoubleWrapper();
		this.imageIsNull = new ReadOnlyBooleanWrapper();
		this.divider = new Divider(viewport.getCornerAngles(),
			viewport.widthProperty(), viewport.heightProperty(),
			viewport.splitCenterXProperty().getReadOnlyProperty(),
			viewport.splitCenterYProperty().getReadOnlyProperty(),
			viewport.splitCenterDxProperty().getReadOnlyProperty(),
			viewport.splitCenterDyProperty().getReadOnlyProperty());

		imageIsNull.bind(isNull(imageView.imageProperty()));
		this.zoomModeIsFit = new SimpleBooleanProperty();
		zoomModeIsFit.bind(imageTransforms.zoomModeProperty().isEqualTo(FIT));
		this.zoomModeIsFill = new SimpleBooleanProperty();
		zoomModeIsFill.bind(imageTransforms.zoomModeProperty().isEqualTo(FILL));
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
						.otherwise(imageTransforms.zoomFixedProperty()))));
		imageRotated.rotateProperty().bind(imageTransforms.rotateProperty());
		this.scale = new Scale();
		scale.xProperty().bind(zoomFactor);
		scale.yProperty().bind(zoomFactor);
		imageWidthTransformed.bind(imageWidthRotated.multiply(zoomFactor));
		imageHeightTransformed.bind(imageHeightRotated.multiply(zoomFactor));
		this.rotate = new Rotate();
		rotate.angleProperty().bind(imageRotated.rotateProperty());
		final var mirror = new Scale();
		mirror.xProperty().bind(when(imageTransforms.mirrorXProperty()).then(-1.0).otherwise(1.0));
		mirror.pivotXProperty().bind(imageWidth.divide(2.0));
		mirror.yProperty().bind(when(imageTransforms.mirrorYProperty()).then(-1.0).otherwise(1.0));
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
			when(viewport.scrollBarEnabledHorizontalProperty())
				.then(negate(viewport.scrollPosXProperty()))
				.otherwise(viewport.widthProperty().subtract(imageWidthTransformed).divide(2.0)));
		translateScroll.yProperty().bind(
			when(viewport.scrollBarEnabledVerticalProperty())
				.then(negate(viewport.scrollPosYProperty()))
				.otherwise(viewport.heightProperty().subtract(imageHeightTransformed).divide(2.0)));
		imageView.getTransforms().addAll(
			translateScroll, scale, translateBack, rotate, translateCenter, mirror);
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

	static ImageLayer createInstance(Viewport viewport, BiConsumer<ImageLayer, Boolean> layerSelectionHandler)
	{
		final var imageLayer = new ImageLayer(viewport);
		// post init:
		imageLayer.paneLayer.setOnMouseDragged(event ->
		{
			imageLayer.mouseDragged = true;
		});
		imageLayer.paneLayer.setOnMouseReleased(event ->
		{
			if (!imageLayer.mouseDragged && event.getButton().equals(MouseButton.PRIMARY) &&
				event.getClickCount() == 1 && !event.isShiftDown() && !event.isAltDown())
			{
				layerSelectionHandler.accept(imageLayer, event.isControlDown());
			}
			imageLayer.mouseDragged = false;
		});
		return imageLayer;
	}

	ImageTransforms getImageTransforms()
	{
		return imageTransforms;
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

	Region getRegion()
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

	boolean isSelected()
	{
		return selected.get();
	}

	void setSelected(boolean selected)
	{
		this.selected.set(selected);
	}

	Divider getDivider()
	{
		return divider;
	}

	void setShapePoints(Double... points)
	{
		clippingShape.getPoints().setAll(points);
		selectionShape.getPoints().setAll(points);
	}
}
