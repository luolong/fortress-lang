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

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Set;

public class ABoundingMap<T, U, L extends LatticeOps<U>> extends AbstractMap<T, U> implements BoundingMap<T,U, L>
{
    volatile ABoundingMap<T, U, L> dualMap;
    // Must be volatile due to lazy initialization / double-checked locking.
    BATree<T, U> table;
    LatticeOps<U> lattice;

    public ABoundingMap(BATree<T, U> table2, LatticeOps<U> lattice_operations,
                        ABoundingMap<T, U, L> supplied_dual) {
        table = table2;
        lattice = lattice_operations;
        dualMap = supplied_dual;
    }

    public ABoundingMap(BATree<T, U> table2, LatticeOps<U> lattice_operations) {
        this(table2, lattice_operations, null);
    }

    public ABoundingMap(LatticeOps<U> lattice_operations, Comparator<T> comparator) {
        this(new BATree<T,U>(comparator), lattice_operations, null);
    }

    public ABoundingMap<T,U,L> copy() {
        return new ABoundingMap<T,U,L>(table.copy(), lattice, null);
    }

    // We don't like this because it lacks the copy semantics that we desire.

//    public ABoundingMap(LatticeOps<U> lattice_operations) {
//        this(new HashMap<T,U>(), lattice_operations, null);
//    }
//
    public ABoundingMap<T, U, L> dual() {
        if (dualMap == null) {
            synchronized(table) {
                if (dualMap == null)
                    dualMap = new ABoundingMap<T, U, L>(table, lattice.dual(), this);
            }
        }
        return dualMap;
    }
    /** puts min/intersection of v and old */
    public U meetPut(T k, U v) {
        U old = table.get(k);
        if (old != null) {
            v = lattice.meet(old, v);
            table.put(k, v);
        } else {
            table.put(k, v);
            return v;
        }
        return v;
    }
    /** puts max/union of v and old */
    public U joinPut(T k, U v) {
        U old = table.get(k);
        if (old != null) {
            v = lattice.join(old, v);
            table.put(k, v);
        } else {
            table.put(k, v);
        }
        return v;
    }

    public U put(T k, U v) {
        return joinPut(k, v);
    }

    /** Used for backtracking during unification */
    public void assign(BoundingMap<T,U,L> replacement) {
        table = ((ABoundingMap<T,U,L>)replacement).table.copy();
        dualMap = null;
    }

    @Override
    public Set<java.util.Map.Entry<T, U>> entrySet() {
        return table.entrySet();
    }
}
