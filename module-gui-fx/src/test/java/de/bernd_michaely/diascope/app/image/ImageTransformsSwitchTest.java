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
import de.bernd_michaely.diascope.app.util.beans.property.EnumProperties;
import java.util.Map;
import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.unmodifiableObservableList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ImageTransformsSwitch Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ImageTransformsSwitchTest
{
	class Transformable_ implements Transformable
	{
		private final ImageTransformsImpl imageTransforms;
		private final String name;

		Transformable_(String name)
		{
			this.name = name;
			this.imageTransforms = new ImageTransformsImpl();
		}

		@Override
		public ImageTransformsImpl getImageTransforms()
		{
			return imageTransforms;
		}

		@Override
		public String toString()
		{
			return "%s[%s]".formatted(getClass().getSimpleName(), name);
		}
	}
	private EnumProperties<Mode> modeProperties;
	private ReadOnlyObjectWrapper<Optional<Transformable_>> singleSelectedLayerProperty;
	private ObservableList<Transformable_> layers;
	private ObservableList<Transformable_> spotLayers;
	private ImageTransformsSwitch<Transformable_> its;

	@BeforeEach
	public void setUp()
	{
		modeProperties = EnumProperties.createInstance(Mode.getInitialMode());
		singleSelectedLayerProperty = new ReadOnlyObjectWrapper<>(Optional.empty());
		layers = observableArrayList();
		spotLayers = observableArrayList();
		its = new ImageTransformsSwitch<>(modeProperties,
			singleSelectedLayerProperty.getReadOnlyProperty(),
			unmodifiableObservableList(layers),
			unmodifiableObservableList(spotLayers));
	}

	@AfterEach
	public void tearDown()
	{
		its.close();
		modeProperties.close();
	}

	private boolean isLocal()
	{
		return its._getLocal().get();
	}

	private boolean isGlobal()
	{
		return !isLocal();
	}

	private void setLocal(boolean local)
	{
		modeProperties.setRawValue(local ? Mode.GRID : Mode.SPLIT);
	}

	/// Finds a description of the Transformable associated with the given
	/// ImageTransforms.
	///
	private String findTransformable(ImageTransforms imageTransforms)
	{
		return layers.stream()
			.filter(t -> t.getImageTransforms() == imageTransforms)
			.findAny()
			.map(t -> "<LAYER_[%s]>".formatted(t))
			.orElseGet(() ->
			{
				final Map<Transformable_, ImageTransformsImpl> map = its._getMapIntermediate();
				return map.entrySet().stream()
					.filter(entry -> entry.getValue() == imageTransforms)
					.findAny()
					.map(Map.Entry::getKey)
					.map(t -> "<INTERMEDIATE_[%s]>".formatted(t))
					.orElseGet(() ->
					{
						if (imageTransforms == its._getFacadeImageTransforms())
						{
							return "<FACADE>";
						}
						else if (imageTransforms == its._getGlobalImageTransforms())
						{
							return "<GLOBAL>";
						}
						else
						{
							return "<NOT FOUND>";
						}
					});
			});
	}

	@Test
	public void test_mode_switch()
	{
		System.out.println("test_ImageTransformsSwitch_mode_switch");
		// check initial state
		assertNotNull(its._getFacadeImageTransforms());
		assertNotNull(its._getGlobalImageTransforms());
		assertTrue(isGlobal());
		assertFalse(its._getLocal().get());
		assertTrue(its._getMapIntermediate().isEmpty());
		assertTrue(layers.isEmpty());
		assertTrue(spotLayers.isEmpty());
		Optional<ImageTransformsImpl> optional;
		optional = its._getSelectedImageTransforms().get();
		assertTrue(optional.isPresent());
		assertEquals(its._getGlobalImageTransforms(), optional.get());
		// switch to local
		setLocal(true);
		assertTrue(isLocal());
		optional = its._getSelectedImageTransforms().get();
		assertFalse(optional.isPresent());
		// add layer
		final Transformable_ ta = new Transformable_("A");
		final Transformable_ tb = new Transformable_("B");
		final Transformable_ tc = new Transformable_("C");
		layers.addAll(ta, tb, tc);
		optional = its._getSelectedImageTransforms().get();
		assertFalse(optional.isPresent());
		ImageTransformsImpl expected, actual;
		// select layer
		singleSelectedLayerProperty.set(Optional.of(tb));
		optional = its._getSelectedImageTransforms().get();
		assertTrue(optional.isPresent());
		expected = its._getMapIntermediate().get(tb);
		actual = optional.get();
		assertEquals(expected, actual,
			"expected »%s« ←→ actual »%s«".formatted(tb, findTransformable(actual)));
		// select other layer
		singleSelectedLayerProperty.set(Optional.of(tc));
		optional = its._getSelectedImageTransforms().get();
		assertTrue(optional.isPresent());
		expected = its._getMapIntermediate().get(tc);
		actual = optional.get();
		assertEquals(expected, actual,
			"expected »%s« ←→ actual »%s«".formatted(tc, findTransformable(actual)));
		// switch to global
		setLocal(false);
		assertFalse(isLocal());
		optional = its._getSelectedImageTransforms().get();
		assertTrue(optional.isPresent());
		assertEquals(its._getGlobalImageTransforms(), optional.get());
	}

	@Test
	public void test_facade_properties()
	{
		System.out.println("test_ImageTransformsSwitch_facade_properties");
		final ImageTransforms facade = its.getFacadeImageTransforms();
		assertTrue(isGlobal());
		// add layer
		final Transformable_ ta = new Transformable_("A");
		final Transformable_ tb = new Transformable_("B");
		final Transformable_ tc = new Transformable_("C");
		layers.addAll(ta, tb, tc);
		final EnumProperties<ZoomMode> zmp_a = ta.getImageTransforms().zoomModeProperties();
		final EnumProperties<ZoomMode> zmp_b = tb.getImageTransforms().zoomModeProperties();
		final EnumProperties<ZoomMode> zmp_c = tc.getImageTransforms().zoomModeProperties();
		zmp_a.isValue(ZoomMode.getDefault());
		zmp_b.isValue(ZoomMode.getDefault());
		zmp_c.isValue(ZoomMode.getDefault());
		// set global mode
		facade.zoomModeRawValueProperty().set(ZoomMode.FIT);
		zmp_a.isValue(ZoomMode.FIT);
		zmp_b.isValue(ZoomMode.FIT);
		zmp_c.isValue(ZoomMode.FIT);
		setLocal(true);
		assertTrue(isLocal());
		zmp_a.isValue(ZoomMode.getDefault());
		zmp_b.isValue(ZoomMode.getDefault());
		zmp_c.isValue(ZoomMode.getDefault());
		// no single selection
		assertTrue(singleSelectedLayerProperty.get().isEmpty());
		facade.zoomModeRawValueProperty().set(ZoomMode.ORIGINAL);
		zmp_a.isValue(ZoomMode.getDefault());
		zmp_b.isValue(ZoomMode.getDefault());
		zmp_c.isValue(ZoomMode.getDefault());
		// select layer b
		singleSelectedLayerProperty.set(Optional.of(tb));
		facade.zoomModeRawValueProperty().set(ZoomMode.ORIGINAL);
		zmp_a.isValue(ZoomMode.getDefault());
		zmp_b.isValue(ZoomMode.ORIGINAL);
		zmp_c.isValue(ZoomMode.getDefault());
		setLocal(false);
		assertTrue(isGlobal());
		zmp_a.isValue(ZoomMode.FIT);
		zmp_b.isValue(ZoomMode.FIT);
		zmp_c.isValue(ZoomMode.FIT);
		setLocal(true);
		assertTrue(isLocal());
		zmp_a.isValue(ZoomMode.getDefault());
		zmp_b.isValue(ZoomMode.ORIGINAL);
		zmp_c.isValue(ZoomMode.getDefault());
		setLocal(false);
		assertTrue(isGlobal());
		zmp_a.isValue(ZoomMode.FIT);
		zmp_b.isValue(ZoomMode.FIT);
		zmp_c.isValue(ZoomMode.FIT);
		// set global mode
		facade.zoomModeRawValueProperty().set(ZoomMode.FILL);
		zmp_a.isValue(ZoomMode.FILL);
		zmp_b.isValue(ZoomMode.FILL);
		zmp_c.isValue(ZoomMode.FILL);
		setLocal(true);
		assertTrue(isLocal());
		zmp_a.isValue(ZoomMode.getDefault());
		zmp_b.isValue(ZoomMode.ORIGINAL);
		zmp_c.isValue(ZoomMode.getDefault());
	}
}
