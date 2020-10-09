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
 package eu.tsystems.mms.tic.testframework.webdrivermanager;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.constants.TesterraProperties;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.utils.StringUtils;
import eu.tsystems.mms.tic.testframework.webdrivermanager.desktop.WebDriverMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openqa.selenium.remote.DesiredCapabilities;

public class DesktopWebDriverRequest extends WebDriverRequest implements Loggable {
    private Map<String, Object> sessionCapabilities;
    private DesiredCapabilities desiredCapabilities;
    private URL seleniumServerURL;
    private WebDriverMode webDriverMode;

    public boolean hasSessionCapabilities() {
        return this.sessionCapabilities != null;
    }

    public Map<String, Object> getSessionCapabilities() {
        if (this.sessionCapabilities == null) {
            this.sessionCapabilities = new HashMap<>();
        }
        return this.sessionCapabilities;
    }

    public DesktopWebDriverRequest setSessionCapabilities(Map<String, Object> sessionCapabilities) {
        this.sessionCapabilities = sessionCapabilities;
        return this;
    }

    public Optional<DesiredCapabilities> getDesiredCapabilities() {
        return Optional.ofNullable(desiredCapabilities);
    }

    public DesktopWebDriverRequest setDesiredCapabilities(DesiredCapabilities desiredCapabilities) {
        this.desiredCapabilities = desiredCapabilities;
        return this;
    }

    public URL getSeleniumServerUrl() {
        if (seleniumServerURL == null) {
            try {
                setSeleniumServerUrl(StringUtils.getFirstValidString(
                        PropertyManager.getProperty(TesterraProperties.SELENIUM_SERVER_URL),
                        "http://" + StringUtils.getFirstValidString(PropertyManager.getProperty(TesterraProperties.SELENIUM_SERVER_HOST), "localhost") + ":" + StringUtils.getFirstValidString(PropertyManager.getProperty(TesterraProperties.SELENIUM_SERVER_PORT), "4444") + "/wd/hub"
                ));
            } catch (MalformedURLException e) {
                log().error("Unable to retrieve default Selenium URL from properties", e);
            }
        }
        return seleniumServerURL;
    }

    public DesktopWebDriverRequest setSeleniumServerUrl(String url) throws MalformedURLException {
        this.seleniumServerURL = new URL(url);
        return this;
    }

    public DesktopWebDriverRequest setSeleniumServerUrl(URL url) {
        this.seleniumServerURL = url;
        return this;
    }

    public WebDriverMode getWebDriverMode() {
        return webDriverMode;
    }

    public DesktopWebDriverRequest setWebDriverMode(WebDriverMode webDriverMode) {
        this.webDriverMode = webDriverMode;
        return this;
    }

    @Override
    public String toString() {
        return "DesktopWebDriverRequest{" +
                "sessionCapabilities=" + sessionCapabilities +
                ", desiredCapabilities=" + desiredCapabilities +
                ", seleniumServerURL='" + seleniumServerURL + '\'' +
                ", webDriverMode=" + webDriverMode +
                ", browser=" + getBrowser() +
                ", sessionKey='" + getSessionKey() + '\'' +
                ", baseUrl='" + getBaseUrl() + '\'' +
                ", browserVersion='" + getBrowserVersion() + '\'' +
                ", storedExecutingNode=" + getExecutingNode() +
                '}';
    }
}
