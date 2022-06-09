package jmri.configurexml.swing;

import java.awt.HeadlessException;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.configurexml.ShutdownPreferences;
import jmri.configurexml.ShutdownPreferences.DialogDisplayOptions;

/**
 * Swing dialog notify that there is un-stored PanelPro data changes.
 *
 * @author Dave Sand Copyright (c) 2022
 */
public class StoreAndCompareDialog {

    private static ShutdownPreferences _preferences = jmri.InstanceManager.getDefault(ShutdownPreferences.class);

    static public void showDialog() {
        if (_preferences.getDisplayDialog().equals(ShutdownPreferences.DialogDisplayOptions.SkipDialog)) {
            performStore();
            return;
        }

        try {
            // Provide option to invoke the store process before the shutdown.
            final JDialog dialog = new JDialog();
            dialog.setTitle(Bundle.getMessage("QuestionTitle"));     // NOI18N
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            JLabel question = new JLabel(Bundle.getMessage("StoreAndCompareRequest"));  // NOI18N
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);

            JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));    // NOI18N
            JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));      // NOI18N
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(noButton);
            button.add(yesButton);
            container.add(button);

            noButton.addActionListener((ActionEvent e) -> {
                dialog.dispose();
                return;
            });

            yesButton.addActionListener((ActionEvent e) -> {
                dialog.setVisible(false);
                performStore();
                dialog.dispose();
                return;
            });

            container.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.setAlignmentY(Component.CENTER_ALIGNMENT);
            dialog.getContentPane().add(container);
            dialog.pack();
            dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - dialog.getHeight() / 2);
            dialog.setModal(true);
            dialog.setVisible(true);

        } catch (HeadlessException ex) {
            // silently do nothig - we can't display a dialog and shutdown continues without a store.
        }
    }

    static private void performStore() {
        new jmri.configurexml.StoreXmlUserAction("").actionPerformed(null);
    }
}
