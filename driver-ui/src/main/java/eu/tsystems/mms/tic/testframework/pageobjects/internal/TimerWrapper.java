/*
 * Testerra
 *
 * (C) 2020, Peter Lehmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
 package eu.tsystems.mms.tic.testframework.pageobjects.internal;

import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.pageobjects.PageOverrides;
import eu.tsystems.mms.tic.testframework.pageobjects.internal.asserts.AbstractTestedPropertyAssertion;
import eu.tsystems.mms.tic.testframework.transfer.ThrowablePackedResponse;
import eu.tsystems.mms.tic.testframework.utils.StringUtils;
import eu.tsystems.mms.tic.testframework.utils.ThrowableUtils;
import eu.tsystems.mms.tic.testframework.utils.Timer;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManagerUtils;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * Created by rnhb on 14.08.2015.
 * @deprecated No more needed since {@link AbstractTestedPropertyAssertion}
 */
@Deprecated
public class TimerWrapper implements Loggable {

    private final static PageOverrides pageOverrides = Testerra.injector.getInstance(PageOverrides.class);

    private static int sleepTimeInMs=200;

    private final WebDriver webDriver;

    public TimerWrapper(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    private int getSleepTimeInMs() {
        return sleepTimeInMs;
    }

    public <T> ThrowablePackedResponse<T> executeSequence(final Timer.Sequence<T> sequence) {
        return executeSequence(sequence, pageOverrides.getTimeoutSeconds());
    }

    public <T> ThrowablePackedResponse<T> executeSequence(final Timer.Sequence<T> sequence, int timeoutInSeconds) {
        Timer timer = new Timer(getSleepTimeInMs(), timeoutInSeconds*1000);
        ThrowablePackedResponse<T> booleanThrowablePackedResponse = null;

        try {
            booleanThrowablePackedResponse = timer.executeSequence(sequence);
        } catch (Throwable t) {
            checkForPageLoadTimeout(t);
            if (t instanceof RuntimeException) { // can be nothing else, but to avoid cast warning
                throw (RuntimeException) t;
            }
        }

        booleanThrowablePackedResponse.setLogError(false);
        return booleanThrowablePackedResponse;
    }

    private void checkForPageLoadTimeout(Throwable throwable) {
        final Throwable throwableContainedIn = ThrowableUtils.getThrowableContainedIn(throwable, TimeoutException.class);
        if (throwableContainedIn == null) {
            return;
        }

        final String message = throwableContainedIn.getMessage();
        if (!StringUtils.isStringEmpty(message)) {
            if (message.contains("Timed out waiting for page load")) {
                log().error("Shutting down WebDriver session(s) due to org.openqa.selenium.TimeoutException");
                    /*
                    Close the session to avoid huge timeouts.
                     */
                final String sessionId = WebDriverManagerUtils.getSessionKey(webDriver);
                if (!StringUtils.isAnyStringEmpty(sessionId)) {
                    WebDriverManager.shutdown();
                } else {
                    webDriver.quit();
                }
            }
        }
    }

}
