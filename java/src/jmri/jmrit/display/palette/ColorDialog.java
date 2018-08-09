package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.util.swing.JmriColorChooser;

/**
 * A dialog to color a Component in a Window.  The ColorChooser
 * interactively modifies the color of the component on the window
 * until the user either cancels or decides to keep the changes.
 * <p>
 * @author Pete Cressman Copyright (C) 2018
 * @since 4.13.1
 */
public class ColorDialog extends JDialog implements ChangeListener {

        JColorChooser _chooser;
        JComponent _target;
        Color _saveColor;
        ActionListener _colorAction;
        JPanel _preview;

        /**
         * 
         * @param client Window holding the component
         * @param t target whose color may be changed
         * @param ca callback to tell client the component's color was changed. 
         * May be null if client doesen't care.
         */
        public ColorDialog(Frame client, JComponent t, ActionListener ca) {
            super(client, Bundle.getMessage("ColorChooser"), true);
            _target = t;
            _saveColor = t.getBackground();
            _colorAction = ca;

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(5, 5));

            _chooser = JmriColorChooser.extendColorChooser(new JColorChooser(_target.getBackground()));
            _chooser.getSelectionModel().addChangeListener(this);
            _chooser.setPreviewPanel(new JPanel());
            panel.add(_chooser, BorderLayout.NORTH);
            panel.add(makeDoneButtonPanel(), BorderLayout.SOUTH);

            super.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancel();
                }
            });
            setContentPane(panel);
            setLocation(jmri.util.PlaceWindow.nextTo(client, t, this));
           
            pack();
            setVisible(true);
//            log.debug("ColorDialog: color= {}", _chooser.getColor());
        }

        protected JPanel makeDoneButtonPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener((ActionEvent event) -> {
                    log.debug("Done button: color= {}", _chooser.getColor());
                    if (_colorAction != null) {
                        _colorAction.actionPerformed(null);
                    }
                    JmriColorChooser.addRecentColor(_chooser.getColor());
                    dispose();
            });
            panel.add(doneButton);

            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener((ActionEvent event) -> {
                    cancel();
                });

            panel.add(cancelButton);

            return panel;
        }

        void cancel() {
            _target.setBackground(_saveColor);
            log.debug("Cancel: color= {}", _saveColor);
            dispose();
            
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            log.debug("stateChanged: color= {}", _chooser.getColor());
            _target.setOpaque(true);
            _target.setBackground(_chooser.getColor());
        }

        private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ColorDialog.class);
}

