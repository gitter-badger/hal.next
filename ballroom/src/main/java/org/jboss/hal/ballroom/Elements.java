/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom;

import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.html.InputElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Helper methods for working with {@link Element}s.
 *
 * @author Harald Pehl
 */
public final class Elements {

    // this is a static helper class which must never be instantiated!
    private Elements() {}

    /**
     * Known input types used in {@link Builder#input(InputType)}. If not otherwise specified the created element is a
     * simple element.
     */
    public enum InputType {
        /**
         * Creates a container element (must be closed using {@link Builder#end()})
         */
        button,
        checkbox,
        color,
        date,
        datetime,
        email,
        file,
        hidden,
        image,
        month,
        number,
        password,
        radio,
        range,
        reset,
        search,
        /**
         * Creates a container element (must be closed using {@link Builder#end()})
         */
        select,
        tel,
        text,
        /**
         * Creates a container element (must be closed using {@link Builder#end()})
         */
        textarea,
        time,
        url,
        week
    }


    /**
     * Known event types used in {@link Builder#on(EventType, EventListener)}.
     */
    public enum EventType {
        abort(Element::setOnclick),
        beforecopy(Element::setOnbeforecopy),
        beforecut(Element::setOnbeforecut),
        beforepaste(Element::setOnbeforepaste),
        blur(Element::setOnblur),
        change(Element::setOnchange),
        click(Element::setOnclick),
        contextmenu(Element::setOncontextmenu),
        copy(Element::setOncopy),
        cut(Element::setOncut),
        dblclick(Element::setOndblclick),
        drag(Element::setOndrag),
        dragend(Element::setOndragend),
        dragenter(Element::setOndragenter),
        dragleave(Element::setOndragleave),
        dragover(Element::setOndragover),
        dragstart(Element::setOndragstart),
        drop(Element::setOndrop),
        error(Element::setOnerror),
        focus(Element::setOnfocus),
        input(Element::setOninput),
        invalid(Element::setOninvalid),
        keydown(Element::setOnkeydown),
        keypress(Element::setOnkeypress),
        keyup(Element::setOnkeyup),
        load(Element::setOnload),
        mousedown(Element::setOnmousedown),
        mousemove(Element::setOnmousemove),
        mouseout(Element::setOnmouseout),
        mouseover(Element::setOnmouseover),
        mouseup(Element::setOnmouseup),
        mousewheel(Element::setOnmousewheel),
        paste(Element::setOnpaste),
        reset(Element::setOnreset),
        scroll(Element::setOnscroll),
        search(Element::setOnsearch),
        select(Element::setOnselect),
        selectstart(Element::setOnselectstart),
        submit(Element::setOnsubmit),
        touchcancel(Element::setOntouchcancel),
        touchend(Element::setOntouchend),
        touchmove(Element::setOntouchmove),
        touchstart(Element::setOntouchstart),
        webkitfullscreenchange(Element::setOnwebkitfullscreenchange),
        webkitfullscreenerror(Element::setOnwebkitfullscreenerror);

        final EventRegistrar registrar;

        EventType(final EventRegistrar registrar) {this.registrar = registrar;}
    }


    @FunctionalInterface
    private interface EventRegistrar {

        void register(Element element, EventListener listener);
    }


    private static class ElementInfo {

        int level;
        Element element;
        boolean container;

        public ElementInfo(final Element element, final boolean container, final int level) {
            this.container = container;
            this.element = element;
            this.level = level;
        }

        @Override
        public String toString() {
            return (container ? "container" : "simple") + " @ " + level + ": " + element;
        }
    }


    /**
     * Builder to create a hierarchy of {@link Element}s. Supports convenience methods to create common elements
     * and attributes. Uses a fluent API to create and append elements on the fly.
     * <p>
     * The builder distinguishes between elements which can contain nested elements (container) and simple element w/o
     * children. The former must be closed using {@link #end()}.
     * <p>
     * In order to create this form,
     * <pre>
     * &lt;form method="get" action="search" class="form form-horizontal"&gt;
     *     &lt;div class="form-group"&gt;
     *         &lt;label class="col-md-3 control-label" for="name"&gt;Name&lt;/label&gt;
     *         &lt;div class="col-md-9"&gt;
     *             &lt;input type="text" id="name" class="form-control" placeholder="Enter your name"/&gt;
     *         &lt;/div&gt;
     *     &lt;/div&gt;
     *     &lt;div class="form-group"&gt;
     *         &lt;label class="col-md-3 control-label" for="age"&gt;Age&lt;/label&gt;
     *         &lt;div class="col-md-9"&gt;
     *             &lt;input type="number" id="age" class="form-control" placeholder="How old are you?"/&gt;
     *         &lt;/div&gt;
     *     &lt;/div&gt;
     *     &lt;div class="form-group"&gt;
     *         &lt;label class="col-md-3 control-label" for="hobbies"&gt;Hobbies&lt;/label&gt;
     *         &lt;div class="col-md-9"&gt;
     *             &lt;textarea rows="3" id="hobbies" class="form-control"&gt;&lt;/textarea&gt;
     *             &lt;span class="help-block textarea"&gt;One item per line&lt;/span&gt;
     *         &lt;/div&gt;
     *     &lt;/div&gt;
     *     &lt;div class="form-group"&gt;
     *         &lt;label class="col-md-3 control-label" for="choose"&gt;Choose&lt;/label&gt;
     *         &lt;div class="col-md-9"&gt;
     *             &lt;select id="choose" class="form-control selectpicker"&gt;
     *                 &lt;option&gt;Lorem&lt;/option&gt;
     *                 &lt;option&gt;ipsum&lt;/option&gt;
     *             &lt;/select&gt;
     *         &lt;/div&gt;
     *     &lt;/div&gt;
     *     &lt;div class="form-group"&gt;
     *         &lt;div class="col-md-offset-3 col-md-9"&gt;
     *             &lt;div class="pull-right form-buttons"&gt;
     *                 &lt;button type="button" class="btn btn-default btn-form"&gt;Cancel&lt;/button&gt;
     *                 &lt;button type="button" class="btn btn-primary btn-form"&gt;Save&lt;/button&gt;
     *             &lt;/div&gt;
     *         &lt;/div&gt;
     *     &lt;/div&gt;
     * &lt;/form&gt;
     * </pre>
     * <p>
     * use the following builder code:
     * <pre>
     * Element form = new Elements.Builder().
     *     .form().attribute("method", "get").attribute("action", "search").css("form form-horizontal")
     *         .div().css("form-group")
     *             .label().css("col-md-3 control-label").attribute("for", "name").innerText("Name").end()
     *             .div().css("col-md-9")
     *                 .input(text).id("name").css("form-control").attribute("placeholder", "Enter your name")
     *             .end()
     *         .end()
     *         .div().css("form-group")
     *             .label().css("col-md-3 control-label").attribute("for", "age").innerText("Age").end()
     *             .div().css("col-md-9")
     *                 .input(number).id("age").css("form-control").attribute("placeholder", "How old are you?")
     *             .end()
     *         .end()
     *         .div().css("form-group")
     *             .label().css("col-md-3 control-label").attribute("for", "hobbies").innerText("Hobbies").end()
     *             .div().css("col-md-9")
     *                 .textarea().attribute("rows", "3").id("hobbies").css("form-control").end()
     *                 .span().css("help-block textarea").innerText("One item per line").end()
     *             .end()
     *         .end()
     *         .div().css("form-group")
     *             .label().css("col-md-3 control-label").attribute("for", "choose").innerText("Choose").end()
     *             .div().css("col-md-9")
     *                 .select().id("choose").css("form-control selectpicker")
     *                     .option().innerText("Lorem").end()
     *                     .option().innerText("ipsum").end()
     *                 .end()
     *             .end()
     *         .end()
     *         .div().css("form-group")
     *             .div().css("col-md-offset-3 col-md-9")
     *                 .div().css("pull-right form-buttons")
     *                     .button().css("btn btn-default btn-form").innerText("Cancel").end()
     *                     .button().css("btn btn-primary btn-form").innerText("Save").end()
     *                 .end()
     *             .end()
     *         .end()
     *     .end()
     * .build();
     * </pre>
     *
     * @author Harald Pehl
     */
    public static final class Builder {

        private final Document document;
        private final Stack<ElementInfo> elements;
        private final Map<String, Element> references;
        private int level;

        public Builder() {
            this(Browser.getDocument());
        }

        protected Builder(Document document) {
            this.document = document;
            this.elements = new Stack<>();
            this.references = new HashMap<>();
        }


        // ------------------------------------------------------ container elements

        /**
         * Creates a new heading container. The element must be closed with {@link #end()}.
         */
        public Builder h(int ordinal) {
            return start("h" + ordinal);
        }

        /**
         * Creates a new paragraph container. The element must be closed with {@link #end()}.
         */
        public Builder p() {
            return start(document.createParagraphElement());
        }

        /**
         * Creates a new ordered list container. The element must be closed with {@link #end()}.
         */
        public Builder ol() {
            return start(document.createOListElement());
        }

        /**
         * Creates a new unordered list container. The element must be closed with {@link #end()}.
         */
        public Builder ul() {
            return start(document.createUListElement());
        }

        /**
         * Creates a new list container. The element must be closed with {@link #end()}.
         */
        public Builder li() {
            return start(document.createLIElement());
        }

        /**
         * Creates a new anchor container. The element must be closed with {@link #end()}.
         */
        public Builder a() {
            return start(document.createAnchorElement());
        }

        /**
         * Creates a new div container. The element must be closed with {@link #end()}.
         */
        public Builder div() {
            return start(document.createDivElement());
        }

        /**
         * Creates a new span container. The element must be closed with {@link #end()}.
         */
        public Builder span() {
            return start(document.createSpanElement());
        }

        /**
         * Creates the named container. The element must be closed with {@link #end()}.
         */
        public Builder start(String tag) {
            return start(document.createElement(tag));
        }

        /**
         * Adds the given element as new container. The element must be closed with {@link #end()}.
         */
        public Builder start(Element element) {
            elements.push(new ElementInfo(element, true, level));
            level++;
            return this;
        }

        /**
         * Closes the current container element.
         *
         * @throws IllegalStateException if there's no current element or if the closing element is no container.
         */
        public Builder end() {
            assertCurrent();
            if (level == 0) {
                throw new IllegalStateException("Unbalanced element hierarchy");
            }

            List<ElementInfo> children = new ArrayList<>();
            while (elements.peek().level == level) {
                children.add(elements.pop());
            }
            List<ElementInfo> stackOrder = Lists.reverse(children);

            if (!elements.peek().container) {
                throw new IllegalStateException("Closing element " + elements.peek().element + " is no container");
            }
            Element closingElement = elements.peek().element;
            for (ElementInfo child : stackOrder) {
                closingElement.appendChild(child.element);
            }

            level--;
            return this;
        }


        // ------------------------------------------------------ form elements

        /**
         * Creates a new form. The element must be closed with {@link #end()}.
         */
        public Builder form() {
            return start(document.createFormElement());
        }

        /**
         * Creates a new form label. The element must be closed with {@link #end()}.
         */
        public Builder label() {
            return start(document.createLabelElement());
        }

        /**
         * Creates a new button. The element must be closed with {@link #end()}.
         */
        public Builder button() {
            return input(InputType.button);
        }

        /**
         * Creates a new select box. The element must be closed with {@link #end()}.
         */
        public Builder select() {
            return input(InputType.select);
        }

        /**
         * Creates an option to be used inside a select box. The element must be closed with {@link #end()}.
         */
        public Builder option() {
            return start(document.createOptionElement());
        }

        /**
         * Creates a new textarea. The element must be closed with {@link #end()}.
         */
        public Builder textarea() {
            return input(InputType.textarea);
        }

        /**
         * Creates the given input field. See {@link InputType} for details
         * whether a container or simple element is created.
         */
        public Builder input(InputType type) {
            switch (type) {
                case button:
                    start(document.createButtonElement());
                    break;

                case color:
                case checkbox:
                case date:
                case datetime:
                case email:
                case file:
                case hidden:
                case image:
                case month:
                case number:
                case password:
                case radio:
                case range:
                case reset:
                case search:
                case tel:
                case text:
                case time:
                case url:
                case week:
                    InputElement inputElement = document.createInputElement();
                    inputElement.setType(type.name());
                    add(inputElement);
                    break;

                case select:
                    start(document.createSelectElement());
                    break;

                case textarea:
                    start(document.createTextAreaElement());
                    break;
            }
            return this;
        }


        // ------------------------------------------------------ simple elements

        /**
         * Creates and adds the named element. The element is a simple element and must not be closed using {@link
         * #end()}.
         */
        public Builder add(String tag) {
            return add(document.createElement(tag));
        }

        /**
         * Adds the given element. The element is a simple element and must not be closed using {@link #end()}.
         */
        public Builder add(Element element) {
            assertCurrent();
            elements.push(new ElementInfo(element, false, level));
            return this;
        }


        // ------------------------------------------------------ modify current element

        /**
         * Sets the id of the last added element.
         */
        public Builder id(String id) {
            assertCurrent();
            elements.peek().element.setId(id);
            return this;
        }

        /**
         * Sets the title of the last added element.
         */
        public Builder title(String title) {
            assertCurrent();
            elements.peek().element.setTitle(title);
            return this;
        }

        /**
         * Sets the css classes for the last added element.
         */
        public Builder css(String classes) {
            assertCurrent();
            elements.peek().element.setClassName(classes);
            return this;
        }

        /**
         * Adds an attribute to the last added element.
         */
        public Builder attribute(String name, String value) {
            assertCurrent();
            elements.peek().element.setAttribute(name, value);
            return this;
        }

        /**
         * Sets the inner HTML on the last added element.
         */
        public Builder innerHtml(SafeHtml html) {
            assertCurrent();
            elements.peek().element.setInnerHTML(html.asString());
            return this;
        }

        /**
         * Sets the inner text on the last added element.
         */
        public Builder innerText(String text) {
            assertCurrent();
            elements.peek().element.setInnerText(text);
            return this;
        }

        private void assertCurrent() {
            if (elements.isEmpty()) {
                throw new IllegalStateException("No current element");
            }
        }


        // ------------------------------------------------------ event handler

        /**
         * Adds the given event listener on the the last added element.
         */
        public Builder on(EventType type, EventListener listener) {
            assertCurrent();

            Element element = elements.peek().element;
            type.registrar.register(element, listener);
            return this;
        }


        // ------------------------------------------------------ references

        /**
         * Stores a named reference for the last added element.
         */
        public Builder rememberAs(String id) {
            assertCurrent();
            references.put(id, elements.peek().element);
            return this;
        }

        /**
         * Returns the element which was stored under the given id.
         *
         * @throws NoSuchElementException if no element was stored under that id.
         */
        @SuppressWarnings("unchecked")
        public <T extends Element> T referenceFor(String id) {
            if (!references.containsKey(id)) {
                throw new NoSuchElementException("No element reference found for '" + id + "'");
            }
            return (T) references.get(id);
        }


        // ------------------------------------------------------ builder

        /**
         * Builds the element hierarchy and returns the top level element.
         *
         * @throws IllegalStateException If the hierarchy is unbalanced.
         */
        public Element build() {
            if (level != 0 && elements.size() != 1) {
                throw new IllegalStateException("Unbalanced element hierarchy");
            }
            return elements.pop().element;
        }

        /**
         * Builds the element hierarchy and returns the top level element as the specified element type.
         *
         * @param <T> The target element type
         *
         * @throws IllegalStateException If the hierarchy is unbalanced.
         */
        @SuppressWarnings("unchecked")
        public <T extends Element> T buildAs() {
            return (T) build();
        }
    }


    // ------------------------------------------------------ element helper methods

    public static void setVisible(Element element, boolean visible) {
        element.getStyle().setDisplay(visible ? "inherit" : "none");
    }
}