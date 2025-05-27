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

import java.lang.ref.WeakReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ZoomMode.FILL;
import static de.bernd_michaely.diascope.app.image.ZoomMode.FIT;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.min;
import static javafx.beans.binding.Bindings.negate;
import static javafx.beans.binding.Bindings.when;

/// Class to describe ImageView transformations.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageLayer
{
	private final Pane paneLayer = new Pane();
	private final ImageView imageView = new ImageView();
	private final Rectangle imageRotated = new Rectangle();
	private final Polygon clippingShape = new Polygon();
	private final BooleanProperty clippingEnabled = new SimpleBooleanProperty();
	private final ImageLayerShape imageLayerShape = new ImageLayerShape();
	private final DoubleProperty aspectRatio;
	private final ReadOnlyDoubleWrapper imageWidth, imageHeight;
	private final ReadOnlyDoubleWrapper imageWidthRotated, imageHeightRotated;
	private final ReadOnlyDoubleWrapper imageWidthTransformed, imageHeightTransformed;
	private final DoubleProperty maxToPreviousWidth, maxToPreviousHeight;
	private final ImageTransforms imageTransforms;
	private final DoubleProperty zoomFitWidth, zoomFitHeight, zoomFit;
	private final DoubleProperty zoomFill;
	private final ReadOnlyBooleanWrapper imageIsNull;
	private final BooleanProperty zoomModeIsFit;
	private final BooleanProperty zoomModeIsFill;
	private final DoubleProperty focusPointX, focusPointY;
	private final Divider divider;
	private final Scale scale;
	private final Rotate rotate;
	private final Translate translateScroll;
	private String imageTitle = "";

	private ImageLayer(Viewport viewport)
	{
		paneLayer.getChildren().add(imageView);
		paneLayer.setMinSize(0, 0);
		paneLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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
		this.imageIsNull = new ReadOnlyBooleanWrapper();
		this.divider = new Divider(viewport.getCornerAngles(),
			viewport.widthProperty(), viewport.heightProperty(),
			viewport.getSplitCenter().xProperty(), viewport.getSplitCenter().yProperty(),
			viewport.getSplitCenter().dxProperty(), viewport.getSplitCenter().dyProperty());
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
		imageTransforms.zoomFactorWrapperProperty().bind(
			when(imageIsNull).then(0.0)
				.otherwise(when(zoomModeIsFit).then(zoomFit)
					.otherwise(when(zoomModeIsFill).then(zoomFill)
						.otherwise(imageTransforms.zoomFixedProperty()))));
		imageRotated.rotateProperty().bind(imageTransforms.rotateProperty());
		this.scale = new Scale();
		scale.xProperty().bind(imageTransforms.zoomFactorProperty());
		scale.yProperty().bind(imageTransforms.zoomFactorProperty());
		imageWidthTransformed.bind(imageWidthRotated.multiply(imageTransforms.zoomFactorProperty()));
		imageHeightTransformed.bind(imageHeightRotated.multiply(imageTransforms.zoomFactorProperty()));
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
	}

	static ImageLayer createInstance(Viewport viewport,
		BiConsumer<ImageLayer, Boolean> layerSelectionHandler,
		Consumer<Divider> onDividerRotate)
	{
		final var imageLayer = new ImageLayer(viewport);
		// post init:
		final Divider d = imageLayer.getDivider();
		d.getMouseDragState().setOnRotate(() -> onDividerRotate.accept(d));
		imageLayer.getImageLayerShape().setLayerSelectionHandler(new Consumer<Boolean>()
		{
			private final WeakReference<ImageLayer> wImageLayer = new WeakReference<>(imageLayer);

			@Override
			public void accept(Boolean value)
			{
				ImageLayer imageLayer = null;
				if (wImageLayer != null)
				{
					imageLayer = wImageLayer.get();
				}
				if (imageLayer != null)
				{
					layerSelectionHandler.accept(imageLayer, value);
				}
			}
		});
		imageLayer.clippingEnabled.addListener(onChange(enabled ->
		{
			if (enabled)
			{
				imageLayer.setNullableClip(imageLayer.clippingShape);
			}
			else
			{
				imageLayer.clearClip();
			}
		}));
		imageLayer.clippingEnabled.bind(viewport.multiLayerModeProperty());
		return imageLayer;
	}

	ImageTransforms getImageTransforms()
	{
		return imageTransforms;
	}

	ReadOnlyDoubleProperty layerWidthProperty()
	{
		return imageWidthTransformed.getReadOnlyProperty();
	}

	ReadOnlyDoubleProperty layerHeightProperty()
	{
		return imageHeightTransformed.getReadOnlyProperty();
	}

	DoubleProperty maxToPreviousWidthProperty()
	{
		return maxToPreviousWidth;
	}

	DoubleProperty maxToPreviousHeightProperty()
	{
		return maxToPreviousHeight;
	}

	Region getRegion()
	{
		return paneLayer;
	}

	/// Set the image to display.
	///
	/// @param imageDescriptor the given image, may be null to clear the display
	///
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
	private void setNullableClip(@Nullable Node clip)
	{
		if (Platform.isSupported(ConditionalFeature.SHAPE_CLIP))
		{
			paneLayer.setClip(clip);
		}
	}

	BooleanProperty selectedProperty()
	{
		return getImageLayerShape().selectedProperty();
	}

	boolean isSelected()
	{
		return selectedProperty().get();
	}

	Divider getDivider()
	{
		return divider;
	}

	ImageLayerShape getImageLayerShape()
	{
		return imageLayerShape;
	}

	private void clearShapePoints()
	{
		clippingShape.getPoints().clear();
		getImageLayerShape().clearPoints();
	}

	void clearClip()
	{
		clearShapePoints();
		setNullableClip(null);
	}

	void setShapePoints(Double... points)
	{
		clippingShape.getPoints().setAll(points);
		getImageLayerShape().setShapePoints(points);
	}

	@Override
	public String toString()
	{
		return imageTitle;
	}
}
