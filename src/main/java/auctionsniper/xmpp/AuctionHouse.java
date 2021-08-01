package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.Item;

public interface AuctionHouse {
    Auction auctionFor(Item item);

    void disconnect();
}
