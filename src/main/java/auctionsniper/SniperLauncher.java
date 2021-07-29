package auctionsniper;

import auctionsniper.xmpp.AuctionHouse;

public class SniperLauncher implements UserRequestListener {
    private final AuctionHouse auctionHouse;
    private final SniperCollector collector;

    public SniperLauncher(AuctionHouse auctionHouse, SniperCollector snipers) {
        this.collector = snipers;
        this.auctionHouse = auctionHouse;
    }

    public void joinAuction(String itemID) {
        Auction auction = auctionHouse.auctionFor(itemID);
        AuctionSniper sniper = new AuctionSniper(itemID, auction);
        auction.addEventListener(sniper);
        collector.addSniper(sniper);
        auction.join();
    }
}
