package jmri;


import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test ActionTurnout
 *
 * @author Daniel Bergqvist 2018
 */
public class CategoryTest {

    @Test
    public void testCategory() {
        Category myCategory1 = Category.registerCategory(
                new MyCategory("MyCategory", "MyDescription", 130));

        Assert.assertNotNull("object exists", myCategory1);
        Assert.assertEquals("Correct name", "MyCategory", myCategory1.name());
        Assert.assertEquals("Correct description", "MyDescription", myCategory1.toString());
        Assert.assertEquals("Correct order", 130, myCategory1.order());

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Category.registerCategory(new MyCategory("MyCategory", "MyDescription", 130));
        });
        Assertions.assertEquals("Category 'MyCategory' with description 'MyDescription' is already registered",
                thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Category.registerCategory(new MyCategory("MyOtherCategory", "MyDescription", 130));
        });
        Assertions.assertEquals("Category 'MyOtherCategory' with description 'MyDescription' is already registered",
                thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Category.registerCategory(new MyCategory("MyCategory", "MyOtherDescription", 130));
        });
        Assertions.assertEquals("Category 'MyCategory' with description 'MyOtherDescription' is already registered",
                thrown.getMessage());

        Category myCategory2 = Category.registerCategory(
                new MyCategory("MyOtherCategory", "MyOtherDescription", 130));
        Assert.assertNotNull("object exists", myCategory2);
        Assert.assertEquals("Correct name", "MyOtherCategory", myCategory2.name());
        Assert.assertEquals("Correct description", "MyOtherDescription", myCategory2.toString());
        Assert.assertEquals("Correct order", 130, myCategory2.order());

        Category myCategory3 = Category.registerCategory(
                new MyCategory("MyOtherOtherCategory", "MyOtherOtherDescription", 220));
        Assert.assertNotNull("object exists", myCategory3);
        Assert.assertEquals("Correct name", "MyOtherOtherCategory", myCategory3.name());
        Assert.assertEquals("Correct description", "MyOtherOtherDescription", myCategory3.toString());
        Assert.assertEquals("Correct order", 220, myCategory3.order());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }


    public static final class MyCategory extends Category {

        public MyCategory(String name, String description, int order) {
            super(name, description, order);
        }
    }

}
