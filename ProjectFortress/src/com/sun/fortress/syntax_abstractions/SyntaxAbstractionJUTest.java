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

package com.sun.fortress.syntax_abstractions;

import java.util.Arrays;
import java.util.List;

import com.sun.fortress.compiler.StaticTest;

public class SyntaxAbstractionJUTest extends StaticTest {

	public SyntaxAbstractionJUTest() {
		staticTests += "syntax_abstraction/";
	}

	private final List<String> NOT_PASSING = Arrays.asList(
			staticTests + "SyntaxHelloWorldUse.fss",
			staticTests + "SyntaxHelloWorld.fss",
			staticTests + "SyntaxGrammarImportsUse.fss",
			staticTests + "XXXSyntaxGrammarImportsUse.fss",
			staticTests + "SyntaxGrammarImports.fss",
			staticTests + "SyntaxGrammarImportsA.fss",
			staticTests + "SyntaxProductionExtends.fsi",
			staticTests + "XXXSyntaxMultipleGrammarsWithSameName.fsi",
			staticTests + "XXXSyntaxMultipleNonterminalDefsWithSameName.fsi",
			staticTests + "XXXSyntaxGrammarExtendsNonExistingGrammar.fsi"
			// really not working:
			// staticTests + "XXXSyntaxNoFortressAstImport.fsi",
	);

	@Override
	public List<String> getNotPassing() {
		return NOT_PASSING;
	}}