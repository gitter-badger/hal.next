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
package org.jboss.hal.client.configuration.subsystem.elytron;

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
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class FactoriesPresenter extends MbuiPresenter<FactoriesPresenter.MyView, FactoriesPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value ={
        AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY, AGGREGATE_SASL_SERVER_FACTORY, CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
        CONFIGURABLE_SASL_SERVER_FACTORY, CUSTOM_CREDENTIAL_SECURITY_FACTORY, HTTP_AUTHENTICATION_FACTORY,
        KERBEROS_SECURITY_FACTORY, MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY, PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
        PROVIDER_SASL_SERVER_FACTORY, SASL_AUTHENTICATION_FACTORY, SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, SERVICE_LOADER_SASL_SERVER_FACTORY,
        AGGREGATE_TRANSFORMER, CHAINED_TRANSFORMER, CONSTANT_TRANSFORMER, CUSTOM_TRANSFORMER, REGEX_VALIDATING_TRANSFORMER,
        REGEX_TRANSFORMER,
    })
    @NameToken(NameTokens.ELYTRON_FACTORIES_TRANSFORMERS)
    public interface MyProxy extends ProxyPlace<FactoriesPresenter> {}

    public interface MyView extends MbuiView<FactoriesPresenter> {
        void updateAggregateHttpServerMechanism(List<NamedNode> model);
        void updateAggregateSaslServer(List<NamedNode> model);
        void updateConfigurableHttpServerMechanism(List<NamedNode> model);
        void updateConfigurableSaslServer(List<NamedNode> model);
        void updateCustomCredentialSecurity(List<NamedNode> model);
        void updateHttpAuthentication(List<NamedNode> model);
        void updateKerberosSecurity(List<NamedNode> model);
        void updateMechanismProviderFilteringSaslServer(List<NamedNode> model);
        void updateProviderHttpServerMechanism(List<NamedNode> model);
        void updateProviderSaslServer(List<NamedNode> model);
        void updateSaslAuthentication(List<NamedNode> model);
        void updateServiceLoaderHttpServerMechanism(List<NamedNode> model);
        void updateServiceLoaderSaslServer(List<NamedNode> model);

        void updateAggregatePrincipalTransformer(List<NamedNode> model);
        void updateChainedPrincipalTransformer(List<NamedNode> model);
        void updateConstantPrincipalTransformer(List<NamedNode> model);
        void updateCustomPrincipalTransformer(List<NamedNode> model);
        void updateRegexPrincipalTransformer(List<NamedNode> model);
        void updateRegexValidatingPrincipalTransformer(List<NamedNode> model);

    }
    // @formatter:on

    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public FactoriesPresenter(final EventBus eventBus,
            final FactoriesPresenter.MyView view,
            final FactoriesPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final Dispatcher dispatcher,
            final FinderPathFactory finderPathFactory,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(Ids.ELYTRON)
                .append(Ids.ELYTRON, Ids.asId(Names.FACTORIES_TRANSFORMERS),
                        resources.constants().settings(), Names.FACTORIES_TRANSFORMERS);
    }

    @Override
    protected void reload() {

        ResourceAddress address = ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
        crud.readChildren(address, asList(
                "aggregate-http-server-mechanism-factory",
                "aggregate-sasl-server-factory",
                "configurable-http-server-mechanism-factory",
                "configurable-sasl-server-factory",
                "custom-credential-security-factory",
                "http-authentication-factory",
                "kerberos-security-factory",
                "mechanism-provider-filtering-sasl-server-factory",
                "provider-http-server-mechanism-factory",
                "provider-sasl-server-factory",
                "sasl-authentication-factory",
                "service-loader-http-server-mechanism-factory",
                "service-loader-sasl-server-factory",

                "aggregate-principal-transformer",
                "chained-principal-transformer",
                "constant-principal-transformer",
                "custom-principal-transformer",
                "regex-principal-transformer",
                "regex-validating-principal-transformer"
                ),
                result -> {
                    // @formatter:off
                    getView().updateAggregateHttpServerMechanism(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateAggregateSaslServer(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateConfigurableHttpServerMechanism(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateConfigurableSaslServer(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateCustomCredentialSecurity(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateHttpAuthentication(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateKerberosSecurity(asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                    getView().updateMechanismProviderFilteringSaslServer(asNamedNodes(result.step(7).get(RESULT).asPropertyList()));
                    getView().updateProviderHttpServerMechanism(asNamedNodes(result.step(8).get(RESULT).asPropertyList()));
                    getView().updateProviderSaslServer(asNamedNodes(result.step(9).get(RESULT).asPropertyList()));
                    getView().updateSaslAuthentication(asNamedNodes(result.step(10).get(RESULT).asPropertyList()));
                    getView().updateServiceLoaderHttpServerMechanism(asNamedNodes(result.step(11).get(RESULT).asPropertyList()));
                    getView().updateServiceLoaderSaslServer(asNamedNodes(result.step(12).get(RESULT).asPropertyList()));

                    getView().updateAggregatePrincipalTransformer(asNamedNodes(result.step(13).get(RESULT).asPropertyList()));
                    getView().updateChainedPrincipalTransformer(asNamedNodes(result.step(14).get(RESULT).asPropertyList()));
                    getView().updateConstantPrincipalTransformer(asNamedNodes(result.step(15).get(RESULT).asPropertyList()));
                    getView().updateCustomPrincipalTransformer(asNamedNodes(result.step(16).get(RESULT).asPropertyList()));
                    getView().updateRegexPrincipalTransformer(asNamedNodes(result.step(17).get(RESULT).asPropertyList()));
                    getView().updateRegexValidatingPrincipalTransformer(asNamedNodes(result.step(18).get(RESULT).asPropertyList()));
                    // @formatter:on
                });
    }

}
