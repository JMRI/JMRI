package jmri.jmrit.beantable;

import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.TurnoutManager;
import jmri.jmrix.acela.*;
import jmri.jmrix.can.*;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * Tests for SignalHeadAddEditFrame
 * @author Steve Young Copyright (C) 2023
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class SignalHeadAddEditFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testNewEditAcelaHead(){
        AcelaTrafficController atc = new AcelaTrafficControlScaffold();
        AcelaSystemConnectionMemo ascm = new AcelaSystemConnectionMemo(atc);
        ascm.register();
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringAcelaaspect"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("ACUName");
        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSignalheadNumber")).getLabelFor()).setText("1");
        new JComboBoxOperator(jfo, 1).setSelectedItem(Bundle.getMessage("StringSignalheadTriple"));
        
        Thread t1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("ErrorSignalHeadAddFailed",1), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        JUnitUtil.waitFor(() -> !t1.isAlive(),"acela no node dialogue complete");
        
        new JComboBoxOperator(jfo, 1).setSelectedItem(Bundle.getMessage("StringSignalheadDouble"));
        new JComboBoxOperator(jfo, 1).setSelectedItem(Bundle.getMessage("StringSignalheadBiPolar"));
        new JComboBoxOperator(jfo, 1).setSelectedItem(Bundle.getMessage("StringSignalheadWigwag"));
        AcelaNode acelaNodeStartingAddress0 = new AcelaNode(0,AcelaNode.SM,atc);
        acelaNodeStartingAddress0.initNode();
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());

        new JComboBoxOperator(jfo, 1).setSelectedItem(Bundle.getMessage("StringSignalheadBiPolar"));
        jfo.getQueueTool().waitEmpty();
        
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("InvalidUserNameAlreadyExists", Bundle.getMessage("BeanNameSignalHead"),"ACUName")
            , Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        JUnitUtil.waitFor(() -> !t2.isAlive(),"acela duplicate username dialogue complete");
        JUnitAppender.assertErrorMessage("User name is not unique ACUName");
        
        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("ACUName2");
        Thread t3 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("WarningTitle")
            , Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        JUnitUtil.waitFor(() -> !t3.isAlive(),"acela duplicate systemname dialogue complete");
        JUnitAppender.assertWarnMessage("Attempt to create signal with duplicate system name AH1");
        
        new JComboBoxOperator(jfo, 1).setSelectedItem(Bundle.getMessage("StringSignalheadDouble"));
        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSignalheadNumber")).getLabelFor()).setText("2");
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(2, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        
        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("AH2");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(AcelaSignalHead.class, newHead);
        
        int type = acelaNodeStartingAddress0.getOutputSignalHeadType(2); // SystemName AH2
        Assertions.assertEquals(AcelaNode.DOUBLE, type);
        
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();

        // now edit the new signal head
        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());
        
        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringAcelaaspect")));
        
        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);
        
        String uName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).getText();
        Assertions.assertEquals("ACUName2", uName);
        
        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("NewUName");
        new JComboBoxOperator(jfo, 0).setSelectedItem(Bundle.getMessage("StringSignalheadWigwag"));
        
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("NewUName", newHead.getUserName());
        type = acelaNodeStartingAddress0.getOutputSignalHeadType(2); // SystemName AH2
        Assertions.assertEquals(AcelaNode.WIGWAG, type);

        // JUnitUtil.waitFor(20000);

        atc.terminateThreads();
        ascm.dispose();
    }

    @Test
    public void testAddEditTripleTurnoutHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringTripleTurnout"));
        typeOperator.getQueueTool().waitEmpty();

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        JUnitUtil.waitFor(() -> !t1.isAlive(),"no system name dialogue complete");
        
        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH3");
        
        new JTextFieldOperator(jfo, 2).setText("4");
        new JTextFieldOperator(jfo, 3).setText("5");
        new JTextFieldOperator(jfo, 4).setText("6");
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(3, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();
        
        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH3");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.TripleTurnoutSignalHead.class, newHead);
        
        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());
        
        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringTripleTurnout")));
        
        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals("IH3", sysName);
        
        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("NewaUName");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("NewaUName", newHead.getUserName());
        
    }

    @Test
    public void testAddEditDoubleTurnoutHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringDoubleTurnout"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH222");
        
        new JTextFieldOperator(jfo, 2).setText("4");
        new JTextFieldOperator(jfo, 3).setText("5");
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(2, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH222");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.DoubleTurnoutSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringDoubleTurnout")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("Nme");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("Nme", newHead.getUserName());
        
    }

    @Test
    public void testAddEditTripleOutputHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringTripleOutput"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH4");
        
        new JTextFieldOperator(jfo, 2).setText("4");
        new JTextFieldOperator(jfo, 3).setText("5");
        new JTextFieldOperator(jfo, 4).setText("6");
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(3, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH4");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.TripleOutputSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringTripleOutput")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("NewaUName");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("NewaUName", newHead.getUserName());
        
    }

    @Test
    public void testAddEditQuadOutputHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringQuadOutput"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH77");
        
        new JTextFieldOperator(jfo, 2).setText("4");
        new JTextFieldOperator(jfo, 3).setText("5");
        new JTextFieldOperator(jfo, 4).setText("6");
        new JTextFieldOperator(jfo, 5).setText("7");
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(4, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH77");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.QuadOutputSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringQuadOutput")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("NewaUName");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("NewaUName", newHead.getUserName());
        
    }

    @Test
    public void testAddEditVirtualHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringVirtual"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH123");
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH123");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.VirtualSignalHead.class, newHead);
        
        ((SignalHeadAddEditFrame)frame).resetAddressFields();
        Assertions.assertEquals("", new JTextFieldOperator((JTextField) new JLabelOperator(jfo,
                Bundle.getMessage("LabelSystemName")).getLabelFor()).getText());
        Assertions.assertEquals("", new JTextFieldOperator((JTextField) new JLabelOperator(jfo,
                Bundle.getMessage("LabelUserName")).getLabelFor()).getText());

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringVirtual")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("N");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("N", newHead.getUserName());
        
    }

    @Test
    public void testAddEditSe8cHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringSE8c4aspect"));
        typeOperator.getQueueTool().waitEmpty();

      //  new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
      //      .getLabelFor()).setText("IH123");
      
        new JTextFieldOperator(jfo, 1).setText("11");
        new JTextFieldOperator(jfo, 2).setText("12");
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(2, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH:SE8c:\"IT11\";\"IT12\"");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.SE8cSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringSE8c4aspect")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("Nse8");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("Nse8", newHead.getUserName());
        
    }

    @Test
    public void testAddEditGrapevineHead(){
        
        GrapevineSystemConnectionMemo gscm = new GrapevineSystemConnectionMemo();
        gscm.setTrafficController(new SerialTrafficControlScaffold(gscm));
        gscm.register();
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringGrapevine"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("GH1");
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("GH1");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.jmrix.grapevine.SerialSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringGrapevine")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("G_N");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("G_N", newHead.getUserName());
        
        gscm.getTrafficController().terminateThreads();
        gscm.dispose();
        
    }

    @Test
    public void testAddEditMergSd2Head(){

        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });

        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringMerg"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH123");

        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        new JComboBoxOperator(jfo, 1).setSelectedItem("2"); // 2 outputs / appearances
        new JComboBoxOperator(jfo, 2).setSelectedItem(Bundle.getMessage("DistantSignal")); // home or distant
        new JTextFieldOperator(jfo, 2).setText("4"); // create IT4

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(1, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH123");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.MergSD2SignalHead.class, newHead);
        Assertions.assertFalse( ((jmri.implementation.MergSD2SignalHead)newHead).getHome());

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringMerg")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("Nm");
        new JComboBoxOperator(jfo, 0).setSelectedItem("4"); // 4 outputs / appearances
        jfo.getQueueTool().waitEmpty();

        new JRadioButtonOperator(jfo, 0).doClick(); // use existing in turnout slot 1
        new JRadioButtonOperator(jfo, 3).doClick(); // new turnout slot 2
        new JRadioButtonOperator(jfo, 5).doClick(); // new turnout slot 3
        jfo.getQueueTool().waitEmpty();

        new JTextFieldOperator(jfo, 2).setText("5"); // create IT5
        new JTextFieldOperator(jfo, 3).setText("6"); // create IT6
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).doClick();// closes frame
        jfo.waitClosed();
        Assertions.assertEquals("Nm", newHead.getUserName());
        
    }

    @Test
    public void testAddEditSingleTurnoutHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringSingle"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH13");
        
        new JTextFieldOperator(jfo, 2).setText("4"); // turnout IH4
        new JComboBoxOperator(jfo, 2).setSelectedItem(Bundle.getMessage("SignalHeadStateRed"));
        new JComboBoxOperator(jfo, 3).setSelectedItem(Bundle.getMessage("SignalHeadStateGreen"));

        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(1, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH13");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.SingleTurnoutSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringSingle")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);
        
        Assertions.assertEquals(Bundle.getMessage("SignalHeadStateRed"),  new JComboBoxOperator(jfo, 1).getSelectedItem());
        Assertions.assertEquals(Bundle.getMessage("SignalHeadStateGreen"),  new JComboBoxOperator(jfo, 2).getSelectedItem());
        
        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("Nme");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("Nme", newHead.getUserName());
        
    }

    @Test
    public void testAddEditDccHead(){
        
        // create a DCC Sys connection
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();

        memo.setTrafficController(tcis);
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        memo.configureManagers();
        
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringDccSigDec"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSignalheadNumber"))
            .getLabelFor()).setText("8");
        
        
        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("MH$8");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.DccSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringDccSigDec")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("Nme");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("Nme", newHead.getUserName());
        
        memo.getTrafficController().terminateThreads();
        memo.dispose();
        
    }

    @Test
    public void testAddEditLsDecHead(){
        
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        
        JFrameOperator jfo = new JFrameOperator( frame.getTitle() );
        JComboBoxOperator typeOperator = new JComboBoxOperator(jfo, 0);
        typeOperator.selectItem(Bundle.getMessage("StringLsDec"));
        typeOperator.getQueueTool().waitEmpty();

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).setText("IH123");
        
        new JTextFieldOperator(jfo, 2).setText("2");
        new JTextFieldOperator(jfo, 3).setText("3");
        new JTextFieldOperator(jfo, 4).setText("4");
        new JTextFieldOperator(jfo, 5).setText("5");
        new JTextFieldOperator(jfo, 6).setText("6");
        new JTextFieldOperator(jfo, 7).setText("7");
        new JTextFieldOperator(jfo, 8).setText("8");
        
        new JComboBoxOperator(jfo, 4).selectItem(InstanceManager.getDefault(TurnoutManager.class).getThrownText());
        new JComboBoxOperator(jfo, 6).selectItem(InstanceManager.getDefault(TurnoutManager.class).getClosedText());
        new JComboBoxOperator(jfo, 8).selectItem(InstanceManager.getDefault(TurnoutManager.class).getThrownText());
                
        jfo.getQueueTool().waitEmpty();

        Assertions.assertEquals(0, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(0, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());
        new JButtonOperator(jfo,Bundle.getMessage("ButtonCreate")).push();
        jfo.getQueueTool().waitEmpty();
        Assertions.assertEquals(1, InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().size());
        Assertions.assertEquals(7, InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().size());

        SignalHead newHead = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName("IH123");
        Assertions.assertNotNull(newHead);
        Assertions.assertInstanceOf(jmri.implementation.LsDecSignalHead.class, newHead);

        new JButtonOperator(jfo,Bundle.getMessage("ButtonCancel")).push();
        jfo.waitClosed();


        frame = new SignalHeadAddEditFrame(newHead);
        ThreadingUtil.runOnGUI(() -> {
            frame.initComponents();
        });
        jfo = new JFrameOperator(frame.getTitle());
        Assertions.assertNotNull(jfo);
        Assertions.assertEquals(newHead, ((SignalHeadAddEditFrame)frame).getSignalHead());

        String title = new JLabelOperator(jfo,0).getText();
        Assertions.assertTrue(title.contains(Bundle.getMessage("StringLsDec")));

        String sysName = new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelSystemName"))
            .getLabelFor()).getText();
        Assertions.assertEquals(newHead.getSystemName(), sysName);

        new JTextFieldOperator((JTextField) new JLabelOperator(jfo,Bundle.getMessage("LabelUserName")).getLabelFor()).setText("N");
        new JButtonOperator(jfo,Bundle.getMessage("ButtonUpdate")).push(); // closes frame
        jfo.waitClosed();
        Assertions.assertEquals("N", newHead.getUserName());
        
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        frame = new SignalHeadAddEditFrame(null); // New Signal Head
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

}
