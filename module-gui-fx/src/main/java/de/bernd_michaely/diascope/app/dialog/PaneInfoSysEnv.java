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
package de.bernd_michaely.diascope.app.dialog;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;

import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;

/**
 * Class to construct the content of a system environment info dialog.
 *
 * @author Bernd Michaely
 */
public class PaneInfoSysEnv
{
	private static final Comparator<Map.Entry<?, ?>> ITEM_COMPARATOR;
	private final TabPane tabPane;

	static
	{
		ITEM_COMPARATOR = (Map.Entry<?, ?> entry1, Map.Entry<?, ?> entry2) ->
		{
			final String key1 = Objects.toString(entry1.getKey(), "");
			final String key2 = Objects.toString(entry2.getKey(), "");
			return key1.compareTo(key2);
		};
	}

	private static class Item
	{
		private final String key, value;

		private Item(Map.Entry<?, ?> entry)
		{
			this.key = Objects.toString(entry.getKey());
			this.value = Objects.toString(entry.getValue());
		}

		@Override
		public boolean equals(@Nullable Object object)
		{
			if (object instanceof Item other)
			{
				return Objects.equals(this.key, other.key) &&
					Objects.equals(this.value, other.value);
			}
			else
			{
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(key, value);
		}

		@Override
		public String toString()
		{
			return String.format("[%s=%s]", key, value);
		}
	}

	private Tab createTab(@UnderInitialization PaneInfoSysEnv this,
		String title, Stream<Item> stream)
	{
		final ObservableList<Item> observableList =
			FXCollections.observableList(stream.collect(Collectors.toList()));
		final TableView<Item> tableView = new TableView<>(observableList);
		// key column:
		final TableColumn<Item, String> columnKey = new TableColumn<>("Key");
		columnKey.setCellValueFactory(
			p -> new ReadOnlyObjectWrapper<>(p.getValue().key));
		columnKey.setSortable(false);
		columnKey.setReorderable(false);
		// value column:
		final TableColumn<Item, String> columnVal = new TableColumn<>("Value");
		columnVal.setCellValueFactory(
			p -> new ReadOnlyObjectWrapper<>(p.getValue().value));
		columnVal.setSortable(false);
		columnVal.setReorderable(false);
		tableView.getColumns().setAll(List.of(columnKey, columnVal));
		tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		tableView.setMinSize(1, 1);
		tableView.setPrefSize(1000, 500);
		columnKey.setResizable(true);
		columnVal.setResizable(true);
		columnKey.setPrefWidth(tableView.getPrefWidth() / 3);
		columnVal.setPrefWidth(tableView.getPrefWidth() * 2 / 3);
		return new Tab(title, tableView);
	}

	public PaneInfoSysEnv()
	{
		tabPane = new TabPane(
			createTab("Java Properties",
				System.getProperties().entrySet().stream()
					.sorted(ITEM_COMPARATOR)
					.map(Item::new)),
			createTab("System Environment",
				System.getenv().entrySet().stream()
					.sorted(ITEM_COMPARATOR)
					.map(Item::new)));
		tabPane.setTabClosingPolicy(UNAVAILABLE);
		tabPane.setMinSize(1, 1);
		tabPane.setPrefSize(1000, 500);
	}

	public Region getDisplay()
	{
		return this.tabPane;
	}
}
