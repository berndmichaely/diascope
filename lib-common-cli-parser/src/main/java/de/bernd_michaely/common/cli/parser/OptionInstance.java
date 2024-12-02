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

import java.util.Objects;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class to describe an option instance.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
class OptionInstance
{
	private final OptionDescriptor optionDescriptor;
	private @MonotonicNonNull String parameter;

	OptionInstance(OptionDescriptor optionDescriptor)
	{
		this.optionDescriptor = Objects.requireNonNull(optionDescriptor);
	}

	OptionDescriptor getOptionDescriptor()
	{
		return this.optionDescriptor;
	}

	@Nullable
	String getParameter()
	{
		return this.parameter;
	}

	/**
	 * Sets the parameter for this option instance.
	 *
	 * @param parameter the parameter
	 * @throws IllegalStateException
	 */
	void setParameter(@Nullable String parameter)
	{
		if (this.optionDescriptor.isFlagOption())
		{
			throw new IllegalStateException("Option has no parameter!");
		}
		if (this.parameter != null)
		{
			throw new IllegalStateException("Parameter assigned twice!");
		}
		this.parameter = Objects.toString(parameter, "");
	}

	boolean checkParamRegExpr()
	{
		final String param = getParameter();
		if (param == null)
		{
			return true;
		}
		else
		{
			final Pattern pattern = getOptionDescriptor().getPattern();
			return (pattern == null) ? true : pattern.matcher(param).matches();
		}
	}
}
