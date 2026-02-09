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
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.ZoomMode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.min;
import static javafx.beans.binding.Bindings.negate;
import static javafx.beans.binding.Bindings.when;

/// Class to describe a single image layer.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
final class ImageLayer implements AutoCloseable
{
	private static final Logger logger = System.getLogger(ImageLayer.class.getName());
	private final Pane paneLayer = new Pane();
	private final ImageView imageView = new ImageView();
	private final Rectangle imageRotated = new Rectangle();
	private final Rectangle clipRectangle = new Rectangle();
	private final Polygon clipPolygon = new Polygon();
	private final Ellipse clipEllipse = new Ellipse();
	private @Nullable Shape clipShape;
	private final BooleanProperty clippingEnabled = new SimpleBooleanProperty();
	private final ImageLayerShapeBase imageLayerShape;
	private final DoubleProperty aspectRatio;
	private final ReadOnlyDoubleWrapper imageWidth = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageHeight = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageWidthRotated = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageHeightRotated = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageWidthTransformed = new ReadOnlyDoubleWrapper();
	private final ReadOnlyDoubleWrapper imageHeightTransformed = new ReadOnlyDoubleWrapper();
	private final ImageTransformsImpl imageTransforms = new ImageTransformsImpl();
	private final DoubleProperty zoomFitWidth, zoomFitHeight, zoomFit;
	private final DoubleProperty zoomFill;
	private final ReadOnlyBooleanWrapper imageIsNull;
	private final DoubleProperty focusPointX, focusPointY;
	private final Scale scale;
	private final Rotate rotate;
	private final Translate translateScroll;
	private final ViewportBoundsLocal viewportBoundsLocal;
	private final ViewportBoundsLocal viewportBounds;
	private @Nullable ImageDescriptor imageDescriptor;
	private String imageTitle = "";

	/// ImageLayer types.
	private enum Type
	{
		/// grid/split mode layer with polygonal selection shape
		GRID_SPLIT,
		/// base layer of spot mode with rectangular selection shape
		BASE,
		/// spot layers of spot mode with rounded selection shape
		SPOT
	}

	private static sealed class ViewportBoundsGlobal permits ViewportBoundsLocal
	{
		final ReadOnlyDoubleProperty width;
		final ReadOnlyDoubleProperty height;
		final ReadOnlyBooleanProperty scrollingEnabledHorizontal;
		final ReadOnlyBooleanProperty scrollingEnabledVertical;
		final ReadOnlyDoubleProperty scrollPosX;
		final ReadOnlyDoubleProperty scrollPosY;

		private ViewportBoundsGlobal(Viewport viewport)
		{
			this(viewport.widthProperty(), viewport.heightProperty(),
				viewport.scrollBarEnabledHorizontalProperty(),
				viewport.scrollBarEnabledVerticalProperty(),
				viewport.scrollPosXProperty(), viewport.scrollPosYProperty());
		}

		private ViewportBoundsGlobal(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height,
			ReadOnlyBooleanProperty scrollBarEnabledHorizontal,
			ReadOnlyBooleanProperty scrollBarEnabledVertical,
			ReadOnlyDoubleProperty scrollPosX, ReadOnlyDoubleProperty scrollPosY)
		{
			this.width = width;
			this.height = height;
			this.scrollingEnabledHorizontal = scrollBarEnabledHorizontal;
			this.scrollingEnabledVertical = scrollBarEnabledVertical;
			this.scrollPosX = scrollPosX;
			this.scrollPosY = scrollPosY;
		}
	}

	static final class ViewportBoundsLocal extends ViewportBoundsGlobal
	{
		// naming convention:
		// the names of writable properties equal the names of corresponding
		// read-only property names prefixed with an underscore
		private final ReadOnlyDoubleWrapper _x;
		private final ReadOnlyDoubleWrapper _y;
		private final ReadOnlyDoubleWrapper _width;
		private final ReadOnlyDoubleWrapper _height;
		private final ReadOnlyBooleanWrapper _scrollingEnabledHorizontal;
		private final ReadOnlyBooleanWrapper _scrollingEnabledVertical;
		private final ReadOnlyDoubleWrapper _scrollPosX;
		private final ReadOnlyDoubleWrapper _scrollPosY;

		/// Constructor for common initialization.
		private ViewportBoundsLocal()
		{
			final var x = new ReadOnlyDoubleWrapper();
			final var y = new ReadOnlyDoubleWrapper();
			final var w = new ReadOnlyDoubleWrapper();
			final var h = new ReadOnlyDoubleWrapper();
			final var seh = new ReadOnlyBooleanWrapper();
			final var sev = new ReadOnlyBooleanWrapper();
			final var spx = new ReadOnlyDoubleWrapper();
			final var spy = new ReadOnlyDoubleWrapper();
			super(w.getReadOnlyProperty(), h.getReadOnlyProperty(),
				seh.getReadOnlyProperty(), sev.getReadOnlyProperty(),
				spx.getReadOnlyProperty(), spy.getReadOnlyProperty());
			this._x = x;
			this._y = y;
			this._width = w;
			this._height = h;
			this._scrollingEnabledHorizontal = seh;
			this._scrollingEnabledVertical = sev;
			this._scrollPosX = spx;
			this._scrollPosY = spy;
		}

		/// Constructor for local viewport.
		private ViewportBoundsLocal(
			ReadOnlyDoubleProperty imageWidthTransformed,
			ReadOnlyDoubleProperty imageHeightTransformed)
		{
			this();
			_scrollingEnabledHorizontal.bind(imageWidthTransformed.greaterThan(width));
			_scrollingEnabledVertical.bind(imageHeightTransformed.greaterThan(height));
		}

		/// Constructor for switch between global and local viewport.
		private ViewportBoundsLocal(ObservableBooleanValue isLocal,
			ViewportBoundsGlobal global, ViewportBoundsLocal local)
		{
			this();
			_x.bind(when(isLocal).then(local._x).otherwise(0d));
			_y.bind(when(isLocal).then(local._y).otherwise(0d));
			_width.bind(when(isLocal).then(local.width).otherwise(global.width));
			_height.bind(when(isLocal).then(local.height).otherwise(global.height));
			_scrollingEnabledHorizontal.bind(when(isLocal)
				.then(local.scrollingEnabledHorizontal).otherwise(global.scrollingEnabledHorizontal));
			_scrollingEnabledVertical.bind(when(isLocal)
				.then(local.scrollingEnabledVertical).otherwise(global.scrollingEnabledVertical));
			_scrollPosX.bind(when(isLocal).then(local.scrollPosX).otherwise(global.scrollPosX));
			_scrollPosY.bind(when(isLocal).then(local.scrollPosY).otherwise(global.scrollPosY));
		}

		DoubleProperty getX()
		{
			return _x;
		}

		DoubleProperty getY()
		{
			return _y;
		}

		DoubleProperty getWidth()
		{
			return _width;
		}

		DoubleProperty getHeight()
		{
			return _height;
		}
	}

	private ImageLayer(Viewport viewport, Type type)
	{
		logger.log(TRACE, () -> "CREATE ImageLayer with mode »%s« and type »%s«"
			.formatted(viewport.modeProperties().getValueOrDefault(), type));
		this.viewportBoundsLocal = new ViewportBoundsLocal(
			imageWidthTransformed.getReadOnlyProperty(),
			imageHeightTransformed.getReadOnlyProperty());
		this.viewportBounds = new ViewportBoundsLocal(
			viewport.modeProperties().isValueProperty(Mode.GRID),
			new ViewportBoundsGlobal(viewport), viewportBoundsLocal);
		clipRectangle.xProperty().bind(viewportBoundsLocal._x.getReadOnlyProperty());
		clipRectangle.yProperty().bind(viewportBoundsLocal._y.getReadOnlyProperty());
		clipRectangle.widthProperty().bind(viewportBoundsLocal.width);
		clipRectangle.heightProperty().bind(viewportBoundsLocal.height);
		this.imageLayerShape = switch (type)
		{
			case GRID_SPLIT ->
				ImageLayerShapeSplit.createInstance(viewport.modeProperties().valueOrDefaultProperty());
			case BASE ->
				new ImageLayerShapeSpotBase();
			case SPOT ->
				ImageLayerShapeSpot.createInstance(viewport);
		};
		paneLayer.getChildren().add(imageView);
		paneLayer.setMinSize(0, 0);
		paneLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		if (imageLayerShape instanceof ImageLayerShapeSpot shapeSpot &&
			clipShape instanceof Ellipse ellipse)
		{
			shapeSpot.bindClipToShape(ellipse);
		}
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
		zoomFitWidth.bind(viewportBounds.width.divide(imageWidthRotated));
		zoomFitHeight.bind(viewportBounds.height.divide(imageHeightRotated));
		zoomFit.bind(min(zoomFitWidth, zoomFitHeight));
		zoomFill.bind(max(zoomFitWidth, zoomFitHeight));
		final EnumProperties<ZoomMode> zoomModeProperties = imageTransforms.zoomModeProperties();
		imageTransforms.zoomFactorWrapperProperty().bind(when(imageIsNull).then(0.0)
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
		translateScroll.xProperty().bind(viewportBounds._x.add(
			when(viewport.scrollBarEnabledHorizontalProperty())
				.then(negate(viewportBounds.scrollPosX))
				.otherwise(viewportBounds.width.subtract(imageWidthTransformed).divide(2.0))));
		translateScroll.yProperty().bind(viewportBounds._y.add(
			when(viewport.scrollBarEnabledVerticalProperty())
				.then(negate(viewportBounds.scrollPosY))
				.otherwise(viewportBounds.height.subtract(imageHeightTransformed).divide(2.0))));
		imageView.getTransforms().addAll(
			translateScroll, scale, translateBack, mirror, rotate, translateCenter);
	}

	static ImageLayer createGridSplitLayer(Viewport viewport,
		BiConsumer<ImageLayer, Boolean> layerSelectionHandler)
	{
		return createInstance(viewport, layerSelectionHandler);
	}

	static ImageLayer createSpotLayer(Viewport viewport,
		BiConsumer<ImageLayer, Boolean> layerSelectionHandler)
	{
		return createInstance(Type.SPOT, viewport, layerSelectionHandler);
	}

	static ImageLayer createSpotBaseLayer(Viewport viewport,
		BiConsumer<ImageLayer, Boolean> layerSelectionHandler)
	{
		return createInstance(Type.BASE, viewport, layerSelectionHandler);
	}

	private static ImageLayer createInstance(Viewport viewport,
		BiConsumer<ImageLayer, Boolean> layerSelectionHandler)
	{
		final var imageLayer = createInstance(Type.GRID_SPLIT, viewport, layerSelectionHandler);
		// post init:
		if (imageLayer.getImageLayerShape() instanceof ImageLayerShapeSplit imageLayerShapeSplit)
		{
			final var layerSelectionModel = viewport.getLayerSelectionModel();
			if (layerSelectionModel != null)
			{
				final var dualProperty = layerSelectionModel.dualSelectedLayerSecondProperty();
				imageLayerShapeSplit.dualSpotSelectedProperty().bind(createBooleanBinding(() ->
				{
					final Optional<ImageLayer> optional = dualProperty.get();
					return optional.isPresent() ?
						layerSelectionModel.getSize() > 2 && optional.get() == imageLayer : false;
				}, dualProperty));
			}
			else
			{
				throw new IllegalStateException("viewport LayerSelectionModel not initialized");
			}
		}
		return imageLayer;
	}

	private static ImageLayer createInstance(Type type, Viewport viewport,
		BiConsumer<ImageLayer, Boolean> layerSelectionHandler)
	{
		final var imageLayer = new ImageLayer(viewport, type);
		// post init:
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
		switch (type)
		{
			case BASE -> imageLayer.setClip(null);
			case SPOT -> imageLayer.setClip(imageLayer.clipEllipse);
			case GRID_SPLIT ->
			{
				viewport.modeProperties().valueOrDefaultProperty().addListener(onChange(mode ->
				{
					switch (mode)
					{
						case SINGLE -> imageLayer.setClip(null);
						case GRID -> imageLayer.setClip(imageLayer.clipRectangle);
						case SPLIT -> imageLayer.setClip(imageLayer.clipPolygon);
					}
				}));
			}
		}
		imageLayer.clippingEnabled.addListener(onChange(() -> imageLayer.setClip(imageLayer.clipShape)));
		imageLayer.clippingEnabled.bind(viewport.multiLayerModeProperty());
		return imageLayer;
	}

	ImageTransformsImpl getImageTransforms()
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

	@Nullable
	ImageDescriptor getImageDescriptor()
	{
		return imageDescriptor;
	}

	/// Set the image to display.
	///
	/// @param imageDescriptor the given image, may be null to clear the display
	///
	void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		this.imageDescriptor = imageDescriptor;
		final var image = imageDescriptor != null ? imageDescriptor.getImage() : null;
		@SuppressWarnings("argument")
		final Runnable setNullableImage = () -> imageView.setImage(image);
		setNullableImage.run();
		imageTitle = imageDescriptor != null ? imageDescriptor.getTitle() : "";
		final double width = image != null ? Math.max(image.getWidth(), 0.0) : 0.0;
		final double height = image != null ? Math.max(image.getHeight(), 0.0) : 0.0;
		imageWidth.set(width);
		imageHeight.set(height);
		imageRotated.setWidth(width);
		imageRotated.setHeight(height);
	}

	private void setClip(@Nullable Shape clip)
	{
		if (Platform.isSupported(ConditionalFeature.SHAPE_CLIP))
		{
			this.clipShape = clippingEnabled.get() ? clip : null;
			@SuppressWarnings("argument")
			final Runnable setNullableClip = () -> paneLayer.setClip(clipShape);
			setNullableClip.run();
			if (clipShape == null)
			{
				// clear shape points:
				clipPolygon.getPoints().clear();
				if (imageLayerShape instanceof ImageLayerShapeSplit shapeSplit)
				{
					shapeSplit.clearPoints();
				}
			}
		}
	}

	boolean isSelected()
	{
		return getImageLayerShape().selectedProperty().get();
	}

	void setSelected(boolean selected)
	{
		getImageLayerShape().selectedProperty().set(selected);
	}

	ImageLayerShapeBase getImageLayerShape()
	{
		return imageLayerShape;
	}

	void setShapePoints(Double... points)
	{
		clipPolygon.getPoints().setAll(points);
		if (getImageLayerShape() instanceof ImageLayerShapeSplit shapeSplit)
		{
			shapeSplit.setShapePoints(points);
		}
	}

	ViewportBoundsLocal getViewportBoundsLocal()
	{
		return viewportBoundsLocal;
	}

	@Deprecated
	void setGridBounds(double x, double y, double w, double h)
	{
		viewportBoundsLocal._x.set(x);
		viewportBoundsLocal._y.set(y);
		viewportBoundsLocal._width.set(w);
		viewportBoundsLocal._height.set(h);
	}

	@Override
	public String toString()
	{
		return imageTitle;
	}

	@Override
	public void close()
	{
		setClip(null);
	}
}
