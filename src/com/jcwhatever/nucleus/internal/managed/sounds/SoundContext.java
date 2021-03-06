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

package com.jcwhatever.nucleus.internal.managed.sounds;

import com.jcwhatever.nucleus.managed.sounds.ISoundContext;
import com.jcwhatever.nucleus.managed.sounds.SoundSettings;
import com.jcwhatever.nucleus.managed.resourcepacks.sounds.types.IResourceSound;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultAgent;
import com.jcwhatever.nucleus.utils.observer.future.IFutureResult;

import org.bukkit.entity.Player;

/**
 * Nucleus implementation of {@link ISoundContext}.
 */
class SoundContext implements ISoundContext {

    private final Player _player;
    private final IResourceSound _sound;
    private final SoundSettings _settings;
    private final FutureResultAgent<ISoundContext> _agent = new FutureResultAgent<>();

    private boolean _isFinished;

    /**
     * Constructor.
     *
     * @param player    The player.
     * @param sound     The sound the player hears.
     */
    SoundContext(Player player, IResourceSound sound, SoundSettings settings) {
        _player = player;
        _sound = sound;
        _settings = settings;
    }

    @Override
    public Player getPlayer() {
        return _player;
    }

    @Override
    public IResourceSound getResourceSound() {
        return _sound;
    }

    @Override
    public SoundSettings getSettings() {
        return _settings;
    }

    @Override
    public boolean isFinished() {
        return _isFinished;
    }

    @Override
    public IFutureResult<ISoundContext> getFuture() {
        return _agent.getFuture();
    }

    IFutureResult<ISoundContext> setFinished() {
        _isFinished = true;

        return _agent.success(this);
    }
}
