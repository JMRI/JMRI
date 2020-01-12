package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import jmri.SignalMast;
import jmri.jmrit.beantable.signalmast.SignalMastTableDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignalMastTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2009, 2010
 */
public class SignalMastTableAction extends AbstractTableAction<SignalMast> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public SignalMastTableAction(String actionName) {
        super(actionName);
    }

    public SignalMastTableAction() {
        this(Bundle.getMessage("TitleSignalMastTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Signal Masts.
     */
    @Override
    protected void createModel() {
        m = new SignalMastTableDataModel();
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSignalMastTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalMastTable";
    }

    // prepare the Add Signal Mast frame
    jmri.jmrit.beantable.signalmast.AddSignalMastJFrame addFrame = null;

    // has to agree with number in SignalMastDataModel
    final static int VALUECOL = BeanTableDataModel.VALUECOL;
    final static int SYSNAMECOL = BeanTableDataModel.SYSNAMECOL;

    @Override
    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            addFrame = new jmri.jmrit.beantable.signalmast.AddSignalMastJFrame();
        } else {
            addFrame.refresh();
        }
        addFrame.setVisible(true);
    }

    /**
     * Insert a table specific Tools menu.
     * Account for the Window and Help menus, which are already added to the menu bar
     * as part of the creation of the JFrame, by adding the Tools menu 2 places earlier
     * unless the table is part of the ListedTableFrame, that adds the Help menu later on.
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame f) {
        JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() -1; // count the number of menus to insert the TableMenu before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = {}", pos);
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                    offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }
        JMenu pathMenu = new JMenu(Bundle.getMessage("MenuTools"));
        menuBar.add(pathMenu, pos + offset);
        JMenuItem item = new JMenuItem(Bundle.getMessage("MenuItemRepeaters"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jmri.jmrit.beantable.signalmast.SignalMastRepeaterJFrame frame = new jmri.jmrit.beantable.signalmast.SignalMastRepeaterJFrame();
                frame.setVisible(true);
            }
        });
    }

    @Override
    protected String getClassName() {
        return SignalMastTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalMastTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastTableAction.class);

}
