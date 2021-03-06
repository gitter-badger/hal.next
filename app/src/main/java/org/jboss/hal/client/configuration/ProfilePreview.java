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
package org.jboss.hal.client.configuration;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static java.util.stream.Collectors.joining;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
class ProfilePreview extends PreviewContent<NamedNode> {

    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final Places places;
    private final Resources resources;
    private final Element includesElement;
    private final Element serverGroupsElement;

    ProfilePreview(final Dispatcher dispatcher, FinderPathFactory finderPathFactory, final Places places,
            final Resources resources, final NamedNode profile) {
        super(profile.getName());
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.places = places;
        this.resources = resources;

        includesElement = Browser.getDocument().createElement("p"); //NON-NLS
        serverGroupsElement = Browser.getDocument().createElement("p"); //NON-NLS
        previewBuilder().add(includesElement).add(serverGroupsElement);
    }

    @Override
    public void update(final NamedNode item) {
        if (item.hasDefined(INCLUDES) && !item.get(INCLUDES).asList().isEmpty()) {
            String includes = item.get(INCLUDES).asList().stream()
                    .map(ModelNode::asString)
                    .collect(joining(", "));
            includesElement.setTextContent(resources.messages().profileIncludes(includes));
            Elements.setVisible(includesElement, true);

        } else {
            Elements.setVisible(includesElement, false);
        }

        Operation operation = new Operation.Builder(new ResourceAddress().add(SERVER_GROUP, "*"), QUERY)
                .param(WHERE, new ModelNode().set(PROFILE, item.getName()))
                .build();
        dispatcher.execute(operation, result -> {
            List<String> serverGroups = result.asList().stream()
                    .map(modelNode -> new ResourceAddress(modelNode.get(ADDRESS)))
                    .map(ResourceAddress::lastValue)
                    .sorted()
                    .collect(Collectors.toList());
            if (!serverGroups.isEmpty()) {
                @NonNls SafeHtmlBuilder html = new SafeHtmlBuilder();
                for (Iterator<String> iterator = serverGroups.iterator(); iterator.hasNext(); ) {
                    String serverGroup = iterator.next();
                    PlaceRequest placeRequest = places.finderPlace(NameTokens.RUNTIME,
                            finderPathFactory.runtimeServerGroupPath(serverGroup)).build();
                    String token = places.historyToken(placeRequest);
                    html.appendHtmlConstant("<a href=\"").appendHtmlConstant(token).appendHtmlConstant("\">")
                            .appendEscaped(serverGroup).appendHtmlConstant("</a>");
                    if (iterator.hasNext()) {
                        html.appendEscaped(", ");
                    }
                }
                serverGroupsElement.setInnerHTML(
                        resources.messages().profileUsedInServerGroups(html.toSafeHtml()).asString());
            } else {
                serverGroupsElement.setInnerHTML(resources.messages().profileNotUsedInServerGroups().asString());

            }
        });
    }
}
