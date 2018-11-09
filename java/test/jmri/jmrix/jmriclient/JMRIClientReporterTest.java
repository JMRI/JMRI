package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JMRIClientReporterTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientReporter class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientReporterTest extends jmri.implementation.AbstractReporterTestBase{

    @Override
    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JMRIClientTrafficController tc = new JMRIClientTrafficController() {
            @Override
            public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
                // do nothing to avoid null pointer when sending to non-existant
                // connection during test.
            }
        };
        r = new JMRIClientReporter(3, new JMRIClientSystemConnectionMemo(tc));
    }

    @Override
    public void tearDown() {
	r = null;
        JUnitUtil.tearDown();
    }

}
