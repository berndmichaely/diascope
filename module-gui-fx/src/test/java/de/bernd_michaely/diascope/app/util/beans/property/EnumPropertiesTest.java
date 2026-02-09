/*
 * Copyright (C) 2025 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.util.beans.property;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.util.beans.property.EnumPropertiesTest.TestEnum.*;
import static org.junit.jupiter.api.Assertions.*;

/// EnumProperties Test.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class EnumPropertiesTest
{
	enum TestEnum
	{
		ONE, TWO, THREE;

		static TestEnum getTestEnumDefault()
		{
			return TWO;
		}
	}

	@Test
	public void test_null()
	{
		System.out.println("test_null");
		assertThrows(NullPointerException.class, () -> EnumProperties.createInstance(null));
	}

	private static void test_characteristics(EnumProperties<TestEnum> ep, TestEnum value)
	{
		for (TestEnum testEnum : TestEnum.values())
		{
			System.out.println("  · test_characteristics for »%s« ← »%s« ".formatted(value, testEnum));
			final boolean expected = testEnum == value;
			assertEquals(expected, ep.isValueProperty(testEnum).getValue());
			assertEquals(expected, ep.isValue(testEnum));
		}
	}

	private static void test_values(EnumProperties<TestEnum> ep, TestEnum testEnum)
	{
		System.out.println("· test value »%s«".formatted(testEnum));
		ep.setRawValue(testEnum);
		System.out.println("  → " + ep);
		final TestEnum expectedRaw = testEnum;
		final TestEnum expected = testEnum != null ? testEnum : getTestEnumDefault();
		assertEquals(expectedRaw, ep.rawValueProperty().get());
		assertEquals(expectedRaw, ep.getRawValue());
		assertTrue(ep.isRawValue(expectedRaw));
		assertEquals(expected, ep.valueOrDefaultProperty().get());
		assertEquals(expected, ep.getValueOrDefault());
		test_characteristics(ep, expected);
	}

	@Test
	public void test_EnumProperties()
	{
		System.out.println("test_EnumProperties");
		try (EnumProperties<TestEnum> ep = EnumProperties.createInstance(getTestEnumDefault()))
		{
			test_values(ep, null);
			assertEquals(TWO, ep.getDefaultValue());
			assertNull(ep.getRawValue());
			assertEquals(TWO, ep.getValueOrDefault());
			for (var testEnum : TestEnum.values())
			{
				test_values(ep, testEnum);
			}
			test_values(ep, null);
		}
	}

	private static class _ChangeListener implements ChangeListener<TestEnum>
	{
		TestEnum oldValue;
		TestEnum newValue;

		_ChangeListener(TestEnum initalValue)
		{
			this.oldValue = initalValue;
			this.newValue = initalValue;
		}

		@Override
		public void changed(ObservableValue<? extends TestEnum> observable, TestEnum oldValue, TestEnum newValue)
		{
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
	}

	@Test
	public void test_ChangeListener()
	{
		System.out.println("test_ChangeListener");
		final var changeListener = new _ChangeListener(ONE);
		assertEquals(ONE, changeListener.oldValue);
		assertEquals(ONE, changeListener.newValue);
		try (var  _ = EnumProperties.createInstance(getTestEnumDefault(), changeListener))
		{
			assertNull(changeListener.oldValue);
			assertEquals(TWO, changeListener.newValue);
		}
	}

	@Test
	public void test_ChangeListeners()
	{
		System.out.println("test_ChangeListeners");
		final var changeListener1 = new _ChangeListener(ONE);
		final var changeListener2 = new _ChangeListener(THREE);
		assertEquals(ONE, changeListener1.oldValue);
		assertEquals(ONE, changeListener1.newValue);
		assertEquals(THREE, changeListener2.oldValue);
		assertEquals(THREE, changeListener2.newValue);
		final var enumProperties = EnumProperties.createInstance(
			getTestEnumDefault(), List.of(changeListener1, changeListener2));
		try (enumProperties)
		{
			assertNull(changeListener1.oldValue);
			assertEquals(TWO, changeListener1.newValue);
			assertNull(changeListener2.oldValue);
			assertEquals(TWO, changeListener2.newValue);
			enumProperties.setRawValue(ONE);
			assertEquals(TWO, changeListener1.oldValue);
			assertEquals(TWO, changeListener2.oldValue);
			assertEquals(ONE, changeListener1.newValue);
			assertEquals(ONE, changeListener2.newValue);
		}
		// check listener cleanup:
		enumProperties.setRawValue(THREE);
		assertEquals(TWO, changeListener1.oldValue);
		assertEquals(TWO, changeListener2.oldValue);
		assertEquals(ONE, changeListener1.newValue);
		assertEquals(ONE, changeListener2.newValue);
	}
}
