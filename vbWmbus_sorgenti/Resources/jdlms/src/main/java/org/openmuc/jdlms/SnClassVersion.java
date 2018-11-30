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

import java.io.Serializable;

/**
 * This class represents a tuple of a class ID and version of a IC.
 * 
 * @see SnObjectInfo
 * @see SnClassInfo
 */
public class SnClassVersion implements Serializable {

    private static final long serialVersionUID = 6213345737286138413L;

    private final int classId;
    private final int version;

    /**
     * Construct a class ID version pair.
     * 
     * @param classId
     *            the class ID.
     * @param version
     *            the version.
     */
    public SnClassVersion(int classId, int version) {
        this.classId = classId;
        this.version = version;
    }

    /**
     * Get the class ID.
     * 
     * @return the class ID.
     */
    public int getClassid() {
        return this.classId;
    }

    /**
     * Get the version.
     * 
     * @return the version.
     */
    public int getVersion() {
        return this.version;
    }

    @Override
    public int hashCode() {
        int h1 = this.classId;
        int h2 = this.version;
        return (h1 + h2) * h2 + h1;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof SnClassVersion)) {
            return false;
        }

        SnClassVersion other = (SnClassVersion) obj;

        return this.classId == other.classId && this.version == other.version;
    }

    @Override
    public String toString() {
        return String.format("{\"class-id\": %d, \"version\": %d}", classId, version);
    }
}
