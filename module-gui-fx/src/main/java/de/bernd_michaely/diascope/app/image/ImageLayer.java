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

import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;
import java.lang.System.Logger;
import java.util.Optional;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ZoomMode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.min;
import static javafx.beans.binding.Bindings.negate;
import static javafx.beans.binding.Bindings.when;

/// Class to describe a single image layer.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayer implements Transformable
{
	private static final Logger logger = System.getLogger(ImageLayer.class.getName());
	private final Pane paneLayer = new Pane();
	private final ImageView imageView = new ImageView();
	private final Rectangle imageRotated = new Rectangle();
	private final DoubleProperty aspectRatio;
	private final ReadOnlyDoubleWrapper imageWidth = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageHeight = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageWidthRotated = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageHeightRotated = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageWidthTransformed = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageHeightTransformed = new ReadOnlyDoubleWrapper();
	private final DefaultImageTransforms imageTransforms = new DefaultImageTransforms();
	private final DoubleProperty zoomFitWidth, zoomFitHeight, zoomFit;
	private final DoubleProperty zoomFill;
	private final ReadOnlyBooleanWrapper imageIsNull;
	private final DoubleProperty focusPointX, focusPointY;
	private final Scale scale;
	private final Rotate rotate;
	private final Translate translateScroll;
	private final ViewportBoundsLocal viewportBoundsLocal;
	private final ViewportBounds viewportBounds;
	private final ObjectProperty<Optional<ImageDescriptor>> imageDescriptor;

	ImageLayer(Viewport viewport)
	{
		logger.log(TRACE, () -> "CREATE ImageLayer with mode »%s«"
			.formatted(viewport.modeProperties().getValueOrDefault()));
		this.viewportBoundsLocal = new ViewportBoundsLocal();
		this.viewportBounds = new ViewportBoundsSwitch(
			viewport.modeProperties().isValueProperty(Mode.GRID),
			viewport.getViewportBounds(), viewportBoundsLocal);
		paneLayer.getChildren().add(imageView);
		paneLayer.setMinSize(0, 0);
		paneLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.aspectRatio = new SimpleDoubleProperty(1.0);
		this.zoomFitWidth = new SimpleDoubleProperty();
		this.zoomFitHeight = new SimpleDoubleProperty();
		this.zoomFit = new SimpleDoubleProperty();
		this.zoomFill = new SimpleDoubleProperty();
		this.imageIsNull = new ReadOnlyBooleanWrapper();
		imageIsNull.bind(isNull(imageView.imageProperty()));
		imageRotated.boundsInParentProperty().addListener(onChange(bounds ->
		{
			imageWidthRotated.set(bounds.getWidth());
			imageHeightRotated.set(bounds.getHeight());
		}));
		aspectRatio.bind(imageWidthRotated.divide(imageHeightRotated));
		zoomFitWidth.bind(viewportBounds.widthProperty().divide(imageWidthRotated));
		zoomFitHeight.bind(viewportBounds.heightProperty().divide(imageHeightRotated));
		zoomFit.bind(min(zoomFitWidth, zoomFitHeight));
		zoomFill.bind(max(zoomFitWidth, zoomFitHeight));
		final EnumProperties<ZoomMode> zoomModeProperties = imageTransforms.zoomModeProperties();
		imageTransforms.setResultingZoomFactorBinding(
			when(imageIsNull).then(0.0)
				.otherwise(when(zoomModeProperties.isValueProperty(FIT)).then(zoomFit)
					.otherwise(when(zoomModeProperties.isValueProperty(FILL)).then(zoomFill)
						.otherwise(when(zoomModeProperties.isValueProperty(ORIGINAL)).then(1.0)
							.otherwise(imageTransforms.zoomFixedProperty())))));
		imageRotated.rotateProperty().bind(imageTransforms.rotateProperty());
		this.scale = new Scale();
		final var zoomFactorProperty = imageTransforms.zoomFactorProperty();
		scale.xProperty().bind(zoomFactorProperty);
		scale.yProperty().bind(zoomFactorProperty);
		imageWidthTransformed.bind(imageWidthRotated.multiply(zoomFactorProperty));
		imageHeightTransformed.bind(imageHeightRotated.multiply(zoomFactorProperty));
		this.rotate = new Rotate();
		rotate.angleProperty().bind(imageRotated.rotateProperty());
		final var mirror = new Scale();
		mirror.xProperty().bind(when(imageTransforms.mirrorXProperty()).then(-1.0).otherwise(1.0));
		mirror.yProperty().bind(when(imageTransforms.mirrorYProperty()).then(-1.0).otherwise(1.0));
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
		translateScroll.xProperty().bind(viewportBounds.xProperty().add(
			when(viewport.scrollBarEnabledHorizontalProperty())
				.then(negate(viewportBounds.scrollPosXProperty()))
				.otherwise(viewportBounds.widthProperty().subtract(imageWidthTransformed).divide(2.0))));
		translateScroll.yProperty().bind(viewportBounds.yProperty().add(
			when(viewport.scrollBarEnabledVerticalProperty())
				.then(negate(viewportBounds.scrollPosYProperty()))
				.otherwise(viewportBounds.heightProperty().subtract(imageHeightTransformed).divide(2.0))));
		imageView.getTransforms().addAll(
			translateScroll, scale, translateBack, mirror, rotate, translateCenter);
		this.imageDescriptor = new SimpleObjectProperty<>(Optional.empty());
		imageDescriptor.addListener(onChange(optional ->
		{
			optional.ifPresentOrElse(descriptor ->
			{
				final var image = descriptor.getImage();
				imageView.setImage(image);
				final double width = Math.max(image.getWidth(), 0.0);
				final double height = Math.max(image.getHeight(), 0.0);
				imageWidth.set(width);
				imageHeight.set(height);
				imageRotated.setWidth(width);
				imageRotated.setHeight(height);
			}, () ->
			{
				@SuppressWarnings("argument")
				final Runnable clearImage = () -> imageView.setImage(null);
				clearImage.run();
				imageWidth.set(0d);
				imageHeight.set(0d);
				imageRotated.setWidth(0d);
				imageRotated.setHeight(0d);
			});
		}));
	}

	@Override
	public DefaultImageTransforms getImageTransforms()
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

	Region getRegion()
	{
		return paneLayer;
	}

	@SuppressWarnings("return")
	ObjectProperty<@Nullable Node> clipProperty()
	{
		return getRegion().clipProperty();
	}

	ObjectProperty<Optional<ImageDescriptor>> imageDescriptorProperty()
	{
		return imageDescriptor;
	}

	Optional<ImageDescriptor> getImageDescriptor()
	{
		return imageDescriptorProperty().get();
	}

	void setImageDescriptor(Optional<ImageDescriptor> imageDescriptor)
	{
		imageDescriptorProperty().set(imageDescriptor);
	}

	ViewportBoundsLocal getViewportBoundsLocal()
	{
		return viewportBoundsLocal;
	}

	@Override
	public String toString()
	{
		return getImageDescriptor().map(ImageDescriptor::getTitle).orElse("");
	}

	@Override
	public void close()
	{
		imageDescriptorProperty().unbind();
		setImageDescriptor(Optional.empty());
	}
}
