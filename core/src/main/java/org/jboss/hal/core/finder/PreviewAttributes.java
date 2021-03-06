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
package org.jboss.hal.core.finder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.ResolveExpressionEvent;
import org.jboss.hal.core.Core;
import org.jboss.hal.core.expression.Expression;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Element to show the basic attributes of a resource inside the preview pane.
 * TODO Apply constraints to attributes?
 *
 * @author Harald Pehl
 */
public class PreviewAttributes<T extends ModelNode> implements HasElements {

    public static class PreviewAttribute {

        final String label;
        final String value;
        final SafeHtml htmlValue;
        final String href;
        final Element element;
        final Iterable<Element> elements;

        public PreviewAttribute(final String label, final String value) {
            this(label, value, null, null, null, null);
        }

        public PreviewAttribute(final String label, final String value, final String href) {
            this(label, value, null, href, null, null);
        }

        public PreviewAttribute(final String label, final SafeHtml value) {
            this(label, null, value, null, null, null);
        }

        public PreviewAttribute(final String label, final SafeHtml value, final String href) {
            this(label, null, value, href, null, null);
        }

        public PreviewAttribute(final String label, final Iterable<Element> elements) {
            this(label, null, null, null, elements, null);
        }

        public PreviewAttribute(final String label, final Element element) {
            this(label, null, null, null, null, element);
        }

        private PreviewAttribute(final String label, final String value, final SafeHtml htmlValue, final String href,
                final Iterable<Element> elements, final Element element) {
            this.label = label;
            this.value = value;
            this.htmlValue = htmlValue;
            this.href = href;
            this.element = element;
            this.elements = elements;
        }

        private boolean isUndefined() {
            return value == null && htmlValue == null;
        }

        private boolean isExpression() {
            return Expression.isExpression(value);
        }
    }


    @FunctionalInterface
    public interface PreviewAttributeFunction<T> {

        PreviewAttribute labelValue(T model);
    }


    private static final String DESCRIPTION = "description";
    private static final String LABEL = "label";
    private static final String VALUE = "value";
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final T model;
    private final Element description;
    private final LabelBuilder labelBuilder;
    private final Elements.Builder builder;
    private final Map<String, PreviewAttributeFunction<T>> functions;
    private final Map<String, Element> labelElements;

    public PreviewAttributes(final T model) {
        this(model, CONSTANTS.mainAttributes(), null, Collections.emptyList());
    }

    public PreviewAttributes(final T model, final String header) {
        this(model, header, null, Collections.emptyList());
    }

    public PreviewAttributes(final T model, final List<String> attributes) {
        this(model, CONSTANTS.mainAttributes(), null, attributes);
    }

    public PreviewAttributes(final T model, final String header, final List<String> attributes) {
        this(model, header, null, attributes);
    }

    public PreviewAttributes(final T model, final String header, final String description,
            final List<String> attributes) {
        this.model = model;
        this.labelBuilder = new LabelBuilder();
        this.functions = new HashMap<>();
        this.labelElements = new HashMap<>();
        this.builder = new Elements.Builder().h(2).textContent(header).end().p().rememberAs(DESCRIPTION).end();

        this.description = builder.referenceFor(DESCRIPTION);
        if (description != null) {
            this.description.setTextContent(description);
        } else {
            Elements.setVisible(this.description, false);
        }

        this.builder.ul().css(listGroup);
        attributes.forEach(this::append);
    }

    public PreviewAttributes<T> append(final String attribute) {
        append(model -> new PreviewAttribute(labelBuilder.label(attribute),
                model.hasDefined(attribute) ? model.get(attribute).asString() : ""));
        return this;
    }

    public PreviewAttributes<T> append(final String attribute, String href) {
        append(model -> new PreviewAttribute(labelBuilder.label(attribute),
                model.hasDefined(attribute) ? model.get(attribute).asString() : "",
                href));
        return this;
    }

    public PreviewAttributes<T> append(final PreviewAttributeFunction<T> function) {
        String id = Ids.uniqueId();
        String labelId = Ids.build(id, LABEL);
        String valueId = Ids.build(id, VALUE);
        functions.put(id, function);

        PreviewAttribute previewAttribute = function.labelValue(model);
        // @formatter:off
        builder.li().rememberAs(id).css(listGroupItem)
            .span().rememberAs(labelId).css(key).textContent(previewAttribute.label).end();
            if (previewAttribute.elements != null || previewAttribute.element != null) {
                builder.span().rememberAs(valueId).css(CSS.value);
                if (previewAttribute.elements != null) {
                    builder.addAll(previewAttribute.elements);
                } else {
                    builder.add(previewAttribute.element);
                }
                builder.end();
            } else {
                if (previewAttribute.href != null) {
                    builder.span().css(value).a(previewAttribute.href).rememberAs(valueId);
                } else {
                    builder.span().css(value).rememberAs(valueId);
                }
                if (previewAttribute.isUndefined()) {
                    builder.textContent(Names.NOT_AVAILABLE);
                }
                else if (previewAttribute.htmlValue != null) {
                    builder.innerHtml(previewAttribute.htmlValue);
                } else {
                    if (previewAttribute.isExpression()) {
                        builder.span();
                    }
                    builder.textContent(previewAttribute.value);
                    if (previewAttribute.value.length() > 15) {
                        builder.title(previewAttribute.value);
                    }
                    if (previewAttribute.isExpression()) {
                        builder.end();
                        builder.span()
                                .css(fontAwesome("link"), clickable, marginLeft5)
                                .title(CONSTANTS.resolveExpression())
                                .on(click, event ->
                                        Core.INSTANCE.eventBus().fireEvent(
                                                new ResolveExpressionEvent(previewAttribute.value)))
                                .end();
                    }
                }
                builder.end();
                if (previewAttribute.href != null) {
                    builder.end();
                }
            }
        builder.end(); // </li>
        // @formatter:on

        Element lastAttributeGroupItem = builder.referenceFor(id);
        labelElements.put(previewAttribute.label, lastAttributeGroupItem);
        return this;
    }

    public PreviewAttributes<T> end() {
        builder.end();
        return this;
    }

    public void refresh(T model) {
        for (Map.Entry<String, PreviewAttributeFunction<T>> entry : functions.entrySet()) {
            String id = entry.getKey();
            String labelId = Ids.build(id, LABEL);
            String valueId = Ids.build(id, VALUE);

            PreviewAttributeFunction<T> function = entry.getValue();
            PreviewAttribute previewAttribute = function.labelValue(model);

            builder.referenceFor(labelId).setTextContent(previewAttribute.label);
            Element valueElement = builder.referenceFor(valueId);
            if (previewAttribute.elements != null) {
                Elements.removeChildrenFrom(valueElement);
                previewAttribute.elements.forEach(valueElement::appendChild);
            } else if (previewAttribute.element != null) {
                Elements.removeChildrenFrom(valueElement);
                valueElement.appendChild(previewAttribute.element);
            } else if (previewAttribute.htmlValue != null || previewAttribute.value != null) {
                if (previewAttribute.href != null && "a".equalsIgnoreCase(valueElement.getTagName())) { //NON-NLS
                    valueElement.setAttribute(UIConstants.HREF, previewAttribute.href);
                }
                if (previewAttribute.htmlValue != null) {
                    valueElement.setInnerHTML(previewAttribute.htmlValue.asString());
                } else {
                    valueElement.setTextContent(previewAttribute.value);
                }
            }
        }
    }

    public void setVisible(String attribute, boolean visible) {
        Elements.setVisible(labelElements.get(labelBuilder.label(attribute)), visible);
    }

    public void setDescription(String description) {
        this.description.setTextContent(description);
        Elements.setVisible(this.description, true);
    }

    public void setDescription(SafeHtml description) {
        this.description.setInnerHTML(description.asString());
        Elements.setVisible(this.description, true);
    }

    public void setDescription(Element description) {
        Elements.removeChildrenFrom(description);
        this.description.appendChild(description);
        Elements.setVisible(this.description, true);
    }

    public void hideDescription() {
        Elements.setVisible(this.description, false);
    }

    @Override
    public Iterable<Element> asElements() {
        return builder.elements();
    }
}
