package jmri.jmrit.operations.setup;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SetupTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Setup t = new Setup();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testPrefixLength() {
        Setup.setSwitchListFormatSameAsManifest(false);
        
        Assert.assertEquals("Default length", 12, Setup.getManifestPrefixLength());
        Assert.assertEquals("Default length", 12, Setup.getSwitchListPrefixLength());
        
        Setup.setDropEnginePrefix("ABCDEFGHIJKLM"); // now 13 characters
        Assert.assertEquals("new length 1", 13, Setup.getManifestPrefixLength());
        Assert.assertEquals("new length 1", 13, Setup.getSwitchListPrefixLength());
        
        Setup.setDropCarPrefix("ABCDEFGHIJKLMN"); // now 14 characters
        Assert.assertEquals("new length 2", 14, Setup.getManifestPrefixLength());
        Assert.assertEquals("new length 2", 13, Setup.getSwitchListPrefixLength());
        
        Setup.setPickupCarPrefix("ABCDEFGHIJKLMNO"); // now 15 characters
        Assert.assertEquals("new length 3", 15, Setup.getManifestPrefixLength());
        Assert.assertEquals("new length 3", 13, Setup.getSwitchListPrefixLength());
        
        Setup.setLocalPrefix("ABCDEFGHIJKLMNOP"); // now 16 characters
        Assert.assertEquals("new length 4", 16, Setup.getManifestPrefixLength());
        Assert.assertEquals("new length 4", 13, Setup.getSwitchListPrefixLength());
        
        Setup.setSwitchListDropCarPrefix("ABCDEFGHIJKLMNOPQ"); // now 17 characters
        Assert.assertEquals("new length 5", 16, Setup.getManifestPrefixLength());
        Assert.assertEquals("new length 5", 17, Setup.getSwitchListPrefixLength());
        
        Setup.setSwitchListPickupCarPrefix("ABCDEFGHIJKLMNOPQR"); // now 18 characters
        Assert.assertEquals("new length 6", 16, Setup.getManifestPrefixLength());
        Assert.assertEquals("new length 6", 18, Setup.getSwitchListPrefixLength());
        
        Setup.setSwitchListLocalPrefix("ABCDEFGHIJKLMNOPQRS"); // now 19 characters
        Assert.assertEquals("new length 7", 16, Setup.getManifestPrefixLength());
        Assert.assertEquals("new length 7", 19, Setup.getSwitchListPrefixLength());
        
        Setup.setSwitchListFormatSameAsManifest(true);
        Assert.assertEquals("confirm length", 16, Setup.getSwitchListPrefixLength());
    }
    
    @Test
    public void testDropTruncatedMessageFormat() {
        Assert.assertEquals("default message", 10, Setup.getDropTruncatedManifestMessageFormat().length);
        // confirm
        Assert.assertEquals("attribute 1", Setup.ROAD, Setup.getDropTruncatedManifestMessageFormat()[0]);
        Assert.assertEquals("attribute 2", Setup.NUMBER, Setup.getDropTruncatedManifestMessageFormat()[1]);
        Assert.assertEquals("attribute 3", Setup.TYPE, Setup.getDropTruncatedManifestMessageFormat()[2]);
        Assert.assertEquals("attribute 4", Setup.LENGTH, Setup.getDropTruncatedManifestMessageFormat()[3]);
        Assert.assertEquals("attribute 5", Setup.COLOR, Setup.getDropTruncatedManifestMessageFormat()[4]);
        Assert.assertEquals("attribute 7", Setup.LOAD, Setup.getDropTruncatedManifestMessageFormat()[5]);
        Assert.assertEquals("attribute 6", Setup.HAZARDOUS, Setup.getDropTruncatedManifestMessageFormat()[6]);
        Assert.assertEquals("attribute 7", Setup.NO_DESTINATION, Setup.getDropTruncatedManifestMessageFormat()[7]);
        Assert.assertEquals("attribute 8", Setup.COMMENT, Setup.getDropTruncatedManifestMessageFormat()[8]);
        Assert.assertEquals("attribute 9", Setup.DROP_COMMENT, Setup.getDropTruncatedManifestMessageFormat()[9]);
    }
    
    @Test
    public void testPickupTruncatedMessageFormat() {
        Assert.assertEquals("default message", 10, Setup.getPickupTruncatedManifestMessageFormat().length);
        // confirm
        Assert.assertEquals("attribute 1", Setup.ROAD, Setup.getPickupTruncatedManifestMessageFormat()[0]);
        Assert.assertEquals("attribute 2", Setup.NUMBER, Setup.getPickupTruncatedManifestMessageFormat()[1]);
        Assert.assertEquals("attribute 3", Setup.TYPE, Setup.getPickupTruncatedManifestMessageFormat()[2]);
        Assert.assertEquals("attribute 4", Setup.LENGTH, Setup.getPickupTruncatedManifestMessageFormat()[3]);
        Assert.assertEquals("attribute 5", Setup.COLOR, Setup.getPickupTruncatedManifestMessageFormat()[4]);
        Assert.assertEquals("attribute 7", Setup.LOAD, Setup.getPickupTruncatedManifestMessageFormat()[5]);
        Assert.assertEquals("attribute 6", Setup.HAZARDOUS, Setup.getPickupTruncatedManifestMessageFormat()[6]);
        Assert.assertEquals("attribute 7", Setup.NO_LOCATION, Setup.getPickupTruncatedManifestMessageFormat()[7]);
        Assert.assertEquals("attribute 8", Setup.COMMENT, Setup.getPickupTruncatedManifestMessageFormat()[8]);
        Assert.assertEquals("attribute 9", Setup.PICKUP_COMMENT, Setup.getPickupTruncatedManifestMessageFormat()[9]);
    }
    
    @Test
    public void testEngineMessageComboBox() {
        Assert.assertEquals("default size", 15, Setup.getEngineMessageComboBox().getItemCount());
        Setup.setTabEnabled(true);
        Assert.assertEquals("with tabs", 18, Setup.getEngineMessageComboBox().getItemCount());
    }
    
    @Test
    public void testCarMessageComboBox() {
        Assert.assertEquals("default size", 24, Setup.getCarMessageComboBox().getItemCount());
        Setup.setTabEnabled(true);
        Assert.assertEquals("with tabs", 27, Setup.getCarMessageComboBox().getItemCount());
    }
    
    @Test
    public void testDirectionInt() {
        Assert.assertEquals("east", 1, Setup.getDirectionInt(Setup.EAST_DIR));
        Assert.assertEquals("west", 2, Setup.getDirectionInt(Setup.WEST_DIR));
        Assert.assertEquals("south", 8, Setup.getDirectionInt(Setup.SOUTH_DIR));
        Assert.assertEquals("north", 4, Setup.getDirectionInt(Setup.NORTH_DIR));
        Assert.assertEquals("error", 0, Setup.getDirectionInt("X"));
    }

    // private final static Logger log = LoggerFactory.getLogger(SetupTest.class);

}
