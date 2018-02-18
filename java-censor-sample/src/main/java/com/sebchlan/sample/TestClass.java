package com.sebchlan.sample;

/**
 * Class docs
 */
public class TestClass {

    String field1;
    int field2;
    public double field3;
    long field4;

    public TestClass() {
        System.out.println("lolo");
    }

    /**
     * Test public
     */
    public void test1() {
        System.out.println("asdf");
    }

    /**
     * Test local
     */
    void test2() {
        System.out.println("asdf");
    }

    /**
     * Test private
     */
    private void test3() {
        System.out.println("asdf");
    }

    /**
     * Test protected
     */
    protected void test6() {
        System.out.println("asdf");
    }

    /**
     * Test public, return String
     */
    public String test4(Object object) {
        System.out.println("asdf");
        return "asdf";
    }

    /**
     * Test public, return int
     */
    public int test5(Object object) {
        System.out.println("asdf");
        return 1;
    }

    class Foobar {

    }

    public class Foobar2 {

    }

}
