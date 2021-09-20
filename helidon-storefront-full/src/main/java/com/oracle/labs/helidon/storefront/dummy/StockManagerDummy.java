/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.labs.helidon.storefront.dummy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.oracle.labs.helidon.storefront.data.ItemDetails;
import com.oracle.labs.helidon.storefront.restclients.StockManager;

public class StockManagerDummy implements StockManager {
	private final static StockManagerDummy singleton = new StockManagerDummy();
	// Setup the initial data

	private Map<String, ItemDetails> stockMap = Arrays
			.asList(new ItemDetails("Pencil", 12), new ItemDetails("Pen", 2), new ItemDetails("Brush", 27)).stream()
			.collect(Collectors.toMap(item -> item.getItemName(), item -> item));

	private StockManagerDummy() {
	}

	public static StockManager getStockManager() {
		return singleton;
	}

	@Override
	public Collection<ItemDetails> getAllStockLevels() {
		return stockMap.values();
	}

	@Override
	public ItemDetails getStockItem(String itemName) {
		return stockMap.get(itemName);
	}

	@Override
	public ItemDetails setStockItemLevel(String itemName, Integer itemCount) {
		ItemDetails item = getStockItem(itemName);
		if (item == null) {
			return null;
		}
		item.setItemCount(itemCount);
		return item;
	}

}
