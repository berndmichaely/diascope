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
package de.bernd_michaely.diascope.app.util.action;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.diascope.app.util.action.ToggleActionTest.ToggleIds.*;
import static org.junit.jupiter.api.Assertions.*;

/// ToggleAction Test.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class ToggleActionTest
{
	enum ToggleIds
	{
		NONE, ZERO, ONE, TWO
	}
	private ToggleAction<ToggleIds> toggleAction;
	private List<ToggleStub> toggleButtons;
	private List<ToggleStub> radioMenuItems;

	@BeforeEach
	public void setUp()
	{
		toggleAction = new ToggleAction<>(NONE, Map.of());
		assertEquals(NONE, toggleAction.selectedIdProperty().get());
		toggleButtons = List.of(new ToggleStub("A"), new ToggleStub("B"), new ToggleStub("C"));
		radioMenuItems = List.of(new ToggleStub("a"), new ToggleStub("b"), new ToggleStub("c"));
		final int n = ToggleIds.values().length - 1;
		for (int i = 0; i < n; i++)
		{
			final var id = ToggleIds.values()[i + 1];
			final ToggleStub toggle1 = toggleButtons.get(i);
			System.out.println("· %s ← %s".formatted(toggle1, id));
			final ToggleStub toggle2 = radioMenuItems.get(i);
			System.out.println("· %s ← %s".formatted(toggle2, id));
			toggleAction.addToggles(id, toggle1, toggle2);
		}
	}

	@AfterEach
	public void tearDown()
	{
		toggleAction = null;
		toggleButtons = null;
		radioMenuItems = null;
	}

	private void _check(ToggleIds expected, ToggleAction<ToggleIds> toggleAction)
	{
		final ToggleIds actual = toggleAction.getSelectedId();
		System.out.println("→ toggleButtons  : " + toggleButtons);
		System.out.println("→ radioMenuItems : " + radioMenuItems);
		System.out.println("  → selected toggle ID : " + actual);
		assertEquals(expected, actual);
	}

	private void _check_change_toggle_selections(List<ToggleStub> toggles)
	{
		System.out.println("  · change toggle selections");
		_check(NONE, toggleAction);
		toggles.get(0).setSelected(true);
		_check(ZERO, toggleAction);
		toggles.get(1).setSelected(true);
		_check(ONE, toggleAction);
		toggles.get(2).setSelected(true);
		_check(TWO, toggleAction);
		toggles.get(2).setSelected(false);
		_check(NONE, toggleAction);
	}

	private void _check_change_SelectedId_property(List<ToggleStub> toggles)
	{
		System.out.println("  · change SelectedId property");
		final int n = ToggleIds.values().length - 1;
		for (int i = 0; i < n; i++)
		{
			final var toggleId = ToggleIds.values()[i + 1];
			toggleAction.setSelectedId(toggleId);
			for (int k = 0; k < n; k++)
			{
				assertEquals(i == k, toggles.get(k).isSelected());
			}
			_check(toggleId, toggleAction);
		}
		toggleAction.setSelectedId(NONE);
		for (ToggleStub toggle : toggles)
		{
			assertFalse(toggle.isSelected());
		}
		_check(NONE, toggleAction);
	}

	@Test
	public void test_ToggleAction_initial_not_unselectedId()
	{
		System.out.println("test_ToggleAction_initial_not_unselectedId");
		toggleAction = new ToggleAction<>(NONE, Map.of());
		assertEquals(NONE, toggleAction.selectedIdProperty().get());
		final ToggleIds preselectedId = TWO;
		// change selected id before adding toggles:
		toggleAction.setSelectedId(preselectedId);
		assertEquals(preselectedId, toggleAction.selectedIdProperty().get());
		toggleButtons = List.of(new ToggleStub("A"), new ToggleStub("B"), new ToggleStub("C"));
		radioMenuItems = List.of(new ToggleStub("a"), new ToggleStub("b"), new ToggleStub("c"));
		final int n = ToggleIds.values().length - 1;
		int indexPreselectedId = -1;
		for (int i = 0; i < n; i++)
		{
			final var id = ToggleIds.values()[i + 1];
			if (id == preselectedId)
			{
				indexPreselectedId = i;
			}
			final ToggleStub toggle1 = toggleButtons.get(i);
			System.out.println("· %s ← %s".formatted(toggle1, id));
			final ToggleStub toggle2 = radioMenuItems.get(i);
			System.out.println("· %s ← %s".formatted(toggle2, id));
			toggleAction.addToggles(id, toggle1, toggle2);
		}
		for (int i = 0; i < n; i++)
		{
			assertEquals(i == indexPreselectedId, toggleButtons.get(i).isSelected());
			assertEquals(i == indexPreselectedId, radioMenuItems.get(i).isSelected());
		}
	}

	@Test
	public void test_ToggleAction_1()
	{
		System.out.println("test_ToggleAction_1");
		_check_change_toggle_selections(toggleButtons);
	}

	@Test
	public void test_ToggleAction_2()
	{
		System.out.println("test_ToggleAction_2");
		_check_change_toggle_selections(radioMenuItems);
	}

	@Test
	public void test_ToggleAction_3()
	{
		System.out.println("test_ToggleAction_3");
		_check_change_SelectedId_property(toggleButtons);
	}

	@Test
	public void test_ToggleAction_4()
	{
		System.out.println("test_ToggleAction_4");
		_check_change_SelectedId_property(radioMenuItems);
	}
}
