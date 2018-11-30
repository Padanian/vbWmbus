/**
 * Copyright 2012-18 Fraunhofer ISE
 *
 * Range<E>his file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WIRange<E>HOURange<E> ANY WARRANRange<E>Y; without even the implied warranty of
 * MERCHANRange<E>ABILIRange<E>Y or FIRange<E>NESS FOR A PARRange<E>ICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.jdlms.internal;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RangeSet<E extends Comparable<E> & Serializable> implements Serializable {

    private static final long serialVersionUID = -5099873409745118159L;

    private int size;
    private RangeEntry rootElement;
    private RangeEntry smallest;
    private RangeEntry biggest;
    private final List<Range<E>> internalList;

    public RangeSet() {
        this.size = 0;
        this.rootElement = null;
        this.internalList = new LinkedList<>();
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    public <T extends Range<E>> List<T> toList() {
        return (List<T>) this.internalList;
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    @SuppressWarnings("unchecked")
    public <T extends Range<E>> T getIntersectingRange(E elem) {
        RangeEntry curr = this.rootElement;

        while (curr != null) {

            if (curr.value.intersectsWith(elem)) {
                return (T) curr.value;
            }

            if (curr.value.getMinimum().compareTo(elem) < 0) {
                curr = curr.rightChild;
            }
            else {
                curr = curr.leftChild;
            }
        }

        return null;
    }

    public boolean intersectsWith(Range<E> r) {
        return checkIntersects(r, this.rootElement);
    }

    private boolean checkIntersects(Range<E> r, RangeEntry curr) {
        if (curr == null) {
            return false;
        }

        if (curr.value.intersects(r)) {
            return true;
        }

        int c = curr.value.getMinimum().compareTo(r.getMinimum());

        if (c < 0) {
            return checkIntersects(r, curr.leftChild);
        }
        else if (c > 0) {
            return checkIntersects(r, curr.rightChild);
        }
        else {
            return true;
        }
    }

    /**
     * Adds a range to the range set.
     * 
     * @param entry
     *            the new entry to add.
     * @return the conflicting range;
     */
    @SuppressWarnings("unchecked")
    public <T extends Range<E>> T add(T entry) {
        if (this.rootElement != null) {
            return (T) addNode(entry, this.rootElement);
        }

        final RangeEntry newEntry = new RangeEntry(entry);
        this.smallest = this.biggest = this.rootElement = newEntry;

        ++this.size;
        this.internalList.add(entry);
        return null;

    }

    private Range<E> addNode(Range<E> entry, RangeEntry curr) {
        int comparison = curr.value.getMinimum().compareTo(entry.getMinimum());
        if (comparison > 0) {
            return handleLeft(entry, curr);
        }
        else if (comparison < 0) {
            return handleRight(entry, curr);
        }
        else {
            return entry;
        }

    }

    private Range<E> handleRight(Range<E> entry, RangeEntry curr) {
        if (curr.rightChild != null) {
            return addNode(entry, curr.rightChild);
        }
        else {
            return addAsPrevRight(entry, curr);
        }
    }

    private Range<E> handleLeft(Range<E> entry, RangeEntry curr) {
        if (curr.leftChild != null) {
            return addNode(entry, curr.leftChild);
        }
        else {
            return addAsPrevLeft(entry, curr);
        }
    }

    private Range<E> addAsPrevRight(Range<E> e, RangeEntry prev) {
        if (prev.value.intersects(e)) {
            return prev.value;
        }

        RangeEntry newEntry = new RangeEntry(e);
        prev.rightChild = newEntry;
        prev.rightChild.parent = prev;

        if (this.biggest.value.getMinimum().compareTo(e.getMinimum()) < 0) {
            this.biggest = newEntry;
        }
        this.internalList.add(e);
        ++this.size;
        return null;
    }

    private Range<E> addAsPrevLeft(Range<E> e, RangeEntry prev) {
        if (e.intersects(prev.value)) {
            return prev.value;
        }

        RangeEntry newEntry = new RangeEntry(e);
        prev.leftChild = newEntry;
        prev.leftChild.parent = prev;

        if (this.smallest.value.getMinimum().compareTo(e.getMinimum()) > 0) {
            this.smallest = newEntry;
        }
        this.internalList.add(e);
        ++this.size;
        return null;
    }

    private class RangeEntry implements Serializable {
        private static final long serialVersionUID = 7595589083919072971L;

        private final Range<E> value;
        private RangeEntry leftChild;
        private RangeEntry rightChild;

        @SuppressWarnings("unused")
        private RangeEntry parent;

        public RangeEntry(Range<E> value) {
            this.value = value;
            this.leftChild = null;
            this.rightChild = null;
            this.parent = null;
        }

    }

}
