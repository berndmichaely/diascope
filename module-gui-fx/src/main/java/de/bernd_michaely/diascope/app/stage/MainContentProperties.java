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

import de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import static de.bernd_michaely.diascope.app.util.beans.property.PersistedProperties.*;

/// Persisted properties for main content parts.
/// The state is maintained separately for window and fullscreen mode.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
interface MainContentProperties
{
	BooleanProperty toolBarVisibleProperty();

	BooleanProperty thumbnailsVisibleProperty();

	BooleanProperty dividersVisibleProperty();

	BooleanProperty scrollBarsVisibleProperty();

	ReadOnlyBooleanProperty fullScreenEnabledProperty();

	static MainContentProperties newInstance(ReadOnlyBooleanProperty fullScreenEnabled)
	{
		record PersistedBooleanPropertyParams(PreferencesKeys key, boolean defaultValue)
			{
		}
		class PropertiesImpl implements MainContentProperties
		{
			private final BooleanProperty toolBarVisible, thumbnailsVisible, dividersVisible, scrollBarsVisible;

			public PropertiesImpl(
				PersistedBooleanPropertyParams paramsToolBar,
				PersistedBooleanPropertyParams paramsThumbnails,
				PersistedBooleanPropertyParams paramsDividers,
				PersistedBooleanPropertyParams paramsScrollBars)
			{
				this.toolBarVisible = newPersistedBooleanProperty(
					paramsToolBar.key(), MainContentProperties.class, paramsToolBar.defaultValue());
				this.thumbnailsVisible = newPersistedBooleanProperty(
					paramsThumbnails.key(), MainContentProperties.class, paramsThumbnails.defaultValue());
				this.dividersVisible = newPersistedBooleanProperty(
					paramsDividers.key(), MainContentProperties.class, paramsDividers.defaultValue());
				this.scrollBarsVisible = newPersistedBooleanProperty(
					paramsScrollBars.key(), MainContentProperties.class, paramsScrollBars.defaultValue());
			}

			@Override
			public BooleanProperty toolBarVisibleProperty()
			{
				return toolBarVisible;
			}

			@Override
			public BooleanProperty thumbnailsVisibleProperty()
			{
				return thumbnailsVisible;
			}

			@Override
			public BooleanProperty dividersVisibleProperty()
			{
				return dividersVisible;
			}

			@Override
			public BooleanProperty scrollBarsVisibleProperty()
			{
				return scrollBarsVisible;
			}

			@Override
			public ReadOnlyBooleanProperty fullScreenEnabledProperty()
			{
				return fullScreenEnabled;
			}
		}
		return new MainContentProperties()
		{
			private final PropertiesImpl propertiesWindow;
			private final PropertiesImpl propertiesFullScreen;
			private final BooleanProperty toolBarVisible;
			private final BooleanProperty thumbnailsVisible;
			private final BooleanProperty dividersVisible;
			private final BooleanProperty scrollBarsVisible;


			{
				propertiesWindow = new PropertiesImpl(
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_WINDOW_TOOLBAR, true),
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_WINDOW_THUMBNAILS, true),
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_WINDOW_DIVIDERS, false),
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_WINDOW_SCROLLBARS, false)
				);
				propertiesFullScreen = new PropertiesImpl(
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_FULLSCREEN_TOOLBAR, true),
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_FULLSCREEN_THUMBNAILS, true),
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_FULLSCREEN_DIVIDERS, false),
					new PersistedBooleanPropertyParams(PreferencesKeys.PREF_KEY_FULLSCREEN_SCROLLBARS, false)
				);
				toolBarVisible = new SimpleBooleanProperty();
				thumbnailsVisible = new SimpleBooleanProperty();
				dividersVisible = new SimpleBooleanProperty();
				scrollBarsVisible = new SimpleBooleanProperty();
				fullScreenEnabled.addListener(ChangeListenerUtil.onChange(isFullScreen ->
				{
					if (isFullScreen)
					{
						toolBarVisible.bindBidirectional(propertiesFullScreen.toolBarVisible);
						thumbnailsVisible.bindBidirectional(propertiesFullScreen.thumbnailsVisible);
						dividersVisible.bindBidirectional(propertiesFullScreen.dividersVisible);
						scrollBarsVisible.bindBidirectional(propertiesFullScreen.scrollBarsVisible);
					}
					else
					{
						toolBarVisible.bindBidirectional(propertiesWindow.toolBarVisible);
						thumbnailsVisible.bindBidirectional(propertiesWindow.thumbnailsVisible);
						dividersVisible.bindBidirectional(propertiesWindow.dividersVisible);
						scrollBarsVisible.bindBidirectional(propertiesWindow.scrollBarsVisible);
					}
				}));
			}

			@Override
			public BooleanProperty toolBarVisibleProperty()
			{
				return toolBarVisible;
			}

			@Override
			public BooleanProperty thumbnailsVisibleProperty()
			{
				return thumbnailsVisible;
			}

			@Override
			public BooleanProperty dividersVisibleProperty()
			{
				return dividersVisible;
			}

			@Override
			public BooleanProperty scrollBarsVisibleProperty()
			{
				return scrollBarsVisible;
			}

			@Override
			public ReadOnlyBooleanProperty fullScreenEnabledProperty()
			{
				return fullScreenEnabled;
			}
		};
	}
}
