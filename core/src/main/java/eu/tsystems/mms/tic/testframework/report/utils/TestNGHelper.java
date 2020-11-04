/*
 * Testerra
 *
 * (C) 2020,  Peter Lehmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
 package eu.tsystems.mms.tic.testframework.report.utils;

import eu.tsystems.mms.tic.testframework.exceptions.SystemException;
import org.testng.IClass;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public final class TestNGHelper {

    @FunctionalInterface
    private interface GetValue<T> {
        T run();
    }

    private static final <T> T tryAny(Class<T> responseType, final String scope, final GetValue<T>... getValues) {
        if (getValues != null) {
            for (GetValue<T> getValue : getValues) {
                try {
                    T value = getValue.run();
                    if (value != null) {
                        return value;
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }
        }

        throw new SystemException("Could not get " + scope);
    }

    public static String getSuiteName(ITestResult testResult, ITestContext testContext) {
        return tryAny(String.class, "SuiteName",
                () -> testResult.getTestContext().getSuite().getName(),
                () -> testContext.getSuite().getName()
        );

    }

    public static String getTestName(ITestResult testResult, ITestContext testContext) {
        return tryAny(String.class, "TestName",
                () -> testResult.getTestContext().getCurrentXmlTest().getName(),
                () -> testContext.getCurrentXmlTest().getName()
        );
    }

    public static IClass getTestClass(ITestResult testResult, ITestContext testContext, IInvokedMethod invokedMethod) {
        return tryAny(IClass.class, "TestClass",
                () -> testResult.getTestClass(),
                () -> invokedMethod.getTestMethod().getTestClass(),
                () -> invokedMethod.getTestResult().getTestClass(),
                () -> testResult.getMethod().getTestClass()
        );
    }

    public static ITestNGMethod getTestMethod(ITestResult testResult, ITestContext testContext, IInvokedMethod invokedMethod) {
        return tryAny(ITestNGMethod.class, "TestMethod",
                () -> testResult.getMethod(),
                () -> invokedMethod.getTestMethod()
        );
    }

}
