package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.PermissionManager;
import jmri.util.JmriJFrame;
import jmri.util.swing.*;

/**
 * A JmriJFrame with permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class JmriJFrameWithPermissions extends JmriJFrame {

    private final JPanel _hiddenPane = new JPanel();
    private Container _contentPane = new JPanel();
    private final JMenuBar _hiddenMenuBar = new JMenuBar();
    private JMenuBar _menuBar = super.getJMenuBar();
    private boolean _keepSize = true;

    public JmriJFrameWithPermissions() {
        setupContentPaneAndMenu();
    }

    public JmriJFrameWithPermissions(String name) {
        super(name);
        setupContentPaneAndMenu();
    }

    public JmriJFrameWithPermissions(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        setupContentPaneAndMenu();
    }

    /**
     * Setup a fake content pane and a fake menu to be used if
     * the user doesn't have permission to view the panel.
     */
    private void setupContentPaneAndMenu() {
        _contentPane.setLayout(new BorderLayout() {
            /* This BorderLayout subclass maps a null constraint to CENTER.
             * Although the reference BorderLayout also does this, some VMs
             * throw an IllegalArgumentException.
             */
            @Override
            public void addLayoutComponent(Component comp, Object constraints) {
                if (constraints == null) {
                    constraints = BorderLayout.CENTER;
                }
                super.addLayoutComponent(comp, constraints);
            }
        });

        if (!InstanceManager.getDefault(PermissionManager.class).isEnabled()) {
            return;
        }
        setGlassPane(new MyGlassPane().init());
        _hiddenPane.setLayout(new GridBagLayout());  // Center innerPanel
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.add(new JLabel(Bundle.getMessage("Editor_PermissionDenied")));
        innerPanel.add(Box.createVerticalStrut(5));
        innerPanel.add(new JLabel(Bundle.getMessage("Editor_LoginToViewPanel")));
        _hiddenPane.add(innerPanel);
        switchContentPaneAndMenu();
        InstanceManager.getDefault(PermissionManager.class).addLoginListener((isLogin) -> {
            switchContentPaneAndMenu();
        });
    }

    @Override
    public void setContentPane(Container contentPane) {
        this._contentPane = contentPane;
    }

    /**
     * Switch contentPane and menu depending on whenether
     * the user has read access to the panel or not.
     */
    private void switchContentPaneAndMenu() {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .hasAtLeastPermission(EditorPermissions.EDITOR_PERMISSION,
                        EditorPermissions.EditorPermissionEnum.View)) {
            super.getGlassPane().setVisible(false);
            super.setContentPane(_hiddenPane);
            super.setJMenuBar(_hiddenMenuBar);
        } else {
            super.setContentPane(_contentPane);
            super.setJMenuBar(_menuBar);
            super.getGlassPane().setVisible(
                    ! InstanceManager.getDefault(PermissionManager.class)
                            .hasAtLeastPermission(EditorPermissions.EDITOR_PERMISSION,
                                    EditorPermissions.EditorPermissionEnum.ViewControl)
            );
        }
        // Save the bounds before pack() since pack() might resize the panel
        Rectangle bounds = getBounds();
        pack();
        if (_keepSize) {
            setBounds(bounds);
        } else {
            setLocation(bounds.x, bounds.y);
        }
        revalidate();
    }

    @Override
    public void revalidate() {
        if (_contentPane != super.getContentPane()) {
            // Ensure the contentPane is validated as well
            super.setContentPane(_contentPane);
            _contentPane.revalidate();
            super.setContentPane(_hiddenPane);
        }
        super.revalidate();
    }

    @Override
    public void setVisible(boolean b) {
        if (b && _contentPane != super.getContentPane()) {
            // Ensure the contentPane is validated as well
            super.setContentPane(_contentPane);
            super.setVisible(b);
            super.setContentPane(_hiddenPane);
        }
        super.setVisible(b);
    }

    @Override
    public Container getContentPane() {
        // We have our own content pane which may or may not be the content
        // pane that's actually in use. If the user doesn't have permission
        // to view the panel, we show another content pane instead which only
        // has a message: "Permission denied. Login to view the panel".
        return _contentPane;
    }

    @Override
    public JMenuBar getJMenuBar() {
        // We have our own menu which may or may not be the menu
        // that's actually in use. If the user doesn't have permission
        // to view the panel, we show another menu instead which is
        // empty.
        return _menuBar;
    }

    @Override
    public void setJMenuBar(JMenuBar menuBar) {
        this._menuBar = menuBar;
        switchContentPaneAndMenu();
    }

    /**
     * Should the panel keep its size when switching between normal and
     * hidden panel?
     * @param keepSize true if to keep size, false if only keep location
     */
    public final void setKeepSize(boolean keepSize) {
        this._keepSize = keepSize;
    }


    /**
     * This pane consumes all the mouse events and key events when visible.
     * It's used when the panel is read only.
     */
    private static class MyGlassPane extends JPanel
            implements JmriMouseListener, KeyListener {

        private MyGlassPane init() {
            setOpaque(false);
            addMouseListener(JmriMouseListener.adapt(this));
            addKeyListener(this);
            return this;
        }

        private void showReadOnly() {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("JmriJFrameWithPermissions_PanelReadOnly"),
                    Bundle.getMessage("JmriJFrameWithPermissions_TitleError"),
                    JmriJOptionPane.ERROR_MESSAGE);
        }

        @Override
        public void mouseClicked(JmriMouseEvent e) {
            // Do nothing
        }

        @Override
        public void mousePressed(JmriMouseEvent e) {
            e.consume();
            requestFocusInWindow();
            showReadOnly();
        }

        @Override
        public void mouseReleased(JmriMouseEvent e) {
            // Do nothing
        }

        @Override
        public void mouseEntered(JmriMouseEvent e) {
            // Do nothing
        }

        @Override
        public void mouseExited(JmriMouseEvent e) {
            // Do nothing
        }

        @Override
        public void keyTyped(KeyEvent e) {
            e.consume();
            showReadOnly();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Do nothing
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Do nothing
        }

    }

}
