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
package de.bernd_michaely.diascope.app.application;

import de.bernd_michaely.diascope.app.stage.MainWindow;
import de.bernd_michaely.diascope.app.stage.PaneFileSystem;
import java.util.concurrent.Future;
import javafx.application.Application;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.concurrent.ForkJoinPool.commonPool;

/// Main application object of the Diascope application.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class DiascopeApplication extends Application
{
	private final boolean optimizeMainWindowInit = true;
	private @Nullable Future<MainWindow> futureMainWindow;
	private @Nullable Future<PaneFileSystem> futurePaneFileSystem;

	@Override
	public void init() throws Exception
	{
		super.init();
		if (optimizeMainWindowInit)
		{
			futureMainWindow = commonPool().submit(MainWindow::new);
			futurePaneFileSystem = commonPool().submit(PaneFileSystem::new);
		}
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		final MainWindow mainWindow;
		final PaneFileSystem paneFileSystem;
		if (futureMainWindow != null)
		{
			mainWindow = futureMainWindow.get();
			futureMainWindow = null;
		}
		else
		{
			mainWindow = new MainWindow();
		}
		if (futurePaneFileSystem != null)
		{
			paneFileSystem = futurePaneFileSystem.get();
			futurePaneFileSystem = null;
		}
		else
		{
			paneFileSystem = new PaneFileSystem();
		}
		mainWindow._start(stage);
		mainWindow.setFileSystemView(paneFileSystem);
	}
}
