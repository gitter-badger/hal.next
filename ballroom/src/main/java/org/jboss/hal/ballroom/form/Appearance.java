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
package org.jboss.hal.ballroom.form;

import javax.annotation.Nullable;

import com.google.gwt.user.client.ui.Focusable;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;

import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.helpBlock;
import static org.jboss.hal.resources.CSS.inputGroup;
import static org.jboss.hal.resources.CSS.inputGroupAddon;

/**
 * Encapsulates the L&F of a {@linkplain FormItem form item} for a given {@linkplain Form.State form state}. The
 * appearance must include the label and the input element. Appearances should not hold state (except UI state).
 * State should be kept in the form item only not in its appearance(s).
 * <p>
 * An appearance can apply / unapply {@linkplain Decoration decorations}.
 *
 * @author Harald Pehl
 */
interface Appearance<T> extends IsElement, Attachable, Focusable {

    /**
     * Used as a {@code data-} attribute in the root element of the appearances.
     */
    String FORM_ITEM_GROUP = "formItemGroup";


    // ------------------------------------------------------ static builder methods

    static Element inputGroup() {
        return new Elements.Builder().div().css(inputGroup).end().build();
    }

    static Element helpBlock() {
        return new Elements.Builder().span().css(helpBlock).end().build();
    }

    static Element restrictedMarker() {
        return new Elements.Builder()
                .span().css(inputGroupAddon).start("i").css(fontAwesome("lock")).end().end()
                .build();
    }

    static Element hintMarker() {
        return new Elements.Builder().span().css(inputGroupAddon).end().build();
    }


    // ------------------------------------------------------ API

    void showValue(T value);

    default void showExpression(String expression) {
        // noop
    }

    default String asString(T value) {
        return String.valueOf(value);
    }

    void clearValue();

    String getId();

    void setId(String id);

    void setName(final String name);

    void setLabel(String label);

    default void apply(Decoration decoration) {
        apply(decoration, null);
    }

    <C> void apply(Decoration decoration, @Nullable C context);

    void unapply(Decoration decoration);
}
