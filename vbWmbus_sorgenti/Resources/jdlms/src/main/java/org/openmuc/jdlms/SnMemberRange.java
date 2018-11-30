/*
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
 *
 */
package org.openmuc.jdlms;

import org.openmuc.jdlms.internal.Range;

/**
 * This class represents an SN class member of a coherent range.
 *
 * @see SnClassInfo
 */
public class SnMemberRange extends Range<Integer> {
    private static final long serialVersionUID = 5341343393124450155L;

    private static final int MAX_SN_OFFSET = 0xFFF8;

    private final int firstId;

    private SnMemberRange(Integer fromInclusive, Integer toInclusive) {
        this(fromInclusive, toInclusive, -1);
    }

    private SnMemberRange(Integer fromInclusive, Integer toInclusive, int firstId) {
        super(fromInclusive, toInclusive);
        this.firstId = firstId;
    }

    /**
     * Get a new range within the lower bound and <code>0xFFF8</code> (both inclusive).
     * 
     * @param fromInclusive
     *            the lower bound.
     * @return a new range object.
     */
    public static SnMemberRange from(int fromInclusive) {
        return new SnMemberRange(fromInclusive, MAX_SN_OFFSET);
    }

    /**
     * Get a new range within the lower bound and <code>0xFFF8</code> (both inclusive).
     * <p>
     * Note: this initializer must only be used, if <i>f<sub>c</sub> l<sub>p</sub> + 1</i>, with <i>f<sub>c</sub></i>
     * the first member ID of this range and <i>l<sub>p</sub> </i> the last member ID of the preceding range.
     * </p>
     * 
     * @param fromInclusive
     *            the lower bound.
     * @param firstMemberId
     *            the member ID of the first element of this range.
     * @return a new range object.
     */
    public static SnMemberRange from(int fromInclusive, int firstMemberId) {
        return new SnMemberRange(fromInclusive, MAX_SN_OFFSET, firstMemberId);
    }

    /**
     * Get a new range within the lower and the upper bound (both inclusive).
     * 
     * @param fromInclusive
     *            the lower bound.
     * @param toInclusive
     *            the upper bound.
     * @return a new range object.
     */
    public static SnMemberRange between(int fromInclusive, int toInclusive) {
        return new SnMemberRange(fromInclusive, toInclusive);
    }

    /**
     * Get a new range within the lower and the upper bound (both inclusive).
     * <p>
     * Note: this initializer must only be used, if <i>f<sub>c</sub> l<sub>p</sub> + 1</i>, with <i>f<sub>c</sub></i>
     * the first member ID of this range and <i>l<sub>p</sub> </i> the last member ID of the preceding range.
     * </p>
     * 
     * @param fromInclusive
     *            the lower bound.
     * @param toInclusive
     *            the upper bound.
     * @param firstMemberId
     *            the member ID of the first element of this range.
     * @return a new range object.
     */
    public static SnMemberRange between(int fromInclusive, int toInclusive, int firstMemberId) {
        return new SnMemberRange(fromInclusive, toInclusive, firstMemberId);
    }

    /**
     * Get a new range with a single element.
     * 
     * @param element
     *            the element. E.g. <code>0x40</code>.
     * @return a new range object.
     */
    public static SnMemberRange is(int element) {
        return new SnMemberRange(element, element);
    }

    /**
     * Get a new range with a single element.
     * <p>
     * Note: this initializer must only be used, if <i>f<sub>c</sub> l<sub>p</sub> + 1</i>, with <i>f<sub>c</sub></i>
     * the first member ID of this range and <i>l<sub>p</sub> </i> the last member ID of the preceding range.
     * </p>
     * 
     * @param element
     *            the element. E.g. <code>0x40</code>.
     * @param memberId
     *            the member ID of the element.
     * @return a new range object.
     * 
     * @see SnMemberRange#is(int)
     */
    public static SnMemberRange is(int element, int memberId) {
        return new SnMemberRange(element, element, memberId);
    }

    /**
     * Get the first range ID. -1 if not set.
     * 
     * @return the first member ID of this range.
     */
    public int getFirstRangeId() {
        return firstId;
    }

    @Override
    public String toString() {
        return String.format("{\"range\": %s, \"first-member-id\": %d}", super.toString(), this.firstId);
    }

}
