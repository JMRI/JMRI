package jmri.jmrit.logixng.tools.swing;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;

/**
 * Swing action to edit the error handling module.
 *
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class EditErrorHandlingModuleAction extends JmriAbstractAction {

    private ErrorModuleEditor _errorModuleEditor;

    public EditErrorHandlingModuleAction() {
        super(getTitle());
        InstanceManager.getDefault(LogixNG_Manager.class)
                .addPropertyChangeListener(LogixNG_Manager.PROPERTY_SETUP, (evt) -> {
                    setName(getTitle());
                });
    }

    public static String getTitle() {
        if (InstanceManager.getDefault(LogixNG_Manager.class).isErrorHandlingModuleEnabled()) {
            return Bundle.getMessage("TitleErrorHandlingModuleEditor_Enabled");
        } else {
            return Bundle.getMessage("TitleErrorHandlingModuleEditor");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_errorModuleEditor == null) {
            _errorModuleEditor = new ErrorModuleEditor();
            _errorModuleEditor.initComponents();
            _errorModuleEditor.setVisible(true);

            _errorModuleEditor.addEditorEventListener(() -> {
                _errorModuleEditor.editorData.forEach((key, value) -> {
                    if (key.equals("Finish")) {                  // NOI18N
                        _errorModuleEditor = null;
                        setName(getTitle());
                    }
                });
            });
        } else {
            _errorModuleEditor.setVisible(true);
        }
    }


    public interface EditorEventListener extends EventListener {

        public void editorEventOccurred();
    }


    /**
     * Editor of the error handling module.
     */
    public static class ErrorModuleEditor extends TreeEditor {

        /**
         * Maintain a list of listeners -- normally only one.
         */
        private final List<EditorEventListener> listenerList = new ArrayList<>();

        /**
         * This contains a list of commands to be processed by the listener
         * recipient.
         */
        final HashMap<String, String> editorData = new HashMap<>();

        /**
         * Construct a ConditionalEditor.
         */
        public ErrorModuleEditor() {
            super(InstanceManager.getDefault(LogixNG_Manager.class).getErrorHandlingModuleSocket(),
                    TreeEditor.EnableClipboard.EnableClipboard,
                    TreeEditor.EnableRootRemoveCutCopy.EnableRootRemoveCutCopy,
                    TreeEditor.EnableRootPopup.EnableRootPopup,
                    TreeEditor.EnableExecuteEvaluate.DisableExecuteEvaluate
            );

            ErrorModuleEditor.this.setTitle(Bundle.getMessage("TitleErrorHandlingModuleEditor"));

            ErrorModuleEditor.this.setRootVisible(false);
        }

        /** {@inheritDoc} */
        @Override
        public void windowClosed(WindowEvent e) {
            editorData.clear();
            editorData.put("Finish", "Editor");  // NOI18N
            fireEditorEvent();
        }

        public void addEditorEventListener(EditorEventListener listener) {
            listenerList.add(listener);
        }

        /**
         * Notify the listeners to check for new data.
         */
        void fireEditorEvent() {
            for (EditorEventListener l : listenerList) {
                l.editorEventOccurred();
            }
        }

    }

    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
