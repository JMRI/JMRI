package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

    /**
     * Table Action for dealing with all the tables in a single view
     * with a list option to the left hand side.
     * <P>
     * @author	Bob Jacobsen   Copyright (C) 2003
     * @author	Kevin Dickerson   Copyright (C) 2009
     * @version	$Revision: 1.3 $
     */

public class ListedTableAction extends AbstractAction {

    String gotoListItem = null;
    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */

   public ListedTableAction(String s, String selection) {
        super(s);
        gotoListItem = selection;
    }
    
   public ListedTableAction(String s) {
        super(s);
    }
    
    public ListedTableAction() { this("Listed Table Access");}
    
    ListedTableFrame f;
    
    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        // create the frame
        f = new ListedTableFrame(){
        };
        setTitle();
        addToFrame(f);
        f.gotoListItem(gotoListItem);
        f.pack();
        f.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    public void addToFrame(ListedTableFrame f) {
    }
    
    void setTitle() { //Note required as sub-panels will set them
    }
    
    String helpTarget() {
        return "package.jmri.jmrit.beantable.ListedTableAction";
    }
    
}