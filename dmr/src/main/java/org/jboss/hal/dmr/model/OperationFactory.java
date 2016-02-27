/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.dmr.model;

import com.google.common.base.Strings;
import org.jboss.hal.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class OperationFactory {

    private static final Logger logger = LoggerFactory.getLogger(OperationFactory.class);

    /**
     * Turns a changeset into a composite operation containing {@link org.jboss.hal.dmr.ModelDescriptionConstants#WRITE_ATTRIBUTE_OPERATION}
     * and {@link org.jboss.hal.dmr.ModelDescriptionConstants#UNDEFINE_ATTRIBUTE_OPERATION} operations.
     */
    public Composite fromChangeSet(final ResourceAddress address, final Map<String, Object> changeSet) {

        Operation writeAttribute = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address).build();
        Operation undefineAttribute = new Operation.Builder(UNDEFINE_ATTRIBUTE_OPERATION, address).build();

        List<Operation> operations = new ArrayList<>();
        for (String name : changeSet.keySet()) {
            Operation step;

            Object value = changeSet.get(name);
            if (value == null
                    || (value instanceof String && (Strings.isNullOrEmpty((String) value)))
                    || (value instanceof List && ((List) value).isEmpty())
                    || (value instanceof Map && ((Map) value).isEmpty())) {
                step = undefineAttribute.clone();
                step.get(NAME).set(name);
                operations.add(step);

            } else {
                step = writeAttribute.clone();
                step.get(NAME).set(name);
                ModelNode valueNode = asValueNode(value);
                if (valueNode != null) {
                    step.get(VALUE).set(valueNode);
                    operations.add(step);
                } else {
                    //noinspection HardCodedStringLiteral
                    logger.error("Unsupported type {} when building composite operation for {} from changeset {}",
                            value.getClass(), address, changeSet);
                }
            }
        }

        return new Composite(operations);
    }

    private ModelNode asValueNode(final Object value) {
        Class type = value.getClass();
        ModelNode valueNode = new ModelNode();

        if (String.class == type) {
            String stringValue = (String) value;
            if (stringValue.startsWith("$")) {
                valueNode.setExpression(stringValue);
            } else {
                valueNode.set(stringValue);
            }
        } else if (Boolean.class == type) {
            valueNode.set((Boolean) value);
        } else if (Integer.class == type) {
            valueNode.set((Integer) value);
        } else if (Double.class == type) {
            valueNode.set((Double) value);
        } else if (Long.class == type) {
            valueNode.set((Long) value);
        } else if (Float.class == type) {
            valueNode.set((Float) value);
        } else if (ArrayList.class == type) {
            valueNode.clear();
            List l = (List) value;
            for (Object o : l) { valueNode.add(String.valueOf(o)); }
        } else if (HashMap.class == type) {
            valueNode.clear();
            Map map = (Map) value;
            for (Object k : map.keySet()) { valueNode.add(String.valueOf(k), String.valueOf(map.get(k))); }
        } else {
            valueNode = null;
        }

        return valueNode;
    }
}