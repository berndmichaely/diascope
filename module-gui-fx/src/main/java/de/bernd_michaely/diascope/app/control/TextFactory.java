/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.control;

import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Utility class to create text nodes.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class TextFactory
{
	private final Paint paint;
	private final double fontSize;

	public TextFactory(Paint paint, double fontSize)
	{
		this.paint = paint;
		this.fontSize = fontSize;
	}

	public Region createTextNode(String content)
	{
		final Text text = new Text(content);
		text.setY(this.fontSize);
		text.setCache(true);
		text.setFill(this.paint);
		text.setFont(Font.font("", FontWeight.BOLD, this.fontSize));
		final DropShadow ds = new DropShadow();
		ds.setOffsetX(5);
		ds.setOffsetY(5);
		ds.setColor(Color.GRAY);
		text.setEffect(ds);
		final double widthTextMax = text.getBoundsInLocal().getWidth();
		final Pane pane = new Pane(text);
		pane.widthProperty().addListener((observable, oldValue, newValue) ->
		{
			final double width = newValue.doubleValue();
			text.setTranslateX(Math.max(0.0, (width - widthTextMax) / 2.0));
			text.setWrappingWidth(width);
		});
		return pane;
	}
}
