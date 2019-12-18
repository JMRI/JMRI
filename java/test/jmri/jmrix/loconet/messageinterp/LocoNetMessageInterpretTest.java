package jmri.jmrix.loconet.messageinterp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.loconet.LnReporter;
import jmri.jmrix.loconet.LnReporterManager;
import jmri.jmrix.loconet.LnTurnout;
import jmri.jmrix.loconet.LnTurnoutManager;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 *
 * @author B. Milhaupt Copyright (C) 2018
 */
public class LocoNetMessageInterpretTest {

    @Test
    public void testTransponding() {
        LocoNetMessage l;
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        LnReporterManager lnrm = new LnReporterManager(lnis.getSystemConnectionMemo());

        jmri.InstanceManager.setReporterManager(lnrm);

        l = new LocoNetMessage(new int[] {0xD0, 0x01, 0x20, 0x08, 0x20, 0x26});
        Assert.assertEquals("out A",
                "Transponder address 1056 absent at LR161 () "+
                        "(BDL16x Board ID 11 RX4 zone A or "+
                        "BXP88 Board ID 21 section 1 or "+
                        "the BXPA1 Board ID 161 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x20, 0x08, 0x20, 0x04});
        Assert.assertEquals(" in A",
                "Transponder address 1056 present at LR161 () "+
                        "(BDL16x Board ID 11 RX4 zone A or "+
                        "BXP88 Board ID 21 section 1 or "+
                        "the BXPA1 Board ID 161 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x22, 0x08, 0x20, 0x24});
        Assert.assertEquals(" in B",
                "Transponder address 1056 present at LR163 () "+
                        "(BDL16x Board ID 11 RX4 zone B or "+
                        "BXP88 Board ID 21 section 3 or "+
                        "the BXPA1 Board ID 163 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x24, 0x7d, 0x70, 0x04});
        Assert.assertEquals(" in C",
                "Transponder address 112 (short, or \"B2\") "+
                        "(or long address 16112) present at LR165 () "+
                        "(BDL16x Board ID 11 RX4 zone C or "+
                        "BXP88 Board ID 21 section 5 or "+
                        "the BXPA1 Board ID 165 section).\n",
                        LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x26, 0x08, 0x20, 0x04});
        Assert.assertEquals(" in D", "Transponder address 1056 present at LR167 () "+
                        "(BDL16x Board ID 11 RX4 zone D or "+
                        "BXP88 Board ID 21 section 7 or "+
                        "the BXPA1 Board ID 167 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x28, 0x08, 0x20, 0x04});
        Assert.assertEquals(" in E", "Transponder address 1056 present at LR169 () "+
                "(BDL16x Board ID 11 RX4 zone E or "+
                        "BXP88 Board ID 22 section 1 or "+
                        "the BXPA1 Board ID 169 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x2A, 0x08, 0x20, 0x04});
        Assert.assertEquals(" in F", "Transponder address 1056 present at LR171 () "+
                "(BDL16x Board ID 11 RX4 zone F or "+
                        "BXP88 Board ID 22 section 3 or "+
                        "the BXPA1 Board ID 171 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x2C, 0x08, 0x20, 0x04});
        Assert.assertEquals(" in G", "Transponder address 1056 present at LR173 () "+
                "(BDL16x Board ID 11 RX4 zone G or "+
                        "BXP88 Board ID 22 section 5 or "+
                        "the BXPA1 Board ID 173 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x2E, 0x08, 0x20, 0x04});
        Assert.assertEquals(" in H",
                "Transponder address 1056 present at LR47 () "+
                "(BDL16x Board ID 3 RX4 zone H or "+
                        "BXP88 Board ID 6 section 7 or "+
                        "the BXPA1 Board ID 47 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x2E, 0x7d, 0x01, 0x04});
        Assert.assertEquals("another in H",
                "Transponder address 1 (short) (or long address 16001) present at LR47 () "+
                "(BDL16x Board ID 3 RX4 zone H or "+
                        "BXP88 Board ID 6 section 7 or "+
                        "the BXPA1 Board ID 47 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x40, 0x7D, 0x03, 0x00, 0x00, 0x00, 0x2D});
        Assert.assertEquals("find loco 3 (short)",
                "Transponding Find query for loco address 3 (short) (or long address 16003).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x7D, 0x03, 0x00, 0x12, 0x00, 0x7F});
        Assert.assertEquals(" in H",
                "Transponder Find report: address 3 (short) (or long address 16003) present at LR19 "+
                "(BDL16x Board 2 RX4 zone B).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        LnReporter r = (LnReporter) lnrm.provideReporter("LR19");

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x7D, 0x03, 0x00, 0x14, 0x00, 0x7F});
        Assert.assertEquals("Transponding no reporter user name",
                "Transponder Find report: address 3 (short) (or long address 16003) present at LR21 "+
                "(BDL16x Board 2 RX4 zone C).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        r.setUserName("AUserName");

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x7D, 0x03, 0x00, 0x12, 0x00, 0x7F});
        Assert.assertEquals("Transponding in B, with reporter user name",
                "Transponder Find report: address 3 (short) (or long address 16003) present at LR19 (AUserName) "+
                "(BDL16x Board 2 RX4 zone B).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x01, 0x7D, 0x03, 0x00, 0x12, 0x00, 0x7F});
        Assert.assertEquals("Transponding Bad Message 1",
                "Unable to parse LocoNet message.\ncontents: E5 09 01 7D 03 00 12 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x40, 0x00, 0x04, 0x00, 0x00, 0x00, 0x2D});
        Assert.assertEquals("find loco 4 (long)",
                "Transponding Find query for loco address 4 (short).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x16, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR23 "+
                "(BDL16x Board 2 RX4 zone D).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        Assert.assertNull("reporter not Created", lnrm.getBySystemName("LR23"));
        lnrm.provideReporter("LR23");

        Assert.assertNull("reporter is Not Yet Created", lnrm.getBySystemName("LR25"));
        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x18, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR25 "+
                "(BDL16x Board 2 RX4 zone E).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
        Assert.assertNull("reporter not Created", lnrm.getBySystemName("LR25"));
        lnrm.provideReporter("LR25");
        ((LnReporter) lnrm.getBySystemName("LR25")).setUserName("Friendly name E");
        Assert.assertEquals("check setting of username", lnrm.getBySystemName("LR25").getUserName(), "Friendly name E");

        ((LnReporter) lnrm.provideReporter("LR31")).setUserName("Friendly Name H");
        ((LnReporter) lnrm.provideReporter("LR29")).setUserName("Friendly Name G");
        ((LnReporter) lnrm.provideReporter("LR27")).setUserName("Friendly Name F");
        ((LnReporter) lnrm.provideReporter("LR23")).setUserName("Friendly Name D");
        ((LnReporter) lnrm.provideReporter("LR21")).setUserName("Friendly Name C");
        ((LnReporter) lnrm.provideReporter("LR19")).setUserName("Friendly Name B");
        ((LnReporter) lnrm.provideReporter("LR17")).setUserName("Friendly Name A");

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x18, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR25 (Friendly name E) "+
                "(BDL16x Board 2 RX4 zone E).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x14, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR21 (Friendly Name C) "+
                "(BDL16x Board 2 RX4 zone C).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x12, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR19 (Friendly Name B) "+
                "(BDL16x Board 2 RX4 zone B).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x10, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR17 (Friendly Name A) "+
                "(BDL16x Board 2 RX4 zone A).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x1A, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR27 (Friendly Name F) "+
                "(BDL16x Board 2 RX4 zone F).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x1C, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR29 (Friendly Name G) "+
                "(BDL16x Board 2 RX4 zone G).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x00, 0x04, 0x00, 0x1E, 0x00, 0x7F});
        Assert.assertEquals(" in D",
                "Transponder Find report: address 4 (short) present at LR31 (Friendly Name H) "+
                "(BDL16x Board 2 RX4 zone H).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x00, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in A/1",
                "Transponder address 1 (short) present at LR1 () "+
                "(BDL16x Board ID 1 RX4 zone A or "+
                        "BXP88 Board ID 1 section 1 or "+
                        "the BXPA1 Board ID 1 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x01, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 2",
                "Transponder address 1 (short) present at LR2 () "+
                        "(BXP88 Board ID 1 section 2 or "+
                        "the BXPA1 Board ID 2 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x02, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in B/3",
                "Transponder address 1 (short) present at LR3 () "+
                "(BDL16x Board ID 1 RX4 zone B or "+
                        "BXP88 Board ID 1 section 3 or "+
                        "the BXPA1 Board ID 3 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x03, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 4",
                "Transponder address 1 (short) present at LR4 () "+
                        "(BXP88 Board ID 1 section 4 or "+
                        "the BXPA1 Board ID 4 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x04, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in C/5",
                "Transponder address 1 (short) present at LR5 () "+
                "(BDL16x Board ID 1 RX4 zone C or "+
                        "BXP88 Board ID 1 section 5 or "+
                        "the BXPA1 Board ID 5 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x05, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 6",
                "Transponder address 1 (short) present at LR6 () "+
                        "(BXP88 Board ID 1 section 6 or "+
                        "the BXPA1 Board ID 6 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x06, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in D/7",
                "Transponder address 1 (short) present at LR7 () "+
                "(BDL16x Board ID 1 RX4 zone D or "+
                        "BXP88 Board ID 1 section 7 or "+
                        "the BXPA1 Board ID 7 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x07, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 8",
                "Transponder address 1 (short) present at LR8 () "+
                        "(BXP88 Board ID 1 section 8 or "+
                        "the BXPA1 Board ID 8 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x08, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in E/9",
                "Transponder address 1 (short) present at LR9 () "+
                "(BDL16x Board ID 1 RX4 zone E or "+
                        "BXP88 Board ID 2 section 1 or "+
                        "the BXPA1 Board ID 9 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x09, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 10",
                "Transponder address 1 (short) present at LR10 () "+
                        "(BXP88 Board ID 2 section 2 or "+
                        "the BXPA1 Board ID 10 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x0A, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in 11",
                "Transponder address 1 (short) present at LR11 () "+
                "(BDL16x Board ID 1 RX4 zone F or "+
                        "BXP88 Board ID 2 section 3 or "+
                        "the BXPA1 Board ID 11 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x0B, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 12",
                "Transponder address 1 (short) present at LR12 () "+
                        "(BXP88 Board ID 2 section 4 or "+
                        "the BXPA1 Board ID 12 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x0C, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in G/13",
                "Transponder address 1 (short) present at LR13 () "+
                "(BDL16x Board ID 1 RX4 zone G or "+
                        "BXP88 Board ID 2 section 5 or "+
                        "the BXPA1 Board ID 13 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x0D, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 14",
                "Transponder address 1 (short) present at LR14 () "+
                        "(BXP88 Board ID 2 section 6 or "+
                        "the BXPA1 Board ID 14 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x0E, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in H/15",
                "Transponder address 1 (short) present at LR15 () "+
                "(BDL16x Board ID 1 RX4 zone H or "+
                        "BXP88 Board ID 2 section 7 or "+
                        "the BXPA1 Board ID 15 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x0F, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 16",
                "Transponder address 1 (short) present at LR16 () "+
                        "(BXP88 Board ID 2 section 8 or "+
                        "the BXPA1 Board ID 16 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x10, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in A/1",
                "Transponder address 1 (short) present at LR17 (Friendly Name A) "+
                "(BDL16x Board ID 2 RX4 zone A or "+
                        "BXP88 Board ID 3 section 1 or "+
                        "the BXPA1 Board ID 17 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x11, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 2",
                "Transponder address 1 (short) present at LR18 () "+
                        "(BXP88 Board ID 3 section 2 or "+
                        "the BXPA1 Board ID 18 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x17, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 8",
                "Transponder address 1 (short) present at LR24 () "+
                        "(BXP88 Board ID 3 section 8 or "+
                        "the BXPA1 Board ID 24 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x18, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in A/1",
                "Transponder address 1 (short) present at LR25 (Friendly name E) "+
                "(BDL16x Board ID 2 RX4 zone E or "+
                        "BXP88 Board ID 4 section 1 or "+
                        "the BXPA1 Board ID 25 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x1F, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 8",
                "Transponder address 1 (short) present at LR32 () "+
                        "(BXP88 Board ID 4 section 8 or "+
                        "the BXPA1 Board ID 32 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x20, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in A/1",
                "Transponder address 1 (short) present at LR33 () "+
                "(BDL16x Board ID 3 RX4 zone A or "+
                        "BXP88 Board ID 5 section 1 or "+
                        "the BXPA1 Board ID 33 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x28, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in E/1",
                "Transponder address 1 (short) present at LR41 () "+
                "(BDL16x Board ID 3 RX4 zone E or "+
                        "BXP88 Board ID 6 section 1 or "+
                        "the BXPA1 Board ID 41 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x29, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in 2",
                "Transponder address 1 (short) present at LR42 () "+
                        "(BXP88 Board ID 6 section 2 or "+
                        "the BXPA1 Board ID 42 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x3F, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 8",
                "Transponder address 1 (short) present at LR64 () "+
                        "(BXP88 Board ID 8 section 8 or "+
                        "the BXPA1 Board ID 64 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x40, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in A/1",
                "Transponder address 1 (short) present at LR65 () "+
                "(BDL16x Board ID 5 RX4 zone A or "+
                        "BXP88 Board ID 9 section 1 or "+
                        "the BXPA1 Board ID 65 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x7F, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in section 8",
                "Transponder address 1 (short) present at LR128 () "+
                        "(BXP88 Board ID 16 section 8 or "+
                        "the BXPA1 Board ID 128 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x21, 0x00, 0x00, 0x01, 0x04});
        Assert.assertEquals("one in A/1",
                "Transponder address 1 (short) present at LR129 () "+
                "(BDL16x Board ID 9 RX4 zone A or "+
                        "BXP88 Board ID 17 section 1 or "+
                        "the BXPA1 Board ID 129 section).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


    }

    @Test
    public void OpcPeerXfer7Byte() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x07, 0x00, 0x00, 0x00, 0x00, 0x1D});
        Assert.assertEquals("not a known 7byte opcPeerXfer",
                "Unable to parse LocoNet message.\ncontents: E5 07 00 00 00 00 1D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
        l = new LocoNetMessage(new int[] {0xE5, 0x07, 0x01, 0x49, 0x42, 0x40, 0x00});
        Assert.assertEquals("Uhlenbrock stop programming track",
                "Uhlenbrock IB-COM / Intellibox II Stop Programming Track.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x07, 0x01, 0x49, 0x42, 0x41, 0x00});
        Assert.assertEquals("Uhlenbrock start programming track",
                "Uhlenbrock IB-COM / Intellibox II Start Programming Track.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x07, 0x01, 0x49, 0x42, 0x42, 0x55});
        Assert.assertEquals("Uhlenbrock unknown programming track operation 1",
                "Unable to parse LocoNet message.\ncontents: E5 07 01 49 42 42 55\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x07, 0x01, 0x49, 0x41, 0x40, 0x54});
        Assert.assertEquals("Uhlenbrock unknown programming track operation 2",
                "Unable to parse LocoNet message.\ncontents: E5 07 01 49 41 40 54\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x07, 0x01, 0x48, 0x42, 0x40, 0x56});
        Assert.assertEquals("Uhlenbrock unknown programming track operation 3",
                "Unable to parse LocoNet message.\ncontents: E5 07 01 48 42 40 56\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x07, 0x00, 0x49, 0x42, 0x40, 0x56});
        Assert.assertEquals("Uhlenbrock unknown programming track operation 4",
                "Unable to parse LocoNet message.\ncontents: E5 07 00 49 42 40 56\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
    }

    @Test
    public void testALM() {

        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("Get Aliasing Information.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("Get Aliasing Information.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03});
        Assert.assertEquals(" ALM task 2 test 1",
                "Write ALM msg 2 ATASK=0 (ID) BLKL=0 BLKH=0 LOGIC=0\n\tARG1L=0x00 ARG1H=0x00 ARG2L=0x00 ARG2H=0x00\n\tARG3L=0x00 ARG3H=0x00 ARG4L=0x00 ARG4H=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testRoutes() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xEE, 0x10, 1, 2, 0, 0, 0x0, 0,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query DCS100/200 Route 1 entries 1-4 or DCS210/240 Route 1 entries 1-4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "", "", ""));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 1, 2, 1, 0, 0x0, 0,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query DCS100/200 Route 1 entries 5-8 or DCS210/240 Route 1 entries 5-8.\n",
                LocoNetMessageInterpret.interpretMessage(l, "", "", ""));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 1, 2, 2, 0, 0x0, 0,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12.\n",
                LocoNetMessageInterpret.interpretMessage(l, "", "", ""));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 1, 2, 3, 0, 0x0, 0,0,0,0,0,0,0,0x7f,0});
        Assert.assertEquals("Query DCS100/200 Route 2 entries 5-8 or DCS210/240 Route 1 entries 13-16.\n",
                LocoNetMessageInterpret.interpretMessage(l, "", "", ""));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x20, 0x00, 0x0, 0xF, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7, 0xF, 0x2D});
        Assert.assertEquals("Query DCS100/200 Route 17 entries 1-4 or DCS210/240 Route 9 entries 1-4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x78, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query DCS100/200 Route 29 entries 1-4 or DCS210/240 Route 63 entries 1-4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7C, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query DCS100/200 Route 31 entries 1-4 or DCS210/240 Route 64 entries 1-4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7D, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query DCS100/200 Route 31 entries 5-8 or DCS210/240 Route 64 entries 5-8.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7E, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query DCS100/200 Route 32 entries 1-4 or DCS210/240 Route 64 entries 9-12.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x02, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Query DCS100/200 Route 32 entries 5-8 or DCS210/240 Route 64 entries 13-16.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x01, 0x03, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Write DCS100/200 Route 32 entries 5-8 or DCS210/240 Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x03, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Report DCS100/200 Route 32 entries 5-8 or DCS210/240 Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x02, 0x7F, 0x01, 0x0F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x74});
        Assert.assertEquals("Report DCS100/200 Route 32 entries 5-8 or DCS210/240 Route 64 entries 13-16 with Unused, Unused, Unused, Unused.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x01, 0x02, 0x02, 0x00, 0x0F, 0x0C, 0x20, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x2B});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13c, Unused, Unused, Unused.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x7F ,0x7F ,0x75});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13c, 128t, 129c, Unused.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x02 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x7F ,0x7F ,0x75});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13c, 128t, 129c, Unused.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x03 ,0x02 ,0x00 ,0x0F ,0x0C ,0x20 ,0x7F ,0x10 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13c, 128t, 129c, 513c.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x03 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x10 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13t, 128t, 129c, 513c.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x03 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x31 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13t, 128c, 129c, 513c.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x03 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x11 ,0x00 ,0x34 ,0x48});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13t, 128c, 129t, 513c.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x01 ,0x03 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x11 ,0x00 ,0x14 ,0x48});
        Assert.assertEquals("Report DCS100/200 Route 2 entries 1-4 or DCS210/240 Route 1 entries 9-12 with 13t, 128c, 129t, 513t.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6 ,0x10 ,0x05 ,0x03 ,0x02 ,0x00 ,0x0F ,0x0C ,0x10 ,0x7F ,0x30 ,0x00 ,0x11 ,0x00 ,0x14 ,0x48});
        Assert.assertEquals("Read ALM msg (Write reply) 5 ATASK=3 (WR) BLKL=2 BLKH=0 LOGIC=15\n" +
                "\tARG1L=0x0C ARG1H=0x10 ARG2L=0x7F ARG2H=0x30\n" +
                "\tARG3L=0x00 ARG3H=0x11 ARG4L=0x00 ARG4H=0x14.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testSVProgrammingProtocolV1() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x53, 0x01, 0x00, 0x02, 0x03, 0x00, 0x00, 0x10, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 1", "LocoBuffer => LocoIO@53/1 Query SV3.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x53, 0x01, 0x00, 0x02, 0x03, 0x00, 0x00, 0x10, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 2", "LocoBuffer => LocoIO@53/1 Query SV3.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x51, 0x01, 0x00, 0x02, 0x34, 0x02, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 3", "Locobuffer=> LocoIO@0x51/1 Query SV52 Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x51, 0x01, 0x00, 0x02, 0x34, 0x12, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 4", "Locobuffer=> LocoIO@0x51/1 Query SV52 Firmware rev 1.8.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x51, 0x01, 0x00, 0x02, 0x34, 0x75, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 5", "Locobuffer=> LocoIO@0x51/1 Query SV52 Firmware rev 1.1.7.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x51, 0x01, 0x00, 0x02, 0x34, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 6", "Locobuffer=> LocoIO@0x51/1 Query SV52.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x50, 0x01, 0x00, 0x02, 0x34, 0x02, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 7", "Locobuffer=> LocoBuffer  Query SV52 Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x50, 0x01, 0x00, 0x02, 0x34, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 7", "Locobuffer=> LocoBuffer  Query SV52 Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x51, 0x50, 0x01, 0x02, 0x02, 0x33, 0x02, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x51/1=> LocoBuffer  Report SV179 = 0 Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x02, 0x33, 0x02, 0x00, 0x08, 0x00, 0x00, 0x00, 0x34, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Report SV179 = 0 Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x01, 0x33, 0x02, 0x00, 0x08, 0x00, 0x00, 0x00, 0x34, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Write SV179 = 180 (0xb4) Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x00, 0x33, 0x02, 0x00, 0x08, 0x00, 0x00, 0x00, 0x01, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Write SV179 = 129 (0x81) Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x00, 0x7f, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Write SV255 = 2 Firmware rev 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Write SV255 = 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));





        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x50, 0x01, 0x00, 0x02, 0x34, 0x63, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 7", "Locobuffer=> LocoBuffer  Query SV52 Firmware rev 9.9.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x50, 0x01, 0x00, 0x02, 0x34, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 7", "Locobuffer=> LocoBuffer  Query SV52.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x51, 0x50, 0x01, 0x02, 0x02, 0x33, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x51/1=> LocoBuffer  Report SV179 = 0.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x02, 0x33, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x34, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Report SV179 = 180 (0xb4).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x01, 0x33, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x34, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Write SV179 = 180 (0xb4).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x00, 0x33, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x01, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Write SV179 = 129 (0x81).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x52, 0x50, 0x01, 0x02, 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x18});
        Assert.assertEquals(" read SV 8", "LocoIO@0x52=> LocoBuffer  Write SV255 = 2.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


    }

    @Test
    public void testSVProgrammingProtocolV2() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x02, 0x02, 0x10, 0x23, 0x00, 0x03, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("SV2 test 1", "(SV Format 2) Read single SV request to destination address 35 initiated by agent 1:\n"
                +"\tRead request for SV3\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x48, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x49});
        Assert.assertEquals("SV2 test 2", "(SV Format 2) Reply from destination address 513 to Identify device request initiated by agent 1:\n"
                +"\tDevice characteristics are manufacturer 3, developer number 4, product "+jmri.util.IntlUtilities.valueOf(1541)+", serial number "+jmri.util.IntlUtilities.valueOf(2055)+"\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x12, 0x40, 0x20, 0x10, 0x08, 0x10, 0x04, 0x02, 0x01, 0x7F, 0x0A});
        Assert.assertEquals("SV2 test 3","(SV Format 2) Write single SV request to destination address "+jmri.util.IntlUtilities.valueOf(41024)+" initiated by agent 1:\n"
                +"\tChange SV"+jmri.util.IntlUtilities.valueOf(2064)+" to 4\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x08});
        Assert.assertEquals("SV test 4", "(SV Format 2) Write single SV request to destination address 0 initiated by agent 1:\n"
                +"\tChange SV0 to 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x10, 0x01, 0x02, 0x04, 0x08, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x08});
        Assert.assertEquals("SV test 5", "(SV Format 2) Write single SV request to destination address 513 initiated by agent 1:\n"
                +"\tChange SV"+jmri.util.IntlUtilities.valueOf(2052)+" to 16\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x02, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x03});
        Assert.assertEquals("SV test 6", "(SV Format 2) Read single SV request to destination address 513 initiated by agent 1:\n"
                +"\tRead request for SV"+jmri.util.IntlUtilities.valueOf(1027)+"\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x03, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x02});
        Assert.assertEquals("SV2 test 7", "(SV Format 2) Write single SV (masked) request to destination address 513 initiated by agent 1:\n"
                +"\tchange SV"+jmri.util.IntlUtilities.valueOf(1027)+" to 5, applying write mask 6\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x01, 0x02, 0x04, 0x08, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x0C});
        Assert.assertEquals("SV2 test 8", "(SV Format 2) Write four request to destination address 513 initiated by agent 1:\n"
                +"\twrite SVs "+jmri.util.IntlUtilities.valueOf(2052)+" thru "+jmri.util.IntlUtilities.valueOf(2055)+"(?) with values 16, 32, 64, and 127\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x02, 0x04, 0x08, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x01, 0x0C});
        Assert.assertEquals("SV2 test 9", "(SV Format 2) Write four request to destination address "+jmri.util.IntlUtilities.valueOf(1026)+" initiated by agent 1:\n"
                +"\twrite SVs "+jmri.util.IntlUtilities.valueOf(4104)+" thru "+jmri.util.IntlUtilities.valueOf(4107)+"(?) with values 32, 64, 127, and 1\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x04, 0x08, 0x10, 0x20, 0x10, 0x40, 0x7F, 0x01, 0x02, 0x0C});
        Assert.assertEquals("SV2 test 10", "(SV Format 2) Write four request to destination address "+jmri.util.IntlUtilities.valueOf(2052)+" initiated by agent 1:\n"
                +"\twrite SVs "+jmri.util.IntlUtilities.valueOf(8208)+" thru "+jmri.util.IntlUtilities.valueOf(8211)+"(?) with values 64, 127, 1, and 2\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x08, 0x10, 0x20, 0x40, 0x10, 0x7F, 0x01, 0x02, 0x04, 0x0C});
        Assert.assertEquals("SV2 test 11", "(SV Format 2) Write four request to destination address "+jmri.util.IntlUtilities.valueOf(4104)+" initiated by agent 1:\n"
                +"\twrite SVs "+jmri.util.IntlUtilities.valueOf(16416)+" thru "+jmri.util.IntlUtilities.valueOf(16419)+"(?) with values 127, 1, 2, and 4\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x10, 0x20, 0x40, 0x7F, 0x10, 0x01, 0x02, 0x04, 0x08, 0x0C});
        Assert.assertEquals("SV2 test 12", "(SV Format 2) Write four request to destination address "+jmri.util.IntlUtilities.valueOf(8208)+" initiated by agent 1:\n"
                +"\twrite SVs "+jmri.util.IntlUtilities.valueOf(32576)+" thru "+jmri.util.IntlUtilities.valueOf(32579)+"(?) with values 1, 2, 4, and 8\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x20, 0x40, 0x7F, 0x01, 0x10, 0x02, 0x04, 0x08, 0x10, 0x0C});
        Assert.assertEquals("SV2 test 13", "(SV Format 2) Write four request to destination address "+jmri.util.IntlUtilities.valueOf(16416)+" initiated by agent 1:\n"
                +"\twrite SVs 383 thru 386(?) with values 2, 4, 8, and 16\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x40, 0x7F, 0x01, 0x02, 0x10, 0x04, 0x08, 0x10, 0x20, 0x0C});
        Assert.assertEquals("SV2 test 14", "(SV Format 2) Write four request to destination address "+jmri.util.IntlUtilities.valueOf(32576)+" initiated by agent 1:\n"
                +"\twrite SVs 513 thru 516(?) with values 4, 8, 16, and 32\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x11, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x0D});
        Assert.assertEquals("SV2 test 15", "(SV Format 2) Write four request to destination address 128 initiated by agent 1:\n"
                +"\twrite SVs 0 thru 3(?) with values 0, 0, 0, and 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x12, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x0E});
        Assert.assertEquals("SV2 test 16", "(SV Format 2) Write four request to destination address "+jmri.util.IntlUtilities.valueOf(32768)+" initiated by agent 1:\n"
                +"\twrite SVs 0 thru 3(?) with values 0, 0, 0, and 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x14, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x08});
        Assert.assertEquals("SV2 test 17", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n"
                +"\twrite SVs 128 thru 131(?) with values 0, 0, 0, and 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x18, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x04});
        Assert.assertEquals("SV2 test 18", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n"
                +"\twrite SVs "+jmri.util.IntlUtilities.valueOf(32768)+" thru "+jmri.util.IntlUtilities.valueOf(32771)+"(?) with values 0, 0, 0, and 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x11, 0x00, 0x00, 0x00, 0x00, 0x0D});
        Assert.assertEquals("SV2 test 19", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n"
                +"\twrite SVs 0 thru 3(?) with values 128, 0, 0, and 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00, 0x00, 0x00, 0x00, 0x0E});
        Assert.assertEquals("SV2 test 20", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n"
                +"\twrite SVs 0 thru 3(?) with values 0, 128, 0, and 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x14, 0x00, 0x00, 0x00, 0x00, 0x08});
        Assert.assertEquals("SV2 test 21", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n"
                +"\twrite SVs 0 thru 3(?) with values 0, 0, 128, and 0\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x05, 0x02, 0x10, 0x00, 0x00, 0x00, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x04});
        Assert.assertEquals("SV2 test 22", "(SV Format 2) Write four request to destination address 0 initiated by agent 1:\n"
                +"\twrite SVs 0 thru 3(?) with values 0, 0, 0, and 128\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x06, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x07});
        Assert.assertEquals("SV2 test 23", "(SV Format 2) Read four SVs request to destination address 513 initiated by agent 1:\n"
                +"\tread SVs "+jmri.util.IntlUtilities.valueOf(1027)+" thru "+jmri.util.IntlUtilities.valueOf(1030)+"(?)\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x07, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x06});
        Assert.assertEquals("SV2 test 24", "(SV Format 2) Discover all devices request initiated by agent 1\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x08, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x09});
        Assert.assertEquals("SV2 test 25", "(SV Format 2) Identify Device request initiated by agent 1 to destination address 513\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x09, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x08});
        Assert.assertEquals("SV2 test 26", "(SV Format 2) Change address request initiated by agent 1:\n"
                +"\tChange address of device with manufacturer 3, developer number 4, product "+jmri.util.IntlUtilities.valueOf(1541)+", and serial number "+jmri.util.IntlUtilities.valueOf(2055)+" so that it responds as destination address 513\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x0A, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x0B});
        Assert.assertEquals("SV2 test 27", "Unable to parse LocoNet message.\ncontents: E5 10 01 0A 02 10 01 02 03 04 10 05 06 07 08 0B\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x0F, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x0E});
        Assert.assertEquals("SV2 test 28", "(SV Format 2) Reconfigure request initiated by agent 1 to destination address 513\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x41, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x40});
        Assert.assertEquals("SV2 test 29", "(SV Format 2) Reply from destination address 513 for Write single SV request initiated by agent 1:\n"
                +"\tSV"+jmri.util.IntlUtilities.valueOf(1027)+" current value is 5\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x42, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x43});
        Assert.assertEquals("SV2 test 30", "(SV Format 2) Reply from destination address 513 for Read single SV request initiated by agent 1:\n"
                +"\tSV"+jmri.util.IntlUtilities.valueOf(1027)+" current value is 5\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x43, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x42});
        Assert.assertEquals("SV2 test 31", "(SV Format 2) Reply from destination address 513 for Write single SV (masked) request initiated by agent 1:\n"
                +"\tSV"+jmri.util.IntlUtilities.valueOf(1027)+" written with mask 6; SV"+jmri.util.IntlUtilities.valueOf(1027)+" current value is 5\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x45, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x44});
        Assert.assertEquals("SV2 test 32", "(SV Format 2) Reply from destination address 513 to Write four request initiated by agent 1:\n"
                +"\tSVs "+jmri.util.IntlUtilities.valueOf(1027)+" thru "+jmri.util.IntlUtilities.valueOf(1030)+"(?) current values are 5, 6, 7, and 8\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x46, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x47});
        Assert.assertEquals("SV2 test 33", "(SV Format 2) Reply from destination address 513 to Read four request initiated by agent 1:\n"
                +"\tSVs "+jmri.util.IntlUtilities.valueOf(1027)+" thru "+jmri.util.IntlUtilities.valueOf(1030)+"(?) current values are 5, 6, 7, and 8\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x47, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x46});
        Assert.assertEquals("SV2 test 34", "(SV Format 2) Reply from destination address 513 to Discover devices request initiated by agent 1:\n"
                +"\tDevice characteristics are manufacturer 3, developer number 4, product "+jmri.util.IntlUtilities.valueOf(1541)+", serial number "+jmri.util.IntlUtilities.valueOf(2055)+"\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x48, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x49});
        Assert.assertEquals("SV2 test 35", "(SV Format 2) Reply from destination address 513 to Identify device request initiated by agent 1:\n"
                +"\tDevice characteristics are manufacturer 3, developer number 4, product "+jmri.util.IntlUtilities.valueOf(1541)+", serial number "+jmri.util.IntlUtilities.valueOf(2055)+"\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x49, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x48});
        Assert.assertEquals("SV2 test 36", "(SV Format 2) Reply to Change address request initiated by agent 1:\n"
                +"\tDevice with manufacturer 3, developer number 4, product "+jmri.util.IntlUtilities.valueOf(1541)+", and serial number "+jmri.util.IntlUtilities.valueOf(2055)+" is now using destination address 513\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x4A, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x4B});
        Assert.assertEquals("SV2 test 37", "Unable to parse LocoNet message.\ncontents: E5 10 01 4A 02 10 01 02 03 04 10 05 06 07 08 4B\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x4F, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x4E});
        Assert.assertEquals("SV2 test 38", "(SV Format 2) Reply from destination address 513 to Reconfigure request initiated by agent 1:\n"
                +"\tDevice characteristics are manufacturer 3, developer number 4, product "+jmri.util.IntlUtilities.valueOf(1541)+", serial number "+jmri.util.IntlUtilities.valueOf(2055)+"\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x00, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x01});
        Assert.assertEquals("SV2 test 39", "Unable to parse LocoNet message.\ncontents: E5 10 01 00 02 10 01 02 03 04 10 05 06 07 08 01\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x11, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x10});
        Assert.assertEquals("SV2 test 40", "Unable to parse LocoNet message.\ncontents: E5 10 01 11 02 10 01 02 03 04 10 05 06 07 08 10\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x21, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x20});
        Assert.assertEquals("SV2 test 41", "Unable to parse LocoNet message.\ncontents: E5 10 01 21 02 10 01 02 03 04 10 05 06 07 08 20\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x31, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x30});
        Assert.assertEquals("SV2 test 42", "Unable to parse LocoNet message.\ncontents: E5 10 01 31 02 10 01 02 03 04 10 05 06 07 08 30\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x40, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x41});
        Assert.assertEquals("SV2 test 43", "Unable to parse LocoNet message.\ncontents: E5 10 01 40 02 10 01 02 03 04 10 05 06 07 08 41\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x51, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x50});
        Assert.assertEquals("SV2 test 44", "Unable to parse LocoNet message.\ncontents: E5 10 01 51 02 10 01 02 03 04 10 05 06 07 08 50\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x61, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x60});
        Assert.assertEquals("SV2 test 45", "Unable to parse LocoNet message.\ncontents: E5 10 01 61 02 10 01 02 03 04 10 05 06 07 08 60\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x71, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x70});
        Assert.assertEquals("SV2 test 46", "Unable to parse LocoNet message.\ncontents: E5 10 01 71 02 10 01 02 03 04 10 05 06 07 08 70\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x00, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x10});
        Assert.assertEquals("SV2 test 47", "Unable to parse LocoNet message.\ncontents: E5 10 01 01 02 00 01 02 03 04 10 05 06 07 08 10\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x02, 0x10, 0x01, 0x02, 0x03, 0x04, 0x00, 0x05, 0x06, 0x07, 0x08, 0x10});
        Assert.assertEquals("SV2 test 48", "Unable to parse LocoNet message.\ncontents: E5 10 01 01 02 10 01 02 03 04 00 05 06 07 08 10\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x01, 0x10, 0x01, 0x02, 0x03, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x03});
        Assert.assertEquals("SV2 test 49", "Unable to parse LocoNet message.\ncontents: E5 10 01 01 01 10 01 02 03 04 10 05 06 07 08 03\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x01, 0x10, 0x01, 0x02, 0x03, 0x04, 0x11, 0x05, 0x06, 0x07, 0x08, 0x03});
        Assert.assertEquals("SV2 test 50", "Unable to parse LocoNet message.\ncontents: E5 10 01 01 01 10 01 02 03 04 11 05 06 07 08 03\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x01, 0x10, 0x01, 0x02, 0x03, 0x04, 0x12, 0x05, 0x06, 0x07, 0x08, 0x03});
        Assert.assertEquals("SV2 test 51", "Unable to parse LocoNet message.\ncontents: E5 10 01 01 01 10 01 02 03 04 12 05 06 07 08 03\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x01, 0x10, 0x01, 0x02, 0x03, 0x04, 0x14, 0x05, 0x06, 0x07, 0x08, 0x03});
        Assert.assertEquals("SV2 test 52", "Unable to parse LocoNet message.\ncontents: E5 10 01 01 01 10 01 02 03 04 14 05 06 07 08 03\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x01, 0x01, 0x01, 0x10, 0x01, 0x02, 0x03, 0x04, 0x18, 0x05, 0x06, 0x07, 0x08, 0x03});
        Assert.assertEquals("SV2 test 53", "Unable to parse LocoNet message.\ncontents: E5 10 01 01 01 10 01 02 03 04 18 05 06 07 08 03\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
    }

    @Test
    public void testLissy() {
        LocoNetMessage l = new LocoNetMessage(new int[] {0xE4, 0x08, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        Assert.assertEquals("Lissy message test 1", "Lissy 1 IR Report: Loco 8501 moving south\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x08, 0x00, 0x40, 0x01, 0x42, 0x35, 0x25});
        Assert.assertEquals("Lissy message test 2", "Lissy 1 IR Report: Loco 8501 moving north\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x09, 0x00, 0x69, 0x00, 0x01, 0x18, 0x00, 0x62});
        Assert.assertEquals("Lissy message test 3",
                "Unrecognized Signal State report (typically sent by CML SIGM10, SIGM20).\ncontents: E4 09 00 69 00 01 18 00 62\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x0A, 0x00, 0x69, 0x00, 0x01, 0x18, 0x00, 0x00, 0x62});
        Assert.assertEquals("Lissy message test 4",
                "SE106 (105) reports AX:0 XA:0 no reservation; Turnout Closed Occupied.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x0B, 0x00, 0x69, 0x01, 0x01, 0x18, 0x00, 0x00, 0x62});
        Assert.assertEquals("Lissy message test 5",
                "Unable to parse LocoNet message.\ncontents: E4 0B 00 69 01 01 18 00 00 62\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x0A, 0x00, 0x69, 0x00, 0x11, 0x18, 0x00, 0x00, 0x62});
        Assert.assertEquals("Lissy message test 6",
                "SE106 (105) reports AX:0 XA:0 AX reserved; Turnout Closed Occupied.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x0A, 0x00, 0x69, 0x00, 0x21, 0x18, 0x00, 0x00, 0x62});
        Assert.assertEquals("Lissy message test 7",
                "SE106 (105) reports AX:0 XA:0 XA reserved; Turnout Closed Occupied.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x0A, 0x00, 0x69, 0x00, 0x31, 0x19, 0x00, 0x00, 0x62});
        Assert.assertEquals("Lissy message test 8",
                "SE106 (105) reports AX:0 XA:0 AX, XA reserved; Turnout Thrown Occupied.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x0A, 0x00, 0x69, 0x00, 0x00, 0x19, 0x00, 0x00, 0x62});
        Assert.assertEquals("Lissy message test 9",
                "SE106 (105) reports AX:0 XA:0 no reservation; Turnout Thrown Not occupied.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x08, 0x01, 0x60, 0x02, 0x42, 0x35, 0x05});
        Assert.assertEquals("Lissy message test 10", "Lissy 2 Wheel Report: 8501 wheels moving south\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x08, 0x01, 0x40, 0x14, 0x42, 0x35, 0x25});
        Assert.assertEquals("Lissy message test 11", "Lissy 20 Wheel Report: 8501 wheels moving north\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testOpcAnalogIO() {
        LocoNetMessage l = new LocoNetMessage(new int[] {0xE4, 0x08, 0x01, 0x01, 0x03, 0x32, 0x11, 0x35});
        Assert.assertEquals("OpcAnalogIO message test 1", "Lissy 3 Wheel Report: 6417 wheels moving north\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x08, 0x01, 0x21, 0x55, 0x01, 0x00, 0x35});
        Assert.assertEquals("OpcAnalogIO message test ", "Lissy 85 Wheel Report: 128 wheels moving south\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE4, 0x08, 0x02, 0x21, 0x55, 0x01, 0x00, 0x35});
        Assert.assertEquals("Unable to parse LocoNet message.\ncontents: E4 08 02 21 55 01 00 35\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testLACK() {
        LocoNetMessage l;

        l =new LocoNetMessage(new int[] {0xB4, 0x6F, 0x23, 0x07});
        Assert.assertEquals("LACK 0x6f 0x23", "LONG_ACK: DCS51 programming reply, thought to mean OK.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x30, 0x00, 0x7B});
        Assert.assertEquals("LACK 0x30 0x00", "LONG_ACK: Switch request Failed!\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x3F, 0x00, 0x7B});
        Assert.assertEquals("LACK 0x3f 0x00", "LONG_ACK: NO FREE SLOTS!\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x39, 0x00, 0x7B});
        Assert.assertEquals("LACK 0x39 0x00", "LONG_ACK: Invalid consist, unable to link.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x3d, 0x00, 0x7B});
        Assert.assertEquals("LACK 0x3d 0x00", "LONG_ACK: The Command Station FIFO is full, the switch command was rejected.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x3d, 0x7f, 0x7B});
        Assert.assertEquals("LACK 0x3d 0x7f", "LONG_ACK: The Command Station accepted the switch command.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x3d, 0x01, 0x7B});
        Assert.assertEquals("LACK 0x3d 0x01",
                "LONG_ACK: Unknown response to Request Switch with ACK command, value 0x01.\ncontents: B4 3D 01 7B\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x3d, 0x7e, 0x7B});
        Assert.assertEquals("LACK 0x3d 0x7e",
                "LONG_ACK: Unknown response to Request Switch with ACK command, value 0x7E.\ncontents: B4 3D 7E 7B\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x00, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x00",
                "LONG_ACK: The Slot Write command was rejected.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x01, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x01",
                "LONG_ACK: The Slot Write command was accepted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x23, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x23",
                "LONG_ACK: DCS51 programming reply, thought to mean OK.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x2B, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x2B",
                "LONG_ACK: DCS51 programming reply, thought to mean OK.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x6b, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x6b",
                "LONG_ACK: DCS51 programming reply, thought to mean OK.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x40, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x40",
                "LONG_ACK: The Slot Write command was accepted blind (no response will be sent).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x7f, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x7f",
                "LONG_ACK: Function not implemented, no reply will follow.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x7e, 0x7B});
        Assert.assertEquals("LACK 0x6f 0x7e",
                "LONG_ACK: Unknown response to Write Slot Data message value 0x7E.\n"
                        + "contents: B4 6F 7E 7B\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testIPL() {
        LocoNetMessage l;

        l =new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x5C, 0x18, 0x00, 0x04, 0x00, 0x07, 0x6A, 0x01, 0x40, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x58});
        Assert.assertEquals("IPL test 1",
                "IPL Identity report.\n\tHost: Digitrax UR92 host, S/N=40016A, S/W Version=0.4\n\tSlave: Digitrax RF24 slave, S/N=AEE, S/W Version=0.7.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08});
        Assert.assertEquals("IPL test 2",
                "Discover all IPL-capable devices request.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x5C, 0x00, 0x01, 0x00, 0x00, 0x00, 0x6A, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3F});
        Assert.assertEquals("IPL test 3",
                "Discover Digitrax UR92 host devices and/or Digitrax (no slave device type specified) devices.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL test 4",
                "Pinging device with serial number 0xAEE.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0f, 0x10, 0x00, 0x2A, 0x18, 0x00, 0x05, 0x00, 0x07, 0x7e, 0x43, 0x00, 0x00, 0x70, 0x0a, 0x00, 0x00, 0x67});
        Assert.assertEquals("IPL test 5",
                "IPL Identity report.\n\tHost: Digitrax DT402(x) host, S/N=437E, S/W Version=0.5\n\tSlave: Digitrax RF24 slave, S/N=A70, S/W Version=0.7.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x23, 0x00, 0x00, 0x08, 0x00, 0x01, 0x01, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x45});
        Assert.assertEquals("IPL test 6",
                "IPL Identity report.\n\tHost: Digitrax PR3 host, S/N=7F01, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x04, 0x00, 0x00, 0x08, 0x00, 0x01, 0x40, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x5D});
        Assert.assertEquals("IPL test 7",
                "IPL Identity report.\n\tHost: Digitrax UT4(x) host, S/N=140, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x33, 0x00, 0x00, 0x08, 0x00, 0x05, 0x59, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 8",
                "IPL Identity report.\n\tHost: Digitrax DCS51 host, S/N=159, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x34, 0x00, 0x00, 0x08, 0x00, 0x05, 0x59, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 8b",
                "IPL Identity report.\n\tHost: Digitrax DCS52 host, S/N=159, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x08, 0x00, 0x05, 0x59, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 9",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=159, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x1b, 0x00, 0x00, 0x08, 0x00, 0x05, 0x59, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 10",
                "IPL Identity report.\n\tHost: Digitrax DCS210 host, S/N=159, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x1c, 0x00, 0x00, 0x08, 0x00, 0x05, 0x59, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 11",
                "IPL Identity report.\n\tHost: Digitrax DCS240 host, S/N=159, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x18, 0x00, 0x08, 0x00, 0x06, 0x59, 0x01, 0x00, 0x18, 0x03, 0x10, 0x12, 0x34, 0x77});
        Assert.assertEquals("IPL test 12",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=159, S/W Version=1.0\n\tSlave: Digitrax RF24 slave, S/N=B4121003, S/W Version=0.6.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x01, 0x00, 0x08, 0x00, 0x18, 0x59, 0x01, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 13",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=159, S/W Version=1.0\n\tSlave: Digitrax (unknown slave device type 1), S/N=800000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x19, 0x00, 0x08, 0x00, 0x06, 0x59, 0x01, 0x00, 0x12, 0x03, 0x10, 0x12, 0x34, 0x77});
        Assert.assertEquals("IPL test 14",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=159, S/W Version=1.0\n\tSlave: Digitrax (unknown slave device type 25), S/N=34129003, S/W Version=0.6.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x08, 0x00, 0x18, 0x59, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 15",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=159, S/W Version=1.0\n\tSlave: Digitrax (unknown slave device type 54), S/N=80, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 16",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=0, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x01, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 17",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=1, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x02, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 18",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=2, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x04, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 19",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=4, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x08, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 20",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=8, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x10, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 21",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=10, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x20, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 22",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=20, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x40, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 23",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=40, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x01, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 24",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=80, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x01, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 25",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=100, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x02, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 26",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=200, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x04, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 27",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=400, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x08, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 28",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=800, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x10, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 29",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=1000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x20, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 30",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=2000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x40, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 31",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=4000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x02, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 32",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=8000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x01, 0x00, 0x77});
        Assert.assertEquals("IPL test 33",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=10000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x02, 0x00, 0x77});
        Assert.assertEquals("IPL test 34",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=20000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x04, 0x00, 0x77});
        Assert.assertEquals("IPL test 35",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=40000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x08, 0x00, 0x77});
        Assert.assertEquals("IPL test 36",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=80000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x10, 0x00, 0x77});
        Assert.assertEquals("IPL test 37",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=100000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x20, 0x00, 0x77});
        Assert.assertEquals("IPL test 38",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=200000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x40, 0x00, 0x77});
        Assert.assertEquals("IPL test 38",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=400000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x04, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 40",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=800000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x01, 0x77});
        Assert.assertEquals("IPL test 41",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=1000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x02, 0x77});
        Assert.assertEquals("IPL test 42",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=2000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x04, 0x77});
        Assert.assertEquals("IPL test 43",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=4000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x08, 0x77});
        Assert.assertEquals("IPL test 44",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=8000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x10, 0x77});
        Assert.assertEquals("IPL test 45",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=10000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x20, 0x77});
        Assert.assertEquals("IPL test 46",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=20000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x00, 0x00, 0x00, 0x00, 0x40, 0x77});
        Assert.assertEquals("IPL test 47",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=40000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x36, 0x00, 0x13, 0x00, 0x18, 0x00, 0x32, 0x21, 0x08, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 48",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=213200, S/W Version=2.3\n\tSlave: Digitrax (unknown slave device type 54), S/N=80000000, S/W Version=3.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 49",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 50",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=0.1\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 51",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=0.2\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 52",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=0.4\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 53",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=1.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 54",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=2.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 55",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=4.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 56",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=0, S/W Version=8.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x01, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 57",
                "IPL Identity report.\n\tHost: Unknown device (Manufacturer code 1, product code 50), S/N=1, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 58",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=1, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 59",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=2, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 60",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=4, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 61",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=8, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 62",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=10, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 63",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=20, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 64",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=40, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 65",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=80, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 66",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=100, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 67",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=200, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 68",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=400, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 69",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=800, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 70",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=1000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 71",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=2000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 72",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=4000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 73",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=8000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 74",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=800000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 75",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=10000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 76",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=20000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 77",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=40000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 78",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=80000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 79",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=100000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 80",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=200000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL test 81",
                "IPL Identity report.\n\tHost: Digitrax DT500(x) host, S/N=400000, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08});
        Assert.assertEquals("IPL test 82",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 01 00 00 00 00 00 00 01 00 00 00 00 00 00 00 08\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 83",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 20 00 00 00 01 00 00 00 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 84",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 40 00 00 01 00 00 00 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 85",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 10 00 01 00 00 00 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 86",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 09 01 00 00 00 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 87",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 03 00 00 00 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 88",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 01 04 00 00 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 89",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 01 00 02 00 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 90",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 01 00 00 70 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 91",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 01 00 00 00 03 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x17, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL test 92",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 01 00 00 00 00 17 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x23, 0x00, 0x00});
        Assert.assertEquals("IPL test 93",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 01 00 00 00 00 00 23 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x45, 0x00});
        Assert.assertEquals("IPL test 94",
                "Unable to parse LocoNet message.\ncontents: E5 14 0F 08 00 00 00 00 00 00 00 01 00 00 00 00 00 00 45 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x00, 0x18, 0x01, 0x00, 0x00, 0x00, 0x6A, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3F});
        Assert.assertEquals("IPL test 95",
                "Discover Digitrax (no host device type specified) devices and/or Digitrax RF24 slave devices.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x08, 0x00, 0x5C, 0x18, 0x01, 0x00, 0x00, 0x00, 0x6A, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3F});
        Assert.assertEquals("IPL test 96",
                "Discover Digitrax UR92 host devices and/or Digitrax RF24 slave devices.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x24, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL identity test 97",
                "IPL Identity report.\n\tHost: Digitrax PR4 host, S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x58, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL identity test 98",
                "IPL Identity report.\n\tHost: Digitrax BXP88 host, S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x63, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL identity test 99",
                "IPL Identity report.\n\tHost: Digitrax LNWI host, S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL identity test 100",
                "IPL Identity report.\n\tHost: Digitrax DB210 Opto host, S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL identity test 101",
                "IPL Identity report.\n\tHost: Digitrax DB210 host, S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x16, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        Assert.assertEquals("IPL identity test 102",
                "IPL Identity report.\n\tHost: Digitrax DB220 host, S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
    }

    @Test
    public void testIplHostNumbers() {
        LocoNetMessage l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x0F, 0x10, 0x00, 0x16, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x77});
        String s;
        for (int i = 0; i < 128; ++i) {
            l.setElement(5, i);
            switch (i) {
                case 0:
                    s = "Digitrax (no host device type specified)";
                    break;
                case 0x01:
                    s = "Digitrax LNRP host";
                    break;
                case 0x04:
                    s = "Digitrax UT4(x) host";
                    break;
                case 0x0C:
                    s = "Walthers (Digitrax) WTL12 host";
                    break;
                case 0x14:
                    s = "Digitrax DB210 Opto host";
                    break;
                case 0x15:
                    s = "Digitrax DB210 host";
                    break;
                case 0x16:
                    s = "Digitrax DB220 host";
                    break;
                case 0x1B:
                    s = "Digitrax DCS210 host";
                    break;
                case 0x1C:
                    s = "Digitrax DCS240 host";
                    break;
                case 0x23:
                    s = "Digitrax PR3 host";
                    break;
                case 0x24:
                    s = "Digitrax PR4 host";
                    break;
                case 0x2A:
                    s = "Digitrax DT402(x) host";
                    break;
                case 0x32:
                    s = "Digitrax DT500(x) host";
                    break;
                case 0x33:
                    s = "Digitrax DCS51 host";
                    break;
                case 0x34:
                    s = "Digitrax DCS52 host";
                    break;
                case 0x58:
                    s = "Digitrax BXP88 host";
                    break;
                case 0x5C:
                    s = "Digitrax UR92 host";
                    break;
                case 0x63:
                    s = "Digitrax LNWI host";
                    break;
                default:
                    s = "Digitrax (unknown host device type "+i+")";
                    break;
            }
            Assert.assertEquals("IPL HOST NAME Test "+i,
                "IPL Identity report.\n\tHost: "+s+", S/N=0, S/W Version=0.0\n\tSlave: None.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
         }
    }

    @Test
    public void testIplPingMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0xAEE.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 02",
                "Pinging device with serial number 0x10AEE.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 03",
                "Pinging device with serial number 0x2000AEE.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 04",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 03 00 00 00 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 05",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 04 00 00 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 06",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 05 00 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 07",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 00 06 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 08",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 00 00 07 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 09",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 00 00 00 08 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 10",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 00 00 00 00 09 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 11",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 00 00 00 00 00 0A 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0b, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 12",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 00 00 00 00 00 00 0B 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C, 0x6B});
        Assert.assertEquals("IPL Ping test 13",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 01 6E 0A 00 00 00 00 00 00 00 00 00 00 00 0C 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 14",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 15",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 16",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping test 17",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 08 70 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x6E, 0x0A, 0x00, 0x24, 0x00, 0x50, 0x0d, 0x21, 0x50, 0x43, 0x21, 0x17, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping Reply test 01",
                "Ping Report response from device with serial number 24000AEE: Local RSSI=21, Remote RSSI=50.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x00, 0x6E, 0x0A, 0x00, 0x24, 0x00, 0x50, 0x0d, 0x21, 0x50, 0x43, 0x21, 0x17, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping Reply test 01",
                "Ping Report response from device with serial number 24000A6E: Local RSSI=21, Remote RSSI=50.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x50, 0x0d, 0x21, 0x50, 0x43, 0x21, 0x17, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping Reply test 01",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 10 00 00 00 00 00 00 50 0D 21 50 43 21 17 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x50, 0x0d, 0x21, 0x50, 0x43, 0x21, 0x17, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping Reply test 01",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 10 70 00 00 00 00 00 50 0D 21 50 43 21 17 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x50, 0x0d, 0x21, 0x50, 0x43, 0x21, 0x17, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping Reply test 01",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 10 10 00 00 00 00 00 50 0D 21 50 43 21 17 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x50, 0x0d, 0x21, 0x50, 0x43, 0x21, 0x17, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping Reply test 01",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 10 20 00 00 00 00 00 50 0D 21 50 43 21 17 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x50, 0x0d, 0x21, 0x50, 0x43, 0x21, 0x17, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL Ping Reply test 01",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 10 40 00 00 00 00 00 50 0D 21 50 43 21 17 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));





        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 01",
                "Ping Report response from device with serial number AEE: Local RSSI=00, Remote RSSI=00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x10, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number AEE: Local RSSI=10, Remote RSSI=00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x14, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number AEE: Local RSSI=89, Remote RSSI=00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x18, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number AEE: Local RSSI=09, Remote RSSI=80.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number AEE: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x07, 0x10, 0x01, 0x6E, 0x0A, 0x00, 0x00, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 10 01 6E 0A 00 00 10 00 00 09 21 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x00, 0x0A, 0x00, 0x00, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number A80: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x00, 0x00, 0x33, 0x00, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 330080: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x03, 0x00, 0x00, 0x33, 0x00, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 338080: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x07, 0x00, 0x00, 0x33, 0x00, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number B38080: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x07, 0x00, 0x00, 0x33, 0x02, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 2B38080: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x0F, 0x00, 0x00, 0x33, 0x02, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 82B38080: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0x0, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 10 00 00 00 00 00 10 00 00 09 21 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x00, 0x01, 0x00, 0x00, 0x0, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 1: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x01, 0x02, 0x00, 0x00, 0x0, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 82: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x00, 0x00, 0x03, 0x00, 0x0, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 300: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x10, 0x02, 0x00, 0x03, 0x00, 0x0, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Ping Report response from device with serial number 8300: Local RSSI=09, Remote RSSI=21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x09, 0x02, 0x00, 0x03, 0x00, 0x0, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 09 02 00 03 00 00 10 00 00 09 21 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x11, 0x02, 0x00, 0x03, 0x00, 0x0, 0x10, 0x00, 0x00, 0x09, 0x21, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping report test 02",
                "Unable to parse LocoNet message.\ncontents: E5 14 08 11 02 00 03 00 00 10 00 00 09 21 00 00 00 00 00 6B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0x1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0x81.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0x200.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x02, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0x8200.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0x90000.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x04, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0x890000.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x00, 0x00, 0x00, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0xA000000.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x08, 0x08, 0x08, 0x00, 0x00, 0x00, 0x0B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6B});
        Assert.assertEquals("IPL ping test 01",
                "Pinging device with serial number 0x8B000000.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testSv1Messages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xe5, 0x10, 0x50, 0x51, 0x01, 0x00, 0x01, 0x02, 0x13, 0x04, 0x10, 0x05, 0x06, 0x07, 0x08, 0x00});
        Assert.assertEquals("SV1 test 1",
                "LocoBuffer => LocoIO@51/5 Write SV2=0x4 Firmware rev 1.9.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x04, 0x01, 0x00, 0x01, 0x10, 0x07, 0x14, 0x10, 0x05, 0x06, 0x07, 0x08, 0x12});
        Assert.assertEquals("SV1 test 2",
                "LocoBuffer => LocoIO@4/5 Write SV16=0x14 Firmware rev 7.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x50, 0x04, 0x01, 0x00, 0x01, 0x10, 0x78, 0x24, 0x1F, 0x05, 0x06, 0x07, 0x08, 0x12});
        Assert.assertEquals("SV1 test 3",
                "LocoBuffer => LocoIO@4/85 Write SV16=0x24 Firmware rev 1.2.0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testProgrammingMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7C, 0x67, 0x00, 0x28, 0x23, 0x00, 0x02, 0x10, 0x54, 0x33, 0x44, 0x3F});
        Assert.assertEquals("OpsModeProg test 1", "Byte Write (No feedback) on Main Track: Decoder address 5155: CV17 value 212 (0xD4, 11010100b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x40, 0x64});
        Assert.assertEquals("OpsModeProg test 2", "LONG_ACK: The Slot Write command was accepted blind (no response will be sent).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7C, 0x2F, 0x00, 0x28, 0x23, 0x00, 0x02, 0x11, 0x54, 0x33, 0x44, 0x76});
        Assert.assertEquals("OpsModeProg test 3", "Byte Read on Main Track (Ops Mode): Decoder address 5155: CV18.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x2F, 0, 0x7D, 0x01, 0x00, 0x02, 0x01, 0x7F, 0x7F, 0x7F, 0x4D}); //!!
        Assert.assertEquals("Bit mode direct read test 16001",
                "Byte Read on Main Track (Ops Mode): Decoder address 1 (short) (or long address 16001): CV2.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x7F, 0x5B});
        Assert.assertEquals("OpsModeProg test 4",
                "LONG_ACK: Function not implemented, no reply will follow.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7C, 0x2F, 0x10, 0x28, 0x23, 0x00, 0x00, 0x11, 0x23, 0x33, 0x44, 0x1B});
        Assert.assertEquals("OpsModeProg test 5",
                "Programming Response: Byte Read on Main Track (Ops Mode) Was successful via RX4/BDL16x: Decoder address 5155: CV18 value 35 (0x23, 00100011b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7C, 0x67, 0x00, 0x28, 0x23, 0x00, 0x00, 0x11, 0x23, 0x33, 0x44, 0x4B});
        Assert.assertEquals("OpsModeProg test 7",
                "Byte Write (No feedback) on Main Track: Decoder address 5155: CV18 value 35 (0x23, 00100011b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7C, 0x2F, 0x00, 0x28, 0x23, 0x00, 0x00, 0x11, 0x23, 0x33, 0x44, 0x03});
        Assert.assertEquals("OpsModeProg test 8",
                "Byte Read on Main Track (Ops Mode): Decoder address 5155: CV18.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x7F, 0x5B});
        Assert.assertEquals("OpsModeProg test 9",
                "LONG_ACK: Function not implemented, no reply will follow.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7C, 0x2F, 0x10, 0x28, 0x23, 0x00, 0x00, 0x11, 0x23, 0x33, 0x44, 0x1B});
        Assert.assertEquals("OpsModeProg test 10",
                "Programming Response: Byte Read on Main Track (Ops Mode) Was successful via RX4/BDL16x: Decoder address 5155: CV18 value 35 (0x23, 00100011b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7C, 0x2F, 0x00, 0x28, 0x23, 0x00, 0x00, 0x10, 0x23, 0x33, 0x44, 0x02});
        Assert.assertEquals("OpsModeProg test 11",
                "Byte Read on Main Track (Ops Mode): Decoder address 5155: CV17.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x7F, 0x5B});
        Assert.assertEquals("OpsModeProg test 12",
                "LONG_ACK: Function not implemented, no reply will follow.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7C, 0x2F, 0x10, 0x28, 0x23, 0x00, 0x02, 0x10, 0x54, 0x33, 0x44, 0x6F});
        Assert.assertEquals("OpsModeProg test 13",
                "Programming Response: Byte Read on Main Track (Ops Mode) Was successful via RX4/BDL16x: Decoder address 5155: CV17 value 212 (0xD4, 11010100b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7C, 0x2F, 0x00, 0x28, 0x23, 0x00, 0x02, 0x11, 0x54, 0x33, 0x44, 0x76});
        Assert.assertEquals("OpsModeProg test 14",
                "Byte Read on Main Track (Ops Mode): Decoder address 5155: CV18.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x7F, 0x5B});
        Assert.assertEquals("OpsModeProg test 15",
                "LONG_ACK: Function not implemented, no reply will follow.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7C, 0x2F, 0x10, 0x01, 0x00, 0x00, 0x00, 0x01, 0x40, 0x33, 0x44, 0x1B});
        Assert.assertEquals("OpsModeProg test 16",
                "Programming Response: Byte Read on Main Track (Ops Mode) Was successful via RX4/BDL16x: Decoder address 128: CV2 value 64 (0x40, 01000000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7C, 0x2F, 0x00, 0x00, 0x01, 0x00, 0x00, 0x65, 0x23, 0x33, 0x44, 0x02});
        Assert.assertEquals("OpsModeProg test 17",
                "Byte Read on Main Track (Ops Mode): Decoder address 1 (short): CV102.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7C, 0x2F, 0x10, 0x00, 0x01, 0x00, 0x02, 0x7f, 0x00, 0x33, 0x44, 0x6F});
        Assert.assertEquals("OpsModeProg test 19",
                "Programming Response: Byte Read on Main Track (Ops Mode) Was successful via RX4/BDL16x: Decoder address 1 (short): CV128 value 128 (0x80, 10000000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       mode
        0x28/0x68   Direct mode byte Read/Write on Service track
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x68, 0, 0x00, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("direct Mode direct write test 1",
                "Byte Write in Direct Mode on Service Track: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x68, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("byte mode direct write test 2",
                "Byte Write in Direct Mode on Service Track: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x68, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Byte mode direct read test 3",
                "Programming Response: Write Byte in Direct Mode on Service Track Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x28, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Byte mode direct read test 4",
                "Byte Read in Direct Mode on Service Track: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x28, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("byte mode direct test 5",
                "Programming Response: Read Byte in Direct Mode on Service Track Was Successful: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x28, 0, 0x00, 0x00, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("byte mode direct test 5",
                "Programming Response: Read Byte in Direct Mode on Service Track Was Successful: CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       mode
        0x08/0x48   Direct mode bit Read/WRite on Service track
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x48, 0, 0x00, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Bit Write in Direct Mode on Service Track: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x48, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Bit Write in Direct Mode on Service Track: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x48, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Write Bit in Direct Mode on Service Track Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x08, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Bit Read in Direct Mode on Service Track: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x08, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Bit in Direct Mode on Service Track Was Successful: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x08, 0, 0x00, 0x00, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Bit in Direct Mode on Service Track Was Successful: CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x20/0x60   Paged mode byte Read/Write on Service track
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x60, 0, 0x00, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Byte Write in Paged Mode on Service Track: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x60, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Byte Write in Paged Mode on Service Track: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x60, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Write Byte in Paged Mode on Service Track Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x20, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Byte Read in Paged Mode on Service Track: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x20, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Paged Mode on Service Track Was Successful: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x20, 0, 0x00, 0x00, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Paged Mode on Service Track Was Successful: CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x28/0x68   Direct mode byte Read/Write on Service track
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x68, 0, 0x00, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Byte Write in Direct Mode on Service Track: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x68, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Byte Write in Direct Mode on Service Track: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x68, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Write Byte in Direct Mode on Service Track Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x28, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Byte Read in Direct Mode on Service Track: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x28, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Direct Mode on Service Track Was Successful: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x28, 0, 0x00, 0x00, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Direct Mode on Service Track Was Successful: CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x10/0x50   Physical Register byte Read/Write on Service Track
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x50, 0, 0x00, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Byte Write in Physical Register Mode on Service Track: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x50, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Byte Write in Physical Register Mode on Service Track: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x50, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Write Byte in Physical Register Mode on Service Track Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x10, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Byte Read in Physical Register Mode on Service Track: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x10, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Physical Register Mode on Service Track Was Successful: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x10, 0, 0x00, 0x00, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Physical Register Mode on Service Track Was Successful: CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x30/0x70   Physical Register byte Read/Write on Service Track
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x70, 0, 0x00, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Byte Write in Physical Register Mode on Service Track: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x70, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Byte Write in Physical Register Mode on Service Track: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x70, 0, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Write Byte in Physical Register Mode on Service Track Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x30, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Byte Read in Physical Register Mode on Service Track: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x30, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Physical Register Mode on Service Track Was Successful: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x30, 0, 0x00, 0x00, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Read Byte in Physical Register Mode on Service Track Was Successful: CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x24/0x64   Ops Mode Byte Read/Write, no feedback
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x64, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Byte Write (No feedback) on Main Track: Decoder address 128: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x64, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Byte Write (No feedback) on Main Track: Decoder address 2 (short): CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x64, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Byte Write (No feedback) on Main Track (Ops Mode) Was Successful: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x24, 0, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Byte Read (No feedback) on Main Track (Ops Mode): Decoder address 1040: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x24, 0, 0x10, 0x20, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Byte Read (No feedback) on Main Track (Ops Mode) Was Successful: Decoder address 2080: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x24, 0, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Byte Read (No feedback) on Main Track (Ops Mode) Was Successful: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x2c/0x6c   Ops Mode Byte Read/Write, feedback
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x6C, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Byte Write on Main Track (Ops Mode): Decoder address 128: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x6C, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Byte Write on Main Track (Ops Mode): Decoder address 2 (short): CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x6C, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Byte Write on Main Track (Ops Mode) Was Successful: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x2C, 0, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Byte Read on Main Track (Ops Mode): Decoder address 1040: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x2C, 0, 0x10, 0x20, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Byte Read on Main Track (Ops Mode) Was Successful: Decoder address 2080: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x2C, 0, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Byte Read on Main Track (Ops Mode) Was Successful: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x04/0x44   Ops Mode Bit Read/Write, no feedback
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x44, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Bit Write (No feedback) on Main Track (Ops Mode): Decoder address 128: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x44, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Bit Write (No feedback) on Main Track (Ops Mode): Decoder address 2 (short): CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x44, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Bit Write (No feedback) on Main Track (Ops Mode) Was Successful: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x04, 0, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Bit Read (No feedback) on Main Track (Ops Mode): Decoder address 1040: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x04, 0, 0x10, 0x20, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read (No feedback) on Main Track (Ops Mode) Was Successful: Decoder address 2080: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x04, 0, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read (No feedback) on Main Track (Ops Mode) Was Successful: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
        PSTAT       Meaning
        0x0c/0x4c   Ops Mode Bit Read/Write, feedback
        */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x4c, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Bit Write on Main Track (Ops Mode): Decoder address 128: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x4c, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Bit Write on Main Track (Ops Mode): Decoder address 2 (short): CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x4c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Bit Write on Main Track (Ops Mode) Was Successful: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x0c, 0, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Bit Read on Main Track (Ops Mode): Decoder address 1040: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x0c, 0, 0x10, 0x20, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read on Main Track (Ops Mode) Was Successful: Decoder address 2080: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x0c, 0, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read on Main Track (Ops Mode) Was Successful: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x4c, 1, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Bit Write on Main Track (Ops Mode) Failed, Service Mode programming track empty: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x4c, 2, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Bit Write on Main Track (Ops Mode) Failed, No Write Acknowledge from decoder: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x4c, 4, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Bit Write on Main Track (Ops Mode) Failed, Read Compare Acknowledge not detected: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x4c, 8, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Bit Write on Main Track (Ops Mode) Failed, User Aborted: Decoder address 520: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x0c, 0x01, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read on Main Track (Ops Mode) Failed, Service Mode programming track empty: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x0c, 0x02, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read on Main Track (Ops Mode) Failed, No Write Acknowledge from decoder: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x0c, 0x04, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read on Main Track (Ops Mode) Failed, Read Compare Acknowledge not detected: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x0c, 0x08, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Bit Read on Main Track (Ops Mode) Failed, User Aborted: Decoder address 3 (short) (or long address 16003): CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x41, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Uhlenbrock IB-COM / Intellibox II Programming Write: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x42, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Uhlenbrock IB-COM / Intellibox II Programming Write: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x43, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Uhlenbrock IB-COM / Intellibox II Programming Write Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x01, 0, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 4",
                "Uhlenbrock IB-COM / Intellibox II Programming Read: CV1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x02, 0, 0x10, 0x20, 0x00, 0x00, 0x00, 0x02, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Uhlenbrock IB-COM / Intellibox II Programming Read Was Successful: CV1 value 2 (0x02, 00000010b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x03, 0, 0x7d, 0x03, 0x00, 0x02, 0x01, 0x08, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct test 5",
                "Programming Response: Uhlenbrock IB-COM / Intellibox II Programming Read Was Successful: CV2 value 136 (0x88, 10001000b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /* oddball cases */

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x58, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Service Track RESERVED MODE Write Detected!: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x18, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Service Track RESERVED MODE Read Detected!: CV129.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x78, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Service Track RESERVED MODE Write Detected!: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEf, 0x0E, 0x7c, 0x38, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Service Track RESERVED MODE Read Detected!: CV129.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x78, 0, 0x01, 0x00, 0x00, 0x00, 0x01, 0x59, 0, 0, 0x6F});
        Assert.assertEquals("Bit Mode direct write test 1",
                "Programming Response: Service Track RESERVED MODE Write Detected! Was Successful: CV2 value 89 (0x59, 01011001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x38, 0, 0x00, 0x02, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct write test 2",
                "Programming Response: Service Track RESERVED MODE Read Detected! Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x14, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 20 (0x14).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x14, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 20 (0x14) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x1c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 28 (0x1C).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x1c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 28 (0x1C) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x34, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 52 (0x34).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x34, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 52 (0x34) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x3c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 60 (0x3C).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x3c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 60 (0x3C) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x54, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 84 (0x54).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x54, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 84 (0x54) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x5c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 92 (0x5C).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x5c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 92 (0x5C) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x74, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 116 (0x74).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x74, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 116 (0x74) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7c, 0x7c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Unknown Programming slot access with programming mode 124 (0x7C).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7c, 0x7c, 0, 0x04, 0x08, 0x00, 0x01, 0x00, 0x09, 0, 0, 0x6F});
        Assert.assertEquals("Bit mode direct read test 3",
                "Programming Response: Unknown Programming slot access with programming mode 124 (0x7C) Was Successful: CV129 value 9 (0x09, 00001001b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testTranspondingMessages() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x40, 0x7D, 0x03, 0x00, 0x00, 0x00, 0x2D});
        Assert.assertEquals(" basic Transponding Test 01",
                "Transponding Find query for loco address 3 (short) (or long address 16003).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x12, 0x7D, 0x03, 0x63});
        Assert.assertEquals(" basic Transponding Test 02",
                "Transponder address 3 (short) (or long address 16003) present at LR19 () "+
                        "(BDL16x Board ID 2 RX4 zone B or "+
                        "BXP88 Board ID 3 section 3 or "+
                        "the BXPA1 Board ID 19 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x0B, 0x70, 0x36});
        Assert.assertEquals(" basic Transponding Test 03",
                "Sensor LS24 () is High.  (BDL16 # 2, DS8; DS54/DS64/SE8c # 3, SwiD/S4/DS08).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x40, 0x01, 0x00, 0x00, 0x00, 0x00, 0x2D});
        Assert.assertEquals(" basic Transponding Test 04",
                "Transponding Find query for loco address 128.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

       l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x00, 0x7D, 0x03, 0x00, 0x12, 0x00, 0x7F});
        Assert.assertEquals(" basic Transponding Test 05",
                "Transponder Find report: address 3 (short) (or long address 16003) present at LR19 (BDL16x Board 2 RX4 zone B).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x00, 0x12, 0x7D, 0x03, 0x43});
        Assert.assertEquals(" basic Transponding Test 06",
                "Transponder address 3 (short) (or long address 16003) absent at LR19 () "+
                        "(BDL16x Board ID 2 RX4 zone B or "+
                        "BXP88 Board ID 3 section 3 or the BXPA1 Board ID 19 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x0B, 0x60, 0x26});
        Assert.assertEquals(" basic Transponding Test 07",
                "Sensor LS24 () is Low.  (BDL16 # 2, DS8; DS54/DS64/SE8c # 3, SwiD/S4/DS08).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x40, 0x00, 0x01, 0x00, 0x00, 0x00, 0x2D});
        Assert.assertEquals(" basic Transponding Test 08",
                "Transponding Find query for loco address 1 (short).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x40, 0x02, 0x01, 0x00, 0x00, 0x00, 0x2D});
        Assert.assertEquals(" basic Transponding Test 09",
                "Transponding Find query for loco address 257.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x09, 0x40, 0x00, 0x71, 0x00, 0x00, 0x00, 0x2D});
        Assert.assertEquals(" basic Transponding Test 10",
                "Transponding Find query for loco address 113 (short, or \"B3\").\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x12, 0x00, 0x03, 0x63});
        Assert.assertEquals(" basic Transponding Test 11",
                "Transponder address 3 (short) present at LR19 () "+
                        "(BDL16x Board ID 2 RX4 zone B or "+
                        "BXP88 Board ID 3 section 3 or "+
                        "the BXPA1 Board ID 19 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x20, 0x15, 0x00, 0x03, 0x63});
        Assert.assertEquals(" basic Transponding Test 12",
                "Transponder address 3 (short) present at LR22 () "+
                        "(BXP88 Board ID 3 section 6 or "+
                        "the BXPA1 Board ID 22 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x00, 0x15, 0x00, 0x03, 0x63});
        Assert.assertEquals(" basic Transponding Test 12",
                "Transponder address 3 (short) absent at LR22 () "+
                        "(BXP88 Board ID 3 section 6 or "+
                        "the BXPA1 Board ID 22 section).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testBasicConsistingMessages() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xB9, 0x0B, 0x05, 0x48});
        Assert.assertEquals(" basic Consisting Test 01", "Consist loco in slot 11 to loco in slot 5.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB8, 0x0E, 0x0D, 0x44});
        Assert.assertEquals(" basic Consisting Test 02", "Remove loco in slot 14 from consist with loco in slot 13.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB9, 0x0E, 0x0D, 0x45});
        Assert.assertEquals(" basic Consisting Test 03", "Consist loco in slot 14 to loco in slot 13.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB8, 0x23, 0x0F, 0x44});
        Assert.assertEquals(" basic Consisting Test 04", "Remove loco in slot 35 from consist with loco in slot 15.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testBasicSensorReportMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xB2, 0x20, 0x41, 0x2C});
        Assert.assertEquals(" basic Sensor Report Test 01", "Sensor LS321 () is Low.  (BDL16 # 21, DS1; DS54/DS64 # 41, AuxA/A1).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x20, 0x61, 0x0C});
        Assert.assertEquals(" basic Sensor Report Test 02",
                "Sensor LS322 () is Low.  (BDL16 # 21, DS2; DS54/DS64 # 41, SwiA/S1).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x23, 0x41, 0x2C});
        Assert.assertEquals(" basic Sensor Report Test 03",
                "Sensor LS327 () is Low.  (BDL16 # 21, DS7; DS54/DS64 # 41, AuxD/A4).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x23, 0x61, 0x0C});
        Assert.assertEquals(" basic Sensor Report Test 04",
                "Sensor LS328 () is Low.  (BDL16 # 21, DS8; DS54/DS64 # 41, SwiD/S4).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x00, 0x10, 0x0C});
        Assert.assertEquals(" basic Sensor Report Test 05",
                "Sensor LS1 () is High.  (BDL16 # 1, DS1; DS54/DS64/SE8c # 1, AuxA/A1/DS01).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x00, 0x20, 0x0C});
        Assert.assertEquals(" basic Sensor Report Test 06",
                "Sensor LS2 () is Low.  (BDL16 # 1, DS2; DS54/DS64/SE8c # 1, SwiA/S1/DS02).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x00, 0x00, 0x0C});
        Assert.assertEquals(" basic Sensor Report Test 07",
                "Sensor LS1 () is Low.  (BDL16 # 1, DS1; DS54/DS64/SE8c # 1, AuxA/A1/DS01).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB2, 0x00, 0x70, 0x0C});
        Assert.assertEquals(" basic Sensor Report Test 08",
                "Sensor LS2 () is High.  (BDL16 # 1, DS2; DS54/DS64/SE8c # 1, SwiA/S1/DS02).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
    }

    @Test
    public void testTurnoutSensorStateMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 01",
                "Turnout LT1 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x01, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 02",
                "Turnout LT2 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x02, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 03",
                "Turnout LT3 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x04, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 04",
                "Turnout LT5 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x08, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 05",
                "Turnout LT9 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x10, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 06",
                "Turnout LT17 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x20, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 07",
                "Turnout LT33 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x40, 0x00, 0x4E});
        Assert.assertEquals("Turnout sensor state test 08",
                "Turnout LT65 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x01, 0x4E});
        Assert.assertEquals("Turnout sensor state test 09",
                "Turnout LT129 () output state: Closed output is Off (open), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x32, 0x4E});
        Assert.assertEquals("Turnout sensor state test 10",
                "Turnout LT257 () output state: Closed output is On (sink), Thrown output is On (sink).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x24, 0x4E});
        Assert.assertEquals("Turnout sensor state test 11",
                "Turnout LT513 () output state: Closed output is On (sink), Thrown output is Off (open).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x18, 0x4E});
        Assert.assertEquals("Turnout sensor state test 12",
                "Turnout LT1025 () output state: Closed output is Off (open), Thrown output is On (sink).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x40, 0x4E});
        Assert.assertEquals("Turnout sensor state test 13",
                "Turnout LT1 () Aux input is Thrown (input on).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x50, 0x4E});
        Assert.assertEquals("Turnout sensor state test 14",
                "Turnout LT1 () Aux input is Closed (input off).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x60, 0x4E});
        Assert.assertEquals("Turnout sensor state test 15",
                "Turnout LT1 () Switch input is Thrown (input on).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB1, 0x00, 0x70, 0x4E});
        Assert.assertEquals("Turnout sensor state test 16",
                "Turnout LT1 () Switch input is Closed (input off).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testBasicImmediatePacketMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x43, 0x07, 0x4F, 0x2D, 0x5E, 0x08, 0x00, 0x16});
        Assert.assertEquals(" Immediate Packet test 01", "Send packet immediate: Locomotive 4013 set F13=Off, F14=Off, F15=Off, F16=On, F17=Off, F18=Off, F19=Off, F20=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6D, 0x7F, 0x59});
        Assert.assertEquals(" Immediate Packet test 02", "LONG_ACK: the Send IMM Packet command was accepted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x07, 0x4F, 0x2D, 0x28, 0x00, 0x00, 0x1F});
        Assert.assertEquals(" Immediate Packet test 03", "Send packet immediate: Locomotive 4013 set F9=Off, F10=Off, F11=Off, F12=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x01, 0x42, 0x23, 0x24, 0x05, 0x06, 0x1F});
        Assert.assertEquals(" Immediate Packet test 04",
                "Send packet immediate: 4 bytes, repeat count 4(68)\n\tDHI=0x01, IM1=0x42, IM2=0x23, IM3=0x24, IM4=0x05, IM5=0x06\n\tpacket: C2 23 24 05 .\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x07, 0x4F, 0x2D, 0x28, 0x00, 0x00, 0x1F});
        Assert.assertEquals(" Immediate Packet test 05",
                "Send packet immediate: Locomotive 4013 set F9=Off, F10=Off, F11=Off, F12=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x05, 0x4B, 0x6D, 0x2C, 0x00, 0x00, 0x5D});
        Assert.assertEquals(" Immediate Packet test 06",
                "Send packet immediate: Locomotive 2925 set F9=Off, F10=Off, F11=On, F12=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x43, 0x07, 0x4F, 0x2D, 0x5E, 0x08, 0x00, 0x16});
        Assert.assertEquals(" Immediate Packet test 07",
                "Send packet immediate: Locomotive 4013 set F13=Off, F14=Off, F15=Off, F16=On, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x43, 0x07, 0x4C, 0x0F, 0x5F, 0x08, 0x00, 0x36});
        Assert.assertEquals(" Immediate Packet test 08",
                "Send packet immediate: Locomotive 3215 set F21=Off, F22=Off, F23=Off, F24=On, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x13, 0x07, 0x4F, 0x2D, 0x5E, 0x08, 0x00, 0x16});
        Assert.assertEquals(" Immediate Packet test 09",
                "Unable to parse LocoNet message.\ncontents: ED 0B 7F 13 07 4F 2D 5E 08 00 16\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x03, 0x07, 0x4F, 0x2D, 0x5E, 0x08, 0x00, 0x16});
        Assert.assertEquals(" Immediate Packet test 10",
                "Unable to parse LocoNet message.\ncontents: ED 0B 7F 03 07 4F 2D 5E 08 00 16\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x23, 0x00, 0x4F, 0x2D, 0x5E, 0x08, 0x35, 0x16});
        Assert.assertEquals(" Immediate Packet test 11",
                "Send packet immediate: 2 bytes, repeat count 3(35)\n\tDHI=0x00, IM1=0x4F, IM2=0x2D, IM3=0x5E, IM4=0x08, IM5=0x35\n\tpacket: 4F 2D .\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x52, 0x07, 0x4B, 0x0F, 0x5F, 0x08, 0x52, 0x36});
        Assert.assertEquals(" Immediate Packet test 12",
                "Send packet immediate: Locomotive 2959 set F21=Off, F22=Off, F23=Off, F24=On, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x52, 0x06, 0x4B, 0x0F, 0x5F, 0x08, 0x52, 0x36});
        Assert.assertEquals(" Immediate Packet test 13",
                "Send packet immediate: 5 bytes, repeat count 2(82)\n\tDHI=0x06, IM1=0x4B, IM2=0x0F, IM3=0x5F, IM4=0x08, IM5=0x52\n\tpacket: 4B 8F DF 08 52 .\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x51, 0x03, 0x7F, 0x04, 0x3D, 0x7F, 0x00, 0x0D});
        Assert.assertEquals(" Immediate Packet test 14",
                "Playable Whistle control - Loco 16260 whistle to 0 (repeat 1 times).\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6D, 0x7F, 0x59});
        Assert.assertEquals(" Immediate Packet test 15",
                "LONG_ACK: the Send IMM Packet command was accepted.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x51, 0x03, 0x7F, 0x04, 0x3D, 0x7F, 0x7E, 0x73});
        Assert.assertEquals(" Immediate Packet test 16",
                "Playable Whistle control - Loco 16260 whistle to 126 (repeat 1 times).\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x51, 0x03, 0x7F, 0x04, 0x3D, 0x7F, 0x40, 0x73});
        Assert.assertEquals(" Immediate Packet test 17",
                "Playable Whistle control - Loco 16260 whistle to 64 (repeat 1 times).\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x51, 0x03, 0x7F, 0x04, 0x3D, 0x7F, 0x46, 0x73});
        Assert.assertEquals(" Immediate Packet test 18",
                "Playable Whistle control - Loco 16260 whistle to 70 (repeat 1 times).\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x51, 0x03, 0x7F, 0x04, 0x3D, 0x7F, 0x01, 0x73});
        Assert.assertEquals(" Immediate Packet test 19",
                "Playable Whistle control - Loco 16260 whistle to 1 (repeat 1 times).\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x51, 0x03, 0x40, 0x02, 0x3D, 0x7F, 0x01, 0x73});
        Assert.assertEquals(" Immediate Packet test 20",
                "Playable Whistle control - Loco 130 whistle to 1 (repeat 1 times).\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x51, 0x03, 0x40, 0x02, 0x3D, 0x7F, 0x3a, 0x73});
        Assert.assertEquals(" Immediate Packet test 21",
                "Playable Whistle control - Loco 130 whistle to 58 (repeat 1 times).\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x07, 0x40, 0x02, 0x5F, 0x06, 0x00, 0x3E});
        Assert.assertEquals(" Immediate Packet test 22",
                "Send packet immediate: Locomotive 130 set F21=Off, F22=On, F23=On, F24=Off, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x07, 0x40, 0x02, 0x5F, 0x02, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 23",
                "Send packet immediate: Locomotive 130 set F21=Off, F22=On, F23=Off, F24=Off, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x07, 0x40, 0x02, 0x5F, 0x07, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 24",
                "Send packet immediate: Locomotive 130 set F21=On, F22=On, F23=On, F24=Off, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x07, 0x40, 0x02, 0x5F, 0x0F, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 25",
                "Send packet immediate: Locomotive 130 set F21=On, F22=On, F23=On, F24=On, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x07, 0x40, 0x02, 0x5F, 0x1F, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 26",
                "Send packet immediate: Locomotive 130 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x07, 0x40, 0x02, 0x5F, 0x3F, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 27",
                "Send packet immediate: Locomotive 130 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=On, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0F, 0x40, 0x02, 0x5F, 0x3F, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 28",
                "Send packet immediate: Locomotive 130 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=On, F27=Off, F28=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0F, 0x40, 0x02, 0x5F, 0x7F, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 29",
                "Send packet immediate: Locomotive 130 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=On, F27=On, F28=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x05, 0x59, 0x57, 0x24, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 30",
                "Send packet immediate: Locomotive 6487 set F9=Off, F10=Off, F11=On, F12=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x05, 0x59, 0x57, 0x2C, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 31",
                "Send packet immediate: Locomotive 6487 set F9=Off, F10=Off, F11=On, F12=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x05, 0x59, 0x57, 0x5E, 0x01, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 32",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=Off, F15=Off, F16=Off, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x05, 0x59, 0x57, 0x5E, 0x03, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 33",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=On, F15=Off, F16=Off, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x05, 0x59, 0x57, 0x5E, 0x07, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 34",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=On, F15=On, F16=Off, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x05, 0x59, 0x57, 0x5E, 0x0f, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 35",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=On, F15=On, F16=On, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x05, 0x59, 0x57, 0x5E, 0x1f, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 36",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x05, 0x59, 0x57, 0x5E, 0x3f, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 37",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=On, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x05, 0x59, 0x57, 0x5E, 0x7f, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 38",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=On, F19=On, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0D, 0x59, 0x57, 0x5E, 0x7F, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 39",
                "Send packet immediate: Locomotive 6487 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=On, F19=On, F20=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x05, 0x59, 0x57, 0x21, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 40",
                "Send packet immediate: Locomotive 6487 set F9=On, F10=Off, F11=Off, F12=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x05, 0x59, 0x57, 0x22, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 41",
                "Send packet immediate: Locomotive 6487 set F9=Off, F10=On, F11=Off, F12=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x1F, 0x26, 0x00, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 42",
                "Send packet immediate: Locomotive 31 set F9=Off, F10=On, F11=On, F12=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x1F, 0x2E, 0x00, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 43",
                "Send packet immediate: Locomotive 31 set F9=Off, F10=On, F11=On, F12=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x1F, 0x20, 0x00, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 44",
                "Send packet immediate: Locomotive 31 set F9=Off, F10=Off, F11=Off, F12=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x1F, 0x21, 0x00, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 45",
                "Send packet immediate: Locomotive 31 set F9=On, F10=Off, F11=Off, F12=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5e, 0x01, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 46",
                "Send packet immediate: Locomotive 31 set F13=On, F14=Off, F15=Off, F16=Off, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5e, 0x03, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 47",
                "Send packet immediate: Locomotive 31 set F13=On, F14=On, F15=Off, F16=Off, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5e, 0x07, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 48",
                "Send packet immediate: Locomotive 31 set F13=On, F14=On, F15=On, F16=Off, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5e, 0x0F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 49",
                "Send packet immediate: Locomotive 31 set F13=On, F14=On, F15=On, F16=On, F17=Off, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5e, 0x1F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 50",
                "Send packet immediate: Locomotive 31 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=Off, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5e, 0x3F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 51",
                "Send packet immediate: Locomotive 31 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=On, F19=Off, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5e, 0x7F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 52",
                "Send packet immediate: Locomotive 31 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=On, F19=On, F20=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x06, 0x1F, 0x5e, 0x7F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 53",
                "Send packet immediate: Locomotive 31 set F13=On, F14=On, F15=On, F16=On, F17=On, F18=On, F19=On, F20=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x00, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 54",
                "Send packet immediate: Locomotive 31 set F21=Off, F22=Off, F23=Off, F24=Off, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x01, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 55",
                "Send packet immediate: Locomotive 31 set F21=On, F22=Off, F23=Off, F24=Off, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x03, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 56",
                "Send packet immediate: Locomotive 31 set F21=On, F22=On, F23=Off, F24=Off, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x07, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 57",
                "Send packet immediate: Locomotive 31 set F21=On, F22=On, F23=On, F24=Off, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x0F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 58",
                "Send packet immediate: Locomotive 31 set F21=On, F22=On, F23=On, F24=On, F25=Off, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x1F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 59",
                "Send packet immediate: Locomotive 31 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=Off, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x3F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 60",
                "Send packet immediate: Locomotive 31 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=On, F27=Off, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x02, 0x1F, 0x5f, 0x7F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 61",
                "Send packet immediate: Locomotive 31 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=On, F27=On, F28=Off.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x06, 0x1F, 0x5f, 0x7F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 62",
                "Send packet immediate: Locomotive 31 set F21=On, F22=On, F23=On, F24=On, F25=On, F26=On, F27=On, F28=On.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x06, 0x1F, 0x01, 0x7F, 0x00, 0x00, 0x7d});
        Assert.assertEquals(" Immediate Packet test 63",
                "Send packet immediate: 3 bytes, repeat count 4(52)\n\tDHI=0x06, IM1=0x1F, IM2=0x01, IM3=0x7F, IM4=0x00, IM5=0x00\n\tpacket: 1F 81 FF .\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0F, 0x40, 0x02, 0x01, 0x3F, 0x00, 0x73});
        Assert.assertEquals(" Immediate Packet test 64",
                "Send packet immediate: 4 bytes, repeat count 4(68)\n\tDHI=0x0F, IM1=0x40, IM2=0x02, IM3=0x01, IM4=0x3F, IM5=0x00\n\tpacket: C0 82 81 BF .\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x5e, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 65",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 0 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x5e, 0x01, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 66",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 1 CV: 0 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x42, 0x5e, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 67",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 128 CV: 0 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x5e, 0x00, 0x01, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 68",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 256 CV: 0 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x5e, 0x00, 0x7f, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 69",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: "+jmri.util.IntlUtilities.valueOf(32512)+" CV: 0 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x5e, 0x00, 0x00, 0x01, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 70",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 1 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x5e, 0x00, 0x00, 0x7F, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 71",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 127 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x5e, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 72",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 128 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x5e, 0x00, 0x00, 0x00, 0x70, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 73",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 384 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x5e, 0x00, 0x00, 0x7f, 0x70, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 74",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 32767 Value: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x5e, 0x00, 0x00, 0x7f, 0x70, 0x7f, 0x01, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 75",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 32767 Value: 1.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x5e, 0x00, 0x00, 0x7f, 0x70, 0x7f, 0x02, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 76",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 32767 Value: 2.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x5e, 0x00, 0x00, 0x7f, 0x72, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 77",
                "Write CV on Main Track (Ops Mode) for Uhlenbrock IB-COM / Intellibox - Address: 0 CV: 32767 Value: 128.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x5d, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 78",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 49 42 40 5D 00 00 00 70 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x6c, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 79",
                "Read CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 0\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x6d, 0x01, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 80",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 1.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x42, 0x6e, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 81",
                "Read CV in Paged Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 128.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x6F, 0x00, 0x01, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 82",
                "Write CV in Paged Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 256.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x70, 0x00, 0x7f, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 83",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 32512.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x01, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 84",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 0 Value: 1.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x72, 0x00, 0x00, 0x7F, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 85",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 86",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 0.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x01, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 87",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 1.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x02, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 88",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 2.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x04, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 89",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 4.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x08, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 90",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 8.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x10, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 91",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 16.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x20, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 92",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 32.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x72, 0x40, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 93",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 64.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x4a, 0x72, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 94",
                "Read CV in Direct Byte Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 128.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x71, 0x01, 0x00, 0x7f, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 95",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 1 Value: 255.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x71, 0x02, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 96",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 2 Value: 128.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x04, 0x00, 0x01, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 97",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 4 Value: 1.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x08, 0x00, 0x02, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 98",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 8 Value: 2.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x10, 0x00, 0x04, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 99",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 16 Value: 4.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x48, 0x71, 0x20, 0x00, 0x08, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 100",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 32 Value: 136.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x40, 0x00, 0x10, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 101",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 64 Value: 16.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x20, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 102",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 0 Value: 32.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 103",
                "Write CV in Register Mode from PT for Uhlenbrock IB-COM / Intellibox - CV: 0 Value: 64.\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x00, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 104",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 49 42 40 00 00 00 40 70 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1e, 0x01, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 105",
                "Unable to parse LocoNet message.\ncontents: ED 1E 01 49 42 40 71 00 00 40 70 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x04, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 106",
                "Unable to parse LocoNet message.\ncontents: ED 1F 04 49 42 40 71 00 00 40 70 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x51, 0x42, 0x40, 0x71, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 107",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 51 42 40 71 00 00 40 70 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x30, 0x40, 0x71, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 108",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 49 30 40 71 00 00 40 70 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x00, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 109",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 49 42 40 00 00 00 40 70 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x40, 0x6F, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 110",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 49 42 40 71 00 00 40 6F 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x40, 0x70, 0x15, 0x00, 0x00, 0x00, 0x10, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 111",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 49 42 40 71 00 00 40 70 15 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xED, 0x1f, 0x01, 0x49, 0x42, 0x40, 0x71, 0x00, 0x00, 0x40, 0x70, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(" Immediate Packet test 112",
                "Unable to parse LocoNet message.\ncontents: ED 1F 01 49 42 40 71 00 00 40 70 00 00 00 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n",
            LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        /*
*/
    }

    @Test
    public void testPlayableWhistleMessages() {
    }

    @Test
    public void testBasicTurnoutControlMessages() {
        LocoNetMessage l;
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        LnTurnoutManager lntm = new LnTurnoutManager(memo, lnis, false);

        jmri.InstanceManager.setTurnoutManager(lntm);

        l = new LocoNetMessage(new int[] {0xB0, 0x4A, 0x12, 0x17});
        Assert.assertEquals(" Turnout Control test 01", "Requesting Switch at LT331 to Thrown (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x4A, 0x02, 0x07});
        Assert.assertEquals(" Turnout Control test 02", "Requesting Switch at LT331 to Thrown (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x08, 0x14, 0x53});
        Assert.assertEquals(" Turnout Control test 03", "Requesting Switch at LT521 to Thrown (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x08, 0x04, 0x43});
        Assert.assertEquals(" Turnout Control test 04", "Requesting Switch at LT521 to Thrown (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x0B, 0x14, 0x50});
        Assert.assertEquals(" Turnout Control test 05", "Requesting Switch at LT524 to Thrown (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x0B, 0x04, 0x40});
        Assert.assertEquals(" Turnout Control test 06", "Requesting Switch at LT524 to Thrown (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x65, 0x10, 0x3a});
        Assert.assertEquals(" Turnout Control test 07", "Requesting Switch at LT102 to Thrown (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x65, 0x00, 0x2a});
        Assert.assertEquals(" Turnout Control test 08", "Requesting Switch at LT102 to Thrown (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x26, 0x31, 0x58});
        Assert.assertEquals(" Turnout Control test 09", "Requesting Switch at LT167 to Closed (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x26, 0x21, 0x48});
        Assert.assertEquals(" Turnout Control test 10", "Requesting Switch at LT167 to Closed (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x27, 0x11, 0x79});
        Assert.assertEquals(" Turnout Control test 11", "Requesting Switch at LT168 to Thrown (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x27, 0x01, 0x69});
        Assert.assertEquals(" Turnout Control test 12", "Requesting Switch at LT168 to Thrown (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x28, 0x31, 0x56});
        Assert.assertEquals(" Turnout Control test 13", "Requesting Switch at LT169 to Closed (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x28, 0x21, 0x46});
        Assert.assertEquals(" Turnout Control test 14", "Requesting Switch at LT169 to Closed (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x29, 0x31, 0x57});
        Assert.assertEquals(" Turnout Control test 15", "Requesting Switch at LT170 to Closed (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        lntm.newTurnout("LT170", "UserNameForLT170ManuallyCreated");

        lntm.newTurnout("LT171", "An Friendly User Name");

        l = new LocoNetMessage(new int[] {0xB0, 0x29, 0x21, 0x47});
        Assert.assertEquals(" Turnout Control test 16", "Requesting Switch at LT170 (UserNameForLT170ManuallyCreated) to Closed (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x2a, 0x31, 0x54});
        Assert.assertEquals(" Turnout Control test 17", "Requesting Switch at LT171 (An Friendly User Name) to Closed (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x2a, 0x21, 0x44});
        Assert.assertEquals(" Turnout Control test 18", "Requesting Switch at LT171 (An Friendly User Name) to Closed (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        lntm.newTurnout("LT172", "Another of those Friendly User Names");
        l = new LocoNetMessage(new int[] {0xB0, 0x2B, 0x31, 0x55});
        Assert.assertEquals(" Turnout Control test 19", "Requesting Switch at LT172 (Another of those Friendly User Names) to Closed (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x2B, 0x21, 0x45});
        Assert.assertEquals(" Turnout Control test 20", "Requesting Switch at LT172 (Another of those Friendly User Names) to Closed (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        lntm.newTurnout("LT173","");
        l = new LocoNetMessage(new int[] {0xB0, 0x2C, 0x31, 0x55});
        Assert.assertEquals(" Turnout Control test 21", "Requesting Switch at LT173 to Closed (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x2C, 0x21, 0x45});
        Assert.assertEquals(" Turnout Control test 22", "Requesting Switch at LT173 to Closed (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x2D, 0x31, 0x55});
        Assert.assertEquals(" Turnout Control test 23", "Requesting Switch at LT174 to Closed (Output On).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x2D, 0x21, 0x45});
        Assert.assertEquals(" Turnout Control test 24", "Requesting Switch at LT174 to Closed (Output Off).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x00, 0x40, 0x0F});
        Assert.assertEquals(" Turnout Control test 25",
                "Unable to parse LocoNet message.\ncontents: B0 00 40 0F\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x78, 0x27, 0x0F});
        Assert.assertEquals("Interrogate 1",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 1/0/0; addresses...\n" +
                "\t33-40, 97-104, 161-168, 225-232, 289-296, 353-360, 417-424, 481-488,\n" +
                "\t545-552, 609-616, 673-680, 737-744, 801-808, 865-872, 929-936, 993-1000,\n" +
                "\t1057-1064, 1121-1128, 1185-1192, 1249-1256, 1313-1320, 1377-1384, 1441-1448, 1505-1512,\n" +
                "\t1569-1576, 1633-1640, 1697-1704, 1761-1768, 1825-1832, 1889-1896, 1953-1960, 2017-2024.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x79, 0x27, 0x0F});
        Assert.assertEquals("Interrogate 2",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 1/0/1; addresses...\n" +
                "\t41-48, 105-112, 169-176, 233-240, 297-304, 361-368, 425-432, 489-496,\n" +
                "\t553-560, 617-624, 681-688, 745-752, 809-816, 873-880, 937-944, 1001-1008,\n" +
                "\t1065-1072, 1129-1136, 1193-1200, 1257-1264, 1321-1328, 1385-1392, 1449-1456, 1513-1520,\n" +
                "\t1577-1584, 1641-1648, 1705-1712, 1769-1776, 1833-1840, 1897-1904, 1961-1968, 2025-2032.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x7a, 0x27, 0x0F});
        Assert.assertEquals("Interrogate 3",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 1/1/0; addresses...\n" +
                "\t49-56, 113-120, 177-184, 241-248, 305-312, 369-376, 433-440, 497-504,\n" +
                "\t561-568, 625-632, 689-696, 753-760, 817-824, 881-888, 945-952, 1009-1016,\n" +
                "\t1073-1080, 1137-1144, 1201-1208, 1265-1272, 1329-1336, 1393-1400, 1457-1464, 1521-1528,\n" +
                "\t1585-1592, 1649-1656, 1713-1720, 1777-1784, 1841-1848, 1905-1912, 1969-1976, 2033-2040.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x7b, 0x27, 0x0F});
        Assert.assertEquals("Interrogate 4",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 1/1/1; addresses...\n" +
                "\t57-64, 121-128, 185-192, 249-256, 313-320, 377-384, 441-448, 505-512,\n" +
                "\t569-576, 633-640, 697-704, 761-768, 825-832, 889-896, 953-960, 1017-1024,\n" +
                "\t1081-1088, 1145-1152, 1209-1216, 1273-1280, 1337-1344, 1401-1408, 1465-1472, 1529-1536,\n" +
                "\t1593-1600, 1657-1664, 1721-1728, 1785-1792, 1849-1856, 1913-1920, 1977-1984, 2041-2048.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x78, 0x07, 0x0F});
        Assert.assertEquals("Interrogate 5",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 0/0/0; addresses...\n" +
                "\t1-8, 65-72, 129-136, 193-200, 257-264, 321-328, 385-392, 449-456,\n" +
                "\t513-520, 577-584, 641-648, 705-712, 769-776, 833-840, 897-904, 961-968,\n" +
                "\t1025-1032, 1089-1096, 1153-1160, 1217-1224, 1281-1288, 1345-1352, 1409-1416, 1473-1480,\n" +
                "\t1537-1544, 1601-1608, 1665-1672, 1729-1736, 1793-1800, 1857-1864, 1921-1928, 1985-1992.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x79, 0x07, 0x0F});
        Assert.assertEquals("Interrogate 6",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 0/0/1; addresses...\n"+
                "\t9-16, 73-80, 137-144, 201-208, 265-272, 329-336, 393-400, 457-464,\n" +
                "\t521-528, 585-592, 649-656, 713-720, 777-784, 841-848, 905-912, 969-976,\n" +
                "\t1033-1040, 1097-1104, 1161-1168, 1225-1232, 1289-1296, 1353-1360, 1417-1424, 1481-1488,\n" +
                "\t1545-1552, 1609-1616, 1673-1680, 1737-1744, 1801-1808, 1865-1872, 1929-1936, 1993-2000.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x7a, 0x07, 0x0F});
        Assert.assertEquals("Interrogate 17",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 0/1/0; addresses...\n" +
                "\t17-24, 81-88, 145-152, 209-216, 273-280, 337-344, 401-408, 465-472,\n" +
                "\t529-536, 593-600, 657-664, 721-728, 785-792, 849-856, 913-920, 977-984,\n" +
                "\t1041-1048, 1105-1112, 1169-1176, 1233-1240, 1297-1304, 1361-1368, 1425-1432, 1489-1496,\n" +
                "\t1553-1560, 1617-1624, 1681-1688, 1745-1752, 1809-1816, 1873-1880, 1937-1944, 2001-2008.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB0, 0x7b, 0x07, 0x0F});
        Assert.assertEquals("Interrogate 8",
                "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of 0/1/1; addresses...\n" +
                "\t25-32, 89-96, 153-160, 217-224, 281-288, 345-352, 409-416, 473-480,\n" +
                "\t537-544, 601-608, 665-672, 729-736, 793-800, 857-864, 921-928, 985-992,\n" +
                "\t1049-1056, 1113-1120, 1177-1184, 1241-1248, 1305-1312, 1369-1376, 1433-1440, 1497-1504,\n" +
                "\t1561-1568, 1625-1632, 1689-1696, 1753-1760, 1817-1824, 1881-1888, 1945-1952, 2009-2016.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


    }

    @Test
    public void testTetherlessQueryAndReplies() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xDF, 0x00, 0x00, 0x00, 0x00, 0x20});
        Assert.assertEquals(" Tetherless Query/Reply test 01",
                "Query Tetherless Receivers.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x03, 0x10, 0x00, 0x45, 0x4E, 0x4C, 0x32, 0x00, 0x30, 0x31, 0x31, 0x20, 0x00, 0x00, 0x00, 0x1A, 0x00, 0x62});
        Assert.assertEquals(" Tetherless Query/Reply test 02",
                "Reported Duplex Group Name=\"ENL2011 \", Password=00000000, Channel=26, ID=0.\n" ,
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x12, 0x00, 0x0F, 0x20, 0x15});
        Assert.assertEquals(" Tetherless Query/Reply test 03", "UR92 Responding with LocoNet ID 7, duplex enabled.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x1F, 0x00, 0x06, 0x00, 0x30});
        Assert.assertEquals(" Tetherless Query/Reply test 04", "UR91 Responding with LocoNet ID 6.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x1F, 0x00, 0x00, 0x00, 0x30});
        Assert.assertEquals(" Tetherless Query/Reply test 05", "UR91 Responding with LocoNet ID 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x02, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04});
        Assert.assertEquals(" Tetherless Query/Reply test 06", "Query Duplex Channel.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x02, 0x10, 0x00, 0x1A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06});
        Assert.assertEquals(" Tetherless Query/Reply test 07", "Reported Duplex Channel is 26.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x07, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01});
        Assert.assertEquals(" Tetherless Query/Reply test 08", "Query Duplex Password.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x07, 0x10, 0x00, 0x30, 0x30, 0x30, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x19});
        Assert.assertEquals(" Tetherless Query/Reply test 09", "Reported Duplex Password is 0000.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x40, 0x1F, 0x05, 0x00, 0x00});
        Assert.assertEquals(" Tetherless Query/Reply test 10",
                "Set LocoNet ID to 5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x30, 0x01, 0x05, 0x00, 0x00});
        Assert.assertEquals(" Tetherless Query/Reply test 11",
                "Unable to parse LocoNet message.\ncontents: DF 30 01 05 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x00, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 12",
                "UR90 Responding with LocoNet ID 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x01, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 13",
                "UR90 Responding with LocoNet ID 1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x02, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 14",
                "UR90 Responding with LocoNet ID 2.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x03, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 15",
                "UR90 Responding with LocoNet ID 3.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x04, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 16",
                "UR90 Responding with LocoNet ID 4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x05, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 17",
                "UR90 Responding with LocoNet ID 5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x06, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 18",
                "UR90 Responding with LocoNet ID 6.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD7, 0x17, 0x00, 0x07, 0x00, 0x3F});
        Assert.assertEquals(" Tetherless Query/Reply test 19",
                "UR90 Responding with LocoNet ID 7.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x40, 0x00, 0x00, 0x00, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 40 00 00 00 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x00, 0x00, 0x00, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 00 00 00 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x01, 0x00, 0x00, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 01 00 00 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x08, 0x00, 0x00, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 08 00 00 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x29, 0x00, 0x00, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 29 00 00 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x00, 0x01, 0x00, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 00 01 00 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x00, 0x40, 0x00, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 00 40 00 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x00, 0x00, 0x08, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 00 00 08 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xDF, 0x01, 0x00, 0x00, 0x7f, 0x60});
        Assert.assertEquals(" Tetherless Query/Reply test 20",
                "Unable to parse LocoNet message.\ncontents: DF 01 00 00 7F 60\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testBasicPM42Events() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x30, 0x19, 0x01});
        Assert.assertEquals(" PM42 Events test 01", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Shorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Shorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x30, 0x1B, 0x03});
        Assert.assertEquals(" PM42 Events test 02", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Shorted.\n\tSub-District 2 - Circuit-Breaker mode - Shorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Shorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x30, 0x1F, 0x07});
        Assert.assertEquals(" PM42 Events test 03", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Shorted.\n\tSub-District 2 - Circuit-Breaker mode - Shorted.\n\tSub-District 3 - Circuit-Breaker mode - Shorted.\n\tSub-District 4 - Circuit-Breaker mode - Shorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x64, 0x30, 0x18, 0x01});
        Assert.assertEquals(" PM42 Events test 04", "PM4x (Board ID 101) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Shorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x64, 0x30, 0x1A, 0x03});
        Assert.assertEquals(" PM42 Events test 05", "PM4x (Board ID 101) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Shorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Shorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x30, 0x16, 0x0E});
        Assert.assertEquals(" PM42 Events test 06", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Shorted.\n\tSub-District 3 - Circuit-Breaker mode - Shorted.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x30, 0x14, 0x0C});
        Assert.assertEquals(" PM42 Events test 07", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Shorted.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x64, 0x30, 0x10, 0x09});
        Assert.assertEquals(" PM42 Events test 08", "PM4x (Board ID 101) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x30, 0x10, 0x08});
        Assert.assertEquals(" PM42 Events test 09", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x01, 0x38, 0x10, 0x64});
        Assert.assertEquals(" PM42 Events test 09",
                "PM4x (Board ID 2) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Auto-Reversing mode - Normal.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x01, 0x38, 0x1F, 0x6B});
        Assert.assertEquals(" PM42 Events test 09",
                "PM4x (Board ID 2) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Shorted.\n\tSub-District 2 - Circuit-Breaker mode - Shorted.\n\tSub-District 3 - Circuit-Breaker mode - Shorted.\n\tSub-District 4 - Auto-Reversing mode - Reversed.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x01, 0x38, 0x18, 0x6C});
        Assert.assertEquals(" PM42 Events test 09",
                "PM4x (Board ID 2) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Auto-Reversing mode - Reversed.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));








        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x11, 0x19, 0x01});
        Assert.assertEquals(" PM42 Events test 01", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Auto-Reversing mode - Reversed.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Shorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x11, 0x18, 0x01});
        Assert.assertEquals(" PM42 Events test 01", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Auto-Reversing mode - Normal.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Shorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x12, 0x12, 0x03});
        Assert.assertEquals(" PM42 Events test 02", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Auto-Reversing mode - Reversed.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x12, 0x10, 0x07});
        Assert.assertEquals(" PM42 Events test 03", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Auto-Reversing mode - Normal.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x64, 0x14, 0x14, 0x01});
        Assert.assertEquals(" PM42 Events test 04", "PM4x (Board ID 101) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Auto-Reversing mode - Reversed.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x64, 0x14, 0x10, 0x03});
        Assert.assertEquals(" PM42 Events test 05", "PM4x (Board ID 101) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Auto-Reversing mode - Normal.\n\tSub-District 4 - Circuit-Breaker mode - Unshorted.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x18, 0x18, 0x0E});
        Assert.assertEquals(" PM42 Events test 06", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n"
                + "\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Auto-Reversing mode - Reversed.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x65, 0x18, 0x10, 0x0C});
        Assert.assertEquals(" PM42 Events test 07", "PM4x (Board ID 102) Power Status Report\n\tSub-District 1 - Circuit-Breaker mode - Unshorted.\n"
                + "\tSub-District 2 - Circuit-Breaker mode - Unshorted.\n\tSub-District 3 - Circuit-Breaker mode - Unshorted.\n\tSub-District 4 - Auto-Reversing mode - Normal.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


    }

    @Test
    public void testPR3ModeMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xD3, 0x10, 0x02, 0x00, 0x00, 0x3D});
        Assert.assertEquals("PR3 mode test 1",
                "Set PR3 to decoder programming track mode (i.e. no command station present).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD3, 0x10, 0x00, 0x00, 0x00, 0x3D});
        Assert.assertEquals("PR3 mode test 2",
                "Set PR3 to MS100 mode without PR3 termination of LocoNet (i.e. use PR3 with command station present).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD3, 0x10, 0x03, 0x00, 0x00, 0x3D});
        Assert.assertEquals("PR3 mode test 3",
                "Set PR3 to MS100 mode with PR3 termination of LocoNet (i.e. use PR3 without command station present).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD3, 0x09, 0x03, 0x00, 0x00, 0x3D});
        Assert.assertEquals("PR3 mode test 4",
                "Unable to parse LocoNet message.\ncontents: D3 09 03 00 00 3D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD3, 0x10, 0x07, 0x00, 0x00, 0x3D});
        Assert.assertEquals("PR3 mode test 5",
                "Unable to parse LocoNet message.\ncontents: D3 10 07 00 00 3D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD3, 0x10, 0x03, 0x01, 0x00, 0x3D});
        Assert.assertEquals("PR3 mode test 6",
                "Unable to parse LocoNet message.\ncontents: D3 10 03 01 00 3D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD3, 0x10, 0x03, 0x00, 0x10, 0x3D});
        Assert.assertEquals("PR3 mode test 7",
                "Unable to parse LocoNet message.\ncontents: D3 10 03 00 10 3D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD3, 0x10, 0x01, 0x00, 0x00, 0x3D});
        Assert.assertEquals("PR3 mode test 8",
                "Unable to parse LocoNet message.\ncontents: D3 10 01 00 00 3D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testTrackPowerMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0x82, 0x7d});
        Assert.assertEquals("Track Power test 1",
                "Set Global (Track) Power to 'OFF'.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x81, 0x7e});
        Assert.assertEquals("Track Power test 2",
                "Master is busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x83, 0x7c});
        Assert.assertEquals("Track Power test 3",
                "Set Global (Track) Power to 'ON'.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x85, 0x7c});
        Assert.assertEquals("Track Power test 4",
                "Set Global (Track) Power to 'Force Idle, Broadcast Emergency STOP'.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testPM42OpSwMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x00, 0x70, 0x00, 0x3D});
        Assert.assertEquals("PM42 OpSws test 1",
                "PM4x 1 Query OpSw1 - Also acts as device query for some device types.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x00, 0x70, 0x02, 0x3D});
        Assert.assertEquals("PM42 OpSws test 1",
                "PM4x 1 Query OpSw2.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x00, 0x01, 0x15, 0x59});
        Assert.assertEquals("PM42 OpSws test 2",
                "Device type report - BDL16x Board ID 1 Version 21 is present.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x01, 0x00, 0x00, 0x4C});
        Assert.assertEquals("PM42 OpSws test 3",
                "Device type report - PM4x Board ID 2 Version (unknown) is present.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x02, 0x02, 0x01, 0x4C});
        Assert.assertEquals("PM42 OpSws test 4",
                "Device type report - SE8C Board ID 3 Version 1 is present.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x40, 0x03, 0x28, 0x4C});
        Assert.assertEquals("PM42 OpSws test 5",
                "Device type report - DS64 Board ID 65 Version 40 is present.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x00, 0x70, 0x28, 0x4C});
        Assert.assertEquals("PM42 OpSws test 6",
                "PM4x 1 Query OpSw21.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x72, 0x00, 0x70, 0x28, 0x4C});
        Assert.assertEquals("PM42 OpSws test 7",
                "PM4x 1 Write OpSw21 value=0 (Thrown).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x72, 0x00, 0x70, 0x29, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "PM4x 1 Write OpSw21 value=1 (Closed).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x72, 0x00, 0x71, 0x19, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "BDL16x 1 Write OpSw13 value=1 (Closed).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x07, 0x71, 0x09, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "BDL16x 8 Query OpSw5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x72, 0x02, 0x72, 0x19, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "SE8C 3 Write OpSw13 value=1 (Closed).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x01, 0x72, 0x09, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "SE8C 2 Query OpSw5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x72, 0x02, 0x73, 0x27, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "DS64 3 Write OpSw20 value=1 (Closed).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x05, 0x73, 0x79, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "DS64 6 Query OpSw61.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x05, 0x74, 0x79, 0x4C});
        Assert.assertEquals("PM42 OpSws test 8",
                "Unable to parse LocoNet message.\ncontents: D0 62 05 74 79 4C\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testDS64OpSwMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x00, 0x73, 0x58, 0x66});
        Assert.assertEquals("DS64 OpSw test 1",
                "DS64 1 Query OpSw45.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x50, 0x30, 0x2B});
        Assert.assertEquals("DS64 OpSw test 2",
                "LONG_ACK: OpSwitch report - opSwitch is 1 (Closed).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x00, 0x73, 0x40, 0x7E});
        Assert.assertEquals("DS64 OpSw test 3",
                "DS64 1 Query OpSw33.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x50, 0x10, 0x0B});
        Assert.assertEquals("DS64 OpSw test 4",
                "LONG_ACK: OpSwitch report - opSwitch is 0 (Thrown).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x00, 0x73, 0x54, 0x6A});
        Assert.assertEquals("DS64 OpSw test 5",
                "DS64 1 Query OpSw43.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x01, 0x73, 0x56, 0x68});
        Assert.assertEquals("DS64 OpSw test 6",
                "DS64 2 Query OpSw44.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x04, 0x73, 0x58, 0x66});
        Assert.assertEquals("DS64 OpSw test 7",
                "DS64 5 Query OpSw45.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x62, 0x7f, 0x73, 0x5A, 0x64});
        Assert.assertEquals("DS64 OpSw test 8",
                "DS64 128 Query OpSw46.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x72, 0x0B, 0x73, 0x01, 0x24});
        Assert.assertEquals("DS64 OpSw test 9",
                "DS64 12 Write OpSw1 value=1 (Closed).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x50, 0x7F, 0x64});
        Assert.assertEquals("DS64 OpSw test 10",
                "LONG_ACK: OpSwitch operation accepted.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD0, 0x72, 0x0B, 0x73, 0x30, 0x15});
        Assert.assertEquals("DS64 OpSw test 11",
                "DS64 12 Write OpSw25 value=0 (Thrown).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testCmdStationCfgSlot() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x10, 0x40, 0x00, 0x08, 0x0D, 0x03, 0x08, 0x00, 0x78, 0x0F, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 01",
                "Report of current Command Station OpSw values:\n\tOpSw1=Thrown, OpSw2=Thrown, OpSw3=Thrown, OpSw4=Thrown, OpSw5=Closed, OpSw6=Thrown, OpSw7=Thrown, OpSw8=Thrown,\n" +
                "\tOpSw9=Thrown, OpSw10=Thrown, OpSw11=Thrown, OpSw12=Thrown, OpSw13=Thrown, OpSw14=Thrown, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Thrown, OpSw18=Thrown, OpSw19=Thrown, OpSw20=Thrown, OpSw21=Thrown, OpSw22=Thrown, OpSw23=Thrown, OpSw24=Thrown,\n" +
                "\tOpSw25=Thrown, OpSw26=Thrown, OpSw27=Thrown, OpSw28=Closed, OpSw29=Thrown, OpSw30=Thrown, OpSw31=Thrown, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Thrown, OpSw36=Thrown, OpSw37=Thrown, OpSw38=Thrown, OpSw39=Thrown, OpSw40=Thrown,\n" +
                "\tOpSw41=Thrown, OpSw42=Thrown, OpSw43=Thrown, OpSw44=Closed, OpSw45=Thrown, OpSw46=Thrown, OpSw47=Thrown, OpSw48=Thrown,\n" +
                "\tOpSw49=Thrown, OpSw50=Thrown, OpSw51=Thrown, OpSw52=Thrown, OpSw53=Thrown, OpSw54=Thrown, OpSw55=Thrown, OpSw56=Thrown,\n" +
                "\tOpSw57=Thrown, OpSw58=Thrown, OpSw59=Thrown, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x00, 0x00, 0x00, 0x00, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 02",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Thrown, OpSw2=Thrown, OpSw3=Thrown, OpSw4=Thrown, OpSw5=Thrown, OpSw6=Thrown, OpSw7=Thrown, OpSw8=Thrown,\n" +
                "\tOpSw9=Thrown, OpSw10=Thrown, OpSw11=Thrown, OpSw12=Thrown, OpSw13=Thrown, OpSw14=Thrown, OpSw15=Thrown, OpSw16=Thrown,\n" +
                "\tOpSw17=Thrown, OpSw18=Thrown, OpSw19=Thrown, OpSw20=Thrown, OpSw21=Thrown, OpSw22=Thrown, OpSw23=Thrown, OpSw24=Thrown,\n" +
                "\tOpSw25=Thrown, OpSw26=Thrown, OpSw27=Thrown, OpSw28=Thrown, OpSw29=Thrown, OpSw30=Thrown, OpSw31=Thrown, OpSw32=Thrown,\n" +
                "\tOpSw33=Thrown, OpSw34=Thrown, OpSw35=Thrown, OpSw36=Thrown, OpSw37=Thrown, OpSw38=Thrown, OpSw39=Thrown, OpSw40=Thrown,\n" +
                "\tOpSw41=Thrown, OpSw42=Thrown, OpSw43=Thrown, OpSw44=Thrown, OpSw45=Thrown, OpSw46=Thrown, OpSw47=Thrown, OpSw48=Thrown,\n" +
                "\tOpSw49=Thrown, OpSw50=Thrown, OpSw51=Thrown, OpSw52=Thrown, OpSw53=Thrown, OpSw54=Thrown, OpSw55=Thrown, OpSw56=Thrown,\n" +
                "\tOpSw57=Thrown, OpSw58=Thrown, OpSw59=Thrown, OpSw60=Thrown, OpSw61=Thrown, OpSw62=Thrown, OpSw63=Thrown, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 03",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7E, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 04",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Thrown, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7D, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 05",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Thrown, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7B, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 06",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Thrown, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x77, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 07",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Thrown, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x6F, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 08",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Thrown, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x5F, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 09",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Thrown, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x3F, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 09",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Thrown, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x7F, 0x3F, 0x7F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 10",
                "Write Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Thrown, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7E, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 11",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Thrown, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7D, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 12",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Thrown, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7B, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 13",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Thrown, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x77, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 14",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Thrown, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x6F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 15",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Thrown, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x5F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 16",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Thrown, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x3F, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 17",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Thrown, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7E, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 18",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Thrown, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7f, 0x7D, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 19",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Thrown, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7B, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 20",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Thrown, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x77, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 21",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Thrown, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x6F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 22",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Thrown, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x5F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 23",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Thrown, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x3F, 0x7F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 24",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Thrown, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7E, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 25",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Thrown, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7f, 0x7F, 0x7D, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 26",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Thrown, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7B, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 27",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Thrown, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x77, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 28",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Thrown, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x6F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 29",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Thrown, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x5F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 30",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Thrown, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x3F, 0x7f, 0x7F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 31",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Thrown, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7E, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 32",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Thrown, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x07, 0x7D, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 33",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Thrown, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7B, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 34",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Thrown, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x77, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 35",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Thrown, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x6F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 36",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Thrown, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x5F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 37",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Thrown, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x3F, 0x7F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 38",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Thrown, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7E, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 39",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Thrown, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x07, 0x7F, 0x7D, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 40",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Thrown, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7B, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 41",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Thrown, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x77, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 42",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Thrown, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x6F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 43",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Thrown, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x5F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 44",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Thrown, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x3F, 0x7F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 45",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Thrown, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7E, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 46",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Thrown, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7D, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 47",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Thrown, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7B, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 48",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Thrown, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x77, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 49",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Thrown, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x6F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 50",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Thrown, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x5F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 51",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Thrown, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x3F, 0x7F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 52",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Thrown, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7F, 0x7E, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 53",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Thrown, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7f, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7F, 0x7D, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 54",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Thrown, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7F, 0x7B, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 55",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Thrown, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7F, 0x77, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 56",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Thrown, OpSw61=Closed, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7F, 0x6F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 57",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Thrown, OpSw62=Closed, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7F, 0x5F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 58",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Thrown, OpSw63=Closed, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x07, 0x7F, 0x7F, 0x7F, 0x3F, 0x00, 0x40});
        Assert.assertEquals("Cmd Stn Cfg Slot test 59",
                "Report of current Command Station OpSw values:\n" +
                "\tOpSw1=Closed, OpSw2=Closed, OpSw3=Closed, OpSw4=Closed, OpSw5=Closed, OpSw6=Closed, OpSw7=Closed, OpSw8=Thrown,\n" +
                "\tOpSw9=Closed, OpSw10=Closed, OpSw11=Closed, OpSw12=Closed, OpSw13=Closed, OpSw14=Closed, OpSw15=Closed, OpSw16=Thrown,\n" +
                "\tOpSw17=Closed, OpSw18=Closed, OpSw19=Closed, OpSw20=Closed, OpSw21=Closed, OpSw22=Closed, OpSw23=Closed, OpSw24=Thrown,\n" +
                "\tOpSw25=Closed, OpSw26=Closed, OpSw27=Closed, OpSw28=Closed, OpSw29=Closed, OpSw30=Closed, OpSw31=Closed, OpSw32=Thrown,\n" +
                "\tOpSw33=Closed, OpSw34=Closed, OpSw35=Closed, OpSw36=Closed, OpSw37=Closed, OpSw38=Closed, OpSw39=Closed, OpSw40=Thrown,\n" +
                "\tOpSw41=Closed, OpSw42=Closed, OpSw43=Closed, OpSw44=Closed, OpSw45=Closed, OpSw46=Closed, OpSw47=Closed, OpSw48=Thrown,\n" +
                "\tOpSw49=Closed, OpSw50=Closed, OpSw51=Closed, OpSw52=Closed, OpSw53=Closed, OpSw54=Closed, OpSw55=Closed, OpSw56=Thrown,\n" +
                "\tOpSw57=Closed, OpSw58=Closed, OpSw59=Closed, OpSw60=Closed, OpSw61=Closed, OpSw62=Closed, OpSw63=Thrown, OpSw64=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
    }

    @Test
    public void testDuplexRadioScan() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x08, 0x00, 0x1A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C});
        Assert.assertEquals("Channel Scan test 1",
                "Query Duplex Channel 26 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x1A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x14});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 26 noise/activity level is 0/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x08, 0x00, 0x0B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1D});
        Assert.assertEquals("Channel Scan test 3",
                "Query Duplex Channel 11 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05});
        Assert.assertEquals("Channel Scan test 4",
                "Reported Duplex Channel 11 noise/activity level is 0/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x08, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1A});
        Assert.assertEquals("Channel Scan test 5",
                "Query Duplex Channel 12 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0C, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0E});
        Assert.assertEquals("Channel Scan test 6",
                "Reported Duplex Channel 12 noise/activity level is 10/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0C, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 12 noise/activity level is 1/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x08, 0x00, 0x0D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B});
        Assert.assertEquals("Channel Scan test 2",
                "Query Duplex Channel 13 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0D, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 13 noise/activity level is 5/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 13 noise/activity level is 0/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x08, 0x00, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x18});
        Assert.assertEquals("Channel Scan test 2",
                "Query Duplex Channel 14 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0E, 0x26, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x26});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 14 noise/activity level is 38/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0E, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0E});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 14 noise/activity level is 14/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x08, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x19});
        Assert.assertEquals("Channel Scan test 2",
                "Query Duplex Channel 15 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0F, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 15 noise/activity level is 23/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 15 noise/activity level is 8/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x14, 0x10, 0x10, 0x00, 0x0F, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09});
        Assert.assertEquals("Channel Scan test 2",
                "Reported Duplex Channel 15 noise/activity level is 8/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void textOpcPeerXfer() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x05, 0x00, 0x00, 0x7f});
        Assert.assertEquals("PeerXfer 1",
                "Unable to parse LocoNet message.\ncontents: E5 05 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testThrottleMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 1, 2, 0, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 1",
                "Throttle Semaphore Symbol Control: "
                        + "Loco 130, Semaphore body unlit, "
                        + "Vertical arm unlit, Diagonal arm unlit, Horizontal arm unlit; "
                        +"Any lit arms are non-blinking.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 1, 2, 0x10, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 2",
                "Throttle Semaphore Symbol Control: "
                        + "Loco 130, Semaphore body lit, "
                        + "Vertical arm unlit, Diagonal arm unlit, Horizontal arm unlit; "
                        +"Any lit arms are non-blinking.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 3, 4, 0x01, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 3",
                "Throttle Semaphore Symbol Control: "
                        + "Loco 388, Semaphore body unlit, "
                        + "Vertical arm unlit, Diagonal arm unlit, Horizontal arm unlit; "
                        +"Any lit arms are blinking.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 3, 4, 0x02, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 3",
                "Throttle Semaphore Symbol Control: "
                        + "Loco 388, Semaphore body unlit, "
                        + "Vertical arm unlit, Diagonal arm unlit, Horizontal arm lit; "
                        +"Any lit arms are non-blinking.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 3, 4, 0x04, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 3",
                "Throttle Semaphore Symbol Control: "
                        + "Loco 388, Semaphore body unlit, "
                        + "Vertical arm unlit, Diagonal arm lit, Horizontal arm unlit; "
                        +"Any lit arms are non-blinking.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 3, 4, 0x08, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 3",
                "Throttle Semaphore Symbol Control: "
                        + "Loco 388, Semaphore body unlit, "
                        + "Vertical arm lit, Diagonal arm unlit, Horizontal arm unlit; "
                        +"Any lit arms are non-blinking.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] { 0xe5, 0x10, 0x7f, 0x00, 0x00, 0, 0x31, 0x41, 0x51, 0x61, 0, 0x32, 0x33, 0x34, 0x35, 0}  );
        Assert.assertEquals("Throttle message 3",
                "Send Throttle Text Message to all throttles with message 1AQa2345.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] { 0xe5, 0x10, 0x7f, 0x00, 0x01, 0, 0x21, 0x22, 0x23, 0x24, 0, 0x25, 0x26, 0x27, 0x28, 0}  );
        Assert.assertEquals("Throttle message 3",
                "Send Throttle Text Message to Throttle 128 with message !\"#$%&'(.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7e, 0x00, 0x00, 0x0, 1, 2, 0, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 1",
                "Unable to parse LocoNet message.\ncontents: E5 10 7E 00 00 00 01 02 00 00 70 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x05, 0x00, 0x0, 0x30, 0x30, 0x30, 0x30, 0x70, 0x30, 0x30, 0x30, 0x30, 0x00});
        Assert.assertEquals("Throttle message 1",
                "Send Throttle Text Message to Throttle 5 (short) with message 00000000.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x22, 0x0, 0x31, 0x32, 0x30, 0x30, 0x70, 0x30, 0x30, 0x30, 0x30, 0x00});
        Assert.assertEquals("Throttle message 1",
                "Send Throttle Text Message to Throttle 4352 with message 12000000.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x10, 1, 2, 0, 0, 0x70, 0, 0, 0, 0, 0x00});
        Assert.assertEquals("Throttle message 1",
                "Throttle Semaphore Symbol Control: Loco 130, Semaphore body unlit, Vertical arm unlit, Diagonal arm unlit, Horizontal arm unlit; Any lit arms are non-blinking.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 0x41, 0x42, 0x41, 0x41, 0x60, 0x50, 0x50, 0x50, 0x50, 0x00});
        Assert.assertEquals("Throttle message 1",
                "Send Throttle Text Message to all throttles with message ABAAPPPP.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7f, 0x00, 0x00, 0x0, 0x41, 0x42, 0x41, 0x41, 0x20, 0x50, 0x50, 0x50, 0x50, 0x00});
        Assert.assertEquals("Throttle message 1",
                "Send Throttle Text Message to all throttles with message ABAAPPPP.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


    }

    @Test
    public void testOpcPeerXfer10() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x73, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=73 (unknown), Throttle ID=0x00 0x00 (0), SLA=0x00, SLB=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x40, 0x01, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=40 (OK), Throttle ID=0x02 0x01 (257), SLA=0x00, SLB=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x7F, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=7F (no key, immed, ignored), Throttle ID=0x00 0x01 (1), SLA=0x00, SLB=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x43, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=43 (+ key during msg), Throttle ID=0x02 0x00 (256), SLA=0x00, SLB=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x42, 0x01, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=42 (- key during msg), Throttle ID=0x00 0x01 (1), SLA=0x30, SLB=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x41, 0x00, 0x02, 0x00, 0x00, 0x00, 0x40, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=41 (R/S key during msg, aborts), Throttle ID=0x02 0x00 (256), SLA=0x00, SLB=0x40.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x4e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=4E (Throttle response to Semaphore Display Command), Throttle ID=0x00 0x00 (0), SLA=0x00, SLB=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x0a, 0x45, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x7f}  );
        Assert.assertEquals(" Slot test 1",
                "Throttle status TCNTRL=45 (unknown), Throttle ID=0x00 0x00 (0), SLA=0x00, SLB=0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testBasicSlotAccessMessages() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xBB, 0x40, 0x00, 0x04}  );
        Assert.assertEquals(" Slot test 1",
                "Request data/status for slot 64.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x40, 0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x51}  );
        Assert.assertEquals(" Slot test 2",
                "Report of slot 64 information:\n"
                +"\tLoco 0 (short) is Not Consisted, Free, operating in 28 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                + "\tMaster supports LocoNet 1.1; Track Status: Off/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7B, 0x00, 0x3F} );
        Assert.assertEquals(" Slot test 3",
                "Request Fast Clock information.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x39, 0x7F, 0x57, 0x07, 0x77, 0x1F, 0x00, 0x00, 0x00, 0x17} );
        Assert.assertEquals(" Slot test 4",
                "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 31, 15:20. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7C, 0x00, 0x38} );
        Assert.assertEquals(" Slot test 5",
                "Request Programming Track information.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7C, 0x6B, 0x02, 0x00, 0x00, 0x07, 0x00, 0x1C, 0x06, 0x7F, 0x7F, 0x1E} );
        Assert.assertEquals(" Slot test 6",
                "Programming Response: Write Byte in Direct Mode on Service Track Failed, No Write Acknowledge from decoder: CV29 value 6 (0x06, 00000110b).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7D, 0x00, 0x39} );
        Assert.assertEquals(" Slot test 7",
                "Unable to parse LocoNet message.\ncontents: BB 7D 00 39\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7D, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6C} );
        Assert.assertEquals(" Slot test 8",
                "Unable to parse LocoNet message.\ncontents: E7 0E 7D 00 00 00 00 05 00 00 00 00 00 6C\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7E, 0x00, 0x3A} );
        Assert.assertEquals(" Slot test 9",
                "Request Extended Command Station OpSwitches (DCS210/DCS240 only).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7E, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6F} );
        Assert.assertEquals(" Slot test 10",
                "Report of current Extended Command Station OpSw values:\n" +
"	OpSw65=Thrown, OpSw66=Thrown, OpSw67=Thrown, OpSw68=Thrown, OpSw69=Thrown, OpSw70=Thrown, OpSw71=Thrown, OpSw72=Thrown,\n" +
"	OpSw73=Thrown, OpSw74=Thrown, OpSw75=Thrown, OpSw76=Thrown, OpSw77=Thrown, OpSw78=Thrown, OpSw79=Thrown, OpSw80=Thrown,\n" +
"	OpSw81=Thrown, OpSw82=Thrown, OpSw83=Thrown, OpSw84=Thrown, OpSw85=Thrown, OpSw86=Thrown, OpSw87=Thrown, OpSw88=Thrown,\n" +
"	OpSw89=Thrown, OpSw90=Thrown, OpSw91=Thrown, OpSw92=Thrown, OpSw93=Thrown, OpSw94=Thrown, OpSw95=Thrown, OpSw96=Thrown,\n" +
"	OpSw97=Thrown, OpSw98=Thrown, OpSw99=Thrown, OpSw100=Thrown, OpSw101=Thrown, OpSw102=Thrown, OpSw103=Thrown, OpSw104=Thrown,\n" +
"	OpSw105=Thrown, OpSw106=Thrown, OpSw107=Thrown, OpSw108=Thrown, OpSw109=Thrown, OpSw110=Thrown, OpSw111=Thrown, OpSw112=Thrown,\n" +
"	OpSw113=Thrown, OpSw114=Thrown, OpSw115=Thrown, OpSw116=Thrown, OpSw117=Thrown, OpSw118=Thrown, OpSw119=Thrown, OpSw120=Thrown,\n" +
"	OpSw121=Thrown, OpSw122=Thrown, OpSw123=Thrown, OpSw124=Thrown, OpSw125=Thrown, OpSw126=Thrown, OpSw127=Thrown, OpSw128=Thrown.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBF, 0x21, 0x31, 0x50} );
        Assert.assertEquals(" Slot test 11", "Request slot for loco address 4273.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x81, 0x7E} );
        Assert.assertEquals(" Slot test 12", "Master is busy.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x01, 0x03, 0x31, 0x00, 0x00, 0x07, 0x00, 0x21, 0x00, 0x00, 0x00, 0x03} );
        Assert.assertEquals(" Slot test 13", "Report of slot 1 information:\n"
                +"\tLoco 4273 is Not Consisted, Free, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x01, 0x01, 0x45} );
        Assert.assertEquals(" Slot test 14", "Set status of slot 1 to IN_USE.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x01, 0x33, 0x31, 0x00, 0x20, 0x07, 0x00, 0x21, 0x00, 0x00, 0x00, 0x33} );
        Assert.assertEquals(" Slot test 15", "Report of slot 1 information:\n"
                +"\tLoco 4273 is Not Consisted, In-Use, operating in 128 SS mode, and is moving Reverse at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBF, 0x1C, 0x02, 0x5E} );
        Assert.assertEquals(" Slot test 16", "Request slot for loco address 3586.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x02, 0x03, 0x02, 0x00, 0x00, 0x07, 0x00, 0x1C, 0x00, 0x00, 0x00, 0x0E} );
        Assert.assertEquals(" Slot test 17", "Report of slot 2 information:\n"
                +"\tLoco 3586 is Not Consisted, Free, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBF, 0x00, 0x00, 0x40} );
        Assert.assertEquals(" Slot test 18", "Request slot for loco address 0 (short).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x81, 0x7E} );
        Assert.assertEquals(" Slot test 19", "Master is busy.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x81, 0x7E} );
        Assert.assertEquals(" Slot test 20", "Master is busy.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x81, 0x7E} );
        Assert.assertEquals(" Slot test 21", "Master is busy.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0x81, 0x7E} );
        Assert.assertEquals(" Slot test 22", "Master is busy.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x02, 0x03, 0x00, 0x00, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10} );
        Assert.assertEquals(" Slot test 23", "Report of slot 2 information:\n"
                +"\tLoco 0 (short) is Not Consisted, Free, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x00 0x00 (0).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA0, 0x0A, 0x00, 0x55} );
        Assert.assertEquals(" Slot test 24", "Set speed of loco in slot 10 to 0.\n"
                , LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA0, 0x02, 0x00, 0x5D} );
        Assert.assertEquals(" Slot test 25", "Set speed of loco in slot 2 to 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA0, 0x02, 0x00, 0x5D} );
        Assert.assertEquals(" Slot test 26", "Set speed of loco in slot 2 to 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA0, 0x08, 0x01, 0x56} );
        Assert.assertEquals(" Slot test 27", "Set speed of loco in slot 8 to EMERGENCY STOP!\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB5, 0x07, 0x13, 0x5E});
        Assert.assertEquals(" Slot test 28", "Write slot 7 with status value 19 (0x13) - Loco is Not Consisted, Common and operating in 128 speed step mode.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x08, 0x33, 0x15, 0x00, 0x00, 0x07, 0x00, 0x0E, 0x00, 0x04, 0x44, 0x79});
        Assert.assertEquals(" Slot test 29", "Write slot 8 information:\n"
                +"\tLoco 1813 is Not Consisted, In-Use, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x44 0x04 (8708).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x00, 0x41});
        Assert.assertEquals(" Slot test 30", "Mark slot 4 as DISPATCHED.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x07, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 31", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x3A, 0x00, 0x71});
        Assert.assertEquals(" Slot test 32", "LONG_ACK: The Move Slots command was rejected.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB4, 0x6F, 0x7F, 0x5B});
        Assert.assertEquals(" Slot test 33", "LONG_ACK: Function not implemented, no reply will follow.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA2, 0x0E, 0x04, 0x57});
        Assert.assertEquals(" Slot test 34",
                "Set loco in slot 14 Sound1/F5=Off Sound2/F6=Off Sound3/F7=On Sound4/F8=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA0, 0x0D, 0x7F, 0x2D});
        Assert.assertEquals(" Slot test 35", "Set speed of loco in slot 13 to 127.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xB5, 0x0F, 0x13, 0x56});
        Assert.assertEquals(" Slot test 36",
                "Write slot 15 with status value 19 (0x13) - Loco is Not Consisted, Common and operating in 128 speed step mode.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA1, 0x55, 0x04, 0x49});
        Assert.assertEquals(" Slot test 37",
                "Set loco in slot 85 direction Forward F0=Off F1=Off F2=Off F3=On F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA1, 0x13, 0x05, 0x1B});
        Assert.assertEquals(" Slot test 38",
                "Set loco in slot 19 direction Forward F0=Off F1=On F2=Off F3=On F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA1, 0x77, 0x10, 0x3B});
        Assert.assertEquals(" Slot test 39",
                "Set loco in slot 119 direction Forward F0=On F1=Off F2=Off F3=Off F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA1, 0x01, 0x10, 0x3B});
        Assert.assertEquals(" Slot test 40",
                "Set loco in slot 1 direction Forward F0=On F1=Off F2=Off F3=Off F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA1, 0x02, 0x36, 0x3B});
        Assert.assertEquals(" Slot test 41",
                "Set loco in slot 2 direction Reverse F0=On F1=Off F2=On F3=On F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA1, 0x38, 0x18, 0x7e});
        Assert.assertEquals(" Slot test 42",
                "Set loco in slot 56 direction Forward F0=On F1=Off F2=Off F3=Off F4=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA2, 0x55, 0x00, 0x08});
        Assert.assertEquals(" Slot test 43",
                "Set loco in slot 85 Sound1/F5=Off Sound2/F6=Off Sound3/F7=Off Sound4/F8=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA2, 0x25, 0x04, 0x7C});
        Assert.assertEquals(" Slot test 44",
                "Set loco in slot 37 Sound1/F5=Off Sound2/F6=Off Sound3/F7=On Sound4/F8=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA2, 0x25, 0x0C, 0x74});
        Assert.assertEquals(" Slot test 45",
                "Set loco in slot 37 Sound1/F5=Off Sound2/F6=Off Sound3/F7=On Sound4/F8=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA2, 0x25, 0x06, 0x7E});
        Assert.assertEquals(" Slot test 46",
                "Set loco in slot 37 Sound1/F5=Off Sound2/F6=On Sound3/F7=On Sound4/F8=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb5, 0x25, 0x1B, 0x74});
        Assert.assertEquals(" Slot test 47",
                "Write slot 37 with status value 27 (0x1B) - Loco is Consist TOP, Common and operating in 128 speed step mode.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x2C, 0x4A});
        Assert.assertEquals(" Slot test 48",
                "Set consist in slot 47 direction Reverse F0=Off F1=Off F2=Off F3=On F4=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x2D, 0x4A});
        Assert.assertEquals(" Slot test 49",
                "Set consist in slot 47 direction Reverse F0=Off F1=On F2=Off F3=On F4=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA3, 0x02, 0x01, 0x5F});
        Assert.assertEquals(" Slot test 51",
                "Set (Intellibox-II format) loco in slot 2 F9=On F10=Off F11=Off F12=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA3, 0x10, 0x02, 0x5F});
        Assert.assertEquals(" Slot test 52",
                "Set (Intellibox-II format) loco in slot 16 F9=Off F10=On F11=Off F12=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA3, 0x02, 0x04, 0x5F});
        Assert.assertEquals(" Slot test 53",
                "Set (Intellibox-II format) loco in slot 2 F9=Off F10=Off F11=On F12=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA3, 0x10, 0x08, 0x5F});
        Assert.assertEquals(" Slot test 54",
                "Set (Intellibox-II format) loco in slot 16 F9=Off F10=Off F11=Off F12=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x02, 0x08, 0x01, 0x00});
        Assert.assertEquals(" Slot test 55",
                "Set (Intellibox-II format) loco in slot 2 F13=On F14=Off F15=Off F16=Off F17=Off F18=Off F19=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x02, 0x08, 0x02, 0x00});
        Assert.assertEquals(" Slot test 56",
                "Set (Intellibox-II format) loco in slot 2 F13=Off F14=On F15=Off F16=Off F17=Off F18=Off F19=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x02, 0x08, 0x04, 0x00});
        Assert.assertEquals(" Slot test 57",
                "Set (Intellibox-II format) loco in slot 2 F13=Off F14=Off F15=On F16=Off F17=Off F18=Off F19=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x02, 0x08, 0x08, 0x00});
        Assert.assertEquals(" Slot test 58",
                "Set (Intellibox-II format) loco in slot 2 F13=Off F14=Off F15=Off F16=On F17=Off F18=Off F19=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x02, 0x08, 0x10, 0x00});
        Assert.assertEquals(" Slot test 59",
                "Set (Intellibox-II format) loco in slot 2 F13=Off F14=Off F15=Off F16=Off F17=On F18=Off F19=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x02, 0x08, 0x20, 0x00});
        Assert.assertEquals(" Slot test 60",
                "Set (Intellibox-II format) loco in slot 2 F13=Off F14=Off F15=Off F16=Off F17=Off F18=On F19=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x02, 0x08, 0x40, 0x00});
        Assert.assertEquals(" Slot test 61",
                "Set (Intellibox-II format) loco in slot 2 F13=Off F14=Off F15=Off F16=Off F17=Off F18=Off F19=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x09, 0x01, 0x01});
        Assert.assertEquals(" Slot test 62",
                "Set (Intellibox-II format) loco in slot 112 F21=On F22=Off F23=Off F24=Off F25=Off F26=Off F27=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x09, 0x02, 0x01});
        Assert.assertEquals(" Slot test 63",
                "Set (Intellibox-II format) loco in slot 112 F21=Off F22=On F23=Off F24=Off F25=Off F26=Off F27=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x09, 0x04, 0x01});
        Assert.assertEquals(" Slot test 64",
                "Set (Intellibox-II format) loco in slot 112 F21=Off F22=Off F23=On F24=Off F25=Off F26=Off F27=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x09, 0x08, 0x01});
        Assert.assertEquals(" Slot test 65",
                "Set (Intellibox-II format) loco in slot 112 F21=Off F22=Off F23=Off F24=On F25=Off F26=Off F27=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x09, 0x10, 0x01});
        Assert.assertEquals(" Slot test 66",
                "Set (Intellibox-II format) loco in slot 112 F21=Off F22=Off F23=Off F24=Off F25=On F26=Off F27=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x09, 0x20, 0x01});
        Assert.assertEquals(" Slot test 67",
                "Set (Intellibox-II format) loco in slot 112 F21=Off F22=Off F23=Off F24=Off F25=Off F26=On F27=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x09, 0x40, 0x01});
        Assert.assertEquals(" Slot test 68",
                "Set (Intellibox-II format) loco in slot 112 F21=Off F22=Off F23=Off F24=Off F25=Off F26=Off F27=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x01, 0x05, 0x10, 0x01});
        Assert.assertEquals(" Slot test 69",
                "Set (Intellibox-II format) loco in slot 1 F12=On F20=Off F28=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x05, 0x20, 0x01});
        Assert.assertEquals(" Slot test 70",
                "Set (Intellibox-II format) loco in slot 112 F12=Off F20=On F28=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xD4, 0x20, 0x70, 0x05, 0x40, 0x01});
        Assert.assertEquals(" Slot test 71",
                "Set (Intellibox-II format) loco in slot 112 F12=Off F20=Off F28=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7f, 0x00, 0x3A} );
        Assert.assertEquals(" Slot test 72",
                "Request Command Station OpSwitches (or DCS210/DCS240 check for multiple command stations on LocoNet).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x43, 0x41});
        Assert.assertEquals(" Slot test 73", "Move data in slot 4 to slot 67.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x00, 0x43, 0x41});
        Assert.assertEquals(" Slot test 74", "Get most recently dispatched slot.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x79, 0x00, 0x4D} );
        Assert.assertEquals(" Slot test 75",
                "Unable to parse LocoNet message.\ncontents: BB 79 00 4D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7A, 0x00, 0x4D} );
        Assert.assertEquals(" Slot test 76",
                "Unable to parse LocoNet message.\ncontents: BB 7A 00 4D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7D, 0x00, 0x4D} );
        Assert.assertEquals(" Slot test 77",
                "Unable to parse LocoNet message.\ncontents: BB 7D 00 4D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBB, 0x7E, 0x00, 0x4D} );
        Assert.assertEquals(" Slot test 78",
                "Request Extended Command Station OpSwitches (DCS210/DCS240 only).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBF, 0x7d, 0x04, 0x40} );
        Assert.assertEquals(" Slot test 79", "Request slot for loco address 4 (short) (or long address 16004).\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBF, 0x00, 0x70, 0x40} );
        Assert.assertEquals(" Slot test 80", "Request slot for loco address 112 (short, or \"B2\").\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xA2, 0x25, 0x07, 0x7E});
        Assert.assertEquals(" Slot test 81",
                "Set loco in slot 37 Sound1/F5=On Sound2/F6=On Sound3/F7=On Sound4/F8=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x79, 0x38});
        Assert.assertEquals(" Slot test 82",
                "Unable to parse LocoNet message.\ncontents: BA 04 79 38\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x7A, 0x3b});
        Assert.assertEquals(" Slot test 83",
                "Unable to parse LocoNet message.\ncontents: BA 04 7A 3B\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x7b, 0x3a});
        Assert.assertEquals(" Slot test 84",
                "Unable to parse LocoNet message.\ncontents: BA 04 7B 3A\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x7c, 0x3d});
        Assert.assertEquals(" Slot test 85",
                "Unable to parse LocoNet message.\ncontents: BA 04 7C 3D\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x7d, 0x3c});
        Assert.assertEquals(" Slot test 86",
                "Unable to parse LocoNet message.\ncontents: BA 04 7D 3C\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x7e, 0x3f});
        Assert.assertEquals(" Slot test 87",
                "Unable to parse LocoNet message.\ncontents: BA 04 7E 3F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBA, 0x04, 0x7f, 0x3e});
        Assert.assertEquals(" Slot test 87",
                "Unable to parse LocoNet message.\ncontents: BA 04 7F 3E\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x00, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 88", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x01, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 89", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: On/Paused; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x02, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 90", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: Off/Running; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x03, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 91", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: On/Running; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x04, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 92", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: Off/Paused; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x05, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 93", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Paused; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x06, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 94", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: Off/Running; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x08, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 88", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: Off/Paused; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x09, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 95", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: On/Paused; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x0A, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 96", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: Off/Running; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x0b, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 97", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports DT200; Track Status: On/Running; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x0c, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 98", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: Off/Paused; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x0d, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 99", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Paused; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x0e, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 100", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: Off/Running; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x04, 0x07, 0x7A, 0x65, 0x0f, 0x6E, 0x20, 0x00, 0x00, 0x00, 0x38});
        Assert.assertEquals(" Slot test 101", "Response Fast Clock is Synchronized, Running, rate is 4:1. Day 32, 06:34. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Busy.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x2C, 0x4A});
        Assert.assertEquals(" Slot test 102",
                "Set consist in slot 47 direction Reverse F0=Off F1=Off F2=Off F3=On F4=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x00, 0x4A});
        Assert.assertEquals(" Slot test 103",
                "Set consist in slot 47 direction Forward F0=Off F1=Off F2=Off F3=Off F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x01, 0x4A});
        Assert.assertEquals(" Slot test 104",
                "Set consist in slot 47 direction Forward F0=Off F1=On F2=Off F3=Off F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x02, 0x4A});
        Assert.assertEquals(" Slot test 105",
                "Set consist in slot 47 direction Forward F0=Off F1=Off F2=On F3=Off F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x04, 0x4A});
        Assert.assertEquals(" Slot test 106",
                "Set consist in slot 47 direction Forward F0=Off F1=Off F2=Off F3=On F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x08, 0x4A});
        Assert.assertEquals(" Slot test 107",
                "Set consist in slot 47 direction Forward F0=Off F1=Off F2=Off F3=Off F4=On.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x10, 0x4A});
        Assert.assertEquals(" Slot test 108",
                "Set consist in slot 47 direction Forward F0=On F1=Off F2=Off F3=Off F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x20, 0x4A});
        Assert.assertEquals(" Slot test 109",
                "Set consist in slot 47 direction Reverse F0=Off F1=Off F2=Off F3=Off F4=Off.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xb6, 0x2F, 0x40, 0x26});
        Assert.assertEquals(" Slot test 110",
                "Unable to parse LocoNet message.\ncontents: B6 2F 40 26\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x08, 0x33, 0x15, 0x00, 0x00, 0x27, 0x00, 0x0E, 0x00, 0x04, 0x44, 0x79});
        Assert.assertEquals(" Slot test 111", "Write slot 8 information:\n"
                +"\tLoco 1813 is Not Consisted, In-Use, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x44 0x04 (8708).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x00, 0x39, 0x7F, 0x57, 0x07, 0x77, 0x1F, 0x00, 0x00, 0x00, 0x17} );
        Assert.assertEquals(" Slot test 112",
                "Response Fast Clock is Synchronized, Frozen, rate is 0:1. Day 31, 15:20. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7B, 0x01, 0x39, 0x7F, 0x57, 0x07, 0x77, 0x1F, 0x20, 0x00, 0x00, 0x17} );
        Assert.assertEquals(" Slot test 113",
                "Response Fast Clock is  Running, rate is 1:1. Day 31, 15:20. Last set by ID 0x00 0x00 (0).\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x08, 0x33, 0x15, 0x00, 0x00, 0x27, 0x00, 0x0E, 0x01, 0x04, 0x44, 0x79});
        Assert.assertEquals(" Slot test 111", "Write slot 8 information:\n"
                +"\tLoco 1813 is Not Consisted, In-Use, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=On, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x44 0x04 (8708).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x08, 0x33, 0x15, 0x00, 0x00, 0x27, 0x00, 0x0E, 0x02, 0x04, 0x44, 0x79});
        Assert.assertEquals(" Slot test 111", "Write slot 8 information:\n"
                +"\tLoco 1813 is Not Consisted, In-Use, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=On, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x44 0x04 (8708).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x08, 0x33, 0x15, 0x00, 0x00, 0x27, 0x00, 0x0E, 0x04, 0x04, 0x44, 0x79});
        Assert.assertEquals(" Slot test 111", "Write slot 8 information:\n"
                +"\tLoco 1813 is Not Consisted, In-Use, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=On, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x44 0x04 (8708).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x08, 0x33, 0x15, 0x00, 0x00, 0x27, 0x00, 0x0E, 0x08, 0x04, 0x44, 0x79});
        Assert.assertEquals(" Slot test 111", "Write slot 8 information:\n"
                +"\tLoco 1813 is Not Consisted, In-Use, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=On\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x44 0x04 (8708).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x01, 0x03, 0x31, 0x00, 0x00, 0x07, 0x00, 0x7d, 0x00, 0x20, 0x30, 0x03} );
        Assert.assertEquals(" Slot test 13", "Report of slot 1 information:\n"
                +"\tLoco 49 (short) (or long address 16049) is Not Consisted, Free, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x30 0x20 (6176).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x01, 0x03, 0x31, 0x00, 0x00, 0x07, 0x00, 0x7F, 0x00, 0x20, 0x30, 0x03} );
        Assert.assertEquals(" Slot test 13", "Report of slot 1 information:\n"
                +"\tLoco 49 (short) (via an Alias) is Not Consisted, Free, operating in 128 SS mode, and is moving Forward at speed 0,\n"
                +"\tF0=Off, F1=Off, F2=Off, F3=Off, F4=Off, F5=Off, F6=Off, F7=Off, F8=Off\n"
                +"\tMaster supports LocoNet 1.1; Track Status: On/Running; Programming Track Status: Available; STAT2=0x00, ThrottleID=0x30 0x20 (6176).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testAliasing() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 01", "Get Aliasing Information.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x00, 0x20, 0x00, 0x0b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 02", "Aliasing Report: 64 aliases supported.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x02, 0x00, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 03", "Get Alias pair 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x02, 0x01, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 04", "Get Alias pair 1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x02, 0x02, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 05", "Get Alias pair 2.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x02, 0x04, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 06", "Get Alias pair 4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x02, 0x08, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 07", "Get Alias pair 8.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x00, 0x00, 0x0f, 0x12, 0x34, 0x56, 0x00, 0x23, 0x14, 0x32, 0x00, 0x00} );
        Assert.assertEquals("aliasing 08", "Report Alias pair 0: 6674 is an alias for 86; 2595 is an alias for 50.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x01, 0x00, 0x0f, 0x00, 0x01, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 09", "Report Alias pair 1: 128 is an alias for 8; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x02, 0x00, 0x0f, 0x00, 0x02, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 10", "Report Alias pair 2: 256 is an alias for 9; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x03, 0x00, 0x0f, 0x00, 0x04, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 11", "Report Alias pair 3: 512 is an alias for 10; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x04, 0x00, 0x0f, 0x00, 0x08, 0x0b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 12", "Report Alias pair 4: 1024 is an alias for 11; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x05, 0x00, 0x0f, 0x00, 0x10, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 13", "Report Alias pair 5: 2048 is an alias for 12; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x06, 0x00, 0x0f, 0x00, 0x20, 0x0d, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 14", "Report Alias pair 6: 4096 is an alias for 13; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x07, 0x00, 0x0f, 0x00, 0x40, 0x0e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 15", "Report Alias pair 7: 8192 is an alias for 14; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x08, 0x00, 0x0f, 0x01, 0x01, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 16", "Report Alias pair 8: 129 is an alias for 15; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x09, 0x00, 0x0f, 0x7f, 0x01, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00} );
        Assert.assertEquals("aliasing 17", "Report Alias pair 9: 255 is an alias for 16; 0 (short) is an alias for 0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x00, 0x00, 0x0f, 0x7f, 0x01, 0x10, 0x00, 0x0A, 0x01, 0x20, 0x00, 0x00} );
        Assert.assertEquals("aliasing 18", "Report Alias pair 0: 255 is an alias for 16; 138 is an alias for 32.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x00, 0x00, 0x0f, 0x7f, 0x01, 0x10, 0x00, 0x0A, 0x02, 0x21, 0x00, 0x00} );
        Assert.assertEquals("aliasing 19", "Report Alias pair 0: 255 is an alias for 16; 266 is an alias for 33.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE6, 0x10, 0x00, 0x02, 0x00, 0x00, 0x0f, 0x7f, 0x01, 0x10, 0x00, 0x21, 0x04, 0x22, 0x00, 0x00} );
        Assert.assertEquals("aliasing 20", "Report Alias pair 0: 255 is an alias for 16; 545 is an alias for 34.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x43, 0x02, 0x00, 0x00, 0x53, 0x01, 0x21, 0x00, 0x21, 0x04, 0x22, 0x00, 0x00} );
        Assert.assertEquals("aliasing 21", "Set Alias pair 2: 211 is an alias for 33; 545 is an alias for 34.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xEE, 0x10, 0x00, 0x43, 0x03, 0x00, 0x00, 0x53, 0x01, 0x21, 0x00, 0x20, 0x04, 0x18, 0x00, 0x00} );
        Assert.assertEquals("aliasing 22", "Set Alias pair 3: 211 is an alias for 33; 544 is an alias for 24.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testConvertToMixed() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xbf, 0x00, 0x01, 0x7f});
        Assert.assertEquals("convert To Mixed 1", "Request slot for loco address 1 (short).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7d, 0x01, 0x7f});
        Assert.assertEquals("convert To Mixed 2", "Request slot for loco address 1 (short) (or long address 16001).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7F, 0x01, 0x7f});
        Assert.assertEquals("convert To Mixed 3", "Request slot for loco address 1 (short).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x00, 99, 0x7f});
        Assert.assertEquals("convert To Mixed 4", "Request slot for loco address 99 (short).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7d, 99, 0x7f});
        Assert.assertEquals("convert To Mixed 5", "Request slot for loco address 99 (short) (or long address 16099).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7f, 99, 0x7f});
        Assert.assertEquals("convert To Mixed 6", "Request slot for loco address 99 (short).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x00, 100, 0x7f});
        Assert.assertEquals("convert To Mixed 7", "Request slot for loco address 100 (short, or \"A0\").\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7d, 100, 0x7f});
        Assert.assertEquals("convert To Mixed 8", "Request slot for loco address 100 (short, or \"A0\") (or long address 16100).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7f, 109, 0x7f});
        Assert.assertEquals("convert To Mixed 9", "Request slot for loco address 109 (short, or \"A9\").\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x00, 111, 0x7f});
        Assert.assertEquals("convert To Mixed 10", "Request slot for loco address 111 (short, or \"B1\").\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7d, 111, 0x7f});
        Assert.assertEquals("convert To Mixed 11", "Request slot for loco address 111 (short, or \"B1\") (or long address 16111).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7f, 114, 0x7f});
        Assert.assertEquals("convert To Mixed 12", "Request slot for loco address 114 (short, or \"B4\").\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x00, 122, 0x7f});
        Assert.assertEquals("convert To Mixed 13", "Request slot for loco address 122 (short, or \"C2\").\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7d, 122, 0x7f});
        Assert.assertEquals("convert To Mixed 14", "Request slot for loco address 122 (short, or \"C2\") (or long address 16122).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7f, 123, 0x7f});
        Assert.assertEquals("convert To Mixed 15", "Request slot for loco address 123 (short, or \"C3\").\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x01, 14, 0x7f});
        Assert.assertEquals("convert To Mixed 16", "Request slot for loco address 142.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xbf, 0x7b, 1, 0x7f});
        Assert.assertEquals("convert To Mixed 17", "Request slot for loco address 15745.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testSwichMessages() {
        LocoNetMessage l;
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo("L", "LocoNet");
        jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        LnTurnoutManager lntm = new LnTurnoutManager(memo, lnis, false);

        jmri.InstanceManager.setTurnoutManager(lntm);

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x00, 0x00});
        Assert.assertEquals("Switch test 1",
                "Requesting Switch at LT1 () to Thrown (Off (open)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x20, 0x00});
        Assert.assertEquals("Switch test 2",
                "Requesting Switch at LT1 () to Closed (Off (open)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x10, 0x00});
        Assert.assertEquals("Switch test 3",
                "Requesting Switch at LT1 () to Thrown (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x30, 0x00});
        Assert.assertEquals("Switch test 4",
                "Requesting Switch at LT1 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x01, 0x30, 0x00});
        Assert.assertEquals("Switch test 5",
                "Requesting Switch at LT2 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x02, 0x30, 0x00});
        Assert.assertEquals("Switch test 6",
                "Requesting Switch at LT3 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x04, 0x30, 0x00});
        Assert.assertEquals("Switch test 7",
                "Requesting Switch at LT5 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x08, 0x30, 0x00});
        Assert.assertEquals("Switch test 8",
                "Requesting Switch at LT9 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x10, 0x30, 0x00});
        Assert.assertEquals("Switch test 9",
                "Requesting Switch at LT17 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x20, 0x30, 0x00});
        Assert.assertEquals("Switch test 10",
                "Requesting Switch at LT33 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x40, 0x30, 0x00});
        Assert.assertEquals("Switch test 11",
                "Requesting Switch at LT65 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x31, 0x00});
        Assert.assertEquals("Switch test 12",
                "Requesting Switch at LT129 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x32, 0x00});
        Assert.assertEquals("Switch test 13",
                "Requesting Switch at LT257 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x34, 0x00});
        Assert.assertEquals("Switch test 14",
                "Requesting Switch at LT513 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x38, 0x00});
        Assert.assertEquals("Switch test 15",
                "Requesting Switch at LT1025 () to Closed (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x10, 0x00});
        Assert.assertEquals("Switch test 16",
                "Requesting Switch at LT1 () to Thrown (On (sink)), with acknowledgment.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x00, 0x00});
        Assert.assertEquals("Switch test 17",
                "Request status of switch LT1 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x01, 0x00, 0x00});
        Assert.assertEquals("Switch test 18",
                "Request status of switch LT2 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x02, 0x00, 0x00});
        Assert.assertEquals("Switch test 19",
                "Request status of switch LT3 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x04, 0x00, 0x00});
        Assert.assertEquals("Switch test 20",
                "Request status of switch LT5 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x08, 0x00, 0x00});
        Assert.assertEquals("Switch test 21",
                "Request status of switch LT9 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x10, 0x00, 0x00});
        Assert.assertEquals("Switch test 22",
                "Request status of switch LT17 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x20, 0x00, 0x00});
        Assert.assertEquals("Switch test 23",
                "Request status of switch LT33 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x40, 0x00, 0x00});
        Assert.assertEquals("Switch test 24",
                "Request status of switch LT65 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x01, 0x00});
        Assert.assertEquals("Switch test 25",
                "Request status of switch LT129 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x02, 0x00});
        Assert.assertEquals("Switch test 26",
                "Request status of switch LT257 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x04, 0x00});
        Assert.assertEquals("Switch test 27",
                "Request status of switch LT513 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        LnTurnout t = (LnTurnout) lntm.provideTurnout("LT513");

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x04, 0x00});
        Assert.assertEquals("Switch test 27",
                "Request status of switch LT513 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
        t.setUserName("This User Name");

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x04, 0x00});
        Assert.assertEquals("Switch test 27",
                "Request status of switch LT513 (This User Name).\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x08, 0x00});
        Assert.assertEquals("Switch test 28",
                "Request status of switch LT1025 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x40, 0x02});
        Assert.assertEquals("Switch test 29",
                "Unable to parse LocoNet message.\ncontents: BD 00 40 02\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x10, 0x53});
        Assert.assertEquals("Switch test 30",
                "Request status of switch LT1 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x20, 0x63});
        Assert.assertEquals("Switch test 31",
                "Request status of switch LT1 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x00, 0x40, 0x33});
        Assert.assertEquals("Switch test 32",
                "Unable to parse LocoNet message.\ncontents: BC 00 40 33\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBD, 0x00, 0x40, 0x00});
        Assert.assertEquals("Switch test 33",
                "Unable to parse LocoNet message.\ncontents: BD 00 40 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xBC, 0x05, 0x30, 0x76});
        Assert.assertEquals("Switch test 34",
                "Request status of switch LT6 ().\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
}

    @Test
    public void testDirf() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xA1, 0x55, 0x44, 0x49});
        Assert.assertEquals(" Slot test 37",
                "Unable to parse LocoNet message.\ncontents: A1 55 44 49\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testPeerXfer20DuplexQuery() {
        LocoNetMessage l;
        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x01, 0x08, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex receiver query",
                "Query Duplex Receivers.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x01, 0x10, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex receiver query",
                "Duplex Receiver Response.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x01, 0x01, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex receiver query",
                "Unable to parse LocoNet message.\ncontents: E5 14 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x08, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex channel query",
                "Query Duplex Channel.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x00, 1,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex channel set",
                "Set Duplex Channel to 128.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x00, 1,1,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex channel set",
                "Set Duplex Channel to 129.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x00, 0,0x40,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex channel set",
                "Set Duplex Channel to 64.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x10, 0, 1,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex channel reply",
                "Reported Duplex Channel is 1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x10, 1, 0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex channel reply",
                "Reported Duplex Channel is 128.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x09, 1, 0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex bogus request",
                "Unable to parse LocoNet message.\ncontents: E5 14 02 09 01 00 00 00 00 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x08, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex name query",
                "Query Duplex Group Name.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to Fade2Blk.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x01,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to "
                        + (char) 0xc6
                        + "ade2Blk.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x02,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to F"
                        + (char) 0xe1
                        + "de2Blk.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x04,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to Fa"
                        + (char) 0xe4
                        + "e2Blk.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x08,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to Fad"
                        + (char) 0xe5
                        + "2Blk.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x00,0x46,0x61,0x64,0x65,0x01,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to Fade"
                        + (char) 0xb2
                        + "Blk.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x00,0x46,0x61,0x64,0x65,0x02,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to Fade2"
                        + (char) 0xc2
                        + "lk.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x00,0x46,0x61,0x64,0x65,0x04,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to Fade2B"
                        + (char) 0xEc
                        + "k.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x00, 0x00,0x46,0x61,0x64,0x65,0x08,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Set Duplex Group Name to Fade2Bl"
                        + (char) 0xEb
                        + ".\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Blk\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x01,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\""
                        + (char) 0xc6
                        + "ade2Blk\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x02,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"F"
                        + (char) 0xe1
                        + "de2Blk\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x04,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fa"
                        + (char) 0xe4
                        + "e2Blk\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x08,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fad"
                        + (char) 0xe5
                        + "2Blk\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x01,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade"
                        + (char) 0xb2
                        + "Blk\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x02,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2"
                        + (char) 0xC2
                        + "lk\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x04,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2B"
                        + (char) 0xec
                        + "k\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x08,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Bl"
                        + (char) 0xeb
                        + "\", Password=00000000, Channel=0, ID=0.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0x2f,0x63,0x3,5,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Blk\", Password=020F0603, Channel=3, ID=5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,1,0x2f,0x63,0x3,5,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Blk\", Password=0A0F0603, Channel=3, ID=5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,2,0x2f,0x63,0x3,5,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Blk\", Password=020F0E03, Channel=3, ID=5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,4,0x2f,0x63,0x3,5,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Blk\", Password=020F0603, Channel=131, ID=5.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,8,0x2f,0x63,0x3,5,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Blk\", Password=020F0603, Channel=3, ID=133.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x10, 0x00,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,8,0x2f,0x63,0x3,5,0x7f});
        Assert.assertEquals("duplex nzme report",
                "Reported Duplex Group Name=\"Fade2Blk\", Password=020F0603, Channel=3, ID=133.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x04, 0x08, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex ID query",
                "Query Duplex ID.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x04, 0x10, 1,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex ID query",
                "Reported Duplex ID is 128.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x04, 0x10, 1,9,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex ID query",
                "Reported Duplex ID is 137.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x04, 0x10, 0,1,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex ID query",
                "Reported Duplex ID is 1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x04, 0x00, 1, 0x16, 0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex ID query",
                "Set Duplex ID to 150.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x04, 0x00, 0,9,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex ID query",
                "Set Duplex ID to 9.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x08, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Query Duplex Password.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x10, 0, 0x31,0x32,0x33,0x34,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Reported Duplex Password is 1234.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x10, 0, 0x35,0x36,0x37,0x38,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Reported Duplex Password is 5678.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x10, 0, 0x39,0x30,0x3a,0x3b,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Reported Duplex Password is 90AB.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0, 0x36, 0x39,0x33,0x32,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Set Duplex Password to 6932.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x30,0x30,0x2F,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 30 30 2F 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x30,0x30,0x3D,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 30 30 3D 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x30,0x30,0x7f,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 30 30 7F 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x30,0x2F,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 30 2F 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x30,0x3D,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 30 3D 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x30,0x7f,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 30 7F 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x2F,0x30,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 2F 30 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x3D,0x30,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 3D 30 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x30,0x7f,0x30,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 30 7F 30 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x2F,0x30,0x30,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 2F 30 30 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x3D,0x30,0x30,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 3D 30 30 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x00, 0,0x7f,0x30,0x30,0x30,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 00 00 7F 30 30 30 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x08, 0x00,0x00,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("Duplex Group RSSI query",
                "Query Duplex Channel 0 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x08, 0x00,0x05,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("Duplex Group RSSI query",
                "Query Duplex Channel 5 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x08, 0x00,0x0b,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("Duplex Group RSSI query",
                "Query Duplex Channel 11 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x08, 0x00,0x1a, 0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("Duplex Group RSSI query",
                "Query Duplex Channel 26 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x08, 0x00,0x60,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("Duplex Group RSSI query",
                "Query Duplex Channel 96 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x08, 0x00,0x7f,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("Duplex Group RSSI query",
                "Query Duplex Channel 127 noise/activity.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x01, 0,0x00,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group RSSI query",
                "Unable to parse LocoNet message.\ncontents: E5 14 10 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x10, 0,0x00,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group RSSI query",
                "Reported Duplex Channel 0 noise/activity level is 0/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x10, 0x02, 0x0b,0x00,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group RSSI query",
                "Reported Duplex Channel 11 noise/activity level is 128/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x10, 0x10, 0x00,0x01A,0x7f,0x00,0x00,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group RSSI query",
                "Reported Duplex Channel 26 noise/activity level is 127/255.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x04, 0x01, 1, 0x16, 0,0,0,0,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex ID query (bogus)",
                "Unable to parse LocoNet message.\ncontents: E5 14 04 01 01 16 00 00 00 00 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x03, 0x02, 0x02,0x46,0x61,0x64,0x65,0x00,0x32,0x42,0x6c,0x6b,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex nzme set",
                "Unable to parse LocoNet message.\ncontents: E5 14 03 02 02 46 61 64 65 00 32 42 6C 6B 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x07, 0x01, 0, 0x36, 0x39,0x33,0x32,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 07 01 00 36 39 33 32 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x02, 0x01, 0, 0x36, 0x39,0x33,0x32,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 02 01 00 36 39 33 32 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xe5, 0x14, 0x01, 0x01, 0, 0x36, 0x39,0x33,0x32,0,0,0,0,0,0,0,0,0,0x7f});
        Assert.assertEquals("duplex Group Password query",
                "Unable to parse LocoNet message.\ncontents: E5 14 01 01 00 36 39 33 32 00 00 00 00 00 00 00 00 00 7F\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


    }

    @Test
    public void testDownloadFirmware() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x01, 0x02, 0x04, 0x00, 0x08, 0x10, 0x20, 0x40, 0x00});
        Assert.assertEquals("IPL firmware setup 1",
                "Download setup message: manufacturer=0, H/W version=2, S/W version=4, device=0x01, options=8.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x10, 0x02, 0x04, 0x08, 0x00, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware setup 2",
                "Download setup message: manufacturer=16, H/W version=4, S/W version=8, device=0x02, options=1.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x10, 0x02, 0x00, 0x08, 0x10, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 1",
                "Download set address 0x100200.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x10, 0x02, 0x01, 0x08, 0x10, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 2",
                "Download set address 0x100201.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x41, 0x00, 0x02, 0x01, 0x08, 0x10, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 3",
                "Download set address 0x800201.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x42, 0x00, 0x02, 0x01, 0x08, 0x10, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 4",
                "Download set address 0x008201.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x43, 0x00, 0x00, 0x01, 0x08, 0x10, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 5",
                "Download set address 0x808001.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x01, 0x08, 0x10, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 6",
                "Download set address 0x000001.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x03, 0x00, 0x08, 0x10, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 7",
                "Download set address 0x000300.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x03, 0x00, 0x08, 0x63, 0x01, 0x02, 0x04, 0x08, 0x00});
        Assert.assertEquals("IPL firmware address 7",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7F 7F 40 00 03 00 08 63 01 02 04 08 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));



        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x04, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 1a",
                "Download send data 0x04 0x00 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x41, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 1b",
                "Download send data 0x80 0x00 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x41, 0x40, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 1c",
                "Download send data 0xC0 0x00 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x03, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 2a",
                "Download send data 0x00 0x03 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x42, 0x00, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 2b",
                "Download send data 0x00 0x87 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x03, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 3a",
                "Download send data 0x00 0x00 0x03 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x44, 0x00, 0x00, 0x07, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 3b",
                "Download send data 0x00 0x00 0x87 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x19, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 4a",
                "Download send data 0x00 0x00 0x00 0x19 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x48, 0x00, 0x00, 0x00, 0x31, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 4b",
                "Download send data 0x00 0x00 0x00 0xB1 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));



        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x21, 0x75, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 5a",
                "Download send data 0x00 0x00 0x00 0x00 0xF5 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x20, 0x40, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 5b",
                "Download send data 0x00 0x00 0x00 0x00 0x40 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x22, 0x00, 0x63, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 6a",
                "Download send data 0x00 0x00 0x00 0x00 0x00 0xE3 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x55, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 6b",
                "Download send data 0x00 0x00 0x00 0x00 0x00 0x55 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x24, 0x00, 0x00, 0x10, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 7a",
                "Download send data 0x00 0x00 0x00 0x00 0x00 0x00 0x90 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x63, 0x00, 0x00});
        Assert.assertEquals("IPL firmware write data 7b",
                "Download send data 0x00 0x00 0x00 0x00 0x00 0x00 0x63 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x28, 0x00, 0x00, 0x00, 0x34, 0x00});
        Assert.assertEquals("IPL firmware write data 8a",
                "Download send data 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0xB4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("IPL firmware write data 8b",
                "Download send data 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x12.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));





        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x04, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 1a",
                "Download verify request 0x04 0x00 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x41, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 1b",
                "Download verify request 0x80 0x00 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x41, 0x40, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 1c",
                "Download verify request 0xC0 0x00 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x03, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 2a",
                "Download verify request 0x00 0x03 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x42, 0x00, 0x07, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 2b",
                "Download verify request 0x00 0x87 0x00 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x03, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 3a",
                "Download verify request 0x00 0x00 0x03 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x44, 0x00, 0x00, 0x07, 0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 3b",
                "Download verify request 0x00 0x00 0x87 0x00 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x19, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 4a",
                "Download verify request 0x00 0x00 0x00 0x19 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x48, 0x00, 0x00, 0x00, 0x31, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 4b",
                "Download verify request 0x00 0x00 0x00 0xB1 0x00 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x31, 0x75, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 5a",
                "Download verify request 0x00 0x00 0x00 0x00 0xF5 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x30, 0x40, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 5b",
                "Download verify request 0x00 0x00 0x00 0x00 0x40 0x00 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x32, 0x00, 0x63, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 6a",
                "Download verify request 0x00 0x00 0x00 0x00 0x00 0xE3 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x55, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 6b",
                "Download verify request 0x00 0x00 0x00 0x00 0x00 0x55 0x00 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x34, 0x00, 0x00, 0x10, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 7a",
                "Download verify request 0x00 0x00 0x00 0x00 0x00 0x00 0x90 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x63, 0x00, 0x00});
        Assert.assertEquals("IPL firmware verify data 7b",
                "Download verify request 0x00 0x00 0x00 0x00 0x00 0x00 0x63 0x00.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x38, 0x00, 0x00, 0x00, 0x34, 0x00});
        Assert.assertEquals("IPL firmware verify data 8a",
                "Download verify request 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0xB4.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("IPL firmware verify data 8b",
                "Download verify request 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x12.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7E, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("IPL firmware verify data 8b",
                "Unable to parse LocoNet message.\ncontents: E5 10 7E 7F 7F 40 00 00 00 00 30 00 00 00 12 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7E, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("IPL firmware verify data 8b",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7E 7F 40 00 00 00 00 30 00 00 00 12 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7F, 0x7E, 0x40, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("IPL firmware verify data 8b",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7F 7E 40 00 00 00 00 30 00 00 00 12 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7F, 0x7E, 0x30, 0x00, 0x00, 0x00, 0x00, 0x30, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("IPL firmware verify data 8b",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7F 7E 30 00 00 00 00 30 00 00 00 12 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));



        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("IPL firmware end 1",
                "Download end operation.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0x12, 0x00});
        Assert.assertEquals("Unknown firmware 1",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7F 7F 40 00 00 00 00 50 00 00 00 12 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware end 1",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7F 7F 40 00 00 00 00 60 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x61, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware end 1",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7F 7F 40 00 00 00 00 61 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));


        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x00, 0x00, 0x00, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals("IPL firmware end 1",
                "Unable to parse LocoNet message.\ncontents: E5 10 7F 7F 7F 40 00 00 00 00 70 00 00 00 00 00\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

        l = new LocoNetMessage(new int[] {0xE5, 0x10, 0x7F, 0x7f, 0x7f, 0x40, 0x00, 0x01, 0x02, 0x04, 0x00, 0x08, 0x10, 0x20, 0x40, 0x00});
        Assert.assertEquals("IPL firmware setup 1",
                "Download setup message: manufacturer=0, H/W version=2, S/W version=4, device=0x01, options=8.\n",
                LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));

    }

    @Test
    public void testExtendedCsOpSws() {
        LocoNetMessage l;

        l = new LocoNetMessage(new int[] {0xE7, 0x0E, 0x7E, 0x11, 0x04, 0x22, 0x00, 0x47, 0x33, 0x00, 0x44, 0x00, 0x6C, 0x03});
        Assert.assertEquals("read 1", "Report of current Extended Command Station OpSw values:\n" +
"	OpSw65=Closed, OpSw66=Thrown, OpSw67=Thrown, OpSw68=Thrown, OpSw69=Closed, OpSw70=Thrown, OpSw71=Thrown, OpSw72=Thrown,\n" +
"	OpSw73=Thrown, OpSw74=Thrown, OpSw75=Closed, OpSw76=Thrown, OpSw77=Thrown, OpSw78=Thrown, OpSw79=Thrown, OpSw80=Thrown,\n" +
"	OpSw81=Thrown, OpSw82=Closed, OpSw83=Thrown, OpSw84=Thrown, OpSw85=Thrown, OpSw86=Closed, OpSw87=Thrown, OpSw88=Thrown,\n" +
"	OpSw89=Thrown, OpSw90=Thrown, OpSw91=Thrown, OpSw92=Thrown, OpSw93=Thrown, OpSw94=Thrown, OpSw95=Thrown, OpSw96=Thrown,\n" +
"	OpSw97=Closed, OpSw98=Closed, OpSw99=Thrown, OpSw100=Thrown, OpSw101=Closed, OpSw102=Closed, OpSw103=Thrown, OpSw104=Thrown,\n" +
"	OpSw105=Thrown, OpSw106=Thrown, OpSw107=Thrown, OpSw108=Thrown, OpSw109=Thrown, OpSw110=Thrown, OpSw111=Thrown, OpSw112=Thrown,\n" +
"	OpSw113=Thrown, OpSw114=Thrown, OpSw115=Closed, OpSw116=Thrown, OpSw117=Thrown, OpSw118=Thrown, OpSw119=Closed, OpSw120=Thrown,\n" +
"	OpSw121=Thrown, OpSw122=Thrown, OpSw123=Thrown, OpSw124=Thrown, OpSw125=Thrown, OpSw126=Thrown, OpSw127=Thrown, OpSw128=Thrown.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
    }

    @Test
    public void testLocoReset() {
        LocoNetMessage l = new LocoNetMessage(new int[] {0x8a, 0x75});
        Assert.assertEquals("check LocoReset", "Loco Reset mechanism triggered.\n", LocoNetMessageInterpret.interpretMessage(l, "LT", "LS", "LR"));
    }

    @Before
    @Test
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    @Test
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
