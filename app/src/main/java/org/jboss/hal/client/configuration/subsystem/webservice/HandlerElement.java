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
package org.jboss.hal.client.configuration.subsystem.webservice;

import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.HANDLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * Element to configure handlers of a handler chain.
 *
 * @author Harald Pehl
 */
class HandlerElement implements IsElement, Attachable, HasPresenter<WebservicePresenter> {

    private final Element root;
    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private WebservicePresenter presenter;

    @SuppressWarnings("ConstantConditions")
    HandlerElement(final Config configType, final MetadataRegistry metadataRegistry,
            final TableButtonFactory tableButtonFactory) {

        String tableId = Ids.build(configType.baseId, "handler", Ids.TABLE_SUFFIX);
        Metadata metadata = metadataRegistry.lookup(HANDLER_TEMPLATE);
        table = new ModelNodeTable.Builder<NamedNode>(tableId, metadata)
                .button(tableButtonFactory.add(HANDLER_TEMPLATE, table -> presenter.addHandler()))
                .button(tableButtonFactory.remove(HANDLER_TEMPLATE,
                        table -> presenter.removeHandler(table.selectedRow().getName())))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .build();

        String formId = Ids.build(configType.baseId, "handler", Ids.FORM_SUFFIX);
        form = new ModelNodeForm.Builder<NamedNode>(formId, metadata)
                .onSave((form, changedValues) -> presenter.saveHandler(form.getModel().getName(), changedValues))
                .prepareReset(form -> presenter.resetHandler(form.getModel().getName(), form))
                .build();

        // @formatter:off
        root = new Elements.Builder()
            .section()
                .h(1).textContent(Names.HANDLER).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(table)
                .add(form)
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
    }

    @Override
    public void detach() {
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(final WebservicePresenter presenter) {
        this.presenter = presenter;
    }

    void update(final List<NamedNode> handlers) {
        form.clear();
        table.update(handlers);
    }
}
