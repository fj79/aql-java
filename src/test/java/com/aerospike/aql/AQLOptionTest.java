/* 
 * Copyright 2012-2015 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.aql;

import org.junit.Test;


public class AQLOptionTest {
	public static final String HOST = "192.168.51.188";
	public static final String PORT = "3000";

	@Test
	public void usageTest() {
		AQLrun.main(new String[]{"-h", HOST, "-p", PORT, "-u"});
	}

	//@Test
	public void noInputFileTest() {
		AQLrun.main(new String[]{"-h", HOST, "-p", PORT, "-u", "-c"});
	}
}
