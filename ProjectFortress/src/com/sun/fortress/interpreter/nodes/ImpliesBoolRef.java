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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.sun.fortress.interpreter.nodes_util.*;
import com.sun.fortress.interpreter.useful.*;

public class ImpliesBoolRef extends BoolRef {
  private final StaticArg _left;
  private final StaticArg _right;

  /**
   * Constructs a ImpliesBoolRef.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ImpliesBoolRef(Span in_span, StaticArg in_left, StaticArg in_right) {
    super(in_span);

    if (in_left == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'left' to the ImpliesBoolRef constructor was null");
    }
    _left = in_left;

    if (in_right == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'right' to the ImpliesBoolRef constructor was null");
    }
    _right = in_right;
  }

    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return v.forImpliesBoolRef(this);
    }

    ImpliesBoolRef(Span span) {
        super(span);
        _left = null;
        _right = null;
    }

  final public StaticArg getLeft() { return _left; }
  final public StaticArg getRight() { return _right; }

  public <RetType> RetType visit(NodeVisitor<RetType> visitor) { return visitor.forImpliesBoolRef(this); }
  public void visit(NodeVisitor_void visitor) { visitor.forImpliesBoolRef(this); }

  /**
   * Implementation of toString that uses
   * {@link #output} to generate a nicely tabbed tree.
   */
  public java.lang.String toString() {
    java.io.StringWriter w = new java.io.StringWriter();
    output(w);
    return w.toString();
  }

  /**
   * Prints this object out as a nicely tabbed tree.
   */
  public void output(java.io.Writer writer) {
    outputHelp(new TabPrintWriter(writer, 2), false);
  }

  protected void outputHelp(TabPrintWriter writer, boolean lossless) {
    writer.print("ImpliesBoolRef:");
    writer.indent();

    Span temp_span = getSpan();
    writer.startLine();
    writer.print("span = ");
    if (lossless) {
      writer.printSerialized(temp_span);
      writer.print(" ");
      writer.printEscaped(temp_span);
    } else { writer.print(temp_span); }

    StaticArg temp_left = getLeft();
    writer.startLine();
    writer.print("left = ");
    temp_left.outputHelp(writer, lossless);

    StaticArg temp_right = getRight();
    writer.startLine();
    writer.print("right = ");
    temp_right.outputHelp(writer, lossless);
    writer.unindent();
  }

  /**
   * Implementation of equals that is based on the values of the fields of the
   * object. Thus, two objects created with identical parameters will be equal.
   */
  public boolean equals(java.lang.Object obj) {
    if (obj == null) return false;
    if ((obj.getClass() != this.getClass()) || (obj.hashCode() != this.hashCode())) {
      return false;
    } else {
      ImpliesBoolRef casted = (ImpliesBoolRef) obj;
      StaticArg temp_left = getLeft();
      StaticArg casted_left = casted.getLeft();
      if (!(temp_left == casted_left || temp_left.equals(casted_left))) return false;
      StaticArg temp_right = getRight();
      StaticArg casted_right = casted.getRight();
      if (!(temp_right == casted_right || temp_right.equals(casted_right))) return false;
      return true;
    }
  }

  /**
   * Implementation of hashCode that is consistent with equals.  The value of
   * the hashCode is formed by XORing the hashcode of the class object with
   * the hashcodes of all the fields of the object.
   */
  protected int generateHashCode() {
    int code = getClass().hashCode();
    StaticArg temp_left = getLeft();
    code ^= temp_left.hashCode();
    StaticArg temp_right = getRight();
    code ^= temp_right.hashCode();
    return code;
  }
}
