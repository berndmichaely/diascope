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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;

import static de.bernd_michaely.common.cli.parser.AnsiColorEscapeCodesUtil.*;
import static de.bernd_michaely.common.cli.parser.CommandLineArguments.*;
import static java.util.Objects.deepEquals;
import static org.junit.Assert.*;

/**
 * Test class for CommandLineArguments.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class CommandLineArgumentsTest
{
	private static class Option
	{
		private final String option;
		private final String param;

		Option(String option, String param)
		{
			this.option = option;
			this.param = param;
		}

		@Override
		public boolean equals(Object object)
		{
			if (object instanceof Option other)
			{
				return deepEquals(this.option, other.option) &&
					deepEquals(this.param, other.param);
			}
			else
			{
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(this.option, this.param);
		}

		static Option of(String option, String param)
		{
			return new Option(option, param);
		}
	}
	private CommandLineArguments args1;
	private List<Option> optionResults = new ArrayList<>();

	@Before
	public void setUp() throws OptionDefinitionException
	{
		this.optionResults = new ArrayList<>();
		this.args1 = new CommandLineArguments(this::handleCallback);
		// flag options:
		for (char c = '0'; c <= '9'; c++)
		{
			this.args1.addFlagOption("option-" + c, c,
				"This is option '" + c + "'.");
		}
		// required param options:
		for (char c = 'a'; c <= 'z'; c++)
		{
			this.args1.addParameterOption("option-" + c, c, true,
				"This is option '" + c + "'.");
		}
		// optional param options:
		for (char c = 'A'; c <= 'Z'; c++)
		{
			this.args1.addParameterOption("option-" + c, c, false,
				"This is option '" + c + "'.");
		}
	}

	private void handleCallback(String option, String param)
	{
		this.optionResults.add(new Option(option, param));
	}

	@Test
	public void printTestDataInfo()
	{
		System.out.println("printTestDataInfo:");
		this.args1.printFormattedDescription(System.out, true);
	}

	@Test(expected = OptionDefinitionException.class)
	public void testDuplicateDefinition() throws OptionDefinitionException
	{
		System.out.println("testDuplicateDefinition");
		final CommandLineArguments cla = new CommandLineArguments(this::handleCallback);
		cla.addFlagOption("longOption", "description");
		cla.addFlagOption("longOption", "description");
	}

	@Test
	public void testParse_null() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_null");
		assertTrue(this.args1.parse((String[]) null).isEmpty());
	}

	@Test
	public void testParse_empty1() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_empty1");
		assertTrue(this.args1.parse(new String[0]).isEmpty());
	}

	@Test
	public void testParse_empty2() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_empty2");
		assertTrue(this.args1.parse("").isEmpty());
	}

	@Test(expected = InvalidCommandLineParametersException.class)
	public void testParse_undefinedShortOption()
		throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_undefinedShortOption");
		this.args1.parse("-?", "one", "two", "three");
	}

	@Test(expected = InvalidCommandLineParametersException.class)
	public void testParse_undefinedLongOption()
		throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_undefinedLongOption");
		this.args1.parse("--undefined-option", "one", "two", "three");
	}

	@Test(expected = InvalidCommandLineParametersException.class)
	public void testParse_invalidShortOptionForm()
		throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_invalidShortOptionForm");
		this.args1.parse("-a=value");
	}

	@Test
	public void testParse_optArgStop()
		throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_optArgStop");
		assertTrue(this.args1.parse("-A", "one", "--option-B=-two", "-C", "-", "-three", "four")
			.equals(List.of("-three", "four")));
		assertTrue(this.optionResults
			.equals(List.of(
				Option.of("option-A", "one"),
				Option.of("option-B", "-two"),
				Option.of("option-C", null))));
	}

	@Test
	public void testParse_001() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_001");
		assertTrue(this.args1.parse("one", "two", "three")
			.equals(List.of("one", "two", "three")));
		assertTrue(this.optionResults.isEmpty());
	}

	@Test
	public void testParse_optionalArgs()
		throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_optionalArgs");
		assertTrue(this.args1.parse("--option-A=one", "--option-B=two", "-CD", "three")
			.isEmpty());
		assertTrue(this.optionResults
			.equals(List.of(
				Option.of("option-A", "one"),
				Option.of("option-B", "two"),
				Option.of("option-C", null),
				Option.of("option-D", "three"))));
	}

	@Test(expected = InvalidCommandLineParametersException.class)
	public void testParse_notEnoughArgs()
		throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_notEnoughArgs");
		assertTrue(this.args1.parse("-abcd", "one", "two", "three")
			.isEmpty());
	}

	@Test
	public void testParse_002() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_002");
		assertTrue(
			this.args1.parse("-7", "-3", "-5", "one", "two", "three")
				.equals(List.of("one", "two", "three")));
		assertTrue(this.optionResults
			.equals(List.of(
				Option.of("option-7", null),
				Option.of("option-3", null),
				Option.of("option-5", null))));
	}

	@Test
	public void testParse_003() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_003");
		assertTrue(
			this.args1.parse("one", "-a", "-b", "-c", "two", "three", "four", "five", "six", "seven")
				.equals(List.of("one", "five", "six", "seven")));
		assertTrue(this.optionResults
			.equals(List.of(
				Option.of("option-a", "two"),
				Option.of("option-b", "three"),
				Option.of("option-c", "four"))));
	}

	@Test
	public void testParse_004() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_004");
		assertTrue(
			this.args1.parse("one", "-abc", "two", "three", "four", "five", "six", "seven")
				.equals(List.of("one", "five", "six", "seven")));
		assertTrue(this.optionResults.equals(List.of(
			Option.of("option-a", "two"),
			Option.of("option-b", "three"),
			Option.of("option-c", "four"))));
	}

	@Test
	public void testParse_005() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_005");
		assertTrue(
			this.args1.parse("one", "-A", "-B", "-C", "two", "three", "four", "five", "six", "seven")
				.equals(List.of("one", "three", "four", "five", "six", "seven")));
		assertTrue(this.optionResults
			.equals(List.of(
				Option.of("option-A", null),
				Option.of("option-B", null),
				Option.of("option-C", "two"))));
	}

	@Test
	public void testParse_006() throws InvalidCommandLineParametersException
	{
		System.out.println("testParse_006");
		assertTrue(
			this.args1.parse(
				"one", "--option-A=Hello", "-b", "-7", "--option-C=World", "two", "three", "-d",
				"four", "five", "six", "seven")
				.equals(List.of("one", "three", "five", "six", "seven")));
		assertTrue(this.optionResults
			.equals(List.of(
				Option.of("option-A", "Hello"),
				Option.of("option-b", "two"),
				Option.of("option-7", null),
				Option.of("option-C", "World"),
				Option.of("option-d", "four")
			)));
	}

	@Test
	public void testParse_ParamRegExpr_Valid()
		throws InvalidCommandLineParametersException, OptionDefinitionException
	{
		System.out.println("testParse_ParamRegExpr_Valid");
		final CommandLineArguments cla = new CommandLineArguments((opt, par) ->
		{
		});
		cla.addParameterOption("aaa", "()|()", false, "Option a");
		cla.addParameterOption("bbb", "(one)|(two)", true, "Option b");
		cla.addParameterOption("ccc", "()|()", false, "Option c");
		assertTrue(cla.parse("--aaa", "--bbb=two", "--ccc").isEmpty());
	}

	@Test(expected = InvalidCommandLineParametersException.class)
	public void testParse_ParamRegExpr_Invalid()
		throws InvalidCommandLineParametersException, OptionDefinitionException
	{
		System.out.println("testParse_ParamRegExpr_Invalid");
		final CommandLineArguments cla = new CommandLineArguments((opt, par) ->
		{
		});
		cla.addParameterOption("aaa", "()|()", false, "Option a");
		cla.addParameterOption("bbb", "(one)|(two)", true, "Option b");
		cla.addParameterOption("ccc", "()|()", false, "Option c");
		assertTrue(cla.parse("--aaa", "--bbb=three", "--ccc").isEmpty());
	}

	@Test
	public void testGetFormattedDescription() throws OptionDefinitionException
	{
		System.out.println("getFormattedDescription test:");
		final CommandLineArguments parser = new CommandLineArguments((opt, par) ->
		{
		});
		parser.addFlagOption("test-option1", "This is the first test option.",
			"It shows a multi line description.");
		parser.addFlagOption("test-option2", "This is the second test option.");
		parser.addFlagOption("help", 'h', "Shows a usage help text.");
		parser.addFlagOption("a-la-la-la-la-long", 'a', "This is the first short option.");
		parser.addFlagOption("z-long", 'z', "This is the last short option.");
		final List<String> formattedDescription = parser.getFormattedDescription(true);
		final Object[] actual = formattedDescription.toArray();
		final String[] expected = new String[]
		{
			formatAsHeader(FORMATTED_DESCRIPTION_HEADER),
			"",
			formatAsOption("-a  --a-la-la-la-la-long  ") + "This is the first short option.",
			formatAsOption("-h  --help                ") + "Shows a usage help text.",
			formatAsOption("    --test-option1        ") + "This is the first test option.",
			"                          It shows a multi line description.",
			formatAsOption("    --test-option2        ") + "This is the second test option.",
			formatAsOption("-z  --z-long              ") + "This is the last short option."
		};
		parser.printFormattedDescription(System.out, true);
		assertArrayEquals(expected, actual);
	}
}
