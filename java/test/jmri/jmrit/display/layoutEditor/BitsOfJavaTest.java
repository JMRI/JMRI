package jmri.jmrit.display.layoutEditor;

import java.util.*;

import jmri.util.JUnitUtil;

import org.junit.*;


/**
 * Checking some idioms that seem marginal
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class BitsOfJavaTest {

// List<Sub> subList = new ArrayList<>();
// List<Super> superList = subList;
    
class Super {
}

class Sub extends Super {
}

}
