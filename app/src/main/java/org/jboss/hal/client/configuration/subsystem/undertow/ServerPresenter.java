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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.FilteringStatementContext.Filter;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.encodeValue;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_KEY;

/**
 * @author Harald Pehl
 */
public class ServerPresenter
        extends ApplicationFinderPresenter<ServerPresenter.MyView, ServerPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(SERVER_ADDRESS)
    @NameToken(NameTokens.UNDERTOW_SERVER)
    public interface MyProxy extends ProxyPlace<ServerPresenter> {}

    public interface MyView extends HalView, HasPresenter<ServerPresenter> {
        void update(ModelNode payload);
        void updateFilterRef(List<NamedNode> filters);
        void updateLocation(List<NamedNode> locations);
        void updateLocationFilterRef(List<NamedNode> filters);
    }
    // @formatter:on

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String serverName;
    private String hostName;
    private String locationName;

    @Inject
    public ServerPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final MetadataRegistry metadataRegistry,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new FilteringStatementContext(statementContext,
                new Filter() {
                    @Override
                    public String filter(final String placeholder) {
                        if (SELECTION_KEY.equals(placeholder)) {
                            return serverName;
                        } else if (HOST.equals(placeholder)) {
                            return hostName;
                        }
                        return null;
                    }

                    @Override
                    public String[] filterTuple(final String placeholder) {
                        return null;
                    }
                });
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        serverName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SERVER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.SERVER),
                        resources.constants().settings(), Names.SERVER)
                .append(Ids.UNDERTOW_SERVER, Ids.undertowServer(serverName), Names.SERVER, serverName);
    }

    @Override
    protected void reload() {
        reload(result -> getView().update(result));
    }

    private void reload(Consumer<ModelNode> payload) {
        crud.readRecursive(SELECTED_SERVER_TEMPLATE.resolve(statementContext), payload::accept);
    }

    void saveServer(final Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        crud.save(Names.SERVER, serverName, SELECTED_SERVER_TEMPLATE.resolve(statementContext), changedValues,
                metadata, this::reload);
    }

    void resetServer(final Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
        crud.reset(Names.SERVER, serverName, SELECTED_SERVER_TEMPLATE.resolve(statementContext), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ host

    void addHost() {
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.UNDERTOW_HOST_ADD,
                resources.messages().addResourceTitle(Names.HOST), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name)
                            .resolve(statementContext);
                    crud.add(Names.HOST, name, address, model, (n, a) -> reload());
                });
        dialog.show();

    }

    void saveHost(final String name, final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        crud.save(Names.HOST, name, address, changedValues, metadata, this::reload);
    }

    void resetHost(final String name, final Form<NamedNode> form) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        crud.reset(Names.HOST, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeHost(final String name) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(HOST + "=" + name).resolve(statementContext);
        crud.remove(Names.HOST, name, address, this::reload);
    }

    void selectHost(String hostName) {
        this.hostName = hostName;
    }

    String hostSegment() {
        return hostName != null ? Names.HOST + ": " + hostName : Names.NOT_AVAILABLE;
    }

    Operation hostSettingOperation(final HostSetting hostSetting) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        return new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
    }

    void addHostSetting(final HostSetting hostSetting) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        crud.addSingleton(hostSetting.type, address, null, a -> reload());
    }

    void saveHostSetting(final HostSetting hostSetting, final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        crud.saveSingleton(hostSetting.type, address, changedValues, metadata, this::reload);
    }

    void resetHostSetting(final HostSetting hostSetting, final Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        crud.resetSingleton(hostSetting.type, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(final Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeHostSetting(final HostSetting hostSetting, final Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        crud.removeSingleton(hostSetting.type, address, new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(final Form<ModelNode> form) {
                reload();
            }
        });
    }


    // ------------------------------------------------------ host filter-ref

    void showFilterRef(final NamedNode host) {
        selectHost(host.getName());
        getView().updateFilterRef(asNamedNodes(failSafePropertyList(host, FILTER_REF)));
    }

    void addFilterRef() {
        Metadata metadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_HOST_FILTER_REF_ADD, metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();
        form.getFormItem(NAME)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, FILTER_SUGGESTIONS));
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.FILTER), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + "=" + name)
                            .resolve(statementContext);
                    crud.add(Names.FILTER, name, address, model, (n, a) -> reloadFilterRef());
                });
        dialog.show();
    }

    void saveFilterRef(final Form<NamedNode> form, final Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + "=" + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        crud.save(Names.FILTER, name, address, changedValues, metadata, this::reloadFilterRef);
    }

    void resetFilterRef(final Form<NamedNode> form) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + "=" + name).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(FILTER_REF_TEMPLATE);
        crud.reset(Names.FILTER, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reloadFilterRef();
            }
        });
    }

    void removeFilterRef(final String name) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(FILTER_REF + "=" + name).resolve(statementContext);
        crud.remove(Names.FILTER, name, address, this::reloadFilterRef);
    }

    private void reloadFilterRef() {
        reload(modelNode -> {
            getView().update(modelNode);
            getView().updateFilterRef(
                    asNamedNodes(failSafePropertyList(modelNode, String.join("/", HOST, hostName, FILTER_REF))));
        });
    }

    // ------------------------------------------------------ host location

    void showLocation(final NamedNode host) {
        selectHost(host.getName());
        getView().updateLocation(asNamedNodes(failSafePropertyList(host, LOCATION)));
    }

    void addLocation() {
        Metadata metadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_HOST_LOCATION_ADD, metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();
        form.getFormItem(HANDLER)
                .registerSuggestHandler(
                        new ReadChildrenAutoComplete(dispatcher, statementContext, HANDLER_SUGGESTIONS));
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.LOCATION), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HOST_TEMPLATE
                            .append(LOCATION + "=" + encodeValue(name))
                            .resolve(statementContext);
                    crud.add(Names.LOCATION, name, address, model, (n, a) -> reloadLocation());
                });
        dialog.show();
    }

    void saveLocation(final Form<NamedNode> form, final Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + "=" + encodeValue(name))
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        crud.save(Names.LOCATION, name, address, changedValues, metadata, this::reloadLocation);
    }

    void resetLocation(final Form<NamedNode> form) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + "=" + encodeValue(name))
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_TEMPLATE);
        crud.reset(Names.LOCATION, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reloadLocation();
            }
        });
    }

    void removeLocation(final String name) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + "=" + encodeValue(name))
                .resolve(statementContext);
        crud.remove(Names.FILTER, name, address, this::reloadLocation);
    }

    private void reloadLocation() {
        reload(modelNode -> {
            getView().update(modelNode);
            getView().updateLocation(asNamedNodes(failSafePropertyList(modelNode,
                    String.join("/", HOST, hostName, LOCATION))));
        });
    }

    private void selectLocation(String locationName) {
        this.locationName = locationName;
    }

    String locationSegment() {
        return locationName != null ? Names.LOCATION + ": " + locationName : Names.NOT_AVAILABLE;
    }


    // ------------------------------------------------------ host location filter-ref

    void showLocationFilterRef(final NamedNode location) {
        selectLocation(location.getName());
        getView().updateLocationFilterRef(asNamedNodes(failSafePropertyList(location, FILTER_REF)));
    }

    void addLocationFilterRef() {
        Metadata metadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.UNDERTOW_HOST_LOCATION_FILTER_REF_ADD, metadata)
                .unboundFormItem(new NameItem(), 0)
                .fromRequestProperties()
                .build();
        form.getFormItem(NAME)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, FILTER_SUGGESTIONS));
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.FILTER), form,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HOST_TEMPLATE
                            .append(LOCATION + "=" + encodeValue(locationName))
                            .append(FILTER_REF + "=" + name)
                            .resolve(statementContext);
                    crud.add(Names.FILTER, name, address, model, (n, a) -> reloadLocationFilterRef());
                });
        dialog.show();
    }

    void saveLocationFilterRef(final Form<NamedNode> form, final Map<String, Object> changedValues) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + "=" + encodeValue(locationName))
                .append(FILTER_REF + "=" + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        crud.save(Names.FILTER, name, address, changedValues, metadata, this::reloadLocationFilterRef);

    }

    void resetLocationFilterRef(final Form<NamedNode> form) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + "=" + encodeValue(locationName))
                .append(FILTER_REF + "=" + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(LOCATION_FILTER_REF_TEMPLATE);
        crud.reset(Names.FILTER, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reloadLocationFilterRef();
            }
        });

    }

    void removeLocationFilterRef(final String name) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE
                .append(LOCATION + "=" + encodeValue(locationName))
                .append(FILTER_REF + "=" + name)
                .resolve(statementContext);
        crud.remove(Names.FILTER, name, address, this::reloadLocationFilterRef);
    }

    private void reloadLocationFilterRef() {
        reload(modelNode -> {
            getView().update(modelNode);
            getView().updateLocationFilterRef(asNamedNodes(failSafePropertyList(modelNode,
                    String.join("/", HOST, hostName, LOCATION, encodeValue(locationName), FILTER_REF))));
        });
    }

    // ------------------------------------------------------ listener

    void addListener(final Listener listenerType) {
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE.append(listenerType.resource + "=*"));
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(listenerType.baseId, Ids.ADD_SUFFIX),
                resources.messages().addResourceTitle(listenerType.type), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + "=" + name)
                            .resolve(statementContext);
                    crud.add(listenerType.type, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveListener(final Listener listenerType, final String name, final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + "=" + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE.append(listenerType.resource + "=*"));
        crud.save(listenerType.type, name, address, changedValues, metadata, this::reload);
    }

    void resetListener(final Listener listenerType, final String name, final Form<NamedNode> form) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + "=" + name)
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE.append(listenerType.resource + "=*"));
        crud.reset(listenerType.type, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(final Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeListener(final Listener listenerType, final String name) {
        ResourceAddress address = SELECTED_SERVER_TEMPLATE.append(listenerType.resource + "=" + name)
                .resolve(statementContext);
        crud.remove(listenerType.type, name, address, this::reload);
    }

    // ------------------------------------------------------ getter

    StatementContext getStatementContext() {
        return statementContext;
    }
}
