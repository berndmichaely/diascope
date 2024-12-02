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
package de.bernd_michaely.diascope.app.stage;

/**
 * Enum to keep keys for preferences.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public enum PreferencesKeys
{
	PREF_KEY_X,
	PREF_KEY_Y,
	PREF_KEY_WIDTH,
	PREF_KEY_HEIGHT,
	PREF_KEY_MAXIMIZE,
	PREF_KEY_MAIN_SPLIT_POS,
	PREF_KEY_IMAGE_SPLIT_POS,
	PREF_KEY_FSTV_VISIBLE,
	PREF_KEY_SELECTED_PATH,
	PREF_KEY_SHOW_HIDDEN_DIRS,
	PREF_KEY_SHOW_STATUS_LINE;

	private final String key;

	static final String PREFIX_KEYS = "PREF_KEY_";

	private PreferencesKeys()
	{
		key = name().substring(PREFIX_KEYS.length()).toLowerCase();
	}

	public String getKey()
	{
		return key;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [" + name() + " → »" + getKey() + "«]";
	}
}
