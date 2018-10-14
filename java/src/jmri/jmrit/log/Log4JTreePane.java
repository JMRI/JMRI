package jmri.jmrit.log;

import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Show the current Log4J Logger tree; not dynamic.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
public class Log4JTreePane extends jmri.util.swing.JmriPanel {

    /**
     * Provide a recommended title for an enclosing frame.
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemLogTreeAction");
    }

    /**
     * Provide menu items
     */
    //public List<JMenu> getMenus() { return null; }
    public Log4JTreePane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * 2nd stage of initialization, invoked after the constructor is complete.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initComponents() {
        LoggerRepository repo = Logger.getRootLogger().getLoggerRepository();

        List<String> list = new ArrayList<String>();
        for (java.util.Enumeration<Logger> e = repo.getCurrentLoggers(); e.hasMoreElements();) {
            Logger l = e.nextElement();
            list.add(l.getName() + " - "
                    + (l.getLevel() != null
                            ? "[" + l.getLevel().toString() + "]"
                            : "{" + Logger.getRootLogger().getLevel().toString() + "}"));
        }
        java.util.Collections.sort(list);
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            result.append(s).append("\n");
        }

        JTextArea text = new JTextArea();
        text.setText(result.toString());
        JScrollPane scroll = new JScrollPane(text);
        add(scroll);

        // start scrolled to top
        text.setCaretPosition(0);
        JScrollBar b = scroll.getVerticalScrollBar();
        b.setValue(b.getMaximum());
    }

    /**
     * 3rd stage of initialization, invoked after Swing components exist.
     */
    @Override
    public void initContext(Object context) {
    }

    @Override
    public void dispose() {
    }

}
