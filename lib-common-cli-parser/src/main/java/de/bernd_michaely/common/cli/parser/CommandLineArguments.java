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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.Nullable;

import static de.bernd_michaely.common.cli.parser.AnsiColorEscapeCodesUtil.*;
import static java.util.Objects.requireNonNull;

/**
 * Simple reimplementation of a GNU getopt style command line argument parsing.
 * The command line arguments must be given as follows:
 * <ul>
 * <li>Each command line option has a mandatory long form, prefixed with '--',
 * and optionally an additional short form, prefixed with '-'.</li>
 * <li>Each command line option can optionally have a parameter, which can be
 * optional or mandatory.</li>
 * <li>Each command line option parameter can optionally be described by a
 * regular expression it has to match.</li>
 * <li>For options with optional parameters, the parameter must immediately
 * follow the option, e.g.
 * <pre>'<code>-o param</code>' or '<code>--option=param</code>'.</pre></li>
 * <li>Non-option arguments are first assigned to command line options still
 * missing a required parameter in the order of their appearance. E.g. if a, b,
 * c are options with required parameters,
 * <pre><code>-a -b -c one two three four</code></pre> and
 * <pre><code>-abc one two three four</code></pre> and
 * <pre><code>-a one -b two -c three four</code></pre> have the same meaning,
 * where 'four' remains as a non-option argument, returned by the
 * {@link #parse(String...) } method.
 * </li>
 * <li>The command line argument '-' ends the parsing of options so that all
 * following command line arguments are considered to be non-option arguments.
 * This allows non-option arguments to start with '-' characters without being
 * confused with options.</li>
 * </ul>
 * Command line options are defined together with a description. There are
 * methods to generate a pretty printed overview of the defined options.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class CommandLineArguments
{
	static final String FORMATTED_DESCRIPTION_HEADER = "OPTIONS:";
	private final BiConsumer<String, @Nullable String> callback;
	private final SortedMap<String, OptionDescriptor> mapLongOptions = new TreeMap<>();
	private final SortedMap<Character, OptionDescriptor> mapShortOptions = new TreeMap<>();
	private final List<OptionInstance> optionInstances = new ArrayList<>();

	/**
	 * Constructs a command line argument parser.
	 *
	 * @param callback a {@link BiConsumer} to call for each command line
	 *                 argument, in the order of appearance of the options. The
	 *                 first string passes the long form of the option, which is
	 *                 never null. The second string passes the parameter given
	 *                 with the option, which may be null, if an optional
	 *                 parameter is not given or the argument is a flag option.
	 */
	public CommandLineArguments(BiConsumer<String, @Nullable String> callback)
	{
		this.callback = requireNonNull(callback, "callback required");
	}

	/**
	 * Adds a command line option which does not expect a parameter.
	 *
	 * @param longOption  the long form of the option
	 * @param description a multi line description for the option
	 * @return this CommandLineArguments
	 * @throws OptionDefinitionException if the same option is defined twice
	 */
	public CommandLineArguments addFlagOption(String longOption, String... description)
		throws OptionDefinitionException
	{
		addFlagOption(longOption, (char) 0, description);
		return this;
	}

	/**
	 * Adds a command line option which does not expect a parameter.
	 *
	 * @param longOption  the long form of the option
	 * @param shortOption the short form of the option
	 * @param description a multi line description for the option
	 * @return this CommandLineArguments
	 * @throws OptionDefinitionException for duplicate option definitions
	 */
	public CommandLineArguments addFlagOption(String longOption, char shortOption,
		String... description) throws OptionDefinitionException
	{
		final OptionDescriptor optionDescriptor = new OptionDescriptor(
			longOption, shortOption, description);
		addOptionDescriptor(optionDescriptor);
		return this;
	}

	/**
	 * Adds a command line option which expects a parameter.
	 *
	 * @param longOption    the long form of the option
	 * @param paramRequired true, if the parameter is mandatory, false, if the
	 *                      parameter is optional
	 * @param description   a multi line description for the option
	 * @return this CommandLineArguments
	 * @throws OptionDefinitionException for duplicate option definitions
	 */
	public CommandLineArguments addParameterOption(String longOption, boolean paramRequired,
		String... description) throws OptionDefinitionException
	{
		addParameterOption(longOption, (char) 0, null, paramRequired, description);
		return this;
	}

	/**
	 * Adds a command line option which expects a parameter.
	 *
	 * @param longOption    the long form of the option
	 * @param shortOption   the short form of the option
	 * @param paramRequired true, if the parameter is mandatory, false, if the
	 *                      parameter is optional
	 * @param description   a multi line description for the option
	 * @return this CommandLineArguments
	 * @throws OptionDefinitionException for duplicate option definitions
	 */
	public CommandLineArguments addParameterOption(String longOption, char shortOption,
		boolean paramRequired, String... description) throws OptionDefinitionException
	{
		addParameterOption(longOption, shortOption, null, paramRequired, description);
		return this;
	}

	/**
	 * Adds a command line option which expects a parameter.
	 *
	 * @param longOption    the long form of the option
	 * @param paramRegExpr  a regular expression the parameter must match
	 * @param paramRequired true, if the parameter is mandatory, false, if the
	 *                      parameter is optional
	 * @param description   a multi line description for the option
	 * @return this CommandLineArguments
	 * @throws OptionDefinitionException for duplicate option definitions
	 */
	public CommandLineArguments addParameterOption(String longOption, String paramRegExpr,
		boolean paramRequired, String... description) throws OptionDefinitionException
	{
		addParameterOption(longOption, (char) 0, paramRegExpr, paramRequired, description);
		return this;
	}

	/**
	 * Adds a command line option which expects a parameter.
	 *
	 * @param longOption    the long form of the option
	 * @param shortOption   the short form of the option
	 * @param paramRegExpr  a regular expression the parameter must match
	 * @param paramRequired true, if the parameter is mandatory, false, if the
	 *                      parameter is optional
	 * @param description   a multi line description for the option
	 * @return this CommandLineArguments
	 * @throws OptionDefinitionException for duplicate option definitions
	 */
	public CommandLineArguments addParameterOption(String longOption, char shortOption,
		@Nullable String paramRegExpr, boolean paramRequired, String... description)
		throws OptionDefinitionException
	{
		final OptionDescriptor optionDescriptor = new OptionDescriptor(
			longOption, shortOption, paramRegExpr, paramRequired, description);
		addOptionDescriptor(optionDescriptor);
		return this;
	}

	private void addOptionDescriptor(OptionDescriptor optionDescriptor)
		throws OptionDefinitionException
	{
		final String longOption = optionDescriptor.getLongOption();
		final char shortOption = optionDescriptor.getShortOption();
		if (this.mapLongOptions.containsKey(longOption))
		{
			throw new OptionDefinitionException(
				"Duplicate option definition for option : --" + longOption);
		}
		this.mapLongOptions.put(longOption, optionDescriptor);
		if (shortOption > 0)
		{
			if (this.mapShortOptions.containsKey(shortOption))
			{
				throw new OptionDefinitionException(
					"Duplicate option definition for option : -" + shortOption);
			}
			this.mapShortOptions.put(shortOption, optionDescriptor);
		}
	}

	/**
	 * Parse command line arguments.
	 *
	 * @param args command line arguments passed by the main method
	 * @return an unmodifiable list of parsed command line non option arguments
	 * @throws InvalidCommandLineParametersException if invalid command line
	 *                                               options or parameters are
	 *                                               detected
	 */
	public List<String> parse(String... args) throws
		InvalidCommandLineParametersException
	{
		this.optionInstances.clear();
		final LinkedList<String> nonOptionParameters = new LinkedList<>();
		if (args != null)
		{
			final Queue<OptionInstance> queueRequiredParams = new LinkedList<>();
			OptionInstance optionParamOptional = null;
			boolean endOfOptionArgs = false;
			for (String arg : args)
			{
				if ((arg != null) && !arg.isEmpty())
				{
					final boolean isLongOption = arg.startsWith("--");
					final boolean isShortOption = !isLongOption && arg.startsWith("-");
					if (endOfOptionArgs)
					{
						nonOptionParameters.add(arg);
					}
					else
					{
						if (arg.equals("-"))
						{
							endOfOptionArgs = true;
							optionParamOptional = null;
						}
						else if (isLongOption)
						{
							optionParamOptional = null;
							final int indexParam = arg.indexOf('=', 2);
							final boolean hasParam = indexParam >= 2;
							final String name = hasParam ? arg.substring(2, indexParam) : arg.substring(2);
							final String param = hasParam ? arg.substring(indexParam + 1) : null;
							final OptionDescriptor optionDescriptor = this.mapLongOptions.get(name);
							if (optionDescriptor == null)
							{
								throw new InvalidCommandLineParametersException("Invalid option : " + name);
							}
							if (optionDescriptor.isFlagOption() && hasParam)
							{
								throw new InvalidCommandLineParametersException(
									"Option has no parameter : " + name);
							}
							if (optionDescriptor.isParameterRequired() && !hasParam)
							{
								throw new InvalidCommandLineParametersException(
									"Option requires parameter : " + name);
							}
							final OptionInstance optionInstance = new OptionInstance(optionDescriptor);
							if (param != null)
							{
								optionInstance.setParameter(param);
							}
							this.optionInstances.add(optionInstance);
						}
						else if (isShortOption)
						{
							for (char option : arg.substring(1).toCharArray())
							{
								optionParamOptional = null;
								final OptionDescriptor optionDescriptor = this.mapShortOptions.get(option);
								if (optionDescriptor == null)
								{
									throw new InvalidCommandLineParametersException(
										"Invalid option : " + option);
								}
								final OptionInstance optionInstance = new OptionInstance(optionDescriptor);
								this.optionInstances.add(optionInstance);
								if (optionDescriptor.isParameterRequired())
								{
									queueRequiredParams.add(optionInstance);
								}
								else if (optionDescriptor.isParameterOptional())
								{
									optionParamOptional = optionInstance;
								}
							}
						}
						else // non option argument
						{
							if (optionParamOptional != null)
							{
								optionParamOptional.setParameter(arg);
								optionParamOptional = null;
							}
							else if (!queueRequiredParams.isEmpty())
							{
								final OptionInstance optionInstance = queueRequiredParams.poll();
								if (optionInstance != null)
								{
									optionInstance.setParameter(arg);
								}
							}
							else
							{
								nonOptionParameters.add(arg);
							}
						}
					}
				}
			}
			if (!queueRequiredParams.isEmpty())
			{
				throw new InvalidCommandLineParametersException(
					"Missing required option parameters!");
			}
			for (OptionInstance oi : this.optionInstances)
			{
				if (!oi.checkParamRegExpr())
				{
					throw new InvalidCommandLineParametersException(
						"Invalid Parameter for option : --" +
						oi.getOptionDescriptor().getLongOption() + "=" + oi.getParameter());
				}
			}
			this.optionInstances.forEach(oi -> callback.accept(
				oi.getOptionDescriptor().getLongOption(), oi.getParameter()));
		}
		return Collections.unmodifiableList(nonOptionParameters);
	}

	/**
	 * Returns a formatted description of the defined options. The list is sorted
	 * by long options.
	 *
	 * @param withHeader true to output a header
	 * @return a formatted description of the defined option
	 */
	public List<String> getFormattedDescription(boolean withHeader)
	{
		final List<String> result = new ArrayList<>();
		if (withHeader)
		{
			result.add(formatAsHeader(FORMATTED_DESCRIPTION_HEADER));
			result.add("");
		}
		final int width = this.mapLongOptions.keySet().stream()
			.mapToInt(String::length)
			.max().orElse(0);
		this.mapLongOptions.forEach((name, od) ->
		{
			final String optShort = od.isProvidingShortOption() ?
				"-" + od.getShortOption() : "";
			result.add(formatAsOption(String.format("%2s  --%-" + width + "s  ",
				optShort, name)) + od.getDescription()[0]);
			final int w2 = width + 8;
			for (int i = 1; i < od.getDescription().length; i++)
			{
				result.add(String.format("%" + w2 + "s%s", "", od.getDescription()[i]));
			}
		});
		return result;
	}

	/**
	 * Prints a formatted description of the defined options.
	 *
	 * @param printStream the stream to print to
	 * @param withHeader  true to output a header
	 */
	public void printFormattedDescription(PrintStream printStream, boolean withHeader)
	{
		getFormattedDescription(withHeader).forEach(printStream::println);
	}
}
