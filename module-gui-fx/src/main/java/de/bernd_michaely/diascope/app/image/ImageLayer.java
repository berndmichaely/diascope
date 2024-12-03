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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.image.MultiImageView.ZoomMode.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static javafx.beans.binding.Bindings.isNull;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.min;
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
	private final AnchorPane paneLayer;
	private final ImageView imageView;
	private final DoubleProperty aspectRatio;
	private final Rectangle imageRotated;
	private final DoubleProperty imageWidth, imageHeight;
	private final DoubleProperty imageWidthRotated, imageHeightRotated;
	private final DoubleProperty imageWidthTransformed, imageHeightTransformed;
	private final DoubleProperty zoomFitWidth, zoomFitHeight, zoomFit;
	private final BooleanProperty zoomFillWidthNeedsScroll, zoomFillHeightNeedsScroll;
	private final DoubleProperty zoomFillWidth, zoomFillHeight, zoomFill;
	private final DoubleProperty zoomFixed;
	private final ObjectProperty<ZoomMode> zoomMode;
	private final ReadOnlyDoubleWrapper zoomFactor;
	private final ReadOnlyBooleanWrapper imageIsNull;
	private final BooleanProperty zoomModeIsFit;
	private final BooleanProperty zoomModeIsFill;
	private final BooleanProperty scrollBarsDisabled;
	private final ReadOnlyBooleanWrapper scrollBarEnabledHorizontal, scrollBarEnabledVertical;
	private final ReadOnlyBooleanWrapper scrollBarVisibleHorizontal, scrollBarVisibleVertical;
	private final DoubleProperty viewportWidthCurrent, viewportHeightCurrent;
	private final DoubleProperty focusPointX, focusPointY;
	private final Scale scale;
	private final Rotate rotate;
	private final Translate translateScroll;
	private final BooleanProperty selected;
	private String imageTitle = "";

	ImageLayer(Viewport viewport)
	{
		this.paneLayer = new AnchorPane();
		paneLayer.setMinSize(0, 0);
		paneLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.imageView = new ImageView();
		paneLayer.getChildren().add(imageView);
		setTopAnchor(imageView, 0.0);
		setLeftAnchor(imageView, 0.0);
		setRightAnchor(imageView, 0.0);
		setBottomAnchor(imageView, 0.0);
		this.aspectRatio = new SimpleDoubleProperty(1.0);
		this.imageRotated = new Rectangle();
		this.imageWidth = new SimpleDoubleProperty();
		this.imageHeight = new SimpleDoubleProperty();
		this.imageWidthRotated = new SimpleDoubleProperty();
		this.imageHeightRotated = new SimpleDoubleProperty();
		this.zoomFitWidth = new SimpleDoubleProperty();
		this.zoomFitHeight = new SimpleDoubleProperty();
		this.zoomFit = new SimpleDoubleProperty();
		this.zoomFillWidthNeedsScroll = new SimpleBooleanProperty();
		this.zoomFillHeightNeedsScroll = new SimpleBooleanProperty();
		this.zoomFillWidth = new SimpleDoubleProperty();
		this.zoomFillHeight = new SimpleDoubleProperty();
		this.zoomFill = new SimpleDoubleProperty();
		this.zoomFixed = new SimpleDoubleProperty();
		this.zoomFactor = new ReadOnlyDoubleWrapper();
		this.imageIsNull = new ReadOnlyBooleanWrapper();

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
		zoomFillWidthNeedsScroll.bind(
			imageHeightRotated.multiply(zoomFitWidth).greaterThan(viewport.heightProperty()));
		zoomFillHeightNeedsScroll.bind(
			imageWidthRotated.multiply(zoomFitHeight).greaterThan(viewport.widthProperty()));
		zoomFillWidth.bind(when(zoomFillWidthNeedsScroll.and(not(viewport.scrollBarsDisabledProperty())))
			.then(viewport.viewportWidthScrollProperty().divide(imageWidthRotated))
			.otherwise(zoomFitWidth));
		zoomFillHeight.bind(when(zoomFillHeightNeedsScroll.and(not(viewport.scrollBarsDisabledProperty())))
			.then(viewport.viewportHeightScrollProperty().divide(imageHeightRotated))
			.otherwise(zoomFitHeight));
		zoomFill.bind(max(zoomFillWidth, zoomFillHeight));
		zoomFactor.bind(
			when(imageIsNull).then(0.0)
				.otherwise(when(zoomModeIsFit).then(zoomFit)
					.otherwise(when(zoomModeIsFill).then(zoomFill)
						.otherwise(zoomFixed))));
		this.scrollBarEnabledHorizontal = new ReadOnlyBooleanWrapper();
		scrollBarEnabledHorizontal.bind(
			when(scrollBarsDisabled).then(false)
				.otherwise(when(zoomModeIsFill).then(zoomFillHeightNeedsScroll)
					.otherwise(imageWidthRotated.multiply(zoomFixed).greaterThan(
						when(imageHeightRotated.multiply(zoomFixed).greaterThan(viewport.heightProperty()))
							.then(viewport.viewportWidthScrollProperty()).otherwise(viewport.widthProperty())))));
		this.scrollBarVisibleHorizontal = new ReadOnlyBooleanWrapper();
		scrollBarVisibleHorizontal.bind(not(viewport.scrollBarsDisabledProperty()).and(scrollBarEnabledHorizontal));
		this.scrollBarEnabledVertical = new ReadOnlyBooleanWrapper();
		scrollBarEnabledVertical.bind(
			when(scrollBarsDisabled).then(false)
				.otherwise(when(zoomModeIsFill).then(zoomFillWidthNeedsScroll)
					.otherwise(imageHeightRotated.multiply(zoomFixed).greaterThan(
						when(imageWidthRotated.multiply(zoomFixed).greaterThan(viewport.widthProperty()))
							.then(viewport.viewportHeightScrollProperty()).otherwise(viewport.heightProperty())))));
		this.scrollBarVisibleVertical = new ReadOnlyBooleanWrapper();
		scrollBarVisibleVertical.bind(not(viewport.scrollBarsDisabledProperty()).and(scrollBarEnabledVertical));
		this.viewportWidthCurrent = new SimpleDoubleProperty();
		viewportWidthCurrent.bind(
			when(scrollBarEnabledVertical)
				.then(viewport.viewportWidthScrollProperty())
				.otherwise(viewport.widthProperty()));
		this.viewportHeightCurrent = new SimpleDoubleProperty();
		viewportHeightCurrent.bind(
			when(scrollBarEnabledHorizontal)
				.then(viewport.viewportHeightScrollProperty())
				.otherwise(viewport.heightProperty()));
		this.scale = new Scale();
		scale.xProperty().bind(zoomFactor);
		scale.yProperty().bind(zoomFactor);
		this.imageWidthTransformed = new SimpleDoubleProperty();
		this.imageHeightTransformed = new SimpleDoubleProperty();
		imageWidthTransformed.bind(imageWidthRotated.multiply(zoomFactor));
		imageHeightTransformed.bind(imageHeightRotated.multiply(zoomFactor));
		this.rotate = new Rotate();
		rotate.angleProperty().bind(imageRotated.rotateProperty());
		final var translateCenter = new Translate();
		translateCenter.xProperty().bind(imageWidth.divide(-2));
		translateCenter.yProperty().bind(imageHeight.divide(-2));
		final var translateBack = new Translate();
		translateBack.xProperty().bind(imageWidthRotated.divide(2));
		translateBack.yProperty().bind(imageHeightRotated.divide(2));
		this.focusPointX = new SimpleDoubleProperty();
		focusPointX.bind(viewport.focusPointX());
		this.focusPointY = new SimpleDoubleProperty();
		focusPointY.bind(viewport.focusPointY());
		this.translateScroll = new Translate();
		translateScroll.xProperty().bind(
			viewportWidthCurrent.subtract(imageWidthTransformed).multiply(
				when(scrollBarEnabledHorizontal).then(viewport.getScrollBarH().valueProperty()).otherwise(0.5)));
		translateScroll.yProperty().bind(
			viewportHeightCurrent.subtract(imageHeightTransformed).multiply(
				when(scrollBarEnabledVertical).then(viewport.getScrollBarV().valueProperty()).otherwise(0.5)));
		imageView.getTransforms().addAll(translateScroll, scale, translateBack, rotate, translateCenter);
		this.selected = new SimpleBooleanProperty();
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

	BooleanProperty selectedProperty()
	{
		return selected;
	}

	boolean isSelected()
	{
		return selected.get();
	}
}
