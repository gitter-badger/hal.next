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
package org.jboss.hal.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.IsSingleSlot;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;
import org.jboss.hal.client.skeleton.FooterPresenter;
import org.jboss.hal.client.skeleton.HeaderPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.Slots;

/**
 * @author Harald Pehl
 */
public class RootPresenter extends Presenter<RootPresenter.MyView, RootPresenter.MyProxy> implements Slots {

    // @formatter:off
    @ProxyStandard
    interface MyProxy extends Proxy<RootPresenter> {}

    interface MyView extends View, HasPresenter<RootPresenter> {}
    // @formatter:on


    static final IsSingleSlot<HeaderPresenter> SLOT_HEADER_CONTENT = new PermanentSlot<>();
    static final IsSingleSlot<FooterPresenter> SLOT_FOOTER_CONTENT = new PermanentSlot<>();

    private final HeaderPresenter headerPresenter;
    private final FooterPresenter footerPresenter;

    @Inject
    public RootPresenter(EventBus eventBus, MyView view, MyProxy proxy,
            HeaderPresenter headerPresenter, FooterPresenter footerPresenter) {
        super(eventBus, view, proxy, RevealType.Root);
        this.headerPresenter = headerPresenter;
        this.footerPresenter = footerPresenter;
    }

    @Override
    protected void onBind() {
        getView().setPresenter(this);
        setInSlot(SLOT_HEADER_CONTENT, headerPresenter);
        setInSlot(SLOT_FOOTER_CONTENT, footerPresenter);
    }

    void tlcMode() {
        headerPresenter.tlcMode();
    }

    void applicationMode() {
        headerPresenter.applicationMode();
    }
}