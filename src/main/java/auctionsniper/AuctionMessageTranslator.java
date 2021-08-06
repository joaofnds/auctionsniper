package auctionsniper;

import auctionsniper.xmpp.XMPPFailureReporter;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.HashMap;
import java.util.Map;

public class AuctionMessageTranslator implements MessageListener {
    private final AuctionEventListener listener;
    private final String sniperId;
    private final XMPPFailureReporter failureReporter;

    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener, XMPPFailureReporter failureReporter) {
        this.sniperId = sniperId;
        this.listener = listener;
        this.failureReporter = failureReporter;
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        var messageBody = message.getBody();
        try {
            translate(messageBody);
        } catch (Exception parseException) {
            failureReporter.cannotTranslateMessage(sniperId, messageBody, parseException);
            listener.auctionFailed();
        }
    }

    public void translate(String message) {
        AuctionEvent event = AuctionEvent.from(message);

        String type = event.type();
        if ("CLOSE".equals(type)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(type)) {
            listener.currentPrice(
                    event.currentPrice(),
                    event.increment(),
                    event.isFrom(sniperId));
        }
    }

    private HashMap<String, String> unpackEventFrom(Message message) {
        HashMap<String, String> event = new HashMap<String, String>();

        for (String element : message.getBody().split(";")) {
            String[] pair = element.split(":");
            event.put(pair[0].trim(), pair[1].trim());
        }

        return event;
    }

    private static class AuctionEvent {
        private final Map<String, String> fields = new HashMap<String, String>();

        static AuctionEvent from(String messageBody) {
            AuctionEvent event = new AuctionEvent();

            for (String field : fieldsIn(messageBody)) {
                event.addField(field);
            }

            return event;
        }

        static String[] fieldsIn(String messageBody) {
            return messageBody.split(";");
        }

        public String type() {
            return get("Event");
        }

        public int currentPrice() {
            return getInt("CurrentPrice");
        }

        public int increment() {
            return getInt("Increment");
        }

        private int getInt(String fieldName) {
            return Integer.parseInt(get(fieldName));
        }

        private void addField(String field) {
            String[] pair = field.split(":");
            fields.put(pair[0].trim(), pair[1].trim());
        }

        private String get(String fieldName) {
            var value = fields.get(fieldName);

            if (value == null) {
                throw new MissingValueException(fieldName);
            }

            return value;
        }

        public AuctionEventListener.PriceSource isFrom(String sniperId) {
            return sniperId.equals(bidder()) ? AuctionEventListener.PriceSource.FromSniper : AuctionEventListener.PriceSource.FromOtherBidder;
        }

        private String bidder() {
            return get("Bidder");
        }

        private class MissingValueException extends RuntimeException {
            public MissingValueException(String fieldName) {
                super("Unable to parse field '" + fieldName + "' from message.");
            }
        }
    }
}
