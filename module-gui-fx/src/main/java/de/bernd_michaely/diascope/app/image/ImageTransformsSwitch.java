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

import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import de.bernd_michaely.diascope.app.util.beans.ListChangeListenerBuilder;
import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;

/// Class for connecting facade with image layer ImageTransforms.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageTransformsSwitch implements AutoCloseable
{
	private final ImageTransformsImpl facadeImageTransforms = new ImageTransformsImpl();
	private final ImageTransformsImpl globalImageTransforms = new ImageTransformsImpl();
	private final Map<ImageLayer, ImageTransformsImpl> mapIntermediate = new IdentityHashMap<>();
	private final ObservableBooleanValue local;
	private final ObjectProperty<Optional<ImageTransformsImpl>> selectedImageTransforms;

	ImageTransformsSwitch(EnumProperties<Mode> modeProperties,
		ReadOnlyObjectProperty<Optional<ImageLayer>> singleSelectedLayerProperty,
		ObservableList<ImageLayer> unmodifiableLayers,
		ObservableList<ImageLayer> unmodifiableSpotLayers)
	{
		this.local = modeProperties.isValueProperty(Mode.GRID);
		this.selectedImageTransforms = new SimpleObjectProperty<>();
		selectedImageTransforms.addListener(onChange(optionalImageTransforms ->
		{
			mapIntermediate.forEach((layer, intermediateTransforms) ->
			{
				layer.getImageTransforms().unbindAllProperties();
				intermediateTransforms.unbindAllProperties();
			});
			globalImageTransforms.unbindAllProperties();
			facadeImageTransforms.unbindAllProperties();
			optionalImageTransforms.ifPresent(selectedTransforms ->
			{
				if (selectedTransforms == globalImageTransforms)
				{
					unmodifiableLayers.stream().map(ImageLayer::getImageTransforms)
						.forEach(layerTransforms -> layerTransforms.bindAllProperties(globalImageTransforms));
					globalImageTransforms.adjustControlProperties(facadeImageTransforms);
					globalImageTransforms.bindAllProperties(facadeImageTransforms);
				}
				else
				{
					final var selectedLayer = singleSelectedLayerProperty.get().get();
					mapIntermediate.forEach((layer, intermediateTransforms) ->
					{
						final var layerTransforms = layer.getImageTransforms();
						layerTransforms.adjustControlProperties(intermediateTransforms);
						layerTransforms.bindAllProperties(intermediateTransforms);
						if (layer == selectedLayer)
						{
							intermediateTransforms.adjustControlProperties(facadeImageTransforms);
							intermediateTransforms.bindAllProperties(facadeImageTransforms);
						}
					});
				}
			});
		}));
		selectedImageTransforms.bind(new ObjectBinding<Optional<ImageTransformsImpl>>()
		{
			{
				@SuppressWarnings("method.invocation")
				final Runnable init = () -> super.bind(local, singleSelectedLayerProperty);
				init.run();
			}
			private final Optional<ImageTransformsImpl> optionalGlobalImageTransforms =
				Optional.of(globalImageTransforms);

			@Override
			protected Optional<ImageTransformsImpl> computeValue()
			{
				return local.get() ? singleSelectedLayerProperty.get().map(mapIntermediate::get) :
					optionalGlobalImageTransforms;
			}
		});
		final Consumer<ImageLayer> addLayer = layer ->
		{
			mapIntermediate.put(layer, new ImageTransformsImpl());
			if (!local.get())
			{
				layer.getImageTransforms().bindAllProperties(globalImageTransforms);
			}
		};
		final Consumer<ImageLayer> removeLayer = layer ->
		{
			try (layer)
			{
				final var removed = mapIntermediate.remove(layer);
				if (removed != null)
				{
					removed.unbindAllProperties();
				}
			}
		};
		unmodifiableLayers.forEach(addLayer);
		unmodifiableLayers.addListener(new ListChangeListenerBuilder<ImageLayer>()
			.onAdd(change -> change.getAddedSubList().forEach(addLayer))
			.onRemove(change -> change.getRemoved().forEach(removeLayer))
			.build());
	}

	ImageTransforms getFacadeImageTransforms()
	{
		return facadeImageTransforms;
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
			mapIntermediate.forEach((layer, intermediateTransforms) ->
			{
				try (intermediateTransforms; layer)
				{
				}
			});
		}
	}
}
