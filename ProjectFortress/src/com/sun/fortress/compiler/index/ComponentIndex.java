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

import com.sun.fortress.nodes.*;
import edu.rice.cs.plt.collect.Relation;

import java.util.*;

public class ComponentIndex extends CompilationUnitIndex {

    private final Set<VarDecl> _initializers;

    public ComponentIndex(Component ast, Map<Id, Variable> variables, Set<VarDecl> initializers, Relation<IdOrOpOrAnonymousName, Function> functions, Set<ParametricOperator> parametricOperators, Map<Id, TypeConsIndex> typeConses, Map<Id, Dimension> dimensions, Map<Id, Unit> units, long modifiedDate) {
        super(ast, variables, functions, parametricOperators, typeConses, dimensions, units, modifiedDate);
        _initializers = initializers;
    }

    public Set<VarDecl> initializers() {
        return Collections.unmodifiableSet(_initializers);
    }

    public String toString() {
        return "Component Index" + "\nVariables: " + variables() + "\ninitializers: " + initializers() + "\nFunctions: " + functions() + "\nparametricOperators:" + parametricOperators() + "\nTypeConses: " + typeConses() + "\nDimensions: " + dimensions() + "\nUnits: " + units();
    }

    @Override
    public Set<APIName> exports() {
        List<APIName> exports = ((Component) ast()).getExports();
        Set<APIName> result = new HashSet<APIName>();

        result.addAll(exports);
        return result;
    }

}
