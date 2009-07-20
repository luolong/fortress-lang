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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.fortress.compiler.typechecker.TypesUtil;
import com.sun.fortress.nodes.BaseType;
import com.sun.fortress.nodes.Expr;
import com.sun.fortress.nodes.Id;
import com.sun.fortress.nodes.IdOrOpOrAnonymousName;
import com.sun.fortress.nodes.NodeUpdateVisitor;
import com.sun.fortress.nodes.Param;
import com.sun.fortress.nodes.StaticArg;
import com.sun.fortress.nodes.StaticParam;
import com.sun.fortress.nodes.Type;
import com.sun.fortress.nodes.WhereClause;
import com.sun.fortress.nodes_util.NodeFactory;
import com.sun.fortress.nodes_util.NodeUtil;
import com.sun.fortress.nodes_util.Span;
import com.sun.fortress.useful.NI;

import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.tuple.Option;

public class Constructor extends Function {

    private final Id _declaringTrait;
    private final List<StaticParam> _staticParams;
    private final Option<List<Param>> _params;
    private final Option<List<BaseType>> _throwsClause;
    private final Option<WhereClause> _where;

    public Constructor(Id declaringTrait,
                       List<StaticParam> staticParams,
                       Option<List<Param>> params,
                       Option<List<BaseType>> throwsClause,
                       Option<WhereClause> where)
    {
        _declaringTrait = declaringTrait;
        _staticParams = staticParams;
        _params = params;
        _throwsClause = throwsClause;
        _where = where;
        
        putThunk(new Thunk<Option<Type>>() {
          @Override public Option<Type> value() {
            return Option.<Type>some(
                NodeFactory.makeTraitType(_declaringTrait,
                                          TypesUtil.staticParamsToArgs(_staticParams)));
          }
        });
    }

    /**
     * Copy another Constructor, performing a substitution with the visitor.
     */
    public Constructor(Constructor that, NodeUpdateVisitor visitor) {
        _declaringTrait = that._declaringTrait;
        _staticParams = visitor.recurOnListOfStaticParam(that._staticParams);
        _params = visitor.recurOnOptionOfListOfParam(that._params);
        _throwsClause = visitor.recurOnOptionOfListOfBaseType(that._throwsClause);
        _where = visitor.recurOnOptionOfWhereClause(that._where);

        _thunk = that._thunk;
        _thunkVisitors = that._thunkVisitors;
        pushVisitor(visitor);
    }

    @Override
    public Span getSpan() { return NodeUtil.getSpan(_declaringTrait); }

    public Id declaringTrait() { return _declaringTrait; }
    
    protected String mandatoryToString() {
        return "constructor " + declaringTrait().toString();
    }
    
    @Override
    protected IdOrOpOrAnonymousName mandatoryToUndecoratedName() {
        // This choice is not tested yet, it could well be the wrong one.
        return declaringTrait();
    }


    
//    public List<StaticParam> staticParams() { return _staticParams; }
//    public Option<List<Param>> params() { return _params; }
//    public Option<List<BaseType>> throwsClause() { return _throwsClause; }
    public Option<WhereClause> where() { return _where; }

	@Override
	public Option<Expr> body() {
		return Option.none();
	}

	@Override
	public List<Param> parameters() {
		if( _params.isNone() )
			return Collections.emptyList();
		else
			return Collections.unmodifiableList(_params.unwrap());
	}

	@Override
	public List<StaticParam> staticParameters() {
		return Collections.unmodifiableList(_staticParams);
	}

	@Override
	public List<BaseType> thrownTypes() {
		if( _throwsClause.isNone() )
			return Collections.emptyList();
		else
			return Collections.unmodifiableList(_throwsClause.unwrap());
	}

	@Override
	public Functional acceptNodeUpdateVisitor(final NodeUpdateVisitor v) {
        return new Constructor(this, v);
	}


}
