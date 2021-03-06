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

import eu.tsystems.mms.tic.testframework.useragents.BrowserInformation;
import eu.tsystems.mms.tic.testframework.useragents.UapBrowserInformation;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class containing utility methods for WebDriverManager. To keep the WebDriverManager Class cleaner.
 *
 * @author sepr
 */
public final class WebDriverManagerUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverManagerUtils.class);

    /**
     * Hide constructor.
     */
    private WebDriverManagerUtils() {
    }

    private static final Map<WebDriver, BrowserInformation> CACHED_BROWSER_INFOS = new ConcurrentHashMap<>();

    public static BrowserInformation getBrowserInformation(final WebDriver driver) {
        if (driver == null) {
            return null;
        }

        if (CACHED_BROWSER_INFOS.containsKey(driver)) {
            return CACHED_BROWSER_INFOS.get(driver);
        }

        BrowserInformation browserInformation;
        WebDriver realDriver = driver;
        if (EventFiringWebDriver.class.isAssignableFrom(driver.getClass())) {
            realDriver = ((EventFiringWebDriver) driver).getWrappedDriver();
        }

        if (ProvidesBrowserInformation.class.isAssignableFrom(realDriver.getClass())) {
            browserInformation = ((ProvidesBrowserInformation) realDriver).getBrowserInformation();
        } else if (JavascriptExecutor.class.isAssignableFrom(realDriver.getClass())) {
            String userAgentString = "unknown";
            try {
                userAgentString = (String) ((JavascriptExecutor) realDriver).executeScript("return navigator.userAgent;");
            } catch (Exception e) {
                LOGGER.error("Error requesting user agent", e);
            }

            browserInformation = new UapBrowserInformation();
            browserInformation.parseUserAgent(userAgentString);
        } else {
            browserInformation = new UapBrowserInformation();
        }

        CACHED_BROWSER_INFOS.put(driver, browserInformation);
        return browserInformation;
    }

    static void removeCachedBrowserInformation(WebDriver eventFiringWebDriver) {
        if (CACHED_BROWSER_INFOS.containsKey(eventFiringWebDriver)) {
            CACHED_BROWSER_INFOS.remove(eventFiringWebDriver);
        }
    }

    /**
     * Quit WebDriver Session.
     *
     * @param driver .
     */
    protected static void quitWebDriverSession(final WebDriver driver) {
        try {
            driver.quit();
        } catch (final Throwable e) {
            LOGGER.warn("WebDriver could not be quit. May someone did before.", e);
        } finally {
            removeCachedBrowserInformation(driver);
        }
    }

    public static void addBrowserVersionToCapabilities(final DesiredCapabilities capabilities, final String version) {
//        capabilities.setCapability(CapabilityType.VERSION, version);
//        capabilities.setCapability(CapabilityType.BROWSER_VERSION, version);
        capabilities.setVersion(version);
    }

    /**
     * Add proxy settings to capabilities. proxyString may be "proxyhost:8080".
     *
     * @param capabilities .
     * @param proxyString .
     * @param noProxyString .
     * @deprecated
     */
    @Deprecated
    public static void addProxyToCapabilities(final DesiredCapabilities capabilities, final String proxyString, final String noProxyString) {
        addProxyToCapabilities(capabilities, proxyString);
    }

    /**
     * Add proxy settings to capabilities. proxyString may be "proxyhost:8080".
     *
     * @param capabilities {@link DesiredCapabilities}
     * @param proxyUrl {@link URL}
     * @deprecated
     */
    @Deprecated
    public static void addProxyToCapabilities(final DesiredCapabilities capabilities, final URL proxyUrl) {
        WebDriverProxyUtils utils = new WebDriverProxyUtils();
        capabilities.setCapability(CapabilityType.PROXY, utils.createSocksProxyFromUrl(proxyUrl));
    }

    /**
     * Add proxy settings to capabilities. proxyString may be "proxyhost:8080".
     *
     * @param capabilities {@link DesiredCapabilities}
     * @param proxyString {@link String}
     * @deprecated
     */
    @Deprecated
    public static void addProxyToCapabilities(final DesiredCapabilities capabilities, final String proxyString) {

        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyString);
        proxy.setFtpProxy(proxyString);
        proxy.setSslProxy(proxyString);
        //        proxy.setSocksProxy(proxyString);

        //        JSONArray a = new JSONArray();
        //        a.put(noProxyString);
        //        proxy.setNoProxy(a + "");
        capabilities.setCapability(CapabilityType.PROXY, proxy);
    }


    /**
     * @deprecated Use {@link WebDriverSessionsManager#getSessionContext(WebDriver)} instead
     */
    public static String getSessionKey(WebDriver driver) {

        return WebDriverSessionsManager.getSessionKey(driver);
    }

}


