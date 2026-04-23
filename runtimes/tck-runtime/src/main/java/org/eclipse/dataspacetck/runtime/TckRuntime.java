/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.runtime;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.core.spi.system.SystemLauncher;
import org.eclipse.dataspacetck.core.system.ConsoleMonitor;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.store.Namespace;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_LAUNCHER;
import static org.eclipse.dataspacetck.core.system.ConfigFunctions.propertyOrEnv;
import static org.eclipse.dataspacetck.core.system.ConsoleMonitor.ANSI_PROPERTY;
import static org.eclipse.dataspacetck.core.system.ConsoleMonitor.DEBUG_PROPERTY;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Bootstraps the JUnit platform using the Jupiter engine and executes configured TCK tests.
 */
public class TckRuntime {
    private static final String TEST_POSTFIX = ".*Test";
    private final List<String> packages = new ArrayList<>();
    private final Map<String, String> properties = new HashMap<>();
    private Monitor monitor;
    private Class<? extends SystemLauncher> launcher;
    private Predicate<String> displayNameMatching;

    private TckRuntime() {
    }

    public TestExecutionSummary execute() {
        if (launcher != null) {
            properties.put(TCK_LAUNCHER, launcher.getName());
        }

        properties.forEach(System::setProperty);

        var summaryListener = new SummaryGeneratingListener();

        var requestBuilder = LauncherDiscoveryRequestBuilder.request()
                .filters(includeClassNamePatterns(TEST_POSTFIX))
                .selectors(packages.stream().map(DiscoverySelectors::selectPackage).toList());


        if (displayNameMatching != null) {
            requestBuilder.filters((PostDiscoveryFilter) descriptor -> displayNameMatching.test(descriptor.getDisplayName())
                    ? FilterResult.included("Matches display name")
                    : FilterResult.excluded("Does not match display name"));
        }

        var request = requestBuilder.build();
        LauncherConfig launcherConfig = LauncherConfig.builder()
                .addLauncherSessionListeners(new StoreMonitorSessionListener(monitor))
                .build();

        var launcher = LauncherFactory.create(launcherConfig);
        launcher.registerTestExecutionListeners(new TckExecutionListener(monitor));
        launcher.registerTestExecutionListeners(summaryListener);
        launcher.discover(request);
        launcher.execute(request);
        properties.forEach((k, v) -> System.clearProperty(k));
        return summaryListener.getSummary();
    }

    public static class Builder {
        private final TckRuntime runtime;

        private Builder() {
            runtime = new TckRuntime();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder property(String key, String value) {
            runtime.properties.put(key, value);
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            runtime.properties.putAll(properties);
            return this;
        }

        public Builder addPackage(String pkg) {
            runtime.packages.add(pkg);
            return this;
        }

        public Builder monitor(Monitor monitor) {
            runtime.monitor = monitor;
            return this;
        }

        public Builder launcher(Class<? extends SystemLauncher> launcher) {
            runtime.launcher = launcher;
            return this;
        }

        /**
         * Permit to filter tests by the content of @DisplayName annotation. If the predicate matches the test is included,
         * excluded otherwise.
         *
         * @param displayNameMatching the predicate.
         * @return the builder.
         */
        public Builder displayNameMatching(Predicate<String> displayNameMatching) {
            runtime.displayNameMatching = displayNameMatching;
            return this;
        }

        public TckRuntime build() {
            if (runtime.monitor == null) {
                runtime.monitor = new ConsoleMonitor(
                        parseBoolean(runtime.properties.getOrDefault(DEBUG_PROPERTY, propertyOrEnv(DEBUG_PROPERTY, "false"))),
                        parseBoolean(runtime.properties.getOrDefault(ANSI_PROPERTY, propertyOrEnv(ANSI_PROPERTY, "true")))
                );
            }

            return runtime;
        }

    }

    private static class StoreMonitorSessionListener implements LauncherSessionListener {

        private final Monitor monitor;

        StoreMonitorSessionListener(Monitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void launcherSessionOpened(@NotNull LauncherSession session) {
            session.getStore().put(Namespace.GLOBAL, Monitor.class.getName(), monitor);
        }
    }
}
