package jmri.util.swing;

import javax.swing.JLabel;

/**
 * Test for popping test window content.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class ButtonTestAction extends jmri.util.swing.JmriAbstractAction {

    public ButtonTestAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        jmri.util.swing.JmriPanel newPane = new jmri.util.swing.JmriPanel() {
            @Override
            public String getHelpTarget() {
                return "html.doc.Technical.JUnit";
            }

            @Override
            public String getTitle() {
                return "Button Test";
            }
        };
        newPane.add(new JLabel("Test panel " + n++));
        return newPane;
    }

    static int n = 1;
}
