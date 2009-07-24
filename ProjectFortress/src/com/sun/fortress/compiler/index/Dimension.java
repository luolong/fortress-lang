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

package com.sun.fortress.compiler.index;

import com.sun.fortress.nodes.DimDecl;
import com.sun.fortress.nodes.NodeUpdateVisitor;
import com.sun.fortress.nodes.StaticParam;

import java.util.ArrayList;
import java.util.List;

public class Dimension extends TypeConsIndex {
    private final DimDecl ast;

    public Dimension(DimDecl _ast) {
        ast = _ast;
    }

    public DimDecl ast() {
        return ast;
    }

    @Override
    public List<StaticParam> staticParameters() {
        return new ArrayList<StaticParam>();
    }

    @Override
    public TypeConsIndex acceptNodeUpdateVisitor(NodeUpdateVisitor visitor) {
        return new Dimension((DimDecl) ast.accept(visitor));
    }
}
