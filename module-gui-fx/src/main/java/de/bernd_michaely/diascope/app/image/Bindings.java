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

import static javafx.beans.binding.Bindings.createDoubleBinding;

/// Factory class for custom bindings.
///
/// @author Factory class for custom bindings.
///
class Bindings
{
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

	/// Binding to calculate the tangent of an angle.
	///
	/// @param angle an angle in degrees
	/// @return the tangent of the given angle
	///
	static DoubleBinding tan(ObservableNumberValue angle)
	{
		return createDoubleBinding(
			() -> Math.tan(Math.toRadians(angle.doubleValue())),
			angle);
	}

	/// Binding to calculate the arctangent.
	///
	/// @param value a given value
	/// @return the arctangent of the given value in degrees
	///
	static DoubleBinding arctan(ObservableNumberValue value)
	{
		return createDoubleBinding(
			() -> Math.toDegrees(Math.atan(value.doubleValue())),
			value);
	}

	/// Binding to normalize an angle.
	///
	/// @param angle an angle in degrees
	/// @return the angle normalized to the range [0°..360°[
	///
	static DoubleBinding normalizeAngle(ObservableNumberValue angle)
	{
		return createDoubleBinding(
			() -> normalizeAngle(angle.doubleValue()),
			angle);
	}
}
