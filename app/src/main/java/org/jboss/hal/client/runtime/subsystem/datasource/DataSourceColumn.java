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
package org.jboss.hal.client.runtime.subsystem.datasource;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.*;
import static org.jboss.hal.client.runtime.subsystem.datasource.DataSourcePresenter.XA_PARAM;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DATA_SOURCE_RUNTIME)
@Requires({DATA_SOURCE_ADDRESS, XA_DATA_SOURCE_ADDRESS})
public class DataSourceColumn extends FinderColumn<DataSource> {

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Environment environment;
    private final Resources resources;
    private final Finder finder;
    private Server server;

    @Inject
    public DataSourceColumn(final ServerActions serverActions,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final StatementContext statementContext,
            final Environment environment,
            final Resources resources,
            final Finder finder,
            final ItemActionFactory itemActionFactory,
            final Places places) {

        super(new Builder<DataSource>(finder, Ids.DATA_SOURCE_RUNTIME, Names.DATASOURCE)
                .withFilter()
                .useFirstActionAsBreadcrumbHandler());

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.environment = environment;
        this.resources = resources;
        this.finder = finder;

        ItemsProvider<DataSource> itemsProvider = (context, callback) -> {
            List<Operation> operations = new ArrayList<>();
            ResourceAddress dataSourceAddress = DATA_SOURCE_SUBSYSTEM_TEMPLATE.resolve(statementContext);
            operations.add(new Operation.Builder(dataSourceAddress, READ_CHILDREN_RESOURCES_OPERATION
            )
                    .param(CHILD_TYPE, DATA_SOURCE)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
            operations.add(new Operation.Builder(dataSourceAddress, READ_CHILDREN_RESOURCES_OPERATION
            )
                    .param(CHILD_TYPE, XA_DATA_SOURCE)
                    .param(INCLUDE_RUNTIME, true)
                    .param(RECURSIVE, true)
                    .build());
            if (!environment.isStandalone()) {
                ResourceAddress serverAddress = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                        .resolve(statementContext);
                operations.add(new Operation.Builder(serverAddress, READ_RESOURCE_OPERATION)
                        .param(INCLUDE_RUNTIME, true)
                        .param(ATTRIBUTES_ONLY, true)
                        .build());
            }
            dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                List<DataSource> combined = new ArrayList<>();
                combined.addAll(result.step(0).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, false)).collect(toList()));
                combined.addAll(result.step(1).get(RESULT).asPropertyList().stream()
                        .map(property -> new DataSource(property, true)).collect(toList()));
                combined.sort(comparing(NamedNode::getName));
                server = environment.isStandalone()
                        ? Server.STANDALONE
                        : new Server(statementContext.selectedHost(), result.step(2).get(RESULT));
                callback.onSuccess(combined);
            });
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider((context, callback) ->
                itemsProvider.get(context, new AsyncCallback<List<DataSource>>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(final List<DataSource> result) {
                        // only datasources w/ enabled statistics will show up in the breadcrumb dropdown
                        List<DataSource> dataSourceWithStatistics = result.stream()
                                .filter(DataSource::isStatisticsEnabled)
                                .collect(toList());
                        callback.onSuccess(dataSourceWithStatistics);
                    }
                }));

        setItemRenderer(dataSource -> new ItemDisplay<DataSource>() {
            @Override
            public String getId() {
                return Ids.dataSourceRuntime(dataSource.getName(), dataSource.isXa());
            }

            @Override
            public Element asElement() {
                return dataSource.isXa() ? ItemDisplay.withSubtitle(dataSource.getName(), Names.XA_DATASOURCE) : null;
            }

            @Override
            public String getTitle() {
                return dataSource.getName();
            }

            @Override
            public Element getIcon() {
                if (!dataSource.isStatisticsEnabled()) {
                    return Icons.unknown();
                } else if (!dataSource.isEnabled()) {
                    return Icons.disabled();
                } else {
                    return Icons.ok();
                }
            }

            @Override
            public String getTooltip() {
                if (!dataSource.isStatisticsEnabled()) {
                    return resources.constants().statisticsDisabled();
                } else {
                    return dataSource.isEnabled() ? resources.constants().enabled() : resources.constants().disabled();
                }
            }

            @Override
            public String getFilterData() {
                //noinspection HardCodedStringLiteral
                return getTitle() + " " +
                        (dataSource.isXa() ? "xa" : "normal") + " " +
                        (dataSource.isEnabled() ? ENABLED : DISABLED);
            }

            @Override
            @SuppressWarnings("HardCodedStringLiteral")
            public List<ItemAction<DataSource>> actions() {
                List<ItemAction<DataSource>> actions = new ArrayList<>();
                if (dataSource.isStatisticsEnabled()) {
                    PlaceRequest placeRequest = places.selectedServer(NameTokens.DATA_SOURCE_RUNTIME)
                            .with(NAME, dataSource.getName())
                            .with(XA_PARAM, String.valueOf(dataSource.isXa()))
                            .build();
                    actions.add(itemActionFactory.view(placeRequest));
                }
                if (dataSource.isEnabled()) {
                    actions.add(new ItemAction.Builder<DataSource>().title(resources.constants().test())
                            .handler(item -> testConnection(item))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    TEST_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushGracefully())
                            .handler(item -> flush(item, FLUSH_GRACEFULLY_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_GRACEFULLY_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushIdle())
                            .handler(item -> flush(item, FLUSH_IDLE_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_IDLE_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushInvalid())
                            .handler(item -> flush(item, FLUSH_INVALID_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_INVALID_CONNECTION_IN_POOL))
                            .build());
                    actions.add(new ItemAction.Builder<DataSource>()
                            .title(resources.constants().flushAll())
                            .handler(item -> flush(item, FLUSH_ALL_CONNECTION_IN_POOL))
                            .constraint(Constraint.executable(
                                    dataSource.isXa() ? XA_DATA_SOURCE_TEMPLATE : DATA_SOURCE_TEMPLATE,
                                    FLUSH_ALL_CONNECTION_IN_POOL))
                            .build());
                }
                return actions;
            }
        });

        setPreviewCallback(item -> new DataSourcePreview(this, server, item, environment, dispatcher, statementContext,
                serverActions, resources));
    }

    private void testConnection(DataSource dataSource) {
        Operation operation = new Operation.Builder(dataSourceAddress(dataSource), TEST_CONNECTION_IN_POOL).build();
        dispatcher.execute(operation,
                result -> MessageEvent.fire(eventBus,
                        Message.success(resources.messages().testConnectionSuccess(dataSource.getName()))),
                (o1, failure) -> MessageEvent.fire(eventBus,
                        Message.error(resources.messages().testConnectionError(dataSource.getName()),
                                failure)),
                (o2, exception) -> MessageEvent.fire(eventBus,
                        Message.error(resources.messages().testConnectionError(dataSource.getName()),
                                exception.getMessage())));
    }

    private void flush(DataSource dataSource, String flushMode) {
        Operation operation = new Operation.Builder(dataSourceAddress(dataSource), flushMode).build();
        dispatcher.execute(operation,
                result -> {
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus, Message.success(resources.messages().flushConnectionSuccess()));
                });
    }

    ResourceAddress dataSourceAddress(DataSource dataSource) {
        return dataSource.isXa()
                ? XA_DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName())
                : DATA_SOURCE_TEMPLATE.resolve(statementContext, dataSource.getName());
    }

    AddressTemplate dataSourceConfigurationTemplate(DataSource dataSource) {
        String resourceName = dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE;
        if (environment.isStandalone()) {
            return AddressTemplate.of("/subsystem=datasources/" + resourceName + "=*");
        } else {
            return AddressTemplate.of("/profile=*/subsystem=datasources/" + resourceName + "=*");
        }
    }

    private ResourceAddress dataSourceConfigurationAddress(DataSource dataSource) {
        String resourceName = dataSource.isXa() ? XA_DATA_SOURCE : DATA_SOURCE;
        if (environment.isStandalone()) {
            return AddressTemplate.of("/subsystem=datasources/" + resourceName + "=*")
                    .resolve(statementContext, dataSource.getName());
        } else {
            String profile = server.get(PROFILE_NAME).asString();
            return AddressTemplate.of("/profile=*/subsystem=datasources/" + resourceName + "=*")
                    .resolve(statementContext, profile, dataSource.getName());
        }
    }

    void enableDataSource(DataSource dataSource) {
        ResourceAddress address = dataSourceConfigurationAddress(dataSource);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().dataSourceEnabled(dataSource.getName())));
            finder.refresh();
        });
    }

    void enableStatistics(DataSource dataSource) {
        ResourceAddress address = dataSourceConfigurationAddress(dataSource);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().statisticsEnabled(dataSource.getName())));
            finder.refresh();
        });
    }
}