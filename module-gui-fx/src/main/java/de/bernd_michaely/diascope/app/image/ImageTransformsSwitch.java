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
/// **API note**
///
/// `_get*()` methods are for use by unit tests only.
///
/// @param <T> generic Transformable type, e.g. ImageLayer
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ImageTransformsSwitch<T extends Transformable> implements AutoCloseable
{
	private final DefaultImageTransforms facadeImageTransforms = new DefaultImageTransforms();
	private final DefaultImageTransforms globalImageTransforms = new DefaultImageTransforms();
	private final Map<T, DefaultImageTransforms> mapIntermediate = new IdentityHashMap<>();
	private final ObservableBooleanValue local;
	private final ObjectProperty<Optional<DefaultImageTransforms>> selectedImageTransforms;

	ImageTransformsSwitch(EnumProperties<Mode> modeProperties,
		ReadOnlyObjectProperty<Optional<T>> singleSelectedLayerProperty,
		ObservableList<T> unmodifiableLayers,
		ObservableList<T> unmodifiableSpotLayers)
	{
		this.local = modeProperties.isValueProperty(Mode.GRID);
		this.selectedImageTransforms = new SimpleObjectProperty<>(Optional.empty());
		selectedImageTransforms.addListener(onChange((oldValue, newValue) ->
		{
			oldValue.ifPresent(DefaultImageTransforms::unbindAllProperties);
			newValue.ifPresent(selectedTransforms ->
			{
				selectedTransforms.adjustControlProperties(facadeImageTransforms);
				selectedTransforms.bindAllProperties(facadeImageTransforms);
			});
		}));
		selectedImageTransforms.bind(new ObjectBinding<Optional<DefaultImageTransforms>>()
		{
			{
				@SuppressWarnings("method.invocation")
				final Runnable init = () -> super.bind(local, singleSelectedLayerProperty);
				init.run();
			}
			private final Optional<DefaultImageTransforms> optionalGlobalImageTransforms =
				Optional.of(globalImageTransforms);

			@Override
			protected Optional<DefaultImageTransforms> computeValue()
			{
				return local.get() ? singleSelectedLayerProperty.get().map(mapIntermediate::get) :
					optionalGlobalImageTransforms;
			}
		});
		final Consumer<T> addLayer = layer ->
		{
			final var intermediateImageTransforms = new DefaultImageTransforms();
			mapIntermediate.put(layer, intermediateImageTransforms);
			final Consumer<Boolean> onLocalChange = isLocal ->
			{
				final var layerImageTransforms = layer.getImageTransforms();
				layerImageTransforms.unbindAllProperties();
				layerImageTransforms.bindAllProperties(isLocal ?
					intermediateImageTransforms : globalImageTransforms);
			};
			onLocalChange.accept(local.get());
			local.addListener(onChange(onLocalChange));
		};
		final Consumer<T> removeLayer = layer ->
		{
			try (layer)
			{
				final DefaultImageTransforms removed = mapIntermediate.remove(layer);
				if (removed != null)
				{
					layer.getImageTransforms().unbindAllProperties();
					removed.unbindAllProperties();
				}
				else
				{
					throw new IllegalStateException(
						getClass().getName() + ": Removing invalid layer");
				}
			}
		};
		unmodifiableLayers.forEach(addLayer);
		unmodifiableLayers.addListener(new ListChangeListenerBuilder<T>()
			.onAdd(change -> change.getAddedSubList().forEach(addLayer))
			.onRemove(change -> change.getRemoved().forEach(removeLayer))
			.build());
		unmodifiableSpotLayers.forEach(layer ->
			layer.getImageTransforms().bindAllProperties(globalImageTransforms));
	}

	/// Returns the ImageTransforms of the facade.
	///
	/// @return the ImageTransforms of the facade
	///
	ImageTransforms getFacadeImageTransforms()
	{
		return facadeImageTransforms;
	}

	DefaultImageTransforms _getFacadeImageTransforms()
	{
		return facadeImageTransforms;
	}

	DefaultImageTransforms _getGlobalImageTransforms()
	{
		return globalImageTransforms;
	}

	ObservableBooleanValue _getLocal()
	{
		return local;
	}

	Map<T, DefaultImageTransforms> _getMapIntermediate()
	{
		return mapIntermediate;
	}

	ObjectProperty<Optional<DefaultImageTransforms>> _getSelectedImageTransforms()
	{
		return selectedImageTransforms;
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
