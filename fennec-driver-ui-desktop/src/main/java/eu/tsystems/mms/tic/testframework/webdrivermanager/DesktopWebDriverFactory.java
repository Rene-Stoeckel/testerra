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
package eu.tsystems.mms.tic.testframework.webdrivermanager;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.constants.Constants;
import eu.tsystems.mms.tic.testframework.constants.ErrorMessages;
import eu.tsystems.mms.tic.testframework.constants.FennecProperties;
import eu.tsystems.mms.tic.testframework.exceptions.FennecRuntimeException;
import eu.tsystems.mms.tic.testframework.exceptions.FennecSetupException;
import eu.tsystems.mms.tic.testframework.exceptions.FennecSystemException;
import eu.tsystems.mms.tic.testframework.internal.*;
import eu.tsystems.mms.tic.testframework.internal.utils.DriverStorage;
import eu.tsystems.mms.tic.testframework.internal.utils.TimingInfosCollector;
import eu.tsystems.mms.tic.testframework.model.NodeInfo;
import eu.tsystems.mms.tic.testframework.pageobjects.clickpath.ClickpathEventListener;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextUtils;
import eu.tsystems.mms.tic.testframework.sikuli.SikuliWebDriver;
import eu.tsystems.mms.tic.testframework.utils.StringUtils;
import eu.tsystems.mms.tic.testframework.utils.TestUtils;
import eu.tsystems.mms.tic.testframework.webdrivermanager.desktop.WebDriverMode;
import net.anthavio.phanbedder.Phanbedder;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by pele on 19.07.2017.
 */
public class DesktopWebDriverFactory implements WebDriverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopWebDriverFactory.class);

    @Override
    public WebDriver getRawWebDriver(WebDriverRequest webDriverRequest) {
        DesktopWebDriverRequest desktopWebDriverRequest;
        if (webDriverRequest instanceof DesktopWebDriverRequest) {
            desktopWebDriverRequest = (DesktopWebDriverRequest) webDriverRequest;
        }
        else if (webDriverRequest instanceof UnspecificWebDriverRequest) {
            desktopWebDriverRequest = new DesktopWebDriverRequest();
            desktopWebDriverRequest.copyFrom(webDriverRequest);
        }
        else {
            throw new FennecSystemException(webDriverRequest.getClass().getSimpleName() +  " is not allowed here");
        }

        /*
        start the session
         */
        WebDriver driver = startSession(desktopWebDriverRequest);

        /*
        Open url
         */
        String baseUrl = desktopWebDriverRequest.baseUrl;
        LOGGER.info("Opening baseUrl: " + baseUrl);
        StopWatch.startPageLoad(driver);
        try {
            driver.get(baseUrl);
        } catch (Exception e) {
            if (StringUtils.containsAll(e.getMessage(), true, "Reached error page", "connectionFailure")) {
                throw new FennecRuntimeException("Could not start driver session, because of unreachable url: " + desktopWebDriverRequest.baseUrl, e);
            }
            throw e;
        }

        return driver;
    }

    private WebDriver startSession(DesktopWebDriverRequest desktopWebDriverRequest) {
        /*
        set webdriver mode
         */
        if (desktopWebDriverRequest.webDriverMode == null) {
            desktopWebDriverRequest.webDriverMode = WebDriverManager.config().webDriverMode;
        }

        /*
        set base url
         */
        if (desktopWebDriverRequest.baseUrl == null) {
            desktopWebDriverRequest.baseUrl = WebDriverManager.getBaseURL();
        }

        /*
        if there is a factories entry for the requested browser, then create the new (raw) instance here and wrap it directly in EventFiringWD
         */
        if (Flags.REUSE_DATAPROVIDER_DRIVER_BY_THREAD) {
            String threadName = Thread.currentThread().getId() + "";
            String testMethodName = ExecutionContextUtils.getMethodNameFromCurrentTestResult();

            if (testMethodName != null) {
                WebDriver driver = DriverStorage.getDriverByTestMethodName(testMethodName, threadName);
                if (driver != null) {
                    LOGGER.info("Re-Using WebDriver for " + testMethodName + ": " + threadName + " driver: " + driver);

                    // cleanup session
                    driver.manage().deleteAllCookies();

                    /*
                    Open url
                     */
                    final String baseUrl = WebDriverManagerUtils.getBaseUrl(desktopWebDriverRequest.baseUrl);
                    LOGGER.info("Opening baseUrl with reused driver: " + baseUrl);
                    StopWatch.startPageLoad(driver);
                    driver.get(baseUrl);

                    return driver;
                } else {
                    return newWebDriver(desktopWebDriverRequest);
                }
            }
        } else {
            /*
            regular branch to create a new web driver session
             */
            return newWebDriver(desktopWebDriverRequest);
        }

        throw new FennecSystemException("WebDriverManager is in a bad state. Please report this to the fennec developers.");
    }

    @Override
    public void setupSession(EventFiringWebDriver eventFiringWebDriver, String sessionId, String browser) {
        // activate clickpath event listener
        eventFiringWebDriver.register(new ClickpathEventListener());

        // activate dynatrace event listener
        if (Flags.DYNATRACE_LOGGING) {
            eventFiringWebDriver.register(new DynatraceEventListener());
        }

        // add event listeners
        eventFiringWebDriver.register(new VisualEventDriverListener());
        eventFiringWebDriver.register(new EventLoggingEventDriverListener());

        /*
         start StopWatch
          */
        StopWatch.startPageLoad(eventFiringWebDriver);

        /*
         Maximize
         */
        if (WebDriverManager.config().maximize) {
            try {
                String res = Defaults.DISPLAY_RESOLUTION;
                LOGGER.error("maximize workaround -> Trying to set resolution to " + res);
                String[] split = res.split("x");
                int width = Integer.valueOf(split[0]);
                int height = Integer.valueOf(split[1]);
                eventFiringWebDriver.manage().window().setSize(new Dimension(width, height));
//                eventFiringWebDriver.manage().window().maximize();
            } catch (Exception e) {
                LOGGER.error("Could not maximize window", e);
            }
        }

        /*
         GET baseUrl
          */
        if (!Browsers.safari.equalsIgnoreCase(browser)) {
            int pageLoadTimeout = Constants.PAGE_LOAD_TIMEOUT_SECONDS;
            int scriptTimeout = PropertyManager.getIntProperty(FennecProperties.WEBDRIVER_TIMEOUT_SECONDS_SCRIPT, 120);
            try {
                eventFiringWebDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error("Could not set Page Load Timeout", e);
            }
            try {
                eventFiringWebDriver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error("Could not set Script Timeout", e);
            }
        } else {
            LOGGER.warn("Not setting timeouts for Safari.");
        }
    }

    WebDriver newWebDriver(DesktopWebDriverRequest desktopWebDriverRequest) {
        String sessionKey = desktopWebDriverRequest.sessionKey;

        final String url = getRemoteServerUrl(desktopWebDriverRequest);

        /*
         * Desired Capabilities, evtl. profiles...
         */
        final DesiredCapabilities capabilities = DesktopWebDriverCapabilities.createCapabilities(WebDriverManager.config(), desktopWebDriverRequest);

        String browser = desktopWebDriverRequest.browser;
        /*
         * Remote or local
         */
        WebDriver newDriver;
        if (desktopWebDriverRequest.webDriverMode == WebDriverMode.remote) {
            /*
             ##### Remote
             */

            URL remoteAddress;
            try {
                remoteAddress = new URL(url);
            } catch (final MalformedURLException e) {
                throw new FennecRuntimeException("MalformedUrlException while building Remoteserver URL: " + url, e);
            }

            /*
             * Start a new web driver session.
             */

            String msg = "on " + remoteAddress + " with capabilities: " + capabilities;
            Object ffprofile = capabilities.getCapability(FirefoxDriver.PROFILE);
            if (ffprofile != null && ffprofile instanceof FirefoxProfile) {
                try {
                    double size = ((double) ((FirefoxProfile) ffprofile).toJson().getBytes().length / 1024);
                    long sizeInKB = Math.round(size);
                    msg += "\n ffprofile size=" + sizeInKB + " KB";
                } catch (IOException e) {
                    // ignore silently
                }
            }

            try {
                switch (browser) {
                    case Browsers.htmlunit:
                        LOGGER.info("Starting HtmlUnitRemoteWebDriver.");
                        newDriver = new RemoteWebDriver(remoteAddress, capabilities);
                        break;
                    /*
                    Rest: fallthrough
                     */
                    case Browsers.firefox:
                    case Browsers.chrome:
                    case Browsers.chromeHeadless:
                    case Browsers.ie:
                    case Browsers.safari:
                    default:
                        newDriver = startNewWebDriverSession(browser, capabilities, remoteAddress, msg, sessionKey);
                }
            } catch (final FennecSetupException e) {
                int ms = Constants.WEBDRIVER_START_RETRY_TIME_IN_MS;
                LOGGER.error("Error starting SikuliWebDriver. Trying again in "
                        + (ms / 1000) + " seconds.", e);
                TestUtils.sleep(ms);
                newDriver = startNewWebDriverSession(browser, capabilities, remoteAddress, msg, sessionKey);
            }

        } else {
            /*
             ##### Local
             */
            String msg = "locally with capabilities: " + capabilities;
            newDriver = startNewWebDriverSession(browser, capabilities, null, msg, sessionKey);
        }

        /*
        Log session id
         */
        SessionId webDriverSessionId = ((RemoteWebDriver) newDriver).getSessionId();
        desktopWebDriverRequest.storedSessionId = webDriverSessionId.toString();
        LOGGER.info("Session ID: " + webDriverSessionId);

        /*
        Log User Agent and executing host
         */
        NodeInfo nodeInfo = DesktopWebDriverUtils.getNodeInfo(desktopWebDriverRequest);
        desktopWebDriverRequest.storedExecutingNode = nodeInfo;
        LOGGER.info("Executing Node " + nodeInfo.toString());
        WebDriverManager.addExecutingSeleniumHostInfo(sessionKey + ": " + nodeInfo.toString());
        WebDriverManagerUtils.logUserAgent(sessionKey, newDriver, nodeInfo);

        return newDriver;
    }

    public static String getRemoteServerUrl(DesktopWebDriverRequest desktopWebDriverRequest) {
        String host = StringUtils.getFirstValidString(desktopWebDriverRequest.seleniumServerHost, PropertyManager.getProperty(FennecProperties.SELENIUM_SERVER_HOST), "localhost");
        String port = StringUtils.getFirstValidString(desktopWebDriverRequest.seleniumServerPort, PropertyManager.getProperty(FennecProperties.SELENIUM_SERVER_PORT), "4444");
        String url = StringUtils.getFirstValidString(desktopWebDriverRequest.seleniumServerURL, PropertyManager.getProperty(FennecProperties.SELENIUM_SERVER_URL), "http://" + host + ":" + port + "/wd/hub");

        // set backwards
        try {
            URL url1 = new URL(url);
            host = url1.getHost();
            port = url1.getPort() + "";
        } catch (MalformedURLException e) {
            LOGGER.error("INTERNAL ERROR: Could not parse URL", e);
        }
        desktopWebDriverRequest.seleniumServerHost = host;
        desktopWebDriverRequest.seleniumServerPort = port;
        desktopWebDriverRequest.seleniumServerURL = url;
        return url;
    }

    /**
     * Remote when remoteAdress != null, local need browser to be set.
     *
     * @param browser       .
     * @param capabilities  .
     * @param remoteAddress .
     * @param msg           .
     * @return.
     */
    private WebDriver startNewWebDriverSession(String browser, DesiredCapabilities capabilities, URL remoteAddress,
                                                      String msg, String sessionKey) {
        /*
        get more capabilities info
         */
        Proxy proxy = (Proxy) capabilities.getCapability(CapabilityType.PROXY);
        if (proxy != null) {
            msg += "\nProxy:";
            msg += "\n http : " + proxy.getHttpProxy();
            msg += "\n ftp  : " + proxy.getFtpProxy();
            msg += "\n socks: " + proxy.getSslProxy();
            msg += "\n ssl  : " + proxy.getSocksProxy();
        }
        try {
            ChromeOptions chromeOptions = (ChromeOptions) capabilities.getCapability(ChromeOptions.CAPABILITY);
            if (chromeOptions != null) {
                msg += "\nChromeOptions:";
                msg += "\n " + chromeOptions.toString();
            }
        } catch (Exception e) {
            LOGGER.warn("Could not log chrome options", e);
        }

        WebDriver driver;
        LOGGER.info("Starting SikuliWebDriver (" + sessionKey + ") " + msg, new NewSessionMarker());
        org.apache.commons.lang3.time.StopWatch sw = new org.apache.commons.lang3.time.StopWatch();
        sw.start();

        final String errorMessage = "Error starting browser session";
        if (remoteAddress != null) {
            /*
            remote mode
             */
            try {
                driver = new SikuliWebDriver(remoteAddress, capabilities);
            } catch (Exception e) {
                throw new FennecSetupException(errorMessage, e);
            }

            // set local file detector
            ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
        } else if (browser != null) {
            /*
             local mode
              */
            switch (browser) {
                case Browsers.firefox:
                    driver = new FirefoxDriver(capabilities);
                    break;
                case Browsers.ie:
                    driver = new InternetExplorerDriver(capabilities);
                    break;
                case Browsers.chrome:
                    driver = new ChromeDriver(capabilities);
                    break;
                case Browsers.chromeHeadless:
                    driver = new ChromeDriver(capabilities);
                    break;
                case Browsers.htmlunit:
                    driver = new HtmlUnitDriver(capabilities);
                    break;
                case Browsers.phantomjs:
                    File phantomjsFile = getPhantomJSBinary();
                    capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsFile.getAbsolutePath());
                    driver = new PhantomJSDriver(capabilities);
                    break;
                case Browsers.safari:
                    driver = new SafariDriver(capabilities);
                    break;
                case Browsers.edge:
                    driver = new EdgeDriver(capabilities);
                    break;
                default:
                    throw new FennecSystemException(ErrorMessages.browserNotSupportedHere(browser));
            }
        } else {
            throw new FennecSystemException("Internal Error when starting webdriver.");
        }

        sw.stop();
        LOGGER.info("Session startup time: " + sw.toString());
        STARTUP_TIME_COLLECTOR.add(new TimingInfo("SessionStartup", "", sw.getTime(TimeUnit.MILLISECONDS), System.currentTimeMillis()));

        return driver;
    }

    public static final TimingInfosCollector STARTUP_TIME_COLLECTOR = new TimingInfosCollector();

    private static File phantomjsFile = null;

    private static File getPhantomJSBinary() {
        if (phantomjsFile == null) {
            LOGGER.info("Unpacking phantomJS...");
            phantomjsFile = Phanbedder.unpack(); //Phanbedder to the rescue!
            LOGGER.info("Unpacked phantomJS to: " + phantomjsFile);
        }
        return phantomjsFile;
    }

}
