package novamachina.exnihilosequentia.common.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.FMLHandshakeHandler;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import novamachina.exnihilosequentia.common.utility.ExNihiloConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketHandler {
    @Nonnull private static final Logger logger = LogManager.getLogger(PacketHandler.class);
    @Nullable private static SimpleChannel handshakeChannel;

    private PacketHandler() {
    }

    @Nullable
    public static SimpleChannel getHandshakeChannel() {
        return handshakeChannel;
    }

    public static void registerMessages() {
        handshakeChannel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ExNihiloConstants.ModIds.EX_NIHILO_SEQUENTIA, "handshake"))
                .networkProtocolVersion(() -> "1")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        handshakeChannel.messageBuilder(HandshakeMessages.C2SAcknowledge.class, 99)
                .loginIndex(HandshakeMessages.LoginIndexedMessage::getLoginIndex, HandshakeMessages.LoginIndexedMessage::setLoginIndex)
                .encoder(HandshakeMessages.C2SAcknowledge::encode)
                .decoder(HandshakeMessages.C2SAcknowledge::decode)
                .consumer(FMLHandshakeHandler.indexFirst((handler, msg, s) -> HandshakeHandler.handleAcknowledge(msg, s)))
                .add();

        handshakeChannel.messageBuilder(HandshakeMessages.S2COreList.class, 1)
                .loginIndex(HandshakeMessages.LoginIndexedMessage::getLoginIndex, HandshakeMessages.LoginIndexedMessage::setLoginIndex)
                .encoder(HandshakeMessages.S2COreList::encode)
                .decoder(HandshakeMessages.S2COreList::decode)
                .consumer(FMLHandshakeHandler.biConsumerFor((handler, msg, supplier) -> {
                    try {
                        HandshakeHandler.handleOreList(msg, supplier);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage());
                    }
                }))
                .markAsLoginPacket()
                .add();
    }
}
