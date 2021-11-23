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

import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Provider for greeting message.
 */
@ApplicationScoped
public class MinimumChange {
	// package this up in an AtomicReference, strictly this is not required as all
	// operations on primitives are atomic, but it's good practice
	private final AtomicReference<Integer> minimumChange = new AtomicReference<>();

	/**
	 * Create a new greeting provider, reading the message from configuration.
	 *
	 * @param message greeting to use
	 */
	public MinimumChange() {
		this.minimumChange.set(3);
	}

	@Inject
	public MinimumChange(@ConfigProperty(name = "app.minimumchange") Integer initialMinimumChange) {
		this.minimumChange.set(initialMinimumChange);
	}

	public Integer getMinimumChange() {
		return minimumChange.get();
	}

	public void setMinimumDelta(Integer newMinimumChange) {
		this.minimumChange.set(newMinimumChange);
	}
}
