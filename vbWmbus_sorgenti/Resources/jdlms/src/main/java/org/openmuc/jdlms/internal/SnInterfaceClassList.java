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
package org.openmuc.jdlms.internal;

import java.util.HashSet;
import java.util.Set;

import org.openmuc.jdlms.SnClassInfo;
import org.openmuc.jdlms.SnMemberRange;

/**
 * List of all supported COSEM interface classes
 */
public class SnInterfaceClassList {

    private static Set<SnClassInfo> s;

    public static Set<SnClassInfo> getDefaultSnObjects() {
        return s;
    }

    static {
        s = new HashSet<>();

        // Register class
        s.add(new SnClassInfo(3, 0, SnMemberRange.from(0x28)));

        // Extended register class
        s.add(new SnClassInfo(4, 0, SnMemberRange.from(0x38)));

        // Demand register class
        s.add(new SnClassInfo(5, 0, SnMemberRange.between(0x48, 0x50)));

        // Register activation class
        s.add(new SnClassInfo(6, 0, SnMemberRange.from(0x30)));

        // Profile generic class
        s.add(new SnClassInfo(7, 1, SnMemberRange.from(0x58)));

        // Clock class
        s.add(new SnClassInfo(8, 0, SnMemberRange.between(0x60, 0x88)));

        // Script table class
        s.add(new SnClassInfo(9, 0, SnMemberRange.from(0x20)));

        // Schedule class
        s.add(new SnClassInfo(10, 0, SnMemberRange.between(0x20, 0x30)));

        // Special days table class
        s.add(new SnClassInfo(11, 0, SnMemberRange.between(0x10, 0x18)));

        // Activity calendar class
        s.add(new SnClassInfo(20, 0, SnMemberRange.is(0x50)));

        // Association SN class
        s.add(new SnClassInfo(12, 0, SnMemberRange.between(0x20, 0x58)));
        s.add(new SnClassInfo(12, 1, SnMemberRange.between(0x30, 0x58, 3)));
        s.add(new SnClassInfo(12, 2, SnMemberRange.between(0x30, 0x58, 3)));
        s.add(new SnClassInfo(12, 3, SnMemberRange.between(0x30, 0x68, 3)));
        s.add(new SnClassInfo(12, 4, SnMemberRange.between(0x30, 0x68, 3)));

        // SAP assignment class
        s.add(new SnClassInfo(17, 0, SnMemberRange.is(0x20)));

        // image transfer class
        s.add(new SnClassInfo(18, 0, SnMemberRange.between(0x40, 0x58)));

        // -------------------NO-METHODS--------------------

        // Data class

        // Register monitor class

        // Utilities table class id:26, v:0

        // Single action schedule class id:22, v:0

        // Status mapping class id:63, v:0

        // IEC local port setup class id:19, v:0,1

        // Modem configuration class id:27, v:0,1

        // Auto answer class id:28, v:0

        // PSTN auto dial class id:29, v:0

        // Auto connect class id:29, v:1

        // IEC HDLC setup class id:23, v:0,1

        // IEC twisted pair setup class id:24, v:0

        // TCP-UDP setup class id:41, v:0

        // / PPP setup class id:44, v:0

        // GPRS modem setup class id:45, v:0

        // SMTP setup class id:46, v:0

        // -------------------------------------------------

        // push setup id:40, v:0
        s.add(new SnClassInfo(40, 0, SnMemberRange.from(0x38)));

        // Register table class
        s.add(new SnClassInfo(61, 0, SnMemberRange.between(0x28, 0x30)));

        // security setup
        s.add(new SnClassInfo(64, 0, SnMemberRange.between(0x28, 0x30)));
        s.add(new SnClassInfo(64, 1, SnMemberRange.between(0x30, 0x70)));

        // parameter monitor
        s.add(new SnClassInfo(65, 0, SnMemberRange.from(0x20)));

        // sensor manager
        s.add(new SnClassInfo(67, 0, SnMemberRange.is(0x80)));

        // disconnect control
        s.add(new SnClassInfo(70, 0, SnMemberRange.between(0x20, 0x28)));

        // m-bus client
        s.add(new SnClassInfo(72, 0, SnMemberRange.between(0x60, 0x98)));
        s.add(new SnClassInfo(72, 1, SnMemberRange.between(0x70, 0xA8)));

        // IPv4 setup class
        s.add(new SnClassInfo(42, 0, SnMemberRange.between(0x60, 0x70)));

    }

    private SnInterfaceClassList() {
    }

}
