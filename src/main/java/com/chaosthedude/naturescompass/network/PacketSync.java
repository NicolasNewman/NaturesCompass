package com.chaosthedude.naturescompass.network;

import java.util.function.Supplier;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSync {

	private boolean canTeleport;

	public PacketSync() {}
	
	public PacketSync(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	public PacketSync(PacketBuffer buf) {
		canTeleport = buf.readBoolean();
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeBoolean(canTeleport);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			NaturesCompass.canTeleport = canTeleport;
		});
		ctx.get().setPacketHandled(true);
	}

}
