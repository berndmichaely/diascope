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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UniqueElementGatherer Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class UniqueElementGathererTest
{
	/// Determines, whether all values of the iterable are equal, and
	/// returns that value, if available.
	///
	/// @param <T> the element type
	/// @param <E> the result type of the extractor
	/// @param iterable the elements to check
	/// @param extractor function to extract a value from elements
	/// @return the single value, if all elements are equal, otherwise, or if the
	///         iterable is empty, the default value
	///
	private static <T, E> Optional<E> getUniqueElement(Iterable<T> iterable, Function<T, E> extractor)
	{
		final var iterator = iterable.iterator();
		boolean isEqual = true;
		E result;
		if (iterator.hasNext())
		{
			result = extractor.apply(iterator.next());
		}
		else
		{
			return Optional.empty();
		}
		while (isEqual && iterator.hasNext())
		{
			isEqual = Objects.equals(result, extractor.apply(iterator.next()));
		}
		return isEqual ? Optional.of(result) : Optional.empty();
	}

	private void _test(int n, int indexNonUnique, boolean parallel)
	{
		System.out.println("· Test of %s - %10s - all items unique: %5b - list size: %9d".formatted(
			UniqueElementGatherer.class.getSimpleName(),
			parallel ? "parallel" : "sequential",
			indexNonUnique < 0, n));
		final List<Integer> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++)
		{
			list.add(i == indexNonUnique ? 2 : 1);
		}
		assertThat(list).size().isEqualTo(n);
		final Stream<Integer> stream = parallel ? list.stream().parallel() : list.stream().sequential();
		final Optional<Integer> resultGatherer = stream.gather(UniqueElementGatherer.of()).findAny();
		assertThat(resultGatherer).isEqualTo(getUniqueElement(list, Function.identity()));
	}

	private static final int N = 50_000_000;

	@Test
	public void test_0_s()
	{
		_test(0, -1, false);
	}

	@Test
	public void test_0_p()
	{
		_test(0, -1, true);
	}

	@Test
	public void test_1_s()
	{
		_test(1, -1, false);
	}

	@Test
	public void test_1_p()
	{
		_test(1, -1, true);
	}

	@Test
	public void test_2_s()
	{
		_test(2, -1, false);
	}

	@Test
	public void test_2_p()
	{
		_test(2, -1, true);
	}

	@Test
	public void test_3_s()
	{
		_test(2, 1, false);
	}

	@Test
	public void test_3_p()
	{
		_test(2, 1, true);
	}

	@Test
	public void test_4_s()
	{
		_test(N, -1, false);
	}

	@Test
	public void test_4_p()
	{
		_test(N, -1, true);
	}

	@Test
	public void test_5_s()
	{
		_test(N, 17, false);
	}

	@Test
	public void test_5_p()
	{
		_test(N, 17, true);
	}

	@Test
	public void test_6_s()
	{
		_test(N, N - 17, false);
	}

	@Test
	public void test_6_p()
	{
		_test(N, N - 17, true);
	}
}
