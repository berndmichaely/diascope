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

import de.bernd_michaely.diascope.app.util.collections.BinaryTree;
import de.bernd_michaely.diascope.app.util.collections.LeafNode;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;

import static java.lang.Math.ceilDiv;
import static java.lang.Math.sqrt;

/// Class to handle grid divider drag operations.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class GridDividerDragControl
{
	private final BinaryTree<GridDivider, ImageLayer> gridTree;
	private final ReadOnlyDoubleProperty width;
	private final ReadOnlyDoubleProperty height;

	GridDividerDragControl(BinaryTree<GridDivider, ImageLayer> gridTree,
		ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height)
	{
		this.gridTree = gridTree;
		this.width = width;
		this.height = height;
	}

	void initializeDividerPositions()
	{
		final int n = gridTree.getNumLeafNodes();
		System.out.println("→ INITIALIZE GRID → n = %d".formatted(n));
		final int rows = (int) sqrt(n);
		final int cols = n > 0 ? ceilDiv(n, rows) : 0;
		int row = 0;
		int col = 0;
		for (var treeNode : gridTree)
		{
			if (treeNode instanceof LeafNode node)
			{
				final ImageLayer layer = (ImageLayer) node.getValue();
				if (layer != null)
				{
					System.out.println("→ INITIALIZE GRID [%d/%d]".formatted(row, col));
					final var viewportBoundsLocal = layer.getViewportBoundsLocal();
					final DoubleBinding w_n = width.divide(cols);
					final DoubleBinding h_n = height.divide(rows);
					viewportBoundsLocal.xProperty().bind(w_n.multiply(col));
					viewportBoundsLocal.yProperty().bind(h_n.multiply(row));
					viewportBoundsLocal.widthProperty().bind(w_n);
					viewportBoundsLocal.heightProperty().bind(h_n);
					col++;
					if (col >= cols)
					{
						col = 0;
						row++;
					}
				}
			}
		}
	}
}
