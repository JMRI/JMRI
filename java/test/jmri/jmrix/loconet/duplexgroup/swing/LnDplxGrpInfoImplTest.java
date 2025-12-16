package jmri.jmrix.loconet.duplexgroup.swing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.jmrix.loconet.LocoNetException;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.duplexgroup.DuplexGroupMessageType;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LnDplxGrpInfoImpl
 *
 * @author Paul Bender Copyright (C) 2016
 * @author      B. Milhaupt Copyright (C) 2018
 */
public class LnDplxGrpInfoImplTest {

    private jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis;
    private jmri.jmrix.loconet.LocoNetSystemConnectionMemo memo;
    private LnDplxGrpInfoImpl dpxGrpInfoImpl;
    private boolean propChangeQueryFlag;
    private boolean propChangeReportFlag;
    private boolean propChangeFlag;
    private int propChangeCount;

    @Test
    public void testCtor() {
        assertNotNull( dpxGrpInfoImpl, "exists");
        assertEquals( 0, dpxGrpInfoImpl.getNumUr92s(),
            "Ctor zeroed number of UR92s");
        assertFalse( dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport(),
            "Ctor cleard 'waiting for IPL query replies' flag");
        assertEquals( 0, dpxGrpInfoImpl.getMessagesHandled(),
            "Ctor has reset 'messagesHandled'");

        assertEquals( "", dpxGrpInfoImpl.getFetchedDuplexGroupName(),
            "verify initialization of acceptedGroupName");
        assertEquals( "", dpxGrpInfoImpl.getFetchedDuplexGroupChannel(),
            "verify initialization of acceptedGroupChannel");
        assertEquals( "", dpxGrpInfoImpl.getFetchedDuplexGroupPassword(),
            "verify initialization of acceptedGroupPassword");
        assertEquals( "", dpxGrpInfoImpl.getFetchedDuplexGroupId(),
            "verify initialization of acceptedGroupId");

        lnis.notify(new LocoNetMessage(new int[] {0x81, 0x00}));
        JUnitUtil.fasterWaitFor(()->{
            return dpxGrpInfoImpl.getMessagesHandled() >= 1;},
                "LocoNetListener is registered");
        assertFalse( dpxGrpInfoImpl.isIplQueryTimerRunning(), "IPL Query timer is not running");
        assertFalse( dpxGrpInfoImpl.isDuplexGroupQueryRunning(), "Duplex Group Info Query timer is not running");

        dpxGrpInfoImpl.dispose();

        for (LocoNetListener listener : lnis.getListeners()) {
            assertNotSame( listener, dpxGrpInfoImpl, "dispose did not remove listener");
        }
    }

    @Test
    public void testMiscellaneousStuff() {
        assertFalse( LnDplxGrpInfoImpl.isPasswordLimitedToNumbers(), "limit Password to Numeric-only");

    }

    @Test
    public void testValidateDuplexGroupName() {
        assertFalse( LnDplxGrpInfoImpl.validateGroupName(""), "Check Group name: empty string");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName(" "), "Check Group name: 1 character");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("\n"), "Check Group name: only newline");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("\r"), "Check Group name: only linefeed");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("\177"), "Check Group name: 1 oddball character");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("ab"), "Check Group name: 2 characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("abc"), "Check Group name: 3 characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("abcd"), "Check Group name: 4 characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("efghi"), "Check Group name: 5 characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("jklmno"), "Check Group name: 6 characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("pqrstuv"), "Check Group name: 7 characters");
        assertTrue ( LnDplxGrpInfoImpl.validateGroupName("ZYXWVUTS"), "Check Group name: 8 characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("123456789"), "Check Group name: 9 characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupName("          "), "Check Group name: 10 characters");
        assertTrue ( LnDplxGrpInfoImpl.validateGroupName("        "), "Check Group name: 8 spaces");
        assertTrue ( LnDplxGrpInfoImpl.validateGroupName("\0\0\0\0\0\0\0\0"), "Check Group name: 8 nulls");
        assertTrue( LnDplxGrpInfoImpl.validateGroupName("\n\n\n\n\n\n\n\n"), "Check Group name: 8 newlines");
        assertTrue( LnDplxGrpInfoImpl.validateGroupName("dEadb33F"), "Check Group name: 8 characters - random");
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
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword(testString), "Check Group Password: zero characters");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("A"), "Check Group Password: A");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("A0"), "Check Group Password: A0");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("A0C"), "Check Group Password: A0C");
        assertTrue( LnDplxGrpInfoImpl.validateGroupPassword("A0AA"), "Check Group Password: A0AA");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("a0BB"), "Check Group Password: a0BB");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("AbCB"), "Check Group Password: AbCB");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("A0cB"), "Check Group Password: A0cB");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("A09c"), "Check Group Password: A09c");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("12345"));
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("123456"), "Check Group Password: 123456");
        assertFalse( LnDplxGrpInfoImpl.validateGroupPassword("1234567"), "Check Group Password: 1234567");

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
                        assertTrue( LnDplxGrpInfoImpl.validateGroupPassword(testString),
                            "Check Group Password: "+testString);

                    }
                }
            }
        }
    }

    @Test
    public void testValidateChannelNumber() {
        for (int i = 0; i < 256; ++i) {
            assertEquals( ((i >=11)&& (i <=26)),
                    LnDplxGrpInfoImpl.validateGroupChannel(i),
                    "Channel Valid check for channel "+i);
        }
    }

    @Test
    public void testValidateGroupIDNumber() {
        for (int i = -1; i < 256; ++i) {
            assertEquals( ((i >=0)&& (i <=127)),
                    LnDplxGrpInfoImpl.validateGroupID(i),
                    "Channel Group ID check for ID "+i);
        }
    }

    @Test
    public void checkCreateUr92GroupIdentityQueryPacket() {
        LocoNetMessage m = LnDplxGrpInfoImpl.createUr92GroupIdentityQueryPacket();
        assertEquals( 0xe5, m.getElement(0), "Group Id Query Message: opcode");
        assertEquals( 0x14, m.getElement(1), "Group Id Query Message: byte 1");
        assertEquals( 0x03, m.getElement(2), "Group Id Query Message: byte 2");
        assertEquals( 0x08, m.getElement(3), "Group Id Query Message: byte 3");
        assertEquals( 0x00, m.getElement(4), "Group Id Query Message: byte 4");
        assertEquals( 0x00, m.getElement(5), "Group Id Query Message: byte 5");
        assertEquals( 0x00, m.getElement(6), "Group Id Query Message: byte 6");
        assertEquals( 0x00, m.getElement(7), "Group Id Query Message: byte 7");
        assertEquals( 0x00, m.getElement(8), "Group Id Query Message: byte 8");
        assertEquals( 0x00, m.getElement(9), "Group Id Query Message: byte 9");
        assertEquals( 0x00, m.getElement(10), "Group Id Query Message: byte 10");
        assertEquals( 0x00, m.getElement(11), "Group Id Query Message: byte 11");
        assertEquals( 0x00, m.getElement(12), "Group Id Query Message: byte 12");
        assertEquals( 0x00, m.getElement(13), "Group Id Query Message: byte 13");
        assertEquals( 0x00, m.getElement(14), "Group Id Query Message: byte 14");
        assertEquals( 0x00, m.getElement(15), "Group Id Query Message: byte 15");
        assertEquals( 0x00, m.getElement(16), "Group Id Query Message: byte 16");
        assertEquals( 0x00, m.getElement(17), "Group Id Query Message: byte 17");
        assertEquals( 0x00, m.getElement(18), "Group Id Query Message: byte 18");
        assertEquals( 0x00, m.getElement(19), "Group Id Query Message: byte 19");
    }

    @Test
    public void checkCreateSetUr92GroupNamePacket() {
        char[] cs = new char[8];
        for (int i = 0; i < 8; ++i) {
            cs[i] = '0';
        }
        String testString;
        String testStringA = new String(cs);
        LocoNetMessage m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringA),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringA+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringA+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringA+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringA+": byte 3");
        assertEquals( 0x00, m.getElement(4), "Set Group Name to "+testStringA+": byte 4");
        assertEquals(  '0', m.getElement(5), "Set Group Name to "+testStringA+": byte 5");
        assertEquals(  '0', m.getElement(6), "Set Group Name to "+testStringA+": byte 6");
        assertEquals(  '0', m.getElement(7), "Set Group Name to "+testStringA+": byte 7");
        assertEquals(  '0', m.getElement(8), "Set Group Name to "+testStringA+": byte 8");
        assertEquals( 0x00, m.getElement(9), "Set Group Name to "+testStringA+": byte 9");
        assertEquals(  '0', m.getElement(10), "Set Group Name to "+testStringA+": byte 10");
        assertEquals(  '0', m.getElement(11), "Set Group Name to "+testStringA+": byte 11");
        assertEquals(  '0', m.getElement(12), "Set Group Name to "+testStringA+": byte 12");
        assertEquals(  '0', m.getElement(13), "Set Group Name to "+testStringA+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringA+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringA+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringA+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringA+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringA+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringA+": byte 19");

        String testStringB = "DEADBeef";
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringB),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringB+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringB+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringB+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringB+": byte 3");
        assertEquals( 0x00, m.getElement(4), "Set Group Name to "+testStringB+": byte 4");
        assertEquals(  'D', m.getElement(5), "Set Group Name to "+testStringB+": byte 5");
        assertEquals(  'E', m.getElement(6), "Set Group Name to "+testStringB+": byte 6");
        assertEquals(  'A', m.getElement(7), "Set Group Name to "+testStringB+": byte 7");
        assertEquals(  'D', m.getElement(8), "Set Group Name to "+testStringB+": byte 8");
        assertEquals( 0x00, m.getElement(9), "Set Group Name to "+testStringB+": byte 9");
        assertEquals(  'B', m.getElement(10), "Set Group Name to "+testStringB+": byte 10");
        assertEquals(  'e', m.getElement(11), "Set Group Name to "+testStringB+": byte 11");
        assertEquals(  'e', m.getElement(12), "Set Group Name to "+testStringB+": byte 12");
        assertEquals(  'f', m.getElement(13), "Set Group Name to "+testStringB+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringB+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringB+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringB+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringB+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringB+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringB+": byte 19");


        Exception ex = assertThrows( LocoNetException.class,
            () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket("EADBeef"),
            "should have failed account short group name string()");
        assertNotNull(ex);

        ex = assertThrows( LocoNetException.class,
            () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket("DEADBeef2"),
            "should have failed account long group name string()");
        assertNotNull(ex);

        ex = assertThrows( LocoNetException.class,
            () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(""),
            "should have failed account short group name string()");
        assertNotNull(ex);

        ex = assertThrows( LocoNetException.class,
            () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket("1"),
            "should have failed account short group name string()");
        assertNotNull(ex);

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[0] = 128;
        String testStringC = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringC),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringC+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringC+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringC+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringC+": byte 3");
        assertEquals( 0x01, m.getElement(4), "Set Group Name to "+testStringC+": byte 4");
        assertEquals( 0x00, m.getElement(5), "Set Group Name to "+testStringC+": byte 5");
        assertEquals(  'i', m.getElement(6), "Set Group Name to "+testStringC+": byte 6");
        assertEquals(  'd', m.getElement(7), "Set Group Name to "+testStringC+": byte 7");
        assertEquals(  'u', m.getElement(8), "Set Group Name to "+testStringC+": byte 8");
        assertEquals( 0x00, m.getElement(9), "Set Group Name to "+testStringC+": byte 9");
        assertEquals(  'c', m.getElement(10), "Set Group Name to "+testStringC+": byte 10");
        assertEquals(  'i', m.getElement(11), "Set Group Name to "+testStringC+": byte 11");
        assertEquals(  'a', m.getElement(12), "Set Group Name to "+testStringC+": byte 12");
        assertEquals(  'l', m.getElement(13), "Set Group Name to "+testStringC+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringC+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringC+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringC+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringC+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringC+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringC+": byte 19");

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[1] = 129;
        String testStringD = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringD),
            "failed account exception thrown by createSetUr92GroupNamePacket()");
        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringD+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringD+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringD+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringD+": byte 3");
        assertEquals( 0x02, m.getElement(4), "Set Group Name to "+testStringD+": byte 4");
        assertEquals(  'f', m.getElement(5), "Set Group Name to "+testStringD+": byte 5");
        assertEquals( 0x01, m.getElement(6), "Set Group Name to "+testStringD+": byte 6");
        assertEquals(  'd', m.getElement(7), "Set Group Name to "+testStringD+": byte 7");
        assertEquals(  'u', m.getElement(8), "Set Group Name to "+testStringD+": byte 8");
        assertEquals( 0x00, m.getElement(9), "Set Group Name to "+testStringD+": byte 9");
        assertEquals(  'c', m.getElement(10), "Set Group Name to "+testStringD+": byte 10");
        assertEquals(  'i', m.getElement(11), "Set Group Name to "+testStringD+": byte 11");
        assertEquals(  'a', m.getElement(12), "Set Group Name to "+testStringD+": byte 12");
        assertEquals(  'l', m.getElement(13), "Set Group Name to "+testStringD+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringD+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringD+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringD+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringD+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringD+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringD+": byte 19");

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[2] = 129;
        String testStringE = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringE),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringE+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringE+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringE+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringE+": byte 3");
        assertEquals( 0x04, m.getElement(4), "Set Group Name to "+testStringE+": byte 4");
        assertEquals(  'f', m.getElement(5), "Set Group Name to "+testStringE+": byte 5");
        assertEquals(  'i', m.getElement(6), "Set Group Name to "+testStringE+": byte 6");
        assertEquals( 0x01, m.getElement(7), "Set Group Name to "+testStringE+": byte 7");
        assertEquals(  'u', m.getElement(8), "Set Group Name to "+testStringE+": byte 8");
        assertEquals( 0x00, m.getElement(9), "Set Group Name to "+testStringE+": byte 9");
        assertEquals(  'c', m.getElement(10), "Set Group Name to "+testStringE+": byte 10");
        assertEquals(  'i', m.getElement(11), "Set Group Name to "+testStringE+": byte 11");
        assertEquals(  'a', m.getElement(12), "Set Group Name to "+testStringE+": byte 12");
        assertEquals(  'l', m.getElement(13), "Set Group Name to "+testStringE+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringE+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringE+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringE+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringE+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringE+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringE+": byte 19");

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[3] = 130;
        String testStringF = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringF),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringF+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringF+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringF+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringF+": byte 3");
        assertEquals( 0x08, m.getElement(4), "Set Group Name to "+testStringF+": byte 4");
        assertEquals(  'f', m.getElement(5), "Set Group Name to "+testStringF+": byte 5");
        assertEquals(  'i', m.getElement(6), "Set Group Name to "+testStringF+": byte 6");
        assertEquals(  'd', m.getElement(7), "Set Group Name to "+testStringF+": byte 7");
        assertEquals( 0x02, m.getElement(8), "Set Group Name to "+testStringF+": byte 8");
        assertEquals( 0x00, m.getElement(9), "Set Group Name to "+testStringF+": byte 9");
        assertEquals(  'c', m.getElement(10), "Set Group Name to "+testStringF+": byte 10");
        assertEquals(  'i', m.getElement(11), "Set Group Name to "+testStringF+": byte 11");
        assertEquals(  'a', m.getElement(12), "Set Group Name to "+testStringF+": byte 12");
        assertEquals(  'l', m.getElement(13), "Set Group Name to "+testStringF+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringF+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringF+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringF+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringF+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringF+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringF+": byte 19");

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[4] = 132;
        String testStringG = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringG),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringG+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringG+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringG+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringG+": byte 3");
        assertEquals( 0x00, m.getElement(4), "Set Group Name to "+testStringG+": byte 4");
        assertEquals(  'f', m.getElement(5), "Set Group Name to "+testStringG+": byte 5");
        assertEquals(  'i', m.getElement(6), "Set Group Name to "+testStringG+": byte 6");
        assertEquals(  'd', m.getElement(7), "Set Group Name to "+testStringG+": byte 7");
        assertEquals(  'u', m.getElement(8), "Set Group Name to "+testStringG+": byte 8");
        assertEquals( 0x01, m.getElement(9), "Set Group Name to "+testStringG+": byte 9");
        assertEquals( 0x04, m.getElement(10), "Set Group Name to "+testStringG+": byte 10");
        assertEquals(  'i', m.getElement(11), "Set Group Name to "+testStringG+": byte 11");
        assertEquals(  'a', m.getElement(12), "Set Group Name to "+testStringG+": byte 12");
        assertEquals(  'l', m.getElement(13), "Set Group Name to "+testStringG+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringG+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringG+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringG+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringG+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringG+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringG+": byte 19");

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[5] = 136;
        String testStringH = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringH),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringH+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringH+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringH+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringH+": byte 3");
        assertEquals( 0x00, m.getElement(4), "Set Group Name to "+testStringH+": byte 4");
        assertEquals(  'f', m.getElement(5), "Set Group Name to "+testStringH+": byte 5");
        assertEquals(  'i', m.getElement(6), "Set Group Name to "+testStringH+": byte 6");
        assertEquals(  'd', m.getElement(7), "Set Group Name to "+testStringH+": byte 7");
        assertEquals(  'u', m.getElement(8), "Set Group Name to "+testStringH+": byte 8");
        assertEquals( 0x02, m.getElement(9), "Set Group Name to "+testStringH+": byte 9");
        assertEquals(  'c', m.getElement(10), "Set Group Name to "+testStringH+": byte 10");
        assertEquals( 0x08, m.getElement(11), "Set Group Name to "+testStringH+": byte 11");
        assertEquals(  'a', m.getElement(12), "Set Group Name to "+testStringH+": byte 12");
        assertEquals(  'l', m.getElement(13), "Set Group Name to "+testStringH+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringH+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringH+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringH+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringH+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringH+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringH+": byte 19");

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[6] = 128+16;
        String testStringI = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringI),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringI+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringI+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringI+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringI+": byte 3");
        assertEquals( 0x00, m.getElement(4), "Set Group Name to "+testStringI+": byte 4");
        assertEquals(  'f', m.getElement(5), "Set Group Name to "+testStringI+": byte 5");
        assertEquals(  'i', m.getElement(6), "Set Group Name to "+testStringI+": byte 6");
        assertEquals(  'd', m.getElement(7), "Set Group Name to "+testStringI+": byte 7");
        assertEquals(  'u', m.getElement(8), "Set Group Name to "+testStringI+": byte 8");
        assertEquals( 0x04, m.getElement(9), "Set Group Name to "+testStringI+": byte 9");
        assertEquals(  'c', m.getElement(10), "Set Group Name to "+testStringI+": byte 10");
        assertEquals(  'i', m.getElement(11), "Set Group Name to "+testStringI+": byte 11");
        assertEquals( 0x10, m.getElement(12), "Set Group Name to "+testStringI+": byte 12");
        assertEquals(  'l', m.getElement(13), "Set Group Name to "+testStringI+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringI+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringI+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringI+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringI+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringI+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringI+": byte 19");

        testString = "fiducial";
        cs = testString.toCharArray();
        cs[7] = 128+32;
        String testStringJ = new String(cs);
        m = assertDoesNotThrow( () -> LnDplxGrpInfoImpl.createSetUr92GroupNamePacket(testStringJ),
            "failed account exception thrown by createSetUr92GroupNamePacket()");

        assertEquals( 0xe5, m.getElement(0), "Set Group Name to "+testStringJ+": opcode");
        assertEquals( 0x14, m.getElement(1), "Set Group Name to "+testStringJ+": byte 1");
        assertEquals( 0x03, m.getElement(2), "Set Group Name to "+testStringJ+": byte 2");
        assertEquals( 0x00, m.getElement(3), "Set Group Name to "+testStringJ+": byte 3");
        assertEquals( 0x00, m.getElement(4), "Set Group Name to "+testStringJ+": byte 4");
        assertEquals(  'f', m.getElement(5), "Set Group Name to "+testStringJ+": byte 5");
        assertEquals(  'i', m.getElement(6), "Set Group Name to "+testStringJ+": byte 6");
        assertEquals(  'd', m.getElement(7), "Set Group Name to "+testStringJ+": byte 7");
        assertEquals(  'u', m.getElement(8), "Set Group Name to "+testStringJ+": byte 8");
        assertEquals( 0x08, m.getElement(9), "Set Group Name to "+testStringJ+": byte 9");
        assertEquals(  'c', m.getElement(10), "Set Group Name to "+testStringJ+": byte 10");
        assertEquals(  'i', m.getElement(11), "Set Group Name to "+testStringJ+": byte 11");
        assertEquals(  'a', m.getElement(12), "Set Group Name to "+testStringJ+": byte 12");
        assertEquals( 0x20, m.getElement(13), "Set Group Name to "+testStringJ+": byte 13");
        assertEquals( 0x00, m.getElement(14), "Set Group Name to "+testStringJ+": byte 14");
        assertEquals( 0x00, m.getElement(15), "Set Group Name to "+testStringJ+": byte 15");
        assertEquals( 0x00, m.getElement(16), "Set Group Name to "+testStringJ+": byte 16");
        assertEquals( 0x00, m.getElement(17), "Set Group Name to "+testStringJ+": byte 17");
        assertEquals( 0x00, m.getElement(18), "Set Group Name to "+testStringJ+": byte 18");
        assertEquals( 0x00, m.getElement(19), "Set Group Name to "+testStringJ+": byte 19");
    }

    @Test
    public void testCreateSetUr92GroupChannelPacket() {
        LocoNetMessage m = new LocoNetMessage(20);
        for (int ch = 0; ch < 256; ++ch) {
            try {
            m = LnDplxGrpInfoImpl.createSetUr92GroupChannelPacket(ch);
            } catch ( LocoNetException e) {
                if ((ch >= 11) && (ch <= 26)) {
                    fail("unexpected exception when creating packet to set channel "+ch, e);
                }
            }
            if ((ch >= 11) && (ch <= 26)) {
                assertEquals( 0xe5, m.getElement(0), "Set Group Channel to "+ch+" Message  opcode");
                assertEquals( 0x14, m.getElement(1), "Set Group Channel to "+ch+" Message  byte 1");
                assertEquals( 0x02, m.getElement(2), "Set Group Channel to "+ch+" Message  byte 2");
                assertEquals( 0x00, m.getElement(3), "Set Group Channel to "+ch+" Message  byte 3");
                assertEquals( 0x00, m.getElement(4), "Set Group Channel to "+ch+" Message  byte 4");
                assertEquals(   ch, m.getElement(5), "Set Group Channel to "+ch+" Message  byte 5");
                assertEquals( 0x00, m.getElement(6), "Set Group Channel to "+ch+" Message  byte 6");
                assertEquals( 0x00, m.getElement(7), "Set Group Channel to "+ch+" Message  byte 7");
                assertEquals( 0x00, m.getElement(8), "Set Group Channel to "+ch+" Message  byte 8");
                assertEquals( 0x00, m.getElement(9), "Set Group Channel to "+ch+" Message  byte 9");
                assertEquals( 0x00, m.getElement(10), "Set Group Channel to "+ch+" Message  byte 10");
                assertEquals( 0x00, m.getElement(11), "Set Group Channel to "+ch+" Message  byte 11");
                assertEquals( 0x00, m.getElement(12), "Set Group Channel to "+ch+" Message  byte 12");
                assertEquals( 0x00, m.getElement(13), "Set Group Channel to "+ch+" Message  byte 13");
                assertEquals( 0x00, m.getElement(14), "Set Group Channel to "+ch+" Message  byte 14");
                assertEquals( 0x00, m.getElement(15), "Set Group Channel to "+ch+" Message  byte 15");
                assertEquals( 0x00, m.getElement(16), "Set Group Channel to "+ch+" Message  byte 16");
                assertEquals( 0x00, m.getElement(17), "Set Group Channel to "+ch+" Message  byte 17");
                assertEquals( 0x00, m.getElement(18), "Set Group Channel to "+ch+" Message  byte 18");
                assertEquals( 0x00, m.getElement(19), "Set Group Channel to "+ch+" Message  byte 19");
            }
        }
    }

    @Test
    public void testCreateSetUr92GroupPasswordPacket() {
        LocoNetMessage m;
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
                        String testString = new String(chars);
                        m = assertDoesNotThrow( () ->
                            LnDplxGrpInfoImpl.createSetUr92GroupPasswordPacket(testString),
                            "unexpected exception when creating packet to set password to "+d0+"/"+d1+"/"+d2+"/"+d3);

                        assertEquals( 0xe5, m.getElement(0), "Set Group Password to "+testString+" Message  opcode");
                        assertEquals( 0x14, m.getElement(1), "Set Group Password to "+testString+" Message  byte 1");
                        assertEquals( 0x07, m.getElement(2), "Set Group Password to "+testString+" Message  byte 2");
                        assertEquals( 0x00, m.getElement(3), "Set Group Password to "+testString+" Message  byte 3");
                        assertEquals( 0x00, m.getElement(4), "Set Group Password to "+testString+" Message  byte 4");
                        assertEquals(   c0, m.getElement(5), "Set Group Password to "+testString+" Message  byte 5");
                        assertEquals(   c1, m.getElement(6), "Set Group Password to "+testString+" Message  byte 6");
                        assertEquals(   c2, m.getElement(7), "Set Group Password to "+testString+" Message  byte 7");
                        assertEquals(   c3, m.getElement(8), "Set Group Password to "+testString+" Message  byte 8");
                        assertEquals( 0x00, m.getElement(9), "Set Group Password to "+testString+" Message  byte 9");
                        assertEquals( 0x00, m.getElement(10), "Set Group Password to "+testString+" Message  byte 10");
                        assertEquals( 0x00, m.getElement(11), "Set Group Password to "+testString+" Message  byte 11");
                        assertEquals( 0x00, m.getElement(12), "Set Group Password to "+testString+" Message  byte 12");
                        assertEquals( 0x00, m.getElement(13), "Set Group Password to "+testString+" Message  byte 13");
                        assertEquals( 0x00, m.getElement(14), "Set Group Password to "+testString+" Message  byte 14");
                        assertEquals( 0x00, m.getElement(15), "Set Group Password to "+testString+" Message  byte 15");
                        assertEquals( 0x00, m.getElement(16), "Set Group Password to "+testString+" Message  byte 16");
                        assertEquals( 0x00, m.getElement(17), "Set Group Password to "+testString+" Message  byte 17");
                        assertEquals( 0x00, m.getElement(18), "Set Group Password to "+testString+" Message  byte 18");
                        assertEquals( 0x00, m.getElement(19), "Set Group Password to "+testString+" Message  byte 19");

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
            } catch (jmri.jmrix.loconet.LocoNetException e) {
                assertTrue( (d0 < 0)|| (d0 > 127), "expect exception only when range is outside of rante [0 to 127]");
            }
            if ((d0 >= 0) && (d0 < 128)) {
                assertEquals( 0xe5, m.getElement(0), "Set Group ID to "+d0+" Message  opcode");
                assertEquals( 0x14, m.getElement(1), "Set Group ID to "+d0+" Message  byte 1");
                assertEquals( 0x04, m.getElement(2), "Set Group ID to "+d0+" Message  byte 2");
                assertEquals( 0x00, m.getElement(3), "Set Group ID to "+d0+" Message  byte 3");
                assertEquals( 0x00, m.getElement(4), "Set Group ID to "+d0+" Message  byte 4");
                assertEquals( d0 & 0x7f, m.getElement(5), "Set Group ID to "+d0+" Message  byte 5");
                assertEquals( 0x00, m.getElement(6), "Set Group ID to "+d0+" Message  byte 6");
                assertEquals( 0x00, m.getElement(7), "Set Group ID to "+d0+" Message  byte 7");
                assertEquals( 0x00, m.getElement(8), "Set Group ID to "+d0+" Message  byte 8");
                assertEquals( 0x00, m.getElement(9), "Set Group ID to "+d0+" Message  byte 9");
                assertEquals( 0x00, m.getElement(10), "Set Group ID to "+d0+" Message  byte 10");
                assertEquals( 0x00, m.getElement(11), "Set Group ID to "+d0+" Message  byte 11");
                assertEquals( 0x00, m.getElement(12), "Set Group ID to "+d0+" Message  byte 12");
                assertEquals( 0x00, m.getElement(13), "Set Group ID to "+d0+" Message  byte 13");
                assertEquals( 0x00, m.getElement(14), "Set Group ID to "+d0+" Message  byte 14");
                assertEquals( 0x00, m.getElement(15), "Set Group ID to "+d0+" Message  byte 15");
                assertEquals( 0x00, m.getElement(16), "Set Group ID to "+d0+" Message  byte 16");
                assertEquals( 0x00, m.getElement(17), "Set Group ID to "+d0+" Message  byte 17");
                assertEquals( 0x00, m.getElement(18), "Set Group ID to "+d0+" Message  byte 18");
                assertEquals( 0x00, m.getElement(19), "Set Group ID to "+d0+" Message  byte 19");
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
            assertEquals( (d0 == 0xe5), LnDplxGrpInfoImpl.isDuplexGroupMessage(m),
                "checking isDuplexGroupMessage for opcode "+d0);
        }
        m.setElement(0, 0xe5);
        for (int d1 = 0; d1 < 256; ++d1) {
            m.setElement(1, d1);
            assertEquals( (d1 == 0x14), LnDplxGrpInfoImpl.isDuplexGroupMessage(m),
                "checking isDuplexGroupMessage for byte 1 "+d1);
        }
        m.setElement(1, 0x14);
        for (int d2 = 0; d2 < 256; ++d2) {
            m.setElement(2, d2);
            assertEquals( (d2 == 2) || (d2 == 3) || (d2 == 4) || (d2 == 7),
                LnDplxGrpInfoImpl.isDuplexGroupMessage(m),
                "checking isDuplexGroupMessage for byte 2="+d2);
        }
        m.setElement(2, 0x2);
        for (int d3 = 0; d3 < 256; ++d3) {
            m.setElement(3, d3);
            assertEquals( (d3 == 0) || (d3 == 8) || (d3 == 0x10),
                LnDplxGrpInfoImpl.isDuplexGroupMessage(m),
                "checking isDuplexGroupMessage for byte 3 "+d3);
        }
        m.setElement(3, 0);
        for (int index = 4; index < 20; ++index) {
            m.setElement(index-1, 0);
            for (int i = 0; i < 16; ++i) {
                int val = JUnitUtil.getRandom().nextInt(256);
                m.setElement(index, val);

                assertTrue( LnDplxGrpInfoImpl.isDuplexGroupMessage(m),
                    "checking isDuplexGroupMessge for byte " + index +
                        " as value " + val);
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
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: 2 byte");

        m = new LocoNetMessage(19);
        m.setElement(0, 0xe5);
        m.setElement(1, 0x13);
        for (int i=2; i< 19;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: 19 byte");

        m = new LocoNetMessage(21);
        m.setElement(0, 0xe5);
        m.setElement(1, 0x15);
        for (int i=2; i< 21;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: 21 byte");

        m = new LocoNetMessage(20);
        m.setElement(0, 0xe5);
        m.setElement(1, 0x13);
        for (int i=2; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: 20 byte with 19-byte length value");

        m.setElement(1, 0x16);
        for (int i=2; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: 20 byte with 22-byte length value");

        m.setElement(1, 0x14);
        m.setElement(2, 0);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value");

        m.setElement(2, 1);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value");

        m.setElement(2, 5);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value");

        m.setElement(2, 6);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt, "not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value");

        m.setElement(2, 8);
        for (int i=3; i< 20;++i) { m.setElement(i, 0);}
        mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
        assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
            mt, "not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value");

        m.setElement(2, 9);
        for (int i=4; i< 20;++i) { m.setElement(i, 0);}
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            assertEquals(
                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                mt,
                "not a duplex message: byte 2="+m.getElement(2)+" not a Duplex value");
        }

        m.setElement(2, 2);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    assertEquals(
                            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_WRITE_MESSAGE,
                            mt,
                            "duplex channel message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 8:
                    assertEquals(
                            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_QUERY_MESSAGE,
                            mt,
                            "duplex channel message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 16:
                    assertEquals(
                            DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_REPORT_MESSAGE,
                            mt,
                            "duplex channel message: byte 4="+m.getElement(2)+" implies a Duplex message");
                    break;
                default:
                    assertEquals(
                        DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                        mt,
                        "not a duplex channel message: byte 4="+m.getElement(2)+" not a Duplex value");
                    break;
            }
        }


        m.setElement(2, 3);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    assertEquals(
                            DuplexGroupMessageType.DUPLEX_GROUP_NAME_WRITE_MESSAGE,
                            mt,
                            "duplex name message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 8:
                    assertEquals(
                            DuplexGroupMessageType.DUPLEX_GROUP_NAME_QUERY_MESSAGE,
                            mt,
                            "duplex name message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 16:
                    assertEquals(
                            DuplexGroupMessageType.DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE,
                            mt,
                            "duplex name message: byte 4="+m.getElement(2)+" implies a Duplex message");
                    break;
                default:
                    assertEquals(
                        DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE,
                        mt,
                        "not a duplex name message: byte 4="+m.getElement(2)+" not a Duplex value");
                    break;
            }
        }


        m.setElement(2, 4);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    assertEquals( DuplexGroupMessageType.DUPLEX_GROUP_ID_WRITE_MESSAGE, mt,
                        "duplex ID message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 8:
                    assertEquals( DuplexGroupMessageType.DUPLEX_GROUP_ID_QUERY_MESSAGE, mt,
                        "duplex ID message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 16:
                    assertEquals( DuplexGroupMessageType.DUPLEX_GROUP_ID_REPORT_MESSAGE, mt,
                        "duplex id message: byte 4="+m.getElement(2)+" implies a Duplex message");
                    break;
                default:
                    assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE, mt,
                        "not a duplex id message: byte 4="+m.getElement(2)+" not a Duplex value");
                    break;
            }
        }

        m.setElement(2, 7);
        for (int i=0; i < 128; ++i) {
            m.setElement(3, i);
            mt = LnDplxGrpInfoImpl.getDuplexGroupIdentityMessageType(m);
            switch (i) {
                case 0:
                    assertEquals( DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_WRITE_MESSAGE, mt,
                        "duplex password message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 8:
                    assertEquals( DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_QUERY_MESSAGE, mt,
                        "duplex password message: byte 4="+m.getElement(4)+" implies a Duplex message");
                    break;
                case 16:
                    assertEquals( DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_REPORT_MESSAGE, mt,
                        "duplex password message: byte 4="+m.getElement(2)+" implies a Duplex message");
                    break;
                default:
                    assertEquals( DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE, mt,
                        "not a duplex password message: byte 4="+m.getElement(2)+" not a Duplex value");
                    break;
            }
        }
    }

    
    @Test
    public void testHandleMessageDuplexInfoReport() {
        java.beans.PropertyChangeListener l = (java.beans.PropertyChangeEvent e) -> {
            switch (e.getPropertyName()) {
                case "DPLXPCK_STAT_LN_UPDATE":
                    // log.warn("prop change query seen");
                    propChangeQueryFlag = true;
                    break;
                case "DPLXPCK_NAME_UPDATE": // prop change update seen
                case "DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR": // prop change update if not currently error seen
                    propChangeReportFlag = true;
                    break;
                default:
                    break;
            }
            propChangeFlag = true;
            propChangeCount++;
        };

        dpxGrpInfoImpl.addPropertyChangeListener(l);
        propChangeCount = 0;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;

        assertFalse( propChangeFlag, "did not see property change flag yet");
        assertFalse( propChangeReportFlag, "did not see property change flag yet");
        assertFalse( propChangeQueryFlag, "did not see property change flag yet");
        assertEquals( 0, propChangeCount, "no prop change listener firings yet");

        assertFalse( dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage(),
            "not yet waiting for Duplex Group Name, etc. Report");
        dpxGrpInfoImpl.queryDuplexGroupIdentity();
        assertTrue( dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage(),
            "Now waiting for Duplex Group Name, etc. Report");



        assertTrue( propChangeReportFlag, "did see initial property change Report flag");
        assertEquals( 9, propChangeCount, "Did see a bunch of invalidation prop changes");

        propChangeCount = 0;
        propChangeReportFlag = false;

        LocoNetMessage rcvMsg = lnis.outbound.get(0);
        dpxGrpInfoImpl.message(rcvMsg); // echo the transmitted message back to the sender

        int ch = 4;
        int id = 131;
        String name = "\231\032\033\034\035\036\237\140";
        String pass="0200";
        LocoNetMessage m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);
        assertTrue( dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage(),
            "Now waiting (2) for Duplex Group Name, etc. Report");

        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertFalse( dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage(),
            "No longer waiting for Duplex Group Name, etc. Report");
        assertEquals( 12, propChangeCount, "Expected 11 prop change events");

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        dpxGrpInfoImpl.message(m);  // transmit the reply
        assertFalse( dpxGrpInfoImpl.isAwaitingDuplexGroupReportMessage(), "No longer (2) waiting for Duplex Group Name, etc. Report");
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertEquals( 2, propChangeCount, "Expected exactly 2 prop change events, one count, one detail");

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(6, m.getElement(6)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertEquals( 3, propChangeCount, "Expected exactly 3 prop change event");

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(6, m.getElement(6)^1);
        m.setElement(15, m.getElement(15)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertEquals( 3, propChangeCount, "Expected exactly 3 prop change event");

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(15, m.getElement(15)^1);
        m.setElement(17, m.getElement(17)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertEquals( 3, propChangeCount, "Expected exactly 3 prop change event");

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(17, m.getElement(17)^1);
        m.setElement(18, m.getElement(18)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertEquals( 3, propChangeCount, "Expected exactly 3 prop change event");

        JUnitUtil.waitFor(1300);

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(17, m.getElement(17)^1);
        m.setElement(18, m.getElement(18)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertEquals( 3, propChangeCount, "Expected exactly 3 prop change event");

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(17, m.getElement(17)^1);
        m.setElement(18, m.getElement(18)^1);
        m.setElement(6, m.getElement(6)^1);

        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == true;},"message received");
        assertEquals( 3, propChangeCount, "Expected exactly 3 prop change event");
        assertFalse( dpxGrpInfoImpl.isIplQueryTimerRunning(), "Query Timer no longer running");

        propChangeCount = 0;
        propChangeFlag = false;
        propChangeQueryFlag = false;
        propChangeReportFlag = false;

        m.setElement(0, m.getElement(0) ^ 1);
        dpxGrpInfoImpl.message(m);  // transmit the reply
        JUnitUtil.fasterWaitFor(()->{return propChangeFlag == false;},"message received");

    }

    @Test
    public void checkCreateUr92GroupNameReportPacket() {
        int ch = 7, id = 4;
        String name = "AbcdeFgh", pass="ABC0";
        LocoNetMessage m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        assertEquals( 0xe5, m.getElement(0), "Group Id Query Message: opcode");
        assertEquals( 0x14, m.getElement(1), "Group Id Query Message: byte 1");
        assertEquals( 0x03, m.getElement(2), "Group Id Query Message: byte 2");
        assertEquals( 0x10, m.getElement(3), "Group Id Query Message: byte 3");
        assertEquals( 0x00, m.getElement(4), "Group Id Query Message: byte 4");
        assertEquals( 0x41, m.getElement(5), "Group Id Query Message: byte 5");
        assertEquals( 0x62, m.getElement(6), "Group Id Query Message: byte 6");
        assertEquals( 0x63, m.getElement(7), "Group Id Query Message: byte 7");
        assertEquals( 0x64, m.getElement(8), "Group Id Query Message: byte 8");
        assertEquals( 0x00, m.getElement(9), "Group Id Query Message: byte 9");
        assertEquals( 0x65, m.getElement(10), "Group Id Query Message: byte 10");
        assertEquals( 0x46, m.getElement(11), "Group Id Query Message: byte 11");
        assertEquals( 0x67, m.getElement(12), "Group Id Query Message: byte 12");
        assertEquals( 0x68, m.getElement(13), "Group Id Query Message: byte 13");
        assertEquals( 0x03, m.getElement(14), "Group Id Query Message: byte 14");
        assertEquals( 0x2b, m.getElement(15), "Group Id Query Message: byte 15");
        assertEquals( 0x40, m.getElement(16), "Group Id Query Message: byte 16");
        assertEquals(  0x7, m.getElement(17), "Group Id Query Message: byte 17");
        assertEquals(  0x4, m.getElement(18), "Group Id Query Message: byte 18");
        assertEquals( 0x00, m.getElement(19), "Group Id Query Message: byte 19");

        ch = 0; id = 7;
        name = "\0\0\0\0\0\0\0\0"; pass="733B";
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        assertEquals( 0xe5, m.getElement(0), "Group Id Query Message: opcode");
        assertEquals( 0x14, m.getElement(1), "Group Id Query Message: byte 1");
        assertEquals( 0x03, m.getElement(2), "Group Id Query Message: byte 2");
        assertEquals( 0x10, m.getElement(3), "Group Id Query Message: byte 3");
        assertEquals( 0x00, m.getElement(4), "Group Id Query Message: byte 4");
        assertEquals(  0x0, m.getElement(5), "Group Id Query Message: byte 5");
        assertEquals(  0x0, m.getElement(6), "Group Id Query Message: byte 6");
        assertEquals(  0x0, m.getElement(7), "Group Id Query Message: byte 7");
        assertEquals(  0x0, m.getElement(8), "Group Id Query Message: byte 8");
        assertEquals( 0x00, m.getElement(9), "Group Id Query Message: byte 9");
        assertEquals(  0x0, m.getElement(10), "Group Id Query Message: byte 10");
        assertEquals(  0x0, m.getElement(11), "Group Id Query Message: byte 11");
        assertEquals(  0x0, m.getElement(12), "Group Id Query Message: byte 12");
        assertEquals(  0x0, m.getElement(13), "Group Id Query Message: byte 13");
        assertEquals( 0x00, m.getElement(14), "Group Id Query Message: byte 14");
        assertEquals( 0x73, m.getElement(15), "Group Id Query Message: byte 15");
        assertEquals( 0x3b, m.getElement(16), "Group Id Query Message: byte 16");
        assertEquals(  0x0, m.getElement(17), "Group Id Query Message: byte 17");
        assertEquals(  0x7, m.getElement(18), "Group Id Query Message: byte 18");
        assertEquals( 0x00, m.getElement(19), "Group Id Query Message: byte 19");

        ch = 129; id = 2;
        name = "\200\201\202\203\204\205\206\207"; pass="0001";
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        assertEquals( 0xe5, m.getElement(0), "Group Id Query Message: opcode");
        assertEquals( 0x14, m.getElement(1), "Group Id Query Message: byte 1");
        assertEquals( 0x03, m.getElement(2), "Group Id Query Message: byte 2");
        assertEquals( 0x10, m.getElement(3), "Group Id Query Message: byte 3");
        assertEquals( 0x0f, m.getElement(4), "Group Id Query Message: byte 4");
        assertEquals( 0x00, m.getElement(5), "Group Id Query Message: byte 5");
        assertEquals( 0x01, m.getElement(6), "Group Id Query Message: byte 6");
        assertEquals( 0x02, m.getElement(7), "Group Id Query Message: byte 7");
        assertEquals( 0x03, m.getElement(8), "Group Id Query Message: byte 8");
        assertEquals( 0x0f, m.getElement(9), "Group Id Query Message: byte 9");
        assertEquals( 0x04, m.getElement(10), "Group Id Query Message: byte 10");
        assertEquals( 0x05, m.getElement(11), "Group Id Query Message: byte 11");
        assertEquals( 0x06, m.getElement(12), "Group Id Query Message: byte 12");
        assertEquals( 0x07, m.getElement(13), "Group Id Query Message: byte 13");
        assertEquals( 0x04, m.getElement(14), "Group Id Query Message: byte 14");
        assertEquals( 0x00, m.getElement(15), "Group Id Query Message: byte 15");
        assertEquals( 0x01, m.getElement(16), "Group Id Query Message: byte 16");
        assertEquals(  0x1, m.getElement(17), "Group Id Query Message: byte 17");
        assertEquals(  0x2, m.getElement(18), "Group Id Query Message: byte 18");
        assertEquals( 0x00, m.getElement(19), "Group Id Query Message: byte 19");

        ch = 4; id = 131;
        name = "\231\032\033\034\035\036\237\140"; pass="0200";
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket(name, pass, ch, id);

        assertEquals( 0xe5, m.getElement(0), "Group Id Query Message: opcode");
        assertEquals( 0x14, m.getElement(1), "Group Id Query Message: byte 1");
        assertEquals( 0x03, m.getElement(2), "Group Id Query Message: byte 2");
        assertEquals( 0x10, m.getElement(3), "Group Id Query Message: byte 3");
        assertEquals( 0x01, m.getElement(4), "Group Id Query Message: byte 4");
        assertEquals( 0x19, m.getElement(5), "Group Id Query Message: byte 5");
        assertEquals( 0x1a, m.getElement(6), "Group Id Query Message: byte 6");
        assertEquals( 0x1b, m.getElement(7), "Group Id Query Message: byte 7");
        assertEquals( 0x1c, m.getElement(8), "Group Id Query Message: byte 8");
        assertEquals( 0x04, m.getElement(9), "Group Id Query Message: byte 9");
        assertEquals( 0x1d, m.getElement(10), "Group Id Query Message: byte 10");
        assertEquals( 0x1e, m.getElement(11), "Group Id Query Message: byte 11");
        assertEquals( 0x1f, m.getElement(12), "Group Id Query Message: byte 12");
        assertEquals( 0x60, m.getElement(13), "Group Id Query Message: byte 13");
        assertEquals( 0x08, m.getElement(14), "Group Id Query Message: byte 14");
        assertEquals( 0x02, m.getElement(15), "Group Id Query Message: byte 15");
        assertEquals( 0x00, m.getElement(16), "Group Id Query Message: byte 16");
        assertEquals(  0x4, m.getElement(17), "Group Id Query Message: byte 17");
        assertEquals(  0x3, m.getElement(18), "Group Id Query Message: byte 18");
        assertEquals( 0x00, m.getElement(19), "Group Id Query Message: byte 19");

    }


    @Test
    public void testCreatePasswordMessage() {
        LocoNetMessage m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("1234");
        assertEquals( 0xe5, m.getOpCode(), "Opcode");
        assertEquals( 0x14, m.getElement(1), "Byte 01");
        assertEquals( 0x07, m.getElement(2), "Byte 02");
        assertEquals( 0x10, m.getElement(3), "Byte 03");
        assertEquals( 0x00, m.getElement(4), "Byte 04");
        assertEquals( 0x31, m.getElement(5), "Byte 05");
        assertEquals( 0x32, m.getElement(6), "Byte 06");
        assertEquals( 0x33, m.getElement(7), "Byte 07");
        assertEquals( 0x34, m.getElement(8), "Byte 08");
        assertEquals( 0x00, m.getElement(9), "Byte 09");
        assertEquals( 0x00, m.getElement(10), "Byte 10");
        assertEquals( 0x00, m.getElement(11), "Byte 11");
        assertEquals( 0x00, m.getElement(12), "Byte 12");
        assertEquals( 0x00, m.getElement(13), "Byte 13");
        assertEquals( 0x00, m.getElement(14), "Byte 14");
        assertEquals( 0x00, m.getElement(15), "Byte 15");
        assertEquals( 0x00, m.getElement(16), "Byte 16");
        assertEquals( 0x00, m.getElement(17), "Byte 17");
        assertEquals( 0x00, m.getElement(18), "Byte 18");

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("123A");
        assertEquals( 0xe5, m.getOpCode(), "Opcode");
        assertEquals( 0x14, m.getElement(1), "Byte 01");
        assertEquals( 0x07, m.getElement(2), "Byte 02");
        assertEquals( 0x10, m.getElement(3), "Byte 03");
        assertEquals( 0x00, m.getElement(4), "Byte 04");
        assertEquals( 0x31, m.getElement(5), "Byte 05");
        assertEquals( 0x32, m.getElement(6), "Byte 06");
        assertEquals( 0x33, m.getElement(7), "Byte 07");
        assertEquals( 0x41, m.getElement(8), "Byte 08");
        assertEquals( 0x00, m.getElement(9), "Byte 09");
        assertEquals( 0x00, m.getElement(10), "Byte 10");
        assertEquals( 0x00, m.getElement(11), "Byte 11");
        assertEquals( 0x00, m.getElement(12), "Byte 12");
        assertEquals( 0x00, m.getElement(13), "Byte 13");
        assertEquals( 0x00, m.getElement(14), "Byte 14");
        assertEquals( 0x00, m.getElement(15), "Byte 15");
        assertEquals( 0x00, m.getElement(16), "Byte 16");
        assertEquals( 0x00, m.getElement(17), "Byte 17");
        assertEquals( 0x00, m.getElement(18), "Byte 18");

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("12C0");
        assertEquals( 0xe5, m.getOpCode(), "Opcode");
        assertEquals( 0x14, m.getElement(1), "Byte 01");
        assertEquals( 0x07, m.getElement(2), "Byte 02");
        assertEquals( 0x10, m.getElement(3), "Byte 03");
        assertEquals( 0x00, m.getElement(4), "Byte 04");
        assertEquals( 0x31, m.getElement(5), "Byte 05");
        assertEquals( 0x32, m.getElement(6), "Byte 06");
        assertEquals( 0x43, m.getElement(7), "Byte 07");
        assertEquals( 0x30, m.getElement(8), "Byte 08");
        assertEquals( 0x00, m.getElement(9), "Byte 09");
        assertEquals( 0x00, m.getElement(10), "Byte 10");
        assertEquals( 0x00, m.getElement(11), "Byte 11");
        assertEquals( 0x00, m.getElement(12), "Byte 12");
        assertEquals( 0x00, m.getElement(13), "Byte 13");
        assertEquals( 0x00, m.getElement(14), "Byte 14");
        assertEquals( 0x00, m.getElement(15), "Byte 15");
        assertEquals( 0x00, m.getElement(16), "Byte 16");
        assertEquals( 0x00, m.getElement(17), "Byte 17");
        assertEquals( 0x00, m.getElement(18), "Byte 18");

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("9B00");
        assertEquals( 0xe5, m.getOpCode(), "Opcode");
        assertEquals( 0x14, m.getElement(1), "Byte 01");
        assertEquals( 0x07, m.getElement(2), "Byte 02");
        assertEquals( 0x10, m.getElement(3), "Byte 03");
        assertEquals( 0x00, m.getElement(4), "Byte 04");
        assertEquals( 0x39, m.getElement(5), "Byte 05");
        assertEquals( 0x42, m.getElement(6), "Byte 06");
        assertEquals( 0x30, m.getElement(7), "Byte 07");
        assertEquals( 0x30, m.getElement(8), "Byte 08");
        assertEquals( 0x00, m.getElement(9), "Byte 09");
        assertEquals( 0x00, m.getElement(10), "Byte 10");
        assertEquals( 0x00, m.getElement(11), "Byte 11");
        assertEquals( 0x00, m.getElement(12), "Byte 12");
        assertEquals( 0x00, m.getElement(13), "Byte 13");
        assertEquals( 0x00, m.getElement(14), "Byte 14");
        assertEquals( 0x00, m.getElement(15), "Byte 15");
        assertEquals( 0x00, m.getElement(16), "Byte 16");
        assertEquals( 0x00, m.getElement(17), "Byte 17");
        assertEquals( 0x00, m.getElement(18), "Byte 18");

        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket("A999");
        assertEquals( 0xe5, m.getOpCode(), "Opcode");
        assertEquals( 0x14, m.getElement(1), "Byte 01");
        assertEquals( 0x07, m.getElement(2), "Byte 02");
        assertEquals( 0x10, m.getElement(3), "Byte 03");
        assertEquals( 0x00, m.getElement(4), "Byte 04");
        assertEquals( 0x41, m.getElement(5), "Byte 05");
        assertEquals( 0x39, m.getElement(6), "Byte 06");
        assertEquals( 0x39, m.getElement(7), "Byte 07");
        assertEquals( 0x39, m.getElement(8), "Byte 08");
        assertEquals( 0x00, m.getElement(9), "Byte 09");
        assertEquals( 0x00, m.getElement(10), "Byte 10");
        assertEquals( 0x00, m.getElement(11), "Byte 11");
        assertEquals( 0x00, m.getElement(12), "Byte 12");
        assertEquals( 0x00, m.getElement(13), "Byte 13");
        assertEquals( 0x00, m.getElement(14), "Byte 14");
        assertEquals( 0x00, m.getElement(15), "Byte 15");
        assertEquals( 0x00, m.getElement(16), "Byte 16");
        assertEquals( 0x00, m.getElement(17), "Byte 17");
        assertEquals( 0x00, m.getElement(18), "Byte 18");

        String password = "\250\200\150\100";
        m = LnDplxGrpInfoImpl.createUr92GroupPasswordReportPacket(password);
        assertEquals( 0xe5, m.getOpCode(), "Opcode");
        assertEquals( 0x14, m.getElement(1), "Byte 01");
        assertEquals( 0x07, m.getElement(2), "Byte 02");
        assertEquals( 0x10, m.getElement(3), "Byte 03");
        assertEquals( 0x0c, m.getElement(4), "Byte 04");
        assertEquals( 0x28, m.getElement(5), "Byte 05");
        assertEquals( 0x00, m.getElement(6), "Byte 06");
        assertEquals( 0x68, m.getElement(7), "Byte 07");
        assertEquals( 0x40, m.getElement(8), "Byte 08");
        assertEquals( 0x00, m.getElement(9), "Byte 09");
        assertEquals( 0x00, m.getElement(10), "Byte 10");
        assertEquals( 0x00, m.getElement(11), "Byte 11");
        assertEquals( 0x00, m.getElement(12), "Byte 12");
        assertEquals( 0x00, m.getElement(13), "Byte 13");
        assertEquals( 0x00, m.getElement(14), "Byte 14");
        assertEquals( 0x00, m.getElement(15), "Byte 15");
        assertEquals( 0x00, m.getElement(16), "Byte 16");
        assertEquals( 0x00, m.getElement(17), "Byte 17");
        assertEquals( 0x00, m.getElement(18), "Byte 18");

    }

    @ToDo("Fix test, JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 2;}); ")
    // @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "DB_DUPLICATE_SWITCH_CLAUSES",
    //    justification = "keep seperate property changes")
    @Test
    public void testCountAndQuery() {
    
        JUnitUtil.fasterWaitFor(()->{return (!dpxGrpInfoImpl.isDuplexGroupQueryRunning());},"dpxGrpInfoImpl not running");

        java.beans.PropertyChangeListener l = (java.beans.PropertyChangeEvent e) -> {
            // log.warn("prop change query seen[{}]", e.getPropertyName());
            switch (e.getPropertyName()) {
                case "DPLXPCK_STAT_LN_UPDATE":
                    propChangeQueryFlag = true;
                    break;
                case "DPLXPCK_NAME_UPDATE":
                case "DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR":
                    propChangeReportFlag = true;
                    break;
                default:
                    break;
            }
            propChangeFlag = true;
            propChangeCount++;
        };

        dpxGrpInfoImpl.addPropertyChangeListener(l);
        propChangeCount = 0;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;
        assertEquals( 0, propChangeCount, "propChangeCount is reset to 0");

        lnis.outbound.removeAllElements();  // clear any possible previous loconet traffic

        assertEquals( 0, propChangeCount, "propChangeCount is reset to 0");
        assertFalse( dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport(),
            "LDGII is not yet waiting for second UR92 Group report (1)");
        lnis.outbound.removeAllElements();
        assertEquals( 0, propChangeCount, "propChangeCount is reset to 0");
        assertEquals( 0, dpxGrpInfoImpl.getNumUr92s(), "Num UR92s is zero");
        assertEquals( 0, lnis.outbound.size(), "LNIS outbound queue is empty");
        assertEquals( 0, propChangeCount, "propChangeCount is reset to 0");
        dpxGrpInfoImpl.countUr92sAndQueryDuplexIdentityInfo();
        assertEquals( 21, propChangeCount, "propChangeCount is now 21");
        JUnitUtil.waitFor(()->{return !lnis.outbound.isEmpty();}, "UR92 IPL query not received");

        assertEquals( 21, propChangeCount, "propChangeCount is now 21");
        assertEquals( 1, lnis.outbound.size(), "LNIS outbound queue has one message");

        assertTrue( dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport(), "LDGII is not yet waiting for second UR92 Group report (2)");
        lnis.sendTestMessage(lnis.outbound.elementAt(lnis.outbound.size()-1));

        LocoNetMessage m2 = LnIPLImplementation.createIplUr92QueryPacket();
        LocoNetMessage m = lnis.outbound.firstElement();
        assertEquals( m2.getNumDataElements(), m.getNumDataElements(),
            "LocoNet message has same number of bytes as UR92 IPL Query message");

        for (int i = 0; i < m2.getNumDataElements(); ++i) {
            assertEquals( m2.getElement(i), m.getElement(i),
                "Received LocoNet message byte "+i+" equals corresponding UR92 IPL Query Message byte");
        }
        lnis.sendTestMessage(m2);

        assertEquals( 21, propChangeCount, "expect propChangeCount of 21");
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
        assertEquals( 0, propChangeCount, "expect propChangeCount of 0");
        lnis.sendLocoNetMessage(m);
        assertEquals( 0, dpxGrpInfoImpl.getNumUr92s(), "Num UR92s is zero");
        assertEquals( 2, lnis.outbound.size(), "2 messages transmitted thus far");

        assertEquals( 0, propChangeCount, "expect propChangeCount of 0");

        dpxGrpInfoImpl.message(lnis.outbound.elementAt(1));  // return the LocoNet echo to the class
        assertEquals( 1, propChangeCount, "expect propChangeCount of 1");
        JUnitUtil.waitFor(()->{return dpxGrpInfoImpl.getNumUr92s() > 0;}, "UR92 IPL reply not received");
        assertEquals( 1, dpxGrpInfoImpl.getNumUr92s(), "got 1 UR92 IPL report");

        assertFalse( dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport(),
            "LDGII is no longer waiting for second UR92 IPL report (3)");

        JUnitUtil.waitFor(()->{return lnis.outbound.size() == 3;}, "UR92 group query not received?");

        assertFalse( dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport(), "LDGII is no longer waiting for UR92 IPL replies (4)");
        JUnitUtil.waitFor(()->{return lnis.outbound.size() == 3;},"wait for lnis outbound 3");
        assertEquals( 0xe5, lnis.outbound.elementAt(2).getOpCode(), "message is Duplex Group Info Query - opcode");
        assertEquals( 0x14, lnis.outbound.elementAt(2).getElement(1), "message is Duplex Group Info Query - b1");
        assertEquals( 3, lnis.outbound.elementAt(2).getElement(2), "message is Duplex Group Info Query - b2");
        assertEquals( 8, lnis.outbound.elementAt(2).getElement(3), "message is Duplex Group Info Query - b3");

        dpxGrpInfoImpl.message(lnis.outbound.elementAt(2));  // echo the Duplex Group Info Query message
        assertFalse( dpxGrpInfoImpl.isWaitingForFirstUr92IPLReport(), "LDGII is no longer waiting for UR92 IPL replies (5)");

        assertEquals( 13, propChangeCount, "expect propChangeCount of 13");
        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 12, 65);
        lnis.sendTestMessage(m);

        assertEquals( 24, propChangeCount, "expect propChangeCount of 24");

        assertEquals( 3, lnis.outbound.size(), "num outbound");

        assertEquals( 1,  dpxGrpInfoImpl.getNumUr92s(), "got the UR92 reply info");

        lnis.sendTestMessage(m);
        // JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 2;},
        //    "2022 June - does not get to 2, was " + dpxGrpInfoImpl.getNumUr92s());

        JUnitUtil.waitFor( () -> propChangeCount > 26, "wait for 27");
        assertEquals( 27, propChangeCount, "expect propChangeCount of 27");

        lnis.sendTestMessage(m);
        // JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 3;}, "Does not get to 3");

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Dcgitrax", "1234", 12, 65);
        lnis.sendTestMessage(m);
        // JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 4;}, "Does not get to 4");
        assertEquals( 32, propChangeCount, "expect propChangeCount of 32");

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1034", 12, 65);

        lnis.sendTestMessage(m);
        // JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 5;}, "Does not get to 5");
        assertEquals( 35, propChangeCount, "expect propChangeCount of 35");

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 13, 65);

        lnis.sendTestMessage(m);
        // JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 6;}, "Does not get  to 6");
        assertEquals( 38, propChangeCount, "expect propChangeCount of 38");

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 12, 7);

        lnis.sendTestMessage(m);
        // JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 7;}, "Does not get to 7");
        assertEquals( 41, propChangeCount, "expect propChangeCount of 41");

        m = LnDplxGrpInfoImpl.createUr92GroupNameReportPacket("Digitrax", "1234", 12, 65);

        lnis.sendTestMessage(m);
        // JUnitUtil.fasterWaitFor(()->{return dpxGrpInfoImpl.getNumUr92s() == 8;}, "Does not get to 8");

        assertEquals( 43, propChangeCount, "expect propChangeCount of 43");

        JUnitUtil.fasterWaitFor(()->{return (!dpxGrpInfoImpl.isDuplexGroupQueryRunning());},"dpxGrpInfoImpl not running");

        propChangeCount = 0;
        propChangeReportFlag = false;
        propChangeQueryFlag = false;

        assertEquals( 0, propChangeCount, "propChangeCount is reset to 0");
        dpxGrpInfoImpl.countUr92sAndQueryDuplexIdentityInfo();
        assertEquals( 21, propChangeCount, "propChangeCount is now 21");
        dpxGrpInfoImpl.countUr92sAndQueryDuplexIdentityInfo();
        assertEquals( 22, propChangeCount, "propChangeCount is now 22");

    }

    @Test
    public void testCreateDupGrpChReportMessage() {
        LocoNetMessage m;
        for (int ii = 0; ii < 256; ++ii) {
            int i = ii;
            m = LnDplxGrpInfoImpl.createUr92GroupChannelReportPacket(i);
            assertEquals( 0xe5, m.getOpCode(), () -> "iteration "+i+"Group Channel Report Packet Opcode is 0xe5");
            assertEquals( 0x14, m.getElement(1), () -> "iteration "+i+"Group Channel Report Packet Byte 1 is 0x14");
            assertEquals( 0x2, m.getElement(2), () -> "iteration "+i+"Group Channel Report Packet Byte 2 is 0x2");
            assertEquals( 0x10, m.getElement(3), () -> "iteration "+i+"Group Channel Report Packet Byte 3 is 0x10");
            assertEquals( (i > 127) ? 1 : 0, m.getElement(4),
                () -> "iteration "+i+"Group Channel Report Packet Byte 4 is "+ ((i > 127) ? 1 : 0));
            assertEquals( i & 0x7F, m.getElement(5),
                () -> "iteration "+i+"Group Channel Report Packet Byte 5 is " + (i & 0x7f));
            assertEquals( 0x0, m.getElement(6), () -> "iteration "+i+"Group Channel Report Packet Byte 6 is 0x0");
            assertEquals( 0x0, m.getElement(7), () -> "iteration "+i+"Group Channel Report Packet Byte 7 is 0x0");
            assertEquals( 0x0, m.getElement(8), () -> "iteration "+i+"Group Channel Report Packet Byte 8 is 0x0");
            assertEquals( 0x0, m.getElement(9), () -> "iteration "+i+"Group Channel Report Packet Byte 9 is 0x0");
            assertEquals( 0x0, m.getElement(10), () -> "iteration "+i+"Group Channel Report Packet Byte 10 is 0x0");
            assertEquals( 0x0, m.getElement(11), () -> "iteration "+i+"Group Channel Report Packet Byte 11 is 0x0");
            assertEquals( 0x0, m.getElement(12), () -> "iteration "+i+"Group Channel Report Packet Byte 12 is 0x0");
            assertEquals( 0x0, m.getElement(13), () -> "iteration "+i+"Group Channel Report Packet Byte 13 is 0x0");
            assertEquals( 0x0, m.getElement(14), () -> "iteration "+i+"Group Channel Report Packet Byte 14 is 0x0");
            assertEquals( 0x0, m.getElement(15), () -> "iteration "+i+"Group Channel Report Packet Byte 15 is 0x0");
            assertEquals( 0x0, m.getElement(16), () -> "iteration "+i+"Group Channel Report Packet Byte 16 is 0x0");
            assertEquals( 0x0, m.getElement(17), () -> "iteration "+i+"Group Channel Report Packet Byte 17 is 0x0");
            assertEquals( 0x0, m.getElement(18), () -> "iteration "+i+"Group Channel Report Packet Byte 18 is 0x0");
        }
    }

    @Test
    public void testCreateDupGrpIDReportMessage() {
        LocoNetMessage m;
        for (int ii = 0; ii < 256; ++ii) {
            int i = ii;
            m = LnDplxGrpInfoImpl.createUr92GroupIdReportPacket(i);
            assertEquals( 0xe5, m.getOpCode(), () -> "iteration "+i+"Group ID Report Packet Opcode is 0xe5");
            assertEquals( 0x14, m.getElement(1), () -> "iteration "+i+"Group ID Report Packet Byte 1 is 0x14");
            assertEquals( 0x4, m.getElement(2), () -> "iteration "+i+"Group ID Report Packet Byte 2 is 0x4");
            assertEquals( 0x10, m.getElement(3), () -> "iteration "+i+"Group ID Report Packet Byte 3 is 0x10");
            assertEquals( (i > 127) ? 1 : 0, m.getElement(4),
                () -> "iteration "+i+"Group ID Report Packet Byte 4 is "+ ((i > 127) ? 1 : 0));
            assertEquals( i & 0x7F, m.getElement(5),
                () -> "iteration "+i+"Group ID Report Packet Byte 5 is " + (i & 0x7f));
            assertEquals( 0x0, m.getElement(6), () -> "iteration "+i+"Group ID Report Packet Byte 6 is 0x0");
            assertEquals( 0x0, m.getElement(7), () -> "iteration "+i+"Group ID Report Packet Byte 7 is 0x0");
            assertEquals( 0x0, m.getElement(8), () -> "iteration "+i+"Group ID Report Packet Byte 8 is 0x0");
            assertEquals( 0x0, m.getElement(9), () -> "iteration "+i+"Group ID Report Packet Byte 9 is 0x0");
            assertEquals( 0x0, m.getElement(10), () -> "iteration "+i+"Group ID Report Packet Byte 10 is 0x0");
            assertEquals( 0x0, m.getElement(11), () -> "iteration "+i+"Group ID Report Packet Byte 11 is 0x0");
            assertEquals( 0x0, m.getElement(12), () -> "iteration "+i+"Group ID Report Packet Byte 12 is 0x0");
            assertEquals( 0x0, m.getElement(13), () -> "iteration "+i+"Group ID Report Packet Byte 13 is 0x0");
            assertEquals( 0x0, m.getElement(14), () -> "iteration "+i+"Group ID Report Packet Byte 14 is 0x0");
            assertEquals( 0x0, m.getElement(15), () -> "iteration "+i+"Group ID Report Packet Byte 15 is 0x0");
            assertEquals( 0x0, m.getElement(16), () -> "iteration "+i+"Group ID Report Packet Byte 16 is 0x0");
            assertEquals( 0x0, m.getElement(17), () -> "iteration "+i+"Group ID Report Packet Byte 17 is 0x0");
            assertEquals( 0x0, m.getElement(18), () -> "iteration "+i+"Group ID Report Packet Byte 18 is 0x0");
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
            assertEquals(testString, result,
                "Group Name etc Report "+m+"first char iteration "+i);

            m.setElement(2, 3);
            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertNull( result, "Group Name etc Write first char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals(testString, result,
                "Group Pw Write "+m+"first char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            m.setElement(4, 0);
            m.setElement(5, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Pw Report "+m+"first char iteration "+i);
        }

        for (int i=0; i<13; ++i) {
            testString = "0"+conversion2[i]+"00";
            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, 0);
            m.setElement(15, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Name etc Report "+m+"second char iteration "+i);

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertNull( result, "Group Name Write "+m+"second char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, 0);
            m.setElement(6, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Pw Write "+m+"second char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result,
                "Group Pw Report "+m+"second char iteration "+i);
        }

        for (int i=0; i<13; ++i) {
            testString = "00"+conversion2[i]+"0";
            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, (i>7)?2:0);
            m.setElement(15, 0);
            m.setElement(16, (i & 7) << 4);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result,
                "Group Name etc Report "+m+"third char iteration "+i);

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertNull( result, "Group Name Write "+m+"third char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, 0);
            m.setElement(6, 0);
            m.setElement(7, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Pw Write "+m+"third char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Pw Report "+m+"third char iteration "+i);
        }

        for (int i=0; i<13; ++i) {
            testString = "000"+conversion2[i];
            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, 0);
            m.setElement(15, 0);
            m.setElement(16, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Name etc Report "+m+"fourth char iteration "+i);

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertNull( result, "Group Name Write "+m+"fourth char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0);
            m.setElement(4, 0);
            m.setElement(5, 0);
            m.setElement(6, 0);
            m.setElement(7, 0);
            m.setElement(8, i);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Pw Write "+m+"fourth char iteration "+i);

            m.setElement(2, 7);
            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
            assertEquals( testString, result, "Group Pw Report "+m+"fourth char iteration "+i);
        }

        m = new LocoNetMessage(2);
        m.setOpCode(0x81);
        m.setElement(1, 0);
        result = LnDplxGrpInfoImpl.extractDuplexGroupPassword(m);
        assertNull( result, "Group Pw Report "+m);
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

        for (int ii = 0; ii < 256; ++ii) {
            int i = ii;
            m.setElement(2, 2);
            m.setElement(4, (i>127) ? 1:0);
            m.setElement(5, i&0x7f);

            result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
            assertEquals( i, result, () -> "iteration "+i+" extracted channel");

            m.setElement(2, 3);
            m.setElement(14, (i>127) ? 4:0);
            m.setElement(17, (i&0x7f));
            result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
            assertEquals( i, result, "extracted channel should be invalid for name operation");
        }

        int expectedResult = -1;
        m.setElement(2, 0);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        assertEquals( expectedResult, result,
            "extracted channel should be invalid for invalid operation");

        m.setElement(2, 4);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        assertEquals( expectedResult, result,
            "etracted channel should be invalid for password operation");

        m.setElement(2, 7);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        assertEquals( expectedResult, result,
            "etracted channel should be invalid for ID operation");

        m = new LocoNetMessage(2);
        m.setOpCode(0x82);
        m.setElement(1, 0);
        assertEquals( expectedResult, LnDplxGrpInfoImpl.extractDuplexGroupChannel(m),
            "interpret bad channel message");
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

        for (int ii = 0; ii < 256; ++ii) {
            int i = ii;
            m.setElement(2, 0x4);
            m.setElement(3, 0);
            m.setElement(4, (i>127) ? 1:0);
            m.setElement(5, i&0x7f);

            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            assertEquals( i, result, () -> "iteration "+i+" ID write operation");

            m.setElement(3, 0x10);
            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            assertEquals( i, result, () -> "iteration "+i+" ID report operation");

            m.setElement(2, 3);
            m.setElement(3, 0x10);
            m.setElement(14, (i>127) ? 8:0);
            m.setElement(18, (i&0x7f));
            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            assertEquals( i, result, () -> "iteration "+i+" ID from group name etc read operation");

            m.setElement(3, 0);
            result = LnDplxGrpInfoImpl.extractDuplexGroupID(m);
            assertEquals( exceptionalResult, result,() ->  "iteration "+i+" ID from group name etc write operation");
        }

        m.setElement(2, 0);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        assertEquals( exceptionalResult, result, "extracted channel should be invalid for invalid operation");

        m.setElement(2, 4);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        assertEquals( exceptionalResult, result, "etracted channel should be invalid for password operation");

        m.setElement(2, 7);
        result = LnDplxGrpInfoImpl.extractDuplexGroupChannel(m);
        assertEquals( exceptionalResult, result, "etracted channel should be invalid for ID operation");

        m = new LocoNetMessage(2);
        m.setOpCode(0x82);
        m.setElement(1, 0);
        assertEquals( exceptionalResult, LnDplxGrpInfoImpl.extractDuplexGroupChannel(m),
            "interpret bad channel message");

    }

    @Test
    public void testExtractDuplexGroupName() {
        LocoNetMessage m = new LocoNetMessage(0x14);
        assertNull( LnDplxGrpInfoImpl.extractDuplexGroupName(m), "expect null for empty message");
    }

    @Test
    public void testSetDuplexGroupName() {

        LocoNetException e = assertThrows( LocoNetException.class,
            () -> dpxGrpInfoImpl.setDuplexGroupName(""),
            "got an exception as intended");
        assertNotNull(e);

        lnis.outbound.removeAllElements();
        assertEquals( 0, lnis.outbound.size(), "outbound queue is empty");
        assertDoesNotThrow( () -> dpxGrpInfoImpl.setDuplexGroupName("deadBeeF"),
            "got an unexpected exception");

        assertEquals( 1, lnis.outbound.size(), "outbound queue is not empty");
        assertEquals( 0xe5, lnis.outbound.get(0).getOpCode(), "outbound queue opcode is correct");
        assertEquals( 0x14, lnis.outbound.get(0).getElement(1), "outbound queue byte 1 is correct");
        assertEquals( 0x03, lnis.outbound.get(0).getElement(2), "outbound queue byte 2 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(3), "outbound queue byte 3 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(4), "outbound queue byte 4 is correct");
        assertEquals( 'd', lnis.outbound.get(0).getElement(5), "outbound queue byte 5 is correct");
        assertEquals( 'e', lnis.outbound.get(0).getElement(6), "outbound queue byte 6 is correct");
        assertEquals( 'a', lnis.outbound.get(0).getElement(7), "outbound queue byte 7 is correct");
        assertEquals( 'd', lnis.outbound.get(0).getElement(8), "outbound queue byte 8 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "outbound queue byte 9 is correct");
        assertEquals( 'B', lnis.outbound.get(0).getElement(10), "outbound queue byte 10 is correct");
        assertEquals( 'e', lnis.outbound.get(0).getElement(11), "outbound queue byte 11 is correct");
        assertEquals( 'e', lnis.outbound.get(0).getElement(12), "outbound queue byte 12 is correct");
        assertEquals( 'F', lnis.outbound.get(0).getElement(13), "outbound queue byte 13 is correct");

    }

    @Test
    public void testSetDuplexGroupChannel() {

        LocoNetException e = assertThrows( LocoNetException.class,
            () -> dpxGrpInfoImpl.setDuplexGroupChannel(-1),
            "got an exception as intended");
        assertNotNull(e);

        lnis.outbound.removeAllElements();
        assertEquals( 0, lnis.outbound.size(), "outbound queue is empty");
        assertDoesNotThrow( () -> dpxGrpInfoImpl.setDuplexGroupChannel(13),
            "got an unexpected exception");

        assertEquals( 1, lnis.outbound.size(), "outbound queue is not empty");
        assertEquals( 0xe5, lnis.outbound.get(0).getOpCode(), "outbound queue opcode is correct");
        assertEquals( 0x14, lnis.outbound.get(0).getElement(1), "outbound queue byte 1 is correct");
        assertEquals( 0x02, lnis.outbound.get(0).getElement(2), "outbound queue byte 2 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(3), "outbound queue byte 3 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(4), "outbound queue byte 4 is correct");
        assertEquals( 13, lnis.outbound.get(0).getElement(5), "outbound queue byte 5 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(6), "outbound queue byte 6 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(7), "outbound queue byte 7 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(8), "outbound queue byte 8 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "outbound queue byte 9 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(10), "outbound queue byte 10 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(11), "outbound queue byte 11 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(12), "outbound queue byte 12 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(13), "outbound queue byte 13 is correct");

    }

    @Test
    public void testSetDuplexGroupID() {

        LocoNetException e = assertThrows( LocoNetException.class,
            () -> dpxGrpInfoImpl.setDuplexGroupId("999"),
            "got an exception as intended");
        assertNotNull(e);

        lnis.outbound.removeAllElements();
        assertEquals( 0, lnis.outbound.size(), "outbound queue is empty");
        assertDoesNotThrow( () -> dpxGrpInfoImpl.setDuplexGroupId("7"),
            "got an unexpected exception");

        assertEquals( 1, lnis.outbound.size(), "outbound queue is not empty");
        assertEquals( 0xe5, lnis.outbound.get(0).getOpCode(), "outbound queue opcode is correct");
        assertEquals( 0x14, lnis.outbound.get(0).getElement(1), "outbound queue byte 1 is correct");
        assertEquals( 0x04, lnis.outbound.get(0).getElement(2), "outbound queue byte 2 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(3), "outbound queue byte 3 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(4), "outbound queue byte 4 is correct");
        assertEquals( 7, lnis.outbound.get(0).getElement(5), "outbound queue byte 5 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(6), "outbound queue byte 6 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(7), "outbound queue byte 7 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(8), "outbound queue byte 8 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "outbound queue byte 9 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(10), "outbound queue byte 10 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(11), "outbound queue byte 11 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(12), "outbound queue byte 12 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(13), "outbound queue byte 13 is correct");

    }

    @Test
    public void testSetDuplexGroupPassword() {

        LocoNetException e = assertThrows( LocoNetException.class,
            () -> dpxGrpInfoImpl.setDuplexGroupPassword("abcd"),
            "got an exception as intended");
        assertNotNull(e);

        lnis.outbound.removeAllElements();
        assertEquals( 0, lnis.outbound.size(), "outbound queue is empty");
        assertDoesNotThrow( () -> dpxGrpInfoImpl.setDuplexGroupPassword("0123"),
            "got an unexpected exception");

        assertEquals( 1, lnis.outbound.size(), "outbound queue is not empty");
        assertEquals( 0xe5, lnis.outbound.get(0).getOpCode(), "outbound queue opcode is correct");
        assertEquals( 0x14, lnis.outbound.get(0).getElement(1), "outbound queue byte 1 is correct");
        assertEquals( 0x07, lnis.outbound.get(0).getElement(2), "outbound queue byte 2 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(3), "outbound queue byte 3 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(4), "outbound queue byte 4 is correct");
        assertEquals( '0', lnis.outbound.get(0).getElement(5), "outbound queue byte 5 is correct");
        assertEquals( '1', lnis.outbound.get(0).getElement(6), "outbound queue byte 6 is correct");
        assertEquals( '2', lnis.outbound.get(0).getElement(7), "outbound queue byte 7 is correct");
        assertEquals( '3', lnis.outbound.get(0).getElement(8), "outbound queue byte 8 is correct");
        assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "outbound queue byte 9 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(10), "outbound queue byte 10 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(11), "outbound queue byte 11 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(12), "outbound queue byte 12 is correct");
        assertEquals( 0, lnis.outbound.get(0).getElement(13), "outbound queue byte 13 is correct");

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);

        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false,false,false);
        // memo.configureManagers(); // Skip this step, else autonomous loconet traffic is generated!
        jmri.InstanceManager.store(memo,jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);

        dpxGrpInfoImpl = new LnDplxGrpInfoImpl(memo);

    }

    @AfterEach
    public void tearDown() {
        dpxGrpInfoImpl.dispose();
        dpxGrpInfoImpl = null;
        lnis = null;
        memo.dispose();
        memo=null;
        
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnDplxGrpInfoImplTest.class);
}
