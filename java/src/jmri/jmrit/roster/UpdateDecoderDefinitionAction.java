package jmri.jmrit.roster;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeNode;
import jmri.InstanceManager;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.symbolicprog.CombinedLocoSelTreePane;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the decoder definitions in the roster.
 * <br><br>
 * When required, provides a user GUI to assist with replacing multiple-match
 * definitions.
 *
 * @author Bob Jacobsen Copyright (C) 2013
 * @see jmri.jmrit.XmlFile
 * @author Dave Heap 2017 - Provide user GUI
 */
public class UpdateDecoderDefinitionAction extends JmriAbstractAction {

    /**
     * The prefix string used to specify a query in decoder definition file
     * replacementFamily and replacementModel elements.
     */
    public static final String QRY_PREFIX = "query:";

    /**
     * The {@link java.util.regex regex} separator to
     * {@link java.lang.String#split(java.lang.String) split} items in
     * replacementFamily and replacementModel elements.
     */
    public static final String QRY_SEPARATOR = "\\|";

    /**
     * The {@code replacementFamily} attribute from the decoder definition file.
     */
    String replacementFamily;
    String replacementFamilyString; // replacementFamily with any QRY_PREFIX stripped
    boolean hasReplacementFamilyQuery; // whether replacementFamily has a QRY_PREFIX

    /**
     * The {@code replacementModel} attribute from the decoder definition file.
     */
    String replacementModel;
    String replacementModelString; // replacementModel with any QRY_PREFIX stripped
    boolean hasReplacementModelQuery; // whether replacementModel has a QRY_PREFIX
    int cV7Value; // the CV7 (versionID) value stored in the roster entry
    int cV8Value; // the CV8 (mfgID) value stored in the roster entry

    /**
     * Displays the last-selected filter action.
     */
    JLabel lastActionDisplay;

    /**
     * A temporary roster entry used in matching and replacement.
     */
    transient volatile RosterEntry tempRe;

    /**
     * A {@link List} based on the combination of any
     * replacementFamily and
     * replacementModel suggestions.
     */
    transient volatile List<DecoderFile> replacementList;

    /**
     * The subset of the <code>replacementList</code> that also matches
     * both the
     * {@link jmri.jmrit.decoderdefn.IdentifyDecoder} manufacturerID
     * stored in CV8 and the
     * {@link jmri.jmrit.decoderdefn.IdentifyDecoder} versionID stored
     * in CV7.
     */
    transient volatile List<DecoderFile> versionMatchList;

    transient volatile DecoderIndexFile di; // the default instance of the DecoderIndexFile
    transient volatile FocusListener fListener;
    transient volatile JLabel statusLabel;
    transient volatile JDialog f;

    final jmri.jmrit.progsupport.ProgModeSelector modePane = new jmri.jmrit.progsupport.ProgServiceModeComboBox();
    JButton cancelButton;
    JToggleButton versionButton;
    JToggleButton replacementButton;
    CombinedLocoSelTreePane combinedLocoSelTree;

    /**
     * Update the decoder definitions in the roster.
     *
     * @param name the name ({@link javax.swing.Action#NAME}) for the action; a
     *             value of {@code null} is ignored
     */
    public UpdateDecoderDefinitionAction(String name) {
        super(name);
    }

    /**
     * Update the decoder definitions in the roster.
     *
     * @param name the name ({@link javax.swing.Action#NAME}) for the action; a
     *             value of {@code null} is ignored
     * @param wi   the window interface controlling how this action is displayed
     */
    public UpdateDecoderDefinitionAction(String name, WindowInterface wi) {
        super(name, wi);
    }

    /**
     * Update the decoder definitions in the roster.
     *
     * @param name the name ({@link javax.swing.Action#NAME}) for the action; a
     *             value of {@code null} is ignored
     * @param i    the small icon ({@link javax.swing.Action#SMALL_ICON}) for
     *             the action; a value of {@code null} is ignored
     * @param wi   the window interface controlling how this action is displayed
     */
    public UpdateDecoderDefinitionAction(String name, Icon i, WindowInterface wi) {
        super(name, i, wi);
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        List<RosterEntry> list = Roster.getDefault().matchingList(null, null, null, null, null, null, null);

        boolean skipQueries = false;

        di = InstanceManager.getDefault(DecoderIndexFile.class);

        for (RosterEntry entry : list) {
            String family = entry.getDecoderFamily();
            String model = entry.getDecoderModel();

            // check if replaced or missing
            List<DecoderFile> decoders = di.matchingDecoderList(null, family, null, null, null, model);
            boolean missing = decoders.size() < 1;
            if (decoders.size() != 1 && !model.equals(family)) {
                log.error("Found {} decoders matching family \"{}\" model \"{}\" from roster entry \"{}\"",
                        decoders.size(), family, model, entry.getId());
                if (missing) {
                    replacementModel = model;  // fall back to try just the decoder name, not family
                    replacementModelString = replacementModel;
                    replacementFamily = null;
                    replacementFamilyString = "";
                } else {
                    continue; // cannot process this one; there are multiple definitions for the same family/model combination
                }
            }

            for (DecoderFile decoder : decoders) {
                if (decoder.getReplacementFamily() != null || decoder.getReplacementModel() != null) {
                    log.debug("Indicated replacements are family \"{}\" model \"{}\"",
                            decoder.getReplacementFamily(), decoder.getReplacementModel());
                }
                replacementFamily = decoder.getReplacementFamily();
                replacementModel = decoder.getReplacementModel();
                hasReplacementFamilyQuery = false;
                hasReplacementModelQuery = false;
                replacementFamilyString = replacementFamily;
                replacementModelString = replacementModel;
                if (replacementFamily != null && replacementFamily.startsWith(QRY_PREFIX)) {
                    hasReplacementFamilyQuery = true;
                    replacementFamilyString = replacementFamily.substring(QRY_PREFIX.length());
                } else if (replacementFamily == null) {
                    replacementFamilyString = family;
                }
                if (replacementModel != null && replacementModel.startsWith(QRY_PREFIX)) {
                    hasReplacementModelQuery = true;
                    replacementModelString = replacementModel.substring(QRY_PREFIX.length());
                } else if (replacementModel == null) {
                    replacementModelString = model;
                }
                log.trace("String replacements are family \"{}\", query={} and model \"{}\", query={}",
                        replacementFamilyString, hasReplacementFamilyQuery, replacementModelString, hasReplacementModelQuery);
            }

            if (replacementModel != null || replacementFamily != null) {

                boolean isToUpdate = true;
                if ((replacementModel != null && replacementModel.startsWith(QRY_PREFIX))
                        || (replacementFamily != null && replacementFamily.startsWith(QRY_PREFIX))
                        || missing) {
                    int retVal = 2;
                    if (!skipQueries) {
                        // build explanatory text
                        StringBuilder sb = new StringBuilder();
                        sb.append(Bundle.getMessage("TextMultRepl1", entry.getId(), family, model)).append("\n\n")
                                .append(Bundle.getMessage(missing ? "TextNoDefn1a" : "TextMultRepl1a")).append("\n");

                        if (replacementFamily != null && !replacementFamily.equals(family) && !replacementFamily.equals(QRY_PREFIX)) {
                            if (replacementFamily.startsWith(QRY_PREFIX)) {
                                sb.append(Bundle.getMessage("TextMultReplFamilyOneOf")).append(": \"");
                                sb.append(replacementFamily.substring(QRY_PREFIX.length()).replaceAll(QRY_SEPARATOR, "\",\""));
                                sb.append("\"\n");
                            } else {
                                sb.append(Bundle.getMessage("TextMultReplFamily"));
                                sb.append(": \"").append(replacementFamily).append("\"\n");
                            }
                        }
                        if (replacementModel != null && !replacementModel.equals(model) && !replacementModel.equals(QRY_PREFIX)) {
                            if (replacementModel.startsWith(QRY_PREFIX)) {
                                sb.append(Bundle.getMessage("TextMultReplModelOneOf")).append(": \"");
                                sb.append(replacementModel.substring(QRY_PREFIX.length()).replaceAll(QRY_SEPARATOR, "\",\""));
                                sb.append("\"\n");
                            } else {
                                sb.append(Bundle.getMessage("TextMultReplModel"));
                                sb.append(": \"").append(replacementModel).append("\"\n");
                            }
                        }

                        sb.append("\n").append(Bundle.getMessage("TextMultRepl2", Bundle.getMessage("ButtonMultReplSelectNew")));
                        sb.append("\n");

                        retVal = multiReplacementDialog(sb.toString(), missing);
                    }
                    log.trace("return value = {}", retVal);
                    if (retVal == 2) {
                        skipQueries = true;
                        log.trace("Skip All");
                    }
                    if (retVal != 0) {
                        log.trace("Skip This");
                        isToUpdate = false;
                    }
                    log.trace("Is to Update = {}", isToUpdate);
                    if (isToUpdate) {
                        decoderSelectionPane(entry);
                        if (tempRe == null) {
                            log.trace("dummy Roster Entry is null");
                            isToUpdate = false;
                        } else {
                            log.trace("dummy Roster Entry returned Family '{}', model '{}'", tempRe.getDecoderFamily(), tempRe.getDecoderModel());
                            if (!tempRe.getDecoderFamily().equals(family)) {
                                replacementFamily = tempRe.getDecoderFamily();
                            } else {
                                replacementFamily = null;
                            }
                            if (!tempRe.getDecoderModel().equals(model)) {
                                replacementModel = tempRe.getDecoderModel();
                            } else {
                                replacementModel = null;
                            }
                        }
                    }
                }

                // change the roster entry
                if (isToUpdate) {
                    if (replacementFamily != null) {
                        log.info("   *** Will update \"{}'\". replacementFamily='{}'", entry.getId(), replacementFamily);
                        entry.setDecoderFamily(replacementFamily);
                    }
                    if (replacementModel != null) {
                        log.info("   *** Will update \"{}'\". replacementModel='{}'", entry.getId(), replacementModel);
                        entry.setDecoderModel(replacementModel);
                    }

                    // write it out (not bothering to do backup?)
                    entry.updateFile();
                }
            }
        }

        // write updated roster
        Roster.getDefault()
                .makeBackupFile(Roster.getDefault().getRosterIndexPath());
        try {
            Roster.getDefault().writeFile(Roster.getDefault().getRosterIndexPath());
        } catch (IOException ex) {
            log.error("Exception while writing the new roster file, may not be complete: " + ex);
        }
        // use the new one

        Roster.getDefault()
                .reloadRosterFile();
    }

    /**
     * Fetch the {@link JOptionPane} associated with this {@link JComponent}.
     * <br><br>
     * Note that:
     * <ul>
     * <li>The {@code source} must be within (or itself be) a
     * {@link JOptionPane}.</li>
     * <li>If {@code source} is a {@link JOptionPane}, the returned element will
     * be {@code source}</li>
     * </ul>
     *
     * @param source the {@link JComponent}
     * @return the {@link JOptionPane} associated with {@code source}
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
            justification = "Code calls method in such a way that the cast is guaranteed to be safe")  // NOI18N
    synchronized JOptionPane getOptionPane(JComponent source) {
        JOptionPane pane;
        if (!(source instanceof JOptionPane)) {
            pane = getOptionPane((JComponent) source.getParent());
        } else {
            pane = (JOptionPane) source;
        }
        return pane;
    }

    /**
     * Creates the "Multiple Replacements Found" dialog box with custom buttons
     * and tooltips.
     *
     * @param text    the explanatory text to display
     * @param missing if true, displays a "missing definition" title rather than
     *                a "multiple replacements" title
     * @return sequence number of the button selected
     */
    synchronized int multiReplacementDialog(String text, boolean missing) {
        // Create custom buttons so we can add tooltips
        final JButton select = new JButton(Bundle.getMessage("ButtonMultReplSelectNew"));
        select.setToolTipText(Bundle.getMessage("ToolTipMultReplSelectNew"));
        select.addActionListener((ActionEvent e) -> {
            JOptionPane pane = getOptionPane((JComponent) e.getSource());
            pane.setValue(select);
        });
        final JButton skipThis = new JButton(Bundle.getMessage("ButtonMultReplSkipThis"));
        skipThis.setToolTipText(Bundle.getMessage("ToolTipMultReplSkipThis"));
        skipThis.addActionListener((ActionEvent e) -> {
            JOptionPane pane = getOptionPane((JComponent) e.getSource());
            pane.setValue(skipThis);
        });
        final JButton skipAll = new JButton(Bundle.getMessage("ButtonMultReplSkipAll"));
        skipAll.setToolTipText(Bundle.getMessage("ToolTipMultReplSkipAll"));
        skipAll.addActionListener((ActionEvent e) -> {
            JOptionPane pane = getOptionPane((JComponent) e.getSource());
            pane.setValue(skipAll);
        });
        int retVal = JOptionPane.CLOSED_OPTION;

        while (retVal == JOptionPane.CLOSED_OPTION) {
            retVal = JOptionPane.showOptionDialog(new JFrame(),
                    text,
                    Bundle.getMessage(missing ? "TitleNoDefn" : "TitleMultRepl"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new JButton[]{select, skipThis, skipAll},
                    select);
            log.trace("retVal={}", retVal);
        }
        return retVal;
    }

    /**
     * Creates the "Replacement Definition" pane, which is similar in appearance
     * to {@link apps.gui3.dp3.PaneProgDp3Action Create New Loco} pane, likewise
     * utilizing a customized instance of {@link CombinedLocoSelTreePane}.
     *
     * @param theEntry an existing roster entry that needs replacement
     */
    synchronized void decoderSelectionPane(RosterEntry theEntry) {

        log.debug("Decoder Selection Pane requested"); // NOI18N

        tempRe = null;
        statusLabel = new JLabel(SymbolicProgBundle.getMessage("StateIdle")); // NOI18N
        log.debug("New decoder requested"); // NOI18N
        makeMatchLists(theEntry);
        log.trace("Version matchlist size={}", versionMatchList.size());
        log.trace("Replacement matchlist size={}", replacementList.size());

        // based on code borrowed from apps.gui3.dp3.PaneProgDp3Action#actionPerformed
        // create the initial frame that steers
        f = new JDialog((Frame) null, Bundle.getMessage("TitleReplDefn", theEntry.getId()), true); // NOI18N
        Container dialogPane = f.getContentPane();
        dialogPane.setLayout(new BoxLayout(dialogPane, BoxLayout.Y_AXIS));
        // ensure status line is cleared on close so it is normal if tempRe-opened
        f.addWindowListener(new WindowAdapter() {
            @Override
            public synchronized void windowClosing(WindowEvent we) {
                statusLabel.setText(SymbolicProgBundle.getMessage("StateIdle")); // NOI18N
                log.debug("window closing");
                f.dispose();
            }
        });
        f.getRootPane().registerKeyboardAction(e -> {
            statusLabel.setText(SymbolicProgBundle.getMessage("StateIdle")); // NOI18N
            log.debug("escape pressed");
            f.dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        final JPanel bottomPanel = new JPanel(new BorderLayout());
        // new Loco on programming track
        combinedLocoSelTree = new CombinedLocoSelTreePane(statusLabel, modePane) {

            @Override
            protected synchronized void openNewLoco() {
                // find the decoderFile object
                go2.setToolTipText(Bundle.getMessage("ToolTipUseSelectedDecoder"));
                DecoderFile decoderFile = di.fileFromTitle(selectedDecoderType());
                log.debug("decoder file: {}", decoderFile.getFileName()); // NOI18N
                // create a dummy RosterEntry with the decoder info
                tempRe = new RosterEntry();
                tempRe.setDecoderFamily(decoderFile.getFamily());
                tempRe.setDecoderModel(decoderFile.getModel());
                tempRe.setId(SymbolicProgBundle.getMessage("LabelNewDecoder")); // NOI18N
                // That's all, folks. The family and model will be picked up from tempRe in the main code.
            }

            @Override
            protected synchronized JPanel layoutRosterSelection() {
                log.debug("layoutRosterSelection");
                return null;
            }

            @Override
            protected synchronized JPanel layoutDecoderSelection() {
                log.debug("layoutDecoderSelection");
                JPanel pan = super.layoutDecoderSelection();
                versionButton = versionMatchButton();
                viewButtons.add(versionButton);
                replacementButton = replacementMatchButton();
                viewButtons.add(replacementButton);
                updateMatchButtons(theEntry);
                dTree.removeTreeSelectionListener(dListener);
                dListener = (TreeSelectionEvent e) -> {
                    log.debug("selection changed, {}empty, {}",
                            (dTree.isSelectionEmpty() ? "" : "not "), Arrays.toString(dTree.getSelectionPaths()));
                    if (dTree.hasFocus()) {
                        setLastActionDisplay("TextManualSelection");
                    }
                    if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null
                            && // check that this isn't just a model
                            ((TreeNode) dTree.getSelectionPath().getLastPathComponent()).isLeaf()
                            && // can't be just a mfg, has to be at least a family
                            dTree.getSelectionPath().getPathCount() > 2
                            && // can't be a multiple decoder selection
                            dTree.getSelectionCount() < 2) {
                        log.debug("Selection event with {}", dTree.getSelectionPath());
                        go2.setEnabled(true);
                        go2.setRequestFocusEnabled(true);
                        go2.requestFocus();
                        go2.setToolTipText(Bundle.getMessage("ToolTipUseSelectedDecoder")); // NOI18N
                    } else {
                        // decoder not selected - require one
                        go2.setEnabled(false);
                        go2.setToolTipText(Bundle.getMessage("ToolTipNoSelectedDecoder")); // NOI18N
                    }
                };
                dTree.addTreeSelectionListener(dListener);
                dTree.removeFocusListener(fListener);
                fListener = new FocusListener() {

                    /**
                     * Invoked when a component gains the keyboard focus.
                     */
                    @Override
                    public synchronized void focusGained(FocusEvent e) {
                        log.debug("Focus Gained, {}empty, {}",
                                (dTree.isSelectionEmpty() ? "" : "not "), Arrays.toString(dTree.getSelectionPaths()));
                        setLastActionDisplay("TextManualSelection");
                    }

                    /**
                     * Invoked when a component loses the keyboard focus.
                     */
                    @Override
                    public void focusLost(FocusEvent e) {
                        log.debug("Focus Lost, {}empty, {}",
                                (dTree.isSelectionEmpty() ? "" : "not "), Arrays.toString(dTree.getSelectionPaths()));
                        setLastActionDisplay("TextManualSelection");
                    }
                };
                dTree.addFocusListener(fListener);
                return pan;
            }

            /**
             * Identify loco button pressed, start the identify operation. This
             * defines what happens when the identify is done.
             * <br><br>
             * This {@code @Override} method invokes
             * {@link #setLastActionDisplay setLastActionDisplay} before
             * starting.
             */
            @Override
            protected synchronized void startIdentifyDecoder() {
                // start identifying a decoder
                setLastActionDisplay("ButtonReadType");

                super.startIdentifyDecoder();
            }

            JToggleButton versionMatchButton() {
                JToggleButton button = new JToggleButton(Bundle.getMessage("ButtonShowVersionMatch"));
                button.setToolTipText(Bundle.getMessage("ToolTipVersionMatch", cV7Value, theEntry.getId()));
                button.addActionListener((java.awt.event.ActionEvent e) -> {
                    resetSelections();
                    updateForDecoderTypeID(versionMatchList);
                    button.setSelected(false);
                    setLastActionDisplay("ButtonShowVersionMatch");
                    setShowMatchedOnly(true);
                });
                return button;
            }

            JToggleButton replacementMatchButton() {
                JToggleButton button = new JToggleButton(Bundle.getMessage("ButtonShowSuggested"));
                button.setToolTipText(Bundle.getMessage("ToolTipShowSuggested"));
                button.addActionListener((java.awt.event.ActionEvent e) -> {
                    resetSelections();
                    updateForDecoderTypeID(replacementList);
                    button.setSelected(false);
                    setLastActionDisplay("ButtonShowSuggested");
                    setShowMatchedOnly(true);
                });
                return button;
            }

            @Override
            protected synchronized JPanel createProgrammerSelection() {
                log.debug("createProgrammerSelection");

                JPanel pane3a = new JPanel();
                pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.Y_AXIS));

                cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
                cancelButton.addActionListener((java.awt.event.ActionEvent e) -> {
                    log.debug("Cancel"); // NOI18N
                    log.debug("Closing f {}", f);
                    WindowEvent wev = new WindowEvent(f, WindowEvent.WINDOW_CLOSING);
                    java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
                    f.dispose();
                });
                cancelButton.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
                cancelButton.setToolTipText(Bundle.getMessage("ToolTipButtonCancel"));
                bottomPanel.add(cancelButton, BorderLayout.WEST);

                lastActionDisplay = new JLabel("", SwingConstants.CENTER);
                bottomPanel.add(lastActionDisplay, BorderLayout.CENTER);

                go2 = new JButton(Bundle.getMessage("ButtonUseSelected"));
                go2.addActionListener((java.awt.event.ActionEvent e) -> {
                    log.debug("Use Selected pressed"); // NOI18N
                    openButton();
                    log.debug("Closing f {}", f);
                    WindowEvent wev = new WindowEvent(f, WindowEvent.WINDOW_CLOSING);
                    java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
                    f.dispose();
                });
                go2.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
                go2.setEnabled(false);
                go2.setToolTipText(Bundle.getMessage("ToolTipNoSelectedDecoder"));
                bottomPanel.add(go2, BorderLayout.EAST);

                return pane3a; //empty pane in this case
            }

        };

        // load primary frame
        // Help panel
        JPanel helpPane = new JPanel();
        JScrollPane helpScroll = new JScrollPane(helpPane);
        helpPane.setLayout(new BoxLayout(helpPane, BoxLayout.Y_AXIS));
        String[] buttons
                = {"ButtonReadType", "ButtonShowVersionMatch", "ButtonShowSuggested", "ButtonAllMatched", "ButtonUseSelected", "ButtonCancel"};
        for (String button : buttons) {
            JLabel l;
            l = new JLabel("<html><strong>&quot;" + Bundle.getMessage(button) + "&quot;</strong></html>");
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            helpPane.add(l);
            int line = 1;
            while (line >= 0) {
                try {
                    String msg = Bundle.getMessage(button + "Help" + line, theEntry.getId());
                    if (msg.isEmpty()) {
                        msg = " ";
                    }
                    l = new JLabel(msg);
                    l.setAlignmentX(Component.LEFT_ALIGNMENT);
                    helpPane.add(l);
                    line++;
                } catch (java.util.MissingResourceException e) {  // deliberately runs until exception
                    line = -1;
                }
            }
            l = new JLabel(" ");
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            helpPane.add(l);
        }

        dialogPane.add(helpScroll);

        JPanel infoPane = new JPanel();
        JLabel l;
        l = new JLabel(Bundle.getMessage("TextReplDefn", theEntry.getDecoderFamily(), theEntry.getDecoderModel(), theEntry.getId()));
        infoPane.add(l);
        dialogPane.add(infoPane);

        JPanel selectorPane = new JPanel();
        selectorPane.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.add(modePane);
        topPanel.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
        selectorPane.add(topPanel, BorderLayout.NORTH);
        combinedLocoSelTree.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        selectorPane.add(combinedLocoSelTree, BorderLayout.CENTER);

        statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        selectorPane.add(bottomPanel, BorderLayout.SOUTH);
        dialogPane.add(selectorPane, BorderLayout.CENTER);

        f.pack();
        log.debug("Tab-Programmer setup created"); // NOI18N

        if (!versionMatchList.isEmpty()) {
            combinedLocoSelTree.updateForDecoderTypeID(versionMatchList);
            setLastActionDisplay("ButtonShowVersionMatch");
            combinedLocoSelTree.setShowMatchedOnly(true);
        } else if (!replacementList.isEmpty()) {
            combinedLocoSelTree.updateForDecoderTypeID(replacementList);
            setLastActionDisplay("ButtonShowSuggested");
            combinedLocoSelTree.setShowMatchedOnly(true);
        }
        f.setVisible(true);
        log.trace("Test done");
    }

    /**
     * Updates the {@link #lastActionDisplay lastActionDisplay} {@link JLabel}
     * to be the text fetched by the key named "{@code TextLastAction}", after
     * inclusion of the text fetched by the key named "{@code propertyName}".
     *
     * @param propertyName the name of a {@link java.util.ResourceBundle} key
     */
    synchronized void setLastActionDisplay(String propertyName) {
        this.lastActionDisplay.setText(Bundle.getMessage("TextLastAction", Bundle.getMessage(propertyName)));
        log.debug("Last Action display changed to {}", this.lastActionDisplay.getText());
    }

    /**
     * Creates two {@link ArrayList ArrayLists} for decoder matching.
     * <br><br>
     * They are:
     * <ul>
     * <li>
     * A {@link #replacementList replacementList} based on the combination of
     * any {@link #replacementFamily replacementFamily} and
     * {@link #replacementModel replacementModel} suggestions.
     * </li>
     * <li>
     * A {@link #versionMatchList versionMatchList} that is the subset of
     * {@link #replacementList replacementList} that also matches both a
     * manufacturerID (from 
     * {@link jmri.jmrit.decoderdefn.IdentifyDecoder} mfgID) 
     * stored in CV8 and a versionID (from
     * {@link jmri.jmrit.decoderdefn.IdentifyDecoder} modelID)  stored
     * in CV7.
     * </li>
     * </ul>
     *
     * @param theEntry an existing roster entry that needs replacement
     */
    synchronized void makeMatchLists(RosterEntry theEntry) {
        versionMatchList = new ArrayList<>();
        replacementList = new ArrayList<>();

        // Get CV values from file.
        theEntry.readFile();
        CvTableModel cvModel = new CvTableModel(null, null);
        theEntry.loadCvModel(null, cvModel);
        CvValue cvObject;
        cV7Value = 0;
        cvObject = cvModel.allCvMap().get("7");
        if (cvObject != null) {
            cV7Value = cvObject.getValue();
        }
        cV8Value = 0;
        cvObject = cvModel.allCvMap().get("8");
        if (cvObject != null) {
            cV8Value = cvObject.getValue();
        }
        log.trace("cV7Value = {}, cV8Value = {}", cV7Value, cV8Value);
        for (String theFamily : replacementFamilyString.split(QRY_SEPARATOR)) {
            if (theFamily != null && theFamily.equals("")) {
                theFamily = null;
            }
            for (String theModel : replacementModelString.split(QRY_SEPARATOR)) {
                if (theModel != null && theModel.equals("")) {
                    theModel = null;
                }
                log.trace("theFamily = {}, theModel = {}", theFamily, theModel);
                List<DecoderFile> decoders = di.matchingDecoderList(null, theFamily, null, null, null, theModel);
                log.trace("Found {} replacement decoders matching family \"{}\" model \"{}\"",
                        decoders.size(), theFamily, theModel);

                for (DecoderFile decoder : decoders) {
                    if ((decoder.getShowable() != DecoderFile.Showable.NO)
                            && !(decoder.getFamily().equals(theEntry.getDecoderFamily()) && decoder.getModel().equals(theEntry.getDecoderModel()))) {
                        if ((cV7Value > 0) && (cV8Value > 0) && decoder.isVersion(cV7Value)) {
                            log.trace("Adding to versionMatchList mfg='{}', family='{}', model='{}'", decoder.getMfg(), decoder.getFamily(), decoder.getModel());
                            versionMatchList.add(new DecoderFile(decoder.getMfg(), null, decoder.getModel(),
                                    null, null, decoder.getFamily(), null, 0, 0, null));
                        }
                        log.trace("Adding to replacementList mfg='{}', family='{}', model='{}'", decoder.getMfg(), decoder.getFamily(), decoder.getModel());
                        replacementList.add(new DecoderFile(decoder.getMfg(), null, decoder.getModel(),
                                null, null, decoder.getFamily(), null, 0, 0, null));
                    }
                }
            }
        }

        updateMatchButtons(theEntry);
    }

    /**
     * Updates the {@link #versionButton versionButton} and
     * {@link #replacementButton replacementButton} availability and tooltips,
     * depending on whether {@link #versionMatchList versionMatchList} and
     * {@link #replacementList replacementList} are empty or not.
     *
     * @param theEntry an existing roster entry that needs replacement
     */
    synchronized void updateMatchButtons(RosterEntry theEntry) {
        if (versionButton != null) {
            if ((versionMatchList == null) || versionMatchList.isEmpty()) {
                versionButton.setEnabled(false);
                versionButton.setToolTipText(Bundle.getMessage("ToolTipNoVersionMatch", theEntry.getId()));
            } else {
                log.trace("versionMatchList size = {}", versionMatchList.size());
                versionButton.setEnabled(true);
                versionButton.setToolTipText(Bundle.getMessage("ToolTipVersionMatch", cV7Value, theEntry.getId()));
            }
        }
        if (replacementButton != null) {
            if ((replacementList == null) || replacementList.isEmpty()) {
                replacementButton.setEnabled(false);
                replacementButton.setToolTipText(Bundle.getMessage("ToolTipNoShowSuggested"));
            } else {
                log.trace("replacementList size = {}", replacementList.size());
                replacementButton.setEnabled(true);
                replacementButton.setToolTipText(Bundle.getMessage("ToolTipShowSuggested"));
            }
        }
    }

    /**
     * Never invoked, because we overrode actionPerformed above.
     *
     * @return never because it deliberately throws an
     *         {@link IllegalArgumentException}
     */
    @Override
    public synchronized jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(UpdateDecoderDefinitionAction.class);
}
