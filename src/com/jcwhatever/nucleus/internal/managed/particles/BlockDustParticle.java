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

package com.jcwhatever.nucleus.internal.managed.particles;

import com.jcwhatever.nucleus.managed.particles.ParticleType;
import com.jcwhatever.nucleus.managed.particles.types.IBlockDustParticle;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

/**
 * Implementation of {@link IBlockDustParticle}.
 */
class BlockDustParticle extends AbstractDirectionalParticle implements IBlockDustParticle {

    private ParticleType<IBlockDustParticle> _type;
    private MaterialData _data;

    /**
     * Constructor.
     */
    BlockDustParticle() {
        super(ParticleType.BLOCK_DUST);

        setMaterialData(new MaterialData(Material.STONE));
    }

    @Override
    public ParticleType getType() {
        return _type;
    }

    @Override
    public MaterialData getMaterialData() {
        return _data;
    }

    @Override
    public void setMaterialData(MaterialData materialData) {
        _data = materialData;
        _type = new ParticleType<>("BLOCK_DUST", IBlockDustParticle.class,
                materialData.getItemType().getId(), materialData.getData());
    }
}