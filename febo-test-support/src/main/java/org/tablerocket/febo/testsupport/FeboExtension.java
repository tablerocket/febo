package org.tablerocket.febo.testsupport;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tablerocket.febo.api.Boot;
import org.tablerocket.febo.api.Febo;
import org.tablerocket.febo.autobundle.AutoBundleSupport;
import org.tablerocket.febo.repository.ClasspathRepositoryStore;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FeboExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static Logger LOG = LoggerFactory.getLogger(FeboExtension.class);

    private Map<String, Class<?>> services = new HashMap<>();
    private Febo febo;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AutoBundleSupport autoBundle = new AutoBundleSupport();
        this.febo = Boot.febo()
                .platform(new ClasspathRepositoryStore()) // the target platform
                .require(autoBundle.scan(getClass().getClassLoader())) // domain bundles
                .keepRunning(true)
        //.require(autoBundle.from(MyTestEntry.class)) // test probe
        ;

        if (context.getTestMethod().isPresent()) {
            Method m = context.getTestMethod().get();
            for (Parameter parameter : m.getParameters()) {
                String name = parameter.getName();
                Class<?> type = parameter.getType();
                services.put(name,type);
                febo.exposePackage(type.getPackage().getName());
            }
        }



        febo.run(new String[]{});
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (febo != null) {
            febo.stop();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Type type = parameterContext.getParameter().getType();
        // check service availability

        LOG.info(" + Need to inject this: " + type.getTypeName() + " for parameter: " + parameterContext.getParameter().getName());
        return true;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            return febo.service(parameterContext.getParameter().getType());
        } catch (Exception e) {
            throw new ParameterResolutionException("Could not aquire service of type " + parameterContext.getParameter().getType().getName());
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        LOG.info("Need to tear febo down here.");
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        LOG.info("Need to start febo here: beforeTestExecution");
    }

}
