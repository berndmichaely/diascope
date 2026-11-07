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

import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.image.NormalizationUtil.*;
import static org.assertj.core.api.Assertions.*;

/// NormalizationUtil Test.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class NormalizationUtilTest
{
	private static void _testFunction(DoubleUnaryOperator function, String name, Map<Double, Double> testArgs)
	{
		System.out.println("# %s::%s".formatted(NormalizationUtil.class.getSimpleName(), name));
		assertThat(testArgs).allSatisfy((arg, value) ->
		{
			final String description = "f(%6.1f°) = %6.1f°".formatted(arg, value);
			System.out.println("  · %s".formatted(description));
			assertThat(function.applyAsDouble(arg)).as(description).isEqualTo(value);
		});
	}

	@Test
	public void test_normalizeAngle()
	{
		final var testArgs = Map.of(
			-180d, 180d,
			-1d, 359d,
			0d, 0d,
			180d, 180d,
			359d, 359d,
			360d, 0d
		);
		_testFunction(arg -> normalizeAngle(arg), "normalizeAngle", testArgs);
		_testFunction(arg -> normalizeAngleToRange(arg, 0d), "normalizeAngleToRange", testArgs);
	}

	@Test
	public void test_normalizeAngleToRange()
	{
		_testFunction(arg -> normalizeAngleToRange(arg, 180d), "normalizeAngleToRange", Map.of(
			-180d, 180d,
			0d, 360d,
			179d, 539d,
			180d, 180d,
			359d, 359d,
			360d, 360d,
			539d, 539d,
			540d, 180d,
			541d, 181d
		));
	}

	@Test
	public void test_normalizeAngleToClosedRange()
	{
		_testFunction(arg -> normalizeAngleToClosedRange(arg, 180d), "normalizeAngleToClosedRange", Map.of(
			-180d, 180d,
			0d, 360d,
			179d, 539d,
			180d, 180d,
			359d, 359d,
			360d, 360d,
			539d, 539d,
			540d, 540d,
			541d, 181d
		));
	}
}
