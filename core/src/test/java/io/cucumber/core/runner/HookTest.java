package io.cucumber.core.runner;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.time.Clock;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HookTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());
    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final CucumberFeature feature = TestFeatureParser.parse("" +
        "Feature: Test feature\n" +
        "  Scenario: Test scenario\n" +
        "     Given I have 4 cukes in my belly\n"
    );
    private final CucumberPickle pickle = feature.getPickles().get(0);


    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        Backend backend = mock(Backend.class);
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        final HookDefinition hook = mock(HookDefinition.class);
        TypeRegistryConfigurer typeRegistryConfigurer = mock(TypeRegistryConfigurer.class);
        when(hook.getTagExpression()).thenReturn("");

        doAnswer(invocation -> {
            Glue glue = invocation.getArgument(0);
            glue.addBeforeHook(hook);
            return null;
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.anyList());

        Runner runner = new Runner(bus, Collections.singleton(backend), objectFactory, typeRegistryConfigurer, runtimeOptions);

        runner.runPickle(pickle);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(hook).execute(ArgumentMatchers.any());
        inOrder.verify(backend).disposeWorld();
    }

}
