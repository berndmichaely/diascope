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
package de.bernd_michaely.diascope.app.stage;

import java.lang.System.Logger;
import java.util.function.DoubleConsumer;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.System.Logger.Level.*;

/**
 * Class to control the progress of the image directory loading process.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class ProgressControl implements DoubleConsumer
{
	private static final Logger logger = System.getLogger(ProgressControl.class.getName());
	static final double PROGRESS_BEGIN = -2.0;
	static final double PROGRESS_END = -3.0;
	private final BorderPane statusLine;
	private @Nullable ProgressBar progressBar;
	private @Nullable Runnable onProgressZero;

	/**
	 * Creates a new instance. The created ProgressBar is placed in the left of
	 * statusLine.
	 *
	 * @param statusLine     the container in which to place the ProgressBar
	 * @param onProgressZero to be run when starting with 0%
	 */
	ProgressControl(BorderPane statusLine)
	{
		this.statusLine = statusLine;
	}

	void setOnProgressZero(Runnable onProgressZero)
	{
		this.onProgressZero = onProgressZero;
	}

	void runOnProgressZero()
	{
		if (onProgressZero != null)
		{
			onProgressZero.run();
		}
	}

	@Override
	public void accept(double progress)
	{
		if (progress >= 0.0 && progress <= 1.0)
		{
			if (progressBar == null)
			{
				progressBar = new ProgressBar(progress);
				statusLine.setLeft(progressBar);
			}
			else
			{
				progressBar.setProgress(progress);
			}
		}
		else if (progress == PROGRESS_BEGIN)
		{
			progressBar = new ProgressBar();
			statusLine.setLeft(progressBar);
		}
		else if (progress == PROGRESS_END)
		{
			final ObservableList<Node> children = statusLine.getChildren();
			if (progressBar != null)
			{
				children.remove(progressBar);
			}
			progressBar = null;
		}
		else
		{
			logger.log(WARNING, "Invalid progress value: " + progress);
		}
	}
}
