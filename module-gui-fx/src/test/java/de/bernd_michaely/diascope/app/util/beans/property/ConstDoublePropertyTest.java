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
package de.bernd_michaely.diascope.app.util.beans.property;

import javafx.beans.property.ReadOnlyDoubleProperty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ConstDoubleProperty Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class ConstDoublePropertyTest
{
	/**
	 * Test of get method, of class ConstDoubleProperty.
	 */
	@Test
	public void testGet()
	{
		assertThat(new ConstDoubleProperty()).extracting(ReadOnlyDoubleProperty::get).isEqualTo(0d);
		assertThat(new ConstDoubleProperty(-3.7)).extracting(ReadOnlyDoubleProperty::get).isEqualTo(-3.7);
	}
}
