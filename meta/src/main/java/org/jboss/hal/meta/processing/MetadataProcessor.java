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
package org.jboss.hal.meta.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.meta.security.SecurityContextRegistry;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Metadata processor which processes the required resources attached to tokens and stores the retrieved metadata in
 * the related registries. In addition you can call {@link #lookup(AddressTemplate, Progress, MetadataCallback)} to get
 * and dynamically create the metadata attached to a specific address template.
 *
 * @author Harald Pehl
 */
@JsType(namespace = "hal.meta")
public class MetadataProcessor {

    public interface MetadataCallback {

        void onMetadata(Metadata metadata);

        void onError(Throwable error);
    }


    /**
     * Recursive depth for the r-r-d operations. Keep this small - some browsers choke on too big payload size
     */
    static final int RRD_DEPTH = 3;

    /**
     * Number of r-r-d operations part of one composite operation.
     */
    private final static int BATCH_SIZE = 3;

    @NonNls private static final Logger logger = LoggerFactory.getLogger(MetadataProcessor.class);

    private final Dispatcher dispatcher;
    private final RequiredResources requiredResources;
    private final MetadataRegistry metadataRegistry;
    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextRegistry securityContextRegistry;
    private final Lookup lookup;
    private final CreateRrdOperations rrdOps;

    @Inject
    @JsIgnore
    public MetadataProcessor(final Environment environment,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final RequiredResources requiredResources,
            final MetadataRegistry metadataRegistry,
            final SecurityContextRegistry securityContextRegistry,
            final ResourceDescriptionRegistry resourceDescriptionRegistry) {
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.requiredResources = requiredResources;
        this.securityContextRegistry = securityContextRegistry;
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.lookup = new Lookup(securityContextRegistry, resourceDescriptionRegistry);
        this.rrdOps = new CreateRrdOperations(statementContext, environment);
    }

    @JsIgnore
    public void process(final String id, final Progress progress, final AsyncCallback<Void> callback) {
        Set<String> resources = requiredResources.getResources(id);
        logger.debug("Process required resources {} for id {}", resources, id);
        if (resources.isEmpty()) {
            logger.debug("No required resources found -> callback.onSuccess(null)");
            callback.onSuccess(null);

        } else {
            Set<AddressTemplate> templates = resources.stream().map(AddressTemplate::of).collect(toSet());
            processInternal(templates, requiredResources.isRecursive(id), progress, callback);
        }
    }

    @JsIgnore
    public void lookup(final AddressTemplate template, Progress progress, final MetadataCallback callback) {
        logger.debug("Lookup metadata for {}", template);
        processInternal(singleton(template), false, progress, new AsyncCallback<Void>() {
            @Override
            public void onFailure(final Throwable throwable) {
                callback.onError(throwable);
            }

            @Override
            public void onSuccess(final Void aVoid) {
                // if we're here all metadata must be in the registry
                callback.onMetadata(metadataRegistry.lookup(template));
            }
        });
    }

    private void processInternal(final Set<AddressTemplate> templates, final boolean recursive, final Progress progress,
            final AsyncCallback<Void> callback) {
        LookupResult lookupResult = lookup.check(templates, recursive);
        if (lookupResult.allPresent()) {
            logger.debug("All metadata have been already processed -> callback.onSuccess(null)");
            callback.onSuccess(null);
        } else {
            logger.debug("{}", lookupResult);

            // create and partition non-optional operations
            List<Operation> operations = rrdOps.create(lookupResult, false);
            List<List<Operation>> piles = Lists.partition(operations, BATCH_SIZE);
            List<Composite> composites = piles.stream().map(Composite::new).collect(toList());
            List<RrdFunction> functions = composites.stream()
                    .map(composite -> new RrdFunction(securityContextRegistry, resourceDescriptionRegistry,
                            dispatcher, composite, false))
                    .collect(toList());

            // create optional operations w/o partitioning!
            List<Operation> optionalOperations = rrdOps.create(lookupResult, true);
            // Do not refactor to
            // List<Composite> optionalComposites = optionalOperations.stream().map(Composite::new).collect(toList());
            // the GWT compiler will crash with an ArrayIndexOutOfBoundsException!
            List<Composite> optionalComposites = new ArrayList<>();
            optionalOperations.forEach(operation -> optionalComposites.add(new Composite(operation)));
            List<RrdFunction> optionalFunctions = optionalComposites.stream()
                    .map(composite -> new RrdFunction(securityContextRegistry, resourceDescriptionRegistry,
                            dispatcher, composite, true))
                    .collect(toList());

            logger.debug("About to execute {} composite operations", composites.size() + optionalComposites.size());
            Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                @Override
                public void onFailure(final FunctionContext context) {
                    logger.debug("Failed to process metadata: {}", context.getError());
                    callback.onFailure(context.getException());
                }

                @Override
                public void onSuccess(final FunctionContext context) {
                    logger.debug("Successfully processed metadata");
                    callback.onSuccess(null);
                }
            };

            List<RrdFunction> allFunctions = new ArrayList<>();
            allFunctions.addAll(functions);
            allFunctions.addAll(optionalFunctions);
            if (functions.size() == 1) {
                new Async<FunctionContext>(progress).single(new FunctionContext(), outcome, allFunctions.get(0));
            } else {
                // Unfortunately we cannot use Async.parallel() here unless someone finds a way
                // to unambiguously map parallel r-r-d operations to their results (multiple "step-1" results)
                //noinspection SuspiciousToArrayCall
                new Async<FunctionContext>(progress).waterfall(new FunctionContext(), outcome,
                        (Function[]) allFunctions.toArray(new RrdFunction[allFunctions.size()]));
            }
        }
    }


    // ------------------------------------------------------ JS methods


    @JsFunction
    public interface JsMetadataCallback {

        void onMetadata(Metadata metadata);
    }

    @JsMethod(name = "lookup")
    public void jsLookup(Object template, JsMetadataCallback callback) {
        MetadataCallback mc = new MetadataCallback() {
            @Override
            public void onMetadata(final Metadata metadata) {
                callback.onMetadata(metadata);
            }

            @Override
            public void onError(final Throwable error) {
                logger.error("Unable to lookup metadata for {}: {}", template, error.getMessage());
            }
        };
        if (template instanceof String) {
            lookup(AddressTemplate.of(((String) template)), Progress.NOOP, mc);
        } else if (template instanceof AddressTemplate) {
            lookup((AddressTemplate) template, Progress.NOOP, mc);
        } else {
            throw new IllegalArgumentException(
                    "Illegal 1st argument: Use MetadataProcessor((AddressTemplate|String), function(Metadata))");
        }
    }
}
