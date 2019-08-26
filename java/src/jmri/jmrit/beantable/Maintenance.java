package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import javax.annotation.*;

import jmri.*;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of static utilities to provide cross referencing information
 * among the various PanelPro objects. Most likely, this is incomplete as there
 * still may be references held by objects unknown to the author. It is intended
 * to inform users where and how the various elements are used. In particular to
 * identify useless elements ('orphans'). Currently, called only from the Logix
 * JFrame, which is probably not its ultimate UI.
 *
 * @author Pete Cressman Copyright 2009
 */
public class Maintenance {

    static final ResourceBundle rbm = ResourceBundle.getBundle("jmri.jmrit.beantable.MaintenanceBundle");

    /**
     * Find references of a System or User name in the various Manager Objects.
     *
     * @param devName name to look for
     * @param parent Frame calling this method
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
     * Find orphaned elements in the various Manager Objects.
     *
     * @param parent Frame to check
     */
    @SuppressWarnings("deprecation") // requires JUnit tests before can reliably redo getSystemNameList-using algorithms
    public static void findOrphansPressed(Frame parent) {
        Vector<String> display = new Vector<String>();
        Vector<String> names = new Vector<String>();

        Iterator<String> iter = InstanceManager.sensorManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null) && !name.equals("ISCLOCKRUNNING")) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"),
                        (Object[]) getTypeAndNames(name)));
                names.add(name);
            }
        }
        iter = InstanceManager.turnoutManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"),
                        (Object[]) getTypeAndNames(name)));
                names.add(name);
            }
        }
        iter = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"),
                        (Object[]) getTypeAndNames(name)));
                names.add(name);
            }
        }
        iter = InstanceManager.lightManagerInstance().getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"),
                        (Object[]) getTypeAndNames(name)));
                names.add(name);
            }
        }
        iter = InstanceManager.getDefault(jmri.ConditionalManager.class).getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"),
                        (Object[]) getTypeAndNames(name)));
                names.add(name);
            }
        }
        iter = InstanceManager.getDefault(jmri.SectionManager.class).getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"),
                        (Object[]) getTypeAndNames(name)));
                names.add(name);
            }
        }
        iter = InstanceManager.getDefault(jmri.BlockManager.class).getSystemNameList().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!search(name, null)) {
                display.add(MessageFormat.format(rbm.getString("OrphanName"),
                        (Object[]) getTypeAndNames(name)));
                names.add(name);
            }
        }
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (int i = 0; i < display.size(); i++) {
            listModel.addElement(display.get(i));
        }
        JList<String> list = new JList<String>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JButton button = new JButton(Bundle.getMessage("ButtonDelete"));
        button.setToolTipText(rbm.getString("OrphanDeleteHint"));

        class SearchListener implements ActionListener {

            JList<String> list;
            Vector<String> n;

            SearchListener(JList<String> list, Vector<String> name) {
                this.list = list;
                this.n = name;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getMaxSelectionIndex();
                if (index < 0) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            rbm.getString("OrphanDeleteHint"),
                            rbm.getString("DeleteTitle"),
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int min = list.getMinSelectionIndex();
                DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
                while (index >= min) {
                    String[] names = getTypeAndNames(n.get(index));
                    if (names[0].equals("Sensor")) { // NOI18N
                        Sensor s = InstanceManager.sensorManagerInstance().getBySystemName(names[2]);
                        if (s == null) {
                            s = InstanceManager.sensorManagerInstance().getBySystemName(names[1]);
                        }
                        if (s != null) {
                            InstanceManager.sensorManagerInstance().deregister(s);
                        }
                    } else if (names[0].equals("Turnout")) { // NOI18N
                        Turnout t = InstanceManager.turnoutManagerInstance().getBySystemName(names[2]);
                        if (t == null) {
                            t = InstanceManager.turnoutManagerInstance().getBySystemName(names[1]);
                        }
                        if (t != null) {
                            InstanceManager.turnoutManagerInstance().deregister(t);
                        }
                    } else if (names[0].equals("SignalHead")) { // NOI18N
                        SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(names[2]);
                        if (sh == null) {
                            sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(names[1]);
                        }
                        if (sh != null) {
                            InstanceManager.getDefault(jmri.SignalHeadManager.class).deregister(sh);
                        }
                    } else if (names[0].equals("Light")) { // NOI18N
                        Light l = InstanceManager.lightManagerInstance().getBySystemName(names[2]);
                        if (l == null) {
                            l = InstanceManager.lightManagerInstance().getBySystemName(names[1]);
                        }
                        if (l != null) {
                            InstanceManager.lightManagerInstance().deregister(l);
                        }
                    } else if (names[0].equals("Conditional")) { // NOI18N
                        Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(names[2]);
                        if (c == null) {
                            c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(names[1]);
                        }
                        if (c != null) {
                            InstanceManager.getDefault(jmri.ConditionalManager.class).deregister(c);
                        }
                    } else if (names[0].equals("Section")) { // NOI18N
                        jmri.Section sec = InstanceManager.getDefault(jmri.SectionManager.class).getBySystemName(names[2]);
                        if (sec == null) {
                            sec = InstanceManager.getDefault(jmri.SectionManager.class).getBySystemName(names[1]);
                        }
                        if (sec != null) {
                            InstanceManager.getDefault(jmri.SectionManager.class).deregister(sec);
                        }
                    } else if (names[0].equals("Block")) { // NOI18N
                        jmri.Block b = InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(names[2]);
                        if (b == null) {
                            b = InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(names[1]);
                        }
                        if (b != null) {
                            InstanceManager.getDefault(jmri.BlockManager.class).deregister(b);
                        }
                    }
                    model.remove(index);
                    n.remove(index);
                    index--;
                }
                index++;
                if (index >= model.getSize()) {
                    index = model.getSize() - 1;
                }
                if (index >= 0) {
                    list.setSelectedIndex(index);
                }
            }
        }
        JScrollPane scrollPane = new JScrollPane(list);
        button.addActionListener(new SearchListener(list, names));
        button.setMaximumSize(button.getPreferredSize());
        makeDialog(scrollPane, button, parent, rbm.getString("OrphanTitle"));
    }

    /**
     * Find useless Conditionals in the various Manager Objects.
     *
     * @param parent Frame to check
     */
    @SuppressWarnings("deprecation") // requires JUnit tests before can reliably redo getSystemNameList-using algorithms
    public static void findEmptyPressed(Frame parent) {
        Vector<String> display = new Vector<String>();
        Vector<String> names = new Vector<String>();

        log.debug("findEmptyPressed");
        Iterator<String> iter = InstanceManager.getDefault(jmri.ConditionalManager.class).getSystemNameList().iterator();
        jmri.ConditionalManager cm = InstanceManager.getDefault(jmri.ConditionalManager.class);
        while (iter.hasNext()) {
            String name = iter.next();
            Conditional c = cm.getBySystemName(name);
            if (c != null) {
                List<ConditionalVariable> variableList = c.getCopyOfStateVariables();
                if (variableList.size() == 0) {
                    String userName = c.getUserName();
                    display.add(MessageFormat.format(rbm.getString("OrphanName"),
                            new Object[]{"Conditional", userName, name}));
                    names.add(name);
                }
            }
        }
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (int i = 0; i < display.size(); i++) {
            listModel.addElement(display.get(i));
        }
        JList<String> list = new JList<String>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JButton button = new JButton(Bundle.getMessage("ButtonDelete"));
        button.setToolTipText(rbm.getString("OrphanDeleteHint") + Bundle.getMessage("ButtonDelete"));

        class EmptyListener implements ActionListener {

            JList<String> list;
            Vector<String> name;

            EmptyListener(JList<String> list, Vector<String> name) {
                this.list = list;
                this.name = name;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getMaxSelectionIndex();
                if (index < 0) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            rbm.getString("OrphanDeleteHint"),
                            rbm.getString("DeleteTitle"),
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int min = list.getMinSelectionIndex();
                DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
                while (index >= min) {
                    String[] names = getTypeAndNames(name.get(index));
                    model.remove(index);
                    Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(names[2]);
                    if (c != null) {
                        Logix x = InstanceManager.getDefault(jmri.ConditionalManager.class).getParentLogix(names[2]);
                        if (x != null) {
                            x.deActivateLogix();
                            x.deleteConditional(names[2]);
                            x.activateLogix();
                        }
                        InstanceManager.getDefault(jmri.ConditionalManager.class).deregister(c);
                        name.remove(index);
                        index--;
                    }
                }
                index++;
                if (index >= model.getSize()) {
                    index = model.getSize() - 1;
                }
                if (index >= 0) {
                    list.setSelectedIndex(index);
                }
            }
        }
        JScrollPane scrollPane = new JScrollPane(list);
        button.addActionListener(new EmptyListener(list, names));
        button.setMaximumSize(button.getPreferredSize());
        makeDialog(scrollPane, button, parent, rbm.getString("EmptyConditionalTitle"));
    }

    /**
     * Find type of element and its names from a name that may be a user name or
     * a system name.
     * <p>
     * Searches each Manager for a reference to the "name".
     *
     * @param name string (name base) to look for
     * @return 4 element String array: {Type, userName, sysName, numListeners}  - 
     * This should probably return an instance of a custom type rather than a bunch of string names
     */
    @Nonnull
    static String[] getTypeAndNames(@Nonnull String name) {
        log.debug("getTypeAndNames for \"{}\"", name);

        String[] result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(SensorManager.class), "Sensor", name);
        if (result != null) return result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(TurnoutManager.class), "Turnout", name);
        if (result != null) return result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(LightManager.class), "Light", name);
        if (result != null) return result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(SignalHeadManager.class), "SignalHead", name);
        if (result != null) return result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(ConditionalManager.class), "Conditional", name);
        if (result != null) return result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(BlockManager.class), "Block", name);
        if (result != null) return result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(SectionManager.class), "Section", name);  // old code has "Block" for type
        if (result != null) return result;

        result = checkForOneTypeAndNames(InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class), "OBlock", name);
        if (result != null) return result;


        return new String[]{"", name, name, "0"};

    }
    // captive for above
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "null return for normal (no error) case is easy to check, and this is a really wierd array")
    // This should probably return an instance of a custom type rather than a bunch of string names
    static private String[] checkForOneTypeAndNames( @Nonnull Manager<? extends NamedBean> manager, @Nonnull String type, @Nonnull String beanName) {
        NamedBean bean = manager.getBeanBySystemName(beanName);
        if (bean != null) return new String[]{type, bean.getUserName(), bean.getSystemName(), Integer.toString(bean.getNumPropertyChangeListeners())};

        bean = manager.getBeanByUserName(beanName);
        if (bean != null) return new String[]{type, bean.getUserName(), bean.getSystemName(), Integer.toString(bean.getNumPropertyChangeListeners())};

        return null;
    }

    /**
     * Check if a given string is either a user or a system name.
     *
     * @param name the string to compare
     * @param found whether the item has already been found somewhere
     * @param names array containing system and user name as items 0 and 1
     * @param line1 message line 1 to use if string is not matched
     * @param line2 message line 2 to use if string is not matched
     * @param line message line to use if string is matched
     * @param tempText body of text to add to, a global variable
     * @return false if name is null or cannot be matched to the names array
     */
    static boolean testName(String name, boolean found, String[] names, String line1, String line2,
            String line, StringBuilder tempText) {
        if (name == null) {
            return false;
        }
        String sysName = names[2];
        String userName = names[1];
        if (name.equals(sysName) || name.equals(userName)) {
            if (!found) {
                if (line1 != null) {
                    tempText.append(line1);
                }
                if (line2 != null) {
                    tempText.append(line2);
                }
            }
            tempText.append(line);
            return true;
        }
        return false;
    }

    /**
     * Search if a given string is used as the name of a NamedBean.
     *
     * @param name the string to look for
     * @param text body of the message to be displayed reporting the result
     * @return true if name is found at least once as a bean name
     */
    @SuppressWarnings("deprecation") // requires JUnit tests before can reliably redo getSystemNameList-using algorithms
    static boolean search(String name, JTextArea text) {
        String[] names = getTypeAndNames(name);
        if (log.isDebugEnabled()) {
            log.debug("search for " + name + " as " + names[0] + " \"" + names[1] + "\" (" + names[2] + ")");
        }
        if (names[0].length() == 0) {
            if (text != null) {
                text.append(MessageFormat.format(rbm.getString("ElementNotFound"), (Object[]) names));
                return false;
            }
        }
        if (text != null) {
            text.append(MessageFormat.format(rbm.getString("ReferenceFollows"), (Object[]) names));
        }
        String sysName = names[2];
        String userName = names[1];
        int referenceCount = 0;
        StringBuilder tempText;
        boolean found = false;
        boolean empty = true;
        // search for references among each class known to be listeners
        Iterator<String> iter1 = InstanceManager.getDefault(jmri.LogixManager.class).getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            Logix x = InstanceManager.getDefault(jmri.LogixManager.class).getBySystemName(sName);
            if (x == null) {
                log.error("Error getting Logix  - " + sName);
                break;
            }
            tempText = new StringBuilder();
            String uName = x.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{"", Bundle.getMessage("BeanNameLogix"), uName, sName});
            for (int i = 0; i < x.getNumConditionals(); i++) {
                sName = x.getConditionalByNumberOrder(i);
                if (sName == null) {
                    log.error("Null conditional system name");
                    break;
                }
                Conditional c = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName(sName);
                if (c == null) {
                    log.error("Invalid conditional system name - " + sName);
                    break;
                }
                uName = c.getUserName();
                String line2 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                        new Object[]{"\t", Bundle.getMessage("BeanNameConditional"), uName, sName});
                String line = MessageFormat.format(rbm.getString("ConditionalReference"), "\t");
                if (sysName.equals(sName) || (userName != null && userName.length() > 0 && userName.equals(uName))) {
                    if (testName(sysName, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
                List<ConditionalVariable> variableList = c.getCopyOfStateVariables();
                for (int k = 0; k < variableList.size(); k++) {
                    ConditionalVariable v = variableList.get(k);
                    line = MessageFormat.format(rbm.getString("VariableReference"),
                            new Object[]{"\t\t", v.getTestTypeString(), v.getDataString()});
                    if (testName(v.getName(), found, names, line1, line2, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
                List<ConditionalAction> actionList = c.getCopyOfActions();
                for (int k = 0; k < actionList.size(); k++) {
                    ConditionalAction a = actionList.get(k);
                    line = MessageFormat.format(rbm.getString("ActionReference"),
                            new Object[]{"\t\t", a.getTypeString(), a.getOptionString(false), a.getActionDataString()});
                    if (testName(a.getDeviceName(), found, names, line1, line2, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
                if (text != null && found) {
                    text.append(tempText.toString());
                    tempText = new StringBuilder();
                    found = false;
                    empty = false;
                    line1 = null;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                found = false;
                empty = false;
            }
        }
        if (text != null) {
            if (empty) {
                text.append("\t" + MessageFormat.format(rbm.getString("NoReference"), "Logix"));
                // cannot put escaped tab char at start of getString
            } else {
                text.append("\n");
            }
        }

        tempText = new StringBuilder();
        found = false;
        empty = true;
        jmri.jmrit.logix.OBlockManager oBlockManager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
        iter1 = oBlockManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.jmrit.logix.OBlock block = oBlockManager.getBySystemName(sName);
            String uName = block.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", Bundle.getMessage("BeanNameOBlock"), uName, sName});
            Sensor sensor = block.getSensor();
            if (sensor != null) {
                String line = MessageFormat.format(rbm.getString("OBlockSensor"), "\t");
                if (testName(sensor.getSystemName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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

        tempText = new StringBuilder();
        found = false;
        empty = true;
        jmri.RouteManager routeManager = InstanceManager.getDefault(jmri.RouteManager.class);
        iter1 = routeManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Route r = routeManager.getBySystemName(sName);
            if (r == null) {
                log.error("Error getting Route  - " + sName);
                break;
            }
            String uName = r.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", Bundle.getMessage("BeanNameRoute"), uName, sName});
            for (int i = 0; i < jmri.Route.MAX_CONTROL_SENSORS; i++) {
                String line = "\t" + MessageFormat.format(rbm.getString("ControlReference"), Bundle.getMessage("BeanNameSensor"));
                if (testName(r.getRouteSensorName(i), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            String line = MessageFormat.format("TurnoutsAlignedSensor", Bundle.getMessage("BeanNameSensor"));
            if (testName(r.getTurnoutsAlignedSensor(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = "\t" + MessageFormat.format(rbm.getString("ControlReference"), Bundle.getMessage("BeanNameTurnout"));
            if (testName(r.getControlTurnout(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format("LockControlTurnout", Bundle.getMessage("BeanNameTurnout"));
            if (testName(r.getLockControlTurnout(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            for (int i = 0; i < r.getNumOutputTurnouts(); i++) {
                line = "\t" + MessageFormat.format(rbm.getString("OutputReference"), Bundle.getMessage("BeanNameTurnout"));
                if (testName(r.getOutputTurnoutByIndex(i), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            for (int i = 0; i < r.getNumOutputSensors(); i++) {
                line = "\t" +  MessageFormat.format(rbm.getString("OutputReference"), Bundle.getMessage("BeanNameSensor"));
                if (testName(r.getOutputSensorByIndex(i), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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

        tempText = new StringBuilder();
        found = false;
        empty = true;
        jmri.TransitManager transitManager = InstanceManager.getDefault(jmri.TransitManager.class);
        iter1 = transitManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Transit transit = transitManager.getBySystemName(sName);
            if (transit == null) {
                log.error("Error getting Transit - " + sName);
                break;
            }
            String uName = transit.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", Bundle.getMessage("BeanNameTransit"), uName, sName});
            List<jmri.TransitSection> sectionList = transit.getTransitSectionList();
            for (int i = 0; i < sectionList.size(); i++) {
                jmri.TransitSection transitSection = sectionList.get(i);
                jmri.Section section = transitSection.getSection();
                uName = section.getUserName();
                sName = section.getSystemName();
                String line2 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                        new Object[]{"\t", rbm.getString("TransitSection"), uName, sName});
                if (sName.equals(sysName) || uName.equals(userName)) {
                    tempText.append(line1);
                    tempText.append(line2);
                    tempText.append(MessageFormat.format(rbm.getString("SectionReference"), "\t\t"));
                    found = true;
                    referenceCount++;
                }
                String line = MessageFormat.format(rbm.getString("ForwardBlocking"), "\t\t");
                if (testName(section.getForwardBlockingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                line = MessageFormat.format(rbm.getString("ForwardStopping"), "\t\t");
                if (testName(section.getForwardStoppingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                line = MessageFormat.format(rbm.getString("ReverseBlocking"), "\t\t");
                if (testName(section.getReverseBlockingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                line = MessageFormat.format(rbm.getString("ReverseStopping"), "\t\t");
                if (testName(section.getReverseStoppingSensorName(), found, names, line1, line2, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
                List<jmri.Block> blockList = section.getBlockList();

                for (int k = 0; k < blockList.size(); k++) {
                    jmri.Block block = blockList.get(k);
                    sName = block.getSystemName();
                    uName = block.getUserName();
                    tempText.append(MessageFormat.format(rbm.getString("ReferenceTitle"),
                            new Object[]{"\t\t", Bundle.getMessage("BeanNameBlock"), uName, sName}));
                    if (sName.equals(sysName) || uName.equals(userName)) {
                        tempText.append(MessageFormat.format(rbm.getString("BlockReference"), "\t\t"));
                        found = true;
                        referenceCount++;
                    }
                    Sensor sensor = block.getSensor();
                    if (sensor != null) {
                        line = MessageFormat.format(rbm.getString("BlockSensor"), "\t\t");
                        if (testName(sensor.getSystemName(), found, names, line1, line2, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                }
                if (text != null && found) {
                    text.append(tempText.toString());
                    tempText = new StringBuilder();
                    found = false;
                    empty = false;
                    line1 = null;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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

        // if (text != null) {
        //   text.append(rbm.getString("NestMessage"));
        // }
        tempText = new StringBuilder();
        found = false;
        empty = true;
        jmri.SectionManager sectionManager = InstanceManager.getDefault(jmri.SectionManager.class);
        java.util.List<String> sysNameList = new java.util.ArrayList<>(sectionManager.getSystemNameList());

        transitManager = InstanceManager.getDefault(jmri.TransitManager.class);
        iter1 = transitManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Transit transit = transitManager.getBySystemName(sName);
            if (transit != null) {
                List<jmri.TransitSection> sectionList = transit.getTransitSectionList();
                for (int i = 0; i < sectionList.size(); i++) {
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
            if (section == null) {
                log.error("Error getting Section - " + sName);
                break;
            }
            String uName = section.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", Bundle.getMessage("BeanNameSection"), uName, sName});
            if (sName.equals(sysName) || uName.equals(userName)) {
                tempText.append(MessageFormat.format(rbm.getString("SectionReference"), "\t"));

                found = true;
                referenceCount++;
            }
            String line = MessageFormat.format(rbm.getString("ForwardBlocking"), "\t");
            if (testName(section.getForwardBlockingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("ForwardStopping"), "\t");
            if (testName(section.getForwardStoppingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("ReverseBlocking"), "\t");
            if (testName(section.getReverseBlockingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("ReverseStopping"), "\t");
            if (testName(section.getReverseStoppingSensorName(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }

            List<jmri.Block> blockList = section.getBlockList();
            for (int k = 0; k < blockList.size(); k++) {
                jmri.Block block = blockList.get(k);
                sName = block.getSystemName();
                uName = block.getUserName();
                String line2 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                        new Object[]{"\t", Bundle.getMessage("BeanNameBlock"), uName, sName});
                if (sName.equals(sysName) || (uName != null && uName.equals(userName))) {
                    tempText.append(line2);
                    tempText.append(MessageFormat.format(rbm.getString("BlockReference"), "\t"));
                    found = true;
                    referenceCount++;
                }
                Sensor sensor = block.getSensor();
                if (sensor != null) {
                    line = MessageFormat.format(rbm.getString("BlockSensor"), "\t\t");
                    if (testName(sensor.getSystemName(), found, names, line1, line2, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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

        tempText = new StringBuilder();
        found = false;
        empty = true;
        jmri.BlockManager blockManager = InstanceManager.getDefault(jmri.BlockManager.class);
        sysNameList = new java.util.ArrayList<>(blockManager.getSystemNameList());

        sectionManager = InstanceManager.getDefault(jmri.SectionManager.class);
        iter1 = sectionManager.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            String sName = iter1.next();
            jmri.Section section = sectionManager.getBySystemName(sName);
            if (section != null) {
                for (Block block : section.getBlockList()) {
                    sysNameList.remove(block.getSystemName());
                }
            }
        }
        iter1 = sysNameList.iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Block b = blockManager.getBySystemName(sName);
            if (b == null) {
                continue;
            }
            String uName = b.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", Bundle.getMessage("BeanNameBlock"), uName, sName});
            if (sName.equals(sysName) || (uName != null && uName.equals(userName))) {
                tempText.append(line1);
                tempText.append(MessageFormat.format(rbm.getString("BlockReference"), "\t"));
                found = true;
                referenceCount++;
            }
            jmri.Sensor s = b.getSensor();
            if (s != null) {
                String line = MessageFormat.format(rbm.getString("BlockSensor"), "\t\t");
                if (testName(s.getSystemName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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

        tempText = new StringBuilder();
        found = false;
        empty = true;
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        iter1 = lbm.getSystemNameList().iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.jmrit.display.layoutEditor.LayoutBlock lb = lbm.getBySystemName(sName);
            if (lb == null) {
                log.error("Error getting LayoutBlock - " + sName);
                break;
            }
            String uName = lb.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", rbm.getString("LayoutBlock"), uName, sName});
            jmri.Sensor s = lb.getOccupancySensor();
            if (s != null) {
                String line = MessageFormat.format(rbm.getString("OccupancySensor"), "\t\t");
                if (testName(s.getSystemName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    referenceCount++;
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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

        tempText = new StringBuilder();
        found = false;
        empty = true;
        java.util.Enumeration<BlockBossLogic> enumeration = BlockBossLogic.entries();
        while (enumeration.hasMoreElements()) {
            // get the next Logix
            BlockBossLogic bbl = enumeration.nextElement();
            String sName = bbl.getName();
            String uName = bbl.getDrivenSignal();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", rbm.getString("BlockBossLogic"), uName, sName});
            if (uName.equals(sysName) || uName.equals(userName) || sName.equals(sysName) || sName.equals(userName)) {
                tempText.append(line1);
                tempText.append(MessageFormat.format(rbm.getString("SignalReference"), "\t"));
                found = true;
                referenceCount++;
            }
            String line = MessageFormat.format(rbm.getString("WatchSensorReference"), "1\t");
            if (testName(bbl.getSensor1(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "2\t");
            if (testName(bbl.getSensor2(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "3\t");
            if (testName(bbl.getSensor3(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "4\t");
            if (testName(bbl.getSensor4(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "5\t");
            if (testName(bbl.getSensor5(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"), "\t");
            if (testName(bbl.getTurnout(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSignalReference"), "1\t");
            if (testName(bbl.getWatchedSignal1(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"), "1Alt\t");
            if (testName(bbl.getWatchedSignal1Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"), "2\t");
            if (testName(bbl.getWatchedSignal2(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchTurnoutReference"), "2Alt\t");
            if (testName(bbl.getWatchedSignal2Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "1\t");
            if (testName(bbl.getWatchedSensor1(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "1Alt\t");
            if (testName(bbl.getWatchedSensor1Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "2\t");
            if (testName(bbl.getWatchedSensor2(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            line = MessageFormat.format(rbm.getString("WatchSensorReference"), "2Alt\t");
            if (testName(bbl.getWatchedSensor2Alt(), found, names, line1, null, line, tempText)) {
                found = true;
                referenceCount++;
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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

        tempText = new StringBuilder();
        found = false;
        empty = true;
        jmri.ConditionalManager conditionalManager = InstanceManager.getDefault(jmri.ConditionalManager.class);
        sysNameList = new java.util.ArrayList<>(conditionalManager.getSystemNameList());

        iter1 = InstanceManager.getDefault(jmri.LogixManager.class).getSystemNameList().iterator();
        while (iter1.hasNext()) {
            String sName = iter1.next();
            Logix x = InstanceManager.getDefault(jmri.LogixManager.class).getBySystemName(sName);
            for (int i = 0; i < x.getNumConditionals(); i++) {
                sName = x.getConditionalByNumberOrder(i);
                sysNameList.remove(sName);
            }
        }
        iter1 = sysNameList.iterator();
        while (iter1.hasNext()) {
            // get the next Logix
            String sName = iter1.next();
            jmri.Conditional c = conditionalManager.getBySystemName(sName);
            if (c == null) {
                log.error("Error getting Condition - " + sName);
                break;
            }
            String uName = c.getUserName();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", Bundle.getMessage("BeanNameConditional"), uName, sName});
            if (sName.equals(sysName) || uName.equals(userName)) {
                tempText.append(line1);
                tempText.append(MessageFormat.format(rbm.getString("ConditionalReference"), "\t"));
                found = true;
                //referenceCount++; Don't count, this conditional is orphaned by logix(es)
            }
            List<ConditionalVariable> variableList = c.getCopyOfStateVariables();
            for (int k = 0; k < variableList.size(); k++) {
                ConditionalVariable v = variableList.get(k);
                String line = MessageFormat.format(rbm.getString("VariableReference"),
                        new Object[]{"\t\t", v.getTestTypeString(), v.getDataString()});
                if (testName(v.getName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    //referenceCount++; Don't count, this conditional is orphaned by logix(es)
                }
            }
            List<ConditionalAction> actionList = c.getCopyOfActions();
            for (int k = 0; k < actionList.size(); k++) {
                ConditionalAction a = actionList.get(k);
                String line = MessageFormat.format(rbm.getString("ActionReference"),
                        new Object[]{"\t\t", a.getTypeString(), a.getOptionString(false), a.getActionDataString()});
                if (testName(a.getDeviceName(), found, names, line1, null, line, tempText)) {
                    found = true;
                    //referenceCount++; Don't count, this conditional is orphaned by logix(es)
                }
            }
            if (text != null && found) {
                text.append(tempText.toString());
                tempText = new StringBuilder();
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
        List<jmri.jmrit.display.Editor> panelList = InstanceManager.getDefault(PanelMenu.class).getEditorPanelList();
        for (int i = 0; i < panelList.size(); i++) {
            jmri.jmrit.display.Editor panelEditor = panelList.get(i);
            name = panelEditor.getTitle();
            String line1 = MessageFormat.format(rbm.getString("ReferenceTitle"),
                    new Object[]{" ", rbm.getString("Panel"), name, name});
            List<Positionable> contents = panelEditor.getContents();
            for (int k = 0; k < contents.size(); k++) {
                Positionable o = contents.get(k);
                if (o.getClass().getName().equals("jmri.jmrit.display.SensorIcon")) {
                    name = ((jmri.jmrit.display.SensorIcon) o).getSensor().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                            new Object[]{"\t", Bundle.getMessage("BeanNameSensor")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.TurnoutIcon")) {
                    name = ((jmri.jmrit.display.TurnoutIcon) o).getTurnout().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                            new Object[]{"\t", Bundle.getMessage("BeanNameTurnout")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.SignalHeadIcon")) {
                    name = ((jmri.jmrit.display.SignalHeadIcon) o).getSignalHead().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                            new Object[]{"\t", Bundle.getMessage("BeanNameSignalHead")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.MultiSensorIcon")) {
                    jmri.jmrit.display.MultiSensorIcon msi = (jmri.jmrit.display.MultiSensorIcon) o;
                    for (int j = 0; j < msi.getNumEntries(); j++) {
                        name = msi.getSensorName(j);
                        String line = MessageFormat.format(rbm.getString("PanelReference"),
                                new Object[]{"\t", Bundle.getMessage("MultiSensor")});
                        if (testName(name, found, names, line1, null, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.IndicatorTurnoutIcon")) {
                    jmri.jmrit.display.IndicatorTurnoutIcon ito = (jmri.jmrit.display.IndicatorTurnoutIcon) o;
                    name = ito.getTurnout().getSystemName();
                    String line = MessageFormat.format(rbm.getString("PanelReference"),
                            new Object[]{"\t", Bundle.getMessage("IndicatorTO")});
                    if (testName(name, found, names, line1, null, line, tempText)) {
                        found = true;
                        referenceCount++;
                    }
                    Sensor sensor = ito.getOccSensor();
                    if (sensor != null) {
                        name = sensor.getSystemName();
                        line = MessageFormat.format(rbm.getString("PanelReference"),
                                new Object[]{"\t", Bundle.getMessage("IndicatorTO")});
                        if (testName(name, found, names, line1, null, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                    jmri.jmrit.logix.OBlock block = ito.getOccBlock();
                    if (block != null) {
                        sensor = block.getSensor();
                        if (sensor != null) {
                            name = sensor.getSystemName();
                            line = MessageFormat.format(rbm.getString("PanelReference"),
                                    new Object[]{"\t", Bundle.getMessage("IndicatorTO")});
                            if (testName(name, found, names, line1, null, line, tempText)) {
                                found = true;
                                referenceCount++;
                            }
                        }
                    }
                } else if (o.getClass().getName().equals("jmri.jmrit.display.IndicatorTrackIcon")) {
                    jmri.jmrit.display.IndicatorTrackIcon track = (jmri.jmrit.display.IndicatorTrackIcon) o;
                    Sensor sensor = track.getOccSensor();
                    if (sensor != null) {
                        name = sensor.getSystemName();
                        String line = MessageFormat.format(rbm.getString("PanelReference"),
                                new Object[]{"\t", Bundle.getMessage("IndicatorTrack")});
                        if (testName(name, found, names, line1, null, line, tempText)) {
                            found = true;
                            referenceCount++;
                        }
                    }
                    jmri.jmrit.logix.OBlock block = track.getOccBlock();
                    if (block != null) {
                        sensor = block.getSensor();
                        if (sensor != null) {
                            name = sensor.getSystemName();
                            String line = MessageFormat.format(rbm.getString("PanelReference"),
                                    new Object[]{"\t", Bundle.getMessage("IndicatorTrack")});
                            if (testName(name, found, names, line1, null, line, tempText)) {
                                found = true;
                                referenceCount++;
                            }
                        }
                    }
                }
                if (text != null && found) {
                    text.append(tempText.toString());
                    tempText = new StringBuilder();
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
                text.append(MessageFormat.format(rbm.getString("Orphan"), (Object[]) names));
            } else {
                text.append(MessageFormat.format(rbm.getString("ReferenceFound"),
                        new Object[]{Integer.valueOf(referenceCount), userName, sysName}));
            }
        }
        if (names[0] != null) {
            // The manager is always a listener
            int numListeners = Integer.parseInt(names[3]) - 1;
            // PickLists are also listeners
            numListeners = numListeners - jmri.jmrit.picker.PickListModel.getNumInstances(names[0]);
            if (names[0].equals("Sensor")) {
                numListeners = numListeners - jmri.jmrit.picker.PickListModel.getNumInstances("MultiSensor"); // NOI18N
            }

            if (numListeners > referenceCount) {
                if (names[0].length() == 0) {
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
                    text.append(MessageFormat.format(rbm.getString("OrphanName"), (Object[]) names) + " has " + numListeners
                            + " listeners installed and only " + referenceCount
                            + " references found.\n" + names[0]
                            + " Tables are listeneners.  Check that the table is closed.");
                }
            }
        }
        return (referenceCount > 0);
    }

    /**
     * Build and display a dialog box with an OK button and optional 2nd button.
     *
     * @param component Body of message to put in dialog box
     * @param button optional second button to add to pane
     * @param parent Frame that asked for this dialog
     * @param title text do use as title of the dialog box
     */
    static void makeDialog(Component component, Component button, Frame parent, String title) {
        JDialog dialog = new JDialog(parent, title, true);
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
        class myListener implements ActionListener {

            java.awt.Window _w;

            myListener(java.awt.Window w) {
                _w = w;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                // dispose on the GUI thread _later_
                jmri.util.ThreadingUtil.runOnGUIEventually( ()->{ 
                    _w.dispose();
                });
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
        if (button != null) {
            panel.add(Box.createHorizontalStrut(5));
            panel.add(button);
        }
        contentPane.add(panel, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(parent);
        dialog.pack();
        // dispose on the GUI thread _later_
        jmri.util.ThreadingUtil.runOnGUIEventually( ()->{ 
            dialog.setVisible(true);
        });
    }

    private final static Logger log = LoggerFactory.getLogger(Maintenance.class);
}
