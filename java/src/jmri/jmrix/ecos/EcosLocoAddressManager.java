package jmri.jmrix.ecos;

import jmri.jmrix.ecos.utilities.*;
import java.util.Enumeration;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;

/**
 * Managers the Ecos Loco entries within JMRI.
 * @author Kevin Dickerson
 * @version     $Revision$
 */
public class EcosLocoAddressManager extends jmri.managers.AbstractManager implements java.beans.PropertyChangeListener, EcosListener, jmri.Manager{

    private Hashtable <String, EcosLocoAddress> _tecos = new Hashtable<String, EcosLocoAddress>();   // stores known Ecos Object ids to DCC
    private Hashtable <Integer, EcosLocoAddress> _tdcc = new Hashtable<Integer, EcosLocoAddress>();  // stores known DCC Address to Ecos Object ids

    
    public EcosLocoAddressManager(EcosSystemConnectionMemo memo){
        adaptermemo = memo;
        locoToRoster = new EcosLocoToRoster(adaptermemo);
        tc = adaptermemo.getTrafficController();
        p = adaptermemo.getPreferenceManager();
        rosterAttribute = p.getRosterAttribute();
        prefix = adaptermemo.getSystemPrefix();
        loadEcosData();
        if (jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class)==null){
            new jmri.jmrit.beantable.ListedTableFrame();
        }
        jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class).addTable("jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableTabAction", "ECoS Loco Database", false);
    }
    
    String prefix;
    public String getSystemPrefix() { return prefix; }
    public char typeLetter() { return 'Z'; }

    public int getXMLOrder(){
        return 65400;
    }
    
    String rosterAttribute;
    private RosterEntry _re;
    private boolean addLocoToRoster = false;
    
    @Override
    public String makeSystemName(String s){ return "";}
    @Override
    public String[] getSystemNameArray() { return new String[0]; }
    @Override
    public List<String> getSystemNameList() {return new ArrayList<String>(); }
    
    public void clearLocoToRoster(){
        addLocoToRoster = false;
    }
    
    public void setLocoToRoster(){
        addLocoToRoster = true;
    }
    
    public boolean getLocoToRoster(){
        return addLocoToRoster;
    }
    EcosPreferences p;
    
    ShutDownTask ecosLocoShutDownTask;
    
    EcosTrafficController tc;
    EcosSystemConnectionMemo adaptermemo;
    
    public EcosLocoAddress provideEcosLoco(String EcosObject, int DCCAddress) {
        EcosLocoAddress l = getByEcosObject(EcosObject);
        if (l!=null) return l;
        l = new EcosLocoAddress(DCCAddress);
        l.setEcosObject(EcosObject);
        register(l);
        return l;
    }
    
    public EcosLocoAddress provideByDccAddress(int dccAddress) {
        EcosLocoAddress l = getByDccAddress(dccAddress);
        //Loco doesn't exist, so we shall create it.
        if (l!=null) return l;
        
        l = new EcosLocoAddress(dccAddress);
        register(l);
        return _tdcc.get(dccAddress);
    }
    
    public EcosLocoAddress provideByEcosObject(String ecosObject) {
        EcosLocoAddress l = getByEcosObject(ecosObject);
        //Loco doesn't exist, so we shall create it.
        if (l!=null) return l;
        
        l = new EcosLocoAddress(ecosObject, p.getRosterAttribute());
        register(l);
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
        int i=0;
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
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    private void loadEcosData(){
        if (p.getPreferencesLoaded()){
            loadData();
        } else {
            /*as the loco address manager is called prior to the remainder of the
            preferences being loaded, we add a thread which waits for the preferences
            to be loaded prior to reading the Ecos Loco database.
            */
            if(waitPrefLoad!=null) {
                waitPrefLoad.interrupt();
                waitPrefLoad=null;
            }
            waitPrefLoad = new Thread(new waitPrefLoad());
            waitPrefLoad.setName("Wait for Preferences to be loaded");
            waitPrefLoad.start();
            return;
        }
    }
    
    private void loadData(){
        tc.addEcosListener(this);

        Roster.instance().addPropertyChangeListener(this);

        EcosMessage m = new EcosMessage("request(10, view)");
        tc.sendWaitMessage(m, this);

        m = new EcosMessage("queryObjects(10, addr, name, protocol)");
        tc.sendEcosMessage(m, this);
        
        if (ecosLocoShutDownTask==null) {
            ecosLocoShutDownTask = new QuietShutDownTask("Ecos Loco Database Shutdown") {
                @Override
                public boolean doAction(){
                    return shutdownDispose();
                }
            };
        }
        if (jmri.InstanceManager.shutDownManagerInstance() !=null){
            jmri.InstanceManager.shutDownManagerInstance().register(ecosLocoShutDownTask);
        }
    }
    
    public void monitorLocos(boolean monitor){
        monitorState = monitor;
        List<String> objects = getEcosObjectList();

        for(int x = 0; x < objects.size(); x++){
            boolean isLongAddress = false;
            if(getByEcosObject(objects.get(x)).getEcosLocoAddress()>128)
                isLongAddress = true;
            jmri.DccLocoAddress la = new jmri.DccLocoAddress(getByEcosObject(objects.get(x)).getEcosLocoAddress(), isLongAddress);
            if(monitor)
                adaptermemo.getThrottleManager().attachListener(la, this);
            else
                adaptermemo.getThrottleManager().removeListener(la, this);
        }
    }
    
    private boolean monitorState = false;
    
    public void deleteEcosLoco(EcosLocoAddress s){
        deregister(s);
    }

    public void register(EcosLocoAddress s) {
        //We should always have at least a DCC address to register a loco.
        //We may not always first time round on initial registration have the Ecos Object.
        String ecosObject = s.getEcosObject();
        int oldsize=0;
        if(ecosObject !=null){
            oldsize = _tecos.size();
            _tecos.put(ecosObject, s);
            firePropertyChange("length", Integer.valueOf(oldsize), Integer.valueOf(_tecos.size()));
        }

        oldsize = _tdcc.size();
        int dccAddress = s.getEcosLocoAddress();
        _tdcc.put(dccAddress, s);
        firePropertyChange("length", Integer.valueOf(oldsize), Integer.valueOf(_tdcc.size()));
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this);
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager
     * uses this method.
     */
    public void deregister(EcosLocoAddress s) {
        s.removePropertyChangeListener(this);
        String ecosObject = s.getEcosObject();
        int oldsize = _tecos.size();
        _tecos.remove(ecosObject);
        firePropertyChange("length", Integer.valueOf(oldsize), Integer.valueOf(_tecos.size()));
        
        int dccAddress = s.getEcosLocoAddress();
        oldsize = _tdcc.size();
        _tdcc.remove(dccAddress);
        firePropertyChange("length", Integer.valueOf(oldsize), Integer.valueOf(_tdcc.size()));
        // listen for name and state changes to forward
    }

    private boolean disposefinal(){
        if (jmri.InstanceManager.configureManagerInstance()!= null)
            jmri.InstanceManager.configureManagerInstance().deregister(this);
        _tecos.clear();
        _tdcc.clear();
        return true;
    }

    /*Dispose is dealt with at shutdown*/
    @Override
    public void dispose(){
    }
    
    public boolean shutdownDispose(){
        boolean hasTempEntries = false;
        Enumeration<String> en = _tecos.keys();
        _tdcc.clear();
        //This will remove/deregister non-temporary locos from the list.
        while (en.hasMoreElements()) {
            String ecosObject = en.nextElement();
            if(_tecos.get(ecosObject).getEcosTempEntry())
                hasTempEntries=true;
            else {
                deregister(getByEcosObject(ecosObject));
                _tecos.remove(ecosObject);
            }
        }

        if(p.getAdhocLocoFromEcos()==0x01){
            disposefinal();
        } else if (!hasTempEntries) {
            disposefinal();
        }
        else if((hasTempEntries) && (p.getAdhocLocoFromEcos()==0x00)){

            final JDialog dialog = new JDialog();
            dialog.setTitle("Remove Loco From ECoS?");
            dialog.setLocation(300,200);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            JLabel question = new JLabel("A number of locos have been created on the Ecos for temporary use");
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);
            question = new JLabel("Do you want these locos removed from the Ecos?");
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);
            final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
            remember.setFont(remember.getFont().deriveFont(10f));
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);
            //user preferences do not have the save option, but once complete the following line can be removed
            //Need to get the method to save connection configuration.
            remember.setVisible(true);
            JButton yesButton = new JButton("Yes");
            JButton noButton = new JButton("No");
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(yesButton);
            button.add(noButton);
            container.add(button);
            
            noButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(remember.isSelected()){
                        p.setAdhocLocoFromEcos(0x01);
                    }
                    disposefinal();
                    dialog.dispose();
                }
            });
            
            yesButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(remember.isSelected()) {
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
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

        /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of roster entries and sync them up
     * with the Ecos.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        //If we are adding the loco to the roster from the ecos, we don't want to be adding it back to the ecos!
        if(getLocoToRoster()) return;
        if (e.getPropertyName().equals("add")){
            _re = (RosterEntry) e.getNewValue();

        } else if (e.getPropertyName().equals("saved")) {
            if (_re!=null){
                if (_re.getAttribute(rosterAttribute)!=null){
                    _re = null;
                    return;
                }
                //if the ecosobject attribute exists this would then indicate that it has already been created on the ecos
                if (p.getAddLocoToEcos()==0x00){
                    final JDialog dialog = new JDialog();
                    dialog.setTitle("Add Loco to the ECoS?");
                    //test.setSize(300,130);
                    dialog.setLocation(300,200);
                    dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                    JPanel container = new JPanel();
                    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                    container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                    JLabel question = new JLabel("Do you also want to add " + _re.getId() + " to the " + adaptermemo.getUserName() + "?");
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
                    remember.setFont(remember.getFont().deriveFont(10f));
                    remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                    //user preferences do not have the save option, but once complete the following line can be removed
                    //Need to get the method to save connection configuration.
                    remember.setVisible(true);
                    JButton yesButton = new JButton("Yes");
                    JButton noButton = new JButton("No");
                    JPanel button = new JPanel();
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);
                    button.add(yesButton);
                    button.add(noButton);
                    container.add(button);

                    noButton.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            if(remember.isSelected()){
                                p.setAddLocoToEcos(0x01);
                            }
                            _re=null;
                            dialog.dispose();
                        }
                    });

                    yesButton.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            if(remember.isSelected()) {
                                p.setAddLocoToEcos(0x02);
                            }
                            RosterToEcos rosterToEcos = new RosterToEcos();
                            rosterToEcos.createEcosLoco(_re, adaptermemo);
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
                if(p.getAddLocoToEcos()==0x02){
                    RosterToEcos rosterToEcos = new RosterToEcos();
                    rosterToEcos.createEcosLoco(_re, adaptermemo);
                    _re = null;
                }
            }
        } else if (e.getPropertyName().equals("remove")){
            _re = (RosterEntry) e.getNewValue();
            if (_re.getAttribute(rosterAttribute)!=null){
                if (p.getRemoveLocoFromEcos()==0x02){
                    RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                    removeObjectFromEcos.removeObjectFromEcos(_re.getAttribute(p.getRosterAttribute()), tc);
                    deleteEcosLoco(provideByEcosObject(_re.getAttribute(p.getRosterAttribute())));
                } else {
                    final JDialog dialog = new JDialog();
                    dialog.setTitle("Remove Loco From ECoS?");
                    //test.setSize(300,130);
                    dialog.setLocation(300,200);
                    dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                    JPanel container = new JPanel();
                    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                    container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                    JLabel question = new JLabel("Do you also want to remove this loco from the " + adaptermemo.getUserName());
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
                    remember.setFont(remember.getFont().deriveFont(10f));
                    remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                    //user preferences do not have the save option, but once complete the following line can be removed
                    //Need to get the method to save connection configuration.
                    remember.setVisible(true);
                    JButton yesButton = new JButton("Yes");
                    JButton noButton = new JButton("No");
                    JPanel button = new JPanel();
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);
                    button.add(yesButton);
                    button.add(noButton);
                    container.add(button);

                    noButton.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            if(remember.isSelected()){
                                p.setRemoveLocoFromEcos(0x01);
                            }
                            provideByEcosObject(_re.getAttribute(p.getRosterAttribute())).setRosterId(null);
                            dialog.dispose();
                        }
                    });

                    yesButton.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            if(remember.isSelected()) {
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
            _re=null;
        } else if (e.getPropertyName().equals("throttleAssigned")){
            jmri.DccLocoAddress la = (jmri.DccLocoAddress)e.getNewValue();
            EcosLocoAddress ela = getByDccAddress(la.getNumber());
            EcosMessage m = new EcosMessage("get("+ela.getEcosObject() + ", speed)");
            tc.sendEcosMessage(m, this);
            m = new EcosMessage("get("+ela.getEcosObject() + ", dir)");
            tc.sendEcosMessage(m, this);
        }
    }

    public void reply(EcosReply m) {
        String strde;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        log.debug("found "+(lines.length)+" response from Ecos");
        if (m.getResultCode()==0){
            int ecosObjectId = m.getEcosObjectId();
            if((ecosObjectId!=10) && ((ecosObjectId<1000) || (ecosObjectId>2000))){
                log.debug("message receieved that is not within the valid loco object range");
                return;
            }
            if (m.isUnsolicited()){
                if(ecosObjectId==10){
                    log.debug("We have received notification of a change in the Loco list");
                    if (lines.length==2){
                        EcosMessage mout = new EcosMessage("queryObjects(10)");
                        tc.sendEcosMessage(mout, this);
                        //Version 3.0.1 of the software has an issue in that it stops sending updates on the 
                        //loco objects when a delete has happened, we therefore need to release the old view
                        //then re-request it.
                        mout = new EcosMessage("release(10, view)");
                        tc.sendEcosMessage(mout, this);
                        mout = new EcosMessage("request(10, view)");
                        tc.sendEcosMessage(mout, this);
                    } else if (lines[1].contains("msg[LIST_CHANGED]")){
                        EcosMessage mout = new EcosMessage("queryObjects(10)");
                        tc.sendEcosMessage(mout, this);
                    }
                } else {
                    EcosLocoAddress tmploco;
                    //So long as the event information is for a turnout we will determine
                    //which turnout it is for and let that deal with the message.
                    //int object = m.getEcosObjectId();
                    //if ((1000<=ecosObjectId) && (ecosObjectId<2000)){
                        log.debug("Forwarding on State change for " + ecosObjectId);
                        String strLocoObject = Integer.toString(ecosObjectId);
                        tmploco = _tecos.get(strLocoObject);
                        if (tmploco!=null){
                            tmploco.reply(m);
                        }
                    //}
                } 
            } else {
                String replyType = m.getReplyType();
                if(replyType.equals("queryObjects")){
                    //This needs restructuring it processes the list of locos
                    if (lines[0].startsWith("<REPLY queryObjects(10)>")){
                        //System.out.println("A return of a simple list of objects");
                        checkLocoList(lines);
                    } 
                    else if (lines[0].startsWith("<REPLY queryObjects(10")){
                        //System.out.println("Reply " + m + " " + getSystemPrefix() + " " + this);
                        //First line is the response header, the last line is the OK
                        for (int i = 1; i<lines.length-1; i++) {
                            if (lines[i].contains("addr[")) { // skip odd lines
                                String[] objectdetail = lines[i].split(" ");
                                EcosLocoAddress tmploco;
                                //The first part of the messages is always the object id.
                                strde = objectdetail[0];
                                int object = Integer.parseInt(strde);
                                if ( (1000<=object) && (object<2000)) {
                                    tmploco = provideByEcosObject(strde);
                                    if(tmploco.getCV7()==null){
                                        tmploco.setCV7("0");
                                        getEcosCVs(tmploco);
                                    }
                                } else return;
                                if (lines[i].contains("addr")){
                                    tmploco.setEcosLocoAddress(GetEcosObjectNumber.getEcosObjectNumber(lines[i], "addr[", "]"));
                                }
                                if (lines[i].contains("name")){
                                    tmploco.setEcosDescription(getName(lines[i]));
                                }
                                if (lines[i].contains("protocol")){
                                    tmploco.setProtocol(getProtocol(lines[i]));
                                }
                                register(tmploco);
                                locoToRoster.addToQueue(tmploco);
                             }
                        }
                        locoToRoster.processQueue();
                    }
                } else if (replyType.equals("get")){
                //Need to really check if this all fits together correctly!  Might need to get the loco id from the reply string to
                //identify the loco correctly
                    EcosLocoAddress tmploco;
                    if((lines[1].contains("speed[")) || (lines[1].contains("dir["))){
                        log.debug("Forwarding on State change for " + ecosObjectId);
                        String strLocoObject = Integer.toString(ecosObjectId);
                        tmploco = _tecos.get(strLocoObject);
                        if (tmploco!=null){
                            tmploco.reply(m);
                        }
                        return;
                    }

                    //int object = GetEcosObjectNumber.getEcosObjectNumber(lines[0], "(", ",");
                    //if ( (1000<=object) && (object<2000)) {
                    tmploco = provideByEcosObject(""+ecosObjectId);
                    if(tmploco.getCV7()==null){
                        tmploco.setCV7("0");
                        getEcosCVs(tmploco);
                    }
                    //} else return;
                    for(int i =1; i<lines.length-1; i++) {
                        if(lines[i].contains("cv[")){
                            int startcvnum = lines[i].indexOf("[")+1;
                            int endcvnum = (lines[i].substring(startcvnum)).indexOf(",")+startcvnum;
                            int cvnum = Integer.parseInt(lines[i].substring(startcvnum, endcvnum));
                            
                            int startcvval = (lines[i].substring(endcvnum)).indexOf(", ")+endcvnum+2;
                            int endcvval = (lines[i].substring(startcvval)).indexOf("]")+startcvval;
                            String cvval = lines[i].substring(startcvval, endcvval);
                            switch(cvnum){
                                case 7 :    tmploco.setCV7(cvval);
                                            break;
                                case 8  :   tmploco.setCV8(cvval);
                                            locoToRoster.processQueue();
                                            break;
                                default : break;
                            }
                        }
                        if (lines[i].contains("addr")){
                            tmploco.setEcosLocoAddress(GetEcosObjectNumber.getEcosObjectNumber(lines[i], "addr[", "]"));
                        }
                        if (lines[i].contains("name")){
                            tmploco.setEcosDescription(getName(lines[i]));
                        }
                        if (lines[i].contains("protocol")){
                            tmploco.setProtocol(getProtocol(lines[i]));
                        }
                        register(tmploco);
                    }
                } 
            }
        }
    }
    
    String getName(String line){
        int startval;
        int endval;
        startval=line.indexOf("name[")+6;
        endval=(line.substring(startval)).indexOf("]")+startval-1;
        return line.substring(startval, endval);
    }
    
    String getProtocol(String line){
        int startval;
        int endval;
        startval=line.indexOf("protocol[")+9;
        endval=(line.substring(startval)).indexOf("]")+startval;
        return line.substring(startval, endval);
    }
    
    /* This is used after an event update form the ecos informing us of a change in the 
     * loco list, we have to determine if it is an addition or delete.
     * We should only ever do either a remove or an add in one go, if we are adding the loco
     * to the roster otherwise this causes a problem with the roster list.
     */
    void checkLocoList(String[] ecoslines){
        String loco;
        for(int i=1; i<ecoslines.length-1; i++){
            loco = ecoslines[i];
            loco = loco.replaceAll("[\\n\\r]","");
            if(getByEcosObject(loco)==null){
                log.debug("We are to add loco " + loco + " to the Ecos Loco List");
                EcosMessage mout = new EcosMessage("get(" + loco + ", addr, name, protocol)");
                tc.sendEcosMessage(mout, this);
            }
        }
        
        String[] jmrilist = getEcosObjectArray();
        boolean nomatch = true;
        for(int i=0; i<jmrilist.length; i++){
            nomatch=true;
            for(int k=1; k<ecoslines.length-1;k++){
                loco = ecoslines[k];
                loco = loco.replaceAll("[\\n\\r]","");
                if (loco.equals(jmrilist[i])){
                    nomatch=false;
                    break;
                }
            }
            if(nomatch){
                //System.out.println("We do not have a match, therefore this should be deleted from the Ecos loco Manager " + jmrilist[i]);
                if(getByEcosObject(jmrilist[i]).getRosterId()!=null){
                    final String rosterid = getByEcosObject(jmrilist[i]).getRosterId();
                    final Roster _roster = Roster.instance();
                    final RosterEntry re = _roster.entryFromTitle(rosterid);
                    re.deleteAttribute(p.getRosterAttribute());
                    re.writeFile(null, null, null);
                    Roster.writeRosterFile();
                    if(p.getRemoveLocoFromJMRI()==0x02){
                        _roster.removeEntry(re);
                        Roster.writeRosterFile();
                    } else if (p.getRemoveLocoFromJMRI()==0x00) {
                        final JDialog dialog = new JDialog();
                        dialog.setTitle("Remove Roster Entry From JMRI?");
                        dialog.setLocation(300,200);
                        dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                        JPanel container = new JPanel();
                        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                        container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                        JLabel question = new JLabel(rosterid + " has been removed from the Ecos do you want to remove it from JMRI?");
                        question.setAlignmentX(Component.CENTER_ALIGNMENT);
                        container.add(question);
                        final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
                        remember.setFont(remember.getFont().deriveFont(10f));
                        remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                        //user preferences do not have the save option, but once complete the following line can be removed
                        //Need to get the method to save connection configuration.
                        remember.setVisible(true);
                        JButton yesButton = new JButton("Yes");
                        JButton noButton = new JButton("No");
                        JPanel button = new JPanel();
                        button.setAlignmentX(Component.CENTER_ALIGNMENT);
                        button.add(yesButton);
                        button.add(noButton);
                        container.add(button);

                        noButton.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                if(remember.isSelected()){
                                    p.setRemoveLocoFromJMRI(0x01);
                                }
                                dialog.dispose();
                            }
                        });

                        yesButton.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                if(remember.isSelected()) {
                                    p.setRemoveLocoFromJMRI(0x02);
                                }
                                setLocoToRoster();
                                _roster.removeEntry(re);
                                Roster.writeRosterFile();
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
                //Even if we do not delete the loco from the roster, we need to remove it from the ecos list.
                deregister(getByEcosObject(jmrilist[i]));
            }
        }
    }
    
    public void message(EcosMessage m){
        
    }
    /*
    *The purpose of this is to get some of the basic cv details that are required
    *for selecting the decoder mfg and family in the roster file.
    *This might work as sending a single request rather than multiple.
    */
    private void getEcosCVs(EcosLocoAddress tmploco){
        tc.addEcosListener(this);
        // ask to be notified
        // We won't look to add new locos created on the ecos yet this can be added in at a later date.

        EcosMessage m = new EcosMessage("get("+tmploco.getEcosObject()+", cv[7])");
        tc.sendEcosMessage(m, this);
        
        m = new EcosMessage("get("+tmploco.getEcosObject()+", cv[8])");
        tc.sendEcosMessage(m, this);
        
        
    }
    
    EcosLocoToRoster locoToRoster;
    
    Thread waitPrefLoad;
    
    private class waitPrefLoad implements Runnable {
        public void run() {
            log.debug("Waiting for the Ecos preferences to be loaded before loading the loco database on the Ecos");
            while(!wait) {
                waitForPrefLoad();
            }
            loadData();
        }

        boolean wait = false;
        int x = 0;
        
        private void waitForPrefLoad() {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {System.out.println(e.toString());}
            wait = p.getPreferencesLoaded();
            if (x>=100){
                wait=true;
                System.out.println(x);
                log.error("Timeout occured on waiting for the Ecos preferences to be loaded");
            }
            x++;
        }
    }
    
    public void refreshItems(){
        // ask to be notified about newly created turnouts on the layout.
        EcosMessage m = new EcosMessage("request(10, view)");
        tc.sendEcosMessage(m, this);
        monitorLocos(monitorState);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoAddressManager.class.getName());
}
