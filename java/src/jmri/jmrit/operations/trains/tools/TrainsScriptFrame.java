package jmri.jmrit.operations.trains.tools;

import java.awt.GridBagLayout;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.script.JmriScriptEngineManager;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of startup and shutdown operation scripts.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dan Boudreau Copyright (C) 2011
 */
public class TrainsScriptFrame extends OperationsFrame {

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);

    // script panels
    JPanel pStartUpScript = new JPanel();
    JPanel pShutDownScript = new JPanel();
    JScrollPane startUpScriptPane;
    JScrollPane shutDownScriptPane;

    // labels
    // major buttons
    JButton addStartUpScriptButton = new JButton(Bundle.getMessage("AddScript"));
    JButton addShutDownScriptButton = new JButton(Bundle.getMessage("AddScript"));
    JButton runStartUpScriptButton = new JButton(Bundle.getMessage("RunScripts"));
    JButton runShutDownScriptButton = new JButton(Bundle.getMessage("RunScripts"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    public TrainsScriptFrame() {
        super();
    }

    @Override
    public void initComponents() {
        // Set up script options in a Scroll Pane..
        startUpScriptPane = new JScrollPane(pStartUpScript);
        startUpScriptPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        startUpScriptPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScriptsStartUp")));

        shutDownScriptPane = new JScrollPane(pShutDownScript);
        shutDownScriptPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        shutDownScriptPane
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScriptsShutDown")));

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        // row 1
        updateStartUpScriptPanel();

        // row 3
        updateShutDownScriptPanel();

        // row 4 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, saveButton, 3, 0);

        getContentPane().add(startUpScriptPane);
        getContentPane().add(shutDownScriptPane);
        getContentPane().add(pB);

        // setup buttons
        addButtonAction(addStartUpScriptButton);
        addButtonAction(addShutDownScriptButton);
        addButtonAction(runStartUpScriptButton);
        addButtonAction(runShutDownScriptButton);
        addButtonAction(saveButton);

        enableButtons(true);

        addHelpMenu("package.jmri.jmrit.operations.Operations_TrainScripts", true); // NOI18N
        packFrame();
        initMinimumSize();
    }

    private void updateStartUpScriptPanel() {
        pStartUpScript.removeAll();
        pStartUpScript.setLayout(new GridBagLayout());
        addItem(pStartUpScript, addStartUpScriptButton, 0, 0);

        // load any existing startup scripts
        List<String> scripts = trainManager.getStartUpScripts();
        if (scripts.size() > 0) {
            addItem(pStartUpScript, runStartUpScriptButton, 1, 0);
        }
        for (int i = 0; i < scripts.size(); i++) {
            JButton removeStartUpScripts = new JButton(Bundle.getMessage("RemoveScript"));
            removeStartUpScripts.setName(scripts.get(i));
            removeStartUpScripts.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    buttonActionRemoveStartUpScript(e);
                }
            });
            addButtonAction(removeStartUpScripts);
            JLabel pathname = new JLabel(scripts.get(i));
            addItem(pStartUpScript, removeStartUpScripts, 0, i + 1);
            addItem(pStartUpScript, pathname, 1, i + 1);
        }
    }

    private void updateShutDownScriptPanel() {
        pShutDownScript.removeAll();
        pShutDownScript.setLayout(new GridBagLayout());
        addItem(pShutDownScript, addShutDownScriptButton, 0, 0);

        // load any existing shutdown scripts
        List<String> scripts = trainManager.getShutDownScripts();
        if (scripts.size() > 0) {
            addItem(pShutDownScript, runShutDownScriptButton, 1, 0);
        }
        for (int i = 0; i < scripts.size(); i++) {
            JButton removeShutDownScripts = new JButton(Bundle.getMessage("RemoveScript"));
            removeShutDownScripts.setName(scripts.get(i));
            removeShutDownScripts.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    buttonActionRemoveShutDownScript(e);
                }
            });
            JLabel pathname = new JLabel(scripts.get(i));
            addItem(pShutDownScript, removeShutDownScripts, 0, i + 1);
            addItem(pShutDownScript, pathname, 1, i + 1);
        }
    }

    // Save train, add scripts buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addStartUpScriptButton) {
            log.debug("train add move script button activated");
            File f = selectFile();
            if (f != null) {
                trainManager.addStartUpScript(FileUtil.getPortableFilename(f));
                updateStartUpScriptPanel();
                packFrame();
            }
        }
        if (ae.getSource() == addShutDownScriptButton) {
            log.debug("train add termination script button activated");
            File f = selectFile();
            if (f != null) {
                trainManager.addShutDownScript(FileUtil.getPortableFilename(f));
                updateShutDownScriptPanel();
                packFrame();
            }
        }
        if (ae.getSource() == runStartUpScriptButton) {
            runScripts(trainManager.getStartUpScripts());
        }
        if (ae.getSource() == runShutDownScriptButton) {
            runScripts(trainManager.getShutDownScripts());
        }
        if (ae.getSource() == saveButton) {
            log.debug("Save button activated");
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    public void buttonActionRemoveStartUpScript(java.awt.event.ActionEvent ae) {
        JButton rbutton = (JButton) ae.getSource();
        log.debug("remove move script button activated " + rbutton.getName());
        trainManager.deleteStartUpScript(rbutton.getName());
        updateStartUpScriptPanel();
        packFrame();
    }

    public void buttonActionRemoveShutDownScript(java.awt.event.ActionEvent ae) {
        JButton rbutton = (JButton) ae.getSource();
        log.debug("remove termination script button activated " + rbutton.getName());
        trainManager.deleteShutDownScript(rbutton.getName());
        updateShutDownScriptPanel();
        packFrame();
    }

    /**
     * We always use the same file chooser in this class, so that the user's
     * last-accessed directory remains available.
     */
    JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PythonScriptFiles"), "py"); // NOI18N

    private File selectFile() {
        if (fc == null) {
            log.error("Could not find user directory");
        } else {
            fc.setDialogTitle(Bundle.getMessage("FindDesiredScriptFile"));
            // when reusing the chooser, make sure new files are included
            fc.rescanCurrentDirectory();
            int retVal = fc.showOpenDialog(null);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                // Run the script from it's filename
                return file;
            }
        }
        return null;
    }

    private void enableButtons(boolean enabled) {
        addStartUpScriptButton.setEnabled(enabled);
        addShutDownScriptButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    private void runScripts(List<String> scripts) {
        for (String script : scripts) {
            String scriptPathname = jmri.util.FileUtil.getExternalFilename(script);
            File file = new File(scriptPathname);
            if (file.exists()) {
                JmriScriptEngineManager.getDefault().runScript(file);
            } else {
                JOptionPane.showMessageDialog(this, script, Bundle.getMessage("ScriptFileNotFound"),
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    private void packFrame() {
        setPreferredSize(null);
        pack();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(TrainsScriptFrame.class
            .getName());
}
