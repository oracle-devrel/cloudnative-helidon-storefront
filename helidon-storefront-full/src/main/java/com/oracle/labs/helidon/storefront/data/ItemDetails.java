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

import io.helidon.common.Reflected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Reflected // this tells the Helidon native-image support to include this as a class that
			// can be accessed via reflection
@Data // Tells Lombok to create getters and setters, equals and hashcode
@NoArgsConstructor // Tells Lombok to create a constructor with no args (needed for the JSCON
					// unmarshalling process to work)
@AllArgsConstructor // Tells Lombok to create a constructor with all the args (makes life easier
					// creating instances)
@Schema(name = "ItemDetails", description = "Details of the item in the database", example = "{\"itemCount\": 10, \"itemName\": \"Pencil\"}")
public class ItemDetails {
	@Schema(required = true, description = "The name of the item", example = "Pencil")
	private String itemName;
	@Schema(required = true, description = "The number of items listed as being available", example = "10")
	private int itemCount;
}