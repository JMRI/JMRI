package jmri.jmrix.rps;

import javax.swing.JComboBox;
import javax.vecmath.Point3d;

/**
 *
 * Provide central access to the RPS algorithms.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class Algorithms implements Constants {

    static final int DEFAULTALGORITHMINDEX = 3;
    static final String[] names = new String[]{
        "Ash 2.0",
        "Ash 2.1",
        "Ash 2.2",
        "Analytic A"
    };

    public static JComboBox<String> algorithmBox() {
        JComboBox<String> j = new JComboBox<String>(names);
        j.setSelectedItem(Engine.instance().getAlgorithm());
        return j;
    }

    /**
     * Create proper Calculator instance
     */
    public static Calculator newCalculator(Point3d[] points, double vs, int offset, String name) {
        if (name.equals(names[0])) {
            return new Ash2_0Algorithm(points, vs, offset);
        } else if (name.equals(names[1])) {
            return new Ash2_1Algorithm(points, vs, offset);
        } else if (name.equals(names[2])) {
            return new Ash2_2Algorithm(points, vs, offset);
        } else if (name.equals(names[3])) {
            return new Analytic_AAlgorithm(points, vs, offset);
        } else // default is most recent
        {
            return new Ash2_1Algorithm(points, vs);
        }
    }

}
