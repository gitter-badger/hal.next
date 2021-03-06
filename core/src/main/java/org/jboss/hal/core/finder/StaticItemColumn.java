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
package org.jboss.hal.core.finder;

import java.util.List;

/**
 * Finder column for {@link StaticItem}.
 *
 * @author Harald Pehl
 */
public class StaticItemColumn extends FinderColumn<StaticItem> {

    public static class StaticItemDisplay implements ItemDisplay<StaticItem> {

        private final StaticItem item;

        public StaticItemDisplay(StaticItem item) {
            this.item = item;
        }

        @Override
        public String getId() {
            return item.getId() != null ? item.getId() : ItemDisplay.super.getId();
        }

        @Override
        public String getTitle() {
            return item.getTitle();
        }

        @Override
        public List<ItemAction<StaticItem>> actions() {
            return item.getActions();
        }

        @Override
        public String nextColumn() {
            return item.getNextColumn();
        }
    }

    public StaticItemColumn(final Finder finder, final String id, final String title,
            final List<StaticItem> items) {
        super(new Builder<StaticItem>(finder, id, title)
                .itemRenderer(StaticItemDisplay::new)
                .initialItems(items)
                .onPreview(StaticItem::getPreviewContent));
    }

    public StaticItemColumn(final Finder finder, final String id, final String title,
            final ItemsProvider<StaticItem> itemsProvider) {
        super(new Builder<StaticItem>(finder, id, title)
                .itemRenderer(StaticItemDisplay::new)
                .itemsProvider(itemsProvider)
                .onPreview(StaticItem::getPreviewContent));
    }
}
