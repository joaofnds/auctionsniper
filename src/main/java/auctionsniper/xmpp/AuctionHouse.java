package auctionsniper.xmpp;

import auctionsniper.Auction;

public interface AuctionHouse {
    Auction auctionFor(String itemID);
    void disconnect();
}
