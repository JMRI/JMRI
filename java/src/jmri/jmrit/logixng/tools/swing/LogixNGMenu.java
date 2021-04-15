package jmri.jmrit.logixng.tools.swing;

import javax.swing.JMenu;

/**
 * Create a "LogixNG" menu
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class LogixNGMenu extends JMenu {

    public LogixNGMenu(String name) {
        this();
        setText(name);
    }

    public LogixNGMenu() {
        super();

        setText(Bundle.getMessage("MenuLogixNG"));

        add(new StartStopAllLogixNGsAction(Bundle.getMessage("MenuStartLogixNG"), true));
        add(new StartStopAllLogixNGsAction(Bundle.getMessage("MenuStopLogixNG"), false));
        add(new TimeDiagramAction());
        add(new ImportLogixAction());
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGMenu.class);
}
