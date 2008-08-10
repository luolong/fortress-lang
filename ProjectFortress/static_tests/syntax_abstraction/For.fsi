(*******************************************************************************
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
 ******************************************************************************)

api For
    import FortressAst.{...}
    import FortressSyntax.{...}

    grammar M extends {Expression, Identifier}
        Expr |Expr:=
            for {i:Id <- e:Expr ,? SPACE}* ; do block:Expr ; end =>
            <[ for2 i** ; e** ; do block ; end ]>
            (*
            case i of
                Empty => <[ block ]>
                Cons(ia, ib) =>
                    case e of
                        Cons (ea, eb) => <[ (ea).loop( fn ia => ffor2 ib** ; eb** ; fdo block fend ) ]>
                    end
            end
            *)
        | for2 i:Id* ; e:Expr* ; do block:Expr ; end =>
            case i of
                Empty => <[ block ]>
                Cons(ia, ib) =>
                    case e of
                        Cons (ea, eb) => <[ ( (ea).loop( fn ia => (for2 ib** ; eb** ; do block ; end) ) ) ]>
                    end
            end

        (*
        Do :Expr:=
            fdo => <[ "" ]>

        End :Expr:=
            fend => <[ "" ]>
            *)
    end
end