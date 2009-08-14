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

package com.sun.fortress.useful;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MacPortsHelper {

    static  Memo1<String, TopSortItemImpl<String>> aTable() {
        return 
        new Memo1<String, TopSortItemImpl<String>>(new Factory1<String, TopSortItemImpl<String>>() {

            public TopSortItemImpl<String> make(String part1) {
                return new TopSortItemImpl<String>(part1);
            }

        });
    }
    
    static Memo1<String, TopSortItemImpl<String>> table = aTable();

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("java ... MacPortsHelper <portdepsdir> [-use/-dep/-rebuild] [<roots> ...]");
            System.err.println("MacPortsHelper takes a directory expected to contain files named <portname>.");
            System.err.println("Each file contains the output of 'port deps <portname>");
            System.err.println("Such a directory could be produced by executing the (bash) commands: ");
            System.err.println("  mkdir portdeps");
            System.err.println("  for i in `port list installed | awk '{print $1'}` ; do ");
            System.err.println("    port deps $i > portdeps/$i");
            System.err.println("  done");
            System.err.println("The result is a topologically sorted list of ports, most dependent first.");
            System.err.println("The list may be uninstalled in order (mostly).");
            System.err.println("The options -uses/-needs/-rebuild affect how subsets are chosen.");
            System.err.println("  -use root1 ... rootN = roots, and everything that uses roots (transitively)");
            System.err.println("  -dep root1 ... rootN = roots, and everything that roots depend on (transitively)");
            System.err.println("  -rebuild root1 ... rootN = roots, everything that they depend on, and everything else that uses what they need");
            System.err.println("Examples:");
            System.err.println("  java MacPortsHelper portdeps -use python26");
            System.err.println("  java MacPortsHelper portdeps -dep python26");
        } else {
            String dirname = args[0];
            File[] fileArray = Files.ls(dirname);
            Set<String> ports = new HashSet<String>();
            for (int i = 0; i < fileArray.length; i++) {
                File f = fileArray[i];
                String a = f.getName();
                ports.add(a);
            }
            for (int i = 0; i < fileArray.length; i++) {
                File f = fileArray[i];
                BufferedReader br = Useful.utf8BufferedFileReader(f);
                String a = f.getName();
                TopSortItemImpl<String> node = table.make(a);
                String b = br.readLine();
                while (b != null) {
                    b = b.trim();
                    // No sense creating an edge to pseudo-port
                    if (ports.contains(b)) node.edgeTo(table.make(b));
                    b = br.readLine();
                }
            }

            // Handle rooted subsets.
            if (args.length > 1) {
                int argl = 1;
                String s = args[1];
                boolean rebuild = false;
                boolean depends = false;
                if ("-rebuild".equals(s)) {
                    rebuild = true;
                    argl = 2;
                } else if (s.startsWith("-use")) {
                    argl = 2;
                } else if (s.startsWith("-dep")) {
                    depends = true;
                    argl = 2;
                }                

                Memo1<String, TopSortItemImpl<String>> newTable = aTable();
                Memo1<String, TopSortItemImpl<String>> rtable = reverse(table);
                
                if (rebuild) {
                    for (int argi = argl; argi < args.length; argi++) {
                        String root = args[argi].trim();
                        if (! newTable.known(root))
                            copyRooted(table, newTable, root);
                    }

                    Memo1<String, TopSortItemImpl<String>> buildTable = aTable();
                    for (TopSortItemImpl<String> dep : newTable.values()) {
                        String sdep = dep.x;
                        if (! buildTable.known(sdep)) 
                            copyRooted(rtable, buildTable, sdep);
                    }
                    table = reverse(buildTable);
                } else {
                    Memo1<String, TopSortItemImpl<String>>  tab = depends ? table : rtable;
                    for (int argi = argl; argi < args.length; argi++) {
                        String root = args[argi].trim();
                        if (! newTable.known(root))
                            copyRooted(tab, newTable, root);
                    }

                    table = depends ? newTable : reverse(newTable);
                }            
            }

            List<TopSortItemImpl<String>> ordered = TopSort.depthFirst(table.values());
            for (TopSortItemImpl i : ordered) {
                System.out.println(i.x);
            }
        }
    }

    private static void copyRooted(
            Memo1<String, TopSortItemImpl<String>> table,
            Memo1<String, TopSortItemImpl<String>> newTable, String root) {
            
            TopSortItemImpl<String> new_root = newTable.make(root);
            TopSortItemImpl<String> node = table.make(root);
            
            for (TopSortItemImpl<String> succ : node.succs ) {
                String s = succ.x;
                if (newTable.known(s))
                    continue;
                TopSortItemImpl<String> new_succ = newTable.make(s);
                new_root.edgeTo(new_succ);
                copyRooted(table, newTable, s);
            }
        
    }
    
    private static Memo1<String, TopSortItemImpl<String>> reverse
        (Memo1<String, TopSortItemImpl<String>> table) {
        Memo1<String, TopSortItemImpl<String>> newTable = aTable();
        for (TopSortItemImpl<String> node : table.values()) {
            TopSortItemImpl<String> new_node = newTable.make(node.x);
            for (TopSortItemImpl<String> succ : node.succs) {
                TopSortItemImpl<String> new_succ = newTable.make(succ.x);
                new_succ.edgeTo(new_node);
            }
        }
        return newTable;
    }

}
