// AbstractTableAction.java

package jmri.jmrit.beantable;

import jmri.Manager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.util.HashMap;
import javax.swing.JTable;
import jmri.util.com.sun.TableSorter;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision$
 */

abstract public class AbstractTableAction extends AbstractAction {

    public AbstractTableAction(String actionName) {
        super(actionName);
    }
    
    public AbstractTableAction(String actionName, Object option) {
        super(actionName);
    }

    protected BeanTableDataModel m;

    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific NamedBean type
     */

    protected abstract void createModel();

    /**
     * Include the correct title
     */

    protected abstract void setTitle();
    
    protected BeanTableFrame f;

    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableSorter sorter = new TableSorter(m);
        JTable dataTable = m.makeJTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());
        
        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);
        
        // create the frame
        f = new BeanTableFrame(m, helpTarget(), dataTable){
            /**
             * Include an "add" button
             */
            void extras() {
                if(includeAddButton){
                    JButton addButton = new JButton(this.rb.getString("ButtonAdd"));
                    addToBottomBox(addButton, this.getClass().getName());
                    addButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            addPressed(e);
                        }
                    });
                }
            }
        };
        setMenuBar(f);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }
    
    public BeanTableDataModel getTableDataModel(){
        createModel();
        return m;
    }
    
    public void setFrame(BeanTableFrame frame){
        f=frame;
    }
    
    /**
     * Allow subclasses to add to the frame
     * without have to actually subclass the BeanTableDataFrame
     */
    public void addToFrame(BeanTableFrame f) {
    }
    
    /**
     * If the subClass is being included in a greater tabbed frame, then this 
     * method is used to add the details to the tabbed frame
     */
    public void addToPanel(AbstractTableTabAction f) { }
    
    /**
     * If the subClass is being included in a greater tabbed frame, then this 
     * is used to specify which manager the subclass should be using.
     */
    protected void setManager(Manager man) { }
    /**
     * Allow subclasses to add alter the frames Menubar
     * without have to actually subclass the BeanTableDataFrame
     */
    public void setMenuBar(BeanTableFrame f){
    }

    public JPanel getPanel(){
        return null;
    }
    
    public void dispose() {
        if (m!=null){
            m.dispose();
        }
    }

    /**
     * Specify the JavaHelp target for this specific panel
     */
    protected String helpTarget() {
        return "index";  // by default, go to the top
    }
    
    public String getClassDescription() { return "Abstract Table Action"; }
    
    public void setMessagePreferencesDetails(){
        HashMap< Integer,String> options = new HashMap< Integer,String>(3);
        options.put(0x00, rb.getString("DeleteAsk"));
        options.put(0x01, rb.getString("DeleteNever"));
        options.put(0x02, rb.getString("DeleteAlways"));
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).messageItemDetails(getClassName(), "deleteInUse", rb.getString("DeleteItemInUse"), options, 0x00);
    }

    protected abstract String getClassName();
    
    public boolean includeAddButton(){ return includeAddButton; }
    
    protected boolean includeAddButton = true;

    /**
    * Used with the Tabbed instances of table action, so that the print option 
    * is handled via that on the appropriate tab.
    */
    public void print(javax.swing.JTable.PrintMode mode, java.text.MessageFormat headerFormat, java.text.MessageFormat footerFormat){ log.error("Caught here");}

    protected abstract void addPressed(ActionEvent e);
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractTableAction.class.getName());
}
/* @(#)AbstractTableAction.java */
