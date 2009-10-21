// AbstractTableAction.java

package jmri.jmrix.ecos.swing.locodatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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

    public AbstractTableAction(String actionName) {
        super(actionName);
    }

    public AbstractTableAction() {
        super();
    }

    
    EcosLocoTableDataModel m;

    //static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    //static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific NamedBean type
     */
    abstract void createModel();

    /**
     * Include the correct title
     */
    abstract void setTitle();

    EcosLocoTableFrame f;

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new EcosLocoTableFrame(m, helpTarget()){
            /**
                    * Include an "add" button
                    */
            void extras() {
                JButton saveButton = new JButton("Save");
                addToBottomBox(saveButton);
                saveButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        savePressed(e);
                    }
                });
                saveButton.setVisible(false);
            }
        };
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }
    

    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new EcosLocoTableFrame(m, helpTarget()){
            /**
             * Include an "save" button
             */
            void extras() {
                JButton saveButton = new JButton("Save");
                addToBottomBox(saveButton);
                saveButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        savePressed(e);
                    }
                });
            }
        };
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    /**
     * Allow subclasses to add to the frame
     * without have to actually suclass the BeanTableDataFrame
     */
    public void addToFrame(EcosLocoTableFrame f) {
    }

    /**
     * Specify the JavaHelp target for this specific panel
     */
    String helpTarget() {
        return "index";  // by default, go to the top
    }

    abstract void savePressed(ActionEvent e);

}
/* @(#)AbstractTableAction.java */
