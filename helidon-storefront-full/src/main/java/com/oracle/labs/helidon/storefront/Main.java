/*Copyright (c) 2021 Oracle and/or its affiliates.

The Universal Permissive License (UPL), Version 1.0

Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both

(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),

without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.

This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.oracle.labs.helidon.storefront;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.config.PollingStrategies;
import io.helidon.config.spi.ConfigSource;
import io.helidon.microprofile.server.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * Main method simulating trigger of main method of the server.
 */

//Have Lombok create a logger for us
@Slf4j
public final class Main {

	/**
	 * Cannot be instantiated.
	 */
	private Main() {
	}

	/**
	 * Application main entry point.
	 * 
	 * @param args command line arguments
	 * @throws IOException if there are problems reading logging properties
	 */
	public static void main(final String[] args) throws IOException {
		// Helidon will automatically locate a logging.propoerties if one exists in the
		// classpath or current working directory and will use that to configure the
		// logging for us, so we don't need to explicitly configure logging

		log.info("Starting server");
		Server server = Server.builder().config(buildConfig()).build().start();

		log.info("Running on http://localhost:" + server.port());
	}

	/**
	 * 
	 * @return
	 */
	private static Config buildConfig() {
		// Build up a list of config sources, as we will have 4 in the end, we need to
		// create a list of built config sources. if we were only creating 3 or less we
		// could use them directly in the Config.builder.sources(source1, sources2)
		// mechanism, but for reasons unclear sources does not use varargs
		//
		// Note that the ConfigSources.{sourcetype} methods produce a builder, so we
		// need to
		// build it before we can add it to the list. again if there were three or less
		// we could pass the ConfigSource builder directly into the sources method of
		// the
		// config builder.
		List<Supplier<? extends ConfigSource>> configSourcesToScan = new ArrayList<>(5);
		configSourcesToScan.add(ConfigSources.file("conf/storefront-config.yaml")
				.pollingStrategy(PollingStrategies.regular(Duration.ofSeconds(5))).optional().build());
		configSourcesToScan.add(ConfigSources.file("conf/storefront-network.yaml").optional().build());
		configSourcesToScan.add(ConfigSources.file("confsecure/storefront-security.yaml").build());
		configSourcesToScan.add(ConfigSources.classpath("META-INF/microprofile-config.properties").build());
		return Config.builder().sources(configSourcesToScan).build();
	}
}
