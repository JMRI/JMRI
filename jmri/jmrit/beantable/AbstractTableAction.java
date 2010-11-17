// AbstractTableAction.java

package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.14 $
 */

abstract public class AbstractTableAction extends AbstractAction {

    public AbstractTableAction(String actionName) {
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

        // create the frame
        f = new BeanTableFrame(m, helpTarget()){
            /**
             * Include an "add" button
             */
            void extras() {
                JButton addButton = new JButton(this.rb.getString("ButtonAdd"));
                addToBottomBox(addButton);
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addPressed(e);
                    }
                });
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
     * Allow subclasses to add alter the frames Menubar
     * without have to actually subclass the BeanTableDataFrame
     */
    public void setMenuBar(BeanTableFrame f){
    }

    public JPanel getPanel(){
        return null;
    }

    /**
     * Specify the JavaHelp target for this specific panel
     */
    protected String helpTarget() {
        return "index";  // by default, go to the top
    }

    protected abstract void addPressed(ActionEvent e);

}
/* @(#)AbstractTableAction.java */
