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
package org.jboss.hal.client.configuration.subsystem.resourceadapter;

import java.util.Arrays;

import org.jboss.hal.client.configuration.subsystem.resourceadapter.ResourceAdapter.AdapterType;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.resourceadapter.ResourceAdapter.AdapterType.ARCHIVE;
import static org.jboss.hal.client.configuration.subsystem.resourceadapter.ResourceAdapter.AdapterType.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSACTION_SUPPORT;

/**
 * @author Harald Pehl
 */
class ResourceAdapterPreview extends PreviewContent<ResourceAdapter> {

    ResourceAdapterPreview(final ResourceAdapter resourceAdapter, final Resources resources) {
        super(resourceAdapter.getName());

        AdapterType adapterType = resourceAdapter.getAdapterType();
        if (adapterType == ARCHIVE) {
            previewBuilder().p().innerHtml(
                    resources.messages().resourceAdapterProvidedBy(adapterType.text(), resourceAdapter.getArchive()))
                    .end();

        } else if (adapterType == MODULE) {
            previewBuilder().p().innerHtml(
                    resources.messages().resourceAdapterProvidedBy(adapterType.text(), resourceAdapter.getModule()))
                    .end();
        }

        PreviewAttributes<ResourceAdapter> attributes = new PreviewAttributes<>(resourceAdapter,
                Arrays.asList(STATISTICS_ENABLED, TRANSACTION_SUPPORT))
                .end();
        previewBuilder().addAll(attributes);
    }
}
