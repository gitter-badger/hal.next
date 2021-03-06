/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
public class LoggingPresenter
        extends MbuiPresenter<LoggingPresenter.MyView, LoggingPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.LOGGING_CONFIGURATION)
    @Requires({LOGGING_SUBSYSTEM_ADDRESS, ROOT_LOGGER_ADDRESS, LOGGER_ADDRESS,
            ASYNC_HANDLER_ADDRESS, CONSOLE_HANDLER_ADDRESS, CUSTOM_HANDLER_ADDRESS, FILE_HANDLER_ADDRESS,
            PERIODIC_ROTATING_FILE_HANDLER_ADDRESS, PERIODIC_SIZE_ROTATING_FILE_HANDLER_ADDRESS,
            SIZE_ROTATING_FILE_HANDLER_ADDRESS, SYSLOG_HANDLER_ADDRESS,
            CUSTOM_FORMATTER_ADDRESS, PATTERN_FORMATTER_ADDRESS})
    public interface MyProxy extends ProxyPlace<LoggingPresenter> {}

    public interface MyView extends MbuiView<LoggingPresenter> {
        void updateLoggingConfig(ModelNode modelNode);

        void updateRootLogger(ModelNode modelNode);
        void noRootLogger();
        void updateLogger(List<NamedNode> items);

        void updateAsyncHandler(List<NamedNode> items);
        void updateConsoleHandler(List<NamedNode> items);
        void updateCustomHandler(List<NamedNode> items);
        void updateFileHandler(List<NamedNode> items);
        void updatePeriodicHandler(List<NamedNode> items);
        void updatePeriodicSizeHandler(List<NamedNode> items);
        void updateSizeHandlerHandler(List<NamedNode> items);
        void updateSyslogHandler(List<NamedNode> items);

        void updateCustomFormatter(List<NamedNode> items);
        void updatePatternFormatter(List<NamedNode> items);
    }
    // @formatter:on


    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;

    @Inject
    public LoggingPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return LOGGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(ModelDescriptionConstants.LOGGING)
                .append(Ids.LOGGING, Ids.LOGGING_CONFIGURATION, Names.LOGGING, Names.CONFIGURATION);
    }

    @Override
    protected void reload() {
        crud.read(LOGGING_SUBSYSTEM_TEMPLATE, 2, result -> {
            // @formatter:off
            getView().updateLoggingConfig(result);

            if (result.hasDefined(ROOT_LOGGER_TEMPLATE.lastName())) {
                getView().updateRootLogger(result.get(ROOT_LOGGER_TEMPLATE.lastName()).get(ROOT_LOGGER_TEMPLATE.lastValue()));
            } else {
                getView().noRootLogger();
            }
            getView().updateLogger(asNamedNodes(failSafePropertyList(result, LOGGER_TEMPLATE.lastName())));

            getView().updateAsyncHandler(asNamedNodes(failSafePropertyList(result, ASYNC_HANDLER_TEMPLATE.lastName())));
            getView().updateConsoleHandler(asNamedNodes(failSafePropertyList(result, CONSOLE_HANDLER_TEMPLATE.lastName())));
            getView().updateCustomHandler(asNamedNodes(failSafePropertyList(result, CUSTOM_HANDLER_TEMPLATE.lastName())));
            getView().updateFileHandler(asNamedNodes(failSafePropertyList(result, FILE_HANDLER_TEMPLATE.lastName())));
            getView().updatePeriodicHandler(asNamedNodes(failSafePropertyList(result, PERIODIC_ROTATING_FILE_HANDLER_TEMPLATE.lastName())));
            getView().updatePeriodicSizeHandler(asNamedNodes(failSafePropertyList(result, PERIODIC_SIZE_ROTATING_FILE_HANDLER_TEMPLATE.lastName())));
            getView().updateSizeHandlerHandler(asNamedNodes(failSafePropertyList(result, SIZE_ROTATING_FILE_HANDLER_TEMPLATE.lastName())));
            getView().updateSyslogHandler(asNamedNodes(failSafePropertyList(result, SYSLOG_HANDLER_TEMPLATE.lastName())));

            getView().updateCustomFormatter(asNamedNodes(failSafePropertyList(result, CUSTOM_FORMATTER_TEMPLATE.lastName())));
            getView().updatePatternFormatter(asNamedNodes(failSafePropertyList(result, PATTERN_FORMATTER_TEMPLATE.lastName())));
            // @formatter:on
        });
    }
}
