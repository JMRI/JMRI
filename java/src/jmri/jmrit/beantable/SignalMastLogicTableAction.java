package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.SignalMastLogicManager;
import jmri.InstanceManager;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;
import jmri.util.JmriJFrame;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
//import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.BorderFactory;
import jmri.util.com.sun.TableSorter;
import java.awt.event.MouseEvent;
import java.awt.Component;

public class SignalMastLogicTableAction extends AbstractTableAction{

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
   public SignalMastLogicTableAction(String s) {
        super(s);
    }
    public SignalMastLogicTableAction() { this(AbstractTableAction.rb.getString("TitleSignalMastLogicTable"));}
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableSorter sorter = new TableSorter(m);
    	JTable dataTable = m.makeJTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());
        // create the frame
        f = new jmri.jmrit.beantable.BeanTableFrame(m, helpTarget(), dataTable){

        };
        setMenuBar(f);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }
    
    public void setMenuBar(BeanTableFrame f){
        final jmri.util.JmriJFrame finalF = f;			// needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        JMenu pathMenu = new JMenu(Bundle.getMessage("Tools"));
        menuBar.add(pathMenu);
        JMenuItem item = new JMenuItem(Bundle.getMessage("MenuItemAutoGen"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    autoCreatePairs(finalF);
        	}
            });
        item = new JMenuItem(Bundle.getMessage("MenuItemAutoGenSections"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
               ((jmri.managers.DefaultSignalMastLogicManager) InstanceManager.signalMastLogicManagerInstance()).generateSection();
        	}
            });
    
    }
    
    ArrayList<Hashtable<SignalMastLogic, SignalMast>> signalMastLogicList = null;  
    
    protected void createModel() {
        m = new BeanTableDataModel() {

            static public final int SOURCECOL = 0;
            static public final int SOURCEAPPCOL = 1;
            static public final int DESTCOL = 2;
            static public final int DESTAPPCOL = 3;
            static public final int COMCOL = 4;
            static public final int DELCOL = 5;
            static public final int ENABLECOL = 6;
            static public final int EDITLOGICCOL = 7;


            //We have to set a manager first off, but this gets replaced.
            protected SignalMastLogicManager getManager() { return InstanceManager.signalMastLogicManagerInstance();}

            
            /*public EcosLocoAddress getByDccAddress(int address) {return getManager().getByDccAddress(address);}*/
            
            public String getValue(String s) {
                return "Set";
            }
            protected String getMasterClassName() { return getClassName(); }
            public void clickOn(jmri.NamedBean t) { }
            
            @Override
            protected synchronized void updateNameList() {
                // first, remove listeners from the individual objects
                if (signalMastLogicList != null) {
                    for (int i = 0; i< signalMastLogicList.size(); i++) {
                        // if object has been deleted, it's not here; ignore it
                        Hashtable<SignalMastLogic, SignalMast> b = signalMastLogicList.get(i);
                        Enumeration<SignalMastLogic> en = b.keys();
                        while (en.hasMoreElements()) {
                            SignalMastLogic sm = en.nextElement();
                            SignalMast dest = b.get(sm);
                            sm.removePropertyChangeListener(this);
                            sm.getSourceMast().removePropertyChangeListener(this);
                            dest.removePropertyChangeListener(this);
                        }
                    }
                }
                ArrayList<SignalMastLogic> source = getManager().getSignalMastLogicList();
                signalMastLogicList = new ArrayList<Hashtable<SignalMastLogic, SignalMast>>();
                for (int i = 0; i< source.size(); i++){
                    ArrayList<SignalMast> destList = source.get(i).getDestinationList();
                    source.get(i).addPropertyChangeListener(this);
                    source.get(i).getSourceMast().addPropertyChangeListener(this);
                    for (int j = 0; j<destList.size(); j++){
                        Hashtable<SignalMastLogic, SignalMast> hash = new Hashtable<SignalMastLogic, SignalMast>(1);
                        hash.put(source.get(i), destList.get(j));
                        destList.get(j).addPropertyChangeListener(this);
                        signalMastLogicList.add(hash);
                    }
                }
            }
            
            //Will need to redo this so that we work out the row number from looking in the signalmastlogiclist.
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if(suppressUpdate)
                    return;
               // updateNameList();
                if (e.getPropertyName().equals("length") ||  e.getPropertyName().equals("updatedDestination") ||  e.getPropertyName().equals("updatedSource")) {
                    updateNameList();
                    //log.debug("Table changed length to "+signalMastLogicList.size());
                    fireTableDataChanged();
                } else if (e.getSource() instanceof SignalMastLogic) {
                    SignalMastLogic logic = (SignalMastLogic)e.getSource();
                    if (matchPropertyName(e)){
                        for (int i = 0; i< signalMastLogicList.size(); i++) {
                            Hashtable<SignalMastLogic, SignalMast> b = signalMastLogicList.get(i);
                            Enumeration<SignalMastLogic> en = b.keys();
                            while (en.hasMoreElements()) {
                                SignalMastLogic sm = en.nextElement();
                                if (sm==logic){
                                    fireTableRowsUpdated(i, i);
                                }
                            }
                        }
                    }
                } else if (e.getSource() instanceof jmri.SignalMast){
                    jmri.SignalMast sigMast = (jmri.SignalMast)e.getSource();
                    for (int i = 0; i<signalMastLogicList.size(); i++){
                        Hashtable<SignalMastLogic, SignalMast> b = signalMastLogicList.get(i);
                        Enumeration<SignalMastLogic> en = b.keys();
                        while (en.hasMoreElements()) {
                            SignalMastLogic sm = en.nextElement();
                            //SignalMast dest = b.get(sm);
                            if(sm.getSourceMast()==sigMast)
                                fireTableRowsUpdated(i, i);
                        }
                    }
                }
            }
            

    //}

            /**
             * Is this property event announcing a change this table should display?
             * <P>
             * Note that events will come both from the NamedBeans and also from the manager
             */
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return ((e.getPropertyName().indexOf("Comment")>=0) || (e.getPropertyName().indexOf("Enable")>=0));
            }

            @Override
            public int getColumnCount(){ 
                return EDITLOGICCOL+1;
            }
            
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col==COMCOL) {
                    getLogicFromRow(row).setComment((String)value, getDestMastFromRow(row));
                }  else if (col==EDITLOGICCOL) {
                        class WindowMaker implements Runnable {
                        int row;
                        WindowMaker(int r){
                            row = r;
                        }
                        public void run() {
                                //Thread.yield();
                                editLogic(row, 0);
                            }
                        }
                    WindowMaker t = new WindowMaker(row);
					javax.swing.SwingUtilities.invokeLater(t);
                    
                } else if (col==DELCOL) {
                    // button fired, delete Bean
                    deleteLogic(row, col);
                } else if (col==ENABLECOL) {
                    boolean enable = ((Boolean)value).booleanValue();
                    if (enable)
                        getLogicFromRow(row).setEnabled(getDestMastFromRow(row));
                    else
                        getLogicFromRow(row).setDisabled(getDestMastFromRow(row));
                }
            }
            
            @Override
            public String getColumnName(int col) {
                 switch (col) {
                case SOURCECOL: return AbstractTableAction.rb.getString("Source");
                case DESTCOL: return AbstractTableAction.rb.getString("Destination");
                case SOURCEAPPCOL: return AbstractTableAction.rb.getString("SignalMastAppearance");
                case DESTAPPCOL: return AbstractTableAction.rb.getString("SignalMastAppearance");
                case COMCOL: return AbstractTableAction.rb.getString("Comment");
                case DELCOL: return AbstractTableAction.rb.getString("ButtonDelete");
                case EDITLOGICCOL: return AbstractTableAction.rb.getString("ButtonEdit");
                case ENABLECOL: return AbstractTableAction.rb.getString("ColumnHeadEnabled");
                default: return "unknown";
                }
            }
            
            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {
                case SOURCECOL:
                case DESTCOL:
                case SOURCEAPPCOL:
                case COMCOL:
                case DESTAPPCOL:
                    return String.class;
                case ENABLECOL:
                        return Boolean.class;
                case EDITLOGICCOL:
                case DELCOL:
                    return JButton.class;
                default:
                    return null;
                }
            }
            
            @Override
            public boolean isCellEditable(int row, int col) {
                switch (col) {
                case COMCOL:
                case EDITLOGICCOL:
                case DELCOL:
                case ENABLECOL:
                    return true;
                default:
                    return false;
                }
            }

            void editLogic(int row, int col){
                sigLog.setMast(getLogicFromRow(row).getSourceMast(), getDestMastFromRow(row));
                sigLog.actionPerformed(null);
            }

            void deleteLogic(int row, int col){
            //This needs to be looked at
                InstanceManager.signalMastLogicManagerInstance().removeSignalMastLogic(getLogicFromRow(row), getDestMastFromRow(row));
            }
            
            public SignalMast getDestMastFromRow(int row){
                    // if object has been deleted, it's not here; ignore it
                Hashtable<SignalMastLogic, SignalMast> b = signalMastLogicList.get(row);
                Enumeration<SignalMastLogic> en = b.keys();
                while (en.hasMoreElements()) {
                    return b.get(en.nextElement());
                }
                return null;
            }
            
            public SignalMastLogic getLogicFromRow(int row){
                Hashtable<SignalMastLogic, SignalMast> b = signalMastLogicList.get(row);
                Enumeration<SignalMastLogic> en = b.keys();
                while (en.hasMoreElements()) {
                    return en.nextElement();
                }
                return null;
            }
        
            @Override
            public int getPreferredWidth(int col) {
                switch (col) {
                case SOURCECOL:
                    return new JTextField(10).getPreferredSize().width;
                case COMCOL:
                    return 75;
                case DESTCOL:
                    return new JTextField(10).getPreferredSize().width;
                case EDITLOGICCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(5).getPreferredSize().width;
                case DELCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(5).getPreferredSize().width;
                case DESTAPPCOL:
                    return new JTextField(10).getPreferredSize().width;
                case SOURCEAPPCOL:
                    return new JTextField(10).getPreferredSize().width;
                case ENABLECOL:
                    return new JTextField(5).getPreferredSize().width;
                default:
                    //log.warn("Unexpected column in getPreferredWidth: "+col);
                    return new JTextField(8).getPreferredSize().width;
                }
            }
    
            public void configureTable(JTable table) {
                setColumnToHoldButton(table, EDITLOGICCOL,
                        new JButton("Edit"));
                table.getTableHeader().setReorderingAllowed(true);

                // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                // resize columns as requested
                for (int i=0; i<table.getColumnCount(); i++) {
                    int width = getPreferredWidth(i);
                    table.getColumnModel().getColumn(i).setPreferredWidth(width);
                }
                table.sizeColumnsToFit(-1);

               // configValueColumn(table);
                configDeleteColumn(table);
            }
            public NamedBean getBySystemName(String name) { return null;}
            public NamedBean getByUserName(String name) { return null;}
            
            synchronized public void dispose() {

                getManager().removePropertyChangeListener(this);
                if (signalMastLogicList != null) {
                    for (int i = 0; i< signalMastLogicList.size(); i++) {
                        SignalMastLogic b = getLogicFromRow(i);
                        if (b!=null) b.removePropertyChangeListener(this);
                    }
                }
            }
            
            public int getRowCount() {
                return signalMastLogicList.size();
            }
            
            @Override
            public Object getValueAt(int row, int col) {
                // some error checking
                if (row >= signalMastLogicList.size()){
                    log.debug("row is greater than list size");
                    return null;
                }
                SignalMastLogic b = getLogicFromRow(row);
                switch (col) {
                case SOURCECOL:
                    return getLogicFromRow(row).getSourceMast().getDisplayName();
                case DESTCOL:  // return user name
                    // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
                    return (b!=null) ? getDestMastFromRow(row).getDisplayName() : null;
                case SOURCEAPPCOL:  //
                    return (b!=null) ? b.getSourceMast().getAspect() : null;
                case DESTAPPCOL:  //
                    return (b!=null) ? getDestMastFromRow(row).getAspect() : null;
                case COMCOL:
                    return (b!=null) ? b.getComment(getDestMastFromRow(row)) : null;
                case DELCOL:
                    return AbstractTableAction.rb.getString("ButtonDelete");
                case EDITLOGICCOL:  //
                        return AbstractTableAction.rb.getString("ButtonEdit");
                case ENABLECOL:
                        return (b!=null) ? b.isEnabled(getDestMastFromRow(row)) : null;
                default:
                    //log.error("internal state inconsistent with table requst for "+row+" "+col);
                    return null;
                }
            }

            protected void configDeleteColumn(JTable table) {
            // have the delete column hold a button
                setColumnToHoldButton(table, DELCOL,
                    new JButton(AbstractTableAction.rb.getString("ButtonDelete")));
            }
            protected String getBeanType(){
                return "Signal Mast Logic";
            }
            
            @Override
            protected void showPopup(MouseEvent e){
            
            }
        };
    }
    
    protected void setTitle() {
        f.setTitle(AbstractTableAction.rb.getString("TitleSignalMastLogicTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalMastLogicTable";// NOI18N
    }
    
    protected void addPressed(ActionEvent e){
        sigLog.setMast(null, null);
        sigLog.actionPerformed(e);
    }
    
    JPanel update;
    boolean suppressUpdate = false;
    JmriJFrame signalMastLogicFrame = null;
    JLabel sourceLabel = new JLabel();
    
    void autoCreatePairs(jmri.util.JmriJFrame f) {
        if (!InstanceManager.layoutBlockManagerInstance().isAdvancedRoutingEnabled()){
            int response = JOptionPane.showConfirmDialog(null, Bundle.getMessage("EnableLayoutBlockRouting"));
            if (response == 0){
                InstanceManager.layoutBlockManagerInstance().enableAdvancedRouting(true);
                JOptionPane.showMessageDialog(null, Bundle.getMessage("LayoutBlockRoutingEnabled"));
            } else {
                return;
            }
        }
        signalMastLogicFrame = new JmriJFrame(Bundle.getMessage("DiscoverSignalMastPairs"), false, false);
        signalMastLogicFrame.setPreferredSize(null);
        JPanel panel1 = new JPanel();
        sourceLabel = new JLabel(Bundle.getMessage("DiscoveringSignalMastPairs"));
        panel1.add(sourceLabel);
        signalMastLogicFrame.add(panel1);
        signalMastLogicFrame.pack();
        signalMastLogicFrame.setVisible(true);
        
        final JCheckBox genSect = new JCheckBox(Bundle.getMessage("AutoGenSectionAfterLogic"));
        genSect.setToolTipText(Bundle.getMessage("AutoGenSectionAfterLogicToolTip"));
        Object[] params = {Bundle.getMessage("AutoGenSignalMastLogicMessage")," ", genSect}; 
        int retval = JOptionPane.showConfirmDialog(f, params, Bundle.getMessage("AutoGenSignalMastLogicTitle"), 
                                                  JOptionPane.YES_NO_OPTION);

        if (retval == 0) {
            InstanceManager.signalMastLogicManagerInstance().addPropertyChangeListener(propertyGenerateListener);
            //This process can take some time, so we do not want to hog the GUI thread
            Runnable r = new Runnable() {
            public void run() {
                //While the global discovery is taking place we remove the listener as this can result in a race condition.
                suppressUpdate=true;
                try {
                    InstanceManager.signalMastLogicManagerInstance().automaticallyDiscoverSignallingPairs();
                } catch (jmri.JmriException e){
                    InstanceManager.signalMastLogicManagerInstance().removePropertyChangeListener(propertyGenerateListener);
                    JOptionPane.showMessageDialog(null, e.toString());
                    signalMastLogicFrame.setVisible(false);
                }
                m.updateNameList();
                suppressUpdate=false;
                m.fireTableDataChanged();
                if(genSect.isSelected()){
                    ((jmri.managers.DefaultSignalMastLogicManager) InstanceManager.signalMastLogicManagerInstance()).generateSection();
                }
              }
            };
            Thread thr = new Thread(r, "Discover Signal Mast Logic");  // NOI18N
            thr.start();
            
        } else {
            signalMastLogicFrame.setVisible(false);
        }
    }
    
    protected transient PropertyChangeListener propertyGenerateListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("autoGenerateComplete")){// NOI18N
                if (signalMastLogicFrame!=null)
                    signalMastLogicFrame.setVisible(false);
                InstanceManager.signalMastLogicManagerInstance().removePropertyChangeListener(this);
                JOptionPane.showMessageDialog(null, Bundle.getMessage("SignalMastPairGenerationComplete"));
            } else if (evt.getPropertyName().equals("autoGenerateUpdate")){// NOI18N
                sourceLabel.setText((String)evt.getNewValue());
                signalMastLogicFrame.pack();
                signalMastLogicFrame.repaint();
            }
        }
    };
    
    jmri.jmrit.signalling.SignallingAction sigLog = new jmri.jmrit.signalling.SignallingAction();
    
    protected String getClassName() { return SignalMastLogicTableAction.class.getName(); }
    
    static final Logger log = LoggerFactory.getLogger(SignalMastLogicTableAction.class.getName());
}
