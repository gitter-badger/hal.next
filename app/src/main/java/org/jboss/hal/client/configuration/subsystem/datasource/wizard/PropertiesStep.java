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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

/**
 * @author Harald Pehl
 */
class PropertiesStep extends WizardStep<Context, State> {

    private final Element root;

    PropertiesStep(final Resources resources) {
        super(resources.constants().xaProperties());
        root = new Elements.Builder().p().textContent(Names.NYI).end().build();
    }

    @Override
    public Element asElement() {
        return root;
    }
}
