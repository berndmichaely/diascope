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

import de.bernd_michaely.common.desktop.fx.collections.selection.Selectable.Action;
import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableList;
import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableListFactory;
import de.bernd_michaely.common.desktop.fx.collections.selection.SelectableProperties;
import java.lang.System.Logger;
import java.util.Optional;
import java.util.function.IntFunction;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.common.desktop.fx.collections.selection.SelectionChangeListener.SelectionChange.SelectionChangeType.*;
import static java.lang.System.Logger.Level.*;

/// Class describing the image layer selection.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class LayerSelectionModel implements SelectableProperties
{
	private static final Logger logger = System.getLogger(LayerSelectionModel.class.getName());
	private final SelectableProperties selectionModel;
	private final ReadOnlyObjectWrapper<Optional<ImageLayer>> singleSelectedLayer;
	private final ReadOnlyObjectWrapper<Optional<ImageLayer>> dualSelectedLayerFirst;
	private final ReadOnlyObjectWrapper<Optional<ImageLayer>> dualSelectedLayerSecond;
	private final ReadOnlyBooleanWrapper singleLayerSelected;
	private final ReadOnlyBooleanWrapper dualLayerSelected;

	LayerSelectionModel(SelectableList<ImageLayer> layers)
	{
		this.selectionModel = SelectableListFactory.listSelectionHandler(layers);
		this.singleSelectedLayer = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.dualSelectedLayerFirst = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.dualSelectedLayerSecond = new ReadOnlyObjectWrapper<>(Optional.empty());
		this.singleLayerSelected = new ReadOnlyBooleanWrapper();
		this.dualLayerSelected = new ReadOnlyBooleanWrapper();
		layers.addSelectionListener(change ->
		{
			logger.log(TRACE, () -> "· List Change: " + change);
			final int from = change.getFrom();
			final int to = change.getTo();
			final boolean wasSingleSelected = singleLayerSelected.get();
			final Optional<ImageLayer> optionalOldSingleSelectedLayer = singleSelectedLayer.get();
			for (int i = from; i <= to; i++)
			{
				layers.get(i).setSelected(layers.isSelected(i));
			}
			final int n = layers.size();
			final int numSelected = selectionModel.getNumSelected();
			final boolean isSingleSelected = numSelected == 1;
			singleLayerSelected.set(isSingleSelected);
			singleSelectedLayer.set(isSingleSelected ?
				layers.stream().filter(ImageLayer::isSelected).findAny() : Optional.empty());
			final boolean isDualSelected = wasSingleSelected && change.getSelectionChangeType() == SINGLE_INCREMENT;
			final boolean isDualLayer = n == 2;
			final boolean isDualMode = isDualSelected || isDualLayer;
			dualLayerSelected.set(isDualMode);
			if (isDualMode)
			{
				if (isDualSelected)
				{
					dualSelectedLayerFirst.set(optionalOldSingleSelectedLayer);
					final var firstLayer = optionalOldSingleSelectedLayer.get();
					dualSelectedLayerSecond.set(layers.stream()
						.filter(ImageLayer::isSelected)
						.filter(layer -> layer != firstLayer)
						.findAny());
				}
				else // n == 2
				{
					if (isSingleSelected)
					{
						dualSelectedLayerSecond.set(singleSelectedLayer.get());
						final var firstLayer = singleSelectedLayer.get().get();
						dualSelectedLayerFirst.set(layers.stream()
							.filter(layer -> layer != firstLayer)
							.findAny());
					}
					else
					{
						dualSelectedLayerFirst.set(Optional.of(layers.getFirst()));
						dualSelectedLayerSecond.set(Optional.of(layers.getLast()));
					}
				}
			}
			else
			{
				dualSelectedLayerFirst.set(Optional.empty());
				dualSelectedLayerSecond.set(Optional.empty());
			}
			logger.log(TRACE, () ->
				"→ numSelected == %d – isDualSelected? %b – isFirstPresent? %b – isSecondPresent? %b"
					.formatted(numSelected, isDualSelected,
						dualSelectedLayerFirst.get().isPresent(), dualSelectedLayerSecond.get().isPresent()));
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
	public void selectRange(int from, int to, @Nullable IntFunction<@Nullable Action> action)
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

	ReadOnlyObjectProperty<Optional<ImageLayer>> dualSelectedLayerFirstProperty()
	{
		return dualSelectedLayerFirst.getReadOnlyProperty();
	}

	ReadOnlyObjectProperty<Optional<ImageLayer>> dualSelectedLayerSecondProperty()
	{
		return dualSelectedLayerSecond.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty singleLayerSelected()
	{
		return singleLayerSelected.getReadOnlyProperty();
	}

	ReadOnlyBooleanProperty dualLayerSelected()
	{
		return dualLayerSelected.getReadOnlyProperty();
	}
}
