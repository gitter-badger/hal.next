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
package org.jboss.hal.client.runtime.subsystem.jndi;

import javax.inject.Inject;

import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.core.ui.Skeleton.applicationOffset;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class JndiView extends HalViewImpl implements JndiPresenter.MyView {

    private static final String JAVA_CONTEXTS = "java: contexts";
    private static final String APPLICATIONS = "applications";
    private static final String HEADER = "header";
    private static final String TREE_CONTAINER = "treeContainer";
    private static final String HINT = "hint";

    private Element header;
    private Element treeContainer;
    private Tree<JndiContext> tree;
    private Element hint;
    private Search search;
    private Form<ModelNode> details;
    private JndiPresenter presenter;

    @Inject
    public JndiView(JndiResources jndiResources, Resources resources) {

        search = new Search.Builder(Ids.JNDI_SEARCH,
                query -> {
                    if (tree.api() != null) {
                        tree.api().search(query);
                    }
                })
                .onClear(() -> {
                    if (tree.api() != null) {
                        tree.api().clearSearch();
                    }
                })
                .build();

        Metadata metadata = Metadata.staticDescription(jndiResources.jndi());
        details = new ModelNodeForm.Builder<>(Ids.JNDI_DETAILS, metadata)
                .include("uri", CLASS_NAME, VALUE)
                .readOnly()
                .unsorted()
                .build();
        registerAttachable(details);

        // @formatter:off
        LayoutBuilder builder = new LayoutBuilder()
            .row()
                .column(4)
                    .h(1).rememberAs(HEADER).textContent(resources.constants().jndiTree()).end()
                    .div().css(flexRow)
                        .div().css(btnGroup, marginRightSmall)
                            .button().css(btn, btnDefault).on(click, event -> presenter.reload())
                                .add("i").css(fontAwesome(CSS.refresh))
                            .end()
                            .button().css(btn, btnDefault).on(click, event -> collapse(tree.api().getSelected()))
                                .add("i").css(fontAwesome("minus"))
                            .end()
                        .end()
                        .add(search)
                    .end()
                    .div().rememberAs(TREE_CONTAINER).css(CSS.treeContainer).end()
                .end()
                .column(8)
                    .h(1).textContent(resources.constants().details()).end()
                    .p().rememberAs(HINT).textContent(resources.constants().noDetails()).end()
                    .add(details)
                .end()
            .end();
        // @formatter:on

        header = builder.referenceFor(HEADER);
        treeContainer = builder.referenceFor(TREE_CONTAINER);
        hint = builder.referenceFor(HINT);
        initElements(builder.elements());
    }

    @Override
    public void setPresenter(final JndiPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void attach() {
        super.attach();
        adjustHeight();
    }

    private void adjustHeight() {
        int headerHeight = header.getOffsetHeight();
        int searchHeight = search.asElement().getOffsetHeight();
        int offset = applicationOffset() + 2 * MARGIN_BIG + headerHeight + searchHeight + 2 * MARGIN_SMALL;
        treeContainer.getStyle().setHeight(vh(offset));
    }

    private void collapse(final Node<JndiContext> node) {
        if (node != null) {
            tree.select(node.id, true);
        }
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final ModelNode jndi) {
        JndiParser jndiParser = new JndiParser();
        JsArrayOf<Node<JndiContext>> nodes = JsArrayOf.create();

        if (jndi.hasDefined(JAVA_CONTEXTS)) {
            JndiContext jndiContext = new JndiContext();
            Node<JndiContext> root = new Node.Builder<>(Ids.JNDI_TREE_JAVA_CONTEXTS_ROOT, "Java Contexts", jndiContext)
                    .root()
                    .folder()
                    .open()
                    .build();
            jndiParser.parse(nodes, root, jndi.get(JAVA_CONTEXTS).asPropertyList());
        }

        if (jndi.hasDefined(APPLICATIONS)) {
            JndiContext jndiContext = new JndiContext();
            Node<JndiContext> root = new Node.Builder<>(Ids.JNDI_TREE_APPLICATIONS_ROOT, "Applications", jndiContext)
                    .root()
                    .folder()
                    .open()
                    .build();
            jndiParser.parse(nodes, root, jndi.get(APPLICATIONS).asPropertyList());
        }

        tree = new Tree<>(Ids.JNDI_TREE, nodes);
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());

        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> {
            if (!"ready".equals(selectionContext.action)) {
                boolean hasSelection = !selectionContext.selected.isEmpty();
                boolean validSelection = hasSelection && selectionContext.node.data.hasDetails;
                Elements.setVisible(hint, !validSelection);
                Elements.setVisible(details.asElement(), validSelection);
                if (validSelection) {
                    JndiContext jndiContext = selectionContext.node.data;
                    ModelNode modelNode = new ModelNode();
                    if (jndiContext.uri != null) {
                        modelNode.get("uri").set(jndiContext.uri);
                    }
                    if (jndiContext.className != null) {
                        modelNode.get(CLASS_NAME).set(jndiContext.className);
                    }
                    if (jndiContext.value != null) {
                        modelNode.get(VALUE).set(jndiContext.value);
                    }
                    details.view(modelNode);
                }
            }
        });

        Elements.setVisible(hint, true);
        Elements.setVisible(details.asElement(), false);
    }
}
