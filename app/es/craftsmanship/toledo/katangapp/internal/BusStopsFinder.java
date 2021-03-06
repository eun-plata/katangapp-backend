/**
 *    Copyright 2016-today Software Craftmanship Toledo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.craftsmanship.toledo.katangapp.internal;

import es.craftsmanship.toledo.katangapp.api.ClosestPointsAlgorithm;
import es.craftsmanship.toledo.katangapp.api.Finder;
import es.craftsmanship.toledo.katangapp.api.exception.APIElementNotFoundException;
import es.craftsmanship.toledo.katangapp.api.http.HttpService;
import es.craftsmanship.toledo.katangapp.api.parser.Parser;
import es.craftsmanship.toledo.katangapp.api.store.Store;
import es.craftsmanship.toledo.katangapp.business.UnreferenceablePointException;
import es.craftsmanship.toledo.katangapp.internal.store.KatangappStore;
import es.craftsmanship.toledo.katangapp.models.BusStop;
import es.craftsmanship.toledo.katangapp.models.BusStopResult;
import es.craftsmanship.toledo.katangapp.models.Constants;
import es.craftsmanship.toledo.katangapp.models.Point;
import es.craftsmanship.toledo.katangapp.models.QueryResult;
import es.craftsmanship.toledo.katangapp.models.ReferenceablePoint;
import es.craftsmanship.toledo.katangapp.models.RouteBusStopInfo;
import es.craftsmanship.toledo.katangapp.models.RouteResult;
import es.craftsmanship.toledo.katangapp.models.Segment;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.libs.F.Promise;

/**
 * @author mdelapenya
 */
public class BusStopsFinder implements Finder {

	@Inject
	public BusStopsFinder(
		ClosestPointsAlgorithm closestPointsAlgorithm, Parser parser,
		HttpService httpService) {

		this.algorithm = closestPointsAlgorithm;
		this.httpService = httpService;
		this.parser = parser;
	}

	public Promise<QueryResult> findRoutes(String busStopId)
		throws APIElementNotFoundException, InterruptedException,
			UnreferenceablePointException {

		final BusStop busStop = katangappStore.getBusStop(busStopId);

		Segment segment = new Segment(busStop, busStop);

		List<Promise<BusStopResult>> busStopResultPromises =
			new ArrayList<>();

		busStopResultPromises.add(processSegment(segment));

		return processBusStopResultsPromise(busStopResultPromises);
	}

	public Promise<QueryResult> findRoutes(
			double latitude, double longitude, int radius)
		throws InterruptedException {

		ReferenceablePoint currentLocation = new Point(latitude, longitude);

		Map<String, BusStop> busStopMap = katangappStore.getBusStopStore();

		Set<ReferenceablePoint> dataSet = new HashSet<ReferenceablePoint>(
			busStopMap.values());

		List<Segment> segments = algorithm.closestSegments(
			currentLocation, dataSet, radius);

		if (segments.isEmpty()) {
			Promise<QueryResult> queryResultPromise = Promise.promise(
				() -> new QueryResult(Collections.<BusStopResult>emptyList()));

			return queryResultPromise;
		}

		List<Promise<BusStopResult>> busStopResultPromises =
			new ArrayList<>();

		for (final Segment segment : segments) {
			busStopResultPromises.add(processSegment(segment));
		}

		return processBusStopResultsPromise(busStopResultPromises);
	}

	private Promise<QueryResult> processBusStopResultsPromise(
		List<Promise<BusStopResult>> busStopResultPromises) {

		Promise<List<BusStopResult>> sequence = Promise.sequence(
			busStopResultPromises);

		Promise<QueryResult> queryResultPromise = sequence.flatMap(
			busStopResults ->
				Promise.promise(() -> new QueryResult(busStopResults)));

		return queryResultPromise;
	}

	private Promise<BusStopResult> processSegment(final Segment segment) {
		ReferenceablePoint to = segment.getTo();

		final BusStop busStop = (BusStop) to;

		List<RouteBusStopInfo> routes = busStop.getRoutes();

		RouteBusStopInfo busStopInfo = routes.get(0);

		String routeId = busStopInfo.getRouteId();

		routeId = routeId.substring(routeId.lastIndexOf("/") + 1);

		Promise<String> responseHtml = httpService.get(
			routeId, busStop.getId(), busStopInfo.getOrderId());

		Calendar calendar = Calendar.getInstance();

		calendar.setTimeZone(Constants.TZ_TOLEDO);

		Promise<List<RouteResult>> routesPromise =
			parser.parseResponse(routeId, calendar.getTime(), responseHtml);

		Promise<BusStopResult> busStopResultPromise = routesPromise.map(
			routeResults -> {

				Collections.sort(routeResults);

				BusStopResult busStopResult = new BusStopResult(
					segment.getDistance(), busStop, routeResults);

				return busStopResult;
			});

		return busStopResultPromise;
	}

	private static Store katangappStore = KatangappStore.getInstance();

	private ClosestPointsAlgorithm algorithm;
	private HttpService httpService;
	private Parser parser;

}
