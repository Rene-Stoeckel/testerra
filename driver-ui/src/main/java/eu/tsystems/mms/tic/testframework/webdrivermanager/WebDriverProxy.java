/*
 * Testerra
 *
 * (C) 2020, Eric Kubenka, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
package eu.tsystems.mms.tic.testframework.webdrivermanager;

import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.utils.IExecutionContextController;
import eu.tsystems.mms.tic.testframework.utils.ObjectUtils;
import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;

public class WebDriverProxy extends ObjectUtils.PassThroughProxy<WebDriver> implements Loggable {

    private final SessionContext sessionContext;
    private static final IExecutionContextController executionContextController = Testerra.getInjector().getInstance(IExecutionContextController.class);

    public WebDriverProxy(WebDriver driver, SessionContext sessionContext) {
        super(driver);
        this.sessionContext = sessionContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        executionContextController.setCurrentSessionContext(this.sessionContext);
        return invoke(method, args);
    }

    public WebDriver getWrappedWebDriver() {
        return target;
    }
}
