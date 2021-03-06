package io.cucumber.core.runner;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.List;

class CoreHookDefinition {

    static CoreHookDefinition create(HookDefinition hookDefinition) {
        // Ideally we would avoid this by keeping the scenario scoped
        // glue in a different bucket from the globally scoped glue.
        if (hookDefinition instanceof ScenarioScoped) {
            return new ScenarioScopedCoreHookDefinition(hookDefinition);
        }
        return new CoreHookDefinition(hookDefinition);
    }

    private final HookDefinition delegate;
    private final Expression tagExpression;

    private CoreHookDefinition(HookDefinition delegate) {
        this.delegate = delegate;
        this.tagExpression = new TagExpressionParser().parse(delegate.getTagExpression());
    }

    void execute(Scenario scenario) throws Throwable {
        delegate.execute(scenario);
    }

    HookDefinition getDelegate() {
        return delegate;
    }

    String getLocation(boolean detail) {
        return delegate.getLocation(detail);
    }

    int getOrder() {
        return delegate.getOrder();
    }

    boolean matches(List<String> tags) {
        return tagExpression.evaluate(tags);
    }

    static class ScenarioScopedCoreHookDefinition extends CoreHookDefinition implements ScenarioScoped {
        private final ScenarioScoped delegate;

        private ScenarioScopedCoreHookDefinition(HookDefinition delegate) {
            super(delegate);
            this.delegate = (ScenarioScoped) delegate;
        }

        @Override
        public void disposeScenarioScope() {
            delegate.disposeScenarioScope();
        }
    }
}
