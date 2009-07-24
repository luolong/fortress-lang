/*******************************************************************************
 Copyright 2009 Sun Microsystems, Inc.,
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

package com.sun.fortress.syntax_abstractions.rats;

import com.sun.fortress.repository.ProjectProperties;
import com.sun.fortress.useful.Debug;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class JavaC {
    /**
     * Line separator.
     * <p/>
     * TODO: How do we obtain newline from system? TODO: Is this relevant?
     */
    static final String nl = "\n";
    static final String PATHSEP = File.pathSeparator;

    public static int compile(String sourceDir, String destinationDir, String filename) {
        Debug.debug(Debug.Type.SYNTAX, "compiling a temporary parser...");
        String classpath =
                sourceDir + PATHSEP + getFortressThirdPartyDependencyJars() + PATHSEP + getFortressBuildDir();
        String[] args = {
                "-nowarn", "-cp", classpath, "-d", destinationDir, filename
        };
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int compilationResult = com.sun.tools.javac.Main.compile(args, pw);
        String errors = sw.getBuffer().toString();
        Debug.debug(Debug.Type.SYNTAX, "done: ", compilationResult);

        if (!errors.equals("")) {
            System.err.println(errors);
        }
        return compilationResult;
    }

    private static String getFortressBuildDir() {
        return ProjectProperties.FORTRESS_AUTOHOME + File.separatorChar + "ProjectFortress" + File.separatorChar +
               "build" + File.separatorChar;
    }

    private static String getFortressThirdPartyDependencyJars() {
        String sepChar = "" + File.separatorChar;
        String thirdPartyDir =
                ProjectProperties.FORTRESS_AUTOHOME + File.separatorChar + "ProjectFortress" + File.separatorChar +
                "third_party" + File.separatorChar;

        String jars = "";
        jars += thirdPartyDir + "ant" + sepChar + "ant-junit.jar" + PATHSEP;
        jars += thirdPartyDir + "ant" + sepChar + "ant.jar" + PATHSEP;
        jars += thirdPartyDir + "jsr166y" + sepChar + "jsr166y.jar" + PATHSEP;
        jars += thirdPartyDir + "junit" + sepChar + "junit.jar" + PATHSEP;
        jars += thirdPartyDir + "junit" + sepChar + "junit_src.jar" + PATHSEP;
        jars += thirdPartyDir + "plt" + sepChar + "plt.jar" + PATHSEP;
        jars += thirdPartyDir + "xtc" + sepChar + "xtc.jar";

        return jars;
    }

}
