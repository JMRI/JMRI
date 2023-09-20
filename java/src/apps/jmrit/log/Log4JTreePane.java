package apps.jmrit.log;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.util.*;

import javax.swing.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.*;

/**
 * Show the current Log4J Logger tree.
 *
 * @author Bob Jacobsen Copyright 2010
 * @author Steve Young Copyright(C) 2023
 * @since 2.9.4
 */
public class Log4JTreePane extends jmri.util.swing.JmriPanel {

    private JTextArea text;
    private JScrollPane scroll;
    private JComboBox<Level> levelSelectionComboBox;
    private JComboBox<String> categoryComboBox;
    private final static String ROOT_LEVEL_STRING = Bundle.getMessage("DataItemRootLoggingLevel");
    private final static Level[] SELECTABLE_LEVELS = new Level[]{ Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.OFF};

    /**
     * Provide a recommended title for an enclosing frame.
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemLogTreeAction");
    }

    public Log4JTreePane() {
        setLayout(new BorderLayout());
    }

    /**
     * 2nd stage of initialization, invoked after the constructor is complete.
     * 
     * Sets up the entire dialog box
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initComponents() {

        add(getEditLoggingLevelPanel(), BorderLayout.SOUTH);

        text = new JTextArea();
        text.setEditable(false);
        scroll = new JScrollPane(text);
        updateTextAreaAndCategorySelect();

        add(scroll, BorderLayout.CENTER);

        JPanel topP = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton(Bundle.getMessage("ButtonRefreshCategories"));
        refreshButton.addActionListener(this::refreshButtonPressed);
        topP.add(refreshButton);
        topP.add(new JLabel(Bundle.getMessage("FieldLogConfiguredInherited")));
        add(topP,BorderLayout.NORTH);
        
        // start scrolled to top
        text.setCaretPosition(0);
        JScrollBar b = scroll.getVerticalScrollBar();
        b.setValue(b.getMaximum());
    }

    /**
     * Set up the bottom part of the dialog where the user can change logging levels.
     * @return The JPanel, ready to use.
     */
    private JPanel getEditLoggingLevelPanel() {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Create a label explaining the combobox.
        JPanel categoryLabelPanel = new JPanel(new FlowLayout());
        categoryLabelPanel.add(new JLabel(Bundle.getMessage("LabelCategoryToChange")));

        // Create a JComboBox and populate it with logger names
        categoryComboBox = new JComboBox<>();
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(categoryComboBox);
        categoryComboBox.setToolTipText(Bundle.getMessage("ToolTipSelectLoggingLevel"));
        categoryComboBox.setEditable(true);

        JPanel catergoryComboboxPanel = new JPanel(new FlowLayout());
        catergoryComboboxPanel.add(categoryComboBox);

        levelSelectionComboBox = new JComboBox<>(SELECTABLE_LEVELS);
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(levelSelectionComboBox);
        levelSelectionComboBox.setSelectedItem(Level.DEBUG);

        // Expand the button slightly to avoid elipsis
        Dimension preferredSize = levelSelectionComboBox.getPreferredSize();
        preferredSize.width = (int) (preferredSize.width * 1.2);
        levelSelectionComboBox.setPreferredSize(preferredSize);
        levelSelectionComboBox.setToolTipText(Bundle.getMessage("ToolTipSelectLoggingLevelValue"));

        JPanel levelSelectPanel = new JPanel(new FlowLayout());
        levelSelectPanel.add(new JLabel(Bundle.getMessage("LabelNewLevelForAboveCategory")));
        levelSelectPanel.add(levelSelectionComboBox);

        JButton setLevelButton = new JButton(Bundle.getMessage("ButtonEditLoggingLevel"));
        setLevelButton.setToolTipText(Bundle.getMessage("ToolTipEditLoggingLevel"));
        setLevelButton.addActionListener(this::editButtonPressed);

        JPanel setButtonPanel = new JPanel(new FlowLayout());
        setButtonPanel.add(setLevelButton);

        p.add(categoryLabelPanel);
        p.add(catergoryComboboxPanel);
        p.add(levelSelectPanel);
        p.add(setButtonPanel);
        return p;
    }

    private void updateTextAreaAndCategorySelect(){
        List<LoggerInfo> loggersWithLevels = Log4JTreePane.getAllLoggersWithLevels();
        populateTextArea(loggersWithLevels);
        updateCategoryComboBox(loggersWithLevels);
    }

    private static List<LoggerInfo> getAllLoggersWithLevels() {
        HashMap<String, LoggerInfo> hs = new HashMap<>();
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        for (Logger category : loggerContext.getLoggers()) {
            hs.put(category.getName(), new LoggerInfo(category.getName() , category.getLevel()));
        }

        Configuration config = loggerContext.getConfiguration();
        Map<String, LoggerConfig> mm = config.getLoggers(); // does not include root logger
        mm.forEach((key, value) -> {
            hs.putIfAbsent(key, new LoggerInfo(value.getName() , value.getLevel()));
            hs.get(key).setInConfig();
        });
        // ensure root logger in list
        hs.putIfAbsent("", new LoggerInfo("" , config.getRootLogger().getLevel()));
        hs.get("").setInConfig();

        List<LoggerInfo> valuesList = new ArrayList<>(hs.values());
        Collections.sort(valuesList); // sorts by real name alphabetically
        return valuesList;
    }

    private void populateTextArea(List<LoggerInfo> loggers){
        StringBuilder result = new StringBuilder();
        for (LoggerInfo s : loggers) {
            result.append("  ").append(s.toString()).append(System.lineSeparator());
        }

        JScrollBar b = scroll.getVerticalScrollBar();
        int beforeScroll = b.getValue();
        int caret = text.getCaretPosition();
        text.setText(result.toString());
        text.setCaretPosition(caret);
        b.setValue(beforeScroll);
    }

    private void updateCategoryComboBox(List<LoggerInfo> loggers) {
        String f = (String)categoryComboBox.getSelectedItem();
        categoryComboBox.removeAllItems();
        for ( LoggerInfo l : loggers ) {
            categoryComboBox.addItem(l.getLoggerName());
        }
        categoryComboBox.setSelectedItem(f == null ? ROOT_LEVEL_STRING : f);
    }

    private void editButtonPressed(ActionEvent e) {
        log.debug("{} pressed", e.getActionCommand());
        String f = (String)categoryComboBox.getSelectedItem();
        Level l = (Level)levelSelectionComboBox.getSelectedItem();
        log.info("changing Logging for {} to {}",f,l);
        if ( ROOT_LEVEL_STRING.equals(f) ){
            f=""; // empty String is actual name for root logger.
        }
        Configurator.setLevel(LogManager.getLogger(f), l);
        updateTextAreaAndCategorySelect();
    }

    private void refreshButtonPressed(ActionEvent e){
        log.debug("{} pressed", e.getActionCommand());
        updateTextAreaAndCategorySelect();
    }

    /**
     * 3rd stage of initialization, invoked after Swing components exist.
     */
    @Override
    public void initContext(Object context) {
    }

    private static class LoggerInfo implements Comparable<LoggerInfo> {

        private final String loggerName;
        private final Level level;
        private boolean inConfig = false;

        private LoggerInfo(String loggerName, Level level) {
            this.loggerName = loggerName;
            this.level = level;
        }

        String getLoggerName() {
            return (loggerName.isBlank() ? ROOT_LEVEL_STRING : loggerName);
        }

        private void setInConfig(){
            inConfig = true;
        }

        @Override
        public int compareTo(LoggerInfo other) {
            return this.loggerName.compareTo(other.loggerName);
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(getLoggerName()).append("  ");
            if ( !inConfig ){
                s.append("{ ").append(level.name()).append(" }");
            } else {
                s.append("[ ").append(level.name()).append(" ]");
            }
            return  s.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LoggerInfo)) {
                return false;
            }
            LoggerInfo other = (LoggerInfo) obj;
            return Objects.equals(loggerName, other.loggerName) && Objects.equals(level, other.level);
        }

        @Override
        public int hashCode() {
            return 13 * Objects.hashCode(loggerName) * Objects.hashCode(level);
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Log4JTreePane.class);

}
