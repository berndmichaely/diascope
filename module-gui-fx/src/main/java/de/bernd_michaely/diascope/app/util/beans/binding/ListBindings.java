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
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ObservableList;

/// List binding utilities.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ListBindings
{
	private static class CumulatedOperations<E> extends DoubleBinding
	{
		private final ObservableList<E> observableList;
		private final Function<E, ReadOnlyDoubleProperty> selector;
		private final DoubleBinaryOperator operator;
		private final double neutralElement;

		private CumulatedOperations(ObservableList<E> observableList,
			Function<E, ReadOnlyDoubleProperty> selector, DoubleBinaryOperator operator, double neutralElement)
		{
			this.observableList = observableList;
			this.selector = selector;
			this.operator = operator;
			this.neutralElement = neutralElement;
		}

		@Override
		protected double computeValue()
		{
			return observableList.stream()
				.map(selector).mapToDouble(ObservableDoubleValue::get)
				.reduce(neutralElement, operator);
		}

		private static <B> DoubleBinding newInstance(ObservableList<B> observableList,
			Function<B, ReadOnlyDoubleProperty> selector, DoubleBinaryOperator operator, double neutralElement)
		{
			final var binding = new CumulatedOperations<>(observableList, selector, operator, neutralElement);
			final Consumer<B> listenerAdd = item -> binding.bind(selector.apply(item));
			final Consumer<B> listenerRemove = item -> binding.unbind(selector.apply(item));
			observableList.forEach(listenerAdd);
			binding.bind(observableList);
			observableList.addListener(new ListChangeListenerBuilder<B>()
				.onAdd(change -> change.getAddedSubList().forEach(listenerAdd))
				.onRemove(change -> change.getRemoved().forEach(listenerRemove))
				.build());
			return binding;
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
	/// @param operator        an associative operator to combine two parameters
	/// @param neutralElement  value for empty list
	/// @return a DoubleBinding containing the combined result
	///
	public static DoubleBinding cumulatedOperations(
		ObservableList<? extends ReadOnlyDoubleProperty> observableList,
		DoubleBinaryOperator operator, double neutralElement)
	{
		return cumulatedOperations(observableList, p -> p, operator, neutralElement);
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
	/// @param operator        an associative operator to combine two parameters
	/// @param neutralElement  value for empty list
	/// @return a DoubleBinding containing the combined result
	///
	public static <T> DoubleBinding cumulatedOperations(ObservableList<T> observableList,
		Function<T, ReadOnlyDoubleProperty> selector, DoubleBinaryOperator operator, double neutralElement)
	{
		return CumulatedOperations.<T>newInstance(observableList, selector, operator, neutralElement);
	}
}
