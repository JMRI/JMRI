package jmri.jmrit.display;

import java.awt.*;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.PermissionManager;
import jmri.util.JmriJFrame;

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
        if (!InstanceManager.getDefault(PermissionManager.class).isEnabled()) {
            return;
        }
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
                        EditorPermissions.EditorPermissionEnum.Read)) {
            super.setContentPane(_hiddenPane);
            super.setJMenuBar(_hiddenMenuBar);
        } else {
            super.setContentPane(_contentPane);
            super.setJMenuBar(_menuBar);
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

}
