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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import static de.bernd_michaely.diascope.app.stage.PreferencesKeys.*;
import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.*;
import static de.bernd_michaely.diascope.app.util.beans.property.PersistedProperties.*;

/// Persisted properties for main content parts.
/// The state is maintained separately for window and fullscreen mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class MainContentProperties
{
	private final BooleanProperty toolBarVisible;
	private final BooleanProperty thumbnailsVisible;
	private final BooleanProperty dividersVisible;
	private final BooleanProperty scrollBarsVisible;

	private record PersistanceParams(PreferencesKeys key, boolean defaultValue)
		{
	}

	MainContentProperties()
	{
		this.toolBarVisible = new SimpleBooleanProperty();
		this.thumbnailsVisible = new SimpleBooleanProperty();
		this.dividersVisible = new SimpleBooleanProperty();
		this.scrollBarsVisible = new SimpleBooleanProperty();
	}

	private MainContentProperties(
		PersistanceParams paramsToolBar, PersistanceParams paramsThumbnails,
		PersistanceParams paramsDividers, PersistanceParams paramsScrollBars)
	{
		final var c = MainContentProperties.class;
		this.toolBarVisible = newPersistedBooleanProperty(
			paramsToolBar.key(), c, paramsToolBar.defaultValue());
		this.thumbnailsVisible = newPersistedBooleanProperty(
			paramsThumbnails.key(), c, paramsThumbnails.defaultValue());
		this.dividersVisible = newPersistedBooleanProperty(
			paramsDividers.key(), c, paramsDividers.defaultValue());
		this.scrollBarsVisible = newPersistedBooleanProperty(
			paramsScrollBars.key(), c, paramsScrollBars.defaultValue());
	}

	BooleanProperty toolBarVisibleProperty()
	{
		return toolBarVisible;
	}

	BooleanProperty thumbnailsVisibleProperty()
	{
		return thumbnailsVisible;
	}

	BooleanProperty dividersVisibleProperty()
	{
		return dividersVisible;
	}

	BooleanProperty scrollBarsVisibleProperty()
	{
		return scrollBarsVisible;
	}

	void bindBidirectional(MainContentProperties other)
	{
		this.toolBarVisible.bindBidirectional(other.toolBarVisible);
		this.thumbnailsVisible.bindBidirectional(other.thumbnailsVisible);
		this.dividersVisible.bindBidirectional(other.dividersVisible);
		this.scrollBarsVisible.bindBidirectional(other.scrollBarsVisible);
	}

	void unbindBidirectional(MainContentProperties other)
	{
		this.toolBarVisible.unbindBidirectional(other.toolBarVisible);
		this.thumbnailsVisible.unbindBidirectional(other.thumbnailsVisible);
		this.dividersVisible.unbindBidirectional(other.dividersVisible);
		this.scrollBarsVisible.unbindBidirectional(other.scrollBarsVisible);
	}

	static MainContentProperties newPersistedProperties(ReadOnlyBooleanProperty fullScreenEnabled)
	{
		final var persistedProperties = new MainContentProperties()
		{
			private final MainContentProperties propertiesWindow =
				new MainContentProperties(
					new PersistanceParams(PREF_KEY_WINDOW_TOOLBAR, true),
					new PersistanceParams(PREF_KEY_WINDOW_THUMBNAILS, true),
					new PersistanceParams(PREF_KEY_WINDOW_DIVIDERS, false),
					new PersistanceParams(PREF_KEY_WINDOW_SCROLLBARS, false)
				);
			private final MainContentProperties propertiesFullScreen =
				new MainContentProperties(
					new PersistanceParams(PREF_KEY_FULLSCREEN_TOOLBAR, false),
					new PersistanceParams(PREF_KEY_FULLSCREEN_THUMBNAILS, false),
					new PersistanceParams(PREF_KEY_FULLSCREEN_DIVIDERS, false),
					new PersistanceParams(PREF_KEY_FULLSCREEN_SCROLLBARS, false)
				);

			private void bindBidirectional(boolean isFullScreen)
			{
				if (isFullScreen)
				{
					unbindBidirectional(propertiesWindow);
					bindBidirectional(propertiesFullScreen);
				}
				else
				{
					unbindBidirectional(propertiesFullScreen);
					bindBidirectional(propertiesWindow);
				}
			}
		};
		persistedProperties.bindBidirectional(fullScreenEnabled.get());
		fullScreenEnabled.addListener(onChange(persistedProperties::bindBidirectional));
		return persistedProperties;
	}
}
