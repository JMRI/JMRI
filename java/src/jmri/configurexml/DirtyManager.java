package jmri.configurexml;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.*;

/**
 * The Dirty Manager is notified whenever potential PanelPro data content is added, deleted or modified.
 * During shutdown, a dialog will be displayed if any of the potential data is 'dirty'.
 * The user will have an opportunity to store the data before shutdown proceeds.
 *
 * The basic process is to listen for propertyChange events.  When a relevant event occurs, the global
 * dirty flag is set.  When the store process has completed, the dirty flag is cleared.
 *
 * During PanelPro data file loading, the propertyChange events are ignored.
 *
 * The propertyChange events can be triggered by managers, individual beans, panels and other special
 * objects such as SSL (blockboss).
 *
 * @author Dave Sand Copyright (c) 2022
 */
public class DirtyManager {

    boolean _dirty = false;
    boolean _loading = false;

    /**
     * During JMRI startup, the InstanceManager might not have default classes ready in time.
     * The process that assigns listeners to the beans uses the queue to handle the potential delays.
     */
    final private Deque<Object> queue = new ArrayDeque<>();

    final private PropertyChangeListener _mgrListen = new ManagerListener();
    final private PropertyChangeListener _beanListen = new BeanListener();

    public DirtyManager() {
    }

    /**
     * When a manager notifies ConfigXmlManager that it is responsible for creating a portion of
     * the PanelPro data file, the manager is forwarded here so that its changes can be captured.
     * @param o The manager object.
     */
    protected void addManager(Object o) {
        log.info("++ 1 dm add manager :: {}", o.getClass().getName());
        if (o instanceof jmri.managers.AbstractManager) {
            var mgr = (jmri.managers.AbstractManager) o;
            mgr.removePropertyChangeListener(_mgrListen);
            mgr.addPropertyChangeListener(_mgrListen);
            queue.push(o);
            loadBeans();
        } else {
            log.info("!! Unable to identify class:  {}", o);
        }
    }

    /**
     * Attach a listener to all of the children of a manager to listen for bean changes.
     * The manager objects are placed in a queue which is supplies the managers to be used.
     * If the InstanceManager has not finished processing a new manager, the name is place
     * back on the queue to be processed later.
     */
    private void loadBeans() {
        log.info(".. 2 Start loadBeans");
        queue.forEach((q) -> {
            log.info(">> 3  {}", q);
        });

        while (!queue.isEmpty()) {
            var o = queue.pop();
            log.info("~~ 4 Load beans for {}", o);

            if (o instanceof BlockManager) {
                if (InstanceManager.containsDefault(BlockManager.class)) {
                    var mgr = InstanceManager.getDefault(BlockManager.class);
                    mgr.getNamedBeanSet().forEach((bean) -> {
                        bean.removePropertyChangeListener(_beanListen);
                        bean.addPropertyChangeListener(_beanListen);
                    });
                } else {
                    log.info("ee  retry {}", o);
                    queue.push(o);
                    return;
                }
                continue;
            }

            if (o instanceof MemoryManager) {
                if (InstanceManager.containsDefault(MemoryManager.class)) {
                    var mgr = InstanceManager.getDefault(MemoryManager.class);
                    mgr.getNamedBeanSet().forEach((bean) -> {
                        bean.removePropertyChangeListener(_beanListen);
                        bean.addPropertyChangeListener(_beanListen);
                    });
                } else {
                    log.info("ee  retry {}", o);
                    queue.push(o);
                    return;
                }
                continue;
            }

            if (o instanceof SensorManager) {
                if (InstanceManager.containsDefault(SensorManager.class)) {
                    var mgr = InstanceManager.getDefault(SensorManager.class);
                    mgr.getNamedBeanSet().forEach((bean) -> {
                        bean.removePropertyChangeListener(_beanListen);
                        bean.addPropertyChangeListener(_beanListen);
                    });
                } else {
                    log.info("ee  retry {}", o);
                    queue.push(o);
                    return;
                }
                continue;
            }

            if (o instanceof TurnoutManager) {
                if (InstanceManager.containsDefault(TurnoutManager.class)) {
                    var mgr = InstanceManager.getDefault(TurnoutManager.class);
                    mgr.getNamedBeanSet().forEach((bean) -> {
                        bean.removePropertyChangeListener(_beanListen);
                        bean.addPropertyChangeListener(_beanListen);
                    });
                } else {
                    log.info("ee  retry {}", o);
                    queue.push(o);
                    return;
                }
                continue;
            }
        }
        log.info(".. 5 Finish loadBeans");
    }

    /**
     * Set the dirty flag true or false.
     * True occurs when changes are reported by listener events and other special cases.
     * After a Store, the flag is set to false.
     * @param dirty Either true or false.
     */
    public void setDirty(boolean dirty) {
        log.info("## dirty flag = {}", dirty);
        _dirty = dirty;
    }

    /**
     * This is used by storeIfNeeded which is invoked by the shutdown manager.  If true, provide an
     * option to do the Store process before the shutdown occurs.
     * @return the dirty state.
     */
    public boolean isDirty() {
        return _dirty;
    }

    /**
     * This is used to ignore the changes that naturally occur data loading.
     * Set by the PanelPro data load processes to before loading and false when done.
     * @param loading Either true or false.
     */
    public void setLoading(boolean loading) {
        log.info("@@ loading flag = {}", loading);
        _loading = loading;
    }

    /**
     * This in invoked by the various dirty processes to see if a load is in process.
     * @return the loading state.
     */
    public boolean isLoading() {
        return _loading;
    }

    /**
     * If the dirty state is true, provide the user with the opporutnity to do a Store before
     * the shutdown completes.  This is invoked by the shutdown manager.
     */
    public void storeIfNeeded() {
        if (isDirty()) {
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
        }
    }

    /**
     * Listen for changes from the managers.  The primary event is the <b>length</b> which indicates
     * a bean has been added to deleted.  When this occurs, the bean listeners are updated.  The
     * other events are default values that some managers have, such as sensor debounce values.
     */
    class ManagerListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (!isLoading()) {
                log.info("== mgr evt {}", evt);
                if (evt.getPropertyName().equals("length")) {
                    log.info("== 0 mgr evt {}", evt.getSource());
                    setDirty(true);
                    queue.push(evt.getSource());
                    loadBeans();
                }
            }
        }
    }

    /**
     * Listen for changes for individual beans.
     */
    class BeanListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (!isLoading()) {
                log.info("-- bean evt {}", evt);
                setDirty(true);
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirtyManager.class);

}
