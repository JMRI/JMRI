package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
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
    String title = Bundle.getMessage("TitleListedTable");

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
        this(Bundle.getMessage("TitleListedTable"));
    }

    ListedTableFrame f;
    int dividerLocation = 0;

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        /* create the frame outside of swing so that we do not 
         hog Swing/AWT execution, then finally display on Swing */
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

                try {
                    javax.swing.SwingUtilities.invokeAndWait(()->{
                        f.gotoListItem(gotoListItem);
                        f.pack();
                        f.setDividerLocation(dividerLocation);
                        f.setVisible(true);
                    });
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    log.error("failed to set ListedTable visible", ex );
                } catch (InterruptedException ex) {
                    log.error("interrupted while setting ListedTable visible", ex );
                }
                
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

    private final static Logger log = LoggerFactory.getLogger(ListedTableAction.class.getName());

}
