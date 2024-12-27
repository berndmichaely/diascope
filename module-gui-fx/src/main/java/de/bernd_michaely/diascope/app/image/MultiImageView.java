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

import java.lang.System.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.System.Logger.Level.*;
import static javafx.beans.binding.Bindings.max;
import static javafx.beans.binding.Bindings.not;
import static javafx.beans.binding.Bindings.or;
import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;

/**
 * Facade of a component to display multiple images.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class MultiImageView
{
	private static final int NULL_INDEX = -1;
	private final ObservableList<ImageLayer> layers;
	private final ImageLayer nullLayer;
	private final ReadOnlyListWrapper<ImageLayer> layersProperty;
	private final ReadOnlyIntegerWrapper selectedSingleIndex;
	private final ReadOnlyBooleanWrapper isSingleSelected;
	private final ReadOnlyObjectWrapper<ImageLayer> selectedLayer;
	private final ReadOnlyDoubleWrapper zoomFactor;
	private final Viewport viewport;
	private final DoubleProperty rotateProperty;
	private final DoubleProperty zoomFixedProperty;
	private final ObjectProperty<ZoomMode> zoomModeProperty;
	private final BooleanProperty scrollBarsEnabled;

	public enum ZoomMode
	{
		FIT, FILL, FIXED;

		static ZoomMode getDefault()
		{
			return FIT;
		}
	}

	private static class NullImageLayer extends ImageLayer
	{
		private static final Logger logger = System.getLogger(NullImageLayer.class.getName());

		private NullImageLayer(Viewport viewport)
		{
			super(viewport);
		}

		@Override
		final void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
		{
			logger.log(WARNING, "Trying to set image on %s : »%s«".formatted(getClass().getName(),
				imageDescriptor != null ? imageDescriptor.getPath() : "(null)"));
		}
	}

	public MultiImageView()
	{
		this.layers = FXCollections.observableArrayList();
		this.layersProperty = new ReadOnlyListWrapper<>(layers);
		this.scrollBarsEnabled = new SimpleBooleanProperty();
		this.viewport = new Viewport(not(scrollBarsEnabled));
		this.nullLayer = new NullImageLayer(viewport);
		this.rotateProperty = new SimpleDoubleProperty(0.0);
		this.zoomFixedProperty = new SimpleDoubleProperty(1.0);
		this.zoomModeProperty = new SimpleObjectProperty<>(ZoomMode.getDefault());
		this.selectedSingleIndex = new ReadOnlyIntegerWrapper(NULL_INDEX);
		this.isSingleSelected = new ReadOnlyBooleanWrapper();
		this.selectedLayer = new ReadOnlyObjectWrapper<>(nullLayer);
		this.zoomFactor = new ReadOnlyDoubleWrapper();
	}

	/**
	 * Returns the main component to be included in surrounding environment.
	 *
	 * @return the main component
	 */
	public Pane getRegion()
	{
		return viewport.getPaneLayers();
	}

	public void addLayer()
	{
		addLayer(layers.size());
	}

	public void addLayer(int index)
	{
		final boolean wasEmpty = layers.isEmpty();
		final ImageLayer layer = createImageLayer(index);
		if (wasEmpty)
		{
			selectedLayer.set(layer);
			toggleLayerSelection(layer);
		}
	}

	private ImageLayer createImageLayer(int index)
	{
		final var imageLayer = new ImageLayer(viewport);
		layers.add(index, imageLayer);
		imageLayer.rotateProperty().bind(rotateProperty);
		imageLayer.zoomFixedProperty().bind(zoomFixedProperty);
		imageLayer.zoomModeProperty().bind(zoomModeProperty);
		final var paneLayer = imageLayer.getPaneLayer();
		setTopAnchor(paneLayer, 0.0);
		setLeftAnchor(paneLayer, 0.0);
		setRightAnchor(paneLayer, 0.0);
		setBottomAnchor(paneLayer, 0.0);
		viewport.getPaneLayers().getChildren().add(index, paneLayer);
		updateScrollRangeBindings();
		// scrollbar visibility bindings:
		final ImageLayer firstLayer = layers.getFirst();
		ObservableBooleanValue propSbvH = firstLayer.scrollBarEnabledHorizontalProperty();
		ObservableBooleanValue propSbvV = firstLayer.scrollBarEnabledVerticalProperty();
		for (int i = 1; i < layers.size(); i++)
		{
			final ImageLayer layer = layers.get(i);
			propSbvH = or(propSbvH, layer.scrollBarEnabledHorizontalProperty());
			propSbvV = or(propSbvV, layer.scrollBarEnabledVerticalProperty());
		}
		viewport.getScrollBarH().visibleProperty().bind(propSbvH);
		viewport.getScrollBarV().visibleProperty().bind(propSbvV);
		paneLayer.setOnMouseClicked(event ->
		{
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1 &&
				event.isControlDown())
			{
				layers.stream()
					.filter(layer -> layer.getPaneLayer() == paneLayer)
					.findFirst().ifPresent(layer -> toggleLayerSelection(layer, event.isShiftDown()));
			}
		});
		return imageLayer;
	}

	private void updateScrollRangeBindings()
	{
		if (layers.isEmpty())
		{
			viewport.layersMaxWidthProperty().unbind();
			viewport.layersMaxWidthProperty().set(0.0);
			viewport.layersMaxHeightProperty().unbind();
			viewport.layersMaxHeightProperty().set(0.0);
		}
		else
		{
			final ImageLayer first = layers.getFirst();
			first.maxToPreviousWidthProperty().bind(max(first.layerWidthProperty(), 0.0));
			first.maxToPreviousHeightProperty().bind(max(first.layerHeightProperty(), 0.0));
			for (int i = 1; i < layers.size(); i++)
			{
				final ImageLayer lPrev = layers.get(i - 1);
				final ImageLayer lNext = layers.get(i);
				lNext.maxToPreviousWidthProperty().bind(
					max(lPrev.maxToPreviousWidthProperty(), lNext.layerWidthProperty()));
				lNext.maxToPreviousHeightProperty().bind(
					max(lPrev.maxToPreviousHeightProperty(), lNext.layerHeightProperty()));
			}
			viewport.layersMaxWidthProperty().bind(layers.getLast().maxToPreviousWidthProperty());
			viewport.layersMaxHeightProperty().bind(layers.getLast().maxToPreviousHeightProperty());
		}
	}

	private void toggleLayerSelection(ImageLayer layer)
	{
		toggleLayerSelection(layer, false);
	}

	private void toggleLayerSelection(ImageLayer layer, boolean multiSelect)
	{
		final int index = layers.indexOf(layer);
		if (index >= 0)
		{
			final BooleanProperty selectedProperty = layer.selectedProperty();
			selectedProperty.set(!selectedProperty.get());
			if (!multiSelect)
			{
				layers.stream()
					.filter(l -> l != layer)
					.forEach(l -> l.selectedProperty().set(false));
			}
			final int numSelected = (int) layers.stream().filter(ImageLayer::isSelected).count();
			isSingleSelected.set(numSelected == 1);
			if (isSingleSelected.get())
			{
				selectedSingleIndex.set(index);
				selectedLayer.set(layer);
				zoomFactor.bind(selectedLayer.get().zoomFactorProperty());
			}
			else
			{
				selectedSingleIndex.set(NULL_INDEX);
				selectedLayer.set(nullLayer);
			}
		}
	}

	public ReadOnlyIntegerProperty selectedSingleIndexProperty()
	{
		return selectedSingleIndex.getReadOnlyProperty();
	}

	public ReadOnlyBooleanProperty isSingleSelectedProperty()
	{
		return isSingleSelected.getReadOnlyProperty();
	}

	/**
	 * Display the given image.
	 *
	 * @param imageDescriptor the given image, may be null to clear the display
	 */
	public void setImageDescriptor(@Nullable ImageDescriptor imageDescriptor)
	{
		final ImageLayer imageViewTransform = selectedLayer.get();
		if (imageViewTransform != nullLayer)
		{
			imageViewTransform.setImageDescriptor(imageDescriptor);
		}
	}

	public DoubleProperty rotateProperty()
	{
		return rotateProperty;
	}

	public BooleanProperty scrollBarsEnabledProperty()
	{
		return scrollBarsEnabled;
	}

	public DoubleProperty zoomFixedProperty()
	{
		return zoomFixedProperty;
	}

	public ObjectProperty<ZoomMode> zoomModeProperty()
	{
		return zoomModeProperty;
	}

	public ReadOnlyDoubleProperty zoomFactorProperty()
	{
		return zoomFactor.getReadOnlyProperty();
	}
}
