/*
 * Copyright (C) 2024 Bernd Michaely (info@bernd-michaely.de)
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

/**
 * Main GUI module of the Diascope application.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
module de.bernd_michaely.diascope.gui.fx
{
	requires de.bernd_michaely.common.filesystem.view.base;
	requires de.bernd_michaely.common.filesystem.view.fx;
	requires de.bernd_michaely.common.semver;
	requires de.bernd_michaely.common.selectable.list.fx;
	requires java.prefs;
	requires javafx.controls;
	requires javafx.graphics;
	requires org.checkerframework.checker.qual;

	exports de.bernd_michaely.diascope.app;
	opens de.bernd_michaely.diascope.app.application to javafx.graphics;
}
