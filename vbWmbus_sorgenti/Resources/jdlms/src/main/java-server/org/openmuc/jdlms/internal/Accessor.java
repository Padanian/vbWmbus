package org.openmuc.jdlms.internal;

public interface Accessor {
    AccessorType getAccessorType();

    enum AccessorType {
        ATTRIBUTE,
        METHOD
    }

}
