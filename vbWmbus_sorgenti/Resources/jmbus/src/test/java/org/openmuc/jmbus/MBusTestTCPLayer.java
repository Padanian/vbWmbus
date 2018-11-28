package org.openmuc.jmbus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openmuc.jmbus.transportlayer.TransportLayer;

public class MBusTestTCPLayer implements TransportLayer {

    private final DataInputStream is;
    private final DataOutputStream os;
    private boolean closed = true;
    private int timeout = 1000;

    public MBusTestTCPLayer(DataInputStream is, DataOutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void open() throws IOException {
        closed = false;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public DataOutputStream getOutputStream() {
        return os;
    }

    @Override
    public DataInputStream getInputStream() {
        return is;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        this.timeout = timeout;
    }

    @Override
    public int getTimeout() throws IOException {
        return timeout;
    }

}
