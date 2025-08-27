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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

/// List binding utilities.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListBindings
{
	public static class ChainedObservableDoubleValues<T>
	{
		private final ReadOnlyDoubleWrapper finalResult = new ReadOnlyDoubleWrapper();
		private final List<ReadOnlyDoubleWrapper> intermediateResults = new ArrayList<>();

		public ChainedObservableDoubleValues(ObservableList<T> observableList,
			Function<T, ReadOnlyDoubleProperty> selector,
			BiFunction<ObservableNumberValue, ObservableNumberValue, NumberBinding> operator,
			double neutralElement)
		{
			final BiConsumer<ObservableList<? extends T>, Integer> bindItems = (list, index) ->
			{
				if (index > 0)
				{
					intermediateResults.get(index).bind(operator.apply(
						intermediateResults.get(index - 1).getReadOnlyProperty(),
						selector.apply(list.get(index))));
				}
				else
				{
					intermediateResults.getFirst().bind(selector.apply(list.getFirst()));
				}
			};
			final Consumer<Change<? extends T>> recreateBindings = change ->
			{
				final var list = change.getList();
				final int n = list.size();
				final int from = change.getFrom();
				final int to = change.getTo();
				intermediateResults.subList(from, to).forEach(DoubleProperty::unbind);
				for (int i = from; i < to; i++)
				{
					bindItems.accept(list, i);
				}
				if (to < n)
				{
					bindItems.accept(list, to);
				}
				else
				{
					finalResult.bind(intermediateResults.getLast());
				}
			};
			// handle initially given list items:
			if (observableList.isEmpty())
			{
				finalResult.set(neutralElement);
			}
			else
			{
				for (int i = 0; i < observableList.size(); i++)
				{
					intermediateResults.add(new ReadOnlyDoubleWrapper());
					bindItems.accept(observableList, i);
				}
				finalResult.bind(intermediateResults.getLast());
			}
			observableList.addListener(new ListChangeListenerBuilder<T>()
				.onAdd(change ->
				{
					final var list = change.getList();
					final int n = list.size();
					final int from = change.getFrom();
					final int to = change.getTo();
					if (change.wasReplaced())
					{
						recreateBindings.accept(change);
					}
					else
					{
						for (int i = from; i < to; i++)
						{
							intermediateResults.add(i, new ReadOnlyDoubleWrapper());
							bindItems.accept(list, i);
						}
						if (to < n)
						{
							bindItems.accept(list, to);
						}
						else
						{
							finalResult.bind(intermediateResults.getLast());
						}
					}
				})
				.onRemove(change ->
				{
					if (!change.wasReplaced())
					{
						final var list = change.getList();
						final int n = list.size();
						final int removedSize = change.getRemovedSize();
						final int from = change.getFrom();
						final var subList = intermediateResults.subList(from, from + removedSize);
						subList.forEach(DoubleProperty::unbind);
						subList.clear();
						if (list.isEmpty())
						{
							finalResult.unbind();
							finalResult.set(neutralElement);
						}
						else
						{
							if (from < n)
							{
								bindItems.accept(list, from);
							}
							else
							{
								finalResult.bind(intermediateResults.getLast());
							}
						}
					}
				})
				.onPermutate(recreateBindings)
				.onUpdate(recreateBindings)
				.build());
		}

		public double getIntermediateResult(int index)
		{
			return intermediateResults.get(index).getReadOnlyProperty().get();
		}

		public double getfinalResult()
		{
			return finalResult.getReadOnlyProperty().get();
		}
	}

	/// Creates a property to hold the dynamically cumulated result of
	/// calculated list values.
	/// For all items of the list, the list item values will be combined with the
	/// operator and the result will be provided as a DoubleExpression.
	/// As the list will be observed for changes, the internal bindings will be
	/// reconfigured.
	///
	/// @param observableList  the observable list
	/// @param operator        function to combine two parameters
	/// @param neutralElement  value for empty list
	/// @return a DoubleExpression containing the combined result
	///
	public static DoubleExpression chainedObservableDoubleValues(
		ObservableList<? extends ReadOnlyDoubleProperty> observableList,
		BiFunction<ObservableNumberValue, ObservableNumberValue, NumberBinding> operator,
		double neutralElement)
	{
		return chainedObservableDoubleValues(observableList, p -> p, operator, neutralElement);
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
	/// @param selector        property holding the parameters to combine
	/// @param operator        function to combine two parameters
	/// @param neutralElement  value for empty list
	/// @return a DoubleExpression containing the combined result
	///
	public static <T> DoubleExpression chainedObservableDoubleValues(
		ObservableList<T> observableList,
		Function<T, ReadOnlyDoubleProperty> selector,
		BiFunction<ObservableNumberValue, ObservableNumberValue, NumberBinding> operator,
		double neutralElement)
	{
		final var chainedValues = new ChainedObservableDoubleValues<>(
			observableList, selector, operator, neutralElement);
		return chainedValues.finalResult.getReadOnlyProperty();
	}
}
