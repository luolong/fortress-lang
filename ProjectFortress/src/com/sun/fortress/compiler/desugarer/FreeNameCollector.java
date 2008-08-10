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

package com.sun.fortress.compiler.desugarer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.sun.fortress.compiler.index.TraitIndex;
import com.sun.fortress.compiler.index.TypeConsIndex;
import com.sun.fortress.compiler.typechecker.TraitTable;
import com.sun.fortress.compiler.typechecker.TypeEnv;
import com.sun.fortress.compiler.typechecker.TypeEnv.BindingLookup;
import com.sun.fortress.exceptions.DesugarerError;
import com.sun.fortress.nodes.*;
import com.sun.fortress.nodes_util.Span;
import com.sun.fortress.useful.Debug;

import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;

public final class 
FreeNameCollector extends NodeDepthFirstVisitor_void {

	private Map<Span,TypeEnv> typeEnvAtNode;
	// A stack keeping track of all nodes that can create new scope 
	private Stack<Node> scopeStack;
	// A stack keeping track of (potentially nested) object exprs 
	private Stack<ObjectExpr> objExprStack;
	// The Trait table enclosing the object expression
	private TraitTable traitTable;
	// The TraitDecl enclosing the object expression
	private TraitDecl enclosingTraitDecl;
	// The ObjectDecl enclosing the object expression
	private ObjectDecl enclosingObjectDecl;
    private boolean inAssignmentLhs;

    private FreeNameCollection freeNames;
    /* Map key: object expr, value: free names captured by object expr */
    private Map<Span, FreeNameCollection> objExprToFreeNames;
    /* Map key: node which the captured mutable varRef is declared under 
                (which should be either ObjectDecl or LocalVarDecl), 
       value: list of pairs for which the VarRefs that needs to be boxed
              pair.first is the span of the decl node for the varRef
              pair.second is the varRef */
    private Map<Span, List<Pair<Span, VarRef>>> declSiteToVarRefs;

	
	private static final int DEBUG_LEVEL = 2;
	private static final int DEBUG_LEVEL0 = 1;
	
	// This class is used to collect names captured by object expressions.
	// A reference is considered as free with repect to an object expression
	// (call it O), if it is not declared within the body of O, not 
    // inherited by O, AND not declared in top level environment.
	public FreeNameCollector(TraitTable traitTable, 
                             Map<Span,TypeEnv> typeEnvAtNode) {
		this.traitTable = traitTable;
		this.typeEnvAtNode = typeEnvAtNode;
		this.scopeStack = new Stack<Node>();
        this.objExprStack = new Stack<ObjectExpr>();
        this.freeNames = new FreeNameCollection();
        this.objExprToFreeNames = new HashMap<Span, FreeNameCollection>();
        this.declSiteToVarRefs = new HashMap<Span, List<Pair<Span,VarRef>>>();
        
		this.enclosingTraitDecl = null; 
		this.enclosingObjectDecl = null; 
		this.inAssignmentLhs = false;
	}

    public Map<Span, FreeNameCollection> getObjExprToFreeNames() {
        return objExprToFreeNames;
    }

    public Map<Span, List<Pair<Span,VarRef>>> getDeclSiteToVarRefs() {
        return declSiteToVarRefs;
    }

	@Override
    public void forTraitDecl(TraitDecl that) {
		scopeStack.push(that);
		enclosingTraitDecl = that;
        super.forTraitDecl(that);
    	enclosingTraitDecl = null;
		scopeStack.pop();
    }

	@Override
    public void forObjectDecl(ObjectDecl that) {
		scopeStack.push(that);
		enclosingObjectDecl = that;
        super.forObjectDecl(that);
     	enclosingObjectDecl = null;
     	scopeStack.pop();
    }

	@Override 
	public void forFnExpr(FnExpr that) {
		scopeStack.push(that);
		super.forFnExpr(that);
		scopeStack.pop();
	}
	
	@Override
	public void forFnDef(FnDef that) {
		scopeStack.push(that);
        super.forFnDef(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forIfClause(IfClause that) {
		scopeStack.push(that);
        super.forIfClause(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forFor(For that) {
		scopeStack.push(that);
        super.forFor(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forLetFn(LetFn that) {
		scopeStack.push(that);
        super.forLetFn(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forLocalVarDecl(LocalVarDecl that) {
		scopeStack.push(that);
        super.forLocalVarDecl(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forLabel(Label that) {
		scopeStack.push(that);
        super.forLabel(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forCatch(Catch that) {
		scopeStack.push(that);
        super.forCatch(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forTypecase(Typecase that) {
		scopeStack.push(that);
        super.forTypecase(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forGeneratedExpr(GeneratedExpr that) {
		scopeStack.push(that);
        super.forGeneratedExpr(that);
		scopeStack.pop();
	}
	
	@Override 
	public void forWhile(While that) {
		scopeStack.push(that);
        super.forWhile(that);
		scopeStack.pop();
	}
	
	@Override
    public void forObjectExpr(ObjectExpr that) {
        scopeStack.push(that);
        objExprStack.push(that);

        super.forObjectExpr(that);

        objExprStack.pop();
        scopeStack.pop();

        objExprToFreeNames.put( that.getSpan(), freeNames.makeCopy() );
        List<VarRef> mutableVars = freeNames.getFreeMutableVarRefs();    

        // Update the declsToVarRefs list 
        if(mutableVars != null) {
            TypeEnv typeEnv = typeEnvAtNode.get( that.getSpan() );

            for(VarRef var : mutableVars) {
            	Option<Node> declNodeOp = typeEnv.declarationSite(var.getVar());
        		if(declNodeOp.isNone()) {
        		    throw new DesugarerError( var.getSpan(), 
                                "Decl node for " + var + " is null!" );
                }
    
                // the Node that corresponds to the declaration of the VarRef
                Node declNode = declNodeOp.unwrap();
                // the Node that contains the declNode, which determines the 
                // scope of the declNode; 
                // must be either ObjectDecl or LocalVarDecl 
                Node declSite = null;
        
        		Debug.debug( Debug.Type.COMPILER, DEBUG_LEVEL0, 
                             "decl site is: ", declNode.stringName() );
            
                if(declNode instanceof LocalVarDecl) {
                    declSite = declNode;
                } else if( declNode instanceof Param || 
                           declNode instanceof LValueBind ) {
                    if( enclosingObjectDecl == null ) {
    		            throw new DesugarerError( var.getSpan(), 
                            "Unexpected decl node for " + var + "; " + 
                            "Decl node is: " + declNode + 
                            ", and no enclosing object decl found." );
                    } else {
                        declSite = enclosingObjectDecl;
                    }
                } else {
    		        throw new DesugarerError( var.getSpan(), 
                            "Unexpected type for decl node of " + var + 
                            "; Decl node is: " + declNode );
                }

                List<Pair<Span,VarRef>> refs = declSiteToVarRefs.get(declSite);
                if(refs == null) {
                    refs = new LinkedList<Pair<Span,VarRef>>();
                }
                refs.add( new Pair<Span,VarRef>(declNode.getSpan(), var) );
                declSiteToVarRefs.put(declSite.getSpan(), refs); 
    		}
        }

        // only reset freeNames if we are out of outer-most object expr 
        if( objExprStack.isEmpty() ) { 
            freeNames = new FreeNameCollection();
        }
    }

	@Override
	public void forAssignment(Assignment that) {
        forAssignmentDoFirst(that);
        recurOnOptionOfType(that.getExprType());

        inAssignmentLhs = true;
        recurOnListOfLhs(that.getLhs());
        inAssignmentLhs = false;

        recurOnOptionOfOpRef(that.getOpr());
        recur(that.getRhs());

        forAssignmentOnly(that);
	} 

	@Override
	public void forVarRef(VarRef that) {
	    // Not in object expression; we are done.
	    if( objExprStack.isEmpty() ) { 
	        super.forVarRef(that);
	        return;
	    }
	   
        forVarRefDoFirst(that);
        recurOnOptionOfType(that.getExprType());
        recur(that.getVar());

		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL0, "FreeNameCollector visiting ", that);
		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL0, "isAssignmentLhs is ", inAssignmentLhs);
		
		if( isDeclaredInObjExpr(that.getVar()) == false &&  
            isDeclaredInTopLevel(that.getVar()) == false ) {
            freeNames.add(that, inAssignmentLhs);
        }

        forVarRefOnly(that);
	}

	@Override
	public void forFnRef(FnRef that) {
	    // Not in object expression; we are done.
        if( objExprStack.isEmpty() ) { 
            super.forFnRef(that);
            return;
        }
       
        forFnRefDoFirst(that);
        recurOnOptionOfType(that.getExprType());
        recur(that.getOriginalName());
        recurOnListOfId(that.getFns());
        recurOnListOfStaticArg(that.getStaticArgs());

		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "FreeNameCollector visiting ", that);
		if( isDeclaredInObjExpr(that.getOriginalName()) == false && 
            isDeclaredInTopLevel(that.getOriginalName()) == false ) {
		    freeNames.add(that, isDottedMethod(that));
		}

        forFnRefOnly(that);
	}

	@Override
	public void forOpRef(OpRef that) {
	    // Not in object expression; we are done.
        if( objExprStack.isEmpty() ) { 
            super.forOpRef(that);
            return;
        }
	    
        forOpRefDoFirst(that);
        recurOnOptionOfType(that.getExprType());
        recur(that.getOriginalName());
        recurOnListOfOpName(that.getOps());
        recurOnListOfStaticArg(that.getStaticArgs());
        
        OpName op = that.getOriginalName();
        // Not handling 
        if( (op instanceof Op) == false &&
            (op instanceof Enclosing) == false ) {
            throw new DesugarerError("Unexpected Op type for OpRef " + that);
        }
		if( isDeclaredInObjExpr(op) == false && 
		    isDeclaredInTopLevel(op) == false ) {
		    freeNames.add(that);
		}

        forOpRefOnly(that);
	}
	
	@Override
	public void forDimRef(DimRef that) {
	    // Not in object expression; we are done.
        if( objExprStack.isEmpty() ) { 
            super.forDimRef(that);
            return;
        }
       
        forDimRefDoFirst(that);
        recur(that.getName());

		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "FreeNameCollector visiting ", that);
		if( isDeclaredInObjExpr(that.getName()) == false && 
            isDeclaredInTopLevel(that.getName()) == false ) {
			// FIXME: I put this in, but the TypeEnv doesn't actually 
            // contain DimRef
		    freeNames.add(that);
		}

        forDimRefOnly(that);
	} 

	@Override
	public void forIntRef(IntRef that) {
	    // Not in object expression; we are done.
        if( objExprStack.isEmpty() ) { 
            super.forIntRef(that);
            return;
        }
       
        forIntRefDoFirst(that);
        recur(that.getName());

		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "FreeNameCollector visiting ", that);
		if( isDeclaredInObjExpr(that.getName()) == false && 
            isDeclaredInTopLevel(that.getName()) == false ) {
		    freeNames.add(that);
		}

        forIntRefOnly(that);
	}

	@Override
	public void forBoolRef(BoolRef that) {
	    // Not in object expression; we are done.
        if( objExprStack.isEmpty() ) { 
            super.forBoolRef(that);
            return;
        }

        forBoolRefDoFirst(that);
        recur(that.getName());

		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "FreeNameCollector visiting ", that);
		if( isDeclaredInObjExpr(that.getName()) == false &&
            isDeclaredInTopLevel(that.getName()) == false ) {
		    freeNames.add(that);
		}

        forBoolRefOnly(that);
	}

	@Override
	public void forVarType(VarType that) {
	    // Not in object expression; we are done.
        if( objExprStack.isEmpty() ) { 
            super.forVarType(that);
            return;
        }
       
        forVarTypeDoFirst(that);
        recur(that.getName());

		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "FreeNameCollector visiting ", that);
		if( isDeclaredInObjExpr(that.getName()) == false &&
            isDeclaredInTopLevel(that.getName()) == false ) {
		    freeNames.add(that);
		}

        forVarTypeDoFirst(that);
	}

	private boolean isDottedMethod(FnRef fnRef) {
		
		Option<TypeConsIndex> typeConsIndex = null;
		TraitIndex traitIndex = null;
		Span declSpan = null;
		Id declId = null;
		
		if( enclosingTraitDecl == null && enclosingObjectDecl == null ) {
			return false;
		}
		
		if(enclosingTraitDecl != null) {
		    declSpan = enclosingTraitDecl.getSpan();
		    declId = enclosingTraitDecl.getName();
		} else if(enclosingObjectDecl != null) {
		    declSpan = enclosingObjectDecl.getSpan();
	        declId = enclosingObjectDecl.getName();
		}
		typeConsIndex = traitTable.typeCons(declId);

		if(typeConsIndex.isNone()) {
			throw new DesugarerError(declSpan, 
			            "TypeConsIndex for " + declId + " is not found.");
		} else if(typeConsIndex.unwrap() instanceof TraitIndex) {
			traitIndex = (TraitIndex) typeConsIndex.unwrap();
		} else {
			throw new DesugarerError(declSpan, 
					"TypeConsIndex for " + declId + " is not type TraitIndex.");
		}

		return traitIndex.dottedMethods().containsFirst(fnRef.getOriginalName());
	}
	
	private boolean isDeclaredInObjExpr(Node idOrOpOrEnclosing) { 
        // FIXME: change if TypeCheckerResult.getTypeEnv(Span) is done 
        ObjectExpr innerMostObjExpr = objExprStack.peek();
        Span objExprSpan =  innerMostObjExpr.getSpan();
		TypeEnv objExprTypeEnv = typeEnvAtNode.get(objExprSpan);
		
        if(objExprTypeEnv == null) {
            throw new DesugarerError( objExprSpan, 
                "TypeEnv corresponding to Object Expr at source " + 
                objExprSpan + " is not found!" );
        }
		    
        // There is no sense checking for the static param binding,
        // because object expr can never declare static params of its own.
		Option<BindingLookup> binding = Option.<BindingLookup>none();
		if(idOrOpOrEnclosing instanceof Id) {    
		    binding = objExprTypeEnv.binding( (Id)idOrOpOrEnclosing );
		} else if(idOrOpOrEnclosing instanceof Op) {
		    binding = objExprTypeEnv.binding( (Op)idOrOpOrEnclosing );
		} else if(idOrOpOrEnclosing instanceof Enclosing) {
            binding = objExprTypeEnv.binding( (Enclosing)idOrOpOrEnclosing );
		} else {
		    throw new DesugarerError("Querying binding from TypeEnv with" +
		                " type " + idOrOpOrEnclosing.getClass().toString() + 
		                " is not supported.");
		}
		
		// The typeEnv are things visible outside of object expression
		// Since we have already passed type checking, we don't need to worry 
		// about undefined variable.  If the binding is not found, the reference
		// must be declared / inherited within the object expression.
		if( binding.isNone() ) {
			Debug.debug(Debug.Type.COMPILER, DEBUG_LEVEL, 
					idOrOpOrEnclosing, " is declared in ", innerMostObjExpr);
			return true;  
		}

		Debug.debug(Debug.Type.COMPILER, DEBUG_LEVEL, idOrOpOrEnclosing, 
			        " is NOT declared in ", innerMostObjExpr);
		return false;
	}

	private boolean isDeclaredInTopLevel(Node idOrOpOrEnclosing) {
		// The first node in this stack must be declared at a top level
		// Its corresponding environment must be the top level environment
		Node topLevelNode = scopeStack.get(0);  
		
		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "top level node is: ", topLevelNode);
		Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "its span is: ", topLevelNode.getSpan());
		
		// FIXME: change if going back to Pair<Node,Span> key
        // FIXME: change when the type checking ppl are done w/ getTypeEnv
		TypeEnv topLevelEnv = typeEnvAtNode.get(topLevelNode.getSpan());

		if(topLevelEnv == null) {
		    throw new DesugarerError("TypeEnv associated with " 
		            + topLevelNode + " is not found.");
		}
		
	    Option<BindingLookup> binding = Option.<BindingLookup>none();
	    Option<StaticParam> staticParam = Option.<StaticParam>none();
	    
	    if(idOrOpOrEnclosing instanceof Id) {    
	        binding = topLevelEnv.binding( (Id)idOrOpOrEnclosing );
	        staticParam = topLevelEnv.staticParam( (Id)idOrOpOrEnclosing );
	    } else if(idOrOpOrEnclosing instanceof Op) {
	        binding = topLevelEnv.binding( (Op)idOrOpOrEnclosing );
	    } else if(idOrOpOrEnclosing instanceof Enclosing) {
	        binding = topLevelEnv.binding( (Enclosing)idOrOpOrEnclosing );
	    } else {
	        throw new DesugarerError("Querying binding from TypeEnv with" +
	                    " type " + idOrOpOrEnclosing.getClass().toString() + 
	                    " is not supported.");
	    }

		if( binding.isNone() && staticParam.isNone() ) {
			Debug.debug(Debug.Type.COMPILER, DEBUG_LEVEL, 
			        idOrOpOrEnclosing, " is NOT declared in top level env.");
			return false;
		}
		
		Debug.debug(Debug.Type.COMPILER, DEBUG_LEVEL, 
		            idOrOpOrEnclosing, " is declared in top level env.");
		
		return true;
	}
	
    /* Will no longer be needed once the getTypeEnv from 
       TypeCheckerResult is in place 
	private void DebugTypeEnvAtNode(Node nodeToLookFor) {
	    int i = 0;
		Span span = nodeToLookFor.getSpan();
		
	    Debug.debug(Debug.Type.COMPILER, 
                    DEBUG_LEVEL, "Debuggging typeEnvAtNode ... ");
        Debug.debug(Debug.Type.COMPILER, 
                DEBUG_LEVEL, "Looking for node: " + nodeToLookFor);
        
	    for(Pair<Node,Span> n : typeEnvAtNode.keySet()) {
			i++;
			Debug.debug(Debug.Type.COMPILER, 
                        DEBUG_LEVEL, "key ", i, ": ", n.first());
			Debug.debug(Debug.Type.COMPILER, 
                        DEBUG_LEVEL, "\t Its span is ", n.second());
			Debug.debug(Debug.Type.COMPILER,                    
                        DEBUG_LEVEL, "\t Are they equal? ", 
                        n.first().getSpan().equals(n.second()));
			Debug.debug(Debug.Type.COMPILER, 
                        DEBUG_LEVEL, "\t It's env contains: ", 
                        typeEnvAtNode.get(n));
			if(n.second().equals(span)) {
			    Debug.debug(Debug.Type.COMPILER, 
			                DEBUG_LEVEL, "Node in data struct: ", n.first());   
			}
		}
	} */

}

