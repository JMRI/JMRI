package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.duplexgroup.DuplexGroupMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeListener;

/**
 * Test simple functioning of LnDplxGrpInfoImpl
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author      B. Milhaupt Copyright (C) 2018
 */
public class LnDplxGrpInfoImplTest {

    jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis;
    jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo;
    LnDplxGrpInfoImpl dpxGrpInfoImpl;
    boolean propChangeQueryFlag;
    boolean propChangeReportFlag;
    boolean propChangeFlag;
    int propChangeCount;
    public javax.swing.Timer pacingTimer = null;
    public boolean initialWaitIsDone;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", dpxGrpInfoImpl);
        Assert.assertEquals("Ctor zeroed number of UR92s", 0,
                dpxGrpInfoImpl.getNumUr92s());
        Assert.assertFalse("Ctor cleard 'waiting for IPL query replies' flag",
                dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport());
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

        dpxGrpInfoImpl.dispose();

        for (LocoNetListener listener : lnis.getListeners()) {
            if (listener == dpxGrpInfoImpl) Assert.fail("dispose did not remove");
        }

        memo.dispose();

    }

    @Test
    public void testMiscellaneousStuff() {
        Assert.assertFalse("limit Password to Numeric-only", LnDplxGrpInfoImpl.isPasswordLimitedToNumbers());

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
        for (int i = -1; i < 256; ++i) {
            Assert.assertEquals("Channel Group ID check for ID "+i,
                    ((i >=0)&& (i <=127)),
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

    @Test
    public void testCreateSetUr92GroupIDPacket() {
        LocoNetMessage m = new LocoNetMessage(20);
        for (int d0 = -1; d0 < 256; ++d0) {
            m.setElement(1, 9876);
            try {
                m = LnDplxGrpInfoImpl.createSetUr92GroupIDPacket(Integer.toString(d0));
            } catch (jmri.jmrix.loconet.LocoNetException E) {
                Assert.assertTrue("expect exception only when range is outside of rante [0 to 127]", (d0 < 0)|| (d0 > 127));
            }
            if ((d0 >= 0) && (d0 < 128)) {
                Assert.assertEquals("Set Group ID to "+d0+" Message  opcode", 0xe5, m.getElement(0));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 1", 0x14, m.getElement(1));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 2", 0x04, m.getElement(2));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 3", 0x00, m.getElement(3));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 4", 0x00, m.getElement(4));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 5", d0 & 0x7f, m.getElement(5));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 6", 0x00, m.getElement(6));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 7", 0x00, m.getElement(7));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 8", 0x00, m.getElement(8));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 9", 0x00, m.getElement(9));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 10", 0x00, m.getElement(10));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 11", 0x00, m.getElement(11));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 12", 0x00, m.getElement(12));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 13", 0x00, m.getElement(13));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 14", 0x00, m.getElement(14));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 15", 0x00, m.getElement(15));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 16", 0x00, m.getElement(16));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 17", 0x00, m.getElement(17));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 18", 0x00, m.getElement(18));
                Assert.assertEquals("Set Group ID to "+d0+" Message  byte 19", 0x00, m.getElement(19));
            }
        }
    }

    @Test
    public void testIsDuplexGroupMessage() {
        LocoNetMessage m = new LocoNetMessage(20);

        m.setElement(1, 0x14);
        m.setElement(2, 2);
        m.setElement(3, 8);
        m.setElement(4, 0);
        m.setElement(5, 0);
        m.setElement(6, 0);
        m.setElement(7, 0);
        m.setElement(8, 0);
        m.setElement(9, 0);
        m.setElement(10, 0);
        m.setElement(11, 0);
        m.setElement(12, 0);
        m.setElement(13, 0);
        m.setElement(14, 0);
        m.setElement(15, 0);
        m.setElement(16, 0);
        m.setElement(17, 0);
        m.setElement(18, 0);
        m.setElement(19, 0);
        for (int d0 = 0; d0 < 256; ++d0) {
            m.setElement(0, d0);
            Assert.assertEquals("checking isDuplexGroupMessage for opcode "+d0,
                    (d0 == 0xe5), LnDplxGrpInfoImpl.isDuplexGroupMessage(m));
        }
        m.setElement(0, 0xe5);
        for (int d1 = 0; d1 < 256; ++d1) {
            m.setElement(1, d1);
            Assert.assertEquals("checking isDuplexGroupMessage for byte 1 "+d1,
                    (d1 == 0x14), LnDplxGrpInfoImpl.isDuplexGroupMessage(m));
        }
        m.setElement(1, 0x14);
        for (int d2 = 0; d2 < 256; ++d2) {
            m.setElement(2, d2);
            Assert.assertEquals("checking isDuplexGroupMessage for byte 2="+d2,
                    (d2 == 2) || (d2 == 3) || (d2 == 4) || (d2 == 7),
                    LnDplxGrpInfoImpl.isDuplexGroupMessage(m));
        }
        m.setElement(2, 0x2);
        for (int d3 = 0; d3 < 256; ++d3) {
            m.setElement(3, d3);
            Assert.assertEquals("checking isDuplexGroupMessage for byte 3 "+d3,
                    (d3 == 0) || (d3 == 8) || (d3 == 0x10),
                    LnDplxGrpInfoImpl.isDuplexGroupMessage(m));
        }
        m.setElement(3, 0);
        for (int index = 4; index < 20; ++index) {
            m.setElement(index-1, 0);
            for (int i = 0; i < 16; ++i) {
                int val = (int)(256*Math.random());
                m.setElement(index, val);

                Assert.assertTrue("checking isDuplexGroupMessge for byte "+index+
                        " as value "+val,
                        LnDplxGrpInfoImpl.isDuplexGroupMessage(m));
            }
        }
    }

    @Test
    public void testGetDupGrpIdentityMsgType() {
//    public static final DuplexGroupMessageType getDuplexGroupIdentityMessageType(LocoNetMessage m) {
//        where DuplexGroupMessageType is comprised of:
//            NOT_A_DUPLEX_GROUP_MESSAGE,
//            DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE,
//            DUPLEX_GROUP_NAME_QUERY_MESSAGE,
//            DUPLEX_GROUP_NAME_WRITE_MESSAGE, // b2=3, b3=0
//            DUPLEX_GROUP_PASSWORD_REPORT_MESSAGE,
//            DUPLEX_GROUP_PASSWORD_QUERY_MESSAGE,
//            DUPLEX_GROUP_PASSWORD_WRITE_MESSAGE, // b2=7, b3=0
//            DUPLEX_GROUP_CHANNEL_REPORT_MESSAGE,
//            DUPLEX_GROUP_CHANNEL_QUERY_MESSAGE,
//            DUPLEX_GROUP_CHANNEL_WRITE_MESSAGE,  // b2=2, b3=0
//            DUPLEX_GROUP_ID_REPORT_MESSAGE,
//            DUPLEX_GROUP_ID_QUERY_MESSAGE,
//            DUPLEX_GROUP_ID_WRITE_MESSAGE; // b2=4, B3=0
//        }

        LocoNetMessage m = new LocoNetMessage(2);
        m.setElement(0, 0x81);
        m.setElement(1, 0);
        DuplexGroupMessageType mt;
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: 2 byte",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m = new LocoNetMessage(19);
        m.setElement(0, 0xe5);
        m.setElement(1, 0x13);
        for (int i=2; i< 19;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: 19 byte",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m = new LocoNetMessage(21);
        m.setElement(0, 0xe5);
        m.setElement(1, 0x15);
        for (int i=2; i< 21;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: 21 byte",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m = new LocoNetMessage(20);
        m.setElement(0, 0xe5);
        m.setElement(1, 0x13);
        for (int i=2; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: 20 byte with 19-byte length value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m.setElement(1, 0x16);
        for (int i=2; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: 20 byte with 22-byte length value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m.setElement(1, 0x14);
        m.setElement(2, 0);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m.setElement(2, 1);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m.setElement(2, 5);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m.setElement(2, 6);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m.setElement(2, 8);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        Assert.assertEquals("not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);

        m.setElement(2, 9);
        for (int i=4; i< 20;++i) { m.setElement(i, 0);}
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            Assert.assertEquals("not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value",
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt);
        }

        m.setElement(2, 2);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    Assert.assertEquals("duplex channel message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_WRITE_MESSAGE,
                            mt);
                    break;
                case 8:
                    Assert.assertEquals("duplex channel message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_QUERY_MESSAGE,
                            mt);
                    break;
                case 16:
                    Assert.assertEquals("duplex channel message: byte 4="+m.getElement(2)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_REPORT_MESSAGE,
                            mt);
                    break;
                default:
                    Assert.assertEquals("not a duplex channel message: byte 4="+m.getElement(2)+" not a Duplex value",
                        DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                        mt);
                    break;
            }
        }


        m.setElement(2, 3);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    Assert.assertEquals("duplex name message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_NAME_WRITE_MESSAGE,
                            mt);
                    break;
                case 8:
                    Assert.assertEquals("duplex name message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_NAME_QUERY_MESSAGE,
                            mt);
                    break;
                case 16:
                    Assert.assertEquals("duplex name message: byte 4="+m.getElement(2)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE,
                            mt);
                    break;
                default:
                    Assert.assertEquals("not a duplex name message: byte 4="+m.getElement(2)+" not a Duplex value",
                        DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                        mt);
                    break;
            }
        }


        m.setElement(2, 4);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    Assert.assertEquals("duplex ID message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_ID_WRITE_MESSAGE,
                            mt);
                    break;
                case 8:
                    Assert.assertEquals("duplex ID message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_ID_QUERY_MESSAGE,
                            mt);
                    break;
                case 16:
                    Assert.assertEquals("duplex id message: byte 4="+m.getElement(2)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_ID_REPORT_MESSAGE,
                            mt);
                    break;
                default:
                    Assert.assertEquals("not a duplex id message: byte 4="+m.getElement(2)+" not a Duplex value",
                        DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                        mt);
                    break;
            }
        }

        m.setElement(2, 7);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    Assert.assertEquals("duplex password message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_WRITE_MESSAGE,
                            mt);
                    break;
                case 8:
                    Assert.assertEquals("duplex password message: byte 4="+m.getElement(4)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_QUERY_MESSAGE,
                            mt);
                    break;
                case 16:
                    Assert.assertEquals("duplex password message: byte 4="+m.getElement(2)+" implies a Duplex message",
                            DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_REPORT_MESSAGE,
                            mt);
                    break;
                default:
                    Assert.assertEquals("not a duplex password message: byte 4="+m.getElement(2)+" not a Duplex value",
                        DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                        mt);
                    break;
            }
        }
    }
    
    @Test
    public void testHandleMessageDuplexInfoReport() {
        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (((e.getPropertyName().equals("DPLXPCK_STAT_LN_UPDATE")))) {
//                    log.warn("prop change query seen");
                    propChangeQueryFlag = true;
                } else if ((e.getPropertyName().equals("DPLXPCK_NAME_UPDATE"))) {
//                    log.warn("prop change update seen");
                    propChangeReportFlag = true;
                } else if (e.getPropertyName().equals("DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR")) {
//                    log.warn("prop change update if not currently error seen");
                    propChangeReportFlag = true;
                }
                propChangeFlag = true;
                propChangeCount++;
            }
        };

        dpxGrpInfoImpl.addPropertyChangeListener(l);
        propChangeCount = 0;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;

        Assert.assertFalse("did not see property change flag yet", propChangeFlag);
        Assert.assertFalse("did not see property change flag yet", propChangeReportFlag);
        Assert.assertFalse("did not see property change flag yet", propChangeQueryFlag);
        Assert.assertEquals("no prop change listener firings yet", 0, propChangeCount);

        Assert.assertFalse("not yet waiting for Duplex Group Name, etc. Report", dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage());
        dpxGrpInfoImpl.queryDuplexGroupIdentity();
        Assert.assertTrue("Now waiting for Duplex Group Name, etc. Report", dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage());



        Assert.assertTrue("did see initial property change Report flag", propChangeReportFlag);
        Assert.assertEquals("Did see a bunch of invalidation prop changes", 8, propChangeCount);

        propChangeCount = 0;
        propChangeReportFlag = false;

        LocoNetMessage rcvMsg = lnis.outbound.get(0);
        dpxGrpInfoImpl.message(rcvMsg); // echo the transmitted message back to the sender

        LocoNetMessage m = new LocoNetMessage(20);
        int ch = 4;
        int id = 131;
        String name = "\231\032\033\034\035\036\237\140";
        String pass="0200";
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);
        Assert.assertTrue("Now waiting (2) for Duplex Group Name, etc. Report", dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage());

        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertFalse("No longer waiting for Duplex Group Name, etc. Report", dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage());
        Assert.assertEquals("Expected exactly one prop change event", 11, propChangeCount);

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        dpxGrpInfoImpl.message(m);  // transmit the reply
        Assert.assertFalse("No longer (2) waiting for Duplex Group Name, etc. Report", dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage());
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertEquals("Expected exactly one prop change event", 1, propChangeCount);

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(6, m.getElement(6)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertEquals("Expected exactly one prop change event", 2, propChangeCount);

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(6, m.getElement(6)^1);
        m.setElement(15, m.getElement(15)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertEquals("Expected exactly one prop change event", 2, propChangeCount);

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(15, m.getElement(15)^1);
        m.setElement(17, m.getElement(17)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertEquals("Expected exactly one prop change event", 2, propChangeCount);

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(17, m.getElement(17)^1);
        m.setElement(18, m.getElement(18)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertEquals("Expected exactly one prop change event", 2, propChangeCount);

        try {
            Thread.sleep(1300);
        } catch (InterruptedException e) {
        }

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(17, m.getElement(17)^1);
        m.setElement(18, m.getElement(18)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertEquals("Expected exactly two prop change event", 2, propChangeCount);

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(17, m.getElement(17)^1);
        m.setElement(18, m.getElement(18)^1);
        m.setElement(6, m.getElement(6)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        Assert.assertEquals("Expected exactly two prop change event", 2, propChangeCount);
        Assert.assertFalse("Query Timer no longer running",dpxGrpInfoImpl.isIplQueryTimerRunning());

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(0, m.getElement(0) ^ 1);
        dpxGrpInfoImpl.message(m);  // transmit the reply
        jmri.util.JUnitUtil.fasterWaitFor(()->{return propChangeFlag == false;},"message received");

    }

    @Test
    public void checkCreateUr92GroupNameReportPacket() {
        int ch = 7, id = 4;
        String name = "AbcdeFgh", pass="ABC0";
        LocoNetMessage m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        Assert.assertEquals("Group Id Query Message: opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Group Id Query Message: byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Group Id Query Message: byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Group Id Query Message: byte 3", 0x10, m.getElement(3));
        Assert.assertEquals("Group Id Query Message: byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Group Id Query Message: byte 5", 0x41, m.getElement(5));
        Assert.assertEquals("Group Id Query Message: byte 6", 0x62, m.getElement(6));
        Assert.assertEquals("Group Id Query Message: byte 7", 0x63, m.getElement(7));
        Assert.assertEquals("Group Id Query Message: byte 8", 0x64, m.getElement(8));
        Assert.assertEquals("Group Id Query Message: byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Group Id Query Message: byte 10", 0x65, m.getElement(10));
        Assert.assertEquals("Group Id Query Message: byte 11", 0x46, m.getElement(11));
        Assert.assertEquals("Group Id Query Message: byte 12", 0x67, m.getElement(12));
        Assert.assertEquals("Group Id Query Message: byte 13", 0x68, m.getElement(13));
        Assert.assertEquals("Group Id Query Message: byte 14", 0x03, m.getElement(14));
        Assert.assertEquals("Group Id Query Message: byte 15", 0x2b, m.getElement(15));
        Assert.assertEquals("Group Id Query Message: byte 16", 0x40, m.getElement(16));
        Assert.assertEquals("Group Id Query Message: byte 17", 0x7, m.getElement(17));
        Assert.assertEquals("Group Id Query Message: byte 18", 0x4, m.getElement(18));
        Assert.assertEquals("Group Id Query Message: byte 19", 0x00, m.getElement(19));

        ch = 0; id = 7;
        name = "\0\0\0\0\0\0\0\0"; pass="733B";
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        Assert.assertEquals("Group Id Query Message: opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Group Id Query Message: byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Group Id Query Message: byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Group Id Query Message: byte 3", 0x10, m.getElement(3));
        Assert.assertEquals("Group Id Query Message: byte 4", 0x00, m.getElement(4));
        Assert.assertEquals("Group Id Query Message: byte 5", 0x0, m.getElement(5));
        Assert.assertEquals("Group Id Query Message: byte 6", 0x0, m.getElement(6));
        Assert.assertEquals("Group Id Query Message: byte 7", 0x0, m.getElement(7));
        Assert.assertEquals("Group Id Query Message: byte 8", 0x0, m.getElement(8));
        Assert.assertEquals("Group Id Query Message: byte 9", 0x00, m.getElement(9));
        Assert.assertEquals("Group Id Query Message: byte 10", 0x0, m.getElement(10));
        Assert.assertEquals("Group Id Query Message: byte 11", 0x0, m.getElement(11));
        Assert.assertEquals("Group Id Query Message: byte 12", 0x0, m.getElement(12));
        Assert.assertEquals("Group Id Query Message: byte 13", 0x0, m.getElement(13));
        Assert.assertEquals("Group Id Query Message: byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Group Id Query Message: byte 15", 0x73, m.getElement(15));
        Assert.assertEquals("Group Id Query Message: byte 16", 0x3b, m.getElement(16));
        Assert.assertEquals("Group Id Query Message: byte 17", 0x0, m.getElement(17));
        Assert.assertEquals("Group Id Query Message: byte 18", 0x7, m.getElement(18));
        Assert.assertEquals("Group Id Query Message: byte 19", 0x00, m.getElement(19));

        ch = 129; id = 2;
        name = "\200\201\202\203\204\205\206\207"; pass="0001";
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        Assert.assertEquals("Group Id Query Message: opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Group Id Query Message: byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Group Id Query Message: byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Group Id Query Message: byte 3", 0x10, m.getElement(3));
        Assert.assertEquals("Group Id Query Message: byte 4", 0x0f, m.getElement(4));
        Assert.assertEquals("Group Id Query Message: byte 5", 0x00, m.getElement(5));
        Assert.assertEquals("Group Id Query Message: byte 6", 0x01, m.getElement(6));
        Assert.assertEquals("Group Id Query Message: byte 7", 0x02, m.getElement(7));
        Assert.assertEquals("Group Id Query Message: byte 8", 0x03, m.getElement(8));
        Assert.assertEquals("Group Id Query Message: byte 9", 0x0f, m.getElement(9));
        Assert.assertEquals("Group Id Query Message: byte 10", 0x04, m.getElement(10));
        Assert.assertEquals("Group Id Query Message: byte 11", 0x05, m.getElement(11));
        Assert.assertEquals("Group Id Query Message: byte 12", 0x06, m.getElement(12));
        Assert.assertEquals("Group Id Query Message: byte 13", 0x07, m.getElement(13));
        Assert.assertEquals("Group Id Query Message: byte 14", 0x04, m.getElement(14));
        Assert.assertEquals("Group Id Query Message: byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Group Id Query Message: byte 16", 0x01, m.getElement(16));
        Assert.assertEquals("Group Id Query Message: byte 17", 0x1, m.getElement(17));
        Assert.assertEquals("Group Id Query Message: byte 18", 0x2, m.getElement(18));
        Assert.assertEquals("Group Id Query Message: byte 19", 0x00, m.getElement(19));

        ch = 4; id = 131;
        name = "\231\032\033\034\035\036\237\140"; pass="0200";
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        Assert.assertEquals("Group Id Query Message: opcode", 0xe5, m.getElement(0));
        Assert.assertEquals("Group Id Query Message: byte 1", 0x14, m.getElement(1));
        Assert.assertEquals("Group Id Query Message: byte 2", 0x03, m.getElement(2));
        Assert.assertEquals("Group Id Query Message: byte 3", 0x10, m.getElement(3));
        Assert.assertEquals("Group Id Query Message: byte 4", 0x01, m.getElement(4));
        Assert.assertEquals("Group Id Query Message: byte 5", 0x19, m.getElement(5));
        Assert.assertEquals("Group Id Query Message: byte 6", 0x1a, m.getElement(6));
        Assert.assertEquals("Group Id Query Message: byte 7", 0x1b, m.getElement(7));
        Assert.assertEquals("Group Id Query Message: byte 8", 0x1c, m.getElement(8));
        Assert.assertEquals("Group Id Query Message: byte 9", 0x04, m.getElement(9));
        Assert.assertEquals("Group Id Query Message: byte 10", 0x1d, m.getElement(10));
        Assert.assertEquals("Group Id Query Message: byte 11", 0x1e, m.getElement(11));
        Assert.assertEquals("Group Id Query Message: byte 12", 0x1f, m.getElement(12));
        Assert.assertEquals("Group Id Query Message: byte 13", 0x60, m.getElement(13));
        Assert.assertEquals("Group Id Query Message: byte 14", 0x08, m.getElement(14));
        Assert.assertEquals("Group Id Query Message: byte 15", 0x02, m.getElement(15));
        Assert.assertEquals("Group Id Query Message: byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Group Id Query Message: byte 17", 0x4, m.getElement(17));
        Assert.assertEquals("Group Id Query Message: byte 18", 0x3, m.getElement(18));
        Assert.assertEquals("Group Id Query Message: byte 19", 0x00, m.getElement(19));

    }


    @Test
    public void testCreatePasswordMessage() {
        LocoNetMessage m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("1234");
        Assert.assertEquals("Opcode", 0xe5, m.getOpCode());
        Assert.assertEquals("Byte 01", 0x14, m.getElement(1));
        Assert.assertEquals("Byte 02", 0x07, m.getElement(2));
        Assert.assertEquals("Byte 03", 0x10, m.getElement(3));
        Assert.assertEquals("Byte 04", 0x00, m.getElement(4));
        Assert.assertEquals("Byte 05", 0x31, m.getElement(5));
        Assert.assertEquals("Byte 06", 0x32, m.getElement(6));
        Assert.assertEquals("Byte 07", 0x33, m.getElement(7));
        Assert.assertEquals("Byte 08", 0x34, m.getElement(8));
        Assert.assertEquals("Byte 09", 0x00, m.getElement(9));
        Assert.assertEquals("Byte 10", 0x00, m.getElement(10));
        Assert.assertEquals("Byte 11", 0x00, m.getElement(11));
        Assert.assertEquals("Byte 12", 0x00, m.getElement(12));
        Assert.assertEquals("Byte 13", 0x00, m.getElement(13));
        Assert.assertEquals("Byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Byte 18", 0x00, m.getElement(18));

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("123A");
        Assert.assertEquals("Opcode", 0xe5, m.getOpCode());
        Assert.assertEquals("Byte 01", 0x14, m.getElement(1));
        Assert.assertEquals("Byte 02", 0x07, m.getElement(2));
        Assert.assertEquals("Byte 03", 0x10, m.getElement(3));
        Assert.assertEquals("Byte 04", 0x00, m.getElement(4));
        Assert.assertEquals("Byte 05", 0x31, m.getElement(5));
        Assert.assertEquals("Byte 06", 0x32, m.getElement(6));
        Assert.assertEquals("Byte 07", 0x33, m.getElement(7));
        Assert.assertEquals("Byte 08", 0x41, m.getElement(8));
        Assert.assertEquals("Byte 09", 0x00, m.getElement(9));
        Assert.assertEquals("Byte 10", 0x00, m.getElement(10));
        Assert.assertEquals("Byte 11", 0x00, m.getElement(11));
        Assert.assertEquals("Byte 12", 0x00, m.getElement(12));
        Assert.assertEquals("Byte 13", 0x00, m.getElement(13));
        Assert.assertEquals("Byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Byte 18", 0x00, m.getElement(18));

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("12C0");
        Assert.assertEquals("Opcode", 0xe5, m.getOpCode());
        Assert.assertEquals("Byte 01", 0x14, m.getElement(1));
        Assert.assertEquals("Byte 02", 0x07, m.getElement(2));
        Assert.assertEquals("Byte 03", 0x10, m.getElement(3));
        Assert.assertEquals("Byte 04", 0x00, m.getElement(4));
        Assert.assertEquals("Byte 05", 0x31, m.getElement(5));
        Assert.assertEquals("Byte 06", 0x32, m.getElement(6));
        Assert.assertEquals("Byte 07", 0x43, m.getElement(7));
        Assert.assertEquals("Byte 08", 0x30, m.getElement(8));
        Assert.assertEquals("Byte 09", 0x00, m.getElement(9));
        Assert.assertEquals("Byte 10", 0x00, m.getElement(10));
        Assert.assertEquals("Byte 11", 0x00, m.getElement(11));
        Assert.assertEquals("Byte 12", 0x00, m.getElement(12));
        Assert.assertEquals("Byte 13", 0x00, m.getElement(13));
        Assert.assertEquals("Byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Byte 18", 0x00, m.getElement(18));

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("9B00");
        Assert.assertEquals("Opcode", 0xe5, m.getOpCode());
        Assert.assertEquals("Byte 01", 0x14, m.getElement(1));
        Assert.assertEquals("Byte 02", 0x07, m.getElement(2));
        Assert.assertEquals("Byte 03", 0x10, m.getElement(3));
        Assert.assertEquals("Byte 04", 0x00, m.getElement(4));
        Assert.assertEquals("Byte 05", 0x39, m.getElement(5));
        Assert.assertEquals("Byte 06", 0x42, m.getElement(6));
        Assert.assertEquals("Byte 07", 0x30, m.getElement(7));
        Assert.assertEquals("Byte 08", 0x30, m.getElement(8));
        Assert.assertEquals("Byte 09", 0x00, m.getElement(9));
        Assert.assertEquals("Byte 10", 0x00, m.getElement(10));
        Assert.assertEquals("Byte 11", 0x00, m.getElement(11));
        Assert.assertEquals("Byte 12", 0x00, m.getElement(12));
        Assert.assertEquals("Byte 13", 0x00, m.getElement(13));
        Assert.assertEquals("Byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Byte 18", 0x00, m.getElement(18));

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("A999");
        Assert.assertEquals("Opcode", 0xe5, m.getOpCode());
        Assert.assertEquals("Byte 01", 0x14, m.getElement(1));
        Assert.assertEquals("Byte 02", 0x07, m.getElement(2));
        Assert.assertEquals("Byte 03", 0x10, m.getElement(3));
        Assert.assertEquals("Byte 04", 0x00, m.getElement(4));
        Assert.assertEquals("Byte 05", 0x41, m.getElement(5));
        Assert.assertEquals("Byte 06", 0x39, m.getElement(6));
        Assert.assertEquals("Byte 07", 0x39, m.getElement(7));
        Assert.assertEquals("Byte 08", 0x39, m.getElement(8));
        Assert.assertEquals("Byte 09", 0x00, m.getElement(9));
        Assert.assertEquals("Byte 10", 0x00, m.getElement(10));
        Assert.assertEquals("Byte 11", 0x00, m.getElement(11));
        Assert.assertEquals("Byte 12", 0x00, m.getElement(12));
        Assert.assertEquals("Byte 13", 0x00, m.getElement(13));
        Assert.assertEquals("Byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Byte 18", 0x00, m.getElement(18));

        String password = "\250\200\150\100";
        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket(password);
        Assert.assertEquals("Opcode",  0xe5, m.getOpCode());
        Assert.assertEquals("Byte 01", 0x14, m.getElement(1));
        Assert.assertEquals("Byte 02", 0x07, m.getElement(2));
        Assert.assertEquals("Byte 03", 0x10, m.getElement(3));
        Assert.assertEquals("Byte 04", 0x0c, m.getElement(4));
        Assert.assertEquals("Byte 05", 0x28, m.getElement(5));
        Assert.assertEquals("Byte 06", 0x00, m.getElement(6));
        Assert.assertEquals("Byte 07", 0x68, m.getElement(7));
        Assert.assertEquals("Byte 08", 0x40, m.getElement(8));
        Assert.assertEquals("Byte 09", 0x00, m.getElement(9));
        Assert.assertEquals("Byte 10", 0x00, m.getElement(10));
        Assert.assertEquals("Byte 11", 0x00, m.getElement(11));
        Assert.assertEquals("Byte 12", 0x00, m.getElement(12));
        Assert.assertEquals("Byte 13", 0x00, m.getElement(13));
        Assert.assertEquals("Byte 14", 0x00, m.getElement(14));
        Assert.assertEquals("Byte 15", 0x00, m.getElement(15));
        Assert.assertEquals("Byte 16", 0x00, m.getElement(16));
        Assert.assertEquals("Byte 17", 0x00, m.getElement(17));
        Assert.assertEquals("Byte 18", 0x00, m.getElement(18));

    }

//
    @Test
    public void testCountAndQuery() {
    
        jmri.util.JUnitUtil.fasterWaitFor(()->{return (!dpxGrpInfoImpl.isDuplexGroupQueryRunning());});

        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (((e.getPropertyName().equals("DPLXPCK_STAT_LN_UPDATE")))) {
//                    log.warn("prop change query seen");
                    propChangeQueryFlag = true;
                } else if ((e.getPropertyName().equals("DPLXPCK_NAME_UPDATE"))) {
//                    log.warn("prop change update seen");
                    propChangeReportFlag = true;
                } else if (e.getPropertyName().equals("DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR")) {
//                    log.warn("prop change update if not currently error seen");
                    propChangeReportFlag = true;
                }
                propChangeFlag = true;
                propChangeCount++;
            }
        };

        dpxGrpInfoImpl.addPropertyChangeListener(l);
        propChangeCount = 0;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;
        Assert.assertEquals("propChangeCount is reset to 0", 0, propChangeCount);

        lnis.outbound.removeAllElements();  // clear any possible previous loconet traffic

        Assert.assertEquals("propChangeCount is reset to 0", 0, propChangeCount);
        Assert.assertFalse("LDGII is not yet waiting for second UR92 Group report (1)", dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport());
        lnis.outbound.removeAllElements();
        Assert.assertEquals("propChangeCount is reset to 0", 0, propChangeCount);
        Assert.assertEquals("Num UR92s is zero", 0, dpxGrpInfoImpl.getNumUr92s());
        Assert.assertEquals("LNIS outbound queue is empty", 0, lnis.outbound.size());
        Assert.assertEquals("propChangeCount is reset to 0", 0, propChangeCount);
        dpxGrpInfoImpl.countUr92sAndQueryDuplexIdentityInfo();
        Assert.assertEquals("propChangeCount is now 18", 18, propChangeCount);
        jmri.util.JUnitUtil.waitFor(()->{return lnis.outbound.size() > 0;}, "UR92 IPL query not received");

        Assert.assertEquals("propChangeCount is now 18", 18, propChangeCount);
        Assert.assertEquals("LNIS outbound queue has one message", 1, lnis.outbound.size());

        Assert.assertTrue("LDGII is not yet waiting for second UR92 Group report (2)", dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport());
        lnis.sendTestMessage(lnis.outbound.elementAt(lnis.outbound.size()-1));

        LocoNetMessage m2 = LnIPLImplementation.createIplUr92QueryPacket();
        LocoNetMessage m = lnis.outbound.firstElement();
        Assert.assertEquals("LocoNet message has same number of bytes as UR92 IPL Query message",
                m2.getNumDataElements(), m.getNumDataElements());

        for (int i = 0; i < m2.getNumDataElements(); ++i) {
            Assert.assertEquals("Received LocoNet message byte "+i+" equals corresponding UR92 IPL Query Message byte",
                    m2.getElement(i), m.getElement(i));
        }
        lnis.sendTestMessage(m2);

        Assert.assertEquals("expect propChangeCount of 18", 18, propChangeCount);
        propChangeCount = 0;

        m = new LocoNetMessage(20);
        m.setOpCode(0xE5);
        m.setElement(1, 0x14);
        m.setElement(2, 0x0F);
        m.setElement(3, 0x10);
        m.setElement(4, 0);
        m.setElement(5, 92);
        m.setElement(6, 24);
        m.setElement(7, 0);
        m.setElement(8, 0);
        m.setElement(9, 0);
        m.setElement(10, 0);
        m.setElement(11, 0);
        m.setElement(12, 0);
        m.setElement(13, 0);
        m.setElement(14, 0);
        m.setElement(15, 0);
        m.setElement(16, 0);
        m.setElement(17, 0);
        m.setElement(18, 0);
        m.setElement(19, 0);
        Assert.assertEquals("expect propChangeCount of 0", 0, propChangeCount);
        lnis.sendLocoNetMessage(m);
        Assert.assertEquals("Num UR92s is zero", 0, dpxGrpInfoImpl.getNumUr92s());
        Assert.assertEquals("2 messages transmitted thus far", 2, lnis.outbound.size());

        Assert.assertEquals("expect propChangeCount of 0", 0, propChangeCount);

        dpxGrpInfoImpl.message(lnis.outbound.elementAt(1));  // return the LocoNet echo to the class
        Assert.assertEquals("expect propChangeCount of 1", 1, propChangeCount);
        jmri.util.JUnitUtil.waitFor(()->{return dpxGrpInfoImpl.getNumUr92s() > 0;}, "UR92 IPL reply not received");
        Assert.assertEquals("got 1 UR92 IPL report", 1, dpxGrpInfoImpl.getNumUr92s());

        Assert.assertFalse("LDGII is no longer waiting for second UR92 IPL report (3)", dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport());

        jmri.util.JUnitUtil.waitFor(()->{return lnis.outbound.size() == 3;}, "UR92 group query not received?");

        Assert.assertFalse("LDGII is no longer waiting for UR92 IPL replies (4)", dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport());
        jmri.util.JUnitUtil.waitFor(()->{return lnis.outbound.size() == 3;});
        Assert.assertEquals("message is Duplex Group Info Query - opcode", 0xe5, lnis.outbound.elementAt(2).getOpCode());
        Assert.assertEquals("message is Duplex Group Info Query - b1", 0x14, lnis.outbound.elementAt(2).getElement(1));
        Assert.assertEquals("message is Duplex Group Info Query - b2", 3, lnis.outbound.elementAt(2).getElement(2));
        Assert.assertEquals("message is Duplex Group Info Query - b3", 8, lnis.outbound.elementAt(2).getElement(3));

        dpxGrpInfoImpl.message(lnis.outbound.elementAt(2));  // echo the Duplex Group Info Query message
        Assert.assertFalse("LDGII is no longer waiting for UR92 IPL replies (5)", dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport());

        Assert.assertEquals("expect propChangeCount of 12", 12, propChangeCount);
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 12, 65);
        lnis.sendTestMessage(m);

        Assert.assertEquals("expect propChangeCount of 22", 22, propChangeCount);

        Assert.assertEquals("num outbound",3, lnis.outbound.size());

        Assert.assertEquals("got the UR92 reply info", 1,  dpxGrpInfoImpl.getNumUr92s() );

        lnis.sendTestMessage(m);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 2;});
        Assert.assertEquals("expect propChangeCount of 24", 24, propChangeCount);

        lnis.sendTestMessage(m);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 3;});

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Dcgitrax", "1234", 12, 65);
        lnis.sendTestMessage(m);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 4;});
        Assert.assertEquals("expect propChangeCount of 27", 27, propChangeCount);

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1034", 12, 65);

        lnis.sendTestMessage(m);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 5;});
        Assert.assertEquals("expect propChangeCount of 29", 29, propChangeCount);

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 13, 65);

        lnis.sendTestMessage(m);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 6;});
        Assert.assertEquals("expect propChangeCount of 31", 31, propChangeCount);

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 12, 7);

        lnis.sendTestMessage(m);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 7;});
        Assert.assertEquals("expect propChangeCount of 33", 33, propChangeCount);

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 12, 65);

        lnis.sendTestMessage(m);
        jmri.util.JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 8;});

        Assert.assertEquals("expect propChangeCount of 34", 34, propChangeCount);

        jmri.util.JUnitUtil.fasterWaitFor(()->{return (!dpxGrpInfoImpl.isDuplexGroupQueryRunning());});

        propChangeCount = 0;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;

        Assert.assertEquals("propChangeCount is reset to 0", 0, propChangeCount);
        dpxGrpInfoImpl.countUr92sAndQueryDuplexIdentityInfo();
        Assert.assertEquals("propChangeCount is now 18", 18, propChangeCount);
        dpxGrpInfoImpl.countUr92sAndQueryDuplexIdentityInfo();
        Assert.assertEquals("propChangeCount is now 18", 19, propChangeCount);


    }

    @Test
    public void testCreateDupGrpChReportMessage() {
        LocoNetMessage m;
        for (int i = 0; i < 256; ++i) {
            m = LnDplxGrpInfoImpl.createUr92GroupChannelReportPacket(i);
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Opcode is 0xe5", 0xe5, m.getOpCode());
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 1 is 0x14", 0x14, m.getElement(1));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 2 is 0x2", 0x2, m.getElement(2));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 3 is 0x10", 0x10, m.getElement(3));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 4 is "+ ((i > 127) ? 1 : 0),
                    (i > 127) ? 1 : 0, m.getElement(4));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 5 is " + (i & 0x7f),
                    i & 0x7F, m.getElement(5));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 6 is 0x0", 0x0, m.getElement(6));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 7 is 0x0", 0x0, m.getElement(7));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 8 is 0x0", 0x0, m.getElement(8));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 9 is 0x0", 0x0, m.getElement(9));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 10 is 0x0", 0x0, m.getElement(10));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 11 is 0x0", 0x0, m.getElement(11));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 12 is 0x0", 0x0, m.getElement(12));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 13 is 0x0", 0x0, m.getElement(13));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 14 is 0x0", 0x0, m.getElement(14));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 15 is 0x0", 0x0, m.getElement(15));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 16 is 0x0", 0x0, m.getElement(16));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 17 is 0x0", 0x0, m.getElement(17));
            Assert.assertEquals("iteration "+i+"Group Channel Report Packet Byte 18 is 0x0", 0x0, m.getElement(18));
        }
    }

    @Test
    public void testCreateDupGrpIDReportMessage() {
        LocoNetMessage m;
        for (int i = 0; i < 256; ++i) {
            m = LnDplxGrpInfoImpl.createUr92GroupIdReportPacket(i);
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Opcode is 0xe5", 0xe5, m.getOpCode());
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 1 is 0x14", 0x14, m.getElement(1));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 2 is 0x4", 0x4, m.getElement(2));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 3 is 0x10", 0x10, m.getElement(3));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 4 is "+ ((i > 127) ? 1 : 0),
                    (i > 127) ? 1 : 0, m.getElement(4));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 5 is " + (i & 0x7f),
                    i & 0x7F, m.getElement(5));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 6 is 0x0", 0x0, m.getElement(6));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 7 is 0x0", 0x0, m.getElement(7));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 8 is 0x0", 0x0, m.getElement(8));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 9 is 0x0", 0x0, m.getElement(9));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 10 is 0x0", 0x0, m.getElement(10));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 11 is 0x0", 0x0, m.getElement(11));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 12 is 0x0", 0x0, m.getElement(12));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 13 is 0x0", 0x0, m.getElement(13));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 14 is 0x0", 0x0, m.getElement(14));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 15 is 0x0", 0x0, m.getElement(15));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 16 is 0x0", 0x0, m.getElement(16));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 17 is 0x0", 0x0, m.getElement(17));
            Assert.assertEquals("iteration "+i+"Group ID Report Packet Byte 18 is 0x0", 0x0, m.getElement(18));
        }
    }

    @Test
    public void testExtractDuplexGroupPassword() {
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

        LocoNetMessage m = new LocoNetMessage(20);
        m.setElement(0, 0xe5);
        m.setElement(1, 0x14);
        for (int i=2; i<20;++i) {
            m.setElement(i, 0);
        }
        String testString;
        String result;
        for (int i=0; i<13; ++i) {
            testString = conversion2[i]+"000";
            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, (i>7)?1:0);
            m.setElement(15, (i & 7) << 4);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Name etc Report "+m+"first char iteration "+i,testString, result);

            m.setElement(2, 3);
            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertNull("Group Name etc Write first char iteration "+i, result);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Write "+m+"first char iteration "+i,testString, result);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            m.setElement(4, 0);
            m.setElement(5, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Report "+m+"first char iteration "+i,testString, result);
        }

        for (int i=0; i<13; ++i) {
            testString = "0"+conversion2[i]+"00";
            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, 0);
            m.setElement(15, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Name etc Report "+m+"second char iteration "+i,testString, result);

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertNull("Group Name Write "+m+"second char iteration "+i,result);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, 0);
            m.setElement(6, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Write "+m+"second char iteration "+i,testString, result);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Report "+m+"second char iteration "+i,testString, result);
        }

        for (int i=0; i<13; ++i) {
            testString = "00"+conversion2[i]+"0";
            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, (i>7)?2:0);
            m.setElement(15, 0);
            m.setElement(16, (i & 7) << 4);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Name etc Report "+m+"third char iteration "+i,testString, result);

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertNull("Group Name Write "+m+"third char iteration "+i,result);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, 0);
            m.setElement(6, 0);
            m.setElement(7, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Write "+m+"third char iteration "+i,testString, result);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Report "+m+"third char iteration "+i,testString, result);
        }

        for (int i=0; i<13; ++i) {
            testString = "000"+conversion2[i];
            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, 0);
            m.setElement(15, 0);
            m.setElement(16, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Name etc Report "+m+"fourth char iteration "+i,testString, result);

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertNull("Group Name Write "+m+"fourth char iteration "+i,result);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, 0);
            m.setElement(6, 0);
            m.setElement(7, 0);
            m.setElement(8, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Write "+m+"fourth char iteration "+i,testString, result);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            Assert.assertEquals("Group Pw Report "+m+"fourth char iteration "+i,testString, result);
        }

        m = new LocoNetMessage(2);
        m.setOpCode(0x81);
        m.setElement(1, 0);
        result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
        Assert.assertNull("Group Pw Report "+m, result);
    }


    @Test
    public void testExtractDuplexGroupChannel() {
        int result;

        LocoNetMessage m = new LocoNetMessage(20);

        m.setOpCode(0xe5);
        m.setElement(1,0x14);
        m.setElement(2, 0x2);
        m.setElement(3, 0x10);

        for (int i = 4; i < 0x14; ++i){
            m.setElement(i, 0);
        }

        for (int i = 0; i < 256; ++i) {
            m.setElement(2, 2);
            m.setElement(4, (i>127) ? 1:0);
            m.setElement(5, i&0x7f);

            result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
            Assert.assertEquals("iteration "+i+" extracted channel", i, result);

            m.setElement(2, 3);
            m.setElement(14, (i>127) ? 4:0);
            m.setElement(17, (i&0x7f));
            result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
            Assert.assertEquals("extracted channel should be invalid for name operation",
                    i, result);
        }

        int expectedResult = -1;
        m.setElement(2, 0);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        Assert.assertEquals("extracted channel should be invalid for invalid operation",
                expectedResult, result);

        m.setElement(2, 4);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        Assert.assertEquals("etracted channel should be invalid for password operation",
                expectedResult, result);

        m.setElement(2, 7);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        Assert.assertEquals("etracted channel should be invalid for ID operation",
                expectedResult, result);

        m = new LocoNetMessage(2);
        m.setOpCode(0x82);
        m.setElement(1, 0);
        Assert.assertEquals("interpret bad channel message", expectedResult,
                LnDplxGrpInfoImpl.extractDuplexGroupChannel(m));
    }

    @Test
    public void testExtractDuplexGroupID() {
        int result;
        int exceptionalResult = -1;

        LocoNetMessage m = new LocoNetMessage(20);

        m.setOpCode(0xe5);
        m.setElement(1,0x14);

        for (int i = 4; i < 0x14; ++i){
            m.setElement(i, 0);
        }

        for (int i = 0; i < 256; ++i) {
            m.setElement(2, 0x4);
            m.setElement(3, 0);
            m.setElement(4, (i>127) ? 1:0);
            m.setElement(5, i&0x7f);

            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            Assert.assertEquals("iteration "+i+" ID write operation", i, result);

            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            Assert.assertEquals("iteration "+i+" ID report operation", i, result);

            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, (i>127) ? 8:0);
            m.setElement(18, (i&0x7f));
            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            Assert.assertEquals("iteration "+i+" ID from group name etc read operation",
                    i, result);

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            Assert.assertEquals("iteration "+i+" ID from group name etc write operation",
                    exceptionalResult, result);
        }

        m.setElement(2, 0);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        Assert.assertEquals("extracted channel should be invalid for invalid operation",
                exceptionalResult, result);

        m.setElement(2, 4);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        Assert.assertEquals("etracted channel should be invalid for password operation",
                exceptionalResult, result);

        m.setElement(2, 7);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        Assert.assertEquals("etracted channel should be invalid for ID operation",
                exceptionalResult, result);

        m = new LocoNetMessage(2);
        m.setOpCode(0x82);
        m.setElement(1, 0);
        Assert.assertEquals("interpret bad channel message", exceptionalResult,
                LnDplxGrpInfoImpl.extractDuplexGroupChannel(m));

    }

    @Test
    public void testExtractDuplexGroupName() {
    LocoNetMessage m = new LocoNetMessage(0x14);
    Assert.assertNull("expect null for empty message",LnDplxGrpInfoImpl.extractDuplexGroupName(m));
    }

    @Test
    public void testSetDuplexGroupName() {

        try {
            dpxGrpInfoImpl.setDuplexGroupName("");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.assertTrue("got an exception as intended", true);
        }

        lnis.outbound.removeAllElements();
        Assert.assertEquals("outbound queue is empty", 0, lnis.outbound.size());
        try {
            dpxGrpInfoImpl.setDuplexGroupName("deadBeeF");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("got an unexpected exception");
        }
        Assert.assertEquals("outbound queue is not empty", 1, lnis.outbound.size());
        Assert.assertEquals("outbound queue opcode is correct", 0xe5, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("outbound queue byte 1 is correct", 0x14, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("outbound queue byte 2 is correct", 0x03, lnis.outbound.get(0).getElement(2));
        Assert.assertEquals("outbound queue byte 3 is correct", 0x00, lnis.outbound.get(0).getElement(3));
        Assert.assertEquals("outbound queue byte 4 is correct", 0x00, lnis.outbound.get(0).getElement(4));
        Assert.assertEquals("outbound queue byte 5 is correct", 'd', lnis.outbound.get(0).getElement(5));
        Assert.assertEquals("outbound queue byte 6 is correct", 'e', lnis.outbound.get(0).getElement(6));
        Assert.assertEquals("outbound queue byte 7 is correct", 'a', lnis.outbound.get(0).getElement(7));
        Assert.assertEquals("outbound queue byte 8 is correct", 'd', lnis.outbound.get(0).getElement(8));
        Assert.assertEquals("outbound queue byte 9 is correct", 0x00, lnis.outbound.get(0).getElement(9));
        Assert.assertEquals("outbound queue byte 10 is correct", 'B', lnis.outbound.get(0).getElement(10));
        Assert.assertEquals("outbound queue byte 11 is correct", 'e', lnis.outbound.get(0).getElement(11));
        Assert.assertEquals("outbound queue byte 12 is correct", 'e', lnis.outbound.get(0).getElement(12));
        Assert.assertEquals("outbound queue byte 13 is correct", 'F', lnis.outbound.get(0).getElement(13));

    }

    @Test
    public void testSetDuplexGroupChannel() {

        try {
            dpxGrpInfoImpl.setDuplexGroupChannel(-1);
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.assertTrue("got an exception as intended", true);
        }

        lnis.outbound.removeAllElements();
        Assert.assertEquals("outbound queue is empty", 0, lnis.outbound.size());
        try {
            dpxGrpInfoImpl.setDuplexGroupChannel(13);
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("got an unexpected exception");
        }
        Assert.assertEquals("outbound queue is not empty", 1, lnis.outbound.size());
        Assert.assertEquals("outbound queue opcode is correct", 0xe5, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("outbound queue byte 1 is correct", 0x14, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("outbound queue byte 2 is correct", 0x02, lnis.outbound.get(0).getElement(2));
        Assert.assertEquals("outbound queue byte 3 is correct", 0x00, lnis.outbound.get(0).getElement(3));
        Assert.assertEquals("outbound queue byte 4 is correct", 0x00, lnis.outbound.get(0).getElement(4));
        Assert.assertEquals("outbound queue byte 5 is correct", 13, lnis.outbound.get(0).getElement(5));
        Assert.assertEquals("outbound queue byte 6 is correct", 0, lnis.outbound.get(0).getElement(6));
        Assert.assertEquals("outbound queue byte 7 is correct", 0, lnis.outbound.get(0).getElement(7));
        Assert.assertEquals("outbound queue byte 8 is correct", 0, lnis.outbound.get(0).getElement(8));
        Assert.assertEquals("outbound queue byte 9 is correct", 0x00, lnis.outbound.get(0).getElement(9));
        Assert.assertEquals("outbound queue byte 10 is correct", 0, lnis.outbound.get(0).getElement(10));
        Assert.assertEquals("outbound queue byte 11 is correct", 0, lnis.outbound.get(0).getElement(11));
        Assert.assertEquals("outbound queue byte 12 is correct", 0, lnis.outbound.get(0).getElement(12));
        Assert.assertEquals("outbound queue byte 13 is correct", 0, lnis.outbound.get(0).getElement(13));

    }

    @Test
    public void testSetDuplexGroupID() {

        try {
            dpxGrpInfoImpl.setDuplexGroupId("999");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.assertTrue("got an exception as intended", true);
        }

        lnis.outbound.removeAllElements();
        Assert.assertEquals("outbound queue is empty", 0, lnis.outbound.size());
        try {
            dpxGrpInfoImpl.setDuplexGroupId("7");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("got an unexpected exception");
        }
        Assert.assertEquals("outbound queue is not empty", 1, lnis.outbound.size());
        Assert.assertEquals("outbound queue opcode is correct", 0xe5, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("outbound queue byte 1 is correct", 0x14, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("outbound queue byte 2 is correct", 0x04, lnis.outbound.get(0).getElement(2));
        Assert.assertEquals("outbound queue byte 3 is correct", 0x00, lnis.outbound.get(0).getElement(3));
        Assert.assertEquals("outbound queue byte 4 is correct", 0x00, lnis.outbound.get(0).getElement(4));
        Assert.assertEquals("outbound queue byte 5 is correct", 7, lnis.outbound.get(0).getElement(5));
        Assert.assertEquals("outbound queue byte 6 is correct", 0, lnis.outbound.get(0).getElement(6));
        Assert.assertEquals("outbound queue byte 7 is correct", 0, lnis.outbound.get(0).getElement(7));
        Assert.assertEquals("outbound queue byte 8 is correct", 0, lnis.outbound.get(0).getElement(8));
        Assert.assertEquals("outbound queue byte 9 is correct", 0x00, lnis.outbound.get(0).getElement(9));
        Assert.assertEquals("outbound queue byte 10 is correct", 0, lnis.outbound.get(0).getElement(10));
        Assert.assertEquals("outbound queue byte 11 is correct", 0, lnis.outbound.get(0).getElement(11));
        Assert.assertEquals("outbound queue byte 12 is correct", 0, lnis.outbound.get(0).getElement(12));
        Assert.assertEquals("outbound queue byte 13 is correct", 0, lnis.outbound.get(0).getElement(13));

    }

    @Test
    public void testSetDuplexGroupPassword() {

        try {
            dpxGrpInfoImpl.setDuplexGroupPassword("abcd");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.assertTrue("got an exception as intended", true);
        }

        lnis.outbound.removeAllElements();
        Assert.assertEquals("outbound queue is empty", 0, lnis.outbound.size());
        try {
            dpxGrpInfoImpl.setDuplexGroupPassword("0123");
        } catch (jmri.jmrix.loconet.LocoNetException e) {
            Assert.fail("got an unexpected exception");
        }
        Assert.assertEquals("outbound queue is not empty", 1, lnis.outbound.size());
        Assert.assertEquals("outbound queue opcode is correct", 0xe5, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("outbound queue byte 1 is correct", 0x14, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("outbound queue byte 2 is correct", 0x07, lnis.outbound.get(0).getElement(2));
        Assert.assertEquals("outbound queue byte 3 is correct", 0x00, lnis.outbound.get(0).getElement(3));
        Assert.assertEquals("outbound queue byte 4 is correct", 0x00, lnis.outbound.get(0).getElement(4));
        Assert.assertEquals("outbound queue byte 5 is correct", '0', lnis.outbound.get(0).getElement(5));
        Assert.assertEquals("outbound queue byte 6 is correct", '1', lnis.outbound.get(0).getElement(6));
        Assert.assertEquals("outbound queue byte 7 is correct", '2', lnis.outbound.get(0).getElement(7));
        Assert.assertEquals("outbound queue byte 8 is correct", '3', lnis.outbound.get(0).getElement(8));
        Assert.assertEquals("outbound queue byte 9 is correct", 0x00, lnis.outbound.get(0).getElement(9));
        Assert.assertEquals("outbound queue byte 10 is correct", 0, lnis.outbound.get(0).getElement(10));
        Assert.assertEquals("outbound queue byte 11 is correct", 0, lnis.outbound.get(0).getElement(11));
        Assert.assertEquals("outbound queue byte 12 is correct", 0, lnis.outbound.get(0).getElement(12));
        Assert.assertEquals("outbound queue byte 13 is correct", 0, lnis.outbound.get(0).getElement(13));

    }



// Yet To Be Tested:
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//    public static Integer extractDuplexGroupChannel(LocoNetMessage m) {
//    public static Integer extractDuplexGroupID(LocoNetMessage m) {
//    public void message(LocoNetMessage m) {
//    public void queryDuplexGroupIdentity() {
//    public void setDuplexGroupChannel(Integer dgc) throws jmri.jmrix.loconet.LocoNetException {
//    public void setDuplexGroupPassword(String dgp) throws jmri.jmrix.loconet.LocoNetException {
//    public void setDuplexGroupId(String dgi) throws jmri.jmrix.loconet.LocoNetException {
//    public int getMessagesHandled() {
//    public boolean isIplQueryTimerRunning() {
//    public boolean isDuplexGroupQueryRunning() {
//    public void connect(jmri.jmrix.loconet.LnTrafficController t) {
//    public void dispose() {

    @Before
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);

        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);
        // memo.configureManagers(); // Skip this step, else autonomous loconet traffic is generated!
        jmri.InstanceManager.store(memo,jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        dpxGrpInfoImpl = new LnDplxGrpInfoImpl(memo);

    }

    @After
    public void tearDown() {
        dpxGrpInfoImpl.dispose();
        dpxGrpInfoImpl = null;
        lnis = null;
        memo.dispose();
        memo=null;
        
        JUnitUtil.tearDown();
    }

//    private final static Logger log = LoggerFactory.getLogger(LnDplxGrpInfoImplTest.class);
}
