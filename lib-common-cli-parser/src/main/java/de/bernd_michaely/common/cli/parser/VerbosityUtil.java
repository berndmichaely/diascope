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
package de.bernd_michaely.common.cli.parser;

import java.io.PrintStream;

/**
 * Utility class for handling the verbosity options of command line
 * applications.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class VerbosityUtil
{
	private PrintStream defaultPrintStream = System.out;
	private int verbosityLevel;

	/**
	 * Constructor to initialize with no verbosity.
	 */
	public VerbosityUtil()
	{
	}

	/**
	 * Constructor to initialize with given verbosity level.
	 *
	 * @param verbosityLevel the given verbosity level
	 */
	public VerbosityUtil(int verbosityLevel)
	{
		this.verbosityLevel = verbosityLevel;
	}

	/**
	 * Returns the default PrintStream.
	 *
	 * @return the default PrintStream
	 */
	public PrintStream getDefaultPrintStream()
	{
		return this.defaultPrintStream;
	}

	/**
	 * Sets the default PrintStream. The default PrintStream in turn defaults to
	 * {@link System#out}.
	 *
	 * @param defaultPrintStream the default PrintStream
	 */
	public void setDefaultPrintStream(PrintStream defaultPrintStream)
	{
		this.defaultPrintStream = defaultPrintStream;
	}

	/**
	 * Returns the current verbosity level.
	 *
	 * @return the current verbosity level which is in the range of
	 *         [0..{@link Integer#MAX_VALUE}]
	 */
	public int getVerbosityLevel()
	{
		return this.verbosityLevel;
	}

	/**
	 * Sets the verbosity level the the given value. A negative value will be
	 * treated like zero.
	 *
	 * @param verbosityLevel the verbosity level to set
	 */
	public void setVerbosityLevel(int verbosityLevel)
	{
		this.verbosityLevel = Math.max(0, verbosityLevel);
	}

	/**
	 * Increases the verbosity level by one up to {@link Integer#MAX_VALUE}.
	 *
	 * @return the verbosity level after the change
	 */
	public int increaseVerbosityLevel()
	{
		if (getVerbosityLevel() < Integer.MAX_VALUE)
		{
			this.verbosityLevel++;
		}
		return getVerbosityLevel();
	}

	/**
	 * Decreases the verbosity level by one down to zero.
	 *
	 * @return the verbosity level after the change
	 */
	public int decreaseVerbosityLevel()
	{
		if (getVerbosityLevel() > 1)
		{
			this.verbosityLevel--;
		}
		return getVerbosityLevel();
	}

	/**
	 * Returns true, if the verbosity level is at least one.
	 *
	 * @return true, if the verbosity level is at least one
	 */
	public boolean isVerbose()
	{
		return this.verbosityLevel > 0;
	}

	/**
	 * Performs the given action, if the verbosity level is at least one.
	 *
	 * @param runnable the action to perform
	 */
	public void ifVerbose(Runnable runnable)
	{
		if (isVerbose())
		{
			runnable.run();
		}
	}

	/**
	 * Print msg (without newline) to stdout.
	 *
	 * @param msg the message to print
	 */
	public void ifVerbosePrint(String msg)
	{
		ifVerbosePrint(msg, getDefaultPrintStream());
	}

	/**
	 * Print msg (without newline) to PrintStream.
	 *
	 * @param msg         the message to print
	 * @param printStream the stream to print to
	 */
	public void ifVerbosePrint(String msg, PrintStream printStream)
	{
		ifVerbose(() -> printStream.print(msg));
	}

	/**
	 * Print msg with newline to stdout.
	 *
	 * @param msg the message to print
	 */
	public void ifVerbosePrintln(String msg)
	{
		ifVerbosePrintln(msg, getDefaultPrintStream());
	}

	/**
	 * Print msg with newline to PrintStream.
	 *
	 * @param msg         the message to print
	 * @param printStream the stream to print to
	 */
	public void ifVerbosePrintln(String msg, PrintStream printStream)
	{
		ifVerbose(() -> printStream.println(msg));
	}

	/**
	 * Format msg (without newline) to stdout.
	 *
	 * @param msg  the formatted message to print
	 * @param args the args to format
	 */
	public void ifVerboseFormat(String msg, Object... args)
	{
		ifVerboseFormat(getDefaultPrintStream(), msg, args);
	}

	/**
	 * Format msg (without newline) to PrintStream.
	 *
	 * @param printStream the stream to print to
	 * @param msg         the formatted message to print
	 * @param args        the args to format
	 */
	public void ifVerboseFormat(PrintStream printStream, String msg, Object... args)
	{
		ifVerbose(() -> printStream.format(msg, args));
	}

	/**
	 * Format msg with newline to stdout.
	 *
	 * @param msg  the formatted message to print
	 * @param args the args to format
	 */
	public void ifVerboseFormatln(String msg, Object... args)
	{
		ifVerboseFormatln(getDefaultPrintStream(), msg, args);
	}

	/**
	 * Format msg with newline to PrintStream.
	 *
	 * @param printStream the stream to print to
	 * @param msg         the formatted message to print
	 * @param args        the args to format
	 */
	public void ifVerboseFormatln(PrintStream printStream, String msg, Object... args)
	{
		ifVerbose(() ->
		{
			printStream.format(msg, args);
			printStream.println();
		});
	}

	/**
	 * Print single newline to stdout.
	 */
	public void ifVerbosePrintNewline()
	{
		ifVerbosePrintNewlines(1, getDefaultPrintStream());
	}

	/**
	 * Print single newline to PrintStream.
	 *
	 * @param printStream the stream to print to
	 */
	public void ifVerbosePrintNewline(PrintStream printStream)
	{
		ifVerbosePrintNewlines(1, printStream);
	}

	/**
	 * Print a number of newlines to stdout.
	 *
	 * @param num the number of newlines to print
	 */
	public void ifVerbosePrintNewlines(int num)
	{
		ifVerbosePrintNewlines(num, getDefaultPrintStream());
	}

	/**
	 * Print a number of newlines to PrintStream.
	 *
	 * @param num         the number of newlines to print
	 * @param printStream the stream to print to
	 */
	public void ifVerbosePrintNewlines(int num, PrintStream printStream)
	{
		ifVerbose(() ->
		{
			for (int i = 0; i < num; i++)
			{
				printStream.println();
			}
		});
	}
}
