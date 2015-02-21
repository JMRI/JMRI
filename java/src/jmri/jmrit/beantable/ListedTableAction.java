package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Action for dealing with all the tables in a single view with a list
 * option to the left hand side.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version	$Revision$
 */
public class ListedTableAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 6097143838837199839L;
    String gotoListItem = null;
    String title = rbean.getString("TitleListedTable");
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s
     */
    public ListedTableAction(String s, String selection) {
        super(s);
        title = s;
        gotoListItem = selection;
    }

    public ListedTableAction(String s, String selection, int divider) {
        super(s);
        title = s;
        gotoListItem = selection;
        dividerLocation = divider;
    }

    public ListedTableAction(String s, int divider) {
        super(s);
        title = s;
        dividerLocation = divider;
    }

    public ListedTableAction(String s) {
        super(s);
        title = s;
    }

    public ListedTableAction() {
        this(rbean.getString("TitleListedTable"));
    }

    ListedTableFrame f;
    int dividerLocation = 0;

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        /* create the frame in a seperate thread outside of swing so that we do not 
         hog the swing thread which is also used for connection traffic */
        Runnable r = new Runnable() {
            public void run() {
                f = new ListedTableFrame(title) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = -2029385586619585289L;
                };
                f.initComponents();
                addToFrame(f);

                f.gotoListItem(gotoListItem);
                f.pack();

                f.setDividerLocation(dividerLocation);
                f.setVisible(true);
            }
        };
        Thread thr = new Thread(r, "Listed Table Generation");
        thr.start();
    }

    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    public void addToFrame(ListedTableFrame f) {
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.ListedTableAction";
    }

    static Logger log = LoggerFactory.getLogger(ListedTableAction.class.getName());

}
