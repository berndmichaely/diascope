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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/// A stub implementation of the Toggle interface.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ToggleStub implements Toggle
{
	private final String label;
	private final ObservableMap<Object, Object> properties;
	private final ObjectProperty<ToggleGroup> toggleGroupProperty;
	private final BooleanProperty selectedProperty;
	private Object userData;

	ToggleStub(String label)
	{
		this.label = label;
		this.properties = FXCollections.observableHashMap();
		this.toggleGroupProperty = new SimpleObjectProperty<>();
		this.selectedProperty = new SimpleBooleanProperty();
	}

	@Override
	public ObservableMap<Object, Object> getProperties()
	{
		return properties;
	}

	@Override
	public ToggleGroup getToggleGroup()
	{
		return toggleGroupProperty.get();
	}

	@Override
	public void setToggleGroup(ToggleGroup toggleGroup)
	{
		toggleGroupProperty.set(toggleGroup);
	}

	@Override
	public Object getUserData()
	{
		return userData;
	}

	@Override
	public void setUserData(Object userData)
	{
		this.userData = userData;
	}

	@Override
	public boolean isSelected()
	{
		return selectedProperty().get();
	}

	@Override
	public void setSelected(boolean selected)
	{
		selectedProperty().set(selected);
	}

	@Override
	public BooleanProperty selectedProperty()
	{
		return selectedProperty;
	}

	@Override
	public ObjectProperty<ToggleGroup> toggleGroupProperty()
	{
		return toggleGroupProperty;
	}

	@Override
	public String toString()
	{
		return "%s[»%s« %s]".formatted(getClass().getSimpleName(), label, isSelected() ? "+" : "-");
	}
}
