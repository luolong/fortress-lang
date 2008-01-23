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

package com.sun.fortress.interpreter.evaluator.values;

import com.sun.fortress.interpreter.env.BetterEnv;
import com.sun.fortress.interpreter.evaluator.types.FType;


public class FObject extends FConstructedValue implements Selectable {

    final BetterEnv lexicalEnv;
    final BetterEnv selfEnv;
    public FObject(FType selfType, BetterEnv lexical_env, BetterEnv self_dot_env) {
        super(selfType);
        this.lexicalEnv = lexical_env;
        this.selfEnv = self_dot_env;
    }

    public FValue select(String s) {
        return getSelfEnv().getValue(s);
    }

    /**
     * The environment that you get from "self."
     */
    public BetterEnv getSelfEnv() {
        return selfEnv;
    }

    public BetterEnv getLexicalEnv() {
        return lexicalEnv;
    }

    public String getString() { return type().toString(); }

    public String toString() {
        return getString() + getClass().getSimpleName();
    }
}
