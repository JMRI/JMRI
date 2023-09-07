package jmri.jmrit.logixng.tools.swing;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import jmri.Manager;
import jmri.NamedBean;
import jmri.Reference;
import jmri.util.swing.JmriJOptionPane;

/**
 * Helper class for deleting a bean
 *
 * @author Daniel Bergqvist (C) 2022
 *
 * @param <E> the type of NamedBean supported by this model
 */
public class DeleteBean<E extends NamedBean> {

    private final Manager<E> _manager;

    public DeleteBean(Manager<E> manager) {
        _manager = manager;
    }

    public void delete(
            final E x,
            boolean hasChildren,
            DeleteTask<E> deleteTask,
            GetListenersRef<E> getListenersRef,
            String className) {
        delete(x, hasChildren, deleteTask, getListenersRef, className, false);
    }

    public boolean delete(
            final E x,
            boolean hasChildren,
            DeleteTask<E> deleteTask,
            GetListenersRef<E> getListenersRef,
            String className,
            boolean modal) {

        final Reference<Boolean> reference = new Reference<>(false);
        final jmri.UserPreferencesManager p;
        p = jmri.InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);

        StringBuilder message = new StringBuilder();
        try {
            _manager.deleteBean(x, "CanDelete");  // NOI18N
        } catch (PropertyVetoException e) {
            if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { // NOI18N
                log.warn("{} : Do Not Delete", e.getMessage());
                message.append(Bundle.getMessage("VetoDeleteBean", x.getBeanType(), x.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME), e.getMessage()));
                JmriJOptionPane.showMessageDialog(null, message.toString(),
                        Bundle.getMessage("QuestionTitle"),
                        JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
            message.append(e.getMessage());
        }
        List<String> listenerRefs = new ArrayList<>();
        getListenersRef.getListenerRefsIncludingChildren(x, listenerRefs);
        int listenerRefsCount = listenerRefs.size();
        log.debug("Delete with {}", listenerRefsCount);
        if (p != null && p.getMultipleChoiceOption(className, "delete") == 0x02 && message.toString().isEmpty()) {
            deleteTask.deleteBean(x);
        } else {
            final JDialog dialog = new JDialog((JFrame)null, modal);
            dialog.setTitle(Bundle.getMessage("QuestionTitle"));
            dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

            if (listenerRefsCount > 0) { // warn of listeners attached before delete
                String prompt = hasChildren
                        ? "DeleteWithChildrenPrompt" : "DeletePrompt";
                JLabel question = new JLabel(Bundle.getMessage(prompt, x.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME)));
                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.add(question);

                ArrayList<String> listeners = new ArrayList<>();
                for (String listenerRef : listenerRefs) {
                    if (!listeners.contains(listenerRef)) {
                        listeners.add(listenerRef);
                    }
                }

                message.append("<br>");
                message.append(Bundle.getMessage("ReminderInUse", listenerRefsCount));
                message.append("<ul>");
                for (String listener : listeners) {
                    message.append("<li>");
                    message.append(listener);
                    message.append("</li>");
                }
                message.append("</ul>");

                JEditorPane pane = new JEditorPane();
                pane.setContentType("text/html");
                pane.setText("<html>" + message.toString() + "</html>");
                pane.setEditable(false);
                JScrollPane jScrollPane = new JScrollPane(pane);
                container.add(jScrollPane);
            } else {
                String prompt = hasChildren
                        ? "DeleteWithChildrenPrompt" : "DeletePrompt";
                String msg = MessageFormat.format(
                        Bundle.getMessage(prompt), x.getSystemName());
                JLabel question = new JLabel(msg);
                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.add(question);
            }

            final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
            remember.setFont(remember.getFont().deriveFont(10f));
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
            JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(yesButton);
            button.add(noButton);
            container.add(button);

            noButton.addActionListener((ActionEvent e) -> {
                //there is no point in remembering this the user will never be
                //able to delete a bean!
                reference.set(false);
                dialog.dispose();
            });

            yesButton.addActionListener((ActionEvent e) -> {
                if (remember.isSelected() && p != null) {
                    p.setMultipleChoiceOption(className, "delete", 0x02);  // NOI18N
                }
                deleteTask.deleteBean(x);
                reference.set(true);
                dialog.dispose();
            });
            container.add(remember);
            container.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.setAlignmentY(Component.CENTER_ALIGNMENT);
            dialog.getContentPane().add(container);
            dialog.pack();

            dialog.getRootPane().setDefaultButton(noButton);
            noButton.requestFocusInWindow(); // set default keyboard focus, after pack() before setVisible(true)
            dialog.getRootPane().registerKeyboardAction(e -> { // escape to exit
                    dialog.setVisible(false);
                    dialog.dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - dialog.getWidth() / 2,
                    (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - dialog.getHeight() / 2);
            dialog.setModal(true);
            dialog.setVisible(true);
        }
        Boolean result = reference.get();
        return result != null && result;
    }


    public interface DeleteTask<T> {
        void deleteBean(T bean);
    }


    public interface GetListenersRef<T> {
        void getListenerRefsIncludingChildren(T bean, List<String> list);
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteBean.class);
}
