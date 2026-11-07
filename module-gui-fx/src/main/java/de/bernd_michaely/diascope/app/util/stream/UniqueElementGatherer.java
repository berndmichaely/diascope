/*
 * Copyright (C) 2026 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.util.stream;

import de.bernd_michaely.diascope.app.util.stream.UniqueElementGatherer.State;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import org.checkerframework.checker.nullness.qual.Nullable;

/// Determines, whether all elements of the stream are equal.
/// If this is the case, a single element stream with that value is returned.
/// If the stream contains more than one unique value or is empty,
/// an empty stream is returned.
///
/// @param <T> the stream element type
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class UniqueElementGatherer<T> implements Gatherer<T, State<T>, T>
{
	/// Factory method.
	///
	/// @param <G> the stream element type
	/// @return a new instance
	///
	public static <G> UniqueElementGatherer<G> of()
	{
		return new UniqueElementGatherer<>();
	}

	public static class State<S>
	{
		private @Nullable S value;
		private boolean isInitialized;

		private State()
		{
		}

		private State(@Nullable S value)
		{
			this.value = value;
			this.isInitialized = true;
		}
	}

	private UniqueElementGatherer()
	{
	}

	@Override
	public BinaryOperator<State<T>> combiner()
	{
		return (state1, state2) ->
		{
			final T value1 = state1.value;
			final T value2 = state2.value;
			return new State<>(Objects.equals(value1, value2) ? value1 : null);
		};
	}

	@Override
	public BiConsumer<State<T>, Downstream<? super T>> finisher()
	{
		return (state, downstream) ->
		{
			final T value = state.value;
			if (value != null)
			{
				downstream.push(value);
			}
		};
	}

	@Override
	public Supplier<State<T>> initializer()
	{
		return State::new;
	}

	@Override
	public Integrator<State<T>, T, T> integrator()
	{
		return (state, nextElement, downstream) ->
		{
			if (downstream.isRejecting())
			{
				return false;
			}
			if (state.isInitialized)
			{
				final boolean isEqual = Objects.equals(state.value, nextElement);
				if (!isEqual)
				{
					state.value = null;
				}
				return isEqual;
			}
			else
			{
				state.value = nextElement;
				state.isInitialized = true;
				return true;
			}
		};
	}
}
