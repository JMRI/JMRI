
package jmri.jmrix.loconet;

import junit.framework.*;
import jmri.*;


public class Se8AlmImplementationTest extends TestCase {

    public Se8AlmImplementationTest(String s) {
	super(s);
    }

    public void testPage() {
        Se8AlmImplementation alm = new Se8AlmImplementation(2,true);
        alm.contents[0][0]=3;  // page 4, index 3
        alm.contents[1][0]=12;
        alm.contents[2][0]=13;
        alm.contents[3][0]=14;
        alm.contents[4][0]=15;
        alm.contents[5][0]=16;
        alm.contents[6][0]=17;
        alm.contents[7][0]=18;

        alm.contents[0][16]=6; // page 6, index 5
        alm.contents[1][16]=162;

        Assert.assertEquals("page register 1",
			    0,   // page registers always on 0
			    alm.page(0,0));
        Assert.assertEquals("block not at zero",
			    3,
			    alm.page(1,0));

        Assert.assertEquals("page register 2",
			    0,   // page registers always on 0
			    alm.page(4,0));
        Assert.assertEquals("block not at zero",
			    6,
			    alm.page(5,0));
    }

    /**
     * Test read and write operations on the default page
     */
    public void testRW() {
        Se8AlmImplementation alm = new Se8AlmImplementation(2,true);
        alm.contents[0][0]=0;  // page 1, index 0
        alm.contents[0][1]=11;
        alm.contents[0][2]=12;
        alm.contents[0][3]=13;
        alm.contents[0][4]=14;
        alm.contents[0][5]=15;
        alm.contents[0][6]=16;
        alm.contents[0][7]=17;
        Assert.assertEquals("read 1",
			    0,
			    alm.retrieve(0,0));
        Assert.assertEquals("read 2",
                            11,
			    alm.retrieve(0,1));
        Assert.assertEquals("read 3",
			    12,
			    alm.retrieve(0,2));
        Assert.assertEquals("read 4",
			    13,
			    alm.retrieve(0,3));
        Assert.assertEquals("read 5",
			    14,
			    alm.retrieve(1,0));
        Assert.assertEquals("read 6",
			    15,
			    alm.retrieve(1,1));
        Assert.assertEquals("read 7",
			    16,
			    alm.retrieve(1,2));
        Assert.assertEquals("read 8",
			    17,
			    alm.retrieve(1,3));
    }

    /**
     * Test read and write operations on alternate pages
     */
    public void testRWpaged() {
        Se8AlmImplementation alm = new Se8AlmImplementation(2,true);

        alm.contents[0][0]=0;  // start on page 1, index 0
        alm.contents[0][1]=11;
        alm.contents[0][2]=12;
        alm.contents[0][3]=13;
        alm.contents[0][4]=14;
        alm.contents[0][5]=15;
        alm.contents[0][6]=16;
        alm.contents[0][7]=17;

        alm.contents[1][0]=0;
        alm.contents[1][1]=111;
        alm.contents[1][2]=112;
        alm.contents[1][3]=113;
        alm.contents[1][4]=114;
        alm.contents[1][5]=115;
        alm.contents[1][6]=116;
        alm.contents[1][7]=117;

        Assert.assertEquals("read 1 page 0",
			    0,     // page 1, index 0
			    alm.retrieve(0,0));
        Assert.assertEquals("read 2 page 0",
                            11,
			    alm.retrieve(0,1));

        alm.store(0,0,1);  // store page as number 2, which goes to index 1

        Assert.assertEquals("read 1 page 1",
			    1,   // page value
			    alm.retrieve(0,0));
        Assert.assertEquals("read 2 page 1",
                            111,
			    alm.retrieve(0,1));
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    LocoNetInterfaceScaffold lnis;
    protected void setUp() {
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        log4jfixtureInst.setUp();
    }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
