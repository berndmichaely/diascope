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

import de.bernd_michaely.diascope.app.ApplicationConfiguration;
import de.bernd_michaely.diascope.app.util.common.JreVersionUtil;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * InfoPane to display system info.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class InfoPaneSystemInfo implements InfoPane
{
	private static final String MSG_UNKNOWN = "<unknown>";
	private final ReadOnlyBooleanWrapper showDefaultFontSizeProperty;

	public InfoPaneSystemInfo()
	{
		this.showDefaultFontSizeProperty = new ReadOnlyBooleanWrapper(false);
	}

	@Override
	public String getTitle()
	{
		return "System Info";
	}

	private ReadOnlyBooleanProperty showDefaultFontSizeProperty()
	{
		return this.showDefaultFontSizeProperty.getReadOnlyProperty();
	}

	@Override
	public Region getDisplay()
	{
		final Label headerAppVersion = new Label("App Version:");
		final Label textAppVersion = new Label();
		ApplicationConfiguration.getState().version().ifPresentOrElse(version ->
		{
			textAppVersion.setText(version.toString());
			final var tooltipAppVersion = new Tooltip(version.getDescription());
			headerAppVersion.setTooltip(tooltipAppVersion);
			textAppVersion.setTooltip(tooltipAppVersion);
		}, () -> textAppVersion.setText(MSG_UNKNOWN));

		final Label headerJreVersion = new Label("Java Runtime Version:");
		final Label textJreVersion = new Label(JreVersionUtil.getJreVersionInfo());

		final Label headerJavaFXVersion = new Label("JavaFX Version:");
		final String versionJavafx = System.getProperty("javafx.version");
		final Label textJavaFXVersion = new Label(versionJavafx != null ?
			versionJavafx : MSG_UNKNOWN);

		final Label headerShapeClipSupport = new Label("Shape clipping support:");
		final boolean isShapeClipSupported = Platform.isSupported(ConditionalFeature.SHAPE_CLIP);
		final Label textShapeClipSupport = new Label(
			//			"Shape clipping supported : " +
			(isShapeClipSupported ? "yes" : "no"));

		final Label headerOSName = new Label("Operating system name:");
		final Label textOSName = new Label(System.getProperty("os.name"));

		final Label headerOSArch = new Label("Operating system architecture:");
		final Label textOSArch = new Label(System.getProperty("os.arch"));

		final Label headerAvailableProcessors = new Label("Number of available processors:");
		final Label textAvailableProcessors = new Label("" + Runtime.getRuntime().availableProcessors());

		final Label headerMaxHeapSize = new Label("Maximum heap memory size:");
		final Label textMaxHeapSize = new Label(
			String.format("%.1f MB", (double) Runtime.getRuntime().maxMemory() / (1 << 20)));

		final Label headerDefaultFontSize = new Label("Default font size:");
		final Label textDefaultFontSize = new Label(DEFAULT_FONT_SIZE + " points");

//		final Label headerX3fLibVer = new Label("X3F Extractor library version");
//		final Label textX3fLibVer = new Label(LibraryVersionInfo.getLibraryVersionAsString());
//
//		final Label headerX3fDataFormat = new Label("Supported X3F data format");
//		final Label textX3fDataFormat = new Label(LibraryVersionInfo.getSupportedX3fVersion());
		final Font fontHeader = Font.font("", FontWeight.BOLD, DEFAULT_FONT_SIZE);
		headerAppVersion.setFont(fontHeader);
		headerJreVersion.setFont(fontHeader);
		headerJavaFXVersion.setFont(fontHeader);
		headerShapeClipSupport.setFont(fontHeader);
		headerOSName.setFont(fontHeader);
		headerOSArch.setFont(fontHeader);
		headerAvailableProcessors.setFont(fontHeader);
		headerMaxHeapSize.setFont(fontHeader);
		headerDefaultFontSize.setFont(fontHeader);
//		headerX3fLibVer.setFont(fontHeader);
//		headerX3fDataFormat.setFont(fontHeader);
		final VBox vBox = new VBox(
			headerAppVersion, textAppVersion,
			headerJreVersion, textJreVersion,
			headerJavaFXVersion, textJavaFXVersion,
			headerShapeClipSupport, textShapeClipSupport,
			headerOSName, textOSName,
			headerOSArch, textOSArch,
			headerAvailableProcessors, textAvailableProcessors,
			headerMaxHeapSize, textMaxHeapSize,
			//			headerX3fLibVer, textX3fLibVer,
			//			headerX3fDataFormat, textX3fDataFormat,
			headerDefaultFontSize, textDefaultFontSize);
		vBox.setPadding(new Insets(DEFAULT_INSET_SIZE, DEFAULT_INSET_SIZE, 0, DEFAULT_INSET_SIZE));
		vBox.setSpacing(DEFAULT_FONT_SIZE / 3);
		headerDefaultFontSize.visibleProperty().bind(showDefaultFontSizeProperty());
		textDefaultFontSize.visibleProperty().bind(showDefaultFontSizeProperty());
		this.showDefaultFontSizeProperty.bind(ApplicationConfiguration.getState().developmentModeProperty());
		return vBox;
	}
}
