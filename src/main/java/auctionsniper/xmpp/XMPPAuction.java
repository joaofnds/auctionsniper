package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.AuctionMessageTranslator;
import auctionsniper.utils.Announcer;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import static java.lang.String.format;

public class XMPPAuction implements Auction {
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";
    private final Announcer<AuctionEventListener> auctionEventListeners = Announcer.to(AuctionEventListener.class);
    private final Chat chat;
    private final XMPPFailureReporter failureReporter;

    public XMPPAuction(XMPPConnection connection, String auctionID, XMPPFailureReporter failureReporter) {
        this.failureReporter = failureReporter;
        var translator = translatorFor(connection);
        chat = connection.getChatManager().createChat(auctionID, translator);
        addEventListener(chatDisconnectorFor(translator));
    }

    private AuctionMessageTranslator translatorFor(XMPPConnection connection) {
        return new AuctionMessageTranslator(connection.getUser(), auctionEventListeners.announce(), failureReporter);
    }

    private AuctionEventListener chatDisconnectorFor(final AuctionMessageTranslator translator) {
        return new AuctionEventListener() {
            @Override
            public void auctionClosed() {
            }

            @Override
            public void currentPrice(int price, int increment, PriceSource fromSniper) {
            }

            @Override
            public void auctionFailed() {
                chat.removeMessageListener(translator);
            }
        };
    }

    @Override
    public void bid(int amount) {
        sendMessage(format(BID_COMMAND_FORMAT, amount));
    }

    @Override
    public void join() {
        sendMessage(JOIN_COMMAND_FORMAT);
    }

    @Override
    public void addEventListener(AuctionEventListener sniper) {
        auctionEventListeners.addListener(sniper);
    }

    private void sendMessage(final String message) {
        try {
            chat.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
}
