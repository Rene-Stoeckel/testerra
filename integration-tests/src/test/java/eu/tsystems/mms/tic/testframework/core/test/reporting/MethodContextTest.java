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
package eu.tsystems.mms.tic.testframework.core.test.reporting;

import eu.tsystems.mms.tic.testframework.AbstractTest;
import eu.tsystems.mms.tic.testframework.execution.testng.AssertCollector;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by pele on 19.10.2016.
 */
public class MethodContextTest extends AbstractTest {

    String level0String = "level 0";

    RuntimeException level3 = new RuntimeException("level 3");
    RuntimeException level2 = new RuntimeException("level 2", level3);
    RuntimeException level1 = new RuntimeException("level 1", level2);
    RuntimeException level0 = new RuntimeException(level0String, level1);

    private RuntimeException getStackedThrowable() {
        return level0;
    }

    @Test
    public void testT01_SetThrowable() throws Exception {
        MethodContext methodContext = ExecutionContextController.getCurrentMethodContext();
        methodContext.setThrowable(null, getStackedThrowable());

        List<String> stackTrace = methodContext.getStackTrace().stackTrace;

        String errorMessage = methodContext.getReadableErrorMessage();
        AssertCollector.assertTrue(errorMessage.contains(level0String), "error message contains " + level0String);

        String[] toCheck = new String[]{
                level1.toString(),
                level2.toString(),
                level3.toString(),
        };
        for (String s : toCheck) {
            AssertCollector.assertTrue(stackTrace.contains(s), "stack trace contains " + s);
        }
    }
}