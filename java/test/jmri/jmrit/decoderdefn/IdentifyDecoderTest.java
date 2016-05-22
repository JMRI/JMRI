package jmri.jmrit.decoderdefn;

import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * IdentifyDecoderTest.java
 *
 * Description:	    tests for the jmrit.roster.IdentifyDecoder class
 * @author			Bob Jacobsen
 * @version			$Revision$
 */
public class IdentifyDecoderTest extends TestCase {

        static int cvRead = -1;

        public void testIdentifyStandard() {
                // initialize the system
                jmri.progdebugger.ProgDebugger p = new jmri.progdebugger.ProgDebugger() {
                        public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
                                cvRead = CV;
                        }
                };
                jmri.InstanceManager.setProgrammerManager(new jmri.managers.DefaultProgrammerManager(p));

                // create our test object
                IdentifyDecoder i = new IdentifyDecoder() {
                        public void done(int mfgID, int modelID, int productID) {}
                        public void message(String m) {}
                        public void error() {}
                };

                i.start();
                Assert.assertEquals("step 1 reads CV ", 8, cvRead);
                Assert.assertEquals("running after 1 ", true, i.isRunning());

                // simulate CV read complete
                i.programmingOpReply(0x12, 0);
                Assert.assertEquals("step 2 reads CV ", 7, cvRead);
                Assert.assertEquals("running after 2 ", true, i.isRunning());

                // simulate CV read complete, ending check
                i.programmingOpReply(123, 0);
                Assert.assertEquals("running after 2 ", false, i.isRunning());
                Assert.assertEquals("found mfg ID ", 0x12, i.mfgID);
                Assert.assertEquals("found model ID ", 123, i.modelID);

        }

        public void testIdentifyHarman() {
                // initialize the system
                jmri.progdebugger.ProgDebugger p = new jmri.progdebugger.ProgDebugger() {
                        public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
                                cvRead = CV;
                        }
                };
                jmri.InstanceManager.setProgrammerManager(new jmri.managers.DefaultProgrammerManager(p));

                // create our test object
                IdentifyDecoder i = new IdentifyDecoder() {
                        public void done(int mfgID, int modelID, int productID) {}
                        public void message(String m) {}
                        public void error() {}
                };

                i.start();
                Assert.assertEquals("step 1 reads CV ", 8, cvRead);
                Assert.assertEquals("running after 1 ", true, i.isRunning());

                // simulate CV read complete on CV8, start 7
                i.programmingOpReply(98, 0);
                Assert.assertEquals("step 2 reads CV ", 7, cvRead);
                Assert.assertEquals("running after 2 ", true, i.isRunning());

                // simulate CV read complete on CV7, start 112
                i.programmingOpReply(123, 0);
                Assert.assertEquals("step 3 reads CV ", 112, cvRead);
                Assert.assertEquals("running after 3 ", true, i.isRunning());

                // simulate CV read complete on CV7, does 113 and ends
                i.programmingOpReply(0xAB, 0);
                Assert.assertEquals("step 4 reads CV ", 113, cvRead);
                Assert.assertEquals("running after 4 ", true, i.isRunning());
                
                // simulate CV read complete on CV113, ends
                i.programmingOpReply(0xCD, 0);
                Assert.assertEquals("running after 5 ", false, i.isRunning());

                Assert.assertEquals("found mfg ID ", 98, i.mfgID);
                Assert.assertEquals("found model ID ", 123, i.modelID);
                Assert.assertEquals("found product ID ", 0xABCD, i.productID);

        }

        // from here down is testing infrastructure

        public IdentifyDecoderTest(String s) {
                super(s);
        }

        // Main entry point
        static public void main(String[] args) {
                String[] testCaseName = {IdentifyDecoderTest.class.getName()};
                junit.swingui.TestRunner.main(testCaseName);
        }

        // test suite from all defined tests
        public static Test suite() {
                TestSuite suite = new TestSuite(IdentifyDecoderTest.class);
                return suite;
        }
        public void setUp() throws Exception {
            super.setUp();
            jmri.util.JUnitUtil.resetInstanceManager();
        }
}
