package com.diamantino.voxelcraft.common.world;

import com.diamantino.voxelcraft.common.blocks.Block;
import com.diamantino.voxelcraft.common.blocks.BlockPos;
import com.diamantino.voxelcraft.server.world.chunk.Chunk;
import com.diamantino.voxelcraft.server.world.chunk.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class World {
    protected final Map<ChunkPos, Chunk> chunkMap = new HashMap<>();
    private final String name;
    private final WorldSettings settings;

    public World(String name, WorldSettings settings) {
        this.name = name;
        this.settings = settings;
    }

    public ChunkPos getChunkPosForBlockPos(BlockPos blockPos) {
        int chunkX = blockPos.x() / Chunk.sizeX;
        int chunkY = blockPos.y() / Chunk.sizeY;
        int chunkZ = blockPos.z() / Chunk.sizeZ;

        return new ChunkPos(chunkX, chunkY, chunkZ);
    }

    public Chunk getChunkForPos(ChunkPos chunkPos) {
        return chunkMap.containsKey(chunkPos) ? chunkMap.get(chunkPos) : chunkMap.put(chunkPos, new Chunk(this, chunkPos));
    }

    public Chunk getChunkForBlockPos(BlockPos blockPos) {
        return getChunkForPos(getChunkPosForBlockPos(blockPos));
    }

    public Block getBlock(BlockPos pos) {
        int x = pos.x() % Chunk.sizeX;
        int y = pos.y() % Chunk.sizeY;
        int z = pos.z() % Chunk.sizeZ;

        return getChunkForBlockPos(pos).getBlockAt(new BlockPos(x, y, z));
    }

    public void setBlock(Block block, BlockPos pos, boolean regenerateChunkMesh) {
        int x = pos.x() % Chunk.sizeX;
        int y = pos.y() % Chunk.sizeY;
        int z = pos.z() % Chunk.sizeZ;

        getChunkForBlockPos(pos).setBlockAt(block, new BlockPos(x, y, z), regenerateChunkMesh);
    }
}