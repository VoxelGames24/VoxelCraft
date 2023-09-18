package com.diamantino.voxelcraft.common.blocks;

import com.diamantino.voxelcraft.client.rendering.RenderType;

import java.util.HashMap;

public class Blocks {
    public static final HashMap<Short, Block> blocks = new HashMap<>(0);

    private static short id = 0;

    public static final Block air = registerBlock("air", new SingleBlockTexture(0), RenderType.TRANSPARENT);
    public static final Block stone = registerBlock("stone", new SingleBlockTexture("stone"), RenderType.OPAQUE);
    public static final Block glass = registerBlock("glass", new SingleBlockTexture("glass"), RenderType.TRANSPARENT);

    private static Block registerBlock(String name, IBlockTexture texture, RenderType renderType) {
        short tmpId = id++;
        Block tmpBlock = new Block(tmpId, name, texture, renderType);
        blocks.put(tmpId, tmpBlock);
        return tmpBlock;
    }
}
