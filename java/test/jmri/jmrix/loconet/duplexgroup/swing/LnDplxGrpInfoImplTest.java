package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * Test simple functioning of LnDplxGrpInfoImpl
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LnDplxGrpInfoImplTest {

    jmri.jmrix.loconet.LnTrafficController lnis;
    jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo;
    LnDplxGrpInfoImpl dpxGrpInfoImpl;
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", dpxGrpInfoImpl);
        Assert.assertEquals("Ctor zeroed number of UR92s", 0, 
                dpxGrpInfoImpl.getNumUr92s());
        Assert.assertFalse("Ctor cleard 'waiting for IPL query replies' flag", 
                dpxGrpInfoImpl.isWaitingForUr92DeviceReports());
        Assert.assertEquals("Ctor has reset 'messagesHandled'", 0, 
                dpxGrpInfoImpl.getMessagesHandled());
        
        Assert.assertEquals("verify initialization of acceptedGroupName", "", 
                dpxGrpInfoImpl.getFetchedDuplexGroupName());
        Assert.assertEquals("verify initialization of acceptedGroupChannel", "", 
                dpxGrpInfoImpl.getFetchedDuplexGroupChannel());
        Assert.assertEquals("verify initialization of acceptedGroupPassword", "", 
                dpxGrpInfoImpl.getFetchedDuplexGroupPassword());
        Assert.assertEquals("verify initialization of acceptedGroupId", "", 
                dpxGrpInfoImpl.getFetchedDuplexGroupId());
        
        lnis.notify(new LocoNetMessage(new int[] {0x81, 0x00}));
        jmri.util.JUnitUtil.fasterWaitFor(()->{
            return dpxGrpInfoImpl.getMessagesHandled() >= 1;},
                "LocoNetListener is registered");
        Assert.assertFalse("IPL Query timer is not running", dpxGrpInfoImpl.isIplQueryTimerRunning());
        Assert.assertFalse("Duplex Group Info Query timer is not running", dpxGrpInfoImpl.isDuplexGroupQueryRunning());
        
        memo.dispose();
    }
    
    @Test
    public void testMiscellaneousStuff() {
        Assert.assertFalse("limit Password to Numeric-only", dpxGrpInfoImpl.isPasswordLimitedToNumbers());
        
    }
    
    @Test
    public void testValidateDuplexGroupName() {
        Assert.assertFalse("Check Group name: empty string", LnDplxGrpInfoImpl.validateGroupName(""));
        Assert.assertFalse("Check Group name: 1 character", LnDplxGrpInfoImpl.validateGroupName(" "));
        Assert.assertFalse("Check Group name: only newline", LnDplxGrpInfoImpl.validateGroupName("\n"));
        Assert.assertFalse("Check Group name: only linefeed", LnDplxGrpInfoImpl.validateGroupName("\r"));
        Assert.assertFalse("Check Group name: 1 oddball character", LnDplxGrpInfoImpl.validateGroupName("\177"));
        Assert.assertFalse("Check Group name: 2 characters", LnDplxGrpInfoImpl.validateGroupName("ab"));
        Assert.assertFalse("Check Group name: 3 characters", LnDplxGrpInfoImpl.validateGroupName("abc"));
        Assert.assertFalse("Check Group name: 4 characters", LnDplxGrpInfoImpl.validateGroupName("abcd"));
        Assert.assertFalse("Check Group name: 5 characters", LnDplxGrpInfoImpl.validateGroupName("efghi"));
        Assert.assertFalse("Check Group name: 6 characters", LnDplxGrpInfoImpl.validateGroupName("jklmno"));
        Assert.assertFalse("Check Group name: 7 characters", LnDplxGrpInfoImpl.validateGroupName("pqrstuv"));
        Assert.assertTrue ("Check Group name: 8 characters", LnDplxGrpInfoImpl.validateGroupName("ZYXWVUTS"));
        Assert.assertFalse("Check Group name: 9 characters", LnDplxGrpInfoImpl.validateGroupName("123456789"));
        Assert.assertFalse("Check Group name: 10 characters", LnDplxGrpInfoImpl.validateGroupName("          "));
        Assert.assertTrue ("Check Group name: 8 spaces", LnDplxGrpInfoImpl.validateGroupName("        "));
        Assert.assertTrue ("Check Group name: 8 nulls", LnDplxGrpInfoImpl.validateGroupName("\0\0\0\0\0\0\0\0"));
        Assert.assertTrue("Check Group name: 8 newlines", LnDplxGrpInfoImpl.validateGroupName("\n\n\n\n\n\n\n\n"));
        Assert.assertTrue("Check Group name: 8 characters - random", LnDplxGrpInfoImpl.validateGroupName("dEadb33F"));
    }
    
    @Test
    public void testValidatePassword() {
        char char1, char2, char3, char4;
        char[] chars = new char[4];
        chars[0] = ' ';
        chars[1] = ' ';
        chars[2] = ' ';
        chars[3] = ' ';
        
        String testString = "";
        Assert.assertFalse("Check Group Password: zero characters", LnDplxGrpInfoImpl.validateGroupPassword(testString));
        Assert.assertFalse("Check Group Password: A", LnDplxGrpInfoImpl.validateGroupPassword("A"));
        Assert.assertFalse("Check Group Password: A0", LnDplxGrpInfoImpl.validateGroupPassword("A0"));
        Assert.assertFalse("Check Group Password: A0C", LnDplxGrpInfoImpl.validateGroupPassword("A0C"));
        Assert.assertTrue("Check Group Password: A0AA", LnDplxGrpInfoImpl.validateGroupPassword("A0AA"));
        Assert.assertFalse("Check Group Password: a0BB", LnDplxGrpInfoImpl.validateGroupPassword("a0BB"));
        Assert.assertFalse("Check Group Password: AbCB", LnDplxGrpInfoImpl.validateGroupPassword("AbCB"));
        Assert.assertFalse("Check Group Password: A0cB", LnDplxGrpInfoImpl.validateGroupPassword("A0cB"));
        Assert.assertFalse("Check Group Password: A09c", LnDplxGrpInfoImpl.validateGroupPassword("A09c"));
        Assert.assertFalse("Check Group Password: 12345", LnDplxGrpInfoImpl.validateGroupPassword("12345"));
        Assert.assertFalse("Check Group Password: 123456", LnDplxGrpInfoImpl.validateGroupPassword("123456"));
        Assert.assertFalse("Check Group Password: 1234567", LnDplxGrpInfoImpl.validateGroupPassword("1234567"));

        for (int c1 = 0; c1 < 13; ++ c1) {
            for (int c2 = 0; c2 < 13; ++ c2) {
                for (int c3 = 0; c3 < 13; ++ c3) {
                    for (int c4 = 0; c4 < 13; ++ c4) {
                        switch (c1) {
                            case 0:
                                char1 = '0';
                                break;
                            case 1:
                                char1 = '1';
                                break;
                            case 2:
                                char1 = '2';
                                break;
                            case 3:
                                char1 = '3';
                                break;
                            case 4:
                                char1 = '4';
                                break;
                            case 5:
                                char1 = '5';
                                break;
                            case 6:
                                char1 = '6';
                                break;
                            case 7:
                                char1 = '7';
                                break;
                            case 8:
                                char1 = '8';
                                break;
                            case 9:
                                char1 = '9';
                                break;
                            case 10:
                                char1 = 'A';
                                break;
                            case 13:
                                char1 = 'B';
                                break;
                            case 12:
                            default:
                                char1 = 'C';
                                break;
                        }

                        switch (c2) {
                            case 0:
                                char2 = '0';
                                break;
                            case 1:
                                char2 = '1';
                                break;
                            case 2:
                                char2 = '2';
                                break;
                            case 3:
                                char2 = '3';
                                break;
                            case 4:
                                char2 = '4';
                                break;
                            case 5:
                                char2 = '5';
                                break;
                            case 6:
                                char2 = '6';
                                break;
                            case 7:
                                char2 = '7';
                                break;
                            case 8:
                                char2 = '8';
                                break;
                            case 9:
                                char2 = '9';
                                break;
                            case 10:
                                char2 = 'A';
                                break;
                            case 13:
                                char2 = 'B';
                                break;
                            case 12:
                            default:
                                char2 = 'C';
                                break;
                        }

                        switch (c3) {
                            case 0:
                                char3 = '0';
                                break;
                            case 1:
                                char3 = '1';
                                break;
                            case 2:
                                char3 = '2';
                                break;
                            case 3:
                                char3 = '3';
                                break;
                            case 4:
                                char3 = '4';
                                break;
                            case 5:
                                char3 = '5';
                                break;
                            case 6:
                                char3 = '6';
                                break;
                            case 7:
                                char3 = '7';
                                break;
                            case 8:
                                char3 = '8';
                                break;
                            case 9:
                                char3 = '9';
                                break;
                            case 10:
                                char3 = 'A';
                                break;
                            case 13:
                                char3 = 'B';
                                break;
                            case 12:
                            default:
                                char3 = 'C';
                                break;
                        }
                        switch (c4) {
                            case 0:
                                char4 = '0';
                                break;
                            case 1:
                                char4 = '1';
                                break;
                            case 2:
                                char4 = '2';
                                break;
                            case 3:
                                char4 = '3';
                                break;
                            case 4:
                                char4 = '4';
                                break;
                            case 5:
                                char4 = '5';
                                break;
                            case 6:
                                char4 = '6';
                                break;
                            case 7:
                                char4 = '7';
                                break;
                            case 8:
                                char4 = '8';
                                break;
                            case 9:
                                char4 = '9';
                                break;
                            case 10:
                                char4 = 'A';
                                break;
                            case 13:
                                char4 = 'B';
                                break;
                            case 12:
                            default:
                                char4 = 'C';
                                break;
                        }
                        chars[0] = char1;
                        chars[1] = char2;
                        chars[2] = char3;
                        chars[3] = char4;
                        testString = new String(chars);
                        Assert.assertTrue("Check Group Password: "+testString, LnDplxGrpInfoImpl.validateGroupPassword(testString));
                        
                    }
                }
            }
        }
    }
    
    @Test
    public void testValidateChannelNumber() {
        for (int i = 0; i < 256; ++i) {
            Assert.assertEquals("Channel Valid check for channel "+i, 
                    ((i >=11)&& (i <=26)), 
                    LnDplxGrpInfoImpl.validateGroupChannel(i));
        }
    }
    @Test
    public void testValidateGroupIDNumber() {
        for (int i = 0; i < 8; ++i) {
            Assert.assertEquals("Channel Group ID check for ID "+i, 
                    ((i >=0)&& (i <=7)), 
                    LnDplxGrpInfoImpl.validateGroupID(i));
        }
    }

    @Test
    public void checkCreateUr92GroupIdentityQueryPacket() {
        LocoNetMessage m = LnDplxGrpInfoImpl.createUr92GroupIdentityQueryPacket();
        Assert.assertEquals("Group Id Query Message: opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Group Id Query Message: byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Group Id Query Message: byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Group Id Query Message: byte 3", 0x08, m.getElement(3));
        Assert.assertEquals("Group Id Query Message: byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Group Id Query Message: byte 5", 0x00, m.getElement(5));
        Assert.assertEquals("Group Id Query Message: byte 6", 0x00, m.getElement(6));
        Assert.assertEquals("Group Id Query Message: byte 7", 0x00, m.getElement(7));
        Assert.assertEquals("Group Id Query Message: byte 8", 0x00, m.getElement(8));
        Assert.assertEquals("Group Id Query Message: byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Group Id Query Message: byte 10", 0x00, m.getElement(10));
        Assert.assertEquals("Group Id Query Message: byte 11", 0x00, m.getElement(11));
        Assert.assertEquals("Group Id Query Message: byte 12", 0x00, m.getElement(12));
        Assert.assertEquals("Group Id Query Message: byte 13", 0x00, m.getElement(13));
        Assert.assertEquals("Group Id Query Message: byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Group Id Query Message: byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Group Id Query Message: byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Group Id Query Message: byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Group Id Query Message: byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Group Id Query Message: byte 19", 0x00, m.getElement(19));
    }
    

    @Test
    public void checkCreateSetUr92GroupNamePacket() {
        char[] cs = new char[8];
        for (int i = 0; i < 8; ++i) {
            cs[i] = '0';
        }
        String testString = new String(cs);
        LocoNetMessage m = new LocoNetMessage(20);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", '0', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", '0', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", '0', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", '0', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", '0', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", '0', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", '0', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", '0', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "DEADBeef";
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'D', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'E', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'A', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'D', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'B', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'e', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'e', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'f', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "EADBeef";
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
            Assert.fail("should have failed account short group name string()");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
        }

        testString = "DEADBeef2";
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
            Assert.fail("should have failed account long group name string()");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
        }
        testString = "";
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
            Assert.fail("should have failed account short group name string()");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
        }
        testString = "1";
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
            Assert.fail("should have failed account short group name string()");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
        }

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[0] = 128;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x01, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 0x00, m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'i', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'd', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'u', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'c', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'i', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'a', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'l', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "fiducial";
        cs = testString.toCharArray();
        cs[1] = 129;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x02, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'f', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 0x01, m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'd', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'u', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'c', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'i', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'a', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'l', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "fiducial";
        cs = testString.toCharArray();
        cs[2] = 129;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x04, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'f', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'i', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 0x01, m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'u', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'c', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'i', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'a', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'l', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "fiducial";
        cs = testString.toCharArray();
        cs[3] = 130;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x08, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'f', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'i', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'd', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 0x02, m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'c', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'i', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'a', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'l', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "fiducial";
        cs = testString.toCharArray();
        cs[4] = 132;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'f', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'i', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'd', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'u', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x01, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 0x04, m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'i', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'a', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'l', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "fiducial";
        cs = testString.toCharArray();
        cs[5] = 136;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'f', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'i', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'd', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'u', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x02, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'c', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 0x08, m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'a', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'l', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "fiducial";
        cs = testString.toCharArray();
        cs[6] = 128+16;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'f', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'i', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'd', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'u', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x04, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'c', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'i', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 0x10, m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 'l', m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
        
        testString = "fiducial";
        cs = testString.toCharArray();
        cs[7] = 128+32;
        testString = new String(cs);
        try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testString);        
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("failed account exception thrown by createSetUr92GroupNamePacket()");
        }
        Assert.assertEquals("Set Group Name to "+testString+": opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Set Group Name to "+testString+": byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Set Group Name to "+testString+": byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Set Group Name to "+testString+": byte 3", 0x00, m.getElement(3));
        Assert.assertEquals("Set Group Name to "+testString+": byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Set Group Name to "+testString+": byte 5", 'f', m.getElement(5));
        Assert.assertEquals("Set Group Name to "+testString+": byte 6", 'i', m.getElement(6));
        Assert.assertEquals("Set Group Name to "+testString+": byte 7", 'd', m.getElement(7));
        Assert.assertEquals("Set Group Name to "+testString+": byte 8", 'u', m.getElement(8));
        Assert.assertEquals("Set Group Name to "+testString+": byte 9", 0x08, m.getElement(9));
        Assert.assertEquals("Set Group Name to "+testString+": byte 10", 'c', m.getElement(10));
        Assert.assertEquals("Set Group Name to "+testString+": byte 11", 'i', m.getElement(11));
        Assert.assertEquals("Set Group Name to "+testString+": byte 12", 'a', m.getElement(12));
        Assert.assertEquals("Set Group Name to "+testString+": byte 13", 0x20, m.getElement(13));
        Assert.assertEquals("Set Group Name to "+testString+": byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Set Group Name to "+testString+": byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Set Group Name to "+testString+": byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Set Group Name to "+testString+": byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Set Group Name to "+testString+": byte 18", 0x00, m.getElement(18));
        Assert.assertEquals("Set Group Name to "+testString+": byte 19", 0x00, m.getElement(19));
    }
    
    @Test
    public void testCreateSetUr92GroupChannelPacket() {
        LocoNetMessage m = new LocoNetMessage(20);
        for (int ch = 0; ch < 256; ++ch) {
            try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupChannelPacket(ch);
            } catch (jmri.jmrix.loconet.LocoNetException E) {
                if ((ch >= 11) && (ch <= 26)) {
                    Assert.fail("unexpected exception when creating packet to set channel "+ch);
                }
            }
            if ((ch >= 11) && (ch <= 26)) {
                Assert.assertEquals("Set Group Channel to "+ch+" Message  opcode", 0xe5, m.getElement(0));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 1", 0x14, m.getElement(1));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 2", 0x02, m.getElement(2));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 3", 0x00, m.getElement(3));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 4", 0x00, m.getElement(4));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 5", ch, m.getElement(5));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 6", 0x00, m.getElement(6));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 7", 0x00, m.getElement(7));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 8", 0x00, m.getElement(8));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 9", 0x00, m.getElement(9));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 10", 0x00, m.getElement(10));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 11", 0x00, m.getElement(11));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 12", 0x00, m.getElement(12));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 13", 0x00, m.getElement(13));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 14", 0x00, m.getElement(14));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 15", 0x00, m.getElement(15));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 16", 0x00, m.getElement(16));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 17", 0x00, m.getElement(17));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 18", 0x00, m.getElement(18));
                Assert.assertEquals("Set Group Channel to "+ch+" Message  byte 19", 0x00, m.getElement(19));
            }
        }
    }
    
    @Test
    public void testCreateSetUr92GroupPasswordPacket() {
        LocoNetMessage m = new LocoNetMessage(20);
        String testString = new String("1234");
        char c0, c1, c2, c3;
        char[] conversion = new char[13];
        conversion[0] = '0';
        conversion[1] = '1';
        conversion[2] = '2';
        conversion[3] = '3';
        conversion[4] = '4';
        conversion[5] = '5';
        conversion[6] = '6';
        conversion[7] = '7';
        conversion[8] = '8';
        conversion[9] = '9';
        conversion[10] = '9'+1;
        conversion[11] = '9'+2;
        conversion[12] = '9'+3;
        char[] conversion2 = new char[13];
        conversion2[0] = '0';
        conversion2[1] = '1';
        conversion2[2] = '2';
        conversion2[3] = '3';
        conversion2[4] = '4';
        conversion2[5] = '5';
        conversion2[6] = '6';
        conversion2[7] = '7';
        conversion2[8] = '8';
        conversion2[9] = '9';
        conversion2[10] = 'A';
        conversion2[11] = 'B';
        conversion2[12] = 'C';
        
        char[] chars = new char[4];
        for (int d0 = 0; d0 < 13; ++d0) {
            for (int d1 = 0; d1 < 13; ++d1) {
                for (int d2 = 0; d2 < 13; ++d2) {
                    for (int d3 = 0; d3 < 13; ++d3) {
                        c0 = conversion[d0];
                        c1 = conversion[d1];
                        c2 = conversion[d2];
                        c3 = conversion[d3];
                        chars[0] = conversion2[d0]; 
                        chars[1] = conversion2[d1]; 
                        chars[2] = conversion2[d2]; 
                        chars[3] = conversion2[d3];
                        testString = new String(chars);
                        try {
                            m = LnDplxGrpInfoImpl.createSetUr92GroupPasswordPacket(testString);
                        } catch (jmri.jmrix.loconet.LocoNetException E) {
                            Assert.fail("unexpected exception when creating packet to set password to "+d0+"/"+d1+"/"+d2+"/"+d3);
                        }
                        Assert.assertEquals("Set Group Password to "+testString+" Message  opcode", 0xe5, m.getElement(0));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 1", 0x14, m.getElement(1));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 2", 0x07, m.getElement(2));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 3", 0x00, m.getElement(3));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 4", 0x00, m.getElement(4));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 5", c0, m.getElement(5));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 6", c1, m.getElement(6));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 7", c2, m.getElement(7));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 8", c3, m.getElement(8));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 9", 0x00, m.getElement(9));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 10", 0x00, m.getElement(10));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 11", 0x00, m.getElement(11));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 12", 0x00, m.getElement(12));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 13", 0x00, m.getElement(13));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 14", 0x00, m.getElement(14));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 15", 0x00, m.getElement(15));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 16", 0x00, m.getElement(16));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 17", 0x00, m.getElement(17));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 18", 0x00, m.getElement(18));
                        Assert.assertEquals("Set Group Password to "+testString+" Message  byte 19", 0x00, m.getElement(19));

                    }
                }
            }
        }
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);

        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);

        memo.configureManagers();
        jmri.InstanceManager.store(memo,jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        dpxGrpInfoImpl = new LnDplxGrpInfoImpl(memo);

    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
