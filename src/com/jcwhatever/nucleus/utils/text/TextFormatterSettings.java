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

package com.jcwhatever.nucleus.utils.text;

import com.google.common.collect.ImmutableMap;
import com.jcwhatever.nucleus.utils.ArrayUtils;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.text.TextFormatter.ITagFormatter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * Settings for {@code TextFormatter}.
 */
public class TextFormatterSettings {

    private final Object _sync = new Object();
    private final Map<String, ITagFormatter> _formatters;
    private volatile char[] _escaped; // characters to escape
    private volatile FormatPolicy _lineReturnPolicy = FormatPolicy.FORMAT;
    private volatile FormatPolicy _unicodePolicy = FormatPolicy.FORMAT;
    private volatile FormatPolicy _colorPolicy = FormatPolicy.FORMAT;
    private volatile FormatPolicy _tagPolicy = FormatPolicy.FORMAT;

    private volatile char _escapedCache;

    /**
     * Constructor.
     *
     * @param formatters  A map of formatters to use.
     */
    public TextFormatterSettings(@Nullable Map<String, ITagFormatter> formatters) {

        ImmutableMap.Builder<String, ITagFormatter> builder = new ImmutableMap.Builder<>();

        if (formatters != null) {
            for (Entry<String, ITagFormatter> entry : formatters.entrySet()) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }

        _formatters = builder.build();
    }

    /**
     * Constructor.
     *
     * @param formatters  The formatters to use.
     */
    public TextFormatterSettings(ITagFormatter... formatters) {
        PreCon.notNull(formatters);

        ImmutableMap.Builder<String, ITagFormatter> builder = new ImmutableMap.Builder<>();

        for (ITagFormatter formatter : formatters) {
            builder.put(formatter.getTag(), formatter);
        }
        _formatters = builder.build();
    }

    /**
     * Constructor.
     *
     * @param source      The {@code TextFormatterSettings} to copy.
     * @param formatters  The formatters to use. Overwrites source settings if duplicate.
     */
    public TextFormatterSettings(TextFormatterSettings source, ITagFormatter... formatters) {
        PreCon.notNull(formatters);

        _escaped = source._escaped != null
                ? Arrays.copyOf(source._escaped, source._escaped.length)
                : null;

        _lineReturnPolicy = source._lineReturnPolicy;
        _unicodePolicy = source._unicodePolicy;
        _colorPolicy = source._colorPolicy;
        _tagPolicy = source._tagPolicy;
        _escapedCache = source._escapedCache;

        ImmutableMap.Builder<String, ITagFormatter> builder = new ImmutableMap.Builder<>();
        for (Entry<String, ITagFormatter> entry : source._formatters.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }

        for (ITagFormatter formatter : formatters) {
            builder.put(formatter.getTag(), formatter);
        }

        _formatters = builder.build();
    }

    /**
     * Constructor.
     *
     * @param source      The {@code TextFormatterSettings} to copy.
     * @param formatters  A map of formatters to use. Overwrites source settings if duplicate.
     */
    public TextFormatterSettings(TextFormatterSettings source,
                                 @Nullable Map<String, ITagFormatter> formatters) {
        PreCon.notNull(source);

        _escaped = source._escaped != null
                ? Arrays.copyOf(source._escaped, source._escaped.length)
                : null;

        _lineReturnPolicy = source._lineReturnPolicy;
        _unicodePolicy = source._unicodePolicy;
        _colorPolicy = source._colorPolicy;
        _tagPolicy = source._tagPolicy;
        _escapedCache = source._escapedCache;

        ImmutableMap.Builder<String, ITagFormatter> builder = new ImmutableMap.Builder<>();
        for (Entry<String, ITagFormatter> entry : source._formatters.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }

        if (formatters != null) {
            for (Entry<String, ITagFormatter> entry : formatters.entrySet()) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }

        _formatters = builder.build();
    }

    /**
     * Determine if a character should be escaped.
     *
     * @param character  The character to check.
     */
    public boolean isEscaped(char character) {
        if (_escaped == null)
            return false;

        if (_escapedCache == character)
            return true;

        synchronized (_sync) {
            for (char escaped : _escaped) {
                if (escaped == character) {
                    _escapedCache = escaped;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get an array of characters that should be escaped.
     */
    public char[] getEscaped() {
        if (_escaped == null)
            return ArrayUtils.EMPTY_CHAR_ARRAY;

        synchronized (_sync) {
            return Arrays.copyOf(_escaped, _escaped.length);
        }
    }

    /**
     * Set the array of characters to escape.
     *
     * @param escaped  The characters that should be escaped.
     */
    public TextFormatterSettings setEscaped(char... escaped) {
        PreCon.notNull(escaped);

        synchronized (_sync) {
            _escaped = Arrays.copyOf(escaped, escaped.length);
        }

        return this;
    }

    /**
     * Determine if line returns sequences should be formatted
     * into actual line returns, ignored, or removed.
     */
    public FormatPolicy getLineReturnPolicy() {
        return _lineReturnPolicy;
    }

    /**
     * Set line return sequences formatted into actual line returns,
     * ignored, or removed.
     *
     * @param policy  The format policy to use.
     *
     * @return  Self for chaining.
     */
    public TextFormatterSettings setLineReturnPolicy(FormatPolicy policy) {
        PreCon.notNull(policy);

        _lineReturnPolicy = policy;

        return this;
    }

    /**
     * Determine if unicode sequences should be formatted
     * into unicode characters.
     */
    public FormatPolicy getUnicodePolicy() {
        return _unicodePolicy;
    }

    /**
     * Set unicode sequences formatted into unicode characters,
     * ignored, or removed.
     *
     * @param policy  The format policy to use.
     *
     * @return  Self for chaining.
     */
    public TextFormatterSettings setUnicodePolicy(FormatPolicy policy) {
        PreCon.notNull(policy);

        _unicodePolicy = policy;

        return this;
    }

    /**
     * Determine if color tags should be formatted into color codes,
     * ignored, or removed.
     */
    public FormatPolicy getColorPolicy() {
        return _colorPolicy;
    }

    /**
     * Set color tags formatted into color codes,
     * ignored, or removed.
     *
     * @param policy  The format policy to use.
     *
     * @return  Self for chaining.
     */
    public TextFormatterSettings setColorPolicy(FormatPolicy policy) {
        PreCon.notNull(policy);

        _colorPolicy = policy;

        return this;
    }

    /**
     * Determine if tags (excluding color tags) should be formatted,
     * ignored, or removed.
     */
    public FormatPolicy getTagPolicy() {
        return _tagPolicy;
    }

    /**
     * Set tags (excluding color tags) formatted, ignored or removed.
     *
     * @param policy  The format policy to use.
     *
     * @return  Self for chaining.
     */
    public TextFormatterSettings getTagPolicy(FormatPolicy policy) {
        PreCon.notNull(policy);

        _tagPolicy = policy;

        return this;
    }

    /**
     * Get all default tag formatters.
     */
    public Collection<ITagFormatter> getFormatters() {
        return Collections.unmodifiableCollection(_formatters.values());
    }

    /**
     * Get a default tag formatter by case sensitive tag.
     *
     * @param tag  The tag text.
     */
    public ITagFormatter getFormatter(String tag) {
        return _formatters.get(tag);
    }

    Map<String, ITagFormatter> getFormatMap() {
        return _formatters;
    }

    /**
     * Specifies how a formatting aspects are handled.
     */
    public enum FormatPolicy {
        /**
         * Formatting component is formatted.
         */
        FORMAT,
        /**
         * Ignored the formatting component, left as is.
         */
        IGNORE,
        /**
         * Remove the formatting component.
         */
        REMOVE
    }
}