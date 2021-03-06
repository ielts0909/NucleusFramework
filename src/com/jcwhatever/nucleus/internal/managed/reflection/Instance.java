/*
 * This file is part of NucleusFramework for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.nucleus.internal.managed.reflection;

import com.jcwhatever.nucleus.mixins.IWrapper;
import com.jcwhatever.nucleus.utils.PreCon;

import javax.annotation.Nullable;

/**
 * Base implementation of an instance wrapper.
 */
abstract class Instance implements IWrapper<Object> {

    private final ReflectedType _type;
    private final Object _instance;

    /**
     * Constructor.
     *
     * @param type      The instances class type represented by a {@link ReflectedType} instance.
     * @param instance  The instance to encapsulate.
     */
    protected Instance(ReflectedType type, Object instance) {
        PreCon.notNull(type);
        PreCon.notNull(instance);

        if (instance instanceof Instance)
            instance = ((Instance) instance)._instance;

        checkInstance(type, instance);

        _type = type;
        _instance = instance;
    }

    /**
     * Get the {@link ReflectedType} of the instance.
     */
    public ReflectedType getReflectedType() {
        return _type;
    }

    /**
     * Invoke a method on the instance using the provided arguments.
     *
     * @param methodName  The name of the method to invoke. If an alias is defined in
     *                    the parent {@link ReflectedType}, the alias can be used.
     * @param arguments   The arguments to pass into the method.
     *
     * @return  Null if the method returns null or void.
     *
     * @see ReflectedType#method
     * @see ReflectedType#methodAlias
     */
    @Nullable
    public Object invoke(String methodName, Object... arguments) {
        return _type.invoke(_instance, methodName, arguments);
    }

    /**
     * Get the encapsulated object instance.
     */
    @Override
    public Object getHandle() {
        return _instance;
    }

    /**
     * Invoked to make sure the instance is valid for the wrapper
     * and reflected type.
     *
     * @param type      The reflected type.
     * @param instance  The instance.
     */
    protected void checkInstance(ReflectedType type, Object instance) {
        if (!type.getHandle().isAssignableFrom(instance.getClass())) {
            throw new RuntimeException("Failed to cast instance to reflected type.");
        }
    }
}
