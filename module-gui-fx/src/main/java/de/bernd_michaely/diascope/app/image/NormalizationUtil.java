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

import static de.bernd_michaely.diascope.app.image.Bindings.C;

/// Utility for normalization of angles.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class NormalizationUtil
{
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

	/// Normalizes the given angle to be within the range
	/// `[rangeLow..(rangeLow+360°)[`.
	///
	/// *Implementation note:* assuming angle is already close to range.
	///
	/// @param angle    the given angle
	/// @param rangeLow the lower range bound
	/// @return the normalized angle
	///
	static double normalizeAngleToRange(double angle, double rangeLow)
	{
		final double rangeHigh = rangeLow + C;
		double a = angle;
		while (a < rangeLow)
		{
			a += C;
		}
		while (a >= rangeHigh)
		{
			a -= C;
		}
		return a;
	}

	/// Normalizes the given angle to be within the range
	/// `[rangeLow..(rangeLow+360°)]`.
	///
	/// *Implementation note:* assuming angle is already close to range.
	///
	/// @param angle    the given angle
	/// @param rangeLow the lower range bound
	/// @return the normalized angle
	///
	static double normalizeAngleToClosedRange(double angle, double rangeLow)
	{
		final double rangeHigh = rangeLow + C;
		double a = angle;
		while (a < rangeLow)
		{
			a += C;
		}
		while (a > rangeHigh)
		{
			a -= C;
		}
		return a;
	}
}
