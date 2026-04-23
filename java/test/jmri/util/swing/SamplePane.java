package jmri.util.swing;

import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;

/**
 * Sample Pane class for tests
 *
 * @author Bob Jacobsen Copyright 2010
 */
// sample class
public class SamplePane extends jmri.util.swing.JmriPanel {

    public SamplePane() {
    }

    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        thisInstanceNumber = SamplePane.staticIndexCount;
        SamplePane.incrementStaticInstanceNumber();

        JButton b = new JButton(new JmriNamedPaneAction("Next" + thisInstanceNumber,
                getWindowInterface(),
                jmri.util.swing.SamplePane.class.getName()));
        add(b);

        JmriNamedPaneAction act = new JmriNamedPaneAction("Extend" + thisInstanceNumber,
                getWindowInterface(),
                jmri.util.swing.SamplePane.class.getName());
        act.setHint(WindowInterface.Hint.EXTEND);
        b = new JButton(act);
        add(b);

        b = new JButton("Close" + thisInstanceNumber);
        add(b);
    }

    @Override
    public String getHelpTarget() {
        return null;
    }

    @Override
    public String getTitle() {
        return "SamplePane " + thisInstanceNumber;
    }

    @Override
    public List<JMenu> getMenus() {
        ArrayList<JMenu> list = new ArrayList<>();
        JMenu m = new JMenu("test 1");
        m.add(new JButton("sub 1"));
        m.add(new JButton("sub 2"));
        list.add(m);
        m = new JMenu("test 2");
        m.add(new JButton("sub a"));
        m.add(new JButton("sub b"));
        list.add(m);
        return list;
    }

    @Override
    public void dispose() {
        disposed.add(thisInstanceNumber);
        super.dispose();
    }

    public static void resetCounts() {
        disposed = new ArrayList<>();
        staticIndexCount = 1;
    }

    private static void incrementStaticInstanceNumber() {
        staticIndexCount++;
    }

    public static List<Integer> getDisposedList() {
        return Collections.unmodifiableList(disposed);
    }

    private int thisInstanceNumber;

    private static ArrayList<Integer> disposed;
    private static int staticIndexCount = 1;

}
