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

import java.io.Serializable;
import java.text.MessageFormat;

public class Range<T extends Serializable & Comparable<T>> implements Serializable {

    private static final long serialVersionUID = 6560287867950653244L;

    private final T fromInclusive;
    private final T toInclusive;

    protected Range(T fromInclusive, T toInclusive) {
        if (toInclusive.compareTo(fromInclusive) < 0) {
            throw new IllegalArgumentException(
                    "Lower bound is greater than upper bound. [" + fromInclusive + "," + toInclusive + "]");
        }
        this.fromInclusive = fromInclusive;
        this.toInclusive = toInclusive;

    }

    public static <T extends Comparable<T> & Serializable> Range<T> between(T fromInclusive, T toInclusive) {
        return new Range<>(fromInclusive, toInclusive);
    }

    public static <T extends Comparable<T> & Serializable> Range<T> is(T element) {
        return new Range<>(element, element);
    }

    /**
     * Checks if this range intersects with an other range.
     * 
     * @param other
     *            the other range.
     * @return true if the ranges intersect.
     */
    public boolean intersects(Range<T> other) {

        if (this.fromInclusive.compareTo(other.fromInclusive) > 0) {
            return other.toInclusive.compareTo(this.fromInclusive) >= 0;
        }
        else if (this.fromInclusive.compareTo(other.fromInclusive) < 0) {

            return this.toInclusive.compareTo(other.fromInclusive) >= 0;
        }
        else {
            return true;
        }
    }

    public boolean intersectsWith(T elem) {
        return this.fromInclusive.compareTo(elem) <= 0 && this.toInclusive.compareTo(elem) >= 0;
    }

    public T getMinimum() {
        return fromInclusive;
    }

    public T getMaximum() {
        return toInclusive;
    }

    @Override
    public String toString() {
        return MessageFormat.format("'{'\"fromInclusive: \"{0,number,#}\", \"toInclusive\": \"{1,number,#}\"'}'",
                fromInclusive, toInclusive);
    }
}
