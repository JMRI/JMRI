
package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
//import jmri.ConditionalManager;
import jmri.Light;
import jmri.Logix;
import jmri.Sensor;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.InstanceManager;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.display.Positionable;

/**
 * A collection of static utilities to provide cross referencing information
 * among the various PanelPro objects.  Most likely, this is incomplete as
 * there still may be references held by objects unknown to the author.  It is
 * intended to inform users where and how the various elements are used.  In
 * particular to identify useless elements ('orphans').  Currently, called only
 * from the Logix JFrame, which is probably not its ultimate UI.
 *<P>
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author  Pete Cressman   Copyright 2009
 * @version $Revision: 1.14 $
 */

public class Maintenance
{
	static final ResourceBundle rbm = ResourceBundle
			.getBundle("jmri.jmrit.beantable.MaintenanceBundle");

    /**
    *  Find references of a System or User name in the various Manager Objects
    */
    public static void deviceReportPressed(String devName, Frame parent) {
        JTextArea text = null;
        JScrollPane scrollPane = null;
        text = new javax.swing.JTextArea(25, 50);
        text.setEditable(false);
        text.setTabSize(4);
        search(devName, text);
        scrollPane = new JScrollPane(text);
        makeDialog(scrollPane, null, parent);
    }

    /**
    *  Find orphaned elements in the various Manager Objects
    */
    public static void findOrphansPressed(Frame parent) {
        Vector <String> display = new Vector<String>();
        Vector <String> names = new Vector<String>();

        Iterator<String> iter = InstanceManager.sensorManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null) && !name.equals("ISCLOCKRUNNING")) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                                           (Object[])getTypeAndNames(name)));
                names.add( name);
            }
        }
        iter = InstanceManager.turnoutManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                                           (Object[])getTypeAndNames(name)));
                names.add( name);
            }
        }
        iter = InstanceManager.signalHeadManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                                           (Object[])getTypeAndNames(name)));
                names.add( name);
            }
        }
        iter = InstanceManager.lightManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                                           (Object[])getTypeAndNames(name)));
                names.add( name);
            }
        }
        iter = InstanceManager.conditionalManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                                           (Object[])getTypeAndNames(name)));
                names.add( name);
            }
        }
        iter = InstanceManager.sectionManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                                           (Object[])getTypeAndNames(name)));
                names.add( name);
            }
        }
        iter = InstanceManager.blockManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                                           (Object[])getTypeAndNames(name)));
                names.add( name);
            }
        }
        DefaultListModel listModel = new DefaultListModel();
        for (int i=0; i<display.size(); i++)  {
            listModel.addElement(display.get(i));
        }
        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JButton button = new JButton(rbm.getString("DeleteButton"));
        button.setToolTipText(rbm.getString("OrphanDeleteHint"));

        class SearchListener implements ActionListener {
             JList l;
             Vector<String> n;
             SearchListener(JList list, Vector<String> name) {
                 l = list;
                 n = name;
             }
             public void actionPerformed(ActionEvent e) {
                 int index = l.getMaxSelectionIndex();
                 if (index < 0)  {
                     javax.swing.JOptionPane.showMessageDialog(null,
                             rbm.getString("OrphanDeleteHint"),
                             rbm.getString("DeleteTitle"),
                             javax.swing.JOptionPane.INFORMATION_MESSAGE);
                     return;
                 }
                 int min =l.getMinSelectionIndex();
                 DefaultListModel model = (DefaultListModel)l.getModel();
                 while (index>=min) {
                     String[] names = getTypeAndNames(n.get(index));
                     if (names[0].equals("Sensor")) {
                         Sensor s = InstanceManager.sensorManagerInstance().getBySystemName(names[2]);
                         if (s == null) {
                             s = InstanceManager.sensorManagerInstance().getBySystemName(names[1]);
                         }
                         InstanceManager.sensorManagerInstance().deregister(s);
                     } else if (names[0].equals("Turnout"))  {
                         Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(names[2]);
                         if (t == null) {
                             t = InstanceManager.turnoutManagerInstance().getBySystemName(names[1]);
                         }
                         InstanceManager.turnoutManagerInstance().deregister(t);
                     } else if (names[0].equals("SignalHead"))  {
                         SignalHead sh = InstanceManager.signalHeadManagerInstance().getBySystemName(names[2]);
                         if (sh == null) {
                             sh = InstanceManager.signalHeadManagerInstance().getBySystemName(names[1]);
                         }
                         InstanceManager.signalHeadManagerInstance().deregister(sh);
                     } else if (names[0].equals("Light"))  {
                         Light l = InstanceManager.lightManagerInstance().getBySystemName(names[2]);
                         if (l == null) {
                             l = InstanceManager.lightManagerInstance().getBySystemName(names[1]);
                         }
                         InstanceManager.lightManagerInstance().deregister(l);
                     } else if (names[0].equals("Conditional"))  {
                         Conditional c = InstanceManager.conditionalManagerInstance().getBySystemName(names[2]);
                         if (c == null) {
                             c = InstanceManager.conditionalManagerInstance().getBySystemName(names[1]);
                         }
                         InstanceManager.conditionalManagerInstance().deregister(c);
                     } else if (names[0].equals("Section"))  {
                         jmri.Section sec = InstanceManager.sectionManagerInstance().getBySystemName(names[2]);
                         if (sec == null) {
                             sec = InstanceManager.sectionManagerInstance().getBySystemName(names[1]);
                         }
                         InstanceManager.sectionManagerInstance().deregister(sec);
                     } else if (names[0].equals("Block"))  {
                         jmri.Block b = InstanceManager.blockManagerInstance().getBySystemName(names[2]);
                         if (b == null) {
                             b = InstanceManager.blockManagerInstance().getBySystemName(names[1]);
                         }
                         InstanceManager.blockManagerInstance().deregister(b);
                     }
                     model.remove(index);
                     n.remove(index);
                     index--;
                 }
                 index++;
                 if (index >= model.getSize()) {
                     index = model.getSize()-1;
                 }
                 if (index >= 0) {
                     l.setSelectedIndex(index);
                 }
             }
        }
        JScrollPane scrollPane = new JScrollPane(list);
        button.addActionListener(new SearchListener(list, names));
        button.setMaximumSize(button.getPreferredSize());
        makeDialog(scrollPane, button, parent);
    }

    /**
    *  Find orphaned elements in the various Manager Objects
    */
    public static void findEmptyPressed(Frame parent) {
        Vector <String> display = new Vector<String>();
        Vector <String> names = new Vector<String>();

        log.debug("findEmptyPressed");
        Iterator<String> iter = InstanceManager.conditionalManagerInstance().getSystemNameList().iterator();
        jmri.ConditionalManager cm = InstanceManager.conditionalManagerInstance();
        while (iter.hasNext()) {
            String name = iter.next();
            Conditional c = cm.getBySystemName(name);
            if (c != null) {
                ArrayList <ConditionalVariable> variableList = c.getCopyOfStateVariables();
                if (variableList.size()==0) {
                    String userName = c.getUserName();
                    display.add(MessageFormat.format(rbm.getString("OrphanName"), 
                                       new Object[]{"Conditional", userName, name}));
                    names.add( name);
                }
            }
        }
        DefaultListModel listModel = new DefaultListModel();
        for (int i=0; i<display.size(); i++)  {
            listModel.addElement(display.get(i));
        }
        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JButton button = new JButton(rbm.getString("DeleteButton"));
        button.setToolTipText(rbm.getString("OrphanDeleteHint"));

        class EmptyListener implements ActionListener {
             JList l;
             Vector<String> n;
             EmptyListener(JList list, Vector<String> name) {
                 l = list;
                 n = name;
             }
             public void actionPerformed(ActionEvent e) {
                 int index = l.getMaxSelectionIndex();
                 if (index < 0)  {
                     javax.swing.JOptionPane.showMessageDialog(null,
                             rbm.getString("OrphanDeleteHint"),
                             rbm.getString("DeleteTitle"),
                             javax.swing.JOptionPane.INFORMATION_MESSAGE);
                     return;
                 }
                 int min =l.getMinSelectionIndex();
                 DefaultListModel model = (DefaultListModel)l.getModel();
                 while (index>=min) {
                     String[] names = getTypeAndNames(n.get(index));
                     model.remove(index);
                     Conditional c = InstanceManager.conditionalManagerInstance().getBySystemName(names[2]);
                     if (c != null) {
                         Logix x = InstanceManager.conditionalManagerInstance().getParentLogix(names[2]);
                         if (x != null) {
                             x.deActivateLogix();
                             x.deleteConditional(names[2]);
                             x.activateLogix();
                         } else {
                             InstanceManager.conditionalManagerInstance().deregister(c);
                         }
                         n.remove(index);
                         index--;
                     }
                 }
                 index++;
                 if (index >= model.getSize()) {
                     index = model.getSize()-1;
                 }
                 if (index >= 0) {
                     l.setSelectedIndex(index);
                 }
             }
        }
        JScrollPane scrollPane = new JScrollPane(list);
        button.addActionListener(new EmptyListener(list, names));
        button.setMaximumSize(button.getPreferredSize());
        makeDialog(scrollPane, button, parent);
    }

    /**
    *  Find type of element and its names from a name that may be a user name
    * or a system name.   (Maybe this can be done at a generic manager level, but there
    * seem to be two kinds of implemetation of Managers and I don't know the which is the
    * preferred kind or why they need to be different.) 
    */
    @SuppressWarnings("null")
	static String[] getTypeAndNames(String name) {
        String userName = name.trim();
        String sysName = userName.toUpperCase();
        boolean found = false;

        jmri.SensorManager sensorManager = InstanceManager.sensorManagerInstance();
        Sensor sen = sensorManager.getBySystemName(sysName);
        if ( sen!=null ) {
            userName = sen.getUserName();
            found = true;
        } else {
            sen = sensorManager.getByUserName(userName);
            if ( sen!=null ) {
                sysName = sen.getSystemName();
                found = true;
            }
        }
        if (found) {
            return (new String[] {"Sensor", userName, sysName,
                                  Integer.toString(sen.getNumPropertyChangeListeners())});
        }
        jmri.TurnoutManager turnoutManager = InstanceManager.turnoutManagerInstance();
        Turnout t = turnoutManager.getBySystemName(sysName);
        if ( t!=null ) {
            userName = t.getUserName();
            found = true;
        } else {
            t = turnoutManager.getByUserName(userName);
            if ( t!=null ) {
                sysName = t.getSystemName();
                found = true;
            }
        }
        if (found) {
            return (new String[] {"Turnout", userName, sysName, 
                                  Integer.toString(t.getNumPropertyChangeListeners())});
        }
        jmri.LightManager lightManager = InstanceManager.lightManagerInstance();
        Light l = lightManager.getBySystemName(sysName);
        if ( l!=null ) {
            userName = l.getUserName();
            found = true;
        } else {
            l = lightManager.getByUserName(userName);
            if ( l!=null ) {
                sysName = l.getSystemName();
                found = true;
            }
        }
        if (found) {
            return (new String[] {"Light", userName, sysName,
                                  Integer.toString(l.getNumPropertyChangeListeners())});
        }
        jmri.SignalHeadManager signalManager = InstanceManager.signalHeadManagerInstance();
        SignalHead sh = signalManager.getBySystemName(sysName);
        if ( sh!=null ) {
            userName = sh.getUserName();
            found = true;
        } else {
            sh = signalManager.getByUserName(userName);
            if ( sh!=null ) {
                sysName = sh.getSystemName();
                found = true;
            }
        }
        if (found) {
            return (new String[] {"SignalHead", userName, sysName,
                                  Integer.toString(sh.getNumPropertyChangeListeners())});
        }
        jmri.ConditionalManager cm = InstanceManager.conditionalManagerInstance();
        Conditional c = cm.getBySystemName(sysName);
        if ( c!=null ) {
            userName = c.getUserName();
            found = true;
        } else {
            c = cm.getByUserName(userName);
            if ( c!=null ) {
                sysName = c.getSystemName();
                found = true;
            }
        }
        if (found) {
            return (new String[] {"Conditional", userName, sysName, 
                                  Integer.toString(c.getNumPropertyChangeListeners())});
        }
        jmri.BlockManager blockManager = InstanceManager.blockManagerInstance();
        jmri.Block b = blockManager.getBySystemName(sysName);
        if ( b!=null ) {
            userName = b.getUserName();
            found = true;
        } else {
            b = blockManager.getByUserName(userName);
            if ( b!=null ) {
                sysName = b.getSystemName();
                found = true;
            }
        }
        if (found) {
            return (new String[] {"Block", userName, sysName,
                                  Integer.toString(b.getNumPropertyChangeListeners())});
        }
        jmri.SectionManager sectionManager = InstanceManager.sectionManagerInstance();
        jmri.Section sec = sectionManager.getBySystemName(sysName);
        if ( sec!=null ) {
            userName = sec.getUserName();
            found = true;
        } else {
            sec = sectionManager.getByUserName(userName);
            if ( sec!=null ) {
                sysName = sec.getSystemName();
                found = true;
            }
        }
        if (found) {
            return (new String[] {"Section", userName, sysName, 
                                  Integer.toString(sec.getNumPropertyChangeListeners())});
        }
        return (new String[] {"", userName, sysName});
    }

    static boolean search(String name, JTextArea text) {
        String[] names = getTypeAndNames(name);
        if (names[0].length() == 0)
        {
            if (text != null) {
                text.append(MessageFormat.format(rbm.getString("ElementNotFound"), (Object[])names));
            }
        }
        if (text != null) {
            text.append(MessageFormat.format(rbm.getString("ReferenceFollows"), (Object[])names));
        }
        String sysName = names[2];
        String userName = names[1];
        int referenceCount = 0;
        String tempText = null;
        boolean found = false;
        boolean empty = true;
        // search for references
		Iterator<String> iter1 = InstanceManager.logixManagerInstance().getSystemNameList().iterator();
		while (iter1.hasNext()) {
			// get the next Logix
			String sName = iter1.next();
			Logix x = InstanceManager.logixManagerInstance().getBySystemName(sName);
			if (x==null) {
				log.error("Error getting Logix  - " + sName);
				break;
			}
            String uName = x.getUserName();
            tempText = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { "", rbm.getString("Logix"), uName,  sName});
            empty = false;
            for (int i=0; i<x.getNumConditionals(); i++)  {
                sName = x.getConditionalByNumberOrder(i);
                if (sName == null) {
					log.error("Null conditional system name");
                    break;
                }
				Conditional c = InstanceManager.conditionalManagerInstance().getBySystemName(sName);
				if (c == null) {
					log.error("Invalid conditional system name - " + sName);
                    break;
				}
                uName = c.getUserName();
                tempText = tempText + MessageFormat.format(rbm.getString("ReferenceTitle"),
                                new Object[] { "\t", rbm.getString("Conditional"), uName,  sName});
                if (sName.equals(sysName) || uName.equals(userName))  {
                    tempText = tempText + MessageFormat.format("\t",rbm.getString("ConditionalReference"));
                    found = true;
                    referenceCount++;
                }
                ArrayList <ConditionalVariable> variableList = c.getCopyOfStateVariables();
                if (variableList.size()==0) {
                    tempText = tempText + MessageFormat.format(rbm.getString("NoVariables"),
                                    new Object[] {c.getUserName(), c.getSystemName(),
                                         x.getUserName(), x.getSystemName()});
                } else for (int k=0; k<variableList.size(); k++)  {
                    ConditionalVariable v = variableList.get(k);
                    if (v.getName().equals(sysName) || v.getName().equals(userName))
                    {
                        tempText = tempText + MessageFormat.format(rbm.getString("VariableReference"),
                                        new Object[] { "\t\t", v.getTestTypeString(), v.getDataString()});
                        found = true;
                        referenceCount++;
                    }
                }
                ArrayList <ConditionalAction> actionList = c.getCopyOfActions();
                for (int k=0; k<actionList.size(); k++) {
                    ConditionalAction a = actionList.get(k);
                    if (a.getDeviceName().equals(sysName) || a.getDeviceName().equals(userName))
                    {
                        tempText = tempText + MessageFormat.format(
                                rbm.getString("ActionReference"),
                                new Object[] {"\t\t", a.getTypeString(), a.getOptionString(), a.getActionDataString()});
                        found = true;
                        referenceCount++;
                    }
                }
            }
            if (text != null && !empty) {
                if (!found)
                    text.append(MessageFormat.format(rbm.getString("NoReference"), "Logix"));
                else {
                    text.append(tempText);
                    text.append("\n");
                    found = false;
                }
            }
        }
        tempText = null;
        found = false;
        empty = true;
        jmri.RouteManager routeManager = InstanceManager.routeManagerInstance();
        iter1 = routeManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Route r = routeManager.getBySystemName(sName);
            if (r==null) {
                log.error("Error getting Route  - "+sName);
                break;
            }
            String uName = r.getUserName();
            tempText = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Route"), uName,  sName});
            empty = false;
            for (int i=0; i<jmri.Route.MAX_CONTROL_SENSORS; i++) {
                sName = r.getRouteSensorName(i);
                if (sName != null)  {
                    if (sName.equals(sysName) || sName.equals(userName))  {
                        tempText = tempText + MessageFormat.format(rbm.getString("ControlReference"),
                                        rbm.getString("Sensor"));
                        found = true;
                        referenceCount++;
                    }
                }
            }
            sName = r.getTurnoutsAlignedSensor();
            if (sName.equals(sysName) || sName.equals(userName))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ControlReference"),
                                rbm.getString("Sensor"));
                found = true;
                referenceCount++;
            }
            sName = r.getControlTurnout();
            if (sName != "" && (sName.equals(sysName) || sName.equals(userName)))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ControlReference"),
                                rbm.getString("Turnout"));
                found = true;
                referenceCount++;
            }
            sName = r.getLockControlTurnout();
            if (sName != "" && (sName.equals(sysName) || sName.equals(userName)))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ControlReference"),
                                rbm.getString("Turnout"));
                found = true;
                referenceCount++;
            }
            for (int i=0; i<r.getNumOutputTurnouts(); i++)
            {
                sName = r.getOutputTurnoutByIndex(i);
                if (sName != null) {
                    if (sName.equals(sysName) || sName.equals(userName))  {
                        tempText = tempText + MessageFormat.format(rbm.getString("OutputReference"),
                                        rbm.getString("Turnout"));
                        found = true;
                        referenceCount++;
                    }
                }
            }
            for (int i=0; i<r.getNumOutputSensors(); i++)
            {
                sName = r.getOutputSensorByIndex(i);
                if (sName != null) {
                    if (sName.equals(sysName) || sName.equals(userName))  {
                        tempText = tempText + MessageFormat.format(rbm.getString("OutputReference"),
                                        rbm.getString("Sensor"));
                        found = true;
                        referenceCount++;
                    }
                }
            }
        }
        if (text != null && !empty) {
            if (!found)
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Route"));
            else {
                text.append(tempText);
                text.append("\n");
                found = false;
            }
        }
        tempText = null;
        found = false;
        empty = true;
        jmri.TransitManager transitManager = InstanceManager.transitManagerInstance();
        iter1 = transitManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Transit transit = transitManager.getBySystemName(sName);
            if (transit==null) {
                log.error("Error getting Transit - "+sName);
                break;
            }
            String uName = transit.getUserName();
            tempText = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Transit"), uName,  sName});
            empty = false;
            ArrayList<jmri.TransitSection> sectionList = transit.getTransitSectionList();
            for (int i=0; i<sectionList.size(); i++) {
                jmri.TransitSection transitSection = sectionList.get(i);
                jmri.Section section = transitSection.getSection();
                uName = section.getUserName();
                sName = section.getSystemName();
                tempText = tempText + MessageFormat.format(rbm.getString("ReferenceTitle"),
                                new Object[] { "\t", rbm.getString("TransitSection"), uName, sName});
                if (sName.equals(sysName) || uName.equals(userName))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("SectionReference"),"\t\t");
                    found = true;
                    referenceCount++;
                }
                sName = section.getForwardBlockingSensorName();
                if (sName.equals(sysName) || sName.equals(userName))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("ForwardBlocking"),"\t\t");
                    found = true;
                    referenceCount++;
                }
                sName = section.getForwardStoppingSensorName();
                if (sName.equals(sysName) || sName.equals(userName))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("ForwardStopping"),"\t\t");
                    found = true;
                    referenceCount++;
                }
                sName = section.getReverseBlockingSensorName();
                if (sName.equals(sysName) || sName.equals(userName))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("ReverseBlocking"),"\t\t");
                    found = true;
                    referenceCount++;
                }
                sName = section.getReverseStoppingSensorName();
                if (sName.equals(sysName) || sName.equals(userName))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("ReverseStopping"),"\t\t");
                    found = true;
                    referenceCount++;
                }
                ArrayList<jmri.Block> blockList = section.getBlockList();

                for (int k=0; k<blockList.size(); k++) {
                    jmri.Block block = blockList.get(k);
                    if (block==null) {
                        log.error("Error getting Block - "+sName);
                    } else {
                        sName = block.getSystemName();
                        uName = block.getUserName();
                        tempText = tempText + MessageFormat.format(rbm.getString("ReferenceTitle"),
                                        new Object[] { "\t\t", rbm.getString("Block"), uName, sName});
                        if (sName.equals(sysName) || uName.equals(userName))  {
                            tempText = tempText + MessageFormat.format(rbm.getString("BlockReference"),"\t\t");
                            found = true;
                            referenceCount++;
                        }
                        Sensor sensor = block.getSensor();
                        if (sensor == null) {
                            tempText = tempText + MessageFormat.format(rbm.getString("BlockNoSensor"),"\t\t\t");
                        } else {
                            sName = sensor.getSystemName();
                            uName = sensor.getUserName();
                            if (sName.equals(sysName) || uName.equals(userName))  {
                                tempText = tempText + MessageFormat.format(rbm.getString("BlockSensor"),"\t\t\t");
                                found = true;
                                referenceCount++;
                            }
                        }
                    }
                }
            }
            if (text != null && !empty) {
                if (!found)
                    text.append(MessageFormat.format(rbm.getString("NoReference"), "Transit"));
                else {
                    text.append(tempText);
                    text.append("\n");
                    found = false;
                }
            }
        }
        if (text != null)
            text.append(rbm.getString("NestMessage"));
        tempText = null;
        found = false;
        empty = true;
        jmri.SectionManager sectionManager = InstanceManager.sectionManagerInstance();
        java.util.List<String> sysNameList = sectionManager.getSystemNameList();
        
        transitManager = InstanceManager.transitManagerInstance();
        iter1 = transitManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Transit transit = transitManager.getBySystemName(sName);
            if (transit!=null) {
                ArrayList<jmri.TransitSection> sectionList = transit.getTransitSectionList();
                for (int i=0; i<sectionList.size(); i++) {
                    jmri.TransitSection transitSection = sectionList.get(i);
                    jmri.Section section = transitSection.getSection();
                    sysNameList.remove(section.getSystemName());
                }
            }
        }       
        iter1 = sysNameList.iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Section section = sectionManager.getBySystemName(sName);
            if (section==null) {
                log.error("Error getting Section - "+sName);
                break;
            }
            String uName = section.getUserName();
            tempText = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Section"), uName,  sName});
            empty = false;
            if (sName.equals(sysName) || uName.equals(userName))  {
                tempText = tempText + MessageFormat.format(rbm.getString("SectionReference"),"\t\t");

                found = true;
                referenceCount++;
            }
            sName = section.getForwardBlockingSensorName();
            if (sName.equals(sysName) || sName.equals(userName))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ForwardBlocking"),"\t");
                found = true;
                referenceCount++;
            }
            sName = section.getForwardStoppingSensorName();
            if (sName.equals(sysName) || sName.equals(userName))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ForwardStopping"),"\t");
                found = true;
                referenceCount++;
            }
            sName = section.getReverseBlockingSensorName();
            if (sName.equals(sysName) || sName.equals(userName))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ReverseBlocking"),"\t");
                found = true;
                referenceCount++;
            }
            sName = section.getReverseStoppingSensorName();
            if (sName.equals(sysName) || sName.equals(userName))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ReverseStopping"),"\t");
                found = true;
                referenceCount++;
            }

            ArrayList<jmri.Block> blockList = section.getBlockList();
            for (int k=0; k<blockList.size(); k++) {
                jmri.Block block = blockList.get(k);
                if (block==null) {
                    log.error("Error getting Block - "+sName);
                } else {
                    sName = block.getSystemName();
                    uName = block.getUserName();
                    tempText = tempText + MessageFormat.format(rbm.getString("ReferenceTitle"),
                                    new Object[] { "\t", rbm.getString("Block"), uName, sName});
                    if (sName.equals(sysName) || (uName!= null && uName.equals(userName)))  {
                        tempText = tempText + MessageFormat.format(rbm.getString("BlockReference"),"\t");
                        found = true;
                        referenceCount++;
                    }
                    Sensor sensor = block.getSensor();
                    if (sensor == null) {
                        tempText = tempText + MessageFormat.format(rbm.getString("BlockNoSensor"),"\t\t");
                    } else {
                        sName = sensor.getSystemName();
                        uName = sensor.getUserName();
                        if (sName.equals(sysName) || uName.equals(userName))  {
                            tempText = tempText + MessageFormat.format(rbm.getString("BlockSensor"),"\t\t");
                            found = true;
                            referenceCount++;
                        }
                    }
                }
            }
            if (text != null && !empty) {
                if (!found)
                    text.append(MessageFormat.format(rbm.getString("NoReference"), "Section"));
                else {
                    text.append(tempText);
                    text.append("\n");
                    found = false;
                }
            }
        }
        tempText = null;
        found = false;
        empty = true;
        jmri.BlockManager blockManager = InstanceManager.blockManagerInstance();
        sysNameList = blockManager.getSystemNameList();

        sectionManager = InstanceManager.sectionManagerInstance();
        iter1 = sectionManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Section section = sectionManager.getBySystemName(sName);
            if (section!=null) {
                sysNameList.remove(section.getBlockList());
            }
        }
        iter1 = sysNameList.iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Block b = blockManager.getBySystemName(sName);
            if (b==null) {
                log.error("Error getting Block - "+sName);
            } else {
                String uName = b.getUserName();
                tempText = MessageFormat.format(rbm.getString("ReferenceTitle"),
                                new Object[] { " ", rbm.getString("Block"), uName,  sName});
                empty = false;
                if (sName.equals(sysName) || (uName !=null && uName.equals(userName)))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("BlockReference"),"\t");
                    found = true;
                    referenceCount++;
                }
                jmri.Sensor s = b.getSensor();
                if (s == null) {
                    MessageFormat.format(rbm.getString("BlockNoSensor"),"\t");
                } else {
                    sName = s.getSystemName();
                    uName = s.getUserName();
                    if (sName.equals(sysName) || uName.equals(userName))  {
                        tempText = tempText + MessageFormat.format(rbm.getString("BlockSensor"),"\t");
                        found = true;
                        referenceCount++;
                    }
                }
            }
        }       
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        iter1 = lbm.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.jmrit.display.layoutEditor.LayoutBlock lb = lbm.getBySystemName(sName);
            if (lb==null) {
                log.error("Error getting LayoutBlock - "+sName);
                break;
            }
            String uName = lb.getUserName();
            tempText = tempText + MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("LayoutBlock"), uName,  sName});
            empty = false;
            jmri.Sensor s = lb.getOccupancySensor();
            if (s == null) {
                tempText = tempText + MessageFormat.format(rbm.getString("BlockNoSensor"),"\t");
            } else {
                sName = s.getSystemName();
                uName = s.getUserName();
                if (sName.equals(sysName) || uName.equals(userName))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("OccupancySensor"),"\t");
                    found = true;
                    referenceCount++;
                }
                if (text != null && !empty) {
                    if (!found)
                        text.append(MessageFormat.format(rbm.getString("NoReference"), "Block"));
                    else {
                        text.append(tempText);
                        text.append("\n");
                        found = false;
                    }
                }
            }
        }
        tempText = null;
        found = false;
        empty = true;
        java.util.Enumeration<BlockBossLogic> enumeration = BlockBossLogic.entries();
        while (enumeration.hasMoreElements()) {
            // get the next Logix
            BlockBossLogic bbl = enumeration.nextElement();
            String sName = bbl.getName();
            String uName = bbl.getDrivenSignal();
            tempText = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("BlockBossLogic"), uName, sName});
            empty = false;
            if (uName.equals(sysName) || uName.equals(userName) || sName.equals(sysName) || sName.equals(userName)) {
                tempText = tempText + MessageFormat.format(rbm.getString("SignalReference"),"\t");
                found = true;
                referenceCount++;
            }
            sName = bbl.getSensor1();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getSensor2();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName))  {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getSensor3();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getSensor4();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getSensor5();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getTurnout();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchTurnoutReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSignal1();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    MessageFormat.format(rbm.getString("WatchSignalReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSignal1Alt();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    MessageFormat.format(rbm.getString("WatchSignalReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSignal2();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    MessageFormat.format(rbm.getString("WatchSignalReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSignal2Alt();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSignalReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSensor1();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSensor1Alt();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    tempText = tempText + MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSensor2();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            sName = bbl.getWatchedSensor2Alt();
            if (sName != null) {
                if (sName.equals(sysName) || sName.equals(userName)) {
                    MessageFormat.format(rbm.getString("WatchSensorReference"),"\t");
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && !empty) {
                if (!found)
                    text.append(MessageFormat.format(rbm.getString("NoReference"), "BlockBossLogic"));
                else {
                    text.append(tempText);
                    text.append("\n");
                    found = false;
                }
            }
        }
        tempText = null;
        found = false;
        empty = true;
        jmri.ConditionalManager conditionalManager = InstanceManager.conditionalManagerInstance();
        sysNameList = conditionalManager.getSystemNameList();
        iter1 = InstanceManager.logixManagerInstance().getSystemNameList().iterator();
        while (iter1.hasNext()) {
            String sName = iter1.next();
			Logix x = InstanceManager.logixManagerInstance().getBySystemName(sName);
            for (int i=0; i<x.getNumConditionals(); i++)  {
                sName = x.getConditionalByNumberOrder(i);
                sysNameList.remove(sName);
            }
        }
        iter1 = sysNameList.iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Conditional c = conditionalManager.getBySystemName(sName);
            if (c==null) {
                log.error("Error getting Condition - "+sName);
                break;
            }
            String uName = c.getUserName();
            tempText = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Conditional"), uName,  sName});
            empty = false;
            if (sName.equals(sysName) || uName.equals(userName))  {
                tempText = tempText + MessageFormat.format(rbm.getString("ConditionalReference"),"\t");
                found = true;
                //referenceCount++; Don't count, this conditional is orphaned by logix(es)
            }
            ArrayList <ConditionalVariable> variableList = c.getCopyOfStateVariables();
            if (variableList.size()==0) {
                tempText = tempText + MessageFormat.format(rbm.getString("Warn5"),
                                new Object[] {c.getUserName(), c.getSystemName()});
            } else for (int k=0; k<variableList.size(); k++)  {
                ConditionalVariable v = variableList.get(k);
                if (v.getName().equals(sysName) || v.getName().equals(userName))
                {
                    tempText = tempText + MessageFormat.format(rbm.getString("VariableReference"),
                                    new Object[] { "\t", v.getTestTypeString(), v.getDataString()});
                    found = true;
                    //referenceCount++; Don't count, this conditional is orphaned by logix(es)
                }
            }
            ArrayList <ConditionalAction> actionList = c.getCopyOfActions();
            for (int k=0; k<actionList.size(); k++) {
                ConditionalAction a = actionList.get(k);
                if (a.getDeviceName().equals(sysName) || a.getDeviceName().equals(userName))
                {
                    tempText = tempText + MessageFormat.format(rbm.getString("ActionReference"),
                                    new Object[] { "\t", a.getTypeString(), 
                                        a.getOptionString(), a.getActionDataString()});
                    found = true;
                    //referenceCount++; Don't count, this conditional is orphaned by logix(es)
                }
            }
        }
        if (text != null && !empty) {
            if (!found)
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Conditional"));
            else {
                text.append(tempText);
                text.append("\n");
                found = false;
            }
        }
        found = false;
        empty = true;
        ArrayList<jmri.jmrit.display.Editor> panelList = jmri.jmrit.display.PanelMenu.instance().getEditorPanelList();
        for (int i=0; i<panelList.size(); i++) {
            jmri.jmrit.display.Editor panelEditor = panelList.get(i);
            name = panelEditor.getTitle();
            if (text != null) {
                text.append(MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Panel"), name, name}));
            }
            List <Positionable> contents = panelEditor.getContents();
            for (int k=0; k<contents.size(); k++) {
                Positionable o = contents.get(k);
                if (o.getClass().getName().equals("jmri.jmrit.display.SensorIcon")) {
                    name = ((jmri.jmrit.display.SensorIcon)o).getSensor().getSystemName();
                    if (name.equals(sysName))
                    {
                        if (text != null) {
                            text.append(MessageFormat.format(rbm.getString("PanelReference"),
                                        new Object[] { "\t", rbm.getString("Sensor")}));
                        }
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.TurnoutIcon")) {
                    name = ((jmri.jmrit.display.TurnoutIcon)o).getTurnout().getSystemName();
                    if (name.equals(sysName))
                    {
                        if (text != null) {
                            text.append(MessageFormat.format(rbm.getString("PanelReference"),
                                        new Object[] { "\t", rbm.getString("Turnout")}));
                        }
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.SignalHeadIcon")) {
                    name = ((jmri.jmrit.display.SignalHeadIcon)o).getSignalHead().getSystemName();
                    if (name.equals(sysName))
                    {
                        if (text != null) {
                            text.append(MessageFormat.format(rbm.getString("PanelReference"),
                                        new Object[] { "\t", rbm.getString("SignalHead")}));
                        }
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.MultiSensorIcon")) {
                    jmri.jmrit.display.MultiSensorIcon msi = (jmri.jmrit.display.MultiSensorIcon)o;
                    for (int j=0; j<msi.getNumEntries(); j++)  {
                        name = msi.getSensorName(j);
                        if (name.equals(sysName))
                        {
                            if (text != null) {
                                text.append(MessageFormat.format(rbm.getString("PanelReference"),
                                            new Object[] { "\t", rbm.getString("MultiSensor")}));
                            }
                            found = true;
                            referenceCount++;
                        }
                    }
                }
            }
        }
        if (text != null) {
            if (!found)
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Panel"));
            else {
                text.append(tempText);
                text.append("\n");
            }
            if (referenceCount == 0) {
                text.append(MessageFormat.format(rbm.getString("Orphan"), (Object[])names));
            } else {
                text.append(MessageFormat.format(rbm.getString("ReferenceFound"), 
                                       new Object[] {Integer.valueOf(referenceCount), userName, sysName}));
            }
        }
        if (names[0] != null && Integer.parseInt(names[3]) > referenceCount)
        {
            log.warn(MessageFormat.format(rbm.getString("OrphanName"), (Object[])names)+" has "+names[3]+
                      " listeners installed. Only "+referenceCount+ " references found.");
        }
        return (referenceCount > 0);
    }

    static void makeDialog(Component component, Component button, Frame parent) {
        JDialog dialog = new JDialog(parent, rbm.getString("DialogTitle"), true);
        JButton ok = new JButton(rbm.getString("OkButton"));
        class myListener implements ActionListener {
             java.awt.Window _w;
             myListener(java.awt.Window w) {
                 _w = w;
             }
             public void actionPerformed(ActionEvent e) {
                 _w.dispose();
             }
        }
        ok.addActionListener(new myListener(dialog));
        ok.setMaximumSize(ok.getPreferredSize());

        java.awt.Container contentPane = dialog.getContentPane();  
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(component, BorderLayout.CENTER);
        contentPane.add(Box.createVerticalStrut(5));
        contentPane.add(Box.createVerticalGlue());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(ok);
        if (button != null)  {
            panel.add(Box.createHorizontalStrut(5));
            panel.add(button);
        }
        contentPane.add(panel, BorderLayout.SOUTH);
        class myAdapter extends java.awt.event.WindowAdapter {
             java.awt.Window _w;
             myAdapter(java.awt.Window w) {
                 _w = w;
             }
             public void windowClosing(java.awt.event.WindowEvent e) {
                 _w.dispose();
             }
        }
        dialog.addWindowListener( new myAdapter(dialog));
        dialog.setLocationRelativeTo(parent);
        dialog.pack();
        dialog.setVisible(true);
    }

	static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(Maintenance.class.getName());
}

