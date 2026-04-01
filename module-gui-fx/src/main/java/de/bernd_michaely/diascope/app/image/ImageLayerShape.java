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

import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import static java.lang.Math.max;
import static java.lang.Math.round;

/// Interface to describe an ImageLayer selection shape.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
sealed interface ImageLayerShape permits AbstractImageLayerShape
{
	List<Color> COLORS_SELECTED = List.of(Color.CORNFLOWERBLUE, Color.CORAL);
	Paint COLOR_UNSELECTED = Color.ALICEBLUE;
	double STROKE_WIDTH_UNSELECTED = max(1, round(Font.getDefault().getSize() / 15));

	void setLayerSelectionHandler(Consumer<Boolean> layerSelectionHandler);

	BooleanProperty selectedProperty();
}
