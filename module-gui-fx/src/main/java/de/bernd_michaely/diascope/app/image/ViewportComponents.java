/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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

import de.bernd_michaely.diascope.app.image.MultiImageView.Mode;
import java.util.Collection;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.unmodifiableObservableList;

/// Class to hold components like selection shapes, dividers related to
/// image layers and the viewport.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class ViewportComponents
{
	final ObservableList<Node> imageLayers = observableArrayList();
	final ObservableList<Node> spotLayers = observableArrayList();
	final ObservableList<Node> gridShapeLines = observableArrayList();
	final ObservableList<Node> splitShapeLines = observableArrayList();
	final ObservableList<Node> gridShapes = observableArrayList();
	final ObservableList<Node> gridEventLines = observableArrayList();
	final ObservableList<Node> splitShapes = observableArrayList();
	final ObservableList<Node> splitEventLines = observableArrayList();
	final ObservableList<Node> spotShapes = observableArrayList();
	private final ObservableList<ObservableList<Node>> lists;
	private final ObservableList<Node> scrollBarNodes;
	private final ObservableList<Node> splitCenterNodes;
	private final Map<ImageLayer, Map<Node, ObservableList<Node>>> mapImageLayerNodes;
	private final EnumMap<Mode, Collection<ObservableList<Node>>> mapListsByMode;

	ViewportComponents(ObservableList<ObservableList<Node>> lists,
		Collection<ScrollBar> scrollBars, Collection<Node> splitCenterShapes)
	{
		this.lists = lists;
		this.scrollBarNodes = unmodifiableObservableList(observableArrayList(scrollBars));
		this.splitCenterNodes = unmodifiableObservableList(observableArrayList(splitCenterShapes));
		this.mapImageLayerNodes = new IdentityHashMap<>();
		this.mapListsByMode = new EnumMap<>(Mode.class);
	}

	void setListsByMode(Mode mode)
	{
		lists.setAll(mapListsByMode.computeIfAbsent(mode, m -> switch (m)
		{
			case SINGLE ->
				List.of(imageLayers, scrollBarNodes);
			case GRID ->
				List.of(imageLayers, gridShapeLines, gridShapes, gridEventLines);
			case SPLIT ->
				List.of(
				imageLayers, splitShapeLines, splitShapes, splitEventLines,
				splitCenterNodes, scrollBarNodes);
			case SPOT ->
				List.of(spotLayers, spotShapes, scrollBarNodes);
		}));
	}

	/// Add the given ImageLayer and all its related components.
	/// Memorize all added nodes per ImageLayer for a later remove.
	///
	void addGridSplitLayer(int index, ImageLayer imageLayer,
		Map<Node, ObservableList<Node>> mapComponents)
	{
		final Map<Node, ObservableList<Node>> map = mapImageLayerNodes
			.computeIfAbsent(imageLayer, _ -> new IdentityHashMap<>());
		mapComponents.forEach((node, list) ->
		{
			list.add(index, node);
			map.put(node, list);
		});
	}

	/// Remove the given ImageLayer and all its related components.
	///
	void removeGridSplitLayer(ImageLayer imageLayer)
	{
		final var map = mapImageLayerNodes.remove(imageLayer);
		if (map != null)
		{
			map.forEach((node, list) -> list.remove(node));
		}
		else
		{
			throw new IllegalStateException(getClass().getName() +
				"::removeGridSplitLayer : Trying to remove an invalid ImageLayer");
		}
	}
}
