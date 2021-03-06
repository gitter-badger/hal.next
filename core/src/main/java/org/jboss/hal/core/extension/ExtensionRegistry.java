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
package org.jboss.hal.core.extension;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.html.HeadElement;
import elemental.html.LinkElement;
import elemental.html.ScriptElement;
import elemental.js.util.JsArrayOf;
import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonObject;
import elemental.xml.XMLHttpRequest;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.core.ApplicationReadyEvent;
import org.jboss.hal.core.ApplicationReadyEvent.ApplicationReadyHandler;
import org.jboss.hal.core.extension.Extension.Point;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.EsParam;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.hidden;

/**
 * Registry to manage HAL extensions written in JavaScript.
 *
 * @author Harald Pehl
 */
@JsType(namespace = "hal.core")
public class ExtensionRegistry implements ApplicationReadyHandler {

    @FunctionalInterface
    public interface MetadataCallback {

        void result(int status, JsonObject json);
    }


    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExtensionRegistry.class);

    private final Queue<Extension> queue;
    private final Set<String> extensions;
    private boolean ready;
    private Element headerDropdown;
    private Element headerExtensions;
    private Element footerDropdown;
    private Element footerExtensions;

    @Inject
    @JsIgnore
    public ExtensionRegistry(final EventBus eventBus) {
        this.queue = new LinkedList<>();
        this.extensions = new HashSet<>();
        eventBus.addHandler(ApplicationReadyEvent.getType(), this);
    }

    @JsIgnore
    public void verifyMetadata(final String url, final MetadataCallback metadataCallback) {
        SafeUri safeUrl = UriUtils.fromString(url);
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
        xhr.setOnreadystatechange(event -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                if (xhr.getStatus() >= 200 && xhr.getStatus() < 400) {
                    String responseText = xhr.getResponseText();
                    if (Strings.isNullOrEmpty(responseText)) {
                        metadataCallback.result(415, null); // 415 - Unsupported Media Type
                    } else {
                        JsonObject extensionJson;
                        try {
                            extensionJson = Json.parse(responseText);
                            metadataCallback.result(xhr.getStatus(), extensionJson);
                        } catch (JsonException e) {
                            logger.error("Unable to parse {} as JSON", safeUrl.asString());
                            metadataCallback.result(500, null);
                        }
                    }

                } else {
                    metadataCallback.result(xhr.getStatus(), null);
                }
            }
        });
        xhr.addEventListener("error", event -> metadataCallback.result(503, null), false); //NON-NLS
        xhr.open(GET.name(), safeUrl.asString(), true);
        xhr.send();
    }

    @JsIgnore
    public boolean verifyScript(final String script) {
        return Browser.getDocument().getHead().querySelector("script[src='" + script + "']") != null; //NON-NLS
    }

    @JsIgnore
    public void inject(final String script, final List<String> stylesheets) {
        jsInject(script, JsHelper.asJsArray(stylesheets));
    }

    @Override
    @JsIgnore
    @SuppressWarnings("HardCodedStringLiteral")
    public void onApplicationReady(final ApplicationReadyEvent event) {
        ready = true;
        headerDropdown = Browser.getDocument().getElementById(Ids.HEADER_EXTENSIONS_DROPDOWN);
        headerExtensions = Browser.getDocument().getElementById(Ids.HEADER_EXTENSIONS);
        footerDropdown = Browser.getDocument().getElementById(Ids.FOOTER_EXTENSIONS_DROPDOWN);
        footerExtensions = Browser.getDocument().getElementById(Ids.FOOTER_EXTENSIONS);

        while (!queue.isEmpty()) {
            failSafeApply(queue.poll());
        }
    }

    /**
     * Registers an extension. Use this method to register your extension.
     * <p>
     * If the extension is already registered, this method will do nothing.
     *
     * @param extension the extension to register.
     */
    public void register(final Extension extension) {
        if (!ready) {
            queue.offer(extension);
        } else {
            failSafeApply(extension);
        }
    }

    private void failSafeApply(Extension extension) {
        if (ready && headerDropdown != null && headerExtensions != null &&
                footerDropdown != null && footerExtensions != null) {
            if (extensions.contains(extension.name)) {
                logger.warn("Extension {} already registered", extension.name);
            } else {
                apply(extension);
            }
        } else {
            logger.error("Cannot register extension {}: Console not ready", extension.name);
        }
    }

    private void apply(Extension extension) {
        extensions.add(extension.name);
        if (extension.point == Extension.Point.HEADER || extension.point == Point.FOOTER) {
            // @formatter:off
            Element li = new Elements.Builder()
                .li()
                    .a()
                        .id(extension.name)
                        .css(clickable)
                        .textContent(extension.title)
                        .on(click, event -> extension.entryPoint.execute())
                    .end()
                .end()
            .build();
            // @formatter:on

            Element ul;
            Element dropdown;
            if (extension.point == Point.HEADER) {
                dropdown = headerDropdown;
                ul = headerExtensions;
            } else {
                dropdown = footerDropdown;
                ul = footerExtensions;
            }
            ul.appendChild(li);
            dropdown.getClassList().remove(hidden);

        } else if (extension.point == Extension.Point.FINDER_ITEM) {
            // TODO Handle finder item extensions
        }
    }


    // ------------------------------------------------------ JS methods

    /**
     * Injects the script and stylesheets of an extension. This method is used during development. Normally you don't
     * have to call this method.
     *
     * @param script the extension's script.
     * @param stylesheets an optional list of stylesheets.
     */
    @JsMethod(name = "inject")
    @SuppressWarnings("HardCodedStringLiteral")
    public void jsInject(final String script, @EsParam("string[]") final JsArrayOf<String> stylesheets) {
        Document document = Browser.getDocument();
        HeadElement head = document.getHead();

        if (stylesheets != null && !stylesheets.isEmpty()) {
            for (int i = 0; i < stylesheets.length(); i++) {
                LinkElement linkElement = document.createLinkElement();
                linkElement.setRel("stylesheet"); //NON-NLS
                linkElement.setHref(stylesheets.get(i));
                head.appendChild(linkElement);
            }
        }
        ScriptElement scriptElement = document.createScriptElement();
        scriptElement.setAsync(true);
        scriptElement.setSrc(script);
        head.appendChild(scriptElement);
    }
}
