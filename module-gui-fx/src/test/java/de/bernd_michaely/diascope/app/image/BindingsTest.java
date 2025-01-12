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
package de.bernd_michaely.diascope.app.image;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Bindings.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class BindingsTest
{
	private void _testNormalizeAngle(double value, double expected)
	{
		final var angle = new SimpleDoubleProperty(value);
		final var resultWrapper = new ReadOnlyDoubleWrapper();
		final ReadOnlyDoubleProperty result = resultWrapper.getReadOnlyProperty();
		resultWrapper.bind(Bindings.normalizeAngle(angle));
		final double r = result.get();
		System.out.println("normalize(%8.2f°) = %6.2f°".formatted(value, r));
		assertEquals(expected, r);
	}

	@Test
	public void testNormalizeAngle()
	{
		_testNormalizeAngle(-1800.0, 0.0);
		_testNormalizeAngle(-720.0, 0.0);
		_testNormalizeAngle(-361.0, 359.0);
		_testNormalizeAngle(-360.0, 0.0);
		_testNormalizeAngle(-359.0, 1.0);
		_testNormalizeAngle(-181.0, 179.0);
		_testNormalizeAngle(-180.0, 180.0);
		_testNormalizeAngle(-179.0, 181.0);
		_testNormalizeAngle(-1.0, 359.0);
		_testNormalizeAngle(-0.0, 0.0);
		_testNormalizeAngle(0.0, 0.0);
		_testNormalizeAngle(1.0, 1.0);
		_testNormalizeAngle(359.9, 359.9);
		_testNormalizeAngle(360.0, 0.0);
		_testNormalizeAngle(539.0, 179.0);
		_testNormalizeAngle(540.0, 180.0);
		_testNormalizeAngle(720.0, 0.0);
		_testNormalizeAngle(1800.0, 0.0);
	}
}
