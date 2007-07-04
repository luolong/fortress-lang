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

package com.sun.fortress.interpreter.nodes;

import com.sun.fortress.interpreter.nodes_util.*;
import com.sun.fortress.interpreter.useful.MagicNumbers;

public class SubscriptAssign extends OprName {
    public SubscriptAssign(Span span) {
        super(span);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.fortress.interpreter.nodes.FnName#mandatoryEquals(java.lang.Object)
     */
    public boolean equals(Object o) {
        SubscriptAssign sa = (SubscriptAssign) o;
        return NodeUtil.getName(sa).equals(NodeUtil.getName(this));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.fortress.interpreter.nodes.FnName#mandatoryHashCode()
     */
    public int hashCode() {
        return NodeUtil.getName(this).hashCode() * MagicNumbers.R;
    }

    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return v.forSubscriptAssign(this);
    }
}
