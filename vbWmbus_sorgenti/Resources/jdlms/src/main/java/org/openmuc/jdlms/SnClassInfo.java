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
package org.openmuc.jdlms;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.openmuc.jdlms.internal.Range;
import org.openmuc.jdlms.internal.RangeSet;

/**
 * Interface class (IC) info used to enable the mapping between attribute or method ID and SN offset.
 * 
 * This class is only applicable if SN referencing is used.
 * 
 * @see ConnectionBuilder#setSnClassInfo(java.util.Collection)
 * @see SnObjectInfo
 */
public class SnClassInfo implements Serializable {

    private static final long serialVersionUID = 4113751726642331620L;

    private final RangeSet<Integer> attributes;
    private final RangeSet<Integer> methods;
    private final SnClassVersion snClassVersion;

    /**
     * Construct a new SN class info.
     * 
     * @param classId
     *            the class ID.
     * @param version
     *            the version.
     * @param methodRange
     *            the SN offset range in which the methods lie.
     */
    public SnClassInfo(int classId, int version, SnMemberRange methodRange) {
        this(new SnClassVersion(classId, version), methodRange);
    }

    /**
     * Construct a collection of SN class info from more or more COSEM SN interface classes.
     * 
     * @param ic
     *            one or multiple SN interface classes.
     * @return a collection of SN class infos.
     */
    @SafeVarargs
    public static Collection<SnClassInfo> mapIcToClassInfo(Class<? extends CosemSnInterfaceObject>... ic) {
        return mapIcToClassInfo(Arrays.asList(ic));
    }

    /**
     * Construct a collection of SN class info from multiple COSEM SN interface classs.
     * 
     * @param ics
     *            one or multiple SN interface classes.
     * @return a collection of SN class infos.
     */
    public static Collection<SnClassInfo> mapIcToClassInfo(Collection<Class<? extends CosemSnInterfaceObject>> ics) {
        List<SnClassInfo> result = new ArrayList<>(ics.size());

        for (Class<? extends CosemSnInterfaceObject> ic : ics) {
            SnClassInfo snClassInfo = new SnClassInfo(ic);
            result.add(snClassInfo);
        }
        return result;
    }

    private SnClassInfo(Class<? extends CosemSnInterfaceObject> cosemInterface) {
        CosemClass cosemClass = cosemInterface.getAnnotation(CosemClass.class);

        if (cosemClass == null) {
            throw new IllegalArgumentException("COSEM IC must be annotated with CosemClass.");
        }

        this.snClassVersion = new SnClassVersion(cosemClass.id(), cosemClass.version());

        this.attributes = new RangeSet<>();
        this.methods = new RangeSet<>();

        Set<CosemSnMember> m = new HashSet<>();
        m.add(new CosemSnMember(0, 1, CosemSnMember.MemberType.ATTRIBUTE));

        addFieldsToSet(cosemInterface, m);
        addMethodsToSet(cosemInterface, m);

        setUpAttributesAndMethodsStruct(m);
    }

    /**
     * Construct a new SN class info.
     * 
     * @param snClassVersion
     *            the (class ID, version) tuple.s
     * @param methodRange
     *            the SN offset range in which the methods lie.
     */
    public SnClassInfo(SnClassVersion snClassVersion, SnMemberRange methodRange) {
        this(snClassVersion, emptyList(), Arrays.asList(methodRange));
    }

    /**
     * Construct a new SN class info.
     * 
     * @param classId
     *            the class ID.
     * @param version
     *            the version.
     * @param attributeRange
     *            the SN offset range in which the attributes lie.
     * @param methodRange
     *            the SN offset range in which the methods lie.
     */
    public SnClassInfo(int classId, int version, SnMemberRange attributeRange, SnMemberRange methodRange) {
        this(new SnClassVersion(classId, version), attributeRange, methodRange);
    }

    /**
     * Construct a new SN class info.
     * 
     * @param snClassVersion
     *            the (class ID, version) tuple.
     * @param attributeRange
     *            the SN offset range in which the attributes lie.
     * @param methodRange
     *            the SN offset range in which the methods lie.
     */
    public SnClassInfo(SnClassVersion snClassVersion, SnMemberRange attributeRange, SnMemberRange methodRange) {
        this(snClassVersion, Arrays.asList(attributeRange), Arrays.asList(methodRange));
    }

    /**
     * Construct a new SN class info.
     * 
     * @param classId
     *            the class ID.
     * @param version
     *            the version.
     * @param attributeRanges
     *            a collection of SN offset range in which the attributes lie.
     * @param methodRanges
     *            a collection of the SN offset range in which the methods lie.
     */
    public SnClassInfo(int classId, int version, Collection<SnMemberRange> attributeRanges,
            List<SnMemberRange> methodRanges) {
        this(new SnClassVersion(classId, version), attributeRanges, methodRanges);
    }

    /**
     * Construct a new SN class info.
     * 
     * @param snClassVersion
     *            the (class ID, version) tuple.
     * @param attributeRanges
     *            a collection of SN offset range in which the attributes lie.
     * @param methodRanges
     *            a collection of the SN offset range in which the methods lie.
     */
    public SnClassInfo(SnClassVersion snClassVersion, Collection<SnMemberRange> attributeRanges,
            List<SnMemberRange> methodRanges) {
        this.snClassVersion = snClassVersion;

        RangeSet<Integer> rSet = new RangeSet<>();
        this.attributes = new RangeSet<>();
        this.methods = new RangeSet<>();

        List<SnMemberRange> attributeRangesSorted = sort(attributeRanges);
        List<SnMemberRange> methodRangesSorted = sort(methodRanges);

        int numRPrevAttributes = firstOffsetFrom(attributeRangesSorted);

        for (SnMemberRange range : attributeRangesSorted) {
            checkIfValHasExisted(rSet, range);

            numRPrevAttributes = addRangeToSet(this.attributes, numRPrevAttributes, range);
        }

        int numRPrevMethods = firstOffsetFrom(methodRangesSorted);
        for (SnMemberRange range : methodRangesSorted) {
            checkIfValHasExisted(rSet, range);
            numRPrevMethods = addRangeToSet(this.methods, numRPrevMethods, range);
        }

    }

    private static List<SnMemberRange> emptyList() {
        return Collections.emptyList();
    }

    private static int firstOffsetFrom(List<SnMemberRange> lSorted) {
        if (!lSorted.isEmpty()) {
            int firstId = lSorted.get(0).getFirstRangeId();
            return firstId == -1 ? 0 : firstId - 1;
        }
        else {
            return 0;
        }
    }

    private void checkIfValHasExisted(RangeSet<Integer> rSet, SnMemberRange range) {
        if (rSet.add(range) != null) {
            throw new IllegalArgumentException("Ranges intersect.");
        }
    }

    private static int addRangeToSet(RangeSet<Integer> rSet, int num, SnMemberRange range) {

        int startId = num + 1;
        if (range.getFirstRangeId() != -1) {
            startId = range.getFirstRangeId();
        }

        int endId = startId + ((range.getMaximum() - range.getMinimum()) / 0x08);

        SnIdEntry e = rSet.add(new SnIdEntry(startId, endId, range.getMinimum()));

        if (e != null) {
            // TODO should no happen. fatal bug.
        }
        return num + endId - startId + 1;
    }

    private static List<SnMemberRange> sort(Collection<SnMemberRange> list) {
        List<SnMemberRange> sorted = new ArrayList<>(list);
        Collections.sort(sorted, new Comparator<SnMemberRange>() {

            @Override
            public int compare(SnMemberRange o1, SnMemberRange o2) {
                return o1.getMaximum().compareTo(o2.getMinimum());
            }

        });
        return sorted;
    }

    /**
     * Compute the short name offset for a given attribute ID.
     * 
     * @param attributeId
     *            the ID of the attribute in COSEM class.
     * @return the short name offset.
     */
    public int computeAttributeSnOffsetFor(int attributeId) {
        int snOffset = calculateRangeSnOffset(attributeId, this.attributes);

        if (snOffset == -1) {
            snOffset = (attributeId - 1) * 0x08;
        }

        return snOffset;
    }

    /**
     * Compute the short name offset for a given method ID.
     * 
     * @param methodId
     *            the ID of the attribute in COSEM class.
     * @return the short name offset.
     */
    public int computeMethodSnOffsetFor(int methodId) {
        return calculateRangeSnOffset(methodId, this.methods);
    }

    private static int calculateRangeSnOffset(int memberId, RangeSet<Integer> r) {
        SnIdEntry m = r.getIntersectingRange(memberId);

        if (m == null) {
            return -1;
        }

        return m.rangeSnOffset + (memberId - m.getMinimum()) * 0x08;
    }

    /**
     * Get the (class ID, version) tuple.
     * 
     * @return (class ID, version) tuple.
     */
    public SnClassVersion getSnClassVersion() {
        return snClassVersion;
    }

    @Override
    public String toString() {
        return String.format("{%n \"class-version\": %s,%n \"attributes\": %s,%n \"methods\": %s%n}", snClassVersion,
                attributes, methods);
    }

    private void setUpAttributesAndMethodsStruct(Set<CosemSnMember> m) {
        ArrayList<CosemSnMember> list = new ArrayList<>(m);
        Collections.sort(list);

        ListIterator<CosemSnMember> iter = list.listIterator();
        while (iter.hasNext()) {
            CosemSnMember first = iter.next();

            CosemSnMember.MemberType memberType = first.getMemberType();
            int firstId = first.getId();
            int lastId = firstId;

            int firstOffset = first.getSnOffset();

            CosemSnMember prev = first;
            while (iter.hasNext()) {
                CosemSnMember next = iter.next();
                int expectedOffset = prev.getSnOffset() + 0x08;
                if (next.getMemberType() != first.getMemberType() || expectedOffset != next.getSnOffset()
                        || prev.getId() + 1 != next.getId()) {
                    // jump back
                    iter.previous();
                    break;
                }
                prev = next;
                lastId = next.getId();
            }

            SnIdEntry range = new SnIdEntry(firstId, lastId, firstOffset);

            if (memberType == CosemSnMember.MemberType.ATTRIBUTE) {
                this.attributes.add(range);
            }
            else {
                this.methods.add(range);
            }
        }
    }

    private static void addMethodsToSet(Class<? extends CosemSnInterfaceObject> c, Set<CosemSnMember> m) {
        for (Method method : c.getDeclaredMethods()) {
            CosemMethod cosemMethod = method.getAnnotation(CosemMethod.class);
            if (cosemMethod == null || cosemMethod.snOffset() == -1) {
                continue;
            }
            int snOffset = cosemMethod.snOffset();
            checkOffset(snOffset);
            boolean r = m.add(new CosemSnMember(snOffset, cosemMethod.id(), CosemSnMember.MemberType.METHOD));
            checkIfMemberHasExisted(r, snOffset);
        }
    }

    private static void addFieldsToSet(Class<? extends CosemSnInterfaceObject> c, Set<CosemSnMember> m) {
        for (Field field : c.getDeclaredFields()) {
            CosemAttribute cosemAttribute = field.getAnnotation(CosemAttribute.class);
            if (cosemAttribute == null || cosemAttribute.snOffset() == -1) {
                continue;
            }
            int snOffset = cosemAttribute.snOffset();

            checkOffset(snOffset);

            boolean r = m.add(new CosemSnMember(snOffset, cosemAttribute.id(), CosemSnMember.MemberType.ATTRIBUTE));
            checkIfMemberHasExisted(r, snOffset);
        }
    }

    private static void checkOffset(int snOffset) {
        if (snOffset % 0x08 != 0) {
            throw new IllegalArgumentException("SN offsets must be a multiple of 0x08.");
        }
    }

    private static void checkIfMemberHasExisted(boolean r, int offset) {
        if (!r) {
            throw new IllegalArgumentException(String.format("Member with SN offset 0x%02X is not unique.", offset));
        }
    }

    private static class CosemSnMember implements Comparable<CosemSnMember> {
        public enum MemberType {
            METHOD,
            ATTRIBUTE
        }

        private final int snOffset;
        private final int id;
        private final MemberType memberType;

        public CosemSnMember(int snOffset, int id, MemberType memberType) {
            this.snOffset = snOffset;
            this.id = id;
            this.memberType = memberType;
        }

        public int getId() {
            return id;
        }

        public MemberType getMemberType() {
            return memberType;
        }

        public int getSnOffset() {
            return snOffset;
        }

        @Override
        public int compareTo(CosemSnMember o) {
            return Integer.compare(this.snOffset, o.snOffset);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CosemSnMember)) {
                return false;
            }
            CosemSnMember other = (CosemSnMember) obj;

            return other.snOffset == this.snOffset;
        }

        @Override
        public int hashCode() {
            return this.snOffset;
        }

        @Override
        public String toString() {
            return String.format("{\"id\": %d, \"offset\": 0x%02X, \"member-type\": \"%s\"}", this.id, this.snOffset,
                    this.memberType);
        }

    }

    private static class SnIdEntry extends Range<Integer> {

        private static final long serialVersionUID = -2092724143767438287L;

        private final int rangeSnOffset;

        public SnIdEntry(Integer fromInclusive, Integer toInclusive, int snOffset) {
            super(fromInclusive, toInclusive);
            this.rangeSnOffset = snOffset;
        }

        @Override
        public String toString() {
            return String.format("{\"range\": [%d, %d], \"snOffset\": \"0x%02X\"}", getMinimum(), getMaximum(),
                    this.rangeSnOffset);
        }

    }

}
