package jmri.jmrix;

import org.junit.jupiter.api.*;

/**
 * Base tests for UsbConnectionConfig objects.
 *
 * @author Paul Bender Copyright (C) 2018
 */
abstract public class AbstractUsbConnectionConfigTestBase extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Test
    @Override
    public void testLoadDetails(){
       // don't try to load details on connections that use libusb, since it
       // is not always available.
    }
}
