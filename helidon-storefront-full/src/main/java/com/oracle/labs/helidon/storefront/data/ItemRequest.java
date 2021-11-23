/*
 * Copyright (c) 2019, 2020, 2021 Oracle and/or its affiliates.
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
package com.oracle.labs.helidon.storefront.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ItemRequest", description = "Details of a Item reservation request", example = "{\"requestedItem\", \"Pin\", \"requestedCount\",5}")
public class ItemRequest {
	@Schema(required = true, description = "Name of the item being requested", example = "Pin")
	private String requestedItem;
	@Schema(required = true, description = "Number of the items being requested, this must be larger than the minimumChange", example = "5")
	private int requestedCount;
}
