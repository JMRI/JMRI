package jmri.configurexml;

/**
 * Test bean for DefaultJavaBeanConfigXMLTest
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class TestBean1 {

    String a = "<sample>";

    public void setA(String a) {
        this.a = a;
    }

    public String getA() {
        return a;
    }

    int b = 127;

    public void setB(int b) {
        this.b = b;
    }

    public int getB() {
        return b;
    }

    @Override
    public boolean equals(Object o1) {
        TestBean1 o2 = (TestBean1) o1;
        return a.equals(o2.a) && (b == o2.b);
    }
    
    @Override
    public int hashCode() {
        if (a == null) return 0;
        return a.hashCode();
    }
}
