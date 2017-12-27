package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;

/**
 * JPanels for the Panel Backgrounds.
 */
public class BackgroundItemPanel extends IconItemPanel {

    /**
     * Constructor for plain icons and backgrounds.
     */
    public BackgroundItemPanel(DisplayFrame parentFrame, String type, Editor editor) {
        super(parentFrame, type, editor);
        _level = Editor.BKG;
    }

    @Override
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

    @Override
    protected JPanel instructions(boolean isBackGround) {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        JPanel panel = super.instructions(isBackGround);
        JPanel blurb = (JPanel) panel.getComponent(0);
        blurb.add(new JLabel(Bundle.getMessage("ToColorBackground", Bundle.getMessage("ButtonBackgroundColor"))));
        blurb.add(javax.swing.Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        return panel;
    }

    private JPanel initBottomPanel() {
        if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
        JPanel bottomPanel = new JPanel();
        JButton backgroundButton = new JButton(Bundle.getMessage("ButtonBackgroundColor"));
        backgroundButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                hideCatalog();
                new ColorDialog(_editor);
            }
        });
        backgroundButton.setToolTipText(Bundle.getMessage("ToolTipEditColor"));
        bottomPanel.add(backgroundButton);
        return bottomPanel;
    }

    @Override
    protected JPanel makeBgButtonPanel(ImagePanel preview1, ImagePanel preview2, BufferedImage[] imgArray, DisplayFrame parent) {
        return null; // no button to set Preview Bg on BackgroundItemPanel
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

                @Override
                public void actionPerformed(ActionEvent a) {
                    _editor.setBackgroundColor(_chooser.getColor());
                    _currentBackground = _chooser.getColor();
                    _backgrounds[0] = DrawSquares.getImage(500, 400, 10, _currentBackground, _currentBackground); // replace panel color preview image in array
                    _iconPanel.setImage(_backgrounds[0]); // show new value in preview pane
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

                @Override
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

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!jmri.util.ThreadingUtil.isGUIThread()) log.error("Not on GUI thread", new Exception("traceback"));
            _preview.setBackground(_chooser.getColor());
            _preview.getParent().setBackground(_chooser.getColor());
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BackgroundItemPanel.class);

}
