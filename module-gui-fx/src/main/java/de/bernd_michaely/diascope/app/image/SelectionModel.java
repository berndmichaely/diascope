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

import java.lang.System.Logger;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.util.beans.ChangeListenerUtil.onChange;
import static java.lang.System.Logger.Level.*;

/**
 * SelectionModel for MultiImageView.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class SelectionModel
{
	private static final Logger logger = System.getLogger(SelectionModel.class.getName());
	private final ImageLayers imageLayers;
	private final ReadOnlyIntegerWrapper numSelectedLayers;
	private final ReadOnlyBooleanWrapper singleLayerSelected;
	private final ReadOnlyObjectWrapper<@Nullable ImageLayer> singleSelectedLayer;

	SelectionModel(ImageLayers imageLayers)
	{
		this.imageLayers = imageLayers;
		this.numSelectedLayers = new ReadOnlyIntegerWrapper();
		this.singleSelectedLayer = new ReadOnlyObjectWrapper<>();
		this.singleLayerSelected = new ReadOnlyBooleanWrapper();
		singleLayerSelected.bind(numSelectedLayers.isEqualTo(1));
		singleSelectedLayer.addListener(onChange(newValue ->
			logger.log(TRACE, () -> "LAYER SELECTED : »" + newValue + "«")));
	}

	ReadOnlyBooleanProperty singleLayerSelectedProperty()
	{
		return singleLayerSelected.getReadOnlyProperty();
	}

	boolean isSingleLayerSelected()
	{
		return singleLayerSelectedProperty().get();
	}

	ReadOnlyObjectProperty<@Nullable ImageLayer> singleSelectedLayerProperty()
	{
		return singleSelectedLayer.getReadOnlyProperty();
	}

	@Nullable
	ImageLayer getSingleSelectedLayer()
	{
		return singleSelectedLayerProperty().get();
	}

	private ObservableList<ImageLayer> getLayers()
	{
		return imageLayers.getLayers();
	}

	void toggleLayerSelection(ImageLayer layer, boolean multiSelect)
	{
//		if (getLayers().contains(layer))
		{
			layer.setSelected(!layer.isSelected());
			int count = 0;
			ImageLayer selected = null;
			for (ImageLayer l : getLayers())
			{
				if (!multiSelect && l != layer)
				{
					l.setSelected(false);
				}
				if (l.isSelected())
				{
					count++;
					selected = l;
				}
			}
			numSelectedLayers.set(count);
			singleSelectedLayer.set(count == 1 ? selected : null);
		}
	}
}
