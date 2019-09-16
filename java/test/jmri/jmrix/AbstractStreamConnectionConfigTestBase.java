package jmri.jmrix;

import jmri.util.junit.annotations.*;
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
    @ToDo("modify Stream port Connections so they load details, then remove this test so parent class test can run or re-implement the test here")
    @Override
    public void testLoadDetails(){
    }
}
