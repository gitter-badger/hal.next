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

import java.util.List;

import com.google.common.collect.Lists;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.client.deployment.Deployment.Status.OK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.CSS.stopCircleO;

/**
 * UI element to show the management model of a deployment in the model browser. Shows an empty state element in case
 * the deployment is inactive.
 *
 * @author Harald Pehl
 */
class DeploymentModelElement implements HasElements {

    private final ModelBrowser modelBrowser;
    private final Resources resources;
    private final EmptyState notEnabled;
    private final List<Element> elements;

    DeploymentModelElement(final ModelBrowser modelBrowser, final Resources resources) {
        this.modelBrowser = modelBrowser;
        this.resources = resources;

        notEnabled = new EmptyState.Builder(resources.constants().notEnabled())
                .icon(fontAwesome(stopCircleO))
                .build();
        notEnabled.asElement().getClassList().add(marginTopLarge);
        Elements.setVisible(notEnabled.asElement(), false);

        elements = Lists.newArrayList(modelBrowser.asElements());
        elements.add(notEnabled.asElement());
    }

    @Override
    public Iterable<Element> asElements() {
        return elements;
    }

    void setSurroundingHeight(final int surroundingHeight) {
        modelBrowser.setSurroundingHeight(surroundingHeight);
    }

    void update(Deployment deployment, Callback enableAction) {
        boolean active = deployment.getStatus() == OK;
        Elements.setVisible(notEnabled.asElement(), !active);
        modelBrowser.asElements().forEach(element -> Elements.setVisible(element, active));

        if (active) {
            ResourceAddress address = deployment.getReferenceServer().getServerAddress()
                    .add(DEPLOYMENT, deployment.getName());
            modelBrowser.setRoot(address, false);
        } else {
            notEnabled.setDescription(resources.messages().deploymentNotEnabled(deployment.getName()));
            notEnabled.setPrimaryAction(resources.constants().enable(), enableAction);
        }
    }
}
