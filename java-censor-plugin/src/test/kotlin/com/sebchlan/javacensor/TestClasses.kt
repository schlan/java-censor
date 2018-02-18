package com.sebchlan.javacensor

const val testClassWithPrivateField = """
public class TestClass {

    private String testField = "test";

}
"""

const val testClassWithPublicField = """
public class TestClass {

    public String testField = "test";

}
"""


const val testClassWithPrivateMethod = """
public class TestClass {

    /**
     * Test public, return String
     */
    private String test(Object object) {
        System.out.println("asdf");
        return "asdf";
    }

}
"""

const val testClassWithPublicMethod = """
public class TestClass {

    /**
     * Test public, return String
     */
    public String test(Object object) {
        System.out.println("asdf");
        return "asdf";
    }

}
"""


const val localTestClass = """
class TestClass {

    int field2;
    public double field3;

    public TestClass() {
        System.out.println("lolo");
    }

    /**
     * Test public
     */
    public void test1() {
        System.out.println("asdf");
    }

}
"""

const val localTestInterface = """
interface TestInterface {

    String constant = "constant";

    /**
     * More public interfaces
     */
    void test();

}
"""

const val testInterface = """
public interface TestInterface {

    String constant = "constant";

    /**
     * More public interfaces
     */
    void test();

}
"""