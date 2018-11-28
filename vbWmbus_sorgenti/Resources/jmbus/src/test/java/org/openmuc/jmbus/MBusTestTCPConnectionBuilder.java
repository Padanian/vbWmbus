package org.openmuc.jmbus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openmuc.jmbus.MBusConnection.MBusTcpBuilder;
import org.openmuc.jmbus.transportlayer.TransportLayer;

public class MBusTestTCPConnectionBuilder extends MBusTcpBuilder {

    private final ByteArrayOutputStream os;
    private final ByteArrayInputStream is;

    protected MBusTestTCPConnectionBuilder(String hostAddress, int port, final ByteArrayInputStream is,
            final ByteArrayOutputStream os) throws IOException {
        super(hostAddress, port);
        this.os = os;
        this.is = is;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // check if a short request message has been sent
                    if (os.size() == 5) {
                        synchronized (is) {
                            // simulate a correct response message
                            is.reset();
                            is.notifyAll();
                        }
                        break;
                    }
                }
            }
        }).start();

    }

    @Override
    protected TransportLayer buildTransportLayer() {
        DataInputStream blockingInputStream = new DataInputStream(is) {
            @Override
            public int read() throws IOException {
                int value = super.read();
                // block InputStream while empty
                if (value < 0) {
                    try {
                        synchronized (is) {
                            is.wait();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    value = super.read();
                }
                return value;
            }
        };
        return new MBusTestTCPLayer(blockingInputStream, new DataOutputStream(os));
    }

}
