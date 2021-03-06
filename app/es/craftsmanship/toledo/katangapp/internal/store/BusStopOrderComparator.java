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

package es.craftsmanship.toledo.katangapp.internal.store;

import es.craftsmanship.toledo.katangapp.models.BusStop;

import java.util.Comparator;

/**
 * @author Manuel de la Peña
 */
public class BusStopOrderComparator implements Comparator<BusStop> {

	@Override
	public int compare(BusStop busStop1, BusStop busStop2) {
		float order1 = Float.valueOf(busStop1.getOrder());
		float order2 = Float.valueOf(busStop2.getOrder());

		if (order1 > order2) {
			return 1;
		}
		else if (order1 < order2) {
			return -1;
		}
		else {
			return 0;
		}
	}

}