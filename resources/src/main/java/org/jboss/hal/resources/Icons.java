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
package org.jboss.hal.resources;

import elemental.client.Browser;
import elemental.dom.Element;

import static org.jboss.hal.resources.CSS.*;

/**
 * Collection of common icons
 *
 * @author Harald Pehl
 */
public interface Icons {

    // ------------------------------------------------------ icon css classes

    String CHECK = fontAwesome("check");
    String DISABLED = fontAwesome("ban");
    String ERROR = pfIcon(errorCircleO);
    String INFO = pfIcon(info);
    String LOCK = fontAwesome("lock");
    String NOT = fontAwesome("times");
    String OK = pfIcon(ok);
    String PAUSE = fontAwesome(pauseCircle) + " " + blue;
    String STOPPED = fontAwesome(stopCircleO) + " " + grey;
    String WARNING = pfIcon(warningTriangleO);
    String UNKNOWN = fontAwesome(questionsCircleO);

    static String flag(boolean value) {
        return value ? CHECK : NOT;
    }


    // ------------------------------------------------------ icon elements

    static Element disabled() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(DISABLED);
        return icon;
    }

    static Element error() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(ERROR);
        return icon;
    }

    static Element info() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(INFO);
        return icon;
    }

    static Element lock() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(LOCK);
        return icon;
    }

    static Element ok() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(OK);
        return icon;
    }

    static Element pause() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(PAUSE);
        return icon;
    }

    static Element stopped() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(STOPPED);
        return icon;
    }

    static Element warning() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(WARNING);
        return icon;
    }

    static Element unknown() {
        Element icon = Browser.getDocument().createSpanElement();
        icon.setClassName(UNKNOWN);
        return icon;
    }
}
