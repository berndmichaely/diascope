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

import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Class to describe a single command line option.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class OptionDescriptor
{
	private final boolean flagOption;
	private final String longOption;
	private final char shortOption;
	private final String[] description;
	private final @Nullable Pattern pattern;
	private final boolean parameterRequired;

	/**
	 * Creates a flag command line option.
	 *
	 * @param longOption  the long option name
	 * @param shortOption the short option name
	 * @param description a description for this option
	 */
	OptionDescriptor(String longOption, char shortOption, String... description)
	{
		this.flagOption = true;
		this.longOption = requireNonNull(longOption);
		this.shortOption = shortOption;
		this.parameterRequired = false;
		this.pattern = null;
		this.description = requireNonNull(description);
	}

	/**
	 * Creates a command line option with parameter.
	 *
	 * @param longOption        the long option name
	 * @param shortOption       the short option name
	 * @param paramRegExpr      a regular expression the parameter must match
	 * @param parameterRequired true, if a parameter is mandatory for this option
	 * @param description       a description for this option
	 */
	OptionDescriptor(String longOption, char shortOption,
		@Nullable String paramRegExpr, boolean parameterRequired, String... description)
	{
		this.flagOption = false;
		this.longOption = requireNonNull(longOption);
		this.shortOption = shortOption;
		this.parameterRequired = parameterRequired;
		this.pattern = (paramRegExpr != null) ? Pattern.compile(paramRegExpr) : null;
		this.description = requireNonNull(description);
	}

	String getLongOption()
	{
		return this.longOption;
	}

	char getShortOption()
	{
		return this.shortOption;
	}

	boolean isProvidingShortOption()
	{
		return this.shortOption > 0;
	}

	String[] getDescription()
	{
		return this.description;
	}

	boolean isFlagOption()
	{
		return this.flagOption;
	}

	boolean isParameterRequired()
	{
		return this.parameterRequired;
	}

	boolean isParameterOptional()
	{
		return !isFlagOption() && !isParameterRequired();
	}

	@Nullable
	Pattern getPattern()
	{
		return this.pattern;
	}
}
