package auctionsniper;

public interface Auction {
    void bid(int amount);

    void join();

    void addEventListener(AuctionEventListener listener);
}
