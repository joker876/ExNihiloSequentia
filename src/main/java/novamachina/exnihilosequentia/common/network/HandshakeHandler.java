package novamachina.exnihilosequentia.common.network;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import novamachina.exnihilosequentia.common.item.ore.EnumOre;
import novamachina.exnihilosequentia.common.utility.ExNihiloLogger;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HandshakeHandler {
    @Nonnull private static final ExNihiloLogger logger = new ExNihiloLogger(LogManager.getLogger());

    private HandshakeHandler() {
    }

    public static void handleAcknowledge(@Nonnull final HandshakeMessages.C2SAcknowledge message,
                                         @Nonnull final Supplier<NetworkEvent.Context> ctx) {
        logger.debug("Received acknowledgement from client. " + message);
        ctx.get().setPacketHandled(true);
    }

    public static void handleOreList(@Nonnull final HandshakeMessages.S2COreList msg,
                                     @Nonnull final Supplier<NetworkEvent.Context> ctx) throws InterruptedException {
        logger.debug("Recieved ore data from server");

        @Nonnull final AtomicBoolean updatedOreList = new AtomicBoolean(false);
        @Nonnull final CountDownLatch block = new CountDownLatch(1);
        ctx.get().enqueueWork(() -> {
            updatedOreList.set(true);

            if (!EnumOre.updateEnabledOres(msg)) {
                updatedOreList.set(false);
            }
            block.countDown();
        });

        try {
            block.await();
        } catch (InterruptedException e) {
            //noinspection ResultOfMethodCallIgnored
            Thread.interrupted();
            logger.error(e.getMessage());
            throw e;
        }

        ctx.get().setPacketHandled(true);

        if (updatedOreList.get()) {
            logger.debug("Successfully synchronized ore list from server.");
            @Nullable final SimpleChannel handshakeChannel = PacketHandler.getHandshakeChannel();
            if (handshakeChannel != null) {
                handshakeChannel.reply(new HandshakeMessages.C2SAcknowledge(), ctx.get());
            }
        } else {
            logger.debug("Failed to synchronize ore list from server.");
            ctx.get().getNetworkManager().disconnect(new StringTextComponent(
                    "Connection closed - [Ex Nihilo: Sequentia] Failed to synchronize ore list from server."));
        }
    }
}
