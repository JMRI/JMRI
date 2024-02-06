package jmri.jmrit.etcs.dmi.swing;

import jmri.jmrit.etcs.CabMessage;
import jmri.jmrit.etcs.TrackSection;
import jmri.jmrit.etcs.MovementAuthority;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * Tests for DmiPanelE.
 * @author Steve Young Copyright (C) 2024
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DmiPanelETest {
    
    private boolean ackTriggered = false;
    
    @Test
    public void testSymbolsInSection(){
    
        DmiFrame df = new DmiFrame("testSymbolsInSection");
        DmiPanel dmiPanel = df.getDmiPanel();
        df.setVisible(true);
        JFrameOperator jfo = new JFrameOperator(df.getTitle());

        // setup main panel as per figures in ERTMSv4 8.2.3.4
        dmiPanel.setMaxDialSpeed(400); // max 400 units / hour
        dmiPanel.setActualSpeed(36); // 36 units / hour
        dmiPanel.setLevel(2); // etcs level 2
        dmiPanel.setMode(11); // full supervision
        dmiPanel.setSafeRadioConnection(1); // safe radio connection up
        
        ArrayList<DmiCircularSpeedGuideSection> csgSectionList = new ArrayList<>();
        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.DARK_GREY, 0, 140, true, true ));
        dmiPanel.setCsgSections(csgSectionList);
        
        ArrayList<TrackSection> trackSectionList1 = new ArrayList<>();
        TrackSection s = new TrackSection(290,140,5);
        trackSectionList1.add(s);
        s = new TrackSection(1000,140,-22);
        trackSectionList1.add(s);
        s = new TrackSection(8000,140,2);
        trackSectionList1.add(s);
        MovementAuthority ma1 = new MovementAuthority(trackSectionList1);
        ArrayList<MovementAuthority> mas1 = new ArrayList<>();
        mas1.add(ma1);
        dmiPanel.resetMovementAuthorities(mas1);
        dmiPanel.setScale(2);


        CabMessage ackMsg = new CabMessage("Unauthorised passing of EOA / LOA", 1, true);

        dmiPanel.addPropertyChangeListener(DmiPanel.PROP_CHANGE_CABMESSAGE_ACK, (PropertyChangeEvent evt) -> {
            ackTriggered = true;
            Assertions.assertEquals(ackMsg.getMessageId(), evt.getNewValue());
        });


        dmiPanel.messageDriver(new CabMessage("1st msg No Ack", 1, false));

        JLabelOperator label1oper = JemmyUtil.getLabelOperatorByName(jfo, "msglabel1");
        JLabelOperator time1oper = JemmyUtil.getLabelOperatorByName(jfo, "timeLabel1");

        Assertions.assertEquals("1st msg No Ack", label1oper.getText());

        dmiPanel.messageDriver(ackMsg);
        Assertions.assertEquals("Unauthorised passing of", label1oper.getText());
        Assertions.assertFalse(time1oper.getText().isEmpty());
        // acknowledge Message
        JemmyUtil.getButtonOperatorByName(jfo, "messageAcknowledgeButton").doClick();
        JUnitUtil.waitFor(() -> ackTriggered, "ack change event triggered");

        dmiPanel.messageDriver(new CabMessage("No MA received at level transition", 1, false));
        JUnitUtil.waitFor(1);
        dmiPanel.messageDriver(new CabMessage("auxiliary message last", 2, false));
        JUnitUtil.waitFor(1);
        dmiPanel.messageDriver(new CabMessage("Entering FS", 1, false));

        JButtonOperator upOper = JemmyUtil.getButtonOperatorByName(jfo, "e10upArrow");
        JButtonOperator downOper = JemmyUtil.getButtonOperatorByName(jfo, "e11downArrow");

        Assertions.assertFalse(upOper.isEnabled());
        Assertions.assertTrue(downOper.isEnabled());

        downOper.doClick();
        downOper.doClick();

        Assertions.assertTrue(upOper.isEnabled());
        Assertions.assertFalse(downOper.isEnabled());

        upOper.doClick();
        Assertions.assertTrue(downOper.isEnabled());

        upOper.doClick();
        Assertions.assertTrue(downOper.isEnabled());
        Assertions.assertFalse(upOper.isEnabled());

        jfo.requestClose();
        jfo.waitClosed();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
