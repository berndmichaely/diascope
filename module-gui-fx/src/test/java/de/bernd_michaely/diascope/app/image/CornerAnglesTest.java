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

import javafx.beans.property.ReadOnlyDoubleWrapper;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.image.Border.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CornerAngles.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class CornerAnglesTest
{
	private void _test_CornerAngles(CornerAngles cornerAngles, double expectedAngle, Border border)
	{
		System.out.println("Corner %s→%s : %f°".formatted(border, border.next(), expectedAngle));
		assertEquals(expectedAngle, cornerAngles.get(border).get());
	}

	@Test
	public void test_CornerAngles()
	{
		System.out.println("CornerAngles: check square viewport:");
		final double size = 50.0;
		final var x = new ReadOnlyDoubleWrapper(size);
		final var y = new ReadOnlyDoubleWrapper(size);
		final var dx = new ReadOnlyDoubleWrapper(size);
		final var dy = new ReadOnlyDoubleWrapper(size);
		final var cornerAngles = new CornerAngles(
			x.getReadOnlyProperty(), y.getReadOnlyProperty(),
			dx.getReadOnlyProperty(), dy.getReadOnlyProperty());
		_test_CornerAngles(cornerAngles, 45.0, RIGHT);
		_test_CornerAngles(cornerAngles, 135.0, BOTTOM);
		_test_CornerAngles(cornerAngles, 225.0, LEFT);
		_test_CornerAngles(cornerAngles, 315.0, TOP);
	}
}
