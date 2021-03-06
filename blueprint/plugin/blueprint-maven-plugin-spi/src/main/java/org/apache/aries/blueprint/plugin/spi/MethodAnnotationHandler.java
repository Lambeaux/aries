/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.blueprint.plugin.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Annotation A on method could write custom XML in blueprint or add bean to blueprint context
 */
public interface MethodAnnotationHandler<A extends Annotation> extends AnnotationHandler<A> {
    /**
     * Handle annotations A on methods
     * @param clazz class which contains annotated methods
     * @param methods methods annotated with annotation A
     * @param contextEnricher context enricher
     * @param beanEnricher bean enricher
     */
    void handleMethodAnnotation(Class<?> clazz, List<Method> methods, ContextEnricher contextEnricher, BeanEnricher beanEnricher);
}
