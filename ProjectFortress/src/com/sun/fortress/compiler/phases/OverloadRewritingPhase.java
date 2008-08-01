package com.sun.fortress.compiler.phases;

import com.sun.fortress.compiler.AnalyzeResult;
import com.sun.fortress.compiler.OverloadRewriter;
import com.sun.fortress.exceptions.MultipleStaticError;
import com.sun.fortress.exceptions.StaticError;
import com.sun.fortress.useful.Debug;

import edu.rice.cs.plt.iter.IterUtil;

public class OverloadRewritingPhase extends Phase {

    public OverloadRewritingPhase(Phase parentPhase) {
        super(parentPhase);
    }

    @Override
    public AnalyzeResult execute() throws StaticError {
        Debug.debug(Debug.Type.FORTRESS, 1, "Start phase Overload Rewriting");
        AnalyzeResult previous = parentPhase.getResult();

        OverloadRewriter.ComponentResult results = 
            OverloadRewriter.rewriteComponents(previous.components(), env);

        if (!results.isSuccessful()) {
            throw new MultipleStaticError(results.errors());
        }

        return new AnalyzeResult(previous.apis(), results.components(),
                IterUtil.<StaticError> empty(), previous.typeEnvAtNode());
    }

}
