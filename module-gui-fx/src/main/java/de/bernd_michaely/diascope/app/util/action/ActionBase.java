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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/// Base class for Actions.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public abstract class ActionBase implements Action
{
	private final BooleanProperty disableProperty = new SimpleBooleanProperty();

	public BooleanProperty disableProperty()
	{
		return disableProperty;
	}

	public boolean isDisable()
	{
		return disableProperty.get();
	}

	public void setDisable(boolean disabled)
	{
		disableProperty.set(disabled);
	}

	static void initActionItem(ActionItemDescriptor actionItemDescriptor, ButtonBase button)
	{
		final var icon = actionItemDescriptor.icon();
		final String buttonTitle = actionItemDescriptor.buttonTitle();
		final Image iconImage;
		if (icon != null)
		{
			iconImage = icon.getIconImage();
			if (iconImage != null)
			{
				button.setGraphic(new ImageView(iconImage));
			}
		}
		else
		{
			iconImage = null;
		}
		if (iconImage == null && buttonTitle != null)
		{
			button.setText(buttonTitle);
		}
		final String tooltipText = actionItemDescriptor.tooltipText();
		final String text = tooltipText != null ? tooltipText : actionItemDescriptor.menuTitle();
		if (text != null && !text.isBlank())
		{
			button.setTooltip(new Tooltip(text));
		}
	}

	static void initActionItem(ActionItemDescriptor actionItemDescriptor, MenuItem menuItem)
	{
		final String menuTitle = actionItemDescriptor.menuTitle();
		if (menuTitle != null)
		{
			menuItem.setText(menuTitle);
		}
		final var icon = actionItemDescriptor.icon();
		if (icon != null)
		{
			final Image iconImage = icon.getIconImage(20);
			if (iconImage != null)
			{
				menuItem.setGraphic(new ImageView(iconImage));
			}
		}
	}
}
