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

import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

/// Trigger Action to handle Buttons and context menues.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class TriggerAction extends Action
{
	private final Set<Button> buttons;
	private final Set<MenuItem> menuItems;
	private final ObjectProperty<EventHandler<ActionEvent>> onActionProperty;

	public TriggerAction(Runnable action)
	{
		this.buttons = new HashSet<>();
		this.menuItems = new HashSet<>();
		this.onActionProperty = new SimpleObjectProperty<>(_ -> action.run());
	}

	public ObjectProperty<EventHandler<ActionEvent>> onActionProperty()
	{
		return onActionProperty;
	}

	public EventHandler<ActionEvent> getOnAction()
	{
		return onActionProperty.get();
	}

	public void setOnAction(EventHandler<ActionEvent> eventHandler)
	{
		onActionProperty.set(eventHandler);
	}

	public void addButton(Button button)
	{
		if (!buttons.add(button))
		{
			throw new IllegalArgumentException("Same button added twice: »%s«".formatted(button));
		}
		button.disableProperty().bind(this.disabledProperty());
		button.onActionProperty().bind(this.onActionProperty());
	}

	public void addMenuItem(MenuItem menuItem)
	{
		if (!menuItems.add(menuItem))
		{
			throw new IllegalArgumentException("Same menu item added twice: »%s«".formatted(menuItem));
		}
		menuItem.disableProperty().bind(this.disabledProperty());
		menuItem.onActionProperty().bind(this.onActionProperty());
	}
}
