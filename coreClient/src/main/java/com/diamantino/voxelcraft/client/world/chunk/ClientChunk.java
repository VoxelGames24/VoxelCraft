package com.diamantino.voxelcraft.client.world.chunk;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.diamantino.voxelcraft.client.blocks.ClientBlock;
import com.diamantino.voxelcraft.client.blocks.ClientBlocks;
import com.diamantino.voxelcraft.client.rendering.RenderType;
import com.diamantino.voxelcraft.client.shaders.Shaders;
import com.diamantino.voxelcraft.client.utils.ClientLoadingUtils;
import com.diamantino.voxelcraft.client.world.ClientWorld;
import com.diamantino.voxelcraft.common.blocks.Block;
import com.diamantino.voxelcraft.common.blocks.BlockPos;
import com.diamantino.voxelcraft.common.registration.Blocks;
import com.diamantino.voxelcraft.common.world.World;
import com.diamantino.voxelcraft.common.world.chunk.Chunk;
import com.diamantino.voxelcraft.common.world.chunk.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client-side chunk class.
 *
 * @author Diamantino
 */
public class ClientChunk extends Chunk {
    /**
     * The mesh of the chunk.
     */
    private Mesh chunkMesh;

    /**
     * The mesh builder of the chunk.
     */
    private final MeshBuilder builder;

    /**
     * Chunk class constructor.
     * @param world The world instance.
     * @param chunkPos The position of the chunk
     */
    public ClientChunk(World world, ChunkPos chunkPos) {
        super(world, chunkPos);

        this.builder = new MeshBuilder();
    }

    /**
     * Renders the mesh of the chunk.
     */
    public void render() {
        if (chunkMesh == null) return;

        chunkMesh.render(Shaders.coreShader, GL20.GL_TRIANGLES);
    }

    /**
     * Dispose the chunk mesh.
     */
    public void dispose() {
        chunkMesh.dispose();
    }

    /**
     * Regenerate the chunk mesh.
     */
    public void regenerateMesh() {
        if (chunkMesh != null) chunkMesh.dispose();

        builder.begin(new VertexAttributes(
            new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        ), GL20.GL_TRIANGLES);

        AtomicInteger currVertex = new AtomicInteger();

        Map<BlockPos, Block> transparentBlocks = new HashMap<>();

        // TODO: Use world to get block from nearby chunks
        // TODO: Optimize (Greedy mesh)
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Block currBlock = chunkBlockData.getBlock(new BlockPos(x, y, z));

                    ClientBlock clientBlock = ClientBlocks.clientBlocks.get(currBlock.name);

                    if (currBlock != Blocks.air.getBlockInstance()) {
                        if (clientBlock.renderType == RenderType.OPAQUE) {
                            currVertex.set(generateFaces(currBlock, x + (chunkPos.x() * sizeX), y + (chunkPos.y() * sizeY), z + (chunkPos.z() * sizeZ), currVertex.get()));
                        } else {
                            transparentBlocks.put(new BlockPos(x, y, z), currBlock);
                        }
                    }
                }
            }
        }

        transparentBlocks.forEach((pos, currBlock) -> {
            currVertex.set(generateFaces(currBlock, pos.x() + (chunkPos.x() * sizeX), pos.y() + (chunkPos.y() * sizeY), pos.z() + (chunkPos.z() * sizeZ), currVertex.get()));
        });

        chunkMesh = builder.end();
    }

    /**
     * Generate the block faces.
     * @param currBlock The block used to generate the faces.
     * @param x The block's X position.
     * @param y The block's Y position.
     * @param z The block's Z position.
     * @param currVertex The current vertex index.
     *
     * @return The updated vertex index.
     */
    private int generateFaces(Block currBlock, int x, int y, int z, int currVertex) {
        ClientWorld clientWorld = (ClientWorld) world;
        TextureAtlas blockAtlas = ClientLoadingUtils.getAtlas("blocks");

        ClientBlock clientBlock = ClientBlocks.clientBlocks.get(currBlock.name);

        // Front Face
        if ((clientWorld.getBlock(new BlockPos(x, y, z - 1)) == Blocks.air.getBlockInstance()) || (clientBlock.renderType == RenderType.OPAQUE && clientWorld.getClientBlock(new BlockPos(x, y, z - 1)).renderType != RenderType.OPAQUE)) {
            TextureRegion currFaceRegion = blockAtlas.getRegions().get(clientBlock.texture.getFrontTexIndex());
            builder.vertex(new Vector3(x - 0.5f, y - 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV2()));
            builder.vertex(new Vector3(x - 0.5f, y + 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x + 0.5f, y + 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x + 0.5f, y - 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV2()));
            builder.index((short) (currVertex + 3), (short) (currVertex + 1), (short) currVertex);
            builder.index((short) (currVertex + 3), (short) (currVertex + 2), (short) (currVertex + 1));

            currVertex += 4;
        }

        // Right Face
        if ((clientWorld.getBlock(new BlockPos(x + 1, y, z)) == Blocks.air.getBlockInstance()) || (clientBlock.renderType == RenderType.OPAQUE && clientWorld.getClientBlock(new BlockPos(x + 1, y, z)).renderType != RenderType.OPAQUE)) {
            TextureRegion currFaceRegion = blockAtlas.getRegions().get(clientBlock.texture.getRightTexIndex());
            builder.vertex(new Vector3(x + 0.5f, y - 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV2()));
            builder.vertex(new Vector3(x + 0.5f, y + 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x + 0.5f, y + 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x + 0.5f, y - 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV2()));
            builder.index((short) (currVertex + 3), (short) (currVertex + 1), (short) currVertex);
            builder.index((short) (currVertex + 3), (short) (currVertex + 2), (short) (currVertex + 1));

            currVertex += 4;
        }

        // Back Face
        if ((clientWorld.getBlock(new BlockPos(x, y, z + 1)) == Blocks.air.getBlockInstance()) || (clientBlock.renderType == RenderType.OPAQUE && clientWorld.getClientBlock(new BlockPos(x, y, z + 1)).renderType != RenderType.OPAQUE)) {
            TextureRegion currFaceRegion = blockAtlas.getRegions().get(clientBlock.texture.getBackTexIndex());
            builder.vertex(new Vector3(x + 0.5f, y - 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV2()));
            builder.vertex(new Vector3(x + 0.5f, y + 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x - 0.5f, y + 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x - 0.5f, y - 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV2()));
            builder.index((short) (currVertex + 3), (short) (currVertex + 1), (short) currVertex);
            builder.index((short) (currVertex + 3), (short) (currVertex + 2), (short) (currVertex + 1));

            currVertex += 4;
        }

        // Left Face
        if ((clientWorld.getBlock(new BlockPos(x - 1, y, z)) == Blocks.air.getBlockInstance()) || (clientBlock.renderType == RenderType.OPAQUE && clientWorld.getClientBlock(new BlockPos(x - 1, y, z)).renderType != RenderType.OPAQUE)) {
            TextureRegion currFaceRegion = blockAtlas.getRegions().get(clientBlock.texture.getLeftTexIndex());
            builder.vertex(new Vector3(x - 0.5f, y - 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV2()));
            builder.vertex(new Vector3(x - 0.5f, y + 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x - 0.5f, y + 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x - 0.5f, y - 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV2()));
            builder.index((short) (currVertex + 3), (short) (currVertex + 1), (short) currVertex);
            builder.index((short) (currVertex + 3), (short) (currVertex + 2), (short) (currVertex + 1));

            currVertex += 4;
        }

        // Top Face
        if ((clientWorld.getBlock(new BlockPos(x, y + 1, z)) == Blocks.air.getBlockInstance()) || (clientBlock.renderType == RenderType.OPAQUE && clientWorld.getClientBlock(new BlockPos(x, y + 1, z)).renderType != RenderType.OPAQUE)) {
            TextureRegion currFaceRegion = blockAtlas.getRegions().get(clientBlock.texture.getTopTexIndex());
            builder.vertex(new Vector3(x - 0.5f, y + 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV2()));
            builder.vertex(new Vector3(x - 0.5f, y + 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x + 0.5f, y + 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x + 0.5f, y + 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV2()));
            builder.index((short) (currVertex + 3), (short) (currVertex + 1), (short) currVertex);
            builder.index((short) (currVertex + 3), (short) (currVertex + 2), (short) (currVertex + 1));

            currVertex += 4;
        }

        // Bottom Face
        if ((clientWorld.getBlock(new BlockPos(x, y - 1, z)) == Blocks.air.getBlockInstance()) || (clientBlock.renderType == RenderType.OPAQUE && clientWorld.getClientBlock(new BlockPos(x, y - 1, z)).renderType != RenderType.OPAQUE)) {
            TextureRegion currFaceRegion = blockAtlas.getRegions().get(clientBlock.texture.getBottomTexIndex());
            builder.vertex(new Vector3(x + 0.5f, y - 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV2()));
            builder.vertex(new Vector3(x + 0.5f, y - 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU2(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x - 0.5f, y - 0.5f, z + 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV()));
            builder.vertex(new Vector3(x - 0.5f, y - 0.5f, z - 0.5f), null, null, new Vector2(currFaceRegion.getU(), currFaceRegion.getV2()));
            builder.index((short) (currVertex + 3), (short) (currVertex + 1), (short) currVertex);
            builder.index((short) (currVertex + 3), (short) (currVertex + 2), (short) (currVertex + 1));

            currVertex += 4;
        }

        return currVertex;
    }

    /**
     * @return The chunk's mesh.
     */
    public Mesh getChunkMesh() {
        return chunkMesh;
    }
}
