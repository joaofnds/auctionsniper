package auctionsniper.xmpp;

import auctionsniper.Item;
import org.apache.commons.io.FilenameUtils;
import org.jivesoftware.smack.XMPPConnection;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.String.format;

public class XMPPAuctionHouse implements AuctionHouse {
    public static final String LOG_FILE_NAME = "auction-sniper.log";
    private static final String AUCTION_RESOURCE = "Auction";
    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    private static final String LOGGER_NAME = "auction-sniper";
    private final XMPPConnection connection;
    private final LoggingXMPPFailureReporter failureReporter;

    public XMPPAuctionHouse(XMPPConnection connection) throws XMPPAuctionException {
        this.connection = connection;
        this.failureReporter = new LoggingXMPPFailureReporter(makeLogger());
    }

    public static XMPPAuctionHouse connect(String hostname, String username, String password) throws Exception {
        XMPPConnection connection = new XMPPConnection(hostname);
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return new XMPPAuctionHouse(connection);
    }

    @Override
    public XMPPAuction auctionFor(Item item) {
        return new XMPPAuction(connection, auctionID(item.identifier), failureReporter);
    }

    private String auctionID(String itemID) {
        return format(AUCTION_ID_FORMAT, itemID, connection.getServiceName());
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    private Logger makeLogger() throws XMPPAuctionException {
        Logger logger = Logger.getLogger(LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.addHandler(simpleFileHandler());
        return logger;
    }

    private FileHandler simpleFileHandler() throws XMPPAuctionException {
        try {
            FileHandler handler = new FileHandler(LOG_FILE_NAME);
            handler.setFormatter(new SimpleFormatter());
            return handler;
        } catch (Exception e) {
            throw new XMPPAuctionException("Could not create logger FileHandler " + FilenameUtils.getFullPath(LOG_FILE_NAME), e);
        }
    }
}
