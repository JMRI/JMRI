
package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @version $Revision$
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
        makeDialog(scrollPane, null, parent, rbm.getString("CrossReferenceTitle"));
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
        makeDialog(scrollPane, button, parent, rbm.getString("OrphanTitle"));
    }

    /**
    *  Find useless conditionals in the various Manager Objects
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
                         }
                         InstanceManager.conditionalManagerInstance().deregister(c);
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
        makeDialog(scrollPane, button, parent, rbm.getString("EmptyConditionalTitle"));
    }

    /**
    *  Find type of element and its names from a name that may be a user name
    * or a system name.   (Maybe this can be done at a generic manager level, but there
    * seem to be two kinds of implemetation of Managers and I don't know the which is the
    * preferred kind or why they need to be different.)
    *
    * Searches each Manager for a reference to the "name" 
    * returns 4 element String: {Type, userName, sysName, numListeners}       
    */
    @SuppressWarnings("null")
	static String[] getTypeAndNames(String name) {
        String userName = name.trim();
        String sysName = userName;
//        String sysName = userName.toUpperCase();
        boolean found = false;
        if (log.isDebugEnabled()) log.debug("getTypeAndNames for \""+name+"\"");

        jmri.SensorManager sensorManager = InstanceManager.sensorManagerInstance();
        Sensor sen = sensorManager.getBySystemName(sysName);
        if ( sen!=null ) {
            userName = sen.getUserName();
            found = true;
        } else {
            sen = sensorManager.getBySystemName(userName.toUpperCase());
            if (sen!=null) {
                sysName = sen.getSystemName();
                userName = sen.getUserName();
                found = true;
            } else {
                sen = sensorManager.getByUserName(userName);
                if ( sen!=null ) {
                    sysName = sen.getSystemName();
                    found = true;
                }
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
            t = turnoutManager.getBySystemName(userName.toUpperCase());
            if (t!=null) {
                sysName = t.getSystemName();
                userName = t.getUserName();
                found = true;
            } else {
                t = turnoutManager.getByUserName(userName);
                if ( t!=null ) {
                    sysName = t.getSystemName();
                    found = true;
                }
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
            l = lightManager.getBySystemName(userName.toUpperCase());
            if (l!=null) {
                sysName = l.getSystemName();
                userName = l.getUserName();
                found = true;
            } else {
                l = lightManager.getByUserName(userName);
                if ( l!=null ) {
                    sysName = l.getSystemName();
                    found = true;
                }
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
            sh = signalManager.getBySystemName(userName.toUpperCase());
            if (sh!=null) {
                sysName = sh.getSystemName();
                userName = sh.getUserName();
                found = true;
            } else {
                sh = signalManager.getByUserName(userName);
                if ( sh!=null ) {
                    sysName = sh.getSystemName();
                    found = true;
                }
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
            c = cm.getBySystemName(userName.toUpperCase());
            if (c!=null) {
                sysName = c.getSystemName();
                userName = c.getUserName();
                found = true;
            } else {
                c = cm.getByUserName(userName);
                if ( c!=null ) {
                    sysName = c.getSystemName();
                    found = true;
                }
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
            b = blockManager.getBySystemName(userName.toUpperCase());
            if (b!=null) {
                sysName = b.getSystemName();
                userName = b.getUserName();
                found = true;
            } else {
                b = blockManager.getByUserName(userName);
                if ( b!=null ) {
                    sysName = b.getSystemName();
                    found = true;
                }
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
            sec = sectionManager.getBySystemName(userName.toUpperCase());
            if (sec!=null) {
                sysName = sec.getSystemName();
                userName = sec.getUserName();
                found = true;
            } else {
                sec = sectionManager.getByUserName(userName);
                if ( sec!=null ) {
                    sysName = sec.getSystemName();
                    found = true;
                }
            }
        }
        if (found) {
            return (new String[] {"Block", userName, sysName, 
                                  Integer.toString(sec.getNumPropertyChangeListeners())});
        }
        log.warn(" No type found for "+userName+" ("+sysName+").");

        jmri.jmrit.logix.OBlockManager oBlockManager = InstanceManager.oBlockManagerInstance();
        jmri.jmrit.logix.OBlock blk = oBlockManager.getBySystemName(sysName);
        if ( sec!=null ) {
            userName = blk.getUserName();
            found = true;
        } else {
            blk = oBlockManager.getBySystemName(userName.toUpperCase());
            if (blk!=null) {
                sysName = blk.getSystemName();
                userName = blk.getUserName();
                found = true;
            } else {
                blk = oBlockManager.getByUserName(userName);
                if ( blk!=null ) {
                    sysName = blk.getSystemName();
                    found = true;
                }
            }
        }
        if (found) {
            return (new String[] {"OBlock", userName, sysName, 
                                  Integer.toString(blk.getNumPropertyChangeListeners())});
        }
        log.warn(" No type found for "+userName+" ("+sysName+").");

        return (new String[] {"", userName, sysName, "0"});
    }

    static boolean testName(String name, boolean found, String[] names, String line1, String line2, 
                            String line, StringBuffer tempText) {
        if (name==null) {
            return false;
        }
        String sysName = names[2];
        String userName = names[1];
        if (name.equals(sysName) || name.equals(userName))
        {
            if (!found) {
                if (line1!=null) {
                    tempText.append(line1);
                }
                if (line2!=null) {
                    tempText.append(line2);
                }
            }
            tempText.append(line);
            return true;
        }
        return false;
    }

    //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    static boolean search(String name, JTextArea text) {
        String[] names = getTypeAndNames(name);
        if (log.isDebugEnabled()) log.debug("search for "+name+" as "+names[0]+" \""+names[1]+"\" ("+names[2]+")");
        if (names[0].length() == 0)
        {
            if (text != null) {
                text.append(MessageFormat.format(rbm.getString("ElementNotFound"), (Object[])names));
                return false;
            }
        }
        if (text != null) {
            text.append(MessageFormat.format(rbm.getString("ReferenceFollows"), (Object[])names));
        }
        String sysName = names[2];
        String userName = names[1];
        int referenceCount = 0;
        StringBuffer tempText = new StringBuffer();
        boolean found = false;
        boolean empty = true;
        // search for references among each class known to be listeners
		Iterator<String> iter1 = InstanceManager.logixManagerInstance().getSystemNameList().iterator();
		while (iter1.hasNext()) {
			// get the next Logix
			String sName = iter1.next();
			Logix x = InstanceManager.logixManagerInstance().getBySystemName(sName);
			if (x==null) {
				log.error("Error getting Logix  - " + sName);
				break;
			}
            tempText = new StringBuffer();
            String uName = x.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { "", rbm.getString("Logix"), uName,  sName});
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
                String line2 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                                new Object[] { "\t", rbm.getString("Conditional"), uName,  sName});
                String line = MessageFormat.format(rbm.getString("ConditionalReference"), "\t");
                if (sysName.equals(sName) || (userName!=null && userName.length()>0 && userName.equals(uName)) ) {
                    if (testName(sysName, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
                ArrayList <ConditionalVariable> variableList = c.getCopyOfStateVariables();
                for (int k=0; k<variableList.size(); k++)  {
                    ConditionalVariable v = variableList.get(k);
                    line = MessageFormat.format(rbm.getString("VariableReference"),
                                    new Object[] { "\t\t", v.getTestTypeString(), v.getDataString()});
                    if (testName(v.getName(), found, names, line1, line2, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
                ArrayList <ConditionalAction> actionList = c.getCopyOfActions();
                for (int k=0; k<actionList.size(); k++) {
                    ConditionalAction a = actionList.get(k);
                    line = MessageFormat.format(rbm.getString("ActionReference"),
                            new Object[] {"\t\t", a.getTypeString(), a.getOptionString(false), a.getActionDataString()});
                    if (testName(a.getDeviceName(), found, names, line1, line2, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
                if (text != null && found) {
                    text.append(tempText.toString());
                    tempText = new StringBuffer();
                    found = false;
                    empty = false;
                    line1 = null;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Logix"));
            } else {
                text.append("\n");
            }
        }

        tempText = new StringBuffer();
        found = false;
        empty = true;
        jmri.jmrit.logix.OBlockManager oBlockManager = InstanceManager.oBlockManagerInstance();
        iter1 = oBlockManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.jmrit.logix.OBlock block = oBlockManager.getBySystemName(sName);
            String uName = block.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("OBlock"), uName,  sName});
            Sensor sensor = block.getSensor();
            if (sensor != null) {
                String line = MessageFormat.format(rbm.getString("OBlockSensor"),"\t");
                if (testName(sensor.getSystemName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "OBlock"));
            } else {
                text.append("\n");
            }
        }

        tempText = new StringBuffer();
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
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Route"), uName,  sName});
            for (int i=0; i<jmri.Route.MAX_CONTROL_SENSORS; i++) {
                String line = MessageFormat.format(rbm.getString("ControlReference"),rbm.getString("Sensor"));
                if (testName(r.getRouteSensorName(i), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            String line = MessageFormat.format("TurnoutsAlignedSensor",rbm.getString("Sensor"));
            if (testName(r.getTurnoutsAlignedSensor(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("ControlReference"), rbm.getString("Turnout"));
            if (testName(r.getControlTurnout(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format("LockControlTurnout", rbm.getString("Turnout"));
            if (testName(r.getLockControlTurnout(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            for (int i=0; i<r.getNumOutputTurnouts(); i++)
            {
                line = MessageFormat.format(rbm.getString("OutputReference"), rbm.getString("Turnout"));
                if (testName(r.getOutputTurnoutByIndex(i), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            for (int i=0; i<r.getNumOutputSensors(); i++)
            {
                line = MessageFormat.format(rbm.getString("OutputReference"), rbm.getString("Sensor"));
                if (testName(r.getOutputSensorByIndex(i), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Route"));
            } else {
                text.append("\n");
            }
        }
        
        tempText = new StringBuffer();
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
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Transit"), uName,  sName});
            ArrayList<jmri.TransitSection> sectionList = transit.getTransitSectionList();
            for (int i=0; i<sectionList.size(); i++) {
                jmri.TransitSection transitSection = sectionList.get(i);
                jmri.Section section = transitSection.getSection();
                uName = section.getUserName();
                sName = section.getSystemName();
                String line2 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                                new Object[] { "\t", rbm.getString("TransitSection"), uName, sName});
                if (sName.equals(sysName) || uName.equals(userName))  {
                    tempText.append(line1);
                    tempText.append(line2);
                    tempText.append(MessageFormat.format(rbm.getString("SectionReference"),"\t\t"));
                    found = true;
                    referenceCount++;
                }
                String line = MessageFormat.format(rbm.getString("ForwardBlocking"),"\t\t");
                if (testName(section.getForwardBlockingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                line = MessageFormat.format(rbm.getString("ForwardStopping"),"\t\t");
                if (testName(section.getForwardStoppingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                line = MessageFormat.format(rbm.getString("ReverseBlocking"),"\t\t");
                if (testName(section.getReverseBlockingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                line = MessageFormat.format(rbm.getString("ReverseStopping"),"\t\t");
                if (testName(section.getReverseStoppingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                ArrayList<jmri.Block> blockList = section.getBlockList();

                for (int k=0; k<blockList.size(); k++) {
                    jmri.Block block = blockList.get(k);
                    sName = block.getSystemName();
                    uName = block.getUserName();
                    tempText.append(MessageFormat.format(rbm.getString("ReferenceTitle"),
                                    new Object[] { "\t\t", rbm.getString("Block"), uName, sName}));
                    if (sName.equals(sysName) || uName.equals(userName))  {
                        tempText.append(MessageFormat.format(rbm.getString("BlockReference"),"\t\t"));
                        found = true;
                        referenceCount++;
                    }
                    Sensor sensor = block.getSensor();
                    if (sensor != null) {
                        line = MessageFormat.format(rbm.getString("BlockSensor"),"\t\t");
                        if (testName(sensor.getSystemName(), found, names, line1, line2, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                }
                if (text != null && found) {
                    text.append(tempText.toString());
                    tempText = new StringBuffer();
                    found = false;
                    empty = false;
                    line1 = null;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Transit"));
            } else {
                text.append("\n");
            }
        }

//        if (text != null) {
//            text.append(rbm.getString("NestMessage"));
//        }
        tempText = new StringBuffer();
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
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Section"), uName,  sName});
            if (sName.equals(sysName) || uName.equals(userName))  {
                tempText.append(MessageFormat.format(rbm.getString("SectionReference"),"\t"));

                found = true;
                referenceCount++;
            }
            String line = MessageFormat.format(rbm.getString("ForwardBlocking"),"\t");
            if (testName(section.getForwardBlockingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("ForwardStopping"),"\t");
            if (testName(section.getForwardStoppingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("ReverseBlocking"),"\t");
            if (testName(section.getReverseBlockingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("ReverseStopping"),"\t");
            if (testName(section.getReverseStoppingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }

            ArrayList<jmri.Block> blockList = section.getBlockList();
            for (int k=0; k<blockList.size(); k++) {
                jmri.Block block = blockList.get(k);
                sName = block.getSystemName();
                uName = block.getUserName();
                String line2 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                                new Object[] { "\t", rbm.getString("Block"), uName, sName});
                if (sName.equals(sysName) || (uName!= null && uName.equals(userName)))  {
                    tempText.append(line2);
                    tempText.append(MessageFormat.format(rbm.getString("BlockReference"),"\t"));
                    found = true;
                    referenceCount++;
                }
                Sensor sensor = block.getSensor();
                if (sensor != null) {
                    line = MessageFormat.format(rbm.getString("BlockSensor"),"\t\t");
                    if (testName(sensor.getSystemName(), found, names, line1, line2, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Section"));
            } else {
                text.append("\n");
            }
        }

        tempText = new StringBuffer();
        found = false;
        empty = true;
        jmri.BlockManager blockManager = InstanceManager.blockManagerInstance();
        sysNameList = blockManager.getSystemNameList();

        sectionManager = InstanceManager.sectionManagerInstance();
        iter1 = sectionManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
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
            String uName = b.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Block"), uName,  sName});
            if (sName.equals(sysName) || (uName !=null && uName.equals(userName)))  {
                tempText.append(line1);
                tempText.append(MessageFormat.format(rbm.getString("BlockReference"),"\t"));
                found = true;
                referenceCount++;
            }
            jmri.Sensor s = b.getSensor();
            if (s != null) {
                String line = MessageFormat.format(rbm.getString("BlockSensor"),"\t\t");
                if (testName(s.getSystemName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Block"));
            } else {
                text.append("\n");
            }
        }

        tempText = new StringBuffer();
        found = false;
        empty = true;
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
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("LayoutBlock"), uName,  sName});
            jmri.Sensor s = lb.getOccupancySensor();
            if (s != null) {
                String line = MessageFormat.format(rbm.getString("OccupancySensor"),"\t\t");
                if (testName(s.getSystemName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "LayoutBlock"));
            } else {
                text.append("\n");
            }
        }

        tempText = new StringBuffer();
        found = false;
        empty = true;
        java.util.Enumeration<BlockBossLogic> enumeration = BlockBossLogic.entries();
        while (enumeration.hasMoreElements()) {
            // get the next Logix
            BlockBossLogic bbl = enumeration.nextElement();
            String sName = bbl.getName();
            String uName = bbl.getDrivenSignal();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("BlockBossLogic"), uName, sName});
            if (uName.equals(sysName) || uName.equals(userName) || sName.equals(sysName) || sName.equals(userName)) {
                tempText.append(line1);
                tempText.append(MessageFormat.format(rbm.getString("SignalReference"),"\t"));
                found = true;
                referenceCount++;
            }
            String line = MessageFormat.format(rbm.getString("WatchSensorReference"),"1\t");
            if (testName(bbl.getSensor1(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"2\t");
            if (testName(bbl.getSensor2(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"3\t");
            if (testName(bbl.getSensor3(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"4\t");
            if (testName(bbl.getSensor4(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"5\t");
            if (testName(bbl.getSensor5(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"),"\t");
            if (testName(bbl.getTurnout(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSignalReference"),"1\t");
            if (testName(bbl.getWatchedSignal1(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"),"1Alt\t");
            if (testName(bbl.getWatchedSignal1Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"),"2\t");
            if (testName(bbl.getWatchedSignal2(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"),"2Alt\t");
            if (testName(bbl.getWatchedSignal2Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"1\t");
            if (testName(bbl.getWatchedSensor1(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"1Alt\t");
            if (testName(bbl.getWatchedSensor1Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"2\t");
            if (testName(bbl.getWatchedSensor2(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"),"2Alt\t");
            if (testName(bbl.getWatchedSensor2Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "BlockBossLogic"));
            } else {
                text.append("\n");
            }
        }

        tempText = new StringBuffer();
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
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Conditional"), uName,  sName});
            if (sName.equals(sysName) || uName.equals(userName))  {
                tempText.append(line1);
                tempText.append(MessageFormat.format(rbm.getString("ConditionalReference"),"\t"));
                found = true;
                //referenceCount++; Don't count, this conditional is orphaned by logix(es)
            }
            ArrayList <ConditionalVariable> variableList = c.getCopyOfStateVariables();
            for (int k=0; k<variableList.size(); k++)  {
                ConditionalVariable v = variableList.get(k);
                String line = MessageFormat.format(rbm.getString("VariableReference"),
                                new Object[] { "\t\t", v.getTestTypeString(), v.getDataString()});
                if (testName(v.getName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    //referenceCount++; Don't count, this conditional is orphaned by logix(es)
                }
            }
            ArrayList <ConditionalAction> actionList = c.getCopyOfActions();
            for (int k=0; k<actionList.size(); k++) {
                ConditionalAction a = actionList.get(k);
                String line = MessageFormat.format(rbm.getString("ActionReference"),
                        new Object[] {"\t\t", a.getTypeString(), a.getOptionString(false), a.getActionDataString()});
                if (testName(a.getDeviceName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    //referenceCount++; Don't count, this conditional is orphaned by logix(es)
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuffer();
                found = false;
                empty = false;
                line1 = null;
            }
        }
        if (text != null) {
            if (empty) {
                text.append(MessageFormat.format(rbm.getString("NoReference"), "Conditional"));
            }
            text.append("\n");
        }

        found = false;
        empty = true;
        ArrayList<jmri.jmrit.display.Editor> panelList = jmri.jmrit.display.PanelMenu.instance().getEditorPanelList();
        for (int i=0; i<panelList.size(); i++) {
            jmri.jmrit.display.Editor panelEditor = panelList.get(i);
            name = panelEditor.getTitle();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[] { " ", rbm.getString("Panel"), name, name});
            List <Positionable> contents = panelEditor.getContents();
            for (int k=0; k<contents.size(); k++) {
                Positionable o = contents.get(k);
                if (o.getClass().getName().equals("jmri.jmrit.display.SensorIcon")) {
                    name = ((jmri.jmrit.display.SensorIcon)o).getSensor().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                                        new Object[] { "\t", rbm.getString("Sensor")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.TurnoutIcon")) {
                    name = ((jmri.jmrit.display.TurnoutIcon)o).getTurnout().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                                        new Object[] { "\t", rbm.getString("Turnout")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.SignalHeadIcon")) {
                    name = ((jmri.jmrit.display.SignalHeadIcon)o).getSignalHead().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                                        new Object[] { "\t", rbm.getString("SignalHead")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.MultiSensorIcon")) {
                    jmri.jmrit.display.MultiSensorIcon msi = (jmri.jmrit.display.MultiSensorIcon)o;
                    for (int j=0; j<msi.getNumEntries(); j++)  {
                        name = msi.getSensorName(j);
                        String line = MessageFormat.format(rbm.getString("PanelReference"),
                                            new Object[] { "\t", rbm.getString("MultiSensor")});
                        if (testName(name, found, names, line1, null, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.IndicatorTurnoutIcon")) {
                    jmri.jmrit.display.IndicatorTurnoutIcon ito = (jmri.jmrit.display.IndicatorTurnoutIcon)o;
                    name = ito.getTurnout().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                                        new Object[] { "\t", rbm.getString("IndicatorTurnout")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                    Sensor sensor = ito.getOccSensor();
                    if (sensor!=null) {
                        name = sensor.getSystemName();
                        line = MessageFormat.format(rbm.getString("PanelReference"),
                                            new Object[] { "\t", rbm.getString("IndicatorTurnout")});
                        if (testName(name, found, names, line1, null, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                    jmri.jmrit.logix.OBlock block = ito.getOccBlock();
                    if (block!=null) {
                        sensor = block.getSensor();
                        if (sensor!=null) {
                            name = sensor.getSystemName();
                            line = MessageFormat.format(rbm.getString("PanelReference"),
                                                new Object[] { "\t", rbm.getString("IndicatorTurnout")});
                            if (testName(name, found, names, line1, null, line, tempText)) {
                                found = true;
                                referenceCount++;
                            }
                        }
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.IndicatorTrackIcon")) {
                    jmri.jmrit.display.IndicatorTrackIcon track = (jmri.jmrit.display.IndicatorTrackIcon)o;
                    Sensor sensor = track.getOccSensor();
                    if (sensor!=null) {
                        name = sensor.getSystemName();
                        String line = MessageFormat.format(rbm.getString("PanelReference"),
                                            new Object[] { "\t", rbm.getString("IndicatorTrack")});
                        if (testName(name, found, names, line1, null, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                    jmri.jmrit.logix.OBlock block = track.getOccBlock();
                    if (block!=null) {
                        sensor = block.getSensor();
                        if (sensor!=null) {
                            name = sensor.getSystemName();
                            String line = MessageFormat.format(rbm.getString("PanelReference"),
                                                new Object[] { "\t", rbm.getString("IndicatorTrack")});
                            if (testName(name, found, names, line1, null, line, tempText)) {
                                found = true;
                                referenceCount++;
                            }
                        }
                    }
                }
                if (text != null && found) {
                    text.append(tempText.toString());
                    tempText = new StringBuffer();
                    found = false;
                    empty = false;
                    line1 = null;
                }
            }
            if (text != null) {
                if (empty) {
                    text.append(MessageFormat.format(rbm.getString("NoReference"), "Panel"));
                }
            }
        }

        if (text != null) {
            if (referenceCount == 0) {
                text.append(MessageFormat.format(rbm.getString("Orphan"), (Object[])names));
            } else {
                text.append(MessageFormat.format(rbm.getString("ReferenceFound"), 
                                       new Object[] {Integer.valueOf(referenceCount), userName, sysName}));
            }
        }
        if (names[0] != null) { 
            // The manager is always a listener
            int numListeners = Integer.parseInt(names[3]) - 1;
            // PickLists are also listeners
            numListeners = numListeners - jmri.jmrit.picker.PickListModel.getNumInstances(names[0]);
            if (names[0].equals("Sensor")) {
                numListeners = numListeners - jmri.jmrit.picker.PickListModel.getNumInstances("MultiSensor");
            }

            if (numListeners > referenceCount)
            {
                if (names[0].length()==0) {
                    names[0] = "Unknown Type?";
                }
                /*
                JOptionPane.showMessageDialog(null, 
                        MessageFormat.format(rbm.getString("OrphanName"), (Object[])names)+" has "+numListeners+
                            " listeners installed and only "+referenceCount+ 
                            " references found.\n"+names[0]+
                            " Tables are listeneners.  Check that the table is closed.", 
                        rbm.getString("infoTitle"), JOptionPane.INFORMATION_MESSAGE);
               */         
                if (text != null) {
                    text.append(MessageFormat.format(rbm.getString("OrphanName"), (Object[])names)+" has "+numListeners+
                            " listeners installed and only "+referenceCount+ 
                            " references found.\n"+names[0]+
                            " Tables are listeneners.  Check that the table is closed.");
                }
            }
        }
        return (referenceCount > 0);
    }

    static void makeDialog(Component component, Component button, Frame parent, String title) {
        JDialog dialog = new JDialog(parent, title, true);
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

	static final Logger log = LoggerFactory
			.getLogger(Maintenance.class.getName());
}

