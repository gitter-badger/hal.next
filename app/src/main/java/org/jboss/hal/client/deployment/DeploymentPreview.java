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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.Icons.flag;

/**
 * @author Harald Pehl
 */
abstract class DeploymentPreview<T extends ModelNode> extends PreviewContent<T> {

    private final LabelBuilder labelBuilder;

    DeploymentPreview(final String header) {
        super(header);
        this.labelBuilder = new LabelBuilder();
    }

    /**
     * Adds the (e)nabled, (m)anaged and (e)xploded and flags to the specified preview attributes.
     */
    void eme(PreviewAttributes<T> attributes) {
        attributes.append(model -> {
            String label = String.join(", ",
                    labelBuilder.label(ENABLED), labelBuilder.label(MANAGED), labelBuilder.label(EXPLODED));
            // @formatter:off
            Elements.Builder builder = new Elements.Builder()
                .span()
                    .title(labelBuilder.label(ENABLED))
                    .css(flag(failSafeBoolean(model, ENABLED)), marginRight5)
                .end()
                .span()
                    .title(labelBuilder.label(MANAGED))
                    .css(flag(failSafeBoolean(model, MANAGED)), marginRight5)
                .end()
                .span()
                    .title(labelBuilder.label(EXPLODED))
                    .css(flag(failSafeBoolean(model, EXPLODED)))
                .end();
            // @formatter:on
            return new PreviewAttribute(label, builder.elements());
        });
    }

    void status(PreviewAttributes<T> attributes, Deployment deployment) {
        attributes.append(model -> new PreviewAttribute(labelBuilder.label(STATUS), deployment.getStatus().name()));
    }

    void subDeployments(Deployment deployment) {
        previewBuilder().h(2).textContent(Names.SUBDEPLOYMENTS).end().ul();
        deployment.getSubdeployments().forEach(
                subdeployment -> previewBuilder().li().textContent(subdeployment.getName()).end());
        previewBuilder().end();
    }

    void contextRoot(PreviewAttributes<T> attributes, Deployment deployment) {
        if (deployment.hasSubsystem(UNDERTOW)) {
            ModelNode contextRoot = failSafeGet(deployment, String.join("/", SUBSYSTEM, UNDERTOW, CONTEXT_ROOT));
            if (contextRoot.isDefined()) {
                attributes.append(model -> new PreviewAttribute(Names.CONTEXT_ROOT, contextRoot.asString()));
            }

        } else if (deployment.hasNestedSubsystem(UNDERTOW)) {
            Elements.Builder builder = new Elements.Builder().ul();
            for (Subdeployment subdeployment : deployment.getSubdeployments()) {
                ModelNode contextRoot = failSafeGet(subdeployment, String.join("/", SUBSYSTEM, UNDERTOW, CONTEXT_ROOT));
                if (contextRoot.isDefined()) {
                    SafeHtml safeHtml = new SafeHtmlBuilder()
                            .appendEscaped(subdeployment.getName() + " ")
                            .appendHtmlConstant("&rarr;") //NON-NLS
                            .appendEscaped(" " + contextRoot.asString())
                            .toSafeHtml();
                    builder.li().innerHtml(safeHtml).end();
                }
            }
            Element element = builder.end().build(); // </ul>
            attributes.append(model -> new PreviewAttribute(Names.CONTEXT_ROOTS, element));
        }
    }
}
