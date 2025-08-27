/*
 * Copyright (C) 2025 Bernd Michaely (info@bernd-michaely.de)
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
package de.bernd_michaely.diascope.app.util.beans.binding;

import de.bernd_michaely.diascope.app.util.beans.ListChangeListenerBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.ObservableList;

/// List binding utilities.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListBindings
{
	private static class ChainedObservableDoubleValues<T>
	{
		private final ReadOnlyDoubleWrapper value = new ReadOnlyDoubleWrapper();
		private final List<DoubleProperty> intermediateResults = new ArrayList<>();

		private ChainedObservableDoubleValues(ObservableList<T> observableList,
			Function<T, ReadOnlyDoubleProperty> selectorParam, Function<T, DoubleProperty> selectorResult,
			BiFunction<ObservableNumberValue, ObservableNumberValue, NumberBinding> operator,
			double neutralElement)
		{
			if (observableList.isEmpty())
			{
				value.set(neutralElement);
			}
			else
			{
				final T first = observableList.getFirst();
				selectorResult.apply(first).bind(selectorParam.apply(first));
				for (int i = 1; i < observableList.size(); i++)
				{
					final T prev = observableList.get(i - 1);
					final T next = observableList.get(i);
					selectorResult.apply(next).bind(operator.apply(
						selectorResult.apply(prev), selectorParam.apply(next)));
				}
				value.bind(selectorResult.apply(observableList.getLast()));
			}
			observableList.addListener(new ListChangeListenerBuilder<T>()
				.onAdd(change ->
				{
					final var list = change.getList();
					final int n = list.size();
					final int from = change.getFrom();
					final int to = change.getTo();
					final int end = to < n ? to + 1 : to;
					for (int i = from; i < end; i++)
					{
						if (i == 0)
						{
							final T first = list.getFirst();
							selectorResult.apply(first).bind(selectorParam.apply(first));
						}
						else
						{
							final T prev = list.get(i - 1);
							final T next = list.get(i);
							selectorResult.apply(next).bind(operator.apply(
								selectorResult.apply(prev), selectorParam.apply(next)));
						}
					}
					if (to == n)
					{
						value.bind(selectorResult.apply(list.getLast()));
					}
				})
				.onRemove(change ->
				{
					final var list = change.getList();
					final int n = list.size();
					final int from = change.getFrom();
					final int to = change.getTo();
					change.getRemoved().stream().map(selectorResult).forEach(DoubleProperty::unbind);
					if (n > 0)
					{
						if (from == 0)
						{
							final T first = list.getFirst();
							selectorResult.apply(first).bind(selectorParam.apply(first));
						}
						if (from > 0 && to < n)
						{
							final T prev = list.get(from - 1);
							final T next = list.get(to);
							selectorResult.apply(next).bind(operator.apply(
								selectorResult.apply(prev), selectorParam.apply(next)));
						}
						if (to == n)
						{
							value.bind(selectorResult.apply(list.getLast()));
						}
					}
					else
					{
						value.unbind();
						value.set(neutralElement);
					}
				})
				.build());
		}
	}

	/// Creates a property to hold the dynamically cumulated result of
	/// calculated list values.
	/// For all items of the list, the parameter values will be combined with the
	/// operator and the result will be provided as a DoubleExpression.
	/// As the list will be observed for changes, the internal bindings will be
	/// reconfigured.
	///
	/// @param <T>             the type of the observable list
	/// @param observableList  the observable list
	/// @param selectorParam   property holding the parameters to combine
	/// @param selectorResult  property holding intermediate results
	/// @param operator        function to combine two parameters
	/// @param neutralElement  value for empty list
	/// @return a DoubleExpression containing the combined result
	///
	public static <T> DoubleExpression chainedObservableDoubleValues(
		ObservableList<T> observableList,
		Function<T, ReadOnlyDoubleProperty> selectorParam,
		Function<T, DoubleProperty> selectorResult,
		BiFunction<ObservableNumberValue, ObservableNumberValue, NumberBinding> operator,
		double neutralElement)
	{
		final var chainedValues = new ChainedObservableDoubleValues<>(
			observableList, selectorParam, selectorResult, operator, neutralElement);
		return chainedValues.value.getReadOnlyProperty();
	}
}
