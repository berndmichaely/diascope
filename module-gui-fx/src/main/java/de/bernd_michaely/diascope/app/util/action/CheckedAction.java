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

import java.lang.System.Logger;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;

/// Checked Action to handle selected and disabled properties of toggles.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class CheckedAction extends Action
{
	private static final Logger logger = System.getLogger(CheckedAction.class.getName());
	private final ActionItemDescriptor actionItemDescriptor;
	private final Set<Toggle> setToggles;
	private final BooleanProperty selectedProperty = new SimpleBooleanProperty();
	private boolean changing;

	public CheckedAction()
	{
		this(ActionItemDescriptor.EMPTY);
	}

	public CheckedAction(ActionItemDescriptor actionItemDescriptor)
	{
		this.actionItemDescriptor = actionItemDescriptor;
		this.setToggles = new HashSet<>();
		selectedProperty.addListener(onChange(selected ->
		{
			if (!changing)
			{
				changing = true;
				try
				{
					setToggles.forEach(t -> t.setSelected(selected));
				}
				finally
				{
					changing = false;
				}
			}
		}));
	}

	/// Add toggles.
	///
	/// @param toggles the toggles to add
	///
	/// @throws IllegalArgumentException if trying to add the same toggle twice
	///
	public void addToggles(Toggle... toggles)
	{
		for (var toggle : toggles)
		{
			if (!setToggles.add(toggle))
			{
				throw new IllegalArgumentException("Same toggle added twice: »%s«".formatted(toggle));
			}
			toggle.selectedProperty().addListener(onChange(selected ->
			{
				if (!changing)
				{
					changing = true;
					try
					{
						setToggles.stream().filter(t -> t != toggle).forEach(t -> t.setSelected(selected));
						selectedProperty.set(selected);
					}
					finally
					{
						changing = false;
					}
				}
			}));
			switch (toggle)
			{
				case MenuItem menuItem ->
				{
					menuItem.disableProperty().bind(this.disableProperty());
					initActionItem(actionItemDescriptor, menuItem);
				}
				case ToggleButton button ->
				{
					button.disableProperty().bind(this.disableProperty());
					initActionItem(actionItemDescriptor, button);
				}
				default ->
				{
					logger.log(WARNING, () -> "Unknown type of toggle: " + toggle);
				}
			}
		}
	}

	public BooleanProperty selectedProperty()
	{
		return selectedProperty;
	}

	public boolean isSelected()
	{
		return selectedProperty.get();
	}

	public void setSelected(boolean selected)
	{
		selectedProperty.set(selected);
	}
}
