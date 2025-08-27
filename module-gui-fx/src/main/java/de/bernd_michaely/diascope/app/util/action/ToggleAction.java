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

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;

/// Toggle Action to handle selected and disabled properties of toggles.
///
/// @param <E> the toggle key type
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ToggleAction<E extends Enum<E>> extends Action
{
	private final E unselectedId;
	private final Map<Toggle, E> toggleIds;
	private final ObjectProperty<E> selectedId;
	private boolean changing;

	/// Creates a new instance.
	///
	/// @param unselectedId the ID returned, if no toggle is selected
	///
	public ToggleAction(E unselectedId)
	{
		this.unselectedId = unselectedId;
		this.toggleIds = new HashMap<>();
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
	public void addToggles(E id, Toggle... toggles)
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
			if (toggle instanceof RadioButton item)
			{
				item.disableProperty().bind(this.disabledProperty());
			}
			else if (toggle instanceof RadioMenuItem item)
			{
				item.disableProperty().bind(this.disabledProperty());
			}
			else if (toggle instanceof ToggleButton item)
			{
				item.disableProperty().bind(this.disabledProperty());
			}
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
}
