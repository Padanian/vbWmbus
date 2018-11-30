/**
 * Copyright 2012-18 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.jdlms.internal;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class RangeTest {

    @Test
    @Parameters(method = "intersectTestParams")
    public void intersectTest(int lowerBound, int upperBound, int elem, boolean expectedResult) throws Exception {
        Range<Integer> range = Range.between(lowerBound, upperBound);

        assertEquals(expectedResult, range.intersectsWith(elem));
    }

    public Object intersectTestParams() {
        Object[] params1 = { 10, 20, 15, true };
        Object[] params2 = { 10, 20, 20, true };
        Object[] params3 = { 10, 20, 10, true };

        Object[] params4 = { 10, 20, 30, false };
        Object[] params5 = { 10, 20, 2, false };

        return new Object[][] { params1, params2, params3, params4, params5 };
    }

    @Test
    @Parameters(method = "rangeIntersectTestParams")
    public <E extends Serializable & Comparable<E>> void rangeIntersectTest(Range<E> r1, Range<E> r2,
            boolean expectedResult) throws Exception {

        // tests symmetry of the intersect function
        // this does not imply transitivity
        assertEquals(expectedResult, r1.intersects(r2));
        assertEquals(expectedResult, r2.intersects(r1));
    }

    public Object rangeIntersectTestParams() {

        Object[] params1 = { Range.between(10, 29), Range.between(25, 737), true };
        Object[] params2 = { Range.between(10, 29), Range.between(29, 737), true };

        Object[] params5 = { Range.between(10, 100), Range.between(10, 102), true };

        Object[] params3 = { Range.between(10, 29), Range.between(88, 737), false };

        Object[] params4 = { Range.between(12, 15), Range.between(1, 2), false };

        return new Object[][] { params1, params2, params3, params4, params5 };
    }

}
