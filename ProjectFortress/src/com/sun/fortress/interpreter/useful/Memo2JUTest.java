/*******************************************************************************
    Copyright 2007 Sun Microsystems, Inc.,
    4150 Network Circle, Santa Clara, California 95054, U.S.A.
    All rights reserved.

    U.S. Government Rights - Commercial software.
    Government users are subject to the Sun Microsystems, Inc. standard
    license agreement and applicable provisions of the FAR and its supplements.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.

    Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
    trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 ******************************************************************************/

package com.sun.fortress.interpreter.useful;

import junit.framework.TestCase;

public class Memo2JUTest extends com.sun.fortress.interpreter.useful.TcWrapper  {

    public Memo2JUTest() {
        super("Memo2JUTest");
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(Memo2JUTest.class);
    }

    /*
     * Test method for 'com.sun.fortress.interpreter.useful.Map2.make(Index1, Index2)'
     */
    public void testMake() {
        Memo2<String, String, String> m2 = new Memo2<String, String, String>(new SFactory());

        String s1 = m2.make("cat", "dog");
        String s2 = m2.make("cat", "dog");
        assertEquals("catdog", s1);
        assertEquals(s1, s2);
        assertEquals(1, m2.map.size()); // Fragile test, depends on impl

        String s3 = m2.make("ca", "tdog");
        String s4 = m2.make("ca", "tdog");
        assertEquals("catdog", s1);
        assertEquals(s3, s4);
        assertEquals(2, m2.map.size()); // Fragile test, depends on impl
        assertNotSame(s1, s3);

    }

    static class SFactory implements Factory2<String, String, String> {

        /* (non-Javadoc)
         * @see com.sun.fortress.interpreter.useful.Factory2#make(Part1, Part2)
         */
        public String make(String part1, String part2) {
            return part1+part2;
        }

    }

}
