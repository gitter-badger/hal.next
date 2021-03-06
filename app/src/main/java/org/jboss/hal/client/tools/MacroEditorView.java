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
package org.jboss.hal.client.tools;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Iterables;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.max;
import static java.util.Collections.singletonList;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class MacroEditorView extends HalViewImpl implements MacroEditorPresenter.MyView {

    private static final String COPY_TO_CLIPBOARD_ELEMENT = "copyToClipboardElement";
    private static final String PLAY_ACTION = "play";
    private static final String RENAME_ACTION = "rename";
    private static final String REMOVE_ACTION = "remove";
    private static final int MIN_HEIGHT = 70;

    private final Resources resources;
    private final EmptyState empty;
    private final ListView<Macro> macroList;
    private final AceEditor editor;
    private final Element copyToClipboard;
    private final Iterable<Element> elements;
    private MacroEditorPresenter presenter;

    @Inject
    public MacroEditorView(Resources resources) {
        this.resources = resources;

        empty = new EmptyState.Builder(resources.constants().noMacros())
                .icon(CSS.fontAwesome("dot-circle-o"))
                .description(resources.messages().noMacrosDescription(resources.constants().startMacro()))
                .build();
        empty.asElement().getClassList().add(noMacros);

        macroList = new ListView<>(Ids.MACRO_LIST, macro -> new ItemDisplay<Macro>() {
            @Override
            public String getTitle() {
                return macro.getName();
            }

            @Override
            public String getDescription() {
                return macro.getDescription();
            }

            @Override
            public boolean stacked() {
                return true;
            }

            @Override
            public List<ItemAction<Macro>> actions() {
                return Arrays.asList(
                        new ItemAction<Macro>(PLAY_ACTION, resources.constants().play(),
                                macro -> presenter.play(macro)),
                        new ItemAction<Macro>(RENAME_ACTION, resources.constants().rename(),
                                macro -> presenter.rename(macro)),
                        new ItemAction<Macro>(REMOVE_ACTION, resources.constants().remove(),
                                macro -> DialogFactory.showConfirmation(
                                        resources.messages().removeConfirmationTitle(macro.getName()),
                                        resources.messages().removeConfirmationQuestion(macro.getName()),
                                        () -> presenter.remove(macro))));
            }
        });
        macroList.onSelect(this::loadMacro);
        macroList.asElement().getClassList().add(CSS.macroList);

        Options editorOptions = new Options();
        editorOptions.readOnly = true;
        editorOptions.showGutter = true;
        editorOptions.showLineNumbers = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.MACRO_EDITOR, editorOptions);

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(macroEditor)
                .button()
                    .css(btn, btnDefault, copy)
                    .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                    .data(UIConstants.PLACEMENT, "left") //NON-NLS
                    .title(resources.constants().copyToClipboard())
                    .rememberAs(COPY_TO_CLIPBOARD_ELEMENT)
                        .span().css(fontAwesome("clipboard")).end()
                .end()
                .add(editor)
            .end();
        // @formatter:on

        Element editorContainer = builder.build();
        copyToClipboard = builder.referenceFor(COPY_TO_CLIPBOARD_ELEMENT);
        Clipboard clipboard = new Clipboard(copyToClipboard);
        clipboard.onCopy(event -> copyToClipboard(event.client));

        // @formatter:off
        elements = new LayoutBuilder()
            .row()
                .column(4).add(macroList.asElement()).end()
                .column(8).add(editorContainer).end()
            .end()
        .elements();
        // @formatter:on

        registerAttachable(editor);
        initElements(Iterables.concat(singletonList(empty.asElement()), elements));
    }

    @Override
    public void attach() {
        super.attach();
        adjustHeight();
        adjustEditorHeight();
        Browser.getWindow().setOnresize(event -> adjustEditorHeight());
    }

    private void adjustHeight() {
        int offset = Skeleton.applicationOffset() + 2 * MARGIN_BIG + 1;
        macroList.asElement().getStyle().setHeight(vh(offset));
    }

    private void adjustEditorHeight() {
        int height = max(Skeleton.applicationHeight() - 2 * MARGIN_BIG - 1, MIN_HEIGHT);
        editor.asElement().getStyle().setHeight(height, PX);
        editor.getEditor().resize();
    }

    @Override
    public void setPresenter(final MacroEditorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void empty() {
        Elements.setVisible(empty.asElement(), true);
        for (Element element : elements) {
            Elements.setVisible(element, false);
        }
    }

    @Override
    public void setMacros(Iterable<Macro> macros) {
        Elements.setVisible(empty.asElement(), false);
        for (Element element : elements) {
            Elements.setVisible(element, true);
        }
        macroList.setItems(macros);
    }

    @Override
    public void selectMacro(final Macro macro) {
        macroList.selectItem(macro);
    }

    @Override
    public void enableMacro(final Macro macro) {
        macroList.enableAction(macro, PLAY_ACTION);
        macroList.enableAction(macro, RENAME_ACTION);
        macroList.enableAction(macro, REMOVE_ACTION);
    }

    @Override
    public void disableMacro(final Macro macro) {
        macroList.disableAction(macro, PLAY_ACTION);
        macroList.disableAction(macro, RENAME_ACTION);
        macroList.disableAction(macro, REMOVE_ACTION);
    }

    private void loadMacro(Macro macro) {
        editor.getEditor().getSession().setValue(macro.asCli());
    }

    private void copyToClipboard(Clipboard clipboard) {
        if (macroList.selectedItem() != null) {
            clipboard.setText(macroList.selectedItem().asCli());
            Tooltip tooltip = Tooltip.element(copyToClipboard);
            tooltip.hide()
                    .setTitle(resources.constants().copied())
                    .show()
                    .onHide(() -> tooltip.setTitle(resources.constants().copyToClipboard()));
            Browser.getWindow().setTimeout(tooltip::hide, 1000);
        }
    }
}
