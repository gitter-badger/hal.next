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
package org.jboss.hal.client.configuration.subsystem.messaging;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_TEMPLATE;

/**
 * @author Harald Pehl
 */
class MessagingSubsystemPreview extends PreviewContent<StaticItem> {

    private final CrudOperations crud;
    private final PreviewAttributes<ModelNode> attributes;

    @SuppressWarnings("HardCodedStringLiteral")
    MessagingSubsystemPreview(CrudOperations crud, Resources resources) {
        super(resources.constants().globalSettings());
        this.crud = crud;
        this.attributes = new PreviewAttributes<>(new ModelNode(),
                asList("global-client-scheduled-thread-pool-max-size",
                        "global-client-thread-pool-max-size"))
                .end();

        previewBuilder().addAll(attributes);
    }

    @Override
    public void update(final StaticItem item) {
        crud.read(MESSAGING_SUBSYSTEM_TEMPLATE, attributes::refresh);
    }
}
