// AbstractTableAction.java

package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

abstract public class AbstractTableAction extends AbstractAction {

    public AbstractTableAction(String s) { super(s);}
    public AbstractTableAction() { }

    BeanTableDataModel m;

    final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific NamedBean type
     */
    abstract void createModel();

    /**
     * Include the correct title
     */
    abstract void setTitle();

    BeanTableFrame f;

    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new BeanTableFrame(m){
            /**
             * Include an "add" button
             */
            void extras() {
                JButton addButton = new JButton(this.rb.getString("ButtonAdd"));
                this.getContentPane().add(addButton);
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addPressed(e);
                    }
                });
            }
        };
        setTitle();
        f.pack();
        f.show();
    }

    abstract void addPressed(ActionEvent e);

}
/* @(#)AbstractTableAction.java */
