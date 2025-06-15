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
package de.bernd_michaely.diascope.app.image;

import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableList;
import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableListFactory;
import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableProperties;
import java.util.Optional;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/// Class describing the image layer selection.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class LayerSelectionModel implements SelectableProperties
{
	private final SelectableProperties selectionModel;
	private final ReadOnlyObjectWrapper<Optional<ImageLayer>> singleSelectedLayer;
	private final ReadOnlyBooleanWrapper singleLayerSelected;

	LayerSelectionModel(SelectableList<ImageLayer> layers)
	{
		this.selectionModel = SelectableListFactory.listSelectionHandler(layers);
		this.singleSelectedLayer = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.singleLayerSelected = new ReadOnlyBooleanWrapper();
		layers.addSelectionListener(change ->
		{
			if (!change.isEmptyRange())
			{
				for (int i = change.getFrom(); i <= change.getTo(); i++)
				{
					if (i >= 0 && i < layers.size())
					{
						layers.get(i).selectedProperty().set(layers.isSelected(i));
					}
				}
				final boolean isSingleSelected = selectionModel.getNumSelected() == 1;
				singleLayerSelected.set(isSingleSelected);
				singleSelectedLayer.set(isSingleSelected ?
					layers.stream().filter(ImageLayer::isSelected).findAny() : Optional.empty());
			}
		});
	}

	@Override
	public ReadOnlyBooleanProperty allSelectedProperty()
	{
		return selectionModel.allSelectedProperty();
	}

	@Override
	public ReadOnlyBooleanProperty emptyProperty()
	{
		return selectionModel.emptyProperty();
	}

	@Override
	public boolean isSelected(int index)
	{
		return selectionModel.isSelected(index);
	}

	@Override
	public ReadOnlyBooleanProperty noneSelectedProperty()
	{
		return selectionModel.noneSelectedProperty();
	}

	@Override
	public ReadOnlyIntegerProperty numSelectedProperty()
	{
		return selectionModel.numSelectedProperty();
	}

	@Override
	public void selectRange(int from, int to, Action action)
	{
		selectionModel.selectRange(from, to, action);
	}

	@Override
	public ReadOnlyIntegerProperty sizeProperty()
	{
		return selectionModel.sizeProperty();
	}

	ReadOnlyObjectProperty<Optional<ImageLayer>> singleSelectedLayerProperty()
	{
		return singleSelectedLayer.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty singleLayerSelected()
	{
		return singleLayerSelected.getReadOnlyProperty();
	}
}
