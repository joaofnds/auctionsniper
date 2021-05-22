package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_ITEM_ID = 3;

    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_RESOURCE = "Auction";
    private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    private final SnipersTableModel snipers = new SnipersTableModel();
    private Chat notToBeGCd;
    private MainWindow ui;

    public Main() throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow(snipers));
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.joinAuction(
                connectTo(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]),
                args[ARG_ITEM_ID]
        );
    }

    private static XMPPConnection connectTo(String hostname, String username, String password) throws XMPPException {
        XMPPConnection connection = new XMPPConnection(hostname);
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return connection;
    }

    private static String auctionId(String itemID, XMPPConnection connection) {
        return String.format(AUCTION_ID_FORMAT, itemID, connection.getServiceName());
    }

    private void joinAuction(XMPPConnection connection, String itemID) throws XMPPException {
        disconnectWhenUICloses(connection);

        final Chat chat = connection.getChatManager().createChat(auctionId(itemID, connection), null);
        this.notToBeGCd = chat;

        Auction auction = new XMPPAuction(chat);
        chat.addMessageListener(
                new AuctionMessageTranslator(
                        connection.getUser(),
                        new AuctionSniper(
                                auction,
                                itemID,
                                new SwingThreadSniperListener(snipers))));

        auction.join();
    }

    private void disconnectWhenUICloses(XMPPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    private class SwingThreadSniperListener implements SniperListener {
        public SwingThreadSniperListener(SnipersTableModel snipers) {
        }

        @Override
        public void sniperStateChanged(SniperSnapshot snapshot) {
            snipers.sniperStateChanged(snapshot);
        }
    }
}
