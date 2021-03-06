package org.jboss.hal.processor.mbui.navigation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.TemplateUtil;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.GroupedForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

/*
 * WARNING! This class is generated. Do not modify.
 */
@Generated("org.jboss.hal.processor.mbui.MbuiViewProcessor")
final class Mbui_NestedView extends NestedView {

    private final Metadata metadata0;
    private final Map<String, Element> handlebarElements;

    @SuppressWarnings("unchecked")
    Mbui_NestedView(MbuiContext mbuiContext) {
        super(mbuiContext);

        AddressTemplate metadata0Template = AddressTemplate.of("/subsystem=foo");
        this.metadata0 = mbuiContext.metadataRegistry().lookup(metadata0Template);
        this.handlebarElements = new HashMap<>();

        form = new ModelNodeForm.Builder<org.jboss.hal.dmr.ModelNode>("form", metadata0)
                .onSave((form, changedValues) -> saveSingletonForm("Form", metadata0Template.resolve(mbuiContext.statementContext()), changedValues))
                .prepareReset(form -> resetSingletonForm("Form", metadata0Template.resolve(mbuiContext.statementContext()), form, metadata0))
                .build();

        navigation = new VerticalNavigation();
        navigation.addPrimary("item", "Main Item", "fa fa-list-ul");

        Elements.Builder subItemBuilder = new Elements.Builder()
                .section()
                .div()
                .innerHtml(SafeHtmlUtils.fromSafeConstant("<h1>Form</h1>"))
                .rememberAs("html0")
                .end()
                .add(form)
                .end();
        Element subItemElement = subItemBuilder.build();
        handlebarElements.put("html0", subItemBuilder.referenceFor("html0"));
        navigation.addSecondary("item", "sub-item", "Sub Item", subItemElement);

        LayoutBuilder layoutBuilder = new LayoutBuilder()
                .row()
                .column()
                .addAll(navigation.panes())
                .end()
                .end();

        registerAttachable(navigation);
        registerAttachable(form);

        Element root = layoutBuilder.build();
        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
    }
}
