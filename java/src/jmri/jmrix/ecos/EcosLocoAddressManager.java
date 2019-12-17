package jmri.jmrix.ecos;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.*;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.utilities.EcosLocoToRoster;
import jmri.jmrix.ecos.utilities.GetEcosObjectNumber;
import jmri.jmrix.ecos.utilities.RemoveObjectFromEcos;
import jmri.jmrix.ecos.utilities.RosterToEcos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the ECoS Loco entries within JMRI.
 *
 * @author Kevin Dickerson
 */
public class EcosLocoAddressManager extends jmri.managers.AbstractManager<NamedBean> implements EcosListener {

    private Hashtable<String, EcosLocoAddress> _tecos = new Hashtable<String, EcosLocoAddress>();   // stores known Ecos Object ids to DCC
    private Hashtable<Integer, EcosLocoAddress> _tdcc = new Hashtable<Integer, EcosLocoAddress>();  // stores known DCC Address to Ecos Object ids

    public EcosLocoAddressManager(EcosSystemConnectionMemo memo) {
        super(memo);
        locoToRoster = new EcosLocoToRoster(getMemo());
        tc = getMemo().getTrafficController();
        p = getMemo().getPreferenceManager();
        rosterAttribute = p.getRosterAttribute();
        loadEcosData();
        try {
            if (jmri.InstanceManager.getNullableDefault(jmri.jmrit.beantable.ListedTableFrame.class) == null) {
                new jmri.jmrit.beantable.ListedTableFrame();
            }
            jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class).addTable("jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableTabAction", "ECoS Loco Database", false);
        } catch (HeadlessException he) {
            // silently ignore inability to display dialog
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcosSystemConnectionMemo getMemo() {
        return (EcosSystemConnectionMemo) memo;
    }

    @Override
    public char typeLetter() {
        return 'Z';
    } // NOI18N

    @Override
    public Class<NamedBean> getNamedBeanClass() {
        return NamedBean.class;
    }

    @Override
    public int getXMLOrder() {
        return 65400;
    }

    String rosterAttribute;
    private RosterEntry _re;
    private boolean addLocoToRoster = false;

    /**
     * EcosLocoAddresses have no system prefix, so return input unchanged.
     * 
     * @param s the input to make a system name
     * @return the resultant system name
     */
    @Override
    public String makeSystemName(String s) {
        return s;
    }

    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public String[] getSystemNameArray() {
        jmri.util.Log4JUtil.deprecationWarning(log, "getSystemNameArray");        
        return new String[0];
    }

    @Override
    @Deprecated  // will be removed when Manager method is removed due to @Override
    public List<String> getSystemNameList() {
        return new ArrayList<String>();
    }

    public void clearLocoToRoster() {
        addLocoToRoster = false;
    }

    public void setLocoToRoster() {
        addLocoToRoster = true;
    }

    public boolean getLocoToRoster() {
        return addLocoToRoster;
    }
    EcosPreferences p;

    ShutDownTask ecosLocoShutDownTask;

    EcosTrafficController tc;

    public EcosLocoAddress provideEcosLoco(String EcosObject, int DCCAddress) {
        EcosLocoAddress l = getByEcosObject(EcosObject);
        if (l != null) {
            return l;
        }
        l = new EcosLocoAddress(DCCAddress);
        l.setEcosObject(EcosObject);
        register(l);
        return l;
    }

    public EcosLocoAddress provideByDccAddress(int dccAddress) {
        EcosLocoAddress l = getByDccAddress(dccAddress);
        //Loco doesn't exist, so we shall create it.
        if (l != null) {
            return l;
        }

        l = new EcosLocoAddress(dccAddress);
        register(l);
        return _tdcc.get(dccAddress);
    }

    public EcosLocoAddress provideByEcosObject(String ecosObject) {
        EcosLocoAddress l = getByEcosObject(ecosObject);
        //Loco doesn't exist, so we shall create it.
        if (l != null) {
            return l;
        }

        l = new EcosLocoAddress(ecosObject, p.getRosterAttribute());
        register(l);

        EcosMessage m = new EcosMessage("request(" + ecosObject + ", view)");
        tc.sendEcosMessage(m, this);
        m = new EcosMessage("get(" + ecosObject + ", speed)");
        tc.sendEcosMessage(m, this);

        m = new EcosMessage("get(" + ecosObject + ", dir)");
        tc.sendEcosMessage(m, this);
        return _tecos.get(ecosObject);
    }

    public EcosLocoAddress getByEcosObject(String ecosObject) {
        return _tecos.get(ecosObject);
    }

    public EcosLocoAddress getByDccAddress(int dccAddress) {
        return _tdcc.get(dccAddress);
    }

    public String[] getEcosObjectArray() {
        String[] arr = new String[_tecos.size()];
        Enumeration<String> en = _tecos.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        return arr;
    }

    public List<String> getEcosObjectList() {
        String[] arr = new String[_tecos.size()];
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _tecos.keys();
        int i = 0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        java.util.Arrays.sort(arr);
        for (i = 0; i < arr.length; i++) {
            out.add(arr[i]);
        }
        return out;
    }

    private void loadEcosData() {
        if (p.getPreferencesLoaded()) {
            loadData();
        } else {
            /*as the loco address manager is called prior to the remainder of the
             preferences being loaded, we add a thread which waits for the preferences
             to be loaded prior to reading the Ecos Loco database.
             */
            if (waitPrefLoad != null) {
                waitPrefLoad.interrupt();
                waitPrefLoad = null;
            }
            waitPrefLoad = new Thread(new WaitPrefLoad());
            waitPrefLoad.setName("Wait for Preferences to be loaded");
            waitPrefLoad.start();
            return;
        }
    }

    private void loadData() {
        tc.addEcosListener(this);

        try {

           Roster.getDefault().addPropertyChangeListener(this);

           EcosMessage m = new EcosMessage("request(10, view)");
           tc.sendWaitMessage(m, this);

           /*m = new EcosMessage("queryObjects(10)");
           tc.sendWaitMessage(m, this);*/
           m = new EcosMessage("queryObjects(10, addr, name, protocol)");
           tc.sendEcosMessage(m, this);

           if (ecosLocoShutDownTask == null) {
               ecosLocoShutDownTask = new QuietShutDownTask("Ecos Loco Database Shutdown") {
                   @Override
                   public boolean execute() {
                       return shutdownDispose();
                   }
               };
           }
           jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(ecosLocoShutDownTask);
        } catch(java.lang.NullPointerException npe) {
            log.debug("Delayed initialization of EcosLocoAddressManager failed, no roster information available");
        }
    }

    public void monitorLocos(boolean monitor) {
        monitorState = monitor;
        List<String> objects = getEcosObjectList();

        for (String ecosObject : objects) {
            EcosMessage m = new EcosMessage("get(" + ecosObject + ", speed)");
            tc.sendEcosMessage(m, this);

            m = new EcosMessage("get(" + ecosObject + ", dir)");
            tc.sendEcosMessage(m, this);
        }
    }

    private boolean monitorState = false;

    public void deleteEcosLoco(EcosLocoAddress s) {
        deregister(s);
    }

    public void register(EcosLocoAddress s) {
        //We should always have at least a DCC address to register a loco.
        //We may not always first time round on initial registration have the Ecos Object.
        String ecosObject = s.getEcosObject();
        int oldsize;
        if (ecosObject != null) {
            oldsize = _tecos.size();
            _tecos.put(ecosObject, s);
            firePropertyChange("length", oldsize, _tecos.size());
        }

        oldsize = _tdcc.size();
        int dccAddress = s.getNumber();
        _tdcc.put(dccAddress, s);
        firePropertyChange("length", oldsize, _tdcc.size());
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this);
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <p>
     * The non-system-specific RouteManager uses this method.
     */
    public void deregister(EcosLocoAddress s) {
        s.removePropertyChangeListener(this);
        String ecosObject = s.getEcosObject();
        int oldsize = _tecos.size();
        _tecos.remove(ecosObject);
        firePropertyChange("length", Integer.valueOf(oldsize), Integer.valueOf(_tecos.size()));

        int dccAddress = s.getNumber();
        oldsize = _tdcc.size();
        _tdcc.remove(dccAddress);
        firePropertyChange("length", Integer.valueOf(oldsize), Integer.valueOf(_tdcc.size()));
        EcosMessage m = new EcosMessage("release(" + ecosObject + ", view)");
        tc.sendEcosMessage(m, this);
        // listen for name and state changes to forward
    }

    private boolean disposefinal() {
        if (jmri.InstanceManager.getNullableDefault(ConfigureManager.class) != null) {
            jmri.InstanceManager.getDefault(jmri.ConfigureManager.class).deregister(this);
        }
        _tecos.clear();
        _tdcc.clear();
        return true;
    }

    /* Dispose is dealt with at shutdown */
    @Override
    public void dispose() {
    }

    public void terminateThreads() {
       if(waitPrefLoad!=null){
          waitPrefLoad.interrupt();
       }
    }

    public boolean shutdownDispose() {
        boolean hasTempEntries = false;
        Enumeration<String> en = _tecos.keys();
        _tdcc.clear();
        //This will remove/deregister non-temporary locos from the list.
        while (en.hasMoreElements()) {
            String ecosObject = en.nextElement();
            if (_tecos.get(ecosObject).getEcosTempEntry()) {
                hasTempEntries = true;
            } else {
                deregister(getByEcosObject(ecosObject));
                _tecos.remove(ecosObject);
            }
        }

        if (p.getAdhocLocoFromEcos() == 0x01) {
            disposefinal();
        } else if (!hasTempEntries) {
            disposefinal();
        } else if (p.getAdhocLocoFromEcos() == EcosPreferences.ASK) {

            final JDialog dialog = new JDialog();
            dialog.setTitle(Bundle.getMessage("RemoveLocoTitle"));
            dialog.setLocation(300, 200);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel question = new JLabel(Bundle.getMessage("RemoveLocoLine1"));
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);
            question = new JLabel(Bundle.getMessage("RemoveLocoLine2"));
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);
            final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
            remember.setFont(remember.getFont().deriveFont(10f));
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);
            //user preferences do not have the save option, but once complete the following line can be removed
            //Need to get the method to save connection configuration.
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
                        p.setAdhocLocoFromEcos(0x01);
                    }
                    disposefinal();
                    dialog.dispose();
                }
            });

            yesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (remember.isSelected()) {
                        p.setAdhocLocoFromEcos(0x02);
                    }
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
        return true;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    @Override
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    /**
     * The PropertyChangeListener interface in this class is intended to keep
     * track of roster entries and sync them up with the Ecos.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        //If we are adding the loco to the roster from the ecos, we don't want to be adding it back to the ecos!
        if (getLocoToRoster()) {
            return;
        }
        if (e.getPropertyName().equals("add")) {
            _re = (RosterEntry) e.getNewValue();

        } else if (e.getPropertyName().equals("saved")) {
            if (_re != null) {
                if (_re.getAttribute(rosterAttribute) != null) {
                    _re = null;
                    return;
                }
                //if the ecosobject attribute exists this would then indicate that it has already been created on the ecos
                if (p.getAddLocoToEcos() == EcosPreferences.ASK) {
                    final JDialog dialog = new JDialog();
                    dialog.setTitle(Bundle.getMessage("AddLocoTitle"));
                    //test.setSize(300,130);
                    dialog.setLocation(300, 200);
                    dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                    JPanel container = new JPanel();
                    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                    container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JLabel question = new JLabel(Bundle.getMessage("AddLocoXQuestion", _re.getId(), getMemo().getUserName()));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                    remember.setFont(remember.getFont().deriveFont(10f));
                    remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                    //user preferences do not have the save option, but once complete the following line can be removed
                    //Need to get the method to save connection configuration.
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
                                p.setAddLocoToEcos(0x01);
                            }
                            _re = null;
                            dialog.dispose();
                        }
                    });

                    yesButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (remember.isSelected()) {
                                p.setAddLocoToEcos(0x02);
                            }
                            RosterToEcos rosterToEcos = new RosterToEcos(getMemo());
                            rosterToEcos.createEcosLoco(_re);
                            _re = null;
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
                if (p.getAddLocoToEcos() == 0x02) {
                    RosterToEcos rosterToEcos = new RosterToEcos(getMemo());
                    rosterToEcos.createEcosLoco(_re);
                    _re = null;
                }
            }
        } else if (e.getPropertyName().equals("remove")) {
            _re = (RosterEntry) e.getNewValue();
            if (_re.getAttribute(rosterAttribute) != null) {
                if (p.getRemoveLocoFromEcos() == EcosPreferences.YES){
                    RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                    removeObjectFromEcos.removeObjectFromEcos(_re.getAttribute(p.getRosterAttribute()), tc);
                    deleteEcosLoco(provideByEcosObject(_re.getAttribute(p.getRosterAttribute())));
                } else if(p.getRemoveLocoFromEcos() == EcosPreferences.ASK ) {
                    final JDialog dialog = new JDialog();
                    dialog.setTitle(Bundle.getMessage("RemoveLocoTitle"));
                    //test.setSize(300,130);
                    dialog.setLocation(300, 200);
                    dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                    JPanel container = new JPanel();
                    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                    container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JLabel question = new JLabel(Bundle.getMessage("RemoveLocoXQuestion", getMemo().getUserName()));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                    remember.setFont(remember.getFont().deriveFont(10f));
                    remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                    //user preferences do not have the save option, but once complete the following line can be removed
                    //Need to get the method to save connection configuration.
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
                                p.setRemoveLocoFromEcos(0x01);
                            }
                            provideByEcosObject(_re.getAttribute(p.getRosterAttribute())).setRosterId(null);
                            dialog.dispose();
                        }
                    });

                    yesButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (remember.isSelected()) {
                                p.setRemoveLocoFromEcos(0x02);
                            }
                            RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                            removeObjectFromEcos.removeObjectFromEcos(_re.getAttribute(p.getRosterAttribute()), tc);
                            deleteEcosLoco(provideByEcosObject(_re.getAttribute(p.getRosterAttribute())));
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
            _re = null;
        } else if (e.getPropertyName().equals("throttleAssigned")) {
            jmri.DccLocoAddress la = (jmri.DccLocoAddress) e.getNewValue();
            EcosLocoAddress ela = getByDccAddress(la.getNumber());
            EcosMessage m = new EcosMessage("get(" + ela.getEcosObject() + ", speed)");
            tc.sendEcosMessage(m, this);
            m = new EcosMessage("get(" + ela.getEcosObject() + ", dir)");
            tc.sendEcosMessage(m, this);
        }
    }

    boolean processLocoToRosterQueue = true;

    @Override
    public void reply(EcosReply m) {
        String strde;

        if (m.getResultCode() == 0) {
            int ecosObjectId = m.getEcosObjectId();
            if ((ecosObjectId != 10) && ((ecosObjectId < 1000) || (ecosObjectId > 2000))) {
                log.debug("message received that is not within the valid loco object range");
                return;
            }
            List<String> headerDetails = m.getReplyHeaderDetails();
            String[] msgDetails = m.getContents();
            if (m.isUnsolicited()) {
                if (ecosObjectId == 10) {
                    log.debug("We have received notification of a change in the Loco list");
                    if (msgDetails.length == 0) {
                        EcosMessage mout = new EcosMessage("queryObjects(10)");
                        tc.sendEcosMessage(mout, this);
                        //Version 3.0.1 of the software has an issue in that it stops sending updates on the
                        //loco objects when a delete has happened, we therefore need to release the old view
                        //then re-request it.
                        mout = new EcosMessage("release(10, view)");
                        tc.sendEcosMessage(mout, this);
                        mout = new EcosMessage("request(10, view)");
                        tc.sendEcosMessage(mout, this);
                    } else if (msgDetails[0].contains("msg[LIST_CHANGED]")) {
                        EcosMessage mout = new EcosMessage("queryObjects(10)");
                        tc.sendEcosMessage(mout, this);
                    }
                } else {
                    EcosLocoAddress tmploco;
                    log.debug("Forwarding on State change for {}", ecosObjectId);
                    String strLocoObject = Integer.toString(ecosObjectId);
                    tmploco = _tecos.get(strLocoObject);
                    if (tmploco != null) {
                        tmploco.reply(m);
                    }
                }
            } else {
                String replyType = m.getReplyType();

                if (replyType.equals("queryObjects")) {
                    if (ecosObjectId == 10) {
                        if (headerDetails.size() == 0 || (headerDetails.size() == 1 && headerDetails.get(0).equals(""))) {
                            checkLocoList(msgDetails);
                        } else {
                            processLocoToRosterQueue = false;
                            //Format of the reply details are ObjectId, followed by object ids.
                            for (String line : msgDetails) {
                                String[] objectdetail = line.split(" ");
                                EcosLocoAddress tmploco = null;
                                //The first part of the messages is always the object id.
                                strde = objectdetail[0];
                                strde = strde.trim();
                                int object = Integer.parseInt(strde);
                                if ((1000 <= object) && (object < 2000)) {
                                    tmploco = provideByEcosObject(strde);
                                }
                                decodeLocoDetails(tmploco, line, true);
                            }
                            locoToRoster.processQueue();
                            processLocoToRosterQueue = true;
                        }
                    }
                } else if (replyType.equals("get")) {
                    EcosLocoAddress tmploco = provideByEcosObject(Integer.toString(ecosObjectId));
                    for (String line : msgDetails) {
                        decodeLocoDetails(tmploco, line, false);
                    }
                }
            }
        }
    }

    void decodeLocoDetails(EcosLocoAddress tmploco, String line, boolean addToRoster) {
        if (tmploco == null) {
            return;
        }
        if (line.contains("cv")) {
            String cv = EcosReply.getContentDetails(line, "cv");
            cv = cv.replaceAll("\\s","");  //remove all white spaces, as 4.1.0 version removed the space after the ,
            int cvnum = Integer.parseInt(cv.substring(0, cv.indexOf(",")));
            int cvval = Integer.parseInt(cv.substring(cv.indexOf(",") + 1, cv.length()));
            tmploco.setCV(cvnum, cvval);
            if (cvnum == 8 && processLocoToRosterQueue) {
                locoToRoster.processQueue();
            }
        }
        if (line.contains("addr")) {
            tmploco.setLocoAddress(GetEcosObjectNumber.getEcosObjectNumber(line, "addr[", "]"));
            if (tmploco.getCV(7) == -1) {
                tmploco.setCV(7, 0);
                getEcosCVs(tmploco);
            }
        }
        if (line.contains("name")) {
            String name = EcosReply.getContentDetails(line, "name").trim();
            name = name.substring(1, name.length() - 1);
            tmploco.setEcosDescription(name);
        }
        if (line.contains("protocol")) {
            tmploco.setProtocol(EcosReply.getContentDetails(line, "protocol"));
        }
        if (line.contains("speed")) {
            tmploco.setSpeed(Integer.parseInt(EcosReply.getContentDetails(line, "speed")));
        }

        if (line.contains("dir")) {
            boolean newDirection = false;
            if (EcosReply.getContentDetails(line, "dir").equals("0")) {
                newDirection = true;
            }
            tmploco.setDirection(newDirection);
        }
        register(tmploco);
        if (p.getAddLocoToJMRI() != EcosPreferences.NO && addToRoster) {
            locoToRoster.addToQueue(tmploco);
        }
    }

    /* This is used after an event update form the ecos informing us of a change in the
     * loco list, we have to determine if it is an addition or delete.
     * We should only ever do either a remove or an add in one go, if we are adding the loco
     * to the roster otherwise this causes a problem with the roster list.
     */
    void checkLocoList(String[] ecoslines) {
        log.debug("Checking loco list");
        String loco;
        for (int i = 0; i < ecoslines.length; i++) {
            loco = ecoslines[i];
            loco = loco.replaceAll("[\\n\\r]", "");
            if (getByEcosObject(loco) == null) {
                log.debug("We are to add loco {} to the Ecos Loco List", loco);
                EcosMessage mout = new EcosMessage("get(" + loco + ", addr, name, protocol)");
                tc.sendEcosMessage(mout, this);
            }
        }

        String[] jmrilist = getEcosObjectArray();
        boolean nomatch = true;
        for (int i = 0; i < jmrilist.length; i++) {
            nomatch = true;
            for (int k = 0; k < ecoslines.length; k++) {
                loco = ecoslines[k];
                loco = loco.replaceAll("[\\n\\r]", "");
                if (loco.equals(jmrilist[i])) {
                    nomatch = false;
                    break;
                }
            }
            if (nomatch) {
                //We do not have a match, therefore this should be deleted from the Ecos loco Manager " + jmrilist[i]
                log.debug("Loco not found so need to remove from register");
                if (getByEcosObject(jmrilist[i]).getRosterId() != null) {
                    final String rosterid = getByEcosObject(jmrilist[i]).getRosterId();
                    final Roster _roster = Roster.getDefault();
                    final RosterEntry re = _roster.entryFromTitle(rosterid);
                    re.deleteAttribute(p.getRosterAttribute());
                    re.writeFile(null, null);
                    Roster.getDefault().writeRoster();
                    if (p.getRemoveLocoFromJMRI() == EcosPreferences.YES) {
                        _roster.removeEntry(re);
                        Roster.getDefault().writeRoster();
                    } else if (p.getRemoveLocoFromJMRI() == EcosPreferences.ASK) {
                        try {
                            final JDialog dialog = new JDialog();
                            dialog.setTitle(Bundle.getMessage("RemoveRosterEntryTitle"));
                            dialog.setLocation(300, 200);
                            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                            JPanel container = new JPanel();
                            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                            JLabel question = new JLabel(Bundle.getMessage("RemoveRosterEntryX", rosterid));
                            question.setAlignmentX(Component.CENTER_ALIGNMENT);
                            container.add(question);
                            final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                            remember.setFont(remember.getFont().deriveFont(10f));
                            remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                            //user preferences do not have the save option, but once complete the following line can be removed
                            //Need to get the method to save connection configuration.
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
                                        p.setRemoveLocoFromJMRI(EcosPreferences.ASK);
                                    }
                                    dialog.dispose();
                                }
                            });

                            yesButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (remember.isSelected()) {
                                        p.setRemoveLocoFromJMRI(EcosPreferences.YES);
                                    }
                                    setLocoToRoster();
                                    _roster.removeEntry(re);
                                    Roster.getDefault().writeRoster();
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

                        } catch (HeadlessException he) {
                            // silently ignore inability to display dialog
                        }
                    }
                }
                //Even if we do not delete the loco from the roster, we need to remove it from the ecos list.
                deregister(getByEcosObject(jmrilist[i]));
            }
        }
    }

    @Override
    public void message(EcosMessage m) {

    }
    /**
     * The purpose of this is to get some of the basic cv details that are required
     * for selecting the decoder mfg and family in the roster file.
     * This might work as sending a single request rather than multiple.
     */
    private void getEcosCVs(EcosLocoAddress tmploco) {
        tc.addEcosListener(this);
        // ask to be notified
        // We won't look to add new locos created on the ecos yet this can be added in at a later date.

        EcosMessage m = new EcosMessage("get(" + tmploco.getEcosObject() + ", cv[7])");
        tc.sendEcosMessage(m, this);

        m = new EcosMessage("get(" + tmploco.getEcosObject() + ", cv[8])");
        tc.sendEcosMessage(m, this);

    }

    EcosLocoToRoster locoToRoster;

    Thread waitPrefLoad;

    private class WaitPrefLoad implements Runnable {

        @Override
        public void run() {
            boolean result = true;
            log.debug("Waiting for the Ecos preferences to be loaded before loading the loco database on the Ecos");
            while (!wait) {
                result = waitForPrefLoad();
            }
            if (result) {
                loadData();
            } else {
                log.debug("waitForPrefLoad requested skip loadData()");
            }
        }

        boolean wait = false;
        int count = 0;

        /**
         * @return true if OK to proceed to load data, false if should abort
         */
        private boolean waitForPrefLoad() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.trace("waitForPrefLoad received InterruptedException, honoring termination request");
                wait = true;
                return false;
            } catch (Exception e) {
                wait = true;
                log.error(e.toString());
                return false;
            }
            wait = p.getPreferencesLoaded();
            if (count >= 1000) {
                wait = true;
                log.warn("Timeout {} occurred on waiting for the Ecos preferences to be loaded", count);
                return false;
            }
            count++;
            return true;
        }
    }

    public void refreshItems() {
        // ask to be notified about newly created locos on the layout.
        EcosMessage m = new EcosMessage("request(10, view)");
        tc.sendEcosMessage(m, this);
        if (monitorState) {
            List<String> objects = getEcosObjectList();
            for (int x = 0; x < objects.size(); x++) {
                //Do a release before anything else.
                m = new EcosMessage("release(" + getByEcosObject(objects.get(x)) + ", view, control)");
                tc.sendEcosMessage(m, this);
            }
            for (int x = 0; x < objects.size(); x++) {
                //Re-request view on loco
                m = new EcosMessage("request(" + getByEcosObject(objects.get(x)) + ", view)");
                tc.sendEcosMessage(m, this);

                m = new EcosMessage("get(" + getByEcosObject(objects.get(x)) + ", speed)");
                tc.sendEcosMessage(m, this);

                m = new EcosMessage("get(" + getByEcosObject(objects.get(x)) + ", dir)");
                tc.sendEcosMessage(m, this);
            }
        }
        //monitorLocos(monitorState);
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage("EcosLocoAddresses");
    }

    private final static Logger log = LoggerFactory.getLogger(EcosLocoAddressManager.class);

}
