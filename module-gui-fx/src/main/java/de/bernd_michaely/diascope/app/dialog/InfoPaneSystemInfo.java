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
package de.bernd_michaely.diascope.app.dialog;

import de.bernd_michaely.common.filesystem.view.fx.FileSystemTreeViewVersionFX;
import de.bernd_michaely.common.semver.SemanticVersion;
import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.util.common.JreVersionUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.bernd_michaely.diascope.app.util.scene.ScreenUtil.*;

/**
 * InfoPane to display system info.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
final class InfoPaneSystemInfo implements InfoPane
{
	private static final String STR_TITLE = "System Info";

	@Override
	public String getTitle()
	{
		return STR_TITLE;
	}

	private static Map<String, ?> getSystemInfo()
	{
		final Map<String, Object> map = new LinkedHashMap<>();
		map.put("App Version",
			ApplicationConfiguration.getState().version().orElse(SemanticVersion.of()));

		final String strTitleUrl = "Available at:";
		try
		{
			map.put(strTitleUrl, new URI(STR_URL_APPLICATION));
		}
		catch (URISyntaxException ex)
		{
			map.put(strTitleUrl, STR_URL_APPLICATION);
		}

		map.put("Filesystem tree view (Base-module)",
			SemanticVersion.of(FileSystemTreeViewVersionFX.getBaseModuleVersion()));
		map.put("Filesystem tree view (FX-module)",
			SemanticVersion.of(FileSystemTreeViewVersionFX.getFxModuleVersion()));

		map.put("Java Runtime Version", JreVersionUtil.getJreVersionInfo());

		final String versionJavafx = System.getProperty("javafx.version");
		map.put("JavaFX Version", versionJavafx != null ? versionJavafx : MSG_UNKNOWN);

		final boolean isShapeClipSupported = Platform.isSupported(ConditionalFeature.SHAPE_CLIP);
		map.put("Shape clipping support", isShapeClipSupported);

		map.put("Operating system name", System.getProperty("os.name"));
		map.put("Operating system architecture", System.getProperty("os.arch"));
		final var runtime = Runtime.getRuntime();
		map.put("Number of available processors", "%d".formatted(runtime.availableProcessors()));
		map.put("Maximum heap memory size", "%d MB".formatted(runtime.maxMemory() / (1 << 20)));

//		final Label headerX3fLibVer = new Label("X3F Extractor library version");
//		final Label textX3fLibVer = new Label(LibraryVersionInfo.getLibraryVersionAsString());
//
//		final Label headerX3fDataFormat = new Label("Supported X3F data format");
//		final Label textX3fDataFormat = new Label(LibraryVersionInfo.getSupportedX3fVersion());
		return map;
	}

	static Map<String, ?> getDevelopmentInfo()
	{
		final Map<String, Object> map = new LinkedHashMap<>();
		map.put("Default font size", "%.1f points".formatted(DEFAULT_FONT_SIZE));
		map.put("Default icon size", "%dx%d".formatted(
			getScaledWidth(false), getScaledHeight(false)));
		map.put("Small icon size", "%dx%d".formatted(
			getScaledWidth(true), getScaledHeight(true)));
		return map;
	}

	static Region createTable(Map<String, ?> map)
	{
		final var gridPane = new GridPane(DEFAULT_FONT_SIZE, DEFAULT_FONT_SIZE / 2);
		final var fontBold = Font.font("", FontWeight.BOLD, DEFAULT_FONT_SIZE);
		int i = 0;
		for (var entry : map.entrySet())
		{
			final String key = entry.getKey();
			final var value = entry.getValue();
			final var labelKey = new Label(key);
			labelKey.setFont(fontBold);
			gridPane.add(labelKey, 0, i);
			GridPane.setHalignment(labelKey, HPos.RIGHT);
			final Node nodeValue = switch (value)
			{
				case null -> new Label();
				case URI uri ->
				{
					final var hyperlink = new Hyperlink(uri.getHost() + uri.getPath());
					final var strURI = uri.toString();
					hyperlink.setTooltip(new Tooltip(strURI));
					hyperlink.setTextFill(COLOR_HYPERLINK);
					hyperlink.setOnAction(_ -> ApplicationConfiguration.showDocument(uri));
					yield hyperlink;
				}
				case SemanticVersion sv ->
				{
					final var label = new Label(sv.getCanonicalForm());
					label.setTooltip(new Tooltip(sv.getDescription()));
					yield label;
				}
				case Boolean b ->
				{
					final var label = new Label(b ? "yes" : "no");
					label.setTextFill(b ? COLOR_YES : COLOR_NO);
					yield label;
				}
				default -> new Label(value.toString());
			};
			gridPane.add(nodeValue, 1, i++);
		}
		final var borderPane = new BorderPane(gridPane);
		BorderPane.setMargin(gridPane, new Insets(3 * DEFAULT_FONT_SIZE, DEFAULT_FONT_SIZE, 0, DEFAULT_FONT_SIZE));
		BorderPane.setAlignment(gridPane, Pos.CENTER);
		return borderPane;
	}

	@Override
	public Region getDisplay()
	{
		return createTable(getSystemInfo());
	}
}
