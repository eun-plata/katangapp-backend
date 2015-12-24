package internal.business.store;

import business.exception.NotFoundException;
import business.store.Store;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author mdelapenya
 */
public class KatangappStoreTest {

	@Rule
	public ExpectedException thrownException = ExpectedException.none();

	@Before
	public void setUp() {
		store = KatangappStore.getInstance();
	}

	@Test
	public void testGetBusStopNotFound() throws Exception {
		thrownException.expect(NotFoundException.class);

		store.getBusStop("notfound");
	}

	@Test
	public void testGetRouteNotFound() throws Exception {
		thrownException.expect(NotFoundException.class);

		store.getRoute("notfound");
	}

	private Store store;

}