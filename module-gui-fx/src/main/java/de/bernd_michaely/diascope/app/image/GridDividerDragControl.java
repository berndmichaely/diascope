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
import de.bernd_michaely.diascope.app.util.collections.InnerNode;
import de.bernd_michaely.diascope.app.util.collections.LeafNode;
import java.lang.System.Logger;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;

import static de.bernd_michaely.diascope.app.image.GridDivider.Orientation.*;
import static java.lang.Math.ceilDiv;
import static java.lang.Math.sqrt;
import static java.lang.System.Logger.Level.*;

/// Class to handle grid divider drag operations.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
class GridDividerDragControl
{
	private static final Logger logger = System.getLogger(GridDividerDragControl.class.getName());
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
		logger.log(TRACE, () -> "→ INITIALIZE GRID → n = %d".formatted(n));
		final int rows = (int) sqrt(n);
		final int cols = n > 0 ? ceilDiv(n, rows) : 0;
		int row = 0;
		int col = 0;
		for (var treeNode : gridTree)
		{
			if (treeNode instanceof LeafNode leafNode)
			{
				final var layer = (ImageLayer) leafNode.getValue();
				if (layer != null)
				{
					logger.log(TRACE, "→ INITIALIZE GRID [%d/%d]".formatted(row, col));
					final var viewportBoundsLocal = layer.getViewportBoundsLocal();
					final DoubleBinding w_n = width.divide(cols);
					final DoubleBinding h_n = height.divide(rows);
					viewportBoundsLocal.xProperty().bind(w_n.multiply(col));
					viewportBoundsLocal.yProperty().bind(h_n.multiply(row));
					viewportBoundsLocal.widthProperty().bind(w_n);
					viewportBoundsLocal.heightProperty().bind(h_n);
					// TODO
					final boolean newRow = ++col >= cols;
					final InnerNode parentNode = leafNode.getParentNode();
					if (parentNode != null)
					{
						if (parentNode.getValue() instanceof GridDivider gridDivider)
						{
							gridDivider.setOrientation(newRow ? HORIZONTAL : VERTICAL);
						}
					}
					if (newRow)
					{
						col = 0;
						row++;
					}
				}
			}
		}
	}
}
