package com.diamantino.voxelcraft.server.world.chunk;

import com.diamantino.voxelcraft.common.blocks.Block;
import com.diamantino.voxelcraft.common.blocks.BlockPos;
import com.diamantino.voxelcraft.common.blocks.Blocks;

public class ChunkData {
    private final Chunk chunk;
    private final IChunkLayer[] chunkLayers = new IChunkLayer[Chunk.sizeY];

    public ChunkData(Chunk chunk) {
        this.chunk = chunk;
    }

    public Block getBlock(BlockPos localPos) {
        if (localPos.y() < 0 || localPos.y() >= Chunk.sizeY)
            return Blocks.air;

        IChunkLayer layer = chunkLayers[localPos.y()];
        return layer != null ? layer.getBlock(localPos.x(), localPos.z()) : Blocks.air;
    }

    public short getBlockId(BlockPos localPos) {
        if (localPos.y() < 0 || localPos.y() >= Chunk.sizeY)
            return Blocks.air.id;

        IChunkLayer layer = chunkLayers[localPos.y()];
        return layer != null ? layer.getBlockId(localPos.x(), localPos.z()) : Blocks.air.id;
    }

    public void setLayer(IChunkLayer layer, int y) {
        chunkLayers[y] = layer;
    }

    public IChunkLayer getLayer(int y) {
        return chunkLayers[y];
    }

    // TODO: Maybe optimize?
    public void setBlock(Block block, BlockPos localPos) {
        if (chunkLayers[localPos.y()] != null) {
            if (chunkLayers[localPos.y()] instanceof SingleBlockChunkLayer singleLayer) {
                if (singleLayer.getBlock() != block) {
                    chunkLayers[localPos.y()] = new ChunkLayer(singleLayer);

                    chunkLayers[localPos.y()].setBlock(block, localPos.x(), localPos.z());
                }
            } else {
                chunkLayers[localPos.y()].setBlock(block, localPos.x(), localPos.z());

                tryCompactLayer(localPos.y());
            }
        } else {
            chunkLayers[localPos.y()] = new SingleBlockChunkLayer(chunk, block, localPos.y());
        }
    }

    public IChunkLayer getChunkLayer(int localY) {
        return chunkLayers[localY];
    }

    public void setChunkLayer(IChunkLayer layer, int localY) {
        chunkLayers[localY] = layer;
    }

    public boolean tryCompactLayer(int localY) {
        boolean changed = false;

        IChunkLayer layer = chunkLayers[localY];
        short prevId = -1;

        if (layer instanceof ChunkLayer chunkLayer) {
            boolean sameBlock = true;

            for (short id : chunkLayer.getBlockList()) {
                if (prevId != -1) {
                    if (id != prevId) {
                        sameBlock = false;
                        break;
                    }
                } else {
                    prevId = id;
                }
            }

            if (sameBlock) {
                chunkLayers[localY] = new SingleBlockChunkLayer(this.chunk, Blocks.blocks.get(prevId), localY);
                changed = true;
            }
        }

        return changed;
    }
}