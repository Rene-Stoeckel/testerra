/*
 * (C) Copyright T-Systems Multimedia Solutions GmbH 2018, ..
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
 * Contributors:
 *     Peter Lehmann <p.lehmann@t-systems.com>
 *     pele <p.lehmann@t-systems.com>
 */
package eu.tsystems.mms.tic.testframework.report.hooks;

import eu.tsystems.mms.tic.testframework.annotations.DismissDryRun;
import eu.tsystems.mms.tic.testframework.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;

/**
 * Created by pele on 30.01.2017.
 */
public abstract class Hook {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hook.class);

    protected static boolean dryRun(ITestNGMethod testNGMethod) {
        final ConstructorOrMethod constructorOrMethod = testNGMethod.getConstructorOrMethod();
        final DismissDryRun dismissDryRun = constructorOrMethod.getMethod().getAnnotation(DismissDryRun.class);
        if (dismissDryRun == null) {
            LOGGER.info("Dry run: " + testNGMethod.getMethodName());
            TestUtils.sleep(500);
            return true;
        }
        return false;
    }

}