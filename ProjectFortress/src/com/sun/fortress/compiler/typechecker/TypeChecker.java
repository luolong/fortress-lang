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

package com.sun.fortress.compiler.typechecker;

import com.sun.fortress.compiler.GlobalEnvironment;
import com.sun.fortress.nodes.*;
import java.util.*;

public class TypeChecker extends NodeDepthFirstVisitor_void {
    private GlobalEnvironment globals;
    private StaticParamEnv staticParams;
    private TypeEnv params; 
    
    public TypeChecker(GlobalEnvironment _globals, 
                       StaticParamEnv _staticParams, 
                       TypeEnv _params) 
    {
        globals = _globals;
        staticParams = _staticParams;
        params = _params;
    }
    
    public void forFnDecl(FnDecl that) {}
}