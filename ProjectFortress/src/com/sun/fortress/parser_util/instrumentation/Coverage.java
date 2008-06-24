/*******************************************************************************
    Copyright 2008 Sun Microsystems, Inc.,
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

package com.sun.fortress.parser_util.instrumentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sun.fortress.useful.Useful;

/*
 * Parser coverage command-line tool
 *
 * usage: java com.sun.parser_util.instrumentation.Coverage 
 *   (no arguments)
 * 
 * The coverage tool runs the instrumented parser (c.s.f.parser.FortressInstrumented,
 * generated by InstrumentedParserGenerator) on the test files in TEST_DIRS ("tests" 
 * and "static_tests"), excluding the ones expected to raise a parse error ("XXX*.fs[is]"). 
 * These files constitute the sample corpus. It reports coverage of the Fortress grammar 
 * based on all of the test files together.
 * 
 * The report currently consists of two parts: a summary and a detailed analysis of 
 * alternates.
 * 
 * Example summary line:
 *   74% alternate coverage in com.sun.fortress.parser.Type (attempted  97%)
 *
 * Of all of the alternates belonging to all of the nonterminals in module Type, 
 * the parser started to try 97% of the alternates, but only 74% of them had
 * committed parses. The other 23% contains alternates that failed to parse and
 * also alternates that successfully parsed but were reverted because they were on 
 * a branch that failed later.
 *
 * Example detailed section:
 *   Module com.sun.fortress.parser.Symbol
 *     EncloserPair case 1/1 (no name) was never tried
 *     ExponentOp case 1/1 (no name) never succeeded
 *     bar case 1/1 (no name) had successful parses reverted
 *     closingComprehension case 1/4 (no name) had successful parses reverted
 *     closingComprehension case 2/4 (no name) had successful parses reverted
 *     closingComprehension case 3/4 (no name) was never tried
 *     closingComprehension case 4/4 (no name) was never tried
 *     ...
 *
 * The EncloserPair nonterminal's single alternate was never tried.
 * The ExponentOp nonterminals' single alternate was tried, but always failed.
 * The bar nonterminal's single alternate was tried and successfully parsed, but
 * only on a failed branch; it had no surviving successful parses.
 * The closingComprehension nonterminal has four cases, and the coverage tool 
 * reports them separately.
 */

public class Coverage {

    private static final String[] TEST_DIRS = 
        new String[] {"tests", "static_tests"};

    private static final boolean RETHROW_PROGRAM_ERRORS = false;
    private static final boolean INCLUDE_OK_ALTERNATES = true;
    private static final boolean SHOW_FREQUENCIES = true;

    /* Use reflection for two reasons:
     * 1) To allow this collection to be compiled in "compile" target, 
     *    when FortressInstrumented may not yet exist.
     * 2) To allow for eventual separation into separate utility.
     */
    private static final String PARSER_CLASS = 
        "com.sun.fortress.parser.FortressInstrumented";
    private static final String PARSER_METHOD = "pFile";
    private static final String PARSER_INFO_METHOD = "moduleInfos";

    private static class Parser {
        Class<?> parserClass;
        Constructor<?> constructor;
        Method parserMethod;
        Method infoMethod;

        Parser(Class<?> parserClass, String parserMethod, String infoMethod) {
            try {
                this.parserClass = parserClass;
                this.constructor = parserClass.getConstructor(Reader.class, String.class);
                this.parserMethod = parserClass.getDeclaredMethod(parserMethod, Integer.TYPE);
                this.infoMethod = parserClass.getDeclaredMethod(infoMethod);
            } catch (NoSuchMethodException nsme) {
                throw new RuntimeException(nsme);
            }
        }

        void parse(Reader in, String filename) {
            try {
                Object p = this.constructor.newInstance(in, filename);
                this.parserMethod.invoke(p, 0);
            } catch (InstantiationException ie) {
                throw new RuntimeException(ie);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite);
            }
        }
        Collection<Info.ModuleInfo> info() {
            try {
                return (Collection<Info.ModuleInfo>)this.infoMethod.invoke(null);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("Warning: Ignoring command-line arguments!");
        }
        Parser parser;
        try {
            parser = new Parser(Class.forName(PARSER_CLASS), PARSER_METHOD, PARSER_INFO_METHOD);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
        parseFiles(parser);
        reportCoverage(parser);
    }

    private static void parseFiles(Parser parser) {
        for (String dir : TEST_DIRS) {
            File f = new File(dir);
            File[] srcfiles = f.listFiles(new FileFilter() {
                    public boolean accept(File sf) {
                        return (sf.getName().endsWith(".fsi") || sf.getName().endsWith(".fss"))
                            && !sf.getName().startsWith("PXX") ;
                    }
                });
            System.out.println("Loading " + srcfiles.length + " test files from " + dir);
            for (File srcfile : srcfiles) {
                parseFile(parser, srcfile);
            }
        }
        System.out.println();
    }

    private static void parseFile(Parser parser, File file) {
        String filename = file.getAbsolutePath();
        Reader in = null; 
        try {
            in = Useful.utf8BufferedFileReader(filename);
            parser.parse(in, filename);
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (RuntimeException re) {
            // FIXME: Make sure this parser error is expected.
            System.out.println("Parse error in file: " + filename);
            if (RETHROW_PROGRAM_ERRORS) { throw re; }
        } finally {
            if (in != null) try { in.close(); } catch (IOException ioe) {}
        }
    }

    private static void reportCoverage(Parser parser) {
        Collection<Info.ModuleInfo> rawModuleInfos = parser.info();
        List<Info.ModuleInfo> moduleInfos;
        moduleInfos = new LinkedList<Info.ModuleInfo>(rawModuleInfos);
        Collections.sort(moduleInfos,
                         new Comparator<Info.ModuleInfo>() {
                             public int compare(Info.ModuleInfo a, Info.ModuleInfo b) {
                                 return a.module.compareTo(b.module);
                             }
                         });
        System.out.println("** Coverage summary: Alternate coverage by module");
        for (Info.ModuleInfo moduleInfo : moduleInfos) {
            printStats(moduleInfo);
        }
        System.out.println();
        System.out.println("** Coverage details");
        for (Info.ModuleInfo moduleInfo : moduleInfos) {
            printUnused(moduleInfo);
        }
    }

    private static void printStats(Info.ModuleInfo m) {
        int all = 0;
        int started = 0;
        int ended = 0;
        int committed = 0;
        for (Info.ProductionInfo p : m.productions) {
            for (Info.SequenceInfo s : p.sequences) {
                all++;
                if (s.startedCount > 0) started++;
                if (s.endedCount > 0) ended++;
                if (s.committedCount > 0) committed++;
            }
        }

        double start_coverage = ((double) started) / ((double) all) * 100.0;
        double comit_coverage = ((double) committed) / ((double) all) * 100.0;

        System.out.println(String.format("%3d%% alternate coverage in %s (attempted %3d%%)",
                                         (int)comit_coverage, m.module, (int)start_coverage));
    }

    private static void printDetails(Info.ModuleInfo m) {
        int m_all = 0;
        int m_started = 0;
        int m_ended = 0;
        int m_committed = 0;
        StringWriter string_out = new StringWriter();
        PrintWriter out = new PrintWriter(string_out);
        for (Info.ProductionInfo p : m.productions) {
            int p_all = 0;
            int p_started = 0;
            int p_ended = 0;
            int p_committed = 0;
            for (Info.SequenceInfo s : p.sequences) {
                p_all++;
                if (s.startedCount > 0) p_started++;
                if (s.endedCount > 0) p_ended++;
                if (s.committedCount > 0) p_committed++;
            }
            out.println("  Production " + p.production);
            //out.println(coverage(2, "started", p_started, p_all));
            //out.println(coverage(2, "ended", p_ended, p_all));
            out.println(coverage(2, "committed", p_committed, p_all));

            m_all++;
            if (p_started > 0) m_started++;
            if (p_ended > 0) m_ended++;
            if (p_committed > 0) m_committed++;
        }
        System.out.println("Module " + m.module);
        //System.out.println(coverage(0,"started", m_started, m_all));
        //System.out.println(coverage(0,"ended", m_ended, m_all));
        System.out.println(coverage(0,"committed", m_committed, m_all));
        System.out.println(string_out.toString());
    }

    private static void printUnused(Info.ModuleInfo m) {
        System.out.println("Module " + m.module);
        for (Info.ProductionInfo p : m.productions) {
            int sequenceCount = p.sequences.size();
            for (Info.SequenceInfo s : p.sequences) {
                if (s.committedCount == 0 || INCLUDE_OK_ALTERNATES) {
                    System.out.print(String.format("    %s case %d/%d (%s)",
                                                   p.production,
                                                   s.sequenceIndex,
                                                   sequenceCount,
                                                   s.sequence == null ? "no name" : s.sequence));
                    if (s.startedCount == 0) {
                        System.out.print(" was never tried");
                    } else if (s.endedCount == 0) {
                        System.out.print(" never succeeded");
                    } else if (s.committedCount == 0) {
                        System.out.print(" had successful parses reverted");
                    } else {
                        System.out.print(" succeeded");
                    }
                    if (SHOW_FREQUENCIES) {
                        System.out.print(frequency(s));
                    }
                    System.out.println();
                }
            }
        }
    }

    private static String frequency(Info.SequenceInfo s) {
        return String.format(" [%d-%d-%d]",
                             s.startedCount,
                             s.endedCount,
                             s.committedCount);
    }

    private static String coverage(int indent, String label, int covered, int total) {
        double percentage = (100.0 * covered) / (double)total;
        char[] indentation = new char[indent];
        Arrays.fill(indentation, ' ');
        return String.format("%s-- %s %3d%% (%d/%d)", 
                             new String(indentation),
                             label, (int)percentage, covered, total);
    }
}
