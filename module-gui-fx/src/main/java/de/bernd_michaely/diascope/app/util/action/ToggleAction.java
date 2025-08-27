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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;

/// Toggle Action to handle selected and disabled properties of toggles.
/// Each toggle item has its own disableProperty.
/// An individual toggle is disabled, if it is disabled via its own
/// disableProperty *or* the whole ToggleAction is disabled.
///
/// @param <E> the toggle key type
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ToggleAction<E extends Enum<E>> extends ActionBase
{
	private static final Logger logger = System.getLogger(ToggleAction.class.getName());
	private final E unselectedId;
	private final Map<Toggle, E> toggleIds;
	private final Map<E, ActionItemDescriptor> actionItemDescriptors;
	private final Map<E, BooleanProperty> disableProperties;
	private final ObjectProperty<E> selectedId;
	private boolean changing;

	/// Creates a new instance.
	///
	/// @param unselectedId		the ID returned, if no toggle is selected
	/// @param mapDescriptors a map of descriptors
	///
	public ToggleAction(E unselectedId, Map<E, ActionItemDescriptor> mapDescriptors)
	{
		final Class<E> enumClass = unselectedId.getDeclaringClass();
		this.unselectedId = unselectedId;
		this.actionItemDescriptors = new EnumMap<>(enumClass);
		final E[] enumConstants = enumClass.getEnumConstants();
		if (enumConstants != null)
		{
			for (E key : enumConstants)
			{
				final var descriptor = mapDescriptors.get(key);
				actionItemDescriptors.put(key, descriptor != null ? descriptor : ActionItemDescriptor.EMPTY);
			}
		}
		this.toggleIds = new HashMap<>();
		this.disableProperties = new EnumMap<>(enumClass);
		this.selectedId = new SimpleObjectProperty<>(unselectedId);
		selectedId.addListener(onChange(id ->
		{
			if (!changing)
			{
				changing = true;
				try
				{
					toggleIds.keySet().forEach(t -> t.setSelected(toggleIds.get(t) == id));
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
	/// @param id      the associated identifier
	/// @param toggles the toggles to add
	///
	/// @throws IllegalArgumentException if
	///
	///   * trying to add the same toggle twice or
	///   * trying to associate a toggle with the unselected ID
	///
	 void addToggles(E id, Toggle... toggles)
	{
		if (id == unselectedId)
		{
			throw new IllegalArgumentException(
				"Trying to associate toggles with the unselected ID: »%s« → »%s«".formatted(toggles, id));
		}
		for (var toggle : toggles)
		{
			if (toggleIds.put(toggle, id) != null)
			{
				throw new IllegalArgumentException(
					"Same toggle added twice: »%s« → »%s«".formatted(toggle, id));
			}
			toggle.setSelected(id == getSelectedId());
			toggle.selectedProperty().addListener(onChange(selected ->
			{
				if (!changing)
				{
					changing = true;
					try
					{
						toggleIds.keySet().stream()
							.filter(t -> t != toggle)
							.forEach(t -> t.setSelected(toggleIds.get(t) == id ? selected : false));
						selectedId.set(selected ? id : unselectedId);
					}
					finally
					{
						changing = false;
					}
				}
			}));
			final var actionItemDescriptor = actionItemDescriptors.get(id);
			if (actionItemDescriptor != null)
			{
				switch (toggle)
				{
					case MenuItem menuItem ->
					{
						menuItem.disableProperty().bind(getDisableProperty(id).or(disableProperty()));
						initActionItem(actionItemDescriptor, menuItem);
					}
					case ToggleButton button ->
					{
						button.disableProperty().bind(getDisableProperty(id).or(disableProperty()));
						initActionItem(actionItemDescriptor, button);
					}
					default ->
					{
						logger.log(TRACE, () -> "Unknown type of toggle: " + toggle);
					}
				}
			}
		}
	}

	@Override
	public List<RadioMenuItem> createMenuItems()
	{
		final E[] enumConstants = unselectedId.getDeclaringClass().getEnumConstants();
		if (enumConstants != null)
		{
			final RadioMenuItem[] result = new RadioMenuItem[enumConstants.length - 1];
			int i = 0;
			for (E key : enumConstants)
			{
				if (key != unselectedId)
				{
					final var radioMenuItem = new RadioMenuItem();
					result[i++] = radioMenuItem;
					addToggles(key, radioMenuItem);
				}
			}
			return List.of(result);
		}
		else
		{
			return List.of();
		}
	}

	@Override
	public List<ToggleButton> createToolBarButtons()
	{
		final E[] enumConstants = unselectedId.getDeclaringClass().getEnumConstants();
		if (enumConstants != null)
		{
			final ToggleButton[] result = new ToggleButton[enumConstants.length - 1];
			int i = 0;
			for (E key : enumConstants)
			{
				if (key != unselectedId)
				{
					final var toggleButton = new ToggleButton();
					result[i++] = toggleButton;
					addToggles(key, toggleButton);
				}
			}
			return List.of(result);
		}
		else
		{
			return List.of();
		}
	}

	public ObjectProperty<E> selectedIdProperty()
	{
		return selectedId;
	}

	public E getSelectedId()
	{
		return selectedIdProperty().get();
	}

	public void setSelectedId(E id)
	{
		selectedIdProperty().set(id);
	}

	public BooleanProperty getDisableProperty(E id)
	{
		return disableProperties.computeIfAbsent(id, _ -> new SimpleBooleanProperty());
	}
}
