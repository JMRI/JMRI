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

/**
 * Swing dialog notify that there is un-stored PanelPro data changes.
 *
 * @author Dave Sand Copyright (c) 2022
 */
public class DirtyManagerDialog {

    static public void showDialog() {
        try {
            // Provide option to invoke the store process before the shutdown.
            final JDialog dialog = new JDialog();
            dialog.setTitle(Bundle.getMessage("QuestionTitle"));     // NOI18N
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            JLabel question = new JLabel(Bundle.getMessage("DirtyManagerQuitNotification"));  // NOI18N
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
                new jmri.configurexml.StoreXmlUserAction("").actionPerformed(null);
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
}








    /**
     * Handle error by formatting and putting up a dialog box
     *
     * @param e the error memo
     */
//     @Override
//     public void showDialog() {
//         // first, send to log
//         super.handle(e);
//
//         try {
//             // then do dialog
//             StringBuilder m = new StringBuilder("<html>").append(e.description);
//             if (e.systemName != null) {
//                 m.append(" System name \"").append(e.systemName).append("\"");
//             }
//             if (e.userName != null && !e.userName.isEmpty()) {
//                 m.append("<br> User name \"").append(e.userName).append("\"");
//             }
//             if (e.operation != null) {
//                 m.append("<br> while ").append(e.operation);
//             }
//             if (e.adapter != null) {
//                 m.append("<br> in adaptor of type ").append(e.adapter.getClass().getName());
//             }
//             if (e.exception != null) {
//                 m.append("<br> Exception: ").append(e.exception.toString());
//             }
//             m.append("<br> See http://jmri.org/help/en/package/jmri/configurexml/ErrorHandler.shtml for more information.</html>");
//
//             jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
//                     showErrorMessage("Error during " + e.title, m.toString(), e.description, "", true, false);
//         } catch (HeadlessException ex) {
//             // silently do nothig - we can't display a dialog and have already
//             // logged the error
//         }
//     }
