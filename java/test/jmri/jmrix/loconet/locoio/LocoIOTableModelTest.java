package jmri.jmrix.loconet.locoio;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet.locoio.LocoIOTableModel class.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 */
public class LocoIOTableModelTest extends TestCase {

    public void testObjectCreate() {
        /*         // prepare an interface */
        /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
        /*  */
        /*         LocoIOTableModel m = new LocoIOTableModel(0x1081, null); */
    }

    /*     // test mapping from cv mode values to strings */
    /*     public void testModeFromValues() { */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         assertEquals("0x0F toggle switch", LocoIOTableModel.TOGGLESWITCH, m.modeFromValues(0x0F, 0x1C10)); */
    /*         assertEquals("0x2F push low", LocoIOTableModel.PUSHBUTTONLO, m.modeFromValues(0x2F, 0x1C10)); */
    /*         assertEquals("0x6F push high", LocoIOTableModel.PUSHBUTTONHI, m.modeFromValues(0x6F, 0x1C10)); */
    /*         assertEquals("0x80 throw", LocoIOTableModel.TURNOUTTHROW, m.modeFromValues(0x80, 0x1C10)); */
    /*         assertEquals("0x80 close", LocoIOTableModel.TURNOUTCLOSE, m.modeFromValues(0x80, 0x1C30)); */
    /*         assertEquals("0xC0 push high", LocoIOTableModel.STATUSMESSAGE, m.modeFromValues(0xC0, 0xCC20)); */
    /*     } */
    /*  */
    /*     // test read from toggle */
    /*     public void testReadOperationToggle() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         int channel = 2; */
    /*         m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN); */
    /*  */
    /*         read3Sequence(channel, 0x0F, 0x1C, 0x10,lnis, true ); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.TOGGLESWITCH, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "1c10", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     // test read from pushbutton low */
    /*     public void testReadOperationPushLow() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         int channel = 5; */
    /*         m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN); */
    /*  */
    /*         read3Sequence(channel, 0x2F, 0x1C, 0x10,lnis, true); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.PUSHBUTTONLO, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "1c10", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     // test read from pushbutton high */
    /*     public void testReadOperationPushHigh() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         int channel = 5; */
    /*         m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN); */
    /*  */
    /*         read3Sequence(channel, 0x6F, 0x1C, 0x10,lnis, true ); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.PUSHBUTTONHI, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "1c10", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     // test read with high bits set */
    /*     public void testReadOperationHighBits() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         int channel = 5; */
    /*         m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN); */
    /*  */
    /*         read3Sequence(channel, 0xC0, 0xFF, 0xFF,lnis, true ); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.STATUSMESSAGE, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "ffff", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     // test capture of OPC_INPUT_REP */
    /*     public void testCaptureInputRep() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         int channel = 5; */
    /*         m.setValueAt(null, channel, LocoIOTableModel.CAPTURECOLUMN); */
    /*  */
    /*         LocoNetMessage msg = new LocoNetMessage(3); */
    /*         msg.setElement(0, LnConstants.OPC_INPUT_REP); */
    /*         msg.setElement(1, 0x23); */
    /*         msg.setElement(2, 0xF1); */
    /*         m.message(msg); */
    /*         Assert.assertEquals("addr", "23f1", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*  */
    /*         msg.setElement(0, LnConstants.OPC_INPUT_REP); */
    /*         msg.setElement(1, 0xF0); */
    /*         msg.setElement(2, 0x32); */
    /*         m.message(msg); */
    /*         Assert.assertEquals("addr", "23f1", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     /** */
    /*      * Service routine, runs through the sequence for a read operation, */
    /*      * returning each of the three bytes given as arguments. */
    /*      * @param channel The channel being read */
    /*      * @param cv value returned for the configuration CV read */
    /*      * @param addrlow value returned for the low address read */
    /*      * @param addrhigh value returned for the high address read */
    /*      * @param lnis Test interface for loconet i/o */
//      */ 
/*     void read3Sequence(int channel, int cv, int addrlow, int addrhigh, */
    /*                        LocoNetInterfaceScaffold lnis, boolean pre133 ) { */
    /*         int src; */
    /*         int dst; */
    /*         int readcv = 12;  // which byte contains the readCV info? */
    /*         if (pre133) readcv = 14; */
    /*         // check transmitted message */
    /*         Assert.assertEquals("One message sent", 1, lnis.outbound.size()); */
    /*         LocoNetMessage msg = (LocoNetMessage)lnis.outbound.elementAt(0); */
    /*         // read low addr */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 00 02 " */
    /*                             +StringUtil.twoHexFromInt(channel*3+4)+" 00 00 10 00 00 00 00 00", msg.toString()); */
    /*  */
    /*         // turn that message around as the echo; we only do this the first time */
    /*         lnis.sendTestMessage(msg); */
    /*         Assert.assertEquals("listener present", 1, lnis.numListeners()); */
    /*         Assert.assertEquals("echo ignored", 1, lnis.outbound.size()); */
    /*  */
    /*         // turn around as the reply to the read low */
    /*         src = msg.getElement(2); */
    /*         dst = msg.getElement(3); */
    /*         msg.setElement(2, dst); */
    /*         msg.setElement(3, src); */
    /*         msg.setElement(4, 0x01);  // seems to be fixed PC address high */
    /*         msg.setElement(8,(pre133?0:10)); */
    /*         int flagbits = 0x10; */
    /*         if (pre133) flagbits+=((addrlow&0x80)!=0?8:0); */
    /*         else flagbits+=((addrlow&0x80)!=0?2:0); */
    /*         msg.setElement(10,flagbits); */
    /*         msg.setElement(readcv,addrlow&0x7f); // low addr */
    /*         lnis.sendTestMessage(msg); */
    /*  */
    /*         // 2nd read */
    /*         Assert.assertEquals("reply does 2nd read", 2, lnis.outbound.size()); */
    /*         msg = (LocoNetMessage)lnis.outbound.elementAt(1); */
    /*         // CV11 for read high address */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 00 02 " */
    /*                             +StringUtil.twoHexFromInt(channel*3+5)+" 00 00 10 00 00 00 00 00", msg.toString()); */
    /*  */
    /*         // turn around as the reply to the read high */
    /*         src = msg.getElement(2); */
    /*         dst = msg.getElement(3); */
    /*         msg.setElement(2, dst); */
    /*         msg.setElement(3, src); */
    /*         msg.setElement(4, 0x01);  // seems to be fixed PC address high */
    /*         msg.setElement(8,(pre133?0:10)); */
    /*         flagbits = 0x10; */
    /*         if (pre133) flagbits+=((addrhigh&0x80)!=0?8:0); */
    /*         else flagbits+=((addrhigh&0x80)!=0?2:0); */
    /*         msg.setElement(10,flagbits); */
    /*         msg.setElement(readcv,addrhigh&0x7f); */
    /*         lnis.sendTestMessage(msg); */
    /*  */
    /*         // 3rd read */
    /*         Assert.assertEquals("reply does 3rd read", 3, lnis.outbound.size()); */
    /*         msg = (LocoNetMessage)lnis.outbound.elementAt(2); */
    /*         // channel 2 (above) is CV 9 for read mode */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 00 02 " */
    /*                             +StringUtil.twoHexFromInt(channel*3+3)+" 00 00 10 00 00 00 00 00", msg.toString()); */
    /*  */
    /*         // turnaround as the reply to the mode read */
    /*         src = msg.getElement(2); */
    /*         dst = msg.getElement(3); */
    /*         msg.setElement(2, dst); */
    /*         msg.setElement(3, src); */
    /*         msg.setElement(4, 0x01);  // seems to be fixed PC address high */
    /*         msg.setElement(8,(pre133?0:10)); */
    /*         flagbits = 0x10; */
    /*         if (pre133) flagbits+=((cv&0x80)!=0?8:0); */
    /*         else flagbits+=((cv&0x80)!=0?2:0); */
    /*         msg.setElement(10, flagbits); */
    /*         msg.setElement(readcv,cv&0x7f); */
    /*         lnis.sendTestMessage(msg); */
    /*  */
    /*         Assert.assertEquals("reply does no more messages", 3, lnis.outbound.size()); */
    /*     } */
    /*  */
    /*     // test setting of mode, and its effect on address */
    /*     public void testSetOnMode() { */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*         int channel = 12; */
    /*         m.setValueAt("101c", channel, LocoIOTableModel.ADDRCOLUMN); */
    /*         m.setValueAt(LocoIOTableModel.TURNOUTCLOSE, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*         Assert.assertEquals("mode", LocoIOTableModel.TURNOUTCLOSE, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "103c", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*         m.setValueAt(LocoIOTableModel.TURNOUTTHROW, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*         Assert.assertEquals("mode", LocoIOTableModel.TURNOUTTHROW, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "101c", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     // test setting of address, and its effect on mode */
    /*     public void testSetAddr() { */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*         int channel = 12; */
    /*         m.setValueAt(LocoIOTableModel.TURNOUTCLOSE, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*         m.setValueAt("1C10", channel, LocoIOTableModel.ADDRCOLUMN); */
    /*         Assert.assertEquals("mode", LocoIOTableModel.TURNOUTTHROW, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "1C10", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*  */
    /*         m.setValueAt("1c30", channel, LocoIOTableModel.ADDRCOLUMN); */
    /*         Assert.assertEquals("mode", LocoIOTableModel.TURNOUTCLOSE, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "1c30", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     // test write from pushbutton high */
    /*     public void testWriteOperationPushHigh() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         int channel = 5; */
    /*         m.setValueAt("1c10", channel, LocoIOTableModel.ADDRCOLUMN); */
    /*         m.setValueAt(LocoIOTableModel.PUSHBUTTONHI, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*         m.setValueAt(null, channel, LocoIOTableModel.WRITECOLUMN); */
    /*  */
    /*         write3Sequence(channel, 0x6F, 0x1C, 0x10,lnis ); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.PUSHBUTTONHI, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "1c10", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     // test write with high bits */
    /*     public void testWriteOperationHighBitsOld() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         int channel = 5; */
    /*         m.setValueAt("ffff", channel, LocoIOTableModel.ADDRCOLUMN); */
    /*         m.setValueAt(LocoIOTableModel.STATUSMESSAGE, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*         m.setValueAt(null, channel, LocoIOTableModel.WRITECOLUMN); */
    /*  */
    /*         write3Sequence(channel, 0xC0, 0xFF, 0xFF,lnis ); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.STATUSMESSAGE, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "ffff", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*     } */
    /*  */
    /*     /** */
    /*      * Service routine, runs through the sequence for a write operation, */
    /*      * checking each of the three bytes given as arguments. */
    /*      * @param channel The channel being read */
    /*      * @param cv value value expected for configuration CV read */
    /*      * @param addrlow value expected for the low address read */
    /*      * @param addrhigh value expected for the high address read */
    /*      * @param lnis Test interface for loconet i/o */
// */
/*     void write3Sequence(int channel, int cv, int val1, int val2, */
    /*                         LocoNetInterfaceScaffold lnis ) { */
    /*         int src; */
    /*         int dst; */
    /*         // check transmitted message */
    /*         Assert.assertEquals("One message sent", 1, lnis.outbound.size()); */
    /*         LocoNetMessage msg = (LocoNetMessage)lnis.outbound.elementAt(0); */
    /*         // write low addr */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 " */
    /*                             +(((val1&0x80)!=0)?("08"):"00")+" 01 " */
    /*                             +StringUtil.twoHexFromInt(channel*3+4)+" 00 " */
    /*                             +StringUtil.twoHexFromInt(val1&0x7f)+" 10 00 00 00 00 00", msg.toString()); */
    /*  */
    /*         // turn that message around as the echo */
    /*         lnis.sendTestMessage(msg); */
    /*         Assert.assertEquals("listener present", 1, lnis.numListeners()); */
    /*         Assert.assertEquals("echo ignored", 1, lnis.outbound.size()); */
    /*  */
    /*         // turn around as the reply to the read low */
    /*         src = msg.getElement(2); */
    /*         dst = msg.getElement(3); */
    /*         msg.setElement(2, dst); */
    /*         msg.setElement(3, src); */
    /*         msg.setElement(4, 0x01);  // seems to be fixed PC address high */
    /*         msg.setElement(8,0); */
    /*         msg.setElement(10, 0x10+((val1&0x80)!=0?8:0)); */
    /*         msg.setElement(14,val1&0x7f); */
    /*         lnis.sendTestMessage(msg); */
    /*  */
    /*         // 2nd write */
    /*         Assert.assertEquals("reply does 2nd read", 2, lnis.outbound.size()); */
    /*         msg = (LocoNetMessage)lnis.outbound.elementAt(1); */
    /*         // CV11 for read high address */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 " */
    /*                             +(((val2&0x80)!=0)?("08"):"00")+" 01 " */
    /*                             +StringUtil.twoHexFromInt(channel*3+5)+" 00 " */
    /*                             +StringUtil.twoHexFromInt(val2&0x7f)+" 10 00 00 00 00 00", msg.toString()); */
    /*  */
    /*         // turn around as the reply to the write high */
    /*         src = msg.getElement(2); */
    /*         dst = msg.getElement(3); */
    /*         msg.setElement(2, dst); */
    /*         msg.setElement(3, src); */
    /*         msg.setElement(4, 0x01);  // seems to be fixed PC address high */
    /*         msg.setElement(8,0); */
    /*         msg.setElement(10, 0x10+((val2&0x80)!=0?8:0)); */
    /*         msg.setElement(14,val2&0x7f); */
    /*         lnis.sendTestMessage(msg); */
    /*  */
    /*         // 3rd read */
    /*         Assert.assertEquals("reply does 3rd read", 3, lnis.outbound.size()); */
    /*         msg = (LocoNetMessage)lnis.outbound.elementAt(2); */
    /*         // channel 2 (above) is CV 9 for read mode */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 " */
    /*                             +(((cv&0x80)!=0)?("08"):"00")+" 01 " */
    /*                             +StringUtil.twoHexFromInt(channel*3+3)+" 00 " */
    /*                             +StringUtil.twoHexFromInt(cv&0x7f)+" 10 00 00 00 00 00", msg.toString()); */
    /*  */
    /*         // turnaround as the reply to the mode read */
    /*         src = msg.getElement(2); */
    /*         dst = msg.getElement(3); */
    /*         msg.setElement(2, dst); */
    /*         msg.setElement(3, src); */
    /*         msg.setElement(4, 0x01);  // seems to be fixed PC address high */
    /*         msg.setElement(8,0); */
    /*         msg.setElement(10, 0x10+((cv&0x80)!=0?8:0)); */
    /*         msg.setElement(14,cv&0x7f); */
    /*         lnis.sendTestMessage(msg); */
    /*  */
    /*         Assert.assertEquals("reply does no more messages", 3, lnis.outbound.size()); */
    /*     } */
    /*  */
    /*     // test for outgoing read request */
    /*     public void testSendReadCommand() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         m.sendReadCommand(1); */
    /*  */
    /*         // check transmitted message */
    /*         Assert.assertEquals("One message sent", 1, lnis.outbound.size()); */
    /*         LocoNetMessage msg = (LocoNetMessage)lnis.outbound.elementAt(0); */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 00 02 01 00 00 10 00 00 00 00 00", msg.toString()); */
    /*     } */
    /*  */
    /*     // test for outgoing write request */
    /*     public void testSendWriteCommand() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         m.sendWriteCommand(1, 0x31); */
    /*  */
    /*         // check transmitted message */
    /*         Assert.assertEquals("One message sent", 1, lnis.outbound.size()); */
    /*         LocoNetMessage msg = (LocoNetMessage)lnis.outbound.elementAt(0); */
    /*         Assert.assertEquals("message length", 16, msg.getNumDataElements()); */
    /*         Assert.assertEquals("message opCode", 0xE5, msg.getOpCode()); */
    /*         Assert.assertEquals("message bytes", "E5 10 50 51 10 00 01 01 00 31 10 00 00 00 00 00", msg.toString()); */
    /*     } */
    /*  */
    /*     // test Alex Shepherd's sequence with pre1.3.7.1 microcode */
    /*     public void testAlexsSequenceOld() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         // do the channel write */
    /*         int channel = 2; */
    /*         m.setValueAt("0000", channel, LocoIOTableModel.ADDRCOLUMN); */
    /*         m.setValueAt(LocoIOTableModel.STATUSMESSAGE, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*         m.setValueAt(null, channel, LocoIOTableModel.WRITECOLUMN); */
    /*  */
    /*         write3Sequence(channel, 0xC0, 0x00, 0x00,lnis ); */
    /*  */
    /*         // clear the test interface */
    /*         lnis.outbound.setSize(0); */
    /*  */
    /*         // change the mode value to confirm */
    /*         m.setValueAt(LocoIOTableModel.TURNOUTTHROW, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*  */
    /*         // and read back */
    /*         m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN); */
    /*         read3Sequence(channel, 0xC0, 0x00, 0x00, lnis, true ); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.STATUSMESSAGE, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "0", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*  */
    /*     } */
    /*  */
    /*     // test Alex Shepherd's sequence with post 1.3.7.1 microcode */
    /*     public void testAlexsSequenceNew() { */
    /*         // prepare an interface */
    /*         LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(); */
    /*  */
    /*         LocoIOTableModel m = new LocoIOTableModel(0x1051, null); */
    /*  */
    /*         // do the channel write */
    /*         int channel = 2; */
    /*         m.setValueAt("0000", channel, LocoIOTableModel.ADDRCOLUMN); */
    /*         m.setValueAt(LocoIOTableModel.STATUSMESSAGE, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*         m.setValueAt(null, channel, LocoIOTableModel.WRITECOLUMN); */
    /*  */
    /*         write3Sequence(channel, 0xC0, 0x00, 0x00,lnis ); */
    /*  */
    /*         // clear the test interface */
    /*         lnis.outbound.setSize(0); */
    /*  */
    /*         // change the mode value to confirm */
    /*         m.setValueAt(LocoIOTableModel.TURNOUTTHROW, channel, LocoIOTableModel.ONMODECOLUMN); */
    /*  */
    /*         // and read back */
    /*         m.setValueAt(null, channel, LocoIOTableModel.READCOLUMN); */
    /*         read3Sequence(channel, 0xC0, 0x00, 0x00, lnis, false ); */
    /*  */
    /*         Assert.assertEquals("mode", LocoIOTableModel.STATUSMESSAGE, */
    /*                             m.getValueAt(channel, m.ONMODECOLUMN)); */
    /*         Assert.assertEquals("addr", "0", m.getValueAt(channel, m.ADDRCOLUMN)); */
    /*  */
    /*     } */
    // from here down is testing infrastructure
    public LocoIOTableModelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoIOTableModelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoIOTableModelTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
