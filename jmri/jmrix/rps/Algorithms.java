// Algorithms.java

package jmri.jmrix.rps;

import javax.swing.JComboBox;
import javax.vecmath.Point3d;

/**
 *
 * Provide central access to the RPS algorithms
 * 
 * @author	   Bob Jacobsen   Copyright (C) 2007
 * @version   $Revision: 1.1 $
 */
public class Algorithms implements Constants {


    static final String[] names = new String[] {
                        "Initial Algorithm", 
                        "Ash 1.0", 
                        "Ash 1.1",
                        "Ash 2.0",
                        "Ash 2.1"
                        };
    
    public static JComboBox algorithmBox() {
        JComboBox j = new JComboBox(names);
        j.setSelectedIndex(3);
        return j;
    }

    /**
     * Create proper Calculator instance
     */
    public static Calculator newCalculator(Point3d[] points, double vs, int offset, String name) {
        if (name.equals(names[0]))
            return new InitialAlgorithm(points, vs);
            
        else if (name.equals(names[1]))
                return new Ash1_0Algorithm(points, vs);
                
        else if (name.equals(names[2]))
                return new Ash1_1Algorithm(points, vs);
                
        else if (name.equals(names[3]))
                return new Ash2_0Algorithm(points, vs, offset);
                
        else if (name.equals(names[4]))
                return new Ash2_1Algorithm(points, vs, offset);
                
        else  // default is most recent
                return new  Ash2_0Algorithm(points, vs);
    }
    
}
