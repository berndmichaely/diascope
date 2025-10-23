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
package de.bernd_michaely.diascope.app.util.math;

import java.util.List;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.util.math.Operators.*;
import static java.lang.Double.*;
import static org.junit.jupiter.api.Assertions.*;

/// Test of class Operators.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class OperatorsTest
{
	private String getString(double d)
	{
		if (d == -MAX_VALUE)
		{
			return "-MAX_VALUE";
		}
		else if (d == MAX_VALUE)
		{
			return "MAX_VALUE";
		}
		else if (d == NEGATIVE_INFINITY)
		{
			return "NEGATIVE_INFINITY";
		}
		else if (d == POSITIVE_INFINITY)
		{
			return "POSITIVE_INFINITY";
		}
		else
		{
			return Double.toString(d);
		}
	}

	/**
	 * Generates the test cases named according to the pattern
	 * {@code  test_max_positive_%d_%d}. This saves a lot of typing, but uses the
	 * method to test to generate the expected values. The result has to be
	 * checked manually for correctness of course for the generated tests to be
	 * valid.
	 */
	private void _generateTestCases()
	{
		final List<Double> params = List.of(
			NEGATIVE_INFINITY, -MAX_VALUE, -1.0, -0.0,
			0.0, 1.0, MAX_VALUE, POSITIVE_INFINITY, NaN);
		final int n = params.size();
		for (int i = 0; i < n; i++)
		{
			for (int k = 0; k < n; k++)
			{
				final Double d1 = params.get(i);
				final Double d2 = params.get(k);
				final double expected = max_positive(d1, d2);
				System.out.println();
				System.out.println("@Test");
				System.out.println("public void test_max_positive_%d_%d()"
					.formatted(i, k));
				System.out.println("{");
				System.out.println("  check(%d, %d, %s, %s, %s);"
					.formatted(i, k, getString(d1), getString(d2), getString(expected)));
				System.out.println("}");
			}
		}
	}

	private void check(int i, int k, double d1, double d2, double expected)
	{
		assertEquals(expected, max_positive(d1, d2));
		System.out.println("· [%d/%d] max_positive(%18s, %18s) == %18s"
			.formatted(i, k, getString(d1), getString(d2), getString(expected)));
	}

	@Test
	public void test_max_positive_0_0()
	{
		check(0, 0, NEGATIVE_INFINITY, NEGATIVE_INFINITY, 0.0);
	}

	@Test
	public void test_max_positive_0_1()
	{
		check(0, 1, NEGATIVE_INFINITY, -MAX_VALUE, 0.0);
	}

	@Test
	public void test_max_positive_0_2()
	{
		check(0, 2, NEGATIVE_INFINITY, -1.0, 0.0);
	}

	@Test
	public void test_max_positive_0_3()
	{
		check(0, 3, NEGATIVE_INFINITY, -0.0, 0.0);
	}

	@Test
	public void test_max_positive_0_4()
	{
		check(0, 4, NEGATIVE_INFINITY, 0.0, 0.0);
	}

	@Test
	public void test_max_positive_0_5()
	{
		check(0, 5, NEGATIVE_INFINITY, 1.0, 1.0);
	}

	@Test
	public void test_max_positive_0_6()
	{
		check(0, 6, NEGATIVE_INFINITY, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_0_7()
	{
		check(0, 7, NEGATIVE_INFINITY, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_0_8()
	{
		check(0, 8, NEGATIVE_INFINITY, NaN, 0.0);
	}

	@Test
	public void test_max_positive_1_0()
	{
		check(1, 0, -MAX_VALUE, NEGATIVE_INFINITY, 0.0);
	}

	@Test
	public void test_max_positive_1_1()
	{
		check(1, 1, -MAX_VALUE, -MAX_VALUE, 0.0);
	}

	@Test
	public void test_max_positive_1_2()
	{
		check(1, 2, -MAX_VALUE, -1.0, 0.0);
	}

	@Test
	public void test_max_positive_1_3()
	{
		check(1, 3, -MAX_VALUE, -0.0, 0.0);
	}

	@Test
	public void test_max_positive_1_4()
	{
		check(1, 4, -MAX_VALUE, 0.0, 0.0);
	}

	@Test
	public void test_max_positive_1_5()
	{
		check(1, 5, -MAX_VALUE, 1.0, 1.0);
	}

	@Test
	public void test_max_positive_1_6()
	{
		check(1, 6, -MAX_VALUE, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_1_7()
	{
		check(1, 7, -MAX_VALUE, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_1_8()
	{
		check(1, 8, -MAX_VALUE, NaN, 0.0);
	}

	@Test
	public void test_max_positive_2_0()
	{
		check(2, 0, -1.0, NEGATIVE_INFINITY, 0.0);
	}

	@Test
	public void test_max_positive_2_1()
	{
		check(2, 1, -1.0, -MAX_VALUE, 0.0);
	}

	@Test
	public void test_max_positive_2_2()
	{
		check(2, 2, -1.0, -1.0, 0.0);
	}

	@Test
	public void test_max_positive_2_3()
	{
		check(2, 3, -1.0, -0.0, 0.0);
	}

	@Test
	public void test_max_positive_2_4()
	{
		check(2, 4, -1.0, 0.0, 0.0);
	}

	@Test
	public void test_max_positive_2_5()
	{
		check(2, 5, -1.0, 1.0, 1.0);
	}

	@Test
	public void test_max_positive_2_6()
	{
		check(2, 6, -1.0, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_2_7()
	{
		check(2, 7, -1.0, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_2_8()
	{
		check(2, 8, -1.0, NaN, 0.0);
	}

	@Test
	public void test_max_positive_3_0()
	{
		check(3, 0, -0.0, NEGATIVE_INFINITY, 0.0);
	}

	@Test
	public void test_max_positive_3_1()
	{
		check(3, 1, -0.0, -MAX_VALUE, 0.0);
	}

	@Test
	public void test_max_positive_3_2()
	{
		check(3, 2, -0.0, -1.0, 0.0);
	}

	@Test
	public void test_max_positive_3_3()
	{
		check(3, 3, -0.0, -0.0, 0.0);
	}

	@Test
	public void test_max_positive_3_4()
	{
		check(3, 4, -0.0, 0.0, 0.0);
	}

	@Test
	public void test_max_positive_3_5()
	{
		check(3, 5, -0.0, 1.0, 1.0);
	}

	@Test
	public void test_max_positive_3_6()
	{
		check(3, 6, -0.0, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_3_7()
	{
		check(3, 7, -0.0, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_3_8()
	{
		check(3, 8, -0.0, NaN, 0.0);
	}

	@Test
	public void test_max_positive_4_0()
	{
		check(4, 0, 0.0, NEGATIVE_INFINITY, 0.0);
	}

	@Test
	public void test_max_positive_4_1()
	{
		check(4, 1, 0.0, -MAX_VALUE, 0.0);
	}

	@Test
	public void test_max_positive_4_2()
	{
		check(4, 2, 0.0, -1.0, 0.0);
	}

	@Test
	public void test_max_positive_4_3()
	{
		check(4, 3, 0.0, -0.0, 0.0);
	}

	@Test
	public void test_max_positive_4_4()
	{
		check(4, 4, 0.0, 0.0, 0.0);
	}

	@Test
	public void test_max_positive_4_5()
	{
		check(4, 5, 0.0, 1.0, 1.0);
	}

	@Test
	public void test_max_positive_4_6()
	{
		check(4, 6, 0.0, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_4_7()
	{
		check(4, 7, 0.0, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_4_8()
	{
		check(4, 8, 0.0, NaN, 0.0);
	}

	@Test
	public void test_max_positive_5_0()
	{
		check(5, 0, 1.0, NEGATIVE_INFINITY, 1.0);
	}

	@Test
	public void test_max_positive_5_1()
	{
		check(5, 1, 1.0, -MAX_VALUE, 1.0);
	}

	@Test
	public void test_max_positive_5_2()
	{
		check(5, 2, 1.0, -1.0, 1.0);
	}

	@Test
	public void test_max_positive_5_3()
	{
		check(5, 3, 1.0, -0.0, 1.0);
	}

	@Test
	public void test_max_positive_5_4()
	{
		check(5, 4, 1.0, 0.0, 1.0);
	}

	@Test
	public void test_max_positive_5_5()
	{
		check(5, 5, 1.0, 1.0, 1.0);
	}

	@Test
	public void test_max_positive_5_6()
	{
		check(5, 6, 1.0, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_5_7()
	{
		check(5, 7, 1.0, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_5_8()
	{
		check(5, 8, 1.0, NaN, 1.0);
	}

	@Test
	public void test_max_positive_6_0()
	{
		check(6, 0, MAX_VALUE, NEGATIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_1()
	{
		check(6, 1, MAX_VALUE, -MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_2()
	{
		check(6, 2, MAX_VALUE, -1.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_3()
	{
		check(6, 3, MAX_VALUE, -0.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_4()
	{
		check(6, 4, MAX_VALUE, 0.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_5()
	{
		check(6, 5, MAX_VALUE, 1.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_6()
	{
		check(6, 6, MAX_VALUE, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_7()
	{
		check(6, 7, MAX_VALUE, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_6_8()
	{
		check(6, 8, MAX_VALUE, NaN, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_0()
	{
		check(7, 0, POSITIVE_INFINITY, NEGATIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_1()
	{
		check(7, 1, POSITIVE_INFINITY, -MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_2()
	{
		check(7, 2, POSITIVE_INFINITY, -1.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_3()
	{
		check(7, 3, POSITIVE_INFINITY, -0.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_4()
	{
		check(7, 4, POSITIVE_INFINITY, 0.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_5()
	{
		check(7, 5, POSITIVE_INFINITY, 1.0, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_6()
	{
		check(7, 6, POSITIVE_INFINITY, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_7()
	{
		check(7, 7, POSITIVE_INFINITY, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_7_8()
	{
		check(7, 8, POSITIVE_INFINITY, NaN, MAX_VALUE);
	}

	@Test
	public void test_max_positive_8_0()
	{
		check(8, 0, NaN, NEGATIVE_INFINITY, 0.0);
	}

	@Test
	public void test_max_positive_8_1()
	{
		check(8, 1, NaN, -MAX_VALUE, 0.0);
	}

	@Test
	public void test_max_positive_8_2()
	{
		check(8, 2, NaN, -1.0, 0.0);
	}

	@Test
	public void test_max_positive_8_3()
	{
		check(8, 3, NaN, -0.0, 0.0);
	}

	@Test
	public void test_max_positive_8_4()
	{
		check(8, 4, NaN, 0.0, 0.0);
	}

	@Test
	public void test_max_positive_8_5()
	{
		check(8, 5, NaN, 1.0, 1.0);
	}

	@Test
	public void test_max_positive_8_6()
	{
		check(8, 6, NaN, MAX_VALUE, MAX_VALUE);
	}

	@Test
	public void test_max_positive_8_7()
	{
		check(8, 7, NaN, POSITIVE_INFINITY, MAX_VALUE);
	}

	@Test
	public void test_max_positive_8_8()
	{
		check(8, 8, NaN, NaN, 0.0);
	}
}
