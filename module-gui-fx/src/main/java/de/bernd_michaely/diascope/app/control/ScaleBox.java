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
package de.bernd_michaely.diascope.app.control;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.diascope.app.control.ScaleBox.SpaceGainingMode.*;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

/**
 * Component to show an encapsulated component scaled down when resized below
 * min size.
 *
 * @author Bernd Michaely
 */
public class ScaleBox
{
	private final Region region;
	private final Region root;
	private final boolean upscaling;
	private double minWidth, minHeight;
	private double prefWidth, prefHeight;
	private boolean initialized;
	private final SpaceGainingMode spaceGainingMode;

	/**
	 * Describes the mode in gaining space when the encapsulated component is
	 * resized below its min size.
	 */
	public enum SpaceGainingMode
	{
		/**
		 * Missing space is gained by scaling down the encapsulated region (default)
		 */
		SCALING,
		/**
		 * Missing space is gained by scrolling the encapsulated region (that is the
		 * region is simply encapsulated in a {@link  ScrollPane})
		 */
		SCROLLING,
		/**
		 * Missing space is handled by the encapsulated region itself
		 */
		NONE;

		public static SpaceGainingMode getDefaultSpaceGainingMode()
		{
			return SCALING;
		}
	}

	/**
	 * Creates an instance encapsulating a region to scale down if resized below
	 * its min size. The regions minSize and prefSize must be set before. The
	 * {@link #initialize()} method is called by the constructor.
	 *
	 * @param region the Region to encapsulate
	 */
	public ScaleBox(Region region)
	{
		this(region, true, false);
	}

	/**
	 * Creates an instance encapsulating a region to scale.The regions minSize,
	 * prefSize and maxSize must be set before calling {@link #initialize()}.
	 *
	 * @param region                the Region to encapsulate
	 * @param initializeImmediately if true, {@link #initialize()} is called by
	 *                              the constructor, otherwise the user must call
	 *                              it once
	 * @param upsize                true to also scale up when growing over
	 *                              preferred size
	 */
	public ScaleBox(Region region, boolean initializeImmediately, boolean upsize)
	{
		this(region, initializeImmediately, upsize, null);
	}

	/**
	 * Creates an instance encapsulating a region.The regions minSize, prefSize
	 * and maxSize must be set before calling {@link #initialize()}.
	 *
	 * @param region                the Region to encapsulate
	 * @param initializeImmediately if true, {@link #initialize()} is called by
	 *                              the constructor, otherwise the user must call
	 *                              it once
	 * @param upscaling             true to also scale up when growing over
	 *                              preferred size
	 * @param spaceGainingMode      determines the {@link SpaceGainingMode} (null
	 *                              defaults to
	 *                              {@link SpaceGainingMode#SCALING SCALING})
	 */
	public ScaleBox(Region region, boolean initializeImmediately,
		boolean upscaling, @Nullable SpaceGainingMode spaceGainingMode)
	{
		this.region = requireNonNull(region, "Region is null");
		this.upscaling = upscaling;
		this.spaceGainingMode = (spaceGainingMode != null) ? spaceGainingMode : getDefaultSpaceGainingMode();
		this.root = SCALING.equals(this.spaceGainingMode) ? new Pane(region) : new ScrollPane(region);
		if (initializeImmediately)
		{
			initialize();
		}
	}

	/**
	 * Performs initializations based on properties of the encapsulated region.
	 * (Calling this method more than once has no effect.)
	 */
	public final void initialize(


		@UnknownInitialization(ScaleBox.class) ScaleBox this)
  {
    if (!initialized)
		{
			try
			{
				if (SCALING.equals(this.spaceGainingMode))
				{
					this.root.setMinSize(1, 1);
					final Orientation contentBias = region.getContentBias();
					// minimum size:
					final double minWidthChildren;
					final double minHeightChildren;
					if (contentBias == null)
					{
						minWidthChildren = region.minWidth(-1.0);
						minHeightChildren = region.minHeight(-1.0);
					}
					else
					{
						switch (contentBias)
						{
							case HORIZONTAL ->
							{
								minWidthChildren = region.minWidth(-1.0);
								minHeightChildren = region.minHeight(minWidthChildren);
							}
							case VERTICAL ->
							{
								minHeightChildren = region.minHeight(-1.0);
								minWidthChildren = region.minWidth(minHeightChildren);
							}
							default ->
								throw new AssertionError("Invalid content bias");
						}
					}
					this.minWidth = max(1.0, minWidthChildren);
					this.minHeight = max(1.0, minHeightChildren);
					// preferred size:
					final double prefWidthChildren;
					final double prefHeightChildren;
					if (contentBias == null)
					{
						prefWidthChildren = region.prefWidth(-1.0);
						prefHeightChildren = region.prefHeight(-1.0);
					}
					else
					{
						switch (contentBias)
						{
							case HORIZONTAL ->
							{
								prefWidthChildren = region.prefWidth(-1.0);
								prefHeightChildren = region.prefHeight(prefWidthChildren);
							}
							case VERTICAL ->
							{
								prefHeightChildren = region.prefHeight(-1.0);
								prefWidthChildren = region.prefWidth(prefHeightChildren);
							}
							default ->
								throw new AssertionError("Invalid content bias");
						}
					}
					this.prefWidth = max(1.0, prefWidthChildren);
					this.prefHeight = max(1.0, prefHeightChildren);
					// change listener:
					this.root.widthProperty().addListener((observable, oldValue, newValue) ->
					{
						final double width = newValue.doubleValue();
						if (width < minWidth)
						{
							final double factor = width / minWidth;
							region.setLayoutX((width - minWidth) / 2);
							region.setPrefWidth(minWidth);
							region.setScaleX(factor);
						}
						else if (upscaling && (width > prefWidth))
						{
							final double factor = width / prefWidth;
							region.setLayoutX((width - prefWidth) / 2);
							region.setPrefWidth(prefWidth);
							region.setScaleX(factor);
						}
						else
						{
							region.setLayoutX(0);
							region.setPrefWidth(width);
							region.setScaleX(1);
						}
					});
					this.root.heightProperty().addListener((observable, oldValue, newValue) ->
					{
						final double height = newValue.doubleValue();
						if (height < minHeight)
						{
							final double factor = height / minHeight;
							region.setLayoutY((height - minHeight) / 2);
							region.setPrefHeight(minHeight);
							region.setScaleY(factor);
						}
						else if (upscaling && (height > prefHeight))
						{
							final double factor = height / prefHeight;
							region.setLayoutY((height - prefHeight) / 2);
							region.setPrefHeight(prefHeight);
							region.setScaleY(factor);
						}
						else
						{
							region.setLayoutY(0);
							region.setPrefHeight(height);
							region.setScaleY(1);
						}
					});
				}
			}
			finally
			{
				initialized = true;
			}
		}
	}

	public Region getDisplay()
	{
		return this.root;
	}
}
