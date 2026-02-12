package org.authzen;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import java.util.Map;

public class ConditionEvaluator {
    private static final JexlEngine JEXL = new JexlBuilder()
            .safe(false)
            .silent(false)
            .strict(false)
            .create();

    public boolean evaluate(String expression, Map<String, Object> context) {
        if (expression == null || expression.isEmpty()) {
            return true;
        }
        try {
            JexlContext jexlContext = new MapContext(context);
            Object result = JEXL.createExpression(expression).evaluate(jexlContext);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}
