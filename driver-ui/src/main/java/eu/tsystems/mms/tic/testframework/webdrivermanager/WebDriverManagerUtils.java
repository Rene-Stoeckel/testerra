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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraRuntimeException;
import eu.tsystems.mms.tic.testframework.model.HostInfo;
import eu.tsystems.mms.tic.testframework.model.NodeInfo;
import eu.tsystems.mms.tic.testframework.report.model.BrowserInformation;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
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

    // url pattern for node proxy requests
    private static final String urlPattern = "http://<ip>:<port>/grid/api/<request>";

    /**
     * Hide constructor.
     */
    private WebDriverManagerUtils() {
    }

    /**
     * Computes the baseUrl to use from the possible sources.
     *
     * @param presetBaseURL A manually set baseUrl
     * @return BaseUrl to use.
     */
    protected static String getBaseUrl(final String presetBaseURL) {
        String baseUrl;
        /*
         * preset baseurl
         */
        if (presetBaseURL != null) {
            baseUrl = presetBaseURL;
        }
        /*
         * baseURL defined by System property or config file
         */
        else {
            baseUrl = Testerra.Properties.BASEURL.asString();
        }
        return baseUrl;
    }

    /**
     * Set Browser type + version in TestRunConfiguration and log infos.
     *
     * @param driver WebDriver or Selenium to get info from.
     */
    protected static void logNewUserAgent(
        final String sessionKey,
        final WebDriver driver,
        final HostInfo hostInfo
    ) {
        BrowserInformation browserInformation = getBrowserInformation(driver);
        LOGGER.info(String.format("New user agent: %s %s", browserInformation.getBrowserName(), browserInformation.getBrowserVersion()));

        SessionContext sessionContext = ExecutionContextController.getCurrentSessionContext();
        if (sessionContext != null) {
            sessionContext.metaData.put("browserInfo", getBrowserInformation(driver));
        } else {
            LOGGER.error("Expected a current active session");
        }
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
            browserInformation = Testerra.injector.getInstance(BrowserInformation.class);
            browserInformation.parseUserAgent(userAgentString);
        } else {
            browserInformation = Testerra.injector.getInstance(BrowserInformation.class);
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
            if (driver == null) {
                LOGGER.info("No WebDriver found. Maybe it has already been closed.");
            } else {
                driver.quit();
                removeCachedBrowserInformation(driver);
            }
        } catch (final Throwable e) {
            LOGGER.info("WebDriver could not be quit. May someone did before.", e);
        }
    }

    /**
     * deletes all cookies of the given Webdriver
     *
     * @param driver Webdriver
     */
    public static void deleteAllCookies(final WebDriver driver) {
        driver.manage().deleteAllCookies();
    }

    public static void addBrowserVersionToCapabilities(final DesiredCapabilities capabilities, final String version) {
        capabilities.setCapability(CapabilityType.VERSION, version);
        capabilities.setCapability(CapabilityType.BROWSER_VERSION, version);
    }

    /**
     * Add proxy settings to capabilities. proxyString may be "proxyhost:8080".
     *
     * @param capabilities  .
     * @param proxyString   .
     * @param noProxyString .
     * @see WebDriverManagerUtils#addProxyToCapabilities(DesiredCapabilities, URL)
     * @deprecated see {@link WebDriverManagerUtils#addProxyToCapabilities(DesiredCapabilities, URL)}
     */
    @Deprecated
    public static void addProxyToCapabilities(final DesiredCapabilities capabilities, final String proxyString, final String noProxyString) {
        addProxyToCapabilities(capabilities, proxyString);
    }

    /**
     * Add proxy settings to capabilities. proxyString may be "proxyhost:8080".
     *
     * @param capabilities {@link DesiredCapabilities}
     * @param proxyUrl     {@link URL}
     */
    public static void addProxyToCapabilities(final DesiredCapabilities capabilities, final URL proxyUrl) {

        final String proxyString = WebDriverProxyUtils.toProxyString(proxyUrl);
        addProxyToCapabilities(capabilities, proxyString);
    }

    /**
     * Add proxy settings to capabilities. proxyString may be "proxyhost:8080".
     *
     * @param capabilities {@link DesiredCapabilities}
     * @param proxyString  {@link String}
     * @deprecated see {@link #addProxyToCapabilities(DesiredCapabilities, URL)}
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


    public static String getSessionKey(WebDriver driver) {

        return WebDriverSessionsManager.getSessionKey(driver);
    }

}


