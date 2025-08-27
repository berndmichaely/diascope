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

import de.bernd_michaely.diascope.app.icons.Icons;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Type to handle the parameters of a single action item.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public record ActionItemDescriptor(
	@Nullable Icons icon,
	@Nullable String buttonTitle,
	@Nullable String menuTitle,
	@Nullable String tooltipText)
{
	public static final ActionItemDescriptor EMPTY =
		new ActionItemDescriptor(null, null, null, null);
}
