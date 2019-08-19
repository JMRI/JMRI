package jmri.jmrix.ecos;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrix.ecos.utilities.GetEcosObjectNumber;
import jmri.jmrix.ecos.utilities.RemoveObjectFromEcos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Ecos systems.
 * <p>
 * System names are "UTnnn", where U is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class EcosTurnoutManager extends jmri.managers.AbstractTurnoutManager
        implements EcosListener {

    public EcosTurnoutManager(EcosSystemConnectionMemo memo) {
        super(memo);
        tc = getMemo().getTrafficController();

        // listen for turnout creation
        // connect to the TrafficManager
        tc.addEcosListener(this);

        // ask to be notified about newly created turnouts on the layout.
        EcosMessage m = new EcosMessage("request(11, view)");
        tc.sendEcosMessage(m, this);

        // get initial state
        m = new EcosMessage("queryObjects(11, addrext)");
        tc.sendEcosMessage(m, this);
        this.addPropertyChangeListener(this);
    }

    EcosTrafficController tc;

    // The hash table simply holds the object number against the EcosTurnout ref.
    private Hashtable<Integer, EcosTurnout> _tecos = new Hashtable<Integer, EcosTurnout>(); // stores known Ecos Object ids to DCC

    /**
     * {@inheritDoc}
     */
    @Override
    public EcosSystemConnectionMemo getMemo() {
        return (EcosSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (java.lang.NumberFormatException e) {
            log.error("failed to convert systemName '{}' to a turnout address", systemName);
            return null;
        }
        Turnout t = new EcosTurnout(addr, getSystemPrefix(), tc, this);
        t.setUserName(userName);
        t.setFeedbackMode("MONITORING");
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    // to listen for status changes from Ecos system
    @Override
    public void reply(EcosReply m) {
        log.debug("reply " + m);
        // is this a list of turnouts?
        EcosTurnout et;

        if (m.getResultCode() == 0) {
            int ecosObjectId = m.getEcosObjectId();
            if ((ecosObjectId != 11) && ((ecosObjectId < 20000) || (ecosObjectId > 30000))) {
                log.debug("message received that is not within the valid turnout object range");
                return;
            }
            List<String> headerDetails = m.getReplyHeaderDetails();
            String[] msgContents = m.getContents();
            //log.info("Initial Header " + headerDetails);
            if (m.isUnsolicited()) {
                if (ecosObjectId == 11) {
                    //Creation or removal of a turnout from the Ecos.
                    if (msgContents[0].contains("msg[LIST_CHANGED]")) {
                        log.debug("We have received notification of a change in the Turnout list");
                        EcosMessage mout = new EcosMessage("queryObjects(11)");
                        tc.sendEcosMessage(mout, this);
                    }
                    //Creation or removal of a turnout from the Ecos.
                } else {
                    log.debug("Forwarding on State change for " + ecosObjectId);
                    et = _tecos.get(ecosObjectId);
                    if (et != null) {
                        et.reply(m);
                        //As the event will come from one object, we shall check to see if it is an extended address,
                        // if it is we also forward the message onto the slaved address.
                        if (et.getExtended() != 0) {
                            log.debug("This is also an extended turnout so forwarding on change to {}", et.getSlaveAddress());
                            EcosTurnout etx = (EcosTurnout) provideTurnout(et.getSlaveAddress());
                            etx.reply(m);
                        }
                    }
                }

            } else {
                String replyType = m.getReplyType();
                if (replyType.equals("queryObjects")) {
                    if (ecosObjectId == 11 && headerDetails.size() == 0) {
                        //if (lines[0].startsWith("<REPLY queryObjects(11)>")) {
                        log.debug("No sub details");
                        checkTurnoutList(msgContents);
                    } else if (headerDetails.contains("addr")) {
                        // yes, make sure TOs exist
                        //log.debug("found "+(lines.length-2)+" turnout objects");
                        for (String item : m.getContents()) {
                            log.debug("header " + item);
                            //for (int i = 1; i<lines.length-1; i++) {
                            if (item.contains("addr")) { // skip odd lines
                                int object = GetEcosObjectNumber.getEcosObjectNumber(item, null, " ");
                                if ((20000 <= object) && (object < 30000)) { // only physical turnouts
                                    int addr = GetEcosObjectNumber.getEcosObjectNumber(item, "[", "]");
                                    log.debug("Found turnout object " + object + " addr " + addr);

                                    if (addr > 0) {
                                        Turnout t = getTurnout(getSystemNamePrefix() + addr);
                                        if (t == null) {
                                            et = (EcosTurnout) provideTurnout(getSystemNamePrefix() + addr);
                                            et.setObjectNumber(object);
                                            _tecos.put(object, et);
                                        }
                                    }
                                } else if ((30000 <= object) && (object < 40000)) {  //This is a ecos route
                                    log.debug("Found route object " + object);

                                    Turnout t = getTurnout(getSystemNamePrefix() + object);
                                    if (t == null) {
                                        et = (EcosTurnout) provideTurnout(getSystemNamePrefix() + object);
                                        et.setObjectNumber(object);
                                        _tecos.put(object, et);
                                    }
                                }
                                if ((20000 <= object) && (object < 40000)) {
                                    EcosMessage em = new EcosMessage("request(" + object + ",view)");
                                    tc.sendEcosMessage(em, this);
                                    em = new EcosMessage("get(" + object + ",state)");
                                    tc.sendEcosMessage(em, this);
                                }
                            }
                        }
                    } else if (headerDetails.contains("addrext")) {
                        //log.info("Extended");
                        for (String item : m.getContents()) {
                            //log.info(item);
                            if (item.contains("addrext")) { // skip odd lines
                                turnoutAddressDetails(item);
                            }
                        }
                    }
                } else if (replyType.equals("get")) {
                    et = (EcosTurnout) getByEcosObject(ecosObjectId);
                    if (headerDetails.contains("state")) {
                        //As this is in response to a change in state we shall forward
                        //it straight on to the ecos turnout to deal with.
                        et.reply(m);
                        //As the event will come from one object, we shall check to see if it is an extended address,
                        // if it is we also forward the message onto the slaved address.
                        if (et.getExtended() != 0) {
                            EcosTurnout etx = (EcosTurnout) provideTurnout(et.getSlaveAddress());
                            etx.reply(m);
                        }

                    } else if (headerDetails.contains("symbol")) {
                        //Extract symbol number and set on turnout.
                        int symbol = GetEcosObjectNumber.getEcosObjectNumber(msgContents[0], "[", "]");
                        et.setExtended(symbol);
                        et.setTurnoutOperation(jmri.InstanceManager.getDefault(TurnoutOperationManager.class).getOperation("NoFeedback"));
                        if ((symbol == 2) || (symbol == 4)) {

                            EcosTurnout etx = (EcosTurnout) provideTurnout(et.getSlaveAddress());
                            etx.setExtended(symbol);
                            etx.setTurnoutOperation(jmri.InstanceManager.getDefault(TurnoutOperationManager.class).getOperation("NoFeedback"));
                            switch (symbol) {
                                case 2:
                                    et.setComment("Three Way Point with " + et.getSlaveAddress());
                                    break;
                                case 4:
                                    et.setComment("Double Slip with " + et.getSlaveAddress());
                                    break;
                                default:
                                    break;
                            }
                        }
                        // get initial state
                        EcosMessage em = new EcosMessage("get(" + ecosObjectId + ",state)");
                        tc.sendEcosMessage(em, this);

                    } else if (headerDetails.contains("addrext")) {
                        turnoutAddressDetails(msgContents[0]);
                    } else {
                        String name = null;
                        for (String li : msgContents) {
                            if (li.contains("name")) {
                                //start=li.indexOf("[")+2;
                                //end=li.indexOf("]")-1;
                                if ((name != null) /*&& (start!=end)*/) {
                                    name = name + EcosReply.getContentDetail(li); /*" " + li.substring(start, end);*/

                                } else {
                                    name = EcosReply.getContentDetail(li); /*li.substring(start, end);*/

                                }
                            }
                        }
                        if (name != null) {
                            et.setUserName(name);
                        }
                    }
                } else if (ecosObjectId >= 20000) { // ecosObjectId <= 30000 is always true at this point (Spotbugs)
                    log.debug("Reply for specific turnout");
                    et = _tecos.get(ecosObjectId);
                    if (et != null) {
                        et.reply(m);
                        //As the event will come from one object, we shall check to see if it is an extended address,
                        // if it is we also forward the message onto the slaved address.
                        if (et.getExtended() != 0) {
                            log.debug("This is also an extended turnout so forwarding on change to {}", et.getSlaveAddress());
                            EcosTurnout etx = (EcosTurnout) provideTurnout(et.getSlaveAddress());
                            etx.reply(m);
                        }
                    }
                }
            }
        } else {
            log.debug("Message received from Ecos is in error");
        }
    }

    protected boolean addingTurnouts = false;

    private void turnoutAddressDetails(String lines) {
        addingTurnouts = true;
        EcosTurnout et;
        int start;
        int end;
        int object = GetEcosObjectNumber.getEcosObjectNumber(lines, null, " ");
        if ((20000 <= object) && (object < 30000)) {
            start = lines.indexOf('[') + 1;
            end = lines.indexOf(']');
            String turnoutadd = stripChar(lines.substring(start, end));
            String[] straddr = turnoutadd.split(",");
            log.debug("Number of Address for this device is " + straddr.length);
            if (straddr.length <= 2) {
                if (straddr.length == 2) {
                    if (!straddr[0].equals(straddr[1])) {
                        log.debug("Addresses are not the same, we shall use the first address listed.");
                    }
                }
                int addr = Integer.parseInt(straddr[0]);
                if (addr > 0) {
                    Turnout t = getTurnout(getSystemNamePrefix() + addr);
                    if (t == null) {
                        et = (EcosTurnout) provideTurnout(getSystemNamePrefix() + addr);
                        et.setObjectNumber(object);
                        _tecos.put(object, et);
                        // listen for changes
                        EcosMessage em = new EcosMessage("request(" + object + ",view)");
                        tc.sendEcosMessage(em, this);

                        // get initial state
                        em = new EcosMessage("get(" + object + ",state)");
                        tc.sendEcosMessage(em, this);

                        em = new EcosMessage("get(" + object + ", name1, name2, name3)");
                        tc.sendEcosMessage(em, this);
                    }
                }

            } else if (straddr.length == 4) {
                log.debug("We have a two address object.");
                //The first two addresses should be the same
                if (!straddr[0].equals(straddr[1])) {
                    log.debug("First Pair of Addresses are not the same, we shall use the first address");
                }
                if (!straddr[2].equals(straddr[3])) {
                    log.debug("Second Pair of Addresses are not the same, we shall use the first address");
                }
                int addr = Integer.parseInt(straddr[0]);
                int addr2 = Integer.parseInt(straddr[2]);
                if (addr > 0) {
                    //addr = straddr[0];
                    Turnout t = getTurnout(getSystemNamePrefix() + addr);
                    if (t == null) {
                        et = (EcosTurnout) provideTurnout(getSystemNamePrefix() + addr);
                        et.setObjectNumber(object);
                        et.setSlaveAddress(addr2);
                        _tecos.put(object, et);

                        //Get the type of accessory...
                        EcosMessage em = new EcosMessage("get(" + object + ",symbol)");
                        tc.sendEcosMessage(em, this);

                        // listen for changes
                        em = new EcosMessage("request(" + object + ",view)");
                        tc.sendEcosMessage(em, this);

                        em = new EcosMessage("get(" + object + ", name1, name2, name3)");
                        tc.sendEcosMessage(em, this);
                    }
                }

                if (addr2 > 0) {
                    Turnout t = getTurnout(getSystemNamePrefix() + addr2);
                    if (t == null) {
                        et = (EcosTurnout) provideTurnout(getSystemNamePrefix() + addr2);
                        et.setMasterObjectNumber(false);
                        et.setObjectNumber(object);
                        et.setComment("Extended address linked with turnout " + getSystemPrefix() + "T" + straddr[0]);
                    }
                }
            }

        } else if ((30000 <= object) && (object < 40000)) {  //This is a ecos route

            log.debug("Found route object " + object);

            Turnout t = getTurnout(getSystemNamePrefix() + object);
            if (t == null) {
                et = (EcosTurnout) provideTurnout(getSystemNamePrefix() + object);
                et.setObjectNumber(object);
                _tecos.put(object, et);

                // get initial state
                EcosMessage em = new EcosMessage("get(" + object + ",state)");
                tc.sendEcosMessage(em, this);
                //Need to do some more work on routes on the ecos.

                // listen for changes
                // em = new EcosMessage("request("+object+",view)");
                // tc.sendEcosMessage(em, null);
                // get the name from the ecos to set as Username
                em = new EcosMessage("get(" + object + ", name1, name2, name3)");
                tc.sendEcosMessage(em, this);
            }
        }
        addingTurnouts = false;
    }

    /* This is used after an event update form the ecos informing us of a change in the 
     * turnout list, we have to determine if it is an addition or delete.
     * We should only ever do either a remove or an add in one go.
     */
    void checkTurnoutList(String[] ecoslines) {
        final EcosPreferences p = getMemo().getPreferenceManager();

        String[] jmrilist = getEcosObjectArray();
        boolean nomatch = true;
        int intTurnout = 0;
        String strECOSTurnout = null;
        for (int i = 0; i < jmrilist.length; i++) {
            nomatch = true;
            String strJMRITurnout = jmrilist[i];
            intTurnout = Integer.parseInt(strJMRITurnout);
            for (String li : ecoslines) {
                strECOSTurnout = li.replaceAll("[\\n\\r]", "");
                if (strECOSTurnout.equals(strJMRITurnout)) {
                    nomatch = false;
                    break;
                }
            }

            if (nomatch) {
                final EcosTurnout et = (EcosTurnout) getByEcosObject(intTurnout);
                _tecos.remove(intTurnout);
                if (p.getRemoveTurnoutsFromJMRI() == 0x02) {
                    //Remove turnout
                    _tecos.remove(et.getObject());
                    deregister(et);
                } else if (p.getRemoveTurnoutsFromJMRI() == 0x00) {
                    final JDialog dialog = new JDialog();
                    dialog.setTitle(Bundle.getMessage("DeleteTurnoutTitle"));
                    dialog.setLocationRelativeTo(null);
                    dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                    JPanel container = new JPanel();
                    container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                    JLabel question = new JLabel(Bundle.getMessage("RemoveTurnoutLine1", et.getDisplayName()));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    question = new JLabel(Bundle.getMessage("RemoveTurnoutLine2"));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                    remember.setFont(remember.getFont().deriveFont(10f));
                    remember.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
                    JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
                    JPanel button = new JPanel();
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);
                    button.add(yesButton);
                    button.add(noButton);
                    container.add(button);

                    noButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (remember.isSelected()) {
                                p.setRemoveTurnoutsFromJMRI(0x01);
                            }
                            dialog.dispose();
                        }
                    });

                    yesButton.addActionListener(new ActionListener() {
                        final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (remember.isSelected()) {
                                p.setRemoveTurnoutsFromJMRI(0x02);
                            }
                            int count = et.getNumPropertyChangeListeners() - 1; // one is this table
                            if (log.isDebugEnabled()) {
                                log.debug("Delete with " + count);
                            }
                            if ((!noWarnDelete) && (count > 0)) {
                                String msg = java.text.MessageFormat.format(
                                        rb.getString("DeletePrompt") + "\n"
                                        + rb.getString("ReminderInUse"),
                                        new Object[]{et.getSystemName(), "" + count});
                                // verify deletion
                                int val = javax.swing.JOptionPane.showOptionDialog(null,
                                        msg, Bundle.getMessage("WarningTitle"),
                                        javax.swing.JOptionPane.YES_NO_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null,
                                        new Object[]{Bundle.getMessage("ButtonYes"),
                                            rb.getString("ButtonYesPlus"),
                                                Bundle.getMessage("ButtonNo")},
                                        Bundle.getMessage("ButtonNo"));
                                if (val == 2) {
                                    _tecos.remove(et.getObject());
                                    deregister(et);
                                    dialog.dispose();
                                    return;  // return without deleting
                                }
                                if (val == 1) { // suppress future warnings
                                    noWarnDelete = true;
                                }
                            }
                            // finally OK, do the actual delete
                            deleteEcosTurnout(et);
                            dialog.dispose();
                        }
                    });
                    container.add(remember);
                    container.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.setAlignmentY(Component.CENTER_ALIGNMENT);
                    dialog.getContentPane().add(container);
                    dialog.pack();
                    dialog.setModal(true);
                    dialog.setVisible(true);
                } else {
                    //We will need to remove the turnout from our list as it no longer exists on the ecos.
                    _tecos.remove(et.getObject());
                }
            }
        }
        int turnout;
        for (String li : ecoslines) {
            String tmpturn = li.replaceAll("[\\n\\r]", "");
            turnout = Integer.parseInt(tmpturn);
            if (getByEcosObject(turnout) == null) {
                EcosMessage mout = new EcosMessage("get(" + turnout + ", addrext)");
                tc.sendEcosMessage(mout, this);
            }
        }
    }

    boolean noWarnDelete = false;

    public String stripChar(String s) {
        String allowed
                = ",0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (allowed.indexOf(s.charAt(i)) >= 0) {
                result.append(s.charAt(i));
            }
        }

        return result.toString();
    }

    @Override
    public void message(EcosMessage m) {
        // messages are ignored
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UCF_USELESS_CONTROL_FLOW", 
        justification = "OK to compare floats, as even tiny differences should trigger update")
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().equals("length")) && (!addingTurnouts)) {
            final EcosPreferences p = getMemo().getPreferenceManager();
            EcosTurnout et;
            String[] ecoslist = this.getEcosObjectArray();
            
             for (Turnout turnout : getNamedBeanSet()) {
                if (turnout.getSystemName().startsWith(getSystemNamePrefix())) {
                    et = (EcosTurnout) turnout;
                    if (et.getObject() == 0) {
                        //We do not support this yet at there are many parameters
                        // when creating a turnout on the ecos.
                    }
                }
            }

            for (int i = 0; i < ecoslist.length; i++) {
                et = (EcosTurnout) getByEcosObject(Integer.parseInt(ecoslist[i]));
                int address = et.getNumber();
                if (getBySystemName(getSystemNamePrefix() + address) == null) {
                    if (p.getRemoveTurnoutsFromEcos() == 0x02) {
                        RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                        removeObjectFromEcos.removeObjectFromEcos("" + et.getObject(), tc);
                        deleteEcosTurnout(et);
                    } else {
                        final EcosTurnout etd = et;
                        final JDialog dialog = new JDialog();
                        dialog.setTitle(Bundle.getMessage("RemoveTurnoutTitle"));
                        dialog.setLocation(300, 200);
                        dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                        JPanel container = new JPanel();
                        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        JLabel question = new JLabel(Bundle.getMessage("RemoveTurnoutX", etd.getSystemName()));
                        question.setAlignmentX(Component.CENTER_ALIGNMENT);
                        container.add(question);
                        final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                        remember.setFont(remember.getFont().deriveFont(10f));
                        remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                        remember.setVisible(true);
                        JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
                        JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
                        JPanel button = new JPanel();
                        button.setAlignmentX(Component.CENTER_ALIGNMENT);
                        button.add(yesButton);
                        button.add(noButton);
                        container.add(button);

                        noButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (remember.isSelected()) {
                                    p.setRemoveTurnoutsFromEcos(0x01);
                                }
                                dialog.dispose();
                            }
                        });

                        yesButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (remember.isSelected()) {
                                    p.setRemoveTurnoutsFromEcos(0x02);
                                }
                                RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                                removeObjectFromEcos.removeObjectFromEcos("" + etd.getObject(), tc);
                                deleteEcosTurnout(etd);
                                dialog.dispose();
                            }
                        });
                        container.add(remember);
                        container.setAlignmentX(Component.CENTER_ALIGNMENT);
                        container.setAlignmentY(Component.CENTER_ALIGNMENT);
                        dialog.getContentPane().add(container);
                        dialog.pack();
                        dialog.setModal(true);
                        dialog.setVisible(true);
                    }
                }
            }
        }
        super.propertyChange(e);
    }

    public void deleteEcosTurnout(EcosTurnout et) {
        addingTurnouts = true;
        deregister(et);
        et.dispose();
        EcosMessage em = new EcosMessage("release(" + et.getObject() + ",view)");
        tc.sendEcosMessage(em, this);
        _tecos.remove(Integer.valueOf(et.getObject()));
        addingTurnouts = false;
    }

    @Override
    public void dispose() {
        Enumeration<Integer> en = _tecos.keys();
        EcosMessage em;
        while (en.hasMoreElements()) {
            int ecosObject = en.nextElement();
            em = new EcosMessage("release(" + ecosObject + ",view)");
            tc.sendEcosMessage(em, this);
        }

        if (jmri.InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            jmri.InstanceManager.getDefault(ConfigureManager.class).deregister(this);
        }
        _tecos.clear();
    }

    public List<String> getEcosObjectList() {
        String[] arr = new String[_tecos.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<Integer> en = _tecos.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = "" + en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(arr[i]);
        }
        return out;
    }

    public String[] getEcosObjectArray() {
        String[] arr = new String[_tecos.size()];
        Enumeration<Integer> en = _tecos.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = "" + en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    }

    public Turnout getByEcosObject(int ecosObject) {
        return _tecos.get(Integer.valueOf(ecosObject));
    }

    public void refreshItems() {
        /*ask to be notified about newly created turnouts on the layout.
         Doing the request to view the list, will also kick off a request to 
         view on each individual turnout*/
        EcosMessage m = new EcosMessage("request(11, view)");
        tc.sendEcosMessage(m, this);
        for (Integer ecosObjectId : _tecos.keySet()) {
            EcosMessage em = new EcosMessage("release(" + ecosObjectId + ",view)");
            tc.sendEcosMessage(em, this);
            em = new EcosMessage("get(" + ecosObjectId + ",state)");
            tc.sendEcosMessage(em, this);
            em = new EcosMessage("request(" + ecosObjectId + ",view)");
            tc.sendEcosMessage(em, this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EcosTurnoutManager.class);

}
