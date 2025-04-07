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
package de.bernd_michaely.diascope.app.stage;

import de.bernd_michaely.diascope.app.util.scene.SceneStylesheetUtil;
import java.util.function.Supplier;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;

/// Class to handle a FullScreen window.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class FullScreen
{
	private final BooleanProperty enabledProperty = new SimpleBooleanProperty();
	private @Nullable Stage stageFullScreen;

	FullScreen(Supplier<Region> contentSupplier, Runnable contentReAttach)
	{
		enabledProperty.addListener(onChange(enabled ->
		{
			if (enabled)
			{
				if (stageFullScreen == null)
				{
					final var stage = new Stage();
					stage.setOnCloseRequest(_ -> enabledProperty.set(false));
					stageFullScreen = stage;
					final var scene = new Scene(contentSupplier.get());
					SceneStylesheetUtil.setStylesheet(scene);
					stage.setScene(scene);
					stage.setFullScreen(true);
					stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
					stage.setFullScreenExitHint("");
					stage.show();
				}
			}
			else // close fullscreen
			{
				if (stageFullScreen != null)
				{
					stageFullScreen.close();
					contentReAttach.run();
					stageFullScreen = null;
				}
			}
		}));
	}

	BooleanProperty enabledProperty()
	{
		return enabledProperty;
	}

	boolean toggle()
	{
		final boolean newValue = !enabledProperty.get();
		enabledProperty.set(newValue);
		return newValue;
	}
}
