/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.app;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.WMBusConnection;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;

class WMBusStart {
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    public static void wmbus(final WMBusConnection wmBusConnection) throws IOException {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (wmBusConnection == null) {
                    return;
                }
                try {
                    wmBusConnection.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        });

    }

    public static class WMBusReceiver implements WMBusListener {
        private final CliPrinter cliPrinter;

        public WMBusReceiver(CliPrinter cliPrinter) {
            this.cliPrinter = cliPrinter;
        }

        @Override
        public void newMessage(WMBusMessage message) {
            this.cliPrinter
                    .printlnInfo(MessageFormat.format("\n\n{0} ------- New Message -------", DF.format(new Date())));
            this.cliPrinter.printlnDebug("Message Bytes: ", message.asBlob());

            try {
                message.getVariableDataResponse().decode();
            } catch (DecodingException e) {
                this.cliPrinter.printlnInfo("Unable to fully decode received message:\n", e.getMessage());
            }

            this.cliPrinter.printInfo(MessageFormat.format("{0}\n", message));
        }

        @Override
        public void discardedBytes(byte[] bytes) {
            this.cliPrinter.printlnInfo("Bytes discarded: ", bytes);
            this.cliPrinter.printlnInfo();
        }

        @Override
        public void stoppedListening(IOException e) {
            this.cliPrinter.printlnInfo("Stopped listening for new messages because: ", e.getMessage());
        }

    }

    private WMBusStart() {
        // hide this
    }

}
