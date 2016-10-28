package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.util.JmriJFrame;

/**
 * JPanels for the various item types that come from tool Tables - e.g. Sensors,
 * Turnouts, etc.
 */
public class BackgroundItemPanel extends IconItemPanel {

    /**
     * Constructor for plain icons and backgrounds
     */
    public BackgroundItemPanel(JmriJFrame parentFrame, String type, Editor editor) {
        super(parentFrame, type, editor);
        _level = Editor.BKG;
    }

    public void init() {
        if (!_initialized) {
            if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
            Thread.yield();
            super.init(true);
            add(initBottomPanel(), 2);
            setSize(getPreferredSize());
            _initialized = true;
        }
    }

    protected JPanel instructions(boolean isBackGround) {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        JPanel panel = super.instructions(isBackGround);
        JPanel blurb = (JPanel) panel.getComponent(0);
        blurb.add(new JLabel(Bundle.getMessage("ToColorBackground", "ButtonBackgroundColor")));
        blurb.add(javax.swing.Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        return panel;
    }

    private JPanel initBottomPanel() {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        JPanel bottomPanel = new JPanel();
        JButton backgroundButton = new JButton(Bundle.getMessage("ButtonBackgroundColor"));
        backgroundButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                hideCatalog();
                new ColorDialog(_editor);
            }
        });
        backgroundButton.setToolTipText(Bundle.getMessage("ToolTipEditColor"));
        bottomPanel.add(backgroundButton);
        return bottomPanel;
    }

    class ColorDialog extends JDialog implements ChangeListener {

        JColorChooser _chooser;
        Editor _editor;
        JPanel _preview;

        ColorDialog(Editor editor) {
            super(_paletteFrame, Bundle.getMessage("ColorChooser"), true);
            _editor = editor;

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(5, 5));

            _chooser = new JColorChooser(editor.getTargetPanel().getBackground());
            _chooser.getSelectionModel().addChangeListener(this);
            _preview = new JPanel();
            _preview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 4),
                    Bundle.getMessage("PanelColor")));
            _preview.add(new JLabel(new NamedIcon("resources/logo.gif", "resources/logo.gif")), BorderLayout.CENTER);
            _chooser.setPreviewPanel(_preview);
            panel.add(_chooser, BorderLayout.CENTER);
            panel.add(makeDoneButtonPanel(), BorderLayout.SOUTH);

            setContentPane(panel);
            _preview.setBackground(_editor.getBackground());
            _preview.getParent().setBackground(_editor.getBackground());
            setSize(_paletteFrame.getSize().width, this.getPreferredSize().height);
            setLocationRelativeTo(_paletteFrame);
            if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
            pack();
            setVisible(true);
        }

        protected JPanel makeDoneButtonPanel() {
            if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener(new ActionListener() {
                ColorDialog dialog;

                public void actionPerformed(ActionEvent a) {
                    _editor.setBackgroundColor(_chooser.getColor());
                    dialog.dispose();
                }

                ActionListener init(ColorDialog d) {
                    dialog = d;
                    return this;
                }
            }.init(this));
            panel.add(doneButton);

            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener(new ActionListener() {
                ColorDialog dialog;

                public void actionPerformed(ActionEvent a) {
                    dialog.dispose();
                }

                ActionListener init(ColorDialog d) {
                    dialog = d;
                    return this;
                }
            }.init(this));
            panel.add(cancelButton);

            return panel;
        }

        public void stateChanged(ChangeEvent e) {
            if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
            _preview.setBackground(_chooser.getColor());
            _preview.getParent().setBackground(_chooser.getColor());
        }
    }
    static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BackgroundItemPanel.class.getName());
}
