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

import java.lang.System.Logger;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A class to create a hyperlink node. If launching an external browser is
 * supported on this platform, a hyperlink node will be created, otherwise a
 * simple label with the URI as text.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class HyperlinkNode
{
	private static final Logger logger = System.getLogger(HyperlinkNode.class.getName());

	/**
	 * Returns true, if launching a default browser to display a URI is supported
	 * on this platform.
	 *
	 * @return true, if the desktop browse action is supported
	 */
	public static boolean isBrowseSupported()
	{
		return false; // TODO
//		return Desktop.isDesktopSupported() ?
//			Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) : false;
	}

	/**
	 * Creates a HyperlinkNode for the given URI, using the URI host as title.
	 *
	 * @param uri the given URI
	 * @return a HyperlinkNode for the given URI
	 */
	public static Labeled createNode(String uri)
	{
		return createNode(uri, null);
	}

	/**
	 * Creates a HyperlinkNode for the given URI and title.
	 *
	 * @param uri   the given URI
	 * @param title the given title
	 * @return a HyperlinkNode for the given URI and title
	 */
	public static Labeled createNode(String uri, @Nullable String title)
	{
		try
		{
			return createNode(new URI(uri), title);
		}
		catch (URISyntaxException ex)
		{
			throw new IllegalArgumentException("Invalid URI : »" + uri + "«", ex);
		}
	}

	/**
	 * Creates a HyperlinkNode for the given URI, using the URI host as title.
	 *
	 * @param uri the given URI
	 * @return a HyperlinkNode for the given URI
	 * @throws NullPointerException     if URI is null
	 * @throws IllegalArgumentException if URI is invalid
	 */
	public static Labeled createNode(URI uri)
	{
		return createNode(uri, null);
	}

	/**
	 * Creates a HyperlinkNode for the given URI and title.
	 *
	 * @param uri   the given URI
	 * @param title the given title
	 * @return a HyperlinkNode for the given URI and title
	 * @throws NullPointerException     if URI is null
	 * @throws IllegalArgumentException if URI is invalid
	 */
	public static Labeled createNode(URI uri, @Nullable String title)
	{
		final String strTitle = (title != null) ? title : uri.getHost();
		final Labeled node;
		if (isBrowseSupported())
		{
			final Hyperlink hyperlink = new Hyperlink(strTitle);
			hyperlink.setTooltip(new Tooltip(uri.toString()));
			hyperlink.setOnAction(event ->
			{
//				try
//				{
//					Desktop.getDesktop().browse(uri);
//				}
//				catch (IOException ex)
//				{
//					logger.log(WARNING,
//						"Can't launch external browser for URI : " + uri, ex);
//				}
			});
			node = hyperlink;
		}
		else
		{
			final Label label = new Label(strTitle);
			label.setTooltip(new Tooltip(uri.toString()));
			node = label;
		}
		return node;
	}
}
