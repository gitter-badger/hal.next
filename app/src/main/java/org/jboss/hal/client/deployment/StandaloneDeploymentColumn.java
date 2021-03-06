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
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.SpanElement;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.deployment.Deployment.Status;
import org.jboss.hal.client.deployment.DeploymentFunctions.AddUnmanagedDeployment;
import org.jboss.hal.client.deployment.DeploymentFunctions.CheckDeployment;
import org.jboss.hal.client.deployment.DeploymentFunctions.UploadOrReplace;
import org.jboss.hal.client.deployment.dialog.AddUnmanagedDialog;
import org.jboss.hal.client.deployment.wizard.NamesStep;
import org.jboss.hal.client.deployment.wizard.UploadContext;
import org.jboss.hal.client.deployment.wizard.UploadDeploymentStep;
import org.jboss.hal.client.deployment.wizard.UploadState;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.SuccessfulOutcome;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.deployment.StandaloneDeploymentColumn.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.deployment.wizard.UploadState.NAMES;
import static org.jboss.hal.client.deployment.wizard.UploadState.UPLOAD;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Column used in standalone mode to manage deployments.
 *
 * @author Harald Pehl
 */
@Column(Ids.DEPLOYMENT)
@Requires(value = DEPLOYMENT_ADDRESS, recursive = false)
public class StandaloneDeploymentColumn extends FinderColumn<Deployment> {

    static final String DEPLOYMENT_ADDRESS = "/deployment=*";
    static final AddressTemplate DEPLOYMENT_TEMPLATE = AddressTemplate.of(DEPLOYMENT_ADDRESS);

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final MetadataRegistry metadataRegistry;
    private final Provider<Progress> progress;
    private final Resources resources;

    @Inject
    public StandaloneDeploymentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Environment environment,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final MetadataRegistry metadataRegistry,
            @Footer final Provider<Progress> progress,
            final Resources resources) {

        super(new Builder<Deployment>(finder, Ids.DEPLOYMENT, Names.DEPLOYMENT)

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(ResourceAddress.root(),
                            READ_CHILDREN_RESOURCES_OPERATION
                    )
                            .param(CHILD_TYPE, DEPLOYMENT)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE_DEPTH, 2)
                            .build();
                    dispatcher.execute(operation, result -> {
                        List<Deployment> deployments = result.asPropertyList().stream()
                                .map(property -> new Deployment(Server.STANDALONE, property.getValue()))
                                .collect(toList());
                        callback.onSuccess(deployments);
                    });
                })

                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .showCount()
                .withFilter());

        this.environment = environment;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.metadataRegistry = metadataRegistry;
        this.progress = progress;
        this.resources = resources;

        List<ColumnAction<Deployment>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction.Builder<Deployment>(Ids.DEPLOYMENT_UPLOAD)
                .title(resources.constants().uploadDeployment())
                .handler(column -> uploadDeployment())
                .constraint(Constraint.executable(DEPLOYMENT_TEMPLATE, ADD))
                .build());
        addActions.add(new ColumnAction.Builder<Deployment>(Ids.DEPLOYMENT_UNMANAGED_ADD)
                .title(resources.messages().addResourceTitle(Names.UNMANAGED_DEPLOYMENT))
                .handler(column -> addUnmanaged())
                .constraint(Constraint.executable(DEPLOYMENT_TEMPLATE, ADD))
                .build());
        addColumnActions(Ids.DEPLOYMENT_ADD_ACTIONS, pfIcon("add-circle-o"), resources.constants().add(), addActions);
        addColumnAction(columnActionFactory.refresh(Ids.DEPLOYMENT_REFRESH));

        setItemRenderer(item -> new ItemDisplay<Deployment>() {
            @Override
            public String getId() {
                return Ids.deployment(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getTooltip() {
                if (item.getStatus() == Status.FAILED) {
                    return resources.constants().failed();
                } else if (item.getStatus() == Status.STOPPED) {
                    return resources.constants().stopped();
                } else if (item.getStatus() == Status.OK) {
                    return resources.constants().activeLower();
                } else {
                    return item.isEnabled() ? resources.constants().enabled() : resources.constants()
                            .disabled();
                }
            }

            @Override
            public Element getIcon() {
                String icon = item.isExploded() ? fontAwesome("folder-open") : fontAwesome("archive");
                SpanElement spanElement = Browser.getDocument().createSpanElement();
                spanElement.setClassName(icon);
                return spanElement;
            }

            @Override
            public String getFilterData() {
                return item.getName() + " " + (item.isEnabled() ? ENABLED : DISABLED);
            }

            @Override
            public List<ItemAction<Deployment>> actions() {
                List<ItemAction<Deployment>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(NameTokens.DEPLOYMENT, Ids.DEPLOYMENT, item.getName()));
                if (item.isEnabled()) {
                    actions.add(new ItemAction.Builder<Deployment>()
                            .title(resources.constants().disable())
                            .handler(deployment -> disable(deployment))
                            .constraint(Constraint.executable(DEPLOYMENT_TEMPLATE, UNDEPLOY))
                            .build());
                } else {
                    actions.add(new ItemAction.Builder<Deployment>()
                            .title(resources.constants().enable())
                            .handler(deployment -> enable(deployment))
                            .constraint(Constraint.executable(DEPLOYMENT_TEMPLATE, DEPLOY))
                            .build());
                }
                if (ManagementModel.supportsExplodeDeployment(environment.getManagementVersion())
                        && !item.isExploded() && !item.isEnabled()) {
                    actions.add(new ItemAction.Builder<Deployment>()
                            .title(resources.constants().explode())
                            .handler(itm -> explode(itm))
                            .constraint(Constraint.executable(DEPLOYMENT_TEMPLATE, EXPLODE))
                            .build());
                }
                actions.add(itemActionFactory.remove(Names.DEPLOYMENT, item.getName(), DEPLOYMENT_TEMPLATE,
                        StandaloneDeploymentColumn.this));
                return actions;
            }
        });

        setPreviewCallback(
                deployment -> new StandaloneDeploymentPreview(StandaloneDeploymentColumn.this, deployment, resources));
        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.upload(this, environment, dispatcher, eventBus, progress,
                    event.dataTransfer.files, resources
            ));
        }
    }

    private void uploadDeployment() {
        Metadata metadata = metadataRegistry.lookup(DEPLOYMENT_TEMPLATE);
        Wizard<UploadContext, UploadState> wizard = new Wizard.Builder<UploadContext, UploadState>(
                resources.messages().addResourceTitle(Names.DEPLOYMENT), new UploadContext())

                .addStep(UPLOAD, new UploadDeploymentStep(resources))
                .addStep(NAMES, new NamesStep(environment, metadata, resources))

                .onBack((context, currentState) -> currentState == NAMES ? UPLOAD : null)
                .onNext((context, currentState) -> currentState == UPLOAD ? NAMES : null)

                .stayOpenAfterFinish()
                .onFinish((wzd, context) -> {
                    String name = context.name;
                    String runtimeName = context.runtimeName;
                    wzd.showProgress(resources.constants().deploymentInProgress(),
                            resources.messages().deploymentInProgress(name));

                    Function[] functions = {
                            new CheckDeployment(dispatcher, name),
                            new UploadOrReplace(environment, dispatcher, name, runtimeName, context.file,
                                    context.enabled)
                    };
                    new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                            new Outcome<FunctionContext>() {
                                @Override
                                public void onFailure(final FunctionContext functionContext) {
                                    wzd.showError(resources.constants().deploymentError(),
                                            resources.messages().deploymentError(name), functionContext.getError());
                                }

                                @Override
                                public void onSuccess(final FunctionContext functionContext) {
                                    refresh(Ids.deployment(name));
                                    wzd.showSuccess(resources.constants().uploadSuccessful(),
                                            resources.messages().uploadSuccessful(name),
                                            resources.messages().view(Names.DEPLOYMENT),
                                            cxt -> { /* nothing to do, deployment is already selected */ });
                                }
                            }, functions);
                })
                .build();
        wizard.show();
    }

    private void addUnmanaged() {
        Metadata metadata = metadataRegistry.lookup(DEPLOYMENT_TEMPLATE);
        AddUnmanagedDialog dialog = new AddUnmanagedDialog(metadata, resources,
                (name, model) -> new Async<FunctionContext>(progress.get()).single(new FunctionContext(),
                        new SuccessfulOutcome(eventBus, resources) {
                            @Override
                            public void onSuccess(final FunctionContext context) {
                                refresh(Ids.deployment(name));
                                MessageEvent.fire(eventBus, Message.success(
                                        resources.messages()
                                                .addResourceSuccess(Names.UNMANAGED_DEPLOYMENT, name)));
                            }
                        }, new AddUnmanagedDeployment(dispatcher, name, model)));
        dialog.show();
    }

    void enable(Deployment deployment) {
        enableDisable(deployment, DEPLOY, resources.messages().deploymentEnabledSuccess(deployment.getName()));
    }

    void disable(Deployment deployment) {
        enableDisable(deployment, UNDEPLOY, resources.messages().deploymentDisabledSuccess(deployment.getName()));
    }

    private void enableDisable(Deployment deployment, String operation, SafeHtml message) {
        String id = Ids.deployment(deployment.getName());
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment.getName());
        Operation op = new Operation.Builder(address, operation).build();
        ItemMonitor.startProgress(id);
        dispatcher.execute(op, result -> {
            ItemMonitor.stopProgress(id);
            refresh(RESTORE_SELECTION);
            MessageEvent.fire(eventBus, Message.success(message));
        });
    }

    private void explode(Deployment deployment) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment.getName());
        Operation operation = new Operation.Builder(address, EXPLODE).build();
        dispatcher.execute(operation, result -> {
            refresh(RESTORE_SELECTION);
            MessageEvent
                    .fire(eventBus, Message.success(resources.messages().deploymentExploded(deployment.getName())));
        });
    }
}
