package es.craftsmanship.toledo.katangapp.api.controllers;

import static play.mvc.Http.Status.OK;

import static play.test.Helpers.GET;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

import es.craftsmanship.toledo.katangapp.api.store.Store;
import es.craftsmanship.toledo.katangapp.internal.store.KatangappStore;
import es.craftsmanship.toledo.katangapp.models.BusStop;
import es.craftsmanship.toledo.katangapp.test.AssertUtils;

import java.util.Map;

import org.junit.Test;

import play.mvc.Result;

import play.test.WithApplication;

/**
 * @author mdelapenya
 */
public class UnautoApplicationTest extends WithApplication {

	@Test
	public void testUnauto() {
		Store busStopStore = KatangappStore.getInstance();

		Map<String, BusStop> busStopMap = busStopStore.getBusStopStore();

		for (Map.Entry<String, BusStop> stopEntry : busStopMap.entrySet()) {
			BusStop busStop = stopEntry.getValue();

			Result result = route(
				fakeRequest(
					GET,
					"/unauto?idl=" + busStop.getRouteId() + "&idp=" +
						busStop.getId() + "&ido=" + busStop.getOrder()));

			AssertUtils.assertTCK(result, "text/plain", OK);

			break;
		}
	}

}