package jmri.jmrix;

import org.junit.*;
import javax.swing.JPanel;
import jmri.util.ThreadingUtil;

/**
 * Base tests for SerialConnectionConfig objects.
 *
 * @author Paul Bender Copyright (C) 2018	
 */
abstract public class AbstractSerialConnectionConfigTestBase extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Test
    @Override
    public void testLoadDetails(){
        ThreadingUtil.runOnGUI(() -> {
            // verify no exceptions thrown
            cc.loadDetails(new JPanel());
            // load details MAY produce an error message if no ports are found.
            jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        });
    }
}
