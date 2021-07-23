package auctionsniper;

import auctionsniper.ui.Column;
import auctionsniper.ui.SnipersTableModel;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class SnipersTableModelTest {
    private final Mockery context = new Mockery();
    private final SnipersTableModel model = new SnipersTableModel();
    private final TableModelListener listener = context.mock(TableModelListener.class);

    @Before
    public void attachModelListener() {
        model.addTableModelListener(listener);
    }

    @Test
    public void hasEnoughColumns() {
        assertThat(model.getColumnCount(), is(Column.values().length));
    }

    @Test
    public void setsUpColumnHeadings() {
        for (Column column : Column.values()) {
            assertThat(column.name, is(model.getColumnName(column.ordinal())));
        }
    }

    @Test
    public void notifiesListenersWhenAddingASniper() {
        SniperSnapshot joining = SniperSnapshot.joining("item123");
        context.checking(new Expectations() {{
            oneOf(listener).tableChanged(with(anyInsertionEvent()));
        }});

        assertThat(model.getRowCount(), is(0));

        model.addSniper(joining);

        assertThat(model.getRowCount(), is(1));
        assertRowMatchesSnapshot(0, joining);
    }

    @Test
    public void setsSniperValuesInColumns() {
        SniperSnapshot joining = SniperSnapshot.joining("item id");
        SniperSnapshot bidding = joining.bidding(555, 666);

        context.checking(new Expectations() {{
            allowing(listener).tableChanged(with(anyInsertionEvent()));
            oneOf(listener).tableChanged(with(aChangeInRow(0)));
        }});

        model.addSniper(joining);
        model.sniperStateChanged(bidding);

        assertRowMatchesSnapshot(0, bidding);
    }

    @Test
    public void holdSniperInAdditionOrder() {
        context.checking(new Expectations() {{
            ignoring(listener);
        }});

        model.addSniper(SniperSnapshot.joining("item 0"));
        model.addSniper(SniperSnapshot.joining("item 1"));

        assertThat(cellValue(0, Column.ITEM_IDENTIFIER), is("item 0"));
        assertThat(cellValue(1, Column.ITEM_IDENTIFIER), is("item 1"));
    }

    private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
        assertThat(cellValue(row, Column.ITEM_IDENTIFIER), is(snapshot.itemId));
        assertThat(cellValue(row, Column.ITEM_IDENTIFIER), is(snapshot.itemId));
        assertThat(cellValue(row, Column.LAST_PRICE), is(snapshot.lastPrice));
        assertThat(cellValue(row, Column.LAST_BID), is(snapshot.lastBid));
        assertThat(cellValue(row, Column.SNIPER_STATE), is(SnipersTableModel.textFor(snapshot.state)));
    }

    private Object cellValue(int rowIndex, Column column) {
        return model.getValueAt(rowIndex, column.ordinal());
    }

    private Matcher<TableModelEvent> anyInsertionEvent() {
        return hasProperty("type", equalTo(TableModelEvent.INSERT));
    }

    private Matcher<TableModelEvent> anInsertionAtRow(int row) {
        return samePropertyValuesAs(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    private Matcher<TableModelEvent> aChangeInRow(int row) {
        return samePropertyValuesAs(new TableModelEvent(model, row));
    }
}
