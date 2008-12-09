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

api TypeProxy

(** DEPRACATED!  To do covariant type matching, match against a
    function with an explicit return type:
       typecase fn (): T => throw NotFound of
           () -> Number => ... (* case for T extends Number *)
           else => ...
       end

    For contravariant matching, use a function with the appropriate
    parameter type instead.  For equality matching, use the identity
    function at T.

 *)

(** Reflection of static type parameters for overloading purposes.
    Works around shortcomings in the story on parametric overloading
    (all overloadings must have the same parameters with the same
    bounds).  Allows us to overload a function based on the parametric
    type of an output. *)

object __Proxy[\T extends (* U *) Any\]
    (* extends { __Proxy[\U\], Object } where { U extends Any } *)
end

end