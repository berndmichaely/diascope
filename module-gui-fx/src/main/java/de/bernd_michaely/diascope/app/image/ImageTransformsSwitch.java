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

import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableProperties;
import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import de.bernd_michaely.diascope.app.util.beans.ListChangeListenerBuilder;
import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;
import java.lang.System.Logger;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import static de.bernd_michaely.diascope.app.image.NormalizationUtil.normalizeAngleToClosedRange;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;

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
class ImageTransformsSwitch<T extends AbstractImageLayer> implements AutoCloseable
{
	private static final Logger logger = System.getLogger(ImageTransformsSwitch.class.getName());

	private record TransformableParams<L extends AbstractImageLayer>(TransformParams local, DeltaTransformParams delta)
	{
		private TransformableParams(L layer)
		{
			final var imageTransforms = layer.getImageTransforms();
			this(TransformParams.createFrom(imageTransforms), DeltaTransformParams.createFrom(imageTransforms));
		}
	}

	private final DefaultImageTransforms facadeImageTransforms = new DefaultImageTransforms();
	private final TransformParams bufferGlobalParams = new TransformParams();
	private final Map<T, TransformableParams> mapBufferLocalParams = new IdentityHashMap<>(); // WeakHashMap?
	private final ObservableBooleanValue local;
	private final ChangeListener<Boolean> localModeListener;

	ImageTransformsSwitch(EnumProperties<Mode> modeProperties,
		SelectableProperties selectionModel, ListDualSelection<T> listDualSelection,
		ObservableList<T> unmodifiableLayers, ObservableList<T> unmodifiableSpotLayers)
	{
		// on view mode change:
		this.local = modeProperties.isValueProperty(Mode.GRID);
		this.localModeListener = onChange(isLocal ->
		{
			if (isLocal) // switch from global to local
			{
				// save global state:
				bufferGlobalParams.readFrom(facadeImageTransforms);
				// recall local layer specific params:
				mapBufferLocalParams.entrySet().forEach(entry ->
				{
					final T layer = entry.getKey();
					final TransformableParams buffer = entry.getValue();
					buffer.local().writeTo(layer.getImageTransforms());
				});
				// determine a selected layer and set facade control params to it, if present,
				// or otherwise use defaults:
				listDualSelection.oldestSelectedItemProperty().get().map(mapBufferLocalParams::get)
					.ifPresentOrElse(
						buffer -> buffer.local().writeTo(facadeImageTransforms),
						() -> TransformParams.initDefaultsOf(facadeImageTransforms));
			}
			else // switch from local to global
			{
				// save local layer specific params:
				mapBufferLocalParams.entrySet().forEach(entry ->
				{
					final T layer = entry.getKey();
					final TransformableParams buffer = entry.getValue();
					buffer.local().readFrom(layer.getImageTransforms());
				});
				// recall global state and set facade control params to it:
				bufferGlobalParams.writeTo(facadeImageTransforms);
			}
		});
		local.addListener(localModeListener);
		// on layer selection change:
		listDualSelection.oldestSelectedItemProperty().addListener(onChange(optionalLayer ->
		{
			if (local.get())
			{
				logger.log(TRACE, () -> "→ oldestSelectedItem : %s".formatted(optionalLayer
					.map(Object::toString).orElse("<NO LAYER>")));
				logger.log(TRACE, () -> "→ BUFFER content     : %s".formatted(optionalLayer
					.map(mapBufferLocalParams::get)
					.map(TransformableParams::local)
					.map(Object::toString).orElse("<NO LAYER>")));
				optionalLayer.map(mapBufferLocalParams::get)
					.ifPresentOrElse(
						buffer -> buffer.local().writeTo(facadeImageTransforms),
						() -> TransformParams.initDefaultsOf(facadeImageTransforms));
			}
		}));
		// on zoom mode change:
		facadeImageTransforms.zoomModeOrDefaultProperty().addListener(onChange(newValue ->
		{
			if (local.get())
			{
				final int n = unmodifiableLayers.size();
				for (int i = 0; i < n; i++)
				{
					if (selectionModel.isSelected(i))
					{
						unmodifiableLayers.get(i).getImageTransforms().zoomModeRawValueProperty().set(newValue);
					}
				}
			}
			else
			{
				Stream.concat(unmodifiableLayers.stream(), unmodifiableSpotLayers.stream()).forEach(t ->
					t.getImageTransforms().zoomModeRawValueProperty().set(newValue));
			}
		}));
		// on image transform property change:
		enum TransformOp
		{
			ZoomMode, ZoomFactor, Rotate, MirrorX, MirrorY
		}
		@FunctionalInterface
		interface AdjustmentFunction<V>
		{
			V apply(V propertyValue, V oldValue, V newValue);
		}
		class ImageTransformPropertyChangeListener<V, P extends Property<V>>
			implements ChangeListener<V>
		{
			private final TransformOp transformOp;
			private final AdjustmentFunction<V> adjustmentFunction;
			private final Function<ImageTransforms, P> transformsToProperty;

			ImageTransformPropertyChangeListener(TransformOp transformOp,
				AdjustmentFunction<V> adjustmentFunction, Function<ImageTransforms, P> transformsToProperty)
			{
				this.transformOp = transformOp;
				this.adjustmentFunction = adjustmentFunction;
				this.transformsToProperty = transformsToProperty;
			}

			@Override
			public void changed(ObservableValue<? extends V> observable, V oldValue, V newValue)
			{
				if (local.get())
				{
					listDualSelection.oldestSelectedItemProperty().get().ifPresent(oldestLayer ->
					{
						// at least one layer is selected
						final int n = unmodifiableLayers.size();
						for (int i = 0; i < n; i++)
						{
							if (selectionModel.isSelected(i))
							{
								final T layer = unmodifiableLayers.get(i);
								final P layerProperty = transformsToProperty.apply(layer.getImageTransforms());
								final V value = layer == oldestLayer ? newValue :
									adjustmentFunction.apply(layerProperty.getValue(), oldValue, newValue);
								layerProperty.setValue(value);
								final var transformableParams = mapBufferLocalParams.get(layer);
								if (transformableParams != null && value != null)
								{
									final var transformParams = transformableParams.local();
									switch (transformOp)
									{
										case ZoomMode -> transformParams.zoomMode = (ZoomMode) value;
										case ZoomFactor -> transformParams.zoomFactor = (double) value;
										case Rotate -> transformParams.rotate = (double) value;
										case MirrorX -> transformParams.mirrorX = (boolean) value;
										case MirrorY -> transformParams.mirrorY = (boolean) value;
										default -> throw new IllegalStateException(
												"%s : missing case for %s :: %s".formatted(
													getClass().getName(), TransformOp.class.getName(), transformOp.name()));
									}
								}
								else
								{
									throw new IllegalStateException(getClass().getName());
								}
							}
						}
					});
				}
				else // global
				{
					Stream.concat(unmodifiableLayers.stream(), unmodifiableSpotLayers.stream())
						.map(AbstractImageLayer::getImageTransforms)
						.map(transformsToProperty)
						.forEach(p -> p.setValue(newValue));
				}
			}
		}
		facadeImageTransforms.zoomModeOrDefaultProperty().addListener(
			new ImageTransformPropertyChangeListener<>(TransformOp.ZoomMode,
				(_, _, n) -> n,
				ImageTransforms::zoomModeRawValueProperty));
		facadeImageTransforms.zoomFixedProperty().addListener(
			new ImageTransformPropertyChangeListener<>(TransformOp.ZoomFactor,
				(p, o, n) -> p.doubleValue() * (n.doubleValue() / o.doubleValue()),
				ImageTransforms::zoomFixedProperty));
		facadeImageTransforms.rotateProperty().addListener(
			new ImageTransformPropertyChangeListener<>(TransformOp.Rotate,
				(p, o, n) -> normalizeAngleToClosedRange(p.doubleValue() + n.doubleValue() - o.doubleValue(), -180.0),
				ImageTransforms::rotateProperty));
		facadeImageTransforms.mirrorXProperty().addListener(
			new ImageTransformPropertyChangeListener<>(TransformOp.MirrorX,
				(p, _, _) -> !p,
				ImageTransforms::mirrorXProperty));
		facadeImageTransforms.mirrorYProperty().addListener(
			new ImageTransformPropertyChangeListener<>(TransformOp.MirrorY,
				(p, _, _) -> !p,
				ImageTransforms::mirrorYProperty));
		// on layer addition / removal:
		final Consumer<T> addLayer = layer ->
		{
			if (mapBufferLocalParams.put(layer, new TransformableParams<>(layer)) != null)
			{
				throw new IllegalStateException(
					"%s : adding the same layer twice".formatted(getClass().getName()));
			}
		};
		final Consumer<T> removeLayer = layer ->
		{
			if (mapBufferLocalParams.remove(layer) == null)
			{
				throw new IllegalStateException(
					"%s : trying to remove non existing layer".formatted(getClass().getName()));
			}
		};
		unmodifiableLayers.forEach(addLayer);
		unmodifiableLayers.addListener(new ListChangeListenerBuilder<T>()
			.onAdd(change -> change.getAddedSubList().forEach(addLayer))
			.onRemove(change -> change.getRemoved().forEach(removeLayer))
			.build());
	}

	/// Returns the ImageTransforms of the facade.
	///
	/// @return the ImageTransforms of the facade
	///
	ImageTransforms getFacadeImageTransforms()
	{
		return facadeImageTransforms;
	}

	DefaultImageTransforms _getGlobalImageTransforms()
	{
		final var imageTransforms = new DefaultImageTransforms();
		bufferGlobalParams.writeTo(imageTransforms);
		return imageTransforms;
	}

	ObservableBooleanValue _getLocal()
	{
		return local;
	}

	Map<T, DefaultImageTransforms> _getMapIntermediate()
	{
		final Map<T, DefaultImageTransforms> mapIntermediate = new IdentityHashMap<>();
		mapBufferLocalParams.forEach((layer, tp) ->
		{
			final var imageTransforms = new DefaultImageTransforms(layer.getImageDescriptor());
			tp.local().writeTo(imageTransforms);
			mapIntermediate.put(layer, imageTransforms);
		});
		return mapIntermediate;
	}

	/// {@inheritDoc}
	///
	/// This implementation unbinds all properties and removes listeners.
	///
	@Override
	public void close()
	{
		try (facadeImageTransforms)
		{
			mapBufferLocalParams.keySet().forEach(layer ->
			{
				try (layer)
				{
				}
			});
			local.removeListener(localModeListener);
		}
	}
}
