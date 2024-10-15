package com.diamantino.voxelcraft.common.networking.packets.c2s;

import com.diamantino.voxelcraft.common.networking.packets.data.PacketBuffer;
import com.diamantino.voxelcraft.common.networking.packets.data.Packets;
import com.diamantino.voxelcraft.common.networking.packets.s2c.ChunkSyncPacket;
import com.diamantino.voxelcraft.common.networking.packets.utils.BasePacket;
import com.diamantino.voxelcraft.server.ServerInstance;
import com.diamantino.voxelcraft.common.world.chunk.ChunkPos;

import java.io.IOException;

/**
 * Packet sent by the client to request a chunk from the server.
 *
 * @author Diamantino
 */
public class RequestChunkPacket extends BasePacket {
    /**
     * The position of the chunk requested.
     */
    private ChunkPos chunkPos;

    /**
     * Creates a new RequestChunkPacket.
     *
     * @param chunkPos The position of the chunk requested.
     */
    public RequestChunkPacket(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    /**
     * Reads the packet data from the buffer.
     *
     * @param senderName The name of the sender of the packet.
     * @param buffer The buffer containing the packet data.
     */
    @Override
    public void readPacketData(String senderName, PacketBuffer buffer) throws IOException {
        this.chunkPos = new ChunkPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        Packets.sendToPlayer(senderName, new ChunkSyncPacket(ServerInstance.instance.world.getChunkForPos(chunkPos)));
    }

    /**
     * Writes the packet data to the buffer.
     *
     * @param buffer The buffer to write the packet data to.
     */
    @Override
    public void writePacketData(PacketBuffer buffer) throws IOException {
        buffer.writeInt(chunkPos.x());
        buffer.writeInt(chunkPos.y());
        buffer.writeInt(chunkPos.z());
    }
}
