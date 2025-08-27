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
package de.bernd_michaely.diascope.app.util.action;

import java.util.List;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;

/// Base interface for Actions.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public interface Action
{
	/// A singleton action to indicate separators.
	Action SEPARATOR = new Action()
	{
		@Override
		public List<SeparatorMenuItem> createMenuItems()
		{
			return List.of(new SeparatorMenuItem());
		}

		@Override
		public List<Separator> createToolBarButtons()
		{
			return List.of(new Separator());
		}
	};

	/// Creates toolbar buttons corresponding to the action.
	///
	/// @return a list of toolbar buttons
	///
	List<? extends Control> createToolBarButtons();

	/// Creates contextmenu items corresponding to the action.
	///
	/// @return a list of contextmenu items
	///
	List<? extends MenuItem> createMenuItems();
}
