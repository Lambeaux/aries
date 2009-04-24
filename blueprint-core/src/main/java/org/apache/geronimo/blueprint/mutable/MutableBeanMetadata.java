/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.blueprint.mutable;

import org.osgi.service.blueprint.reflect.BeanArgument;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.BeanProperty;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.Target;

/**
 * TODO
 *
 * @author <a href="mailto:dev@geronimo.apache.org">Apache Geronimo Project</a>
 * @version $Rev: 766508 $, $Date: 2009-04-19 22:09:27 +0200 (Sun, 19 Apr 2009) $
 */
public interface MutableBeanMetadata extends BeanMetadata, MutableComponentMetadata {

    void setId(String id);

    void setClassName(String className);

    void setInitMethodName(String initMethodName);

    void setDestroyMethodName(String destroyMethodName);

    void addArgument(BeanArgument argument);

    BeanArgument addArgument(Metadata value, String valueType, int index);

    void removeArgument(BeanArgument argument);

    void addProperty(BeanProperty property);

    BeanProperty addProperty(String name, Metadata value);

    void removeProperty(BeanProperty property);

    void setLazyInit(boolean lazyInit);

    void setFactoryMethodName(String factoryMethodName);

    void setFactoryComponent(Target factoryComponent);

    void setScope(String scope);

    void addExplicitDependency(String dependency);

    void removeExplicitDependency(String dependency);

}
