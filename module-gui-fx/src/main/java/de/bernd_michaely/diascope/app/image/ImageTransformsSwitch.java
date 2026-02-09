/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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

import de.bernd_michaely.diascope.app.util.beans.ListChangeListenerBuilder;
import java.lang.System.Logger;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;

/// Class for connecting facade with image layer ImageTransforms.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageTransformsSwitch implements AutoCloseable
{
	private static final Logger logger = System.getLogger(ImageTransformsSwitch.class.getName());
	private final ObservableList<ImageLayer> unmodifiableLayers;
	private final ObservableList<ImageLayer> unmodifiableSpotLayers;
	private final ReadOnlyObjectProperty<Optional<ImageLayer>> singleSelectedLayerProperty;
	private final ReadOnlyBooleanProperty gridModeProperty;
	private final ImageTransformsImpl facadeImageTransforms;
	private final ImageTransformsImpl globalImageTransforms;
	private final Map<ImageLayer, ImageTransformsImpl> mapImageTransforms;

	ImageTransformsSwitch(
		ObservableList<ImageLayer> unmodifiableLayers,
		ObservableList<ImageLayer> unmodifiableSpotLayers,
		ReadOnlyObjectProperty<Optional<ImageLayer>> singleSelectedLayerProperty,
		ReadOnlyBooleanProperty gridModeProperty)
	{
		this.unmodifiableLayers = unmodifiableLayers;
		this.unmodifiableSpotLayers = unmodifiableSpotLayers;
		this.singleSelectedLayerProperty = singleSelectedLayerProperty;
		this.gridModeProperty = gridModeProperty;
		this.facadeImageTransforms = new ImageTransformsImpl();
		this.globalImageTransforms = new ImageTransformsImpl();
		this.mapImageTransforms = new IdentityHashMap<>();
//		System.out.println("this.globalImageTransforms.bindControlProperties(this.facadeImageTransforms)");

		unmodifiableSpotLayers.forEach(layer ->
			layer.getImageTransforms().bindControlProperties(globalImageTransforms));

		final Consumer<ImageLayer> bindLayer = imageLayer ->
		{
		};
		final Consumer<ImageLayer> unbindLayer = imageLayer ->
		{
		};
		if (!unmodifiableLayers.isEmpty())
		{
			throw new IllegalStateException(
				"%s : Layers not empty before addListener".formatted(getClass().getName()));
		}
		unmodifiableLayers.addListener(new ListChangeListenerBuilder<ImageLayer>()
			.onAdd(change ->
			{
				final boolean isGridMode = gridModeProperty.get();
				change.getAddedSubList().forEach(layer ->
				{
					final var imageTransforms = new ImageTransformsImpl();
					layer.getImageTransforms().bindControlProperties(imageTransforms);
					mapImageTransforms.put(layer, imageTransforms);
					if (!isGridMode)
					{
						imageTransforms.bindControlProperties(globalImageTransforms);
					}
				});
			})
			.onRemove(change ->
			{
				change.getRemoved().forEach(layer ->
				{
					try (var  _ = mapImageTransforms.remove(layer))
					{
					}
				});
			})
			.build());
		singleSelectedLayerProperty.addListener(onChange(optionalImageLayer ->
		{
			if (optionalImageLayer.isPresent())
			{
				final var imageLayer = optionalImageLayer.get();
			}
			else
			{
			}
		}));
		final Consumer<Boolean> onGridModeChange = isGridMode ->
		{
			if (isGridMode)
			{
				globalImageTransforms.unbindControlProperties();
			}
			else
			{
				globalImageTransforms.adjustControlProperties(facadeImageTransforms);
				globalImageTransforms.bindControlProperties(facadeImageTransforms);
			}
		};
		onGridModeChange.accept(gridModeProperty.get());
		gridModeProperty.addListener(onChange(onGridModeChange));
	}

	ImageTransforms getFacadeImageTransforms()
	{
		return facadeImageTransforms;
	}

	private void bindLayer(ImageLayer imageLayer)
	{
	}

	private void unbindLayer(ImageLayer imageLayer)
	{
	}

	/// {@inheritDoc}
	///
	/// This implementation unbinds all properties.
	///
	@Override
	public void close()
	{
		try (facadeImageTransforms; globalImageTransforms)
		{
			mapImageTransforms.clear();
		}
	}
}
