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

import java.util.List;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.image.Border.*;
import static java.lang.Math.rint;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Divider.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class DividerTest
{
	@Test
	public void recap_atan2()
	{
		assertEquals(Math.atan2(0.0, 0.0), 0.0); // ensure result is not NaN
		final var values = List.of(1.0, +0.0, -0.0, -1.0);
		for (var y : values)
		{
			for (var x : values)
			{
				System.out.println("atan2(%+4.1f, %+4.1f) == %+6.1f°"
					.formatted(y, x, Math.toDegrees(Math.atan2(y, x))));
			}
		}
	}

	@Test
	public void test_getDividerBorder()
	{
		System.out.println("test divider border");
		final var width = new ReadOnlyDoubleWrapper(100.0);
		final var height = new ReadOnlyDoubleWrapper(100.0);
		final var x = new ReadOnlyDoubleWrapper();
		final var y = new ReadOnlyDoubleWrapper();
		// keep split center in the viewport center:
		x.bind(width.divide(2.0));
		y.bind(height.divide(2.0));
		final var dx = new ReadOnlyDoubleWrapper();
		dx.bind(width.subtract(x));
		final var dy = new ReadOnlyDoubleWrapper();
		dy.bind(height.subtract(y));
		final var cornerAngles = new CornerAngles(
			x.getReadOnlyProperty(), y.getReadOnlyProperty(),
			dx.getReadOnlyProperty(), dy.getReadOnlyProperty());
		final var divider = new Divider(cornerAngles, width, height, x, y, dx, dy);
		divider.setAngle(0.0);
		assertEquals(RIGHT, divider.getBorder());
		divider.setAngle(45.0);
		assertEquals(RIGHT, divider.getBorder());
		divider.setAngle(45.001);
		assertEquals(BOTTOM, divider.getBorder());
		divider.setAngle(90.0);
		assertEquals(BOTTOM, divider.getBorder());
		divider.setAngle(135.0);
		assertEquals(BOTTOM, divider.getBorder());
		divider.setAngle(135.001);
		assertEquals(LEFT, divider.getBorder());
		divider.setAngle(180.0);
		assertEquals(LEFT, divider.getBorder());
		divider.setAngle(225.0);
		assertEquals(LEFT, divider.getBorder());
		divider.setAngle(225.001);
		assertEquals(TOP, divider.getBorder());
		divider.setAngle(270.0);
		assertEquals(TOP, divider.getBorder());
		divider.setAngle(315.0);
		assertEquals(TOP, divider.getBorder());
		divider.setAngle(315.001);
		assertEquals(RIGHT, divider.getBorder());
		divider.setAngle(360.0);
		assertEquals(RIGHT, divider.getBorder());
		assertEquals(360.0, divider.getAngle());
	}

	@Test
	public void test_getDividerBorderXY()
	{
		System.out.println("test divider border intersection");
		final var width = new ReadOnlyDoubleWrapper(100.0);
		final var height = new ReadOnlyDoubleWrapper(100.0);
		final var x = new ReadOnlyDoubleWrapper();
		final var y = new ReadOnlyDoubleWrapper();
		// keep split center in the viewport center:
		x.bind(width.divide(2.0));
		y.bind(height.divide(2.0));
		final var dx = new ReadOnlyDoubleWrapper();
		dx.bind(width.subtract(x));
		final var dy = new ReadOnlyDoubleWrapper();
		dy.bind(height.subtract(y));
		final var cornerAngles = new CornerAngles(
			x.getReadOnlyProperty(), y.getReadOnlyProperty(),
			dx.getReadOnlyProperty(), dy.getReadOnlyProperty());
		final var divider = new Divider(cornerAngles, width, height, x, y, dx, dy);

		final double d30add = rint(50.0 + 50.0 * tan(toRadians(30.0)));
		final double d30sub = rint(50.0 - 50.0 * tan(toRadians(30.0)));

		divider.setAngle(0.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(50.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(30.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(d30add, rint(divider.getBorderIntersectionY()));

		divider.setAngle(60.0);
		assertEquals(d30add, rint(divider.getBorderIntersectionX()));
		assertEquals(100.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(90.0);
		assertEquals(50.0, rint(divider.getBorderIntersectionX()));
		assertEquals(100.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(120.0);
		assertEquals(d30sub, rint(divider.getBorderIntersectionX()));
		assertEquals(100.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(150.0);
		assertEquals(0.0, rint(divider.getBorderIntersectionX()));
		assertEquals(d30add, rint(divider.getBorderIntersectionY()));

		divider.setAngle(180.0);
		assertEquals(0.0, rint(divider.getBorderIntersectionX()));
		assertEquals(50.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(210.0);
		assertEquals(0.0, rint(divider.getBorderIntersectionX()));
		assertEquals(d30sub, rint(divider.getBorderIntersectionY()));

		divider.setAngle(240.0);
		assertEquals(d30sub, rint(divider.getBorderIntersectionX()));
		assertEquals(0.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(270.0);
		assertEquals(50.0, rint(divider.getBorderIntersectionX()));
		assertEquals(0.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(300.0);
		assertEquals(d30add, rint(divider.getBorderIntersectionX()));
		assertEquals(0.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(330.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(d30sub, rint(divider.getBorderIntersectionY()));

		divider.setAngle(360.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(50.0, rint(divider.getBorderIntersectionY()));

		// changing the viewport width:
		width.set(200.0);
		divider.setAngle(0.0);
		assertEquals(200.0, rint(divider.getBorderIntersectionX()));
		assertEquals(50.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(90.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(100.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(180.0);
		assertEquals(0.0, rint(divider.getBorderIntersectionX()));
		assertEquals(50.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(270.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(0.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(360.0);
		assertEquals(200.0, rint(divider.getBorderIntersectionX()));
		assertEquals(50.0, rint(divider.getBorderIntersectionY()));

		// changing the viewport height:
		height.set(300.0);
		divider.setAngle(0.0);
		assertEquals(200.0, rint(divider.getBorderIntersectionX()));
		assertEquals(150.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(90.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(300.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(180.0);
		assertEquals(0.0, rint(divider.getBorderIntersectionX()));
		assertEquals(150.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(270.0);
		assertEquals(100.0, rint(divider.getBorderIntersectionX()));
		assertEquals(0.0, rint(divider.getBorderIntersectionY()));

		divider.setAngle(360.0);
		assertEquals(200.0, rint(divider.getBorderIntersectionX()));
		assertEquals(150.0, rint(divider.getBorderIntersectionY()));
	}
}
