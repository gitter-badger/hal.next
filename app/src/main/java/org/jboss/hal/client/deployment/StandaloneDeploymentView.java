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
package org.jboss.hal.client.deployment;

import javax.inject.Inject;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.hal.resources.CSS.navTabsHal;

/**
 * @author Harald Pehl
 */
public class StandaloneDeploymentView extends HalViewImpl implements StandaloneDeploymentPresenter.MyView {

    private final boolean supportsReadContent;
    private final Tabs tabs;
    private boolean initialHeightAdjusted;
    private BrowseContentElement browseContent;
    private DeploymentModelElement deploymentModel;
    private StandaloneDeploymentPresenter presenter;

    @Inject
    public StandaloneDeploymentView(final Dispatcher dispatcher, final ModelBrowser modelBrowser,
            final Environment environment, final Resources resources) {
        supportsReadContent = ManagementModel.supportsReadContentFromDeployment(environment.getManagementVersion());
        browseContent = new BrowseContentElement(dispatcher, resources, () -> presenter.reload());
        deploymentModel = new DeploymentModelElement(modelBrowser, resources);

        if (supportsReadContent) {
            tabs = new Tabs()
                    .add(Ids.CONTENT_TAB, resources.constants().content(), browseContent.asElement())
                    .add(Ids.DEPLOYMENT_TAB, Names.MANAGEMENT_MODEL, deploymentModel.asElements());
            tabs.asElement().querySelector("." + navTabsHal).getStyle().setMarginTop(0, PX);
            initElement(tabs.asElement());
        } else {
            tabs = null;
            Element root = Browser.getDocument().createDivElement();
            deploymentModel.asElements().forEach(root::appendChild);
            initElement(root);
        }
    }

    @Override
    public void attach() {
        super.attach();
        if (supportsReadContent) {
            browseContent.attach();

            Element ul = tabs.asElement().querySelector("ul." + navTabsHal);//NON-NLS
            if (ul != null) {
                int tabsHeight = ul.getOffsetHeight() + 5;
                browseContent.setSurroundingHeight(tabsHeight);
                deploymentModel.setSurroundingHeight(tabsHeight);

                // The heights of the elements on the initially hidden tab need to be adjusted once they're visible
                tabs.onShow(Ids.DEPLOYMENT_TAB, () -> {
                    if (!initialHeightAdjusted) {
                        deploymentModel.setSurroundingHeight(tabsHeight);
                        initialHeightAdjusted = true;
                    }
                });
            }
        }
    }

    @Override
    public void setPresenter(final StandaloneDeploymentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void reset() {
        if (supportsReadContent) {
            tabs.showTab(0);
        }
    }

    @Override
    public void update(final ModelNode browseContentResult, final Deployment deployment, final int tab) {
        if (supportsReadContent) {
            browseContent.setContent(deployment.getName(), browseContentResult);
            tabs.showTab(tab);
        }
        deploymentModel.update(deployment, () -> presenter.enable(deployment.getName()));
    }
}
