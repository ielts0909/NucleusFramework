/* This file is part of GenericsLib for Bukkit, licensed under the MIT License (MIT).
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


package com.jcwhatever.bukkit.generic.events;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of event handlers for a specific event.
 */
class EventHandlerCollection {

    private final List<HandlerContainer> _handlers;

    /**
     * Constructor.
     */
    public EventHandlerCollection() {
        _handlers = new ArrayList<HandlerContainer>(20);
    }

    /**
     * Call all event handlers and pass in the provided event instance.
     *
     * @param event  The event instance.
     * @param <T>    The event type which extends {@code AbstractGenericsEvent}.
     */
    <T extends AbstractGenericsEvent> T call(T event) {

        // iterate handlers and call them
        for (HandlerContainer handler : _handlers) {

            // skip handler if the even is cancelled and it is not a watcher.
            if (handler.getPriority() != GenericsEventPriority.WATCHER &&
                    event.isCancelled()) {
                continue;
            }

            // call the handler
            try {
                handler.getHandler().call(event);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return event;
    }

    /**
     * Clear all handlers
     */
    void clear() {
        _handlers.clear();
    }

    /**
     * Add an event handler.
     *
     * @param eventHandler  The event handler to add.
     * @param priority      The handler priority.
     *
     * @return  True if the handler was added, False if it's already added.
     */
    boolean add(EventHandler eventHandler,  GenericsEventPriority priority) {

        // create handler container and make sure it is not already added.
        HandlerContainer handler = new HandlerContainer(null, eventHandler, priority);
        if (_handlers.contains(handler))
            return false;

        // add handler
        _handlers.add(handler);

        // sort handlers by priority
        Collections.sort(_handlers);

        return true;
    }

    /**
     * Add an event handler that was retrieved from a {@code GenericsEventListener}.
     *
     * @param listener    The {@code GenericsEventListener} the handler is from.
     * @param eventClass  The event type the handler is for.
     * @param method      The reflection {@code Method} that points to the handler method.
     * @param annotation  The handler methods {@code GenericsEventHandler} annotation.
     *
     * @throws IllegalAccessException
     */
    void add(final GenericsEventListener listener, final Class<?> eventClass, Method method, GenericsEventHandler annotation)
            throws IllegalAccessException {

        // make the possibly private method accessible
        method.setAccessible(true);

        // get a method handle for faster calls
        final MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);

        // create an event handler that can be used to call the method.
        EventHandler handler = new EventHandler() {
            @Override
            public void call(AbstractGenericsEvent event) {
                try {
                    methodHandle.invoke(listener, eventClass.cast(event));
                    //methodHandle.invoke();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        };

        // add event handler container
        _handlers.add(new HandlerContainer(listener, handler, annotation.priority()));

        // sort event handlers by priority
        Collections.sort(_handlers);
    }

    /**
     * Remove a listener and all event handlers associated with it.
     *
     * @param listener  The listener to remove.
     */
    void removeListener(GenericsEventListener listener) {

        // iterate event handler containers
        Iterator<HandlerContainer> iterator = _handlers.iterator();
        while (iterator.hasNext()) {
            HandlerContainer handler = iterator.next();

            // remove handler if it is from the listener
            if (listener.equals(handler.getListener())) {
                iterator.remove();
            }
        }
    }

    /**
     * Remove a handler by instance.
     *
     * @param eventHandler  The event handler to remove.
     */
    void removeHandler(EventHandler eventHandler) {
        HandlerContainer handler = new HandlerContainer(null, eventHandler, null);
        _handlers.remove(handler);
    }

    /**
     * An event handler container that provides sorting by priority.
     */
    private static class HandlerContainer implements Comparable<HandlerContainer> {

        private final GenericsEventListener _listener;
        private final EventHandler _handler;
        private final GenericsEventPriority _priority;

        /**
         * Constructor.
         */
        public HandlerContainer(GenericsEventListener listener, EventHandler handler, GenericsEventPriority priority) {
            _listener = listener;
            _handler = handler;
            _priority = priority;
        }

        /**
         * Get the listener the event handler is from.
         *
         * @return  Null if the handler was registered without a listener.
         */
        @Nullable
        public GenericsEventListener getListener() {
            return _listener;
        }

        /**
         * Get the encapsulated event handler.
         */
        public EventHandler getHandler() {
            return _handler;
        }

        /**
         * Get the event priority.
         */
        public GenericsEventPriority getPriority() {
            return _priority;
        }

        /**
         * Compare by event priority.
         */
        @Override
        public int compareTo(HandlerContainer o) {
            return Integer.compare(o.getPriority().getSortOrder(), _priority.getSortOrder());
        }

        @Override
        public int hashCode() {
            return _handler.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof HandlerContainer && ((HandlerContainer) obj).getHandler().equals(_handler);
        }
    }

}
