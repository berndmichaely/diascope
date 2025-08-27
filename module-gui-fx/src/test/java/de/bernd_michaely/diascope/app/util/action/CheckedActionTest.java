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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// CheckedAction Test.
///
/// @author Bernd Michaely (info@bernd-michaely.de)
///
public class CheckedActionTest
{
	private CheckedAction checkedAction;
	private ToggleStub button;
	private ToggleStub menuItem;

	@BeforeEach
	public void setUp()
	{
		checkedAction = new CheckedAction();
		button = new ToggleStub("button");
		menuItem = new ToggleStub("menuItem");
	}

	@AfterEach
	public void tearDown()
	{
		checkedAction = null;
		button = null;
		menuItem = null;
	}

	private void _check(boolean expected)
	{
		System.out.println("  · check for all → selected is " + expected);
		assertEquals(expected, checkedAction.isSelected());
		assertEquals(expected, button.isSelected());
		assertEquals(expected, menuItem.isSelected());
	}

	@Test
	public void test_CheckedAction_initial_false()
	{
		System.out.println("test_CheckedAction_initial_false");
		assertFalse(checkedAction.isSelected());
		checkedAction.addToggle(button);
		checkedAction.addToggle(menuItem);
		_check(false);
		System.out.println("→ test checkedAction");
		checkedAction.setSelected(true);
		_check(true);
		checkedAction.setSelected(false);
		_check(false);
		System.out.println("→ test button");
		button.setSelected(true);
		_check(true);
		button.setSelected(false);
		_check(false);
		System.out.println("→ test menuItem");
		menuItem.setSelected(true);
		_check(true);
		menuItem.setSelected(false);
		_check(false);
	}

	@Test
	public void test_CheckedAction_initial_true()
	{
		System.out.println("test_CheckedAction_initial_true");
		assertFalse(checkedAction.isSelected());
		checkedAction.setSelected(true);
		assertTrue(checkedAction.isSelected());
		checkedAction.addToggle(button);
		checkedAction.addToggle(menuItem);
		_check(true);
		System.out.println("→ test checkedAction");
		checkedAction.setSelected(false);
		_check(false);
		System.out.println("→ test button");
		button.setSelected(true);
		_check(true);
		button.setSelected(false);
		_check(false);
		System.out.println("→ test menuItem");
		menuItem.setSelected(true);
		_check(true);
		menuItem.setSelected(false);
		_check(false);
	}
}
