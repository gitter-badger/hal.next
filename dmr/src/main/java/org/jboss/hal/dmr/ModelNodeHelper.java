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
package org.jboss.hal.dmr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.EsReturn;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.stream.Collectors.toList;

/**
 * Static helper methods for dealing with {@link ModelNode}s and {@link NamedNode}ss. Some methods accept a path
 * parameter separated by "/" to get a deeply nested data.
 *
 * @author Harald Pehl
 */
@JsType
public final class ModelNodeHelper {

    private static final String ENCODED_SLASH = "%2F";

    @JsIgnore
    public static String encodeValue(String value) {
        return value.replace("/", ENCODED_SLASH);
    }

    @JsIgnore
    public static String decodeValue(String value) {
        return value.replace(ENCODED_SLASH, "/");
    }

    /**
     * Tries to get a deeply nested model node from the specified model node. Nested paths must be separated with "/".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "/"
     *
     * @return The nested node or an empty / undefined model node
     */
    public static ModelNode failSafeGet(final ModelNode modelNode, final String path) {
        ModelNode undefined = new ModelNode();

        if (Strings.emptyToNull(path) != null) {
            Iterable<String> keys = Splitter.on('/').omitEmptyStrings().trimResults().split(path);
            if (!Iterables.isEmpty(keys)) {
                ModelNode context = modelNode;
                for (String key : keys) {
                    String safeKey = decodeValue(key);
                    if (context.hasDefined(safeKey)) {
                        context = context.get(safeKey);
                    } else {
                        context = undefined;
                        break;
                    }
                }
                return context;
            }
        }

        return undefined;
    }

    /**
     * Tries to get a deeply nested boolean value from the specified model node. Nested paths must be separated with
     * "/".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "/"
     *
     * @return the boolean value or false.
     */
    public static boolean failSafeBoolean(final ModelNode modelNode, final String path) {
        ModelNode attribute = failSafeGet(modelNode, path);
        return attribute.isDefined() && attribute.asBoolean();
    }

    @JsIgnore
    public static List<ModelNode> failSafeList(final ModelNode modelNode, final String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asList() : Collections.emptyList();
    }

    @JsIgnore
    public static List<Property> failSafePropertyList(final ModelNode modelNode, final String path) {
        ModelNode result = failSafeGet(modelNode, path);
        return result.isDefined() ? result.asPropertyList() : Collections.emptyList();
    }

    /**
     * Turns a list of properties into a list of named model nodes which contains a {@link
     * ModelDescriptionConstants#NAME} key with the properties name.
     */
    @JsIgnore
    public static List<NamedNode> asNamedNodes(List<Property> properties) {
        return properties.stream().map(NamedNode::new).collect(toList());
    }

    @JsIgnore
    public static <T> T getOrDefault(final ModelNode modelNode, String attribute, Supplier<T> supplier,
            T defaultValue) {
        T result = defaultValue;
        if (modelNode != null && modelNode.hasDefined(attribute)) {
            try {
                result = supplier.get();
            } catch (Throwable t) {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
     * Looks for the specified attribute and tries to convert it to an enum constant using
     * {@code LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNode.get(attribute).asString())}.
     */
    @JsIgnore
    public static <E extends Enum<E>> E asEnumValue(final ModelNode modelNode, final String attribute,
            final Function<String, E> valueOf, final E defaultValue) {
        if (modelNode.hasDefined(attribute)) {
            return asEnumValue(modelNode.get(attribute), valueOf, defaultValue);
        }
        return defaultValue;
    }

    @JsIgnore
    public static <E extends Enum<E>> E asEnumValue(final ModelNode modelNode, final Function<String, E> valueOf,
            final E defaultValue) {
        E value = defaultValue;
        String converted = LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNode.asString());
        try {
            value = valueOf.apply(converted);
        } catch (IllegalArgumentException ignored) {}
        return value;
    }

    /**
     * The reverse operation to {@link #asEnumValue(ModelNode, String, Function, Enum)}.
     */
    @JsIgnore
    public static <E extends Enum<E>> String asAttributeValue(final E enumValue) {
        return UPPER_UNDERSCORE.to(LOWER_HYPHEN, enumValue.name());
    }


    private ModelNodeHelper() {}


    // ------------------------------------------------------ JS methods

    /**
     * Tries to get a deeply nested node array from the specified model node. Nested paths must be separated with "/".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "/"
     *
     * @return the model nodes or an empty array
     */
    @JsMethod(name = "failSafeList")
    @EsReturn("ModelNode[]")
    public static JsArrayOf<ModelNode> jsFailSafeList(final ModelNode modelNode, final String path) {
        return asJsArray(failSafeList(modelNode, path));
    }

    /**
     * Tries to get a deeply nested property array from the specified model node. Nested paths must be separated with
     * "/".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "/"
     *
     * @return the properties or an empty array
     */
    @JsMethod(name = "failSafePropertyList")
    @EsReturn("Property[]")
    public static JsArrayOf<Property> jsFailSafePropertyList(final ModelNode modelNode, final String path) {
        return asJsArray(failSafePropertyList(modelNode, path));
    }

    /**
     * Turns an properties array into an array of names nodes.
     *
     * @param properties The properties
     *
     * @return the array of named nodes
     */
    @JsMethod(name = "asNamedNodes")
    @EsReturn("NamedNode[]")
    public static JsArrayOf<NamedNode> jsAsNamedNodes(@EsParam("Property[]") JsArrayOf<Property> properties) {
        return asJsArray(asNamedNodes(asList(properties)));
    }

    private static <T> JsArrayOf<T> asJsArray(List<T> list) {
        JsArrayOf<T> array = JsArrayOf.create();
        for (T t : list) {
            array.push(t);
        }
        return array;
    }

    @SuppressWarnings("Duplicates")
    private static <T> List<T> asList(JsArrayOf<T> array) {
        if (array != null) {
            List<T> list = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                list.add(array.get(i));
            }
            return list;
        }
        return new ArrayList<>(); // Do not replace with Collections.emptyList()!
    }
}
