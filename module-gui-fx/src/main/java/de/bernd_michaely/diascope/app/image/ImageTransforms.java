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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Properties for external control of image transformations.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public sealed interface ImageTransforms permits DefaultImageTransforms
{
	/// Property holding the nullable zoom mode value.
	///
	/// @return Property holding the zoom mode value.
	///         The property value may be `null`.
	///         (The property itself is never `null`.)
	/// @see #zoomModeOrDefaultProperty()
	///
	ObjectProperty<@Nullable ZoomMode> zoomModeRawValueProperty();

	/// Property returning the non-`null` zoom mode value or the default, if the
	/// raw value is `null`.
	///
	/// @return Property returning the zoomModeRawValue, if it is non-`null`,
	///         or the default value otherwise.
	///         (That is, the property value is never `null`).
	/// @see #zoomModeRawValueProperty()
	///
	ReadOnlyObjectProperty<ZoomMode> zoomModeOrDefaultProperty();

	/// Property holding the externally given fixed zoom factor value.
	///
	/// @return a property holding the externally given fixed zoom factor value
	///
	DoubleProperty zoomFixedProperty();

	/// Property returning the actual calculated zoom factor value.
	///
	/// @return a property returning the actual calculated zoom factor value
	///
	ReadOnlyDoubleProperty zoomFactorProperty();

	/// Property holding the rotation angle in degrees.
	///
	/// @return a property holding the rotation angle in degrees
	///
	DoubleProperty rotateProperty();

	/// Property indicating the mirroring along x axis.
	///
	/// @return a property indicating the mirroring along x axis
	///
	BooleanProperty mirrorXProperty();

	/// Property indicating the mirroring along y axis.
	///
	/// @return a property indicating the mirroring along y axis
	///
	BooleanProperty mirrorYProperty();
}
