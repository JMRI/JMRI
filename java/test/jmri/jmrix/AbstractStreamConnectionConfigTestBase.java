package jmri.jmrix;

import org.junit.*;
import javax.swing.JPanel;

/**
 * Base tests for StreamConnectionConfig objects.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractStreamConnectionConfigTestBase extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Test
    @Ignore("Stream connections don't (currently) load details")
    public void testLoadDetails(){
    }
}
