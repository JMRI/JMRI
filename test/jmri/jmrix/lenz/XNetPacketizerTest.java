package jmri.jmrix.lenz;

import junit.framework.*;
import apps.tests.*;

/**
 * <p>Title: XNetPacketizerTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.1 $
 */
public class XNetPacketizerTest extends TestCase {
  Log4JFixture log4jfixtureInst = new Log4JFixture(this);

  public XNetPacketizerTest(String s) {
    super(s);
  }

  protected void setUp() {
    log4jfixtureInst.setUp();
  }

  protected void tearDown() {
    log4jfixtureInst.tearDown();
  }

  public void testDummy() {}

}
