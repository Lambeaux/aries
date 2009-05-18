/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xbean.recipe;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.osgi.service.blueprint.convert.ConversionService;
import org.apache.geronimo.blueprint.utils.ConversionUtils;

/**
 * @version $Rev$ $Date$
 */
public class ArrayRecipe extends AbstractRecipe {
    private final List<Recipe> list;
    private Class typeClass;
    private final EnumSet<Option> options = EnumSet.noneOf(Option.class);
    private final ConversionService conversionService;

    public ArrayRecipe(ConversionService conversionService, Class type) {
        this.conversionService = conversionService;
        this.list = new ArrayList<Recipe>();
        this.typeClass = type;
    }

    public void allow(Option option) {
        options.add(option);
    }

    public void disallow(Option option) {
        options.remove(option);
    }

    public List<Recipe> getNestedRecipes() {
        List<Recipe> nestedRecipes = new ArrayList<Recipe>(list.size());
        for (Object o : list) {
            if (o instanceof Recipe) {
                Recipe recipe = (Recipe) o;
                nestedRecipes.add(recipe);
            }
        }
        return nestedRecipes;
    }

    public List<Recipe> getConstructorRecipes() {
        if (!options.contains(Option.LAZY_ASSIGNMENT)) {
            return getNestedRecipes();
        }
        return Collections.emptyList();
    }

    protected Object internalCreate(Type expectedType, boolean lazyRefAllowed) throws ConstructionException {
        Class type = typeClass != null ? typeClass : Object.class;

        // create array instance
        Object array;
        try {
            array = Array.newInstance(type, list.size());
        } catch (Exception e) {
            throw new ConstructionException("Error while creating array instance: " + type.getName());
        }

        // add to execution context if name is specified
        if (getName() != null) {
            ExecutionContext.getContext().addObject(getName(), array);
        }

        boolean refAllowed = options.contains(Option.LAZY_ASSIGNMENT);

        int index = 0;
        for (Recipe recipe : list) {
            Object value;
            if (recipe != null) {
                try {
                    value = ConversionUtils.convert(recipe.create(Object.class, refAllowed), type, conversionService);
                } catch (Exception e) {
                    throw new ConstructionException("Unable to convert value " + recipe + " to type " + type, e);
                }
            } else {
                value = null;
            }
            
            if (value instanceof Reference) {
                Reference reference = (Reference) value;
                reference.setAction(new UpdateArray(array, index));
            } else {
                //noinspection unchecked
                Array.set(array, index, value);
            }
            index++;
        }
        
        return array;
    }

    private Class getType(Type expectedType) {       
        Class expectedClass = RecipeHelper.toClass(expectedType);
        if (expectedClass.isArray()) {
            expectedClass = expectedClass.getComponentType();
        }
        Class type = expectedClass;
        if (typeClass != null ) {
            type = typeClass;
            // in case where expectedType is a subclass of the assigned type
            if (type.isAssignableFrom(expectedClass)) {
                type = expectedClass;
            }
        }

        return type;
    }

    public void add(Recipe value) {
        list.add(value);
    }

    public void addAll(Collection<Recipe> value) {
        list.addAll(value);
    }

    public void remove(Object value) {
        list.remove(value);
    }

    public void removeAll(Object value) {
        list.remove(value);
    }

    public List<Recipe> getAll() {
        return Collections.unmodifiableList(list);
    }

    private static class UpdateArray implements Reference.Action {
        private final Object array;
        private final int index;

        public UpdateArray(Object array, int index) {
            this.array = array;
            this.index = index;
        }

        @SuppressWarnings({"unchecked"})
        public void onSet(Reference ref) {
            Array.set(array, index, ref.get());
        }
    }
}
