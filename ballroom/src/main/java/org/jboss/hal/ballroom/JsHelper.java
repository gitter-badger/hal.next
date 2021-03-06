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
package org.jboss.hal.ballroom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.js.util.JsArrayOf;
import elemental.js.util.JsMapFromStringTo;
import org.jboss.hal.ballroom.dragndrop.DragEvent;
import org.jboss.hal.ballroom.dragndrop.DropEventHandler;

import static org.jboss.hal.resources.CSS.ondrag;

/**
 * @author Harald Pehl
 */
public final class JsHelper {

    @SuppressWarnings("Duplicates")
    public static <T> List<T> asList(JsArrayOf<T> array) {
        if (array != null) {
            List<T> list = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                list.add(array.get(i));
            }
            return list;
        }
        return new ArrayList<>(); // Do not replace with Collections.emptyList()!
    }

    public static <T> JsArrayOf<T> asJsArray(List<T> list) {
        JsArrayOf<T> array = JsArrayOf.create();
        for (T t : list) {
            array.push(t);
        }
        return array;
    }

    public static Map<String, Object> asMap(JsMapFromStringTo<Object> jsMap) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < jsMap.keys().length(); i++) {
            String key = jsMap.keys().get(i);
            map.put(key, jsMap.get(key));
        }
        return map;
    }

    public static JsMapFromStringTo<Object> asJsMap(Map<String, Object> map) {
        JsMapFromStringTo<Object> jsMap = JsMapFromStringTo.create();
        map.forEach(jsMap::put);
        return jsMap;
    }

    public static native boolean supportsAdvancedUpload() /*-{
        var div = document.createElement('div');
        return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) &&
            'FormData' in window && 'FileReader' in window;
    }-*/;

    public static void addDropHandler(Element element, DropEventHandler handler) {
        EventListener noop = event -> {
            event.preventDefault();
            event.stopPropagation();
        };
        EventListener addDragIndicator = event -> {
            noop.handleEvent(event);
            element.getClassList().add(ondrag);
        };
        EventListener removeDragIndicator = event -> {
            noop.handleEvent(event);
            element.getClassList().remove(ondrag);
        };

        element.setOndrag(noop);
        element.setOndragstart(noop);

        element.setOndragenter(addDragIndicator);
        element.setOndragover(addDragIndicator);

        element.setOndragleave(removeDragIndicator);
        element.setOndragend(removeDragIndicator);

        element.setOndrop(event -> {
            noop.handleEvent(event);
            removeDragIndicator.handleEvent(event);

            DragEvent dragEvent = (DragEvent) event;
            handler.onDrop(dragEvent);
        });
    }

    private JsHelper() {
    }
}
