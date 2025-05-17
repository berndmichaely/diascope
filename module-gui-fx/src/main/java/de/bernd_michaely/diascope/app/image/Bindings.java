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

import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableNumberValue;

/// Factory class for custom bindings.
///
/// @author Factory class for custom bindings.
///
class Bindings
{
	private static class TanBinding extends DoubleBinding
	{
		private final ObservableNumberValue angle;

		private TanBinding(ObservableNumberValue angle)
		{
			this.angle = angle;
		}

		@Override
		protected double computeValue()
		{
			return Math.tan(Math.toRadians(angle.doubleValue()));
		}

		private static TanBinding newInstance(ObservableNumberValue angle)
		{
			final var binding = new TanBinding(angle);
			binding.bind(angle);
			return binding;
		}
	}

	private static class ArcTanBinding extends DoubleBinding
	{
		private final ObservableNumberValue value;

		private ArcTanBinding(ObservableNumberValue value)
		{
			this.value = value;
		}

		@Override
		protected double computeValue()
		{
			return Math.toDegrees(Math.atan(value.doubleValue()));
		}

		private static ArcTanBinding newInstance(ObservableNumberValue value)
		{
			final var binding = new ArcTanBinding(value);
			binding.bind(value);
			return binding;
		}
	}

	/// The number of degrees of a full circle.
	static final double C = 360.0;

	/// Returns the normalized angle.
	///
	/// @param angle an angle in degrees
	/// @return the angle normalized to the range [0°..360°[
	///
	static double normalizeAngle(double angle)
	{
		double a = angle % C;
		if (a < 0.0)
		{
			a += C;
		}
		return a == -0.0 ? 0.0 : a;
	}

	private static class NormalizeAngleBinding extends DoubleBinding
	{
		private final ObservableNumberValue angle;

		private NormalizeAngleBinding(ObservableNumberValue angle)
		{
			this.angle = angle;
		}

		@Override
		protected double computeValue()
		{
			return normalizeAngle(angle.doubleValue());
		}

		private static NormalizeAngleBinding newInstance(ObservableNumberValue angle)
		{
			final var binding = new NormalizeAngleBinding(angle);
			binding.bind(angle);
			return binding;
		}
	}

	/// Binding to calculate the tangent of an angle.
	///
	/// @param angle an angle in degrees
	/// @return the tangent of the given angle
	///
	static DoubleBinding tan(ObservableNumberValue angle)
	{
		return TanBinding.newInstance(angle);
	}

	/// Binding to calculate the arctangent.
	///
	/// @param value a given value
	/// @return the arctangent of the given value in degrees
	///
	static DoubleBinding arctan(ObservableNumberValue value)
	{
		return ArcTanBinding.newInstance(value);
	}

	/// Binding to normalize an angle.
	///
	/// @param angle an angle in degrees
	/// @return the angle normalized to the range [0°..360°[
	///
	static DoubleBinding normalizeAngle(ObservableNumberValue angle)
	{
		return NormalizeAngleBinding.newInstance(angle);
	}
}
