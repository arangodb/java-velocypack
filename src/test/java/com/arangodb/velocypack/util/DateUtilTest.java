/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.velocypack.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;

import com.arangodb.velocypack.internal.util.DateUtil;

/**
 * @author Mark Vollmary
 *
 */
public class DateUtilTest {

	@Test
	public void format() throws Exception {
		assertThat(DateUtil.format(new Date(1523891841000L)), is("2018-04-16T15:17:21.000Z"));
	}

	@Test
	public void parse() throws Exception {
		assertThat(DateUtil.parse("2018-04-16T15:17:21.000Z").getTime(), is(1523891841000L));
	}
}
