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
package com.oracle.labs.helidon.storefront.exceptions;

public class StorefrontException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7887970832976155719L;

	public StorefrontException() {
		super();
	}

	public StorefrontException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorefrontException(String message, Throwable cause) {
		super(message, cause);
	}

	public StorefrontException(String message) {
		super(message);
	}

	public StorefrontException(Throwable cause) {
		super(cause);
	}
}
