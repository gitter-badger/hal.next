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
package org.jboss.hal.client.runtime.subsystem.datasource;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;

/**
 * @author Harald Pehl
 */
interface AddressTemplates {

    String DATA_SOURCE_ADDRESS = "/{selected.host}/{selected.server}/subsystem=datasources/data-source=*";
    String DATA_SOURCE_POOL_ADDRESS = DATA_SOURCE_ADDRESS + "/statistics=pool";
    String DATA_SOURCE_JDBC_ADDRESS = DATA_SOURCE_ADDRESS + "/statistics=jdbc";
    String XA_DATA_SOURCE_ADDRESS = "/{selected.host}/{selected.server}/subsystem=datasources/xa-data-source=*";
    String XA_DATA_SOURCE_POOL_ADDRESS = XA_DATA_SOURCE_ADDRESS + "/statistics=pool";
    String XA_DATA_SOURCE_JDBC_ADDRESS = XA_DATA_SOURCE_ADDRESS + "/statistics=jdbc";

    AddressTemplate DATA_SOURCE_TEMPLATE = AddressTemplate.of(DATA_SOURCE_ADDRESS);
    AddressTemplate DATA_SOURCE_POOL_TEMPLATE = AddressTemplate.of(DATA_SOURCE_POOL_ADDRESS);
    AddressTemplate DATA_SOURCE_JDBC_TEMPLATE = AddressTemplate.of(DATA_SOURCE_JDBC_ADDRESS);
    AddressTemplate XA_DATA_SOURCE_TEMPLATE = AddressTemplate.of(XA_DATA_SOURCE_ADDRESS);
    AddressTemplate XA_DATA_SOURCE_POOL_TEMPLATE = AddressTemplate.of(XA_DATA_SOURCE_POOL_ADDRESS);
    AddressTemplate XA_DATA_SOURCE_JDBC_TEMPLATE = AddressTemplate.of(XA_DATA_SOURCE_JDBC_ADDRESS);

    AddressTemplate DATA_SOURCE_SUBSYSTEM_TEMPLATE = AddressTemplate
            .of(SELECTED_HOST, SELECTED_SERVER, "subsystem=datasources");
}
