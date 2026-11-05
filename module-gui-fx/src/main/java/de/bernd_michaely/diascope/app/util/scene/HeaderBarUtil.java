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
package de.bernd_michaely.diascope.app.util.scene;

import de.bernd_michaely.diascope.app.stage.MainWindow;
import io.brunoborges.jairosvg.JairoSVG;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/// Utility to use a HeaderBar.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class HeaderBarUtil
{
	private static final Logger logger = System.getLogger(HeaderBarUtil.class.getName());
	private static final String FILE_RES_ICON_STAGE_2 = "diascope_t.svg";

	public static @Nullable
	Image createResourcePngImage(String fileName)
	{
		Image icon = null;
		try (var inputStream = MainWindow.class.getResourceAsStream(fileName))
		{
			if (inputStream != null)
			{
				icon = new Image(inputStream);
			}
		}
		catch (IOException ex)
		{
			logger.log(TRACE, "Can't find stage icon »%s«".formatted(fileName), ex);
			icon = null;
		}
		if (icon == null)
		{
			logger.log(WARNING, "Can't find stage icon (ignoring…)");
		}
		return icon;
	}

	public static @Nullable
	Image createResourceSvgImage(String fileName, double size)
	{
		Image icon = null;
		try (var inputStream = MainWindow.class.getResourceAsStream(fileName))
		{
			if (inputStream != null)
			{
				final byte[] bufferPng = JairoSVG.builder()
					.fromStream(inputStream)
					.outputWidth(size)
					.outputHeight(size)
					.toPng();
				try (final var byteArrayInputStream = new ByteArrayInputStream(bufferPng))
				{
					icon = new Image(byteArrayInputStream);
				}
			}
		}
		catch (Exception ex)
		{
			logger.log(TRACE, "Can't find stage icon »%s«".formatted(fileName), ex);
			icon = null;
		}
		if (icon == null)
		{
			logger.log(WARNING, "Can't find stage icon (ignoring…)");
		}
		return icon;
	}

	public static Region createHeaderBar(Stage stage, Label title)
	{
		return createHeaderBar(stage, title, null);
	}

	public static Region createHeaderBar(Stage stage, Region headerBarCtrl)
	{
		return createHeaderBar(stage, null, headerBarCtrl);
	}

	public static Region createHeaderBar(Stage stage, @Nullable Region title, @Nullable Region headerBarCtrl)
	{
		final Region mainControl;
		if (headerBarCtrl != null)
		{
			mainControl = headerBarCtrl;
		}
		else if (title != null)
		{
			mainControl = title;
		}
		else
		{
			throw new IllegalStateException(HeaderBarUtil.class.getName() +
				"createHeaderBar : at least one control is needed");
		}
		final double offset = Font.getDefault().getSize() * 3 / 8;
		final var headerBarMain = new HeaderBar();
		if (title != null)
		{
			final boolean centerTitleToHeaderBar = true;
			if (centerTitleToHeaderBar)
			{
				title.setMouseTransparent(true);
				headerBarMain.setCenter(title);
			}
			else
			{
				final var titlePane = new BorderPane(title);
				titlePane.setMouseTransparent(true);
				headerBarMain.setCenter(titlePane);
			}
		}
		else
		{
			mainControl.setMouseTransparent(true);
		}
		if (headerBarCtrl != null)
		{
			if (title != null)
			{
				headerBarMain.setLeft(headerBarCtrl);
			}
			else
			{
				headerBarMain.setCenter(headerBarCtrl);
			}
			HeaderBar.setAlignment(headerBarCtrl, Pos.CENTER_LEFT);
			HeaderBar.setMargin(headerBarCtrl, new Insets(offset));
			if (title != null)
			{
				HeaderBar.setMargin(title, new Insets(offset));
			}
		}
		final var imageView = new ImageView();
		imageView.setPreserveRatio(true);
		mainControl.heightProperty().addListener(onChange(newValue ->
		{
			final double height = newValue.doubleValue();
			imageView.fitWidthProperty().set(height);
			imageView.fitHeightProperty().set(height);
			final var image = createResourceSvgImage(FILE_RES_ICON_STAGE_2, height);
			if (image != null)
			{
				imageView.setImage(image);
			}
		}));
		imageView.setMouseTransparent(true);
		HeaderBar.setAlignment(imageView, Pos.CENTER);
		HeaderBar.setMargin(imageView, new Insets(0, offset, 0, offset));
		final var headerBarIcon = new HeaderBar();
		headerBarIcon.setCenter(imageView);
		headerBarIcon.setRightSystemPadding(false);
		headerBarMain.setLeftSystemPadding(false);
		final var pane = new BorderPane(headerBarMain);
		pane.setLeft(headerBarIcon);
		pane.setBorder(Border.EMPTY);
		pane.setPadding(Insets.EMPTY);
		return pane;
	}
}
