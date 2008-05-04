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
package com.sun.fortress.interpreter.rewrite;

import static com.sun.fortress.interpreter.evaluator.InterpreterBug.bug;

import java.util.List;

import com.sun.fortress.nodes.AbstractNode;
import com.sun.fortress.nodes.FnRef;
import com.sun.fortress.nodes.Id;
import com.sun.fortress.nodes.IdType;
import com.sun.fortress.nodes.InstantiatedType;
import com.sun.fortress.nodes.QualifiedIdName;
import com.sun.fortress.nodes.StaticArg;
import com.sun.fortress.nodes.VarRef;
import com.sun.fortress.nodes._RewriteFnRef;
import com.sun.fortress.nodes_util.NodeFactory;

public class RewriteInPresenceOfTypeInfo extends Rewrite {

    public static RewriteInPresenceOfTypeInfo Only = new RewriteInPresenceOfTypeInfo();
    private RewriteInPresenceOfTypeInfo() {
    }

    @Override
    public AbstractNode visit(AbstractNode node) {
        if (node instanceof QualifiedIdName) {
            QualifiedIdName q = (QualifiedIdName) node;
            if (q.getApi().isSome()) {
                node = new QualifiedIdName(q.getSpan(), q.getName());
            }
        } else if (node instanceof FnRef) {

                FnRef fr = (FnRef) node;
                List<Id> fns = fr.getFns();
                List<StaticArg> sargs = fr.getStaticArgs();
                Id idn = fns.get(0);
                QualifiedIdName qidn = NodeFactory.makeQIdfromId(idn);

                if (fns.size() != 1) {
                    return bug("Overloaded function in FnRef " + node.toStringVerbose());
                }

                if (sargs.size() > 0)
                    return visit(new _RewriteFnRef(fr.getSpan(),
                        fr.isParenthesized(),
                                                   new VarRef(idn.getSpan(),
                                                              qidn),
                        sargs));

                else
                    return visit(new VarRef(idn.getSpan(), fr.isParenthesized(), qidn));

        } else if (node instanceof InstantiatedType) {
            InstantiatedType it = (InstantiatedType) node;
            if (it.getArgs().size() == 0) {
                return visit(new IdType(it.getSpan(), it.getName()));
            }
        }
        return visitNode(node);
    }

}
