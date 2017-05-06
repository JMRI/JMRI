package jmri.swing;

import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import javax.swing.JFrame;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrix.lenz.lv102.ConnectionLabel class
 *
 * @author	Bob Jacobsen Copyright (c) 2001, 2002
 */
public class ConnectionLabelTest {

    private jmri.jmrix.ConnectionConfig config = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConnectionLabel action = new ConnectionLabel(config);
        Assert.assertNotNull(action);
    }

    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       jmri.util.JUnitUtil.resetInstanceManager();
       config = new jmri.jmrix.AbstractConnectionConfig(){
          @Override
          protected void checkInitDone(){
          }
          @Override
          public void updateAdapter(){
          }
          @Override
          protected void setInstance(){
          }
          @Override
          public String getInfo(){
             return "foo";
          }
          @Override
          public void loadDetails(final javax.swing.JPanel details){
          }
          @Override
          protected void showAdvancedItems(){
          }
          @Override
          public String getManufacturer(){
             return "foo";
          }

          @Override
          public void setManufacturer(String manufacturer){
          }
          @Override
          public String getConnectionName(){
             return "bar";
          }
          @Override
          public boolean getDisabled(){
             return false;
          }
          @Override
          public void setDisabled(boolean disabled){
          }
          @Override
          public jmri.jmrix.PortAdapter getAdapter() {
              return null;
          }
          @Override
          public String name(){ 
             return "bar";
          }
       };
    }

    @After
    public void tearDown(){
       jmri.util.JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
