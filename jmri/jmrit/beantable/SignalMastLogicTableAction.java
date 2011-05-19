package jmri.jmrit.beantable;

import java.awt.Toolkit;
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

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import jmri.util.com.sun.TableSorter;

public class SignalMastLogicTableAction extends AbstractTableAction implements PropertyChangeListener{

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
    	JTable dataTable = makeJTable(sorter);
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
        JMenu pathMenu = new JMenu(rb.getString("Pairs"));
        menuBar.add(pathMenu);
        JMenuItem item = new JMenuItem(rb.getString("MenuItemAutoGen"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    autoCreatePairs(finalF);
        	}
            });
    
    }
    
    ArrayList<Hashtable<SignalMastLogic, SignalMast>> signalMastLogicList = null;
    //Hashtable<SignalMastLogic, ArrayList<SignalMast>> signalMastLogicList = null;
    
    
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
               // updateNameList();
                if (e.getPropertyName().equals("length")) {
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
                    else {
                    // a value changed.  Find it, to avoid complete redraw
                    //String object = ((SignalMastLogic)e.getSource());
                    //if (log.isDebugEnabled()) log.debug("Update cell "+signalMastLogicList.indexOf(name)+","
                     //                                   +VALUECOL+" for "+name);
                    // since we can add columns, the entire row is marked as updated
                        int row = signalMastLogicList.indexOf((SignalMastLogic)e.getSource());
                        fireTableRowsUpdated(row, row);
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
                else if (e.getPropertyName().equals("autoGenerateComplete")){
                    JOptionPane.showMessageDialog(null, "Generation of Signalling Pairs Completed");
                }
            }
            

    //}

            /**
             * Is this property event announcing a change this table should display?
             * <P>
             * Note that events will come both from the NamedBeans and also from the manager
             */
            @Override
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
            
            public void refreshSelections(){
                fireTableRowsUpdated(0, getRowCount());
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
    
            @Override
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

            @Override
            synchronized public void dispose() {

                getManager().removePropertyChangeListener(this);
                if (signalMastLogicList != null) {
                    for (int i = 0; i< signalMastLogicList.size(); i++) {
                        SignalMastLogic b = getLogicFromRow(i);
                        if (b!=null) b.removePropertyChangeListener(this);
                    }
                }
            }

            @Override
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

            @Override
            protected void configDeleteColumn(JTable table) {
            // have the delete column hold a button
                setColumnToHoldButton(table, DELCOL,
                    new JButton(AbstractTableAction.rb.getString("ButtonDelete")));
            }

            
        };
    }
    
    protected void setTitle() {
        f.setTitle(AbstractTableAction.rb.getString("TitleSignalMastLogicTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalMastLogicTable";
    }
    
    protected void addPressed(ActionEvent e){
        sigLog.setMast(null, null);
        sigLog.actionPerformed(e);
    }
    
    JPanel update;
    
    void autoCreatePairs(jmri.util.JmriJFrame f) {
        
        int retval = JOptionPane.showOptionDialog(f, rb.getString("AutoGenSignalMastLogicMessage"), rb.getString("AutoGenSignalMastLogicTitle"),
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (retval == 0) {
            try {
            
                /*update = new JPanel();
                progressMonitor = new ProgressMonitor(update,
                                  "Running a Long Task",
                                  "", 0, 100);
                progressMonitor.setProgress(0);
                InstanceManager.signalMastLogicManagerInstance().addPropertyChangeListener(this);
                
                JFrame frame = new JFrame("ProgressMonitorDemo");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                startButton = new JButton("Start");
                startButton.setActionCommand("start");
                startButton.addActionListener(this);

                taskOutput = new JTextArea(5, 20);
                taskOutput.setMargin(new Insets(5,5,5,5));
                taskOutput.setEditable(false);
                
                update.add(startButton, BorderLayout.PAGE_START);
                update.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
                update.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                frame.setContentPane(update);
                
                frame.pack();
                frame.setVisible(true);*/
                
                Hashtable<SignalMast, ArrayList<SignalMast>> result = InstanceManager.signalMastLogicManagerInstance().automaticallyDiscoverSignallingPairs();
                

                
                
                
            } catch (jmri.JmriException e){
                JOptionPane.showMessageDialog(null, e.toString());
            }
            
        }
        
        
        
/*        JFrame frame = new JFrame("ProgressMonitorDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ProgressMonitorDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        //newContentPane);

        //Display the window.

        
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        JPanel update = new JPanel();
        update.add(startButton, BorderLayout.PAGE_START);
        update.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        update.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.setContentPane(update);
        
        frame.pack();
        frame.setVisible(true);*/
        
    }
    
    private ProgressMonitor progressMonitor;
    private JButton startButton;
    private JTextArea taskOutput;
    //private Task task;
    int numberOfEvents = 0;
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("autoGenerateTotal")){
            numberOfEvents = (Integer) evt.getNewValue();
        }
        if (evt.getPropertyName().equals("autoGenerateState") ) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message =
                String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            taskOutput.append(message);
            if (progressMonitor.isCanceled()) {
                Toolkit.getDefaultToolkit().beep();
                /*if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                    taskOutput.append("Task canceled.\n");
                } else {
                    taskOutput.append("Task completed.\n");
                }
                /*startButton.setEnabled(true);*/
            }
        }

    }

    
    jmri.jmrit.signalling.SignallingAction sigLog = new jmri.jmrit.signalling.SignallingAction();

    
    protected String getClassName() { return SignalMastLogicTableAction.class.getName(); }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastLogicTableAction.class.getName());
}