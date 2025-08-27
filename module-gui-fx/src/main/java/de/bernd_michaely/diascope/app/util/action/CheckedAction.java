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
import java.util.List;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;

/// Checked Action to handle selected and disabled properties of toggles.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class CheckedAction extends ActionBase
{
	private static final Logger logger = System.getLogger(CheckedAction.class.getName());
	private final ActionItemDescriptor actionItemDescriptor;
	private final Set<Toggle> setToggles;
	private final Set<CheckMenuItem> menuItems;
	private final BooleanProperty selectedProperty = new SimpleBooleanProperty();
	private boolean changing;

	public CheckedAction()
	{
		this(ActionItemDescriptor.EMPTY);
	}

	public CheckedAction(ActionItemDescriptor actionItemDescriptor)
	{
		this.actionItemDescriptor = actionItemDescriptor;
		this.menuItems = new HashSet<>();
		this.setToggles = new HashSet<>();
		selectedProperty.addListener(onChange(selected ->
		{
			if (!changing)
			{
				changing = true;
				try
				{
					setToggles.forEach(t -> t.setSelected(selected));
					menuItems.forEach(t -> t.setSelected(selected));
				}
				finally
				{
					changing = false;
				}
			}
		}));
	}

	void addMenuItem(CheckMenuItem checkMenuItem)
	{
		if (!menuItems.add(checkMenuItem))
		{
			throw new IllegalArgumentException("Same menu item added twice: »%s«".formatted(checkMenuItem));
		}
		checkMenuItem.setSelected(isSelected());
		checkMenuItem.selectedProperty().addListener(onChange(selected ->
		{
			if (!changing)
			{
				changing = true;
				try
				{
					menuItems.stream().filter(m -> m != checkMenuItem).forEach(m -> m.setSelected(selected));
					setToggles.stream().forEach(t -> t.setSelected(selected));
					selectedProperty.set(selected);
				}
				finally
				{
					changing = false;
				}
			}
		}));
		checkMenuItem.disableProperty().bind(this.disableProperty());
		initActionItem(actionItemDescriptor, checkMenuItem);
	}

	void addToggle(Toggle toggle)
	{
		if (!setToggles.add(toggle))
		{
			throw new IllegalArgumentException("Same toggle added twice: »%s«".formatted(toggle));
		}
		toggle.setSelected(isSelected());
		toggle.selectedProperty().addListener(onChange(selected ->
		{
			if (!changing)
			{
				changing = true;
				try
				{
					setToggles.stream().filter(t -> t != toggle).forEach(t -> t.setSelected(selected));
					menuItems.stream().forEach(m -> m.setSelected(selected));
					selectedProperty.set(selected);
				}
				finally
				{
					changing = false;
				}
			}
		}));
		if (toggle instanceof ToggleButton button)
		{
			button.disableProperty().bind(this.disableProperty());
			initActionItem(actionItemDescriptor, button);
		}
	}

	@Override
	public List<CheckMenuItem> createMenuItems()
	{
		final var menuItem = new CheckMenuItem();
		addMenuItem(menuItem);
		return List.of(menuItem);
	}

	@Override
	public List<ToggleButton> createToolBarButtons()
	{
		final var button = new ToggleButton();
		addToggle(button);
		return List.of(button);
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

	public void toggle()
	{
		setSelected(!isSelected());
	}
}
