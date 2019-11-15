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
 *     Peter Lehmann
 *     pele
 */
/*
 * Created on 23.02.2012
 *
 * Copyright(c) 2011 - 2011 T-Systems Multimedia Solutions GmbH
 * Riesaer Str. 5, 01129 Dresden
 * All rights reserved.
 */
package eu.tsystems.mms.tic.testframework.utils;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.constants.GuiElementType;
import eu.tsystems.mms.tic.testframework.constants.TestOS;
import eu.tsystems.mms.tic.testframework.constants.TesterraProperties;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraSystemException;
import eu.tsystems.mms.tic.testframework.internal.Constants;
import eu.tsystems.mms.tic.testframework.internal.Flags;
import eu.tsystems.mms.tic.testframework.internal.Viewport;
import eu.tsystems.mms.tic.testframework.remote.RemoteDownloadPath;
import eu.tsystems.mms.tic.testframework.report.IReport;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.context.Screenshot;
import eu.tsystems.mms.tic.testframework.report.model.context.report.Report;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.sikuli.api.ScreenLocation;
import org.sikuli.api.ScreenRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Helper class containing some util methods for tt.
 *
 * @author pele
 */
public class UITestUtils {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UITestUtils.class);

    /**
     * Error string for screenshots.
     */
    private static final String ERROR_TAKING_SCREENSHOT = "Error taking screenshot.";

    /**
     * A date format for files like screenshots.
     */
    private static final DateFormat FILES_DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy__HH_mm_ss");

    private static final String BROWSER_DOWNLOAD_PATH_UUID = UUID.randomUUID().toString();

    @Deprecated
    private static final boolean STITCH = PropertyManager.getBooleanProperty(TesterraProperties.STITCH_CHROME_SCREENSHOTS, true);

    private static final IReport report = Testerra.injector.getInstance(IReport.class);

    public static Screenshot takeScreenshot(WebDriver webDriver) {
        return takeScreenshot(webDriver, false);
    }

    public static void takeScreenshot(WebDriver webDriver, Screenshot screenshot) {
        takeScreenshot(
            screenshot,
            webDriver,
            webDriver.getWindowHandle(),
            WebDriverManager.getSessionKeyFrom(webDriver)
        );
    }

    @Deprecated
    public static Screenshot takeScreenshot(
        WebDriver webDriver,
        boolean intoReport
    ) {
        Screenshot screenshot = takeScreenshot(
            webDriver,
            webDriver.getWindowHandle(),
            WebDriverManager.getSessionKeyFrom(webDriver)
        );
        if (intoReport) {
            if (screenshot != null) {
                report.addScreenshot(screenshot, IReport.Mode.MOVE);
            }
        }

        return screenshot;
    }

    public static void takeScreenshot(
        Screenshot screenshot,
        WebDriver eventFiringWebDriver,
        String originalWindowHandle,
        String sessionKey
    ) {
        try {
            takeWebDriverScreenshotToFile(eventFiringWebDriver, screenshot.getScreenshotFile());

            // get page source (webdriver)
            String pageSource = eventFiringWebDriver.getPageSource();

            if (pageSource == null) {
                LOGGER.error("getPageSource() returned nothing, skipping to add page source");
            } else {

                // save page source to file
                savePageSource(pageSource, screenshot.getPageSourceFile());
            }

                /*
                get infos
                 */
            if (sessionKey != null) {
                screenshot.meta().put(Screenshot.Meta.SESSION_KEY.toString(), sessionKey);
            }
            screenshot.meta().put(Screenshot.Meta.TITLE.toString(), eventFiringWebDriver.getTitle());

            /*
            window and focus infos
             */
            String window = "";
            String windowHandle = eventFiringWebDriver.getWindowHandle();
            if (originalWindowHandle != null) {
                if (windowHandle.equals(originalWindowHandle)) {
                    screenshot.meta().put(Screenshot.Meta.DRIVER_FOCUS.toString(), "true");
                } else {
                    screenshot.meta().put(Screenshot.Meta.DRIVER_FOCUS.toString(), "false");
                }
            }
            Set<String> windowHandles = eventFiringWebDriver.getWindowHandles();
            if (windowHandles.size() < 2) {
                window = "#1/1";
            } else {
                String[] handleStrings = windowHandles.toArray(new String[0]);
                for (int i = 0; i < handleStrings.length; i++) {
                    if (handleStrings[i].equals(windowHandle)) {
                        window = "#" + (i + 1) + "/" + handleStrings.length;
                    }
                }
            }

            String currentUrl = eventFiringWebDriver.getCurrentUrl();
            screenshot.meta().put(Screenshot.Meta.WINDOW.toString(), window);
            screenshot.meta().put(Screenshot.Meta.URL.toString(), currentUrl);
        } catch (final Exception e) {
            LOGGER.error(ERROR_TAKING_SCREENSHOT, e);
        }
    }

    public static Screenshot takeScreenshot(
        WebDriver eventFiringWebDriver,
        String originalWindowHandle,
        String sessionKey
    ) {
        if (!IReport.Properties.SCREENSHOTTER_ACTIVE.asBool()) {
            return null;
        }

        WebDriverRequest webDriverRequest = WebDriverManager.getRelatedWebDriverRequest(eventFiringWebDriver);
        if (Browsers.htmlunit.equalsIgnoreCase(webDriverRequest.browser)) {
            LOGGER.warn("Not taking screenshot for htmunit");
            return null;
        }

        /*
         * Take the screenshot
         */
        if (eventFiringWebDriver != null) {
            Screenshot screenshot = new Screenshot();
            takeScreenshot(
                screenshot,
                eventFiringWebDriver,
                originalWindowHandle,
                sessionKey
            );
            return screenshot;
        } else {
            LOGGER.info("No screenshot was taken. WebDriver is not active.");
        }

        return null;
    }

    public static void takeWebDriverScreenshotToFile(WebDriver eventFiringWebDriver, File screenShotTargetFile) {
        WebDriver driver;
        if (eventFiringWebDriver instanceof EventFiringWebDriver) {
            driver = ((EventFiringWebDriver) eventFiringWebDriver).getWrappedDriver();
        } else {
            // just to be able to execute
            driver = eventFiringWebDriver;
        }

        /*
         * The ChromeDriver doesn't support Screenshots of a large Site currently (Selenium 2.44), only the viewport ist
         * captured. To allow full-page screenshots, we stitch several viewport-screenshots together.
         * If this is eventually supported by WebDriver, this special branch can be removed.
         */
        WebDriverRequest relatedWebDriverRequest = WebDriverManager.getRelatedWebDriverRequest(eventFiringWebDriver);
        String browser = relatedWebDriverRequest.browser;
        if (Browsers.chrome.equalsIgnoreCase(browser) && STITCH) {
            makeStitchedChromeScreenshot(driver, screenShotTargetFile);
            return;
        } else if (Browsers.ie.equalsIgnoreCase(browser)) {
            Viewport viewport = JSUtils.getViewport(driver);

            if (viewport.height > Constants.IE_SCREENSHOT_LIMIT) {
                LOGGER.warn("IE: Not taking screenshot because screen size is larger than height limit of " + Constants.IE_SCREENSHOT_LIMIT);
                return;
            }
        }

        // take screenshot
        makeSimpleScreenshot(driver, screenShotTargetFile);
    }

    private static void makeSimpleScreenshot(WebDriver driver, File screenShotTargetFile) {
        try {
            File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            if (file != null) {
                FileUtils.moveFile(file, screenShotTargetFile);
            }
        } catch (WebDriverException e) {
            LOGGER.error("Could not get screenshot: "+ e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.error("Error moving screenshot: " + e.getLocalizedMessage());
        }
    }

    @Deprecated
    private static void makeStitchedChromeScreenshot(WebDriver driver, File screenShotTargetFile) {
        /*
        deactivated since it is not working correctly - pele 05.12.2017
         */

        makeSimpleScreenshot(driver, screenShotTargetFile);
    }

    /**
     * Utility to store a Screenshot at the specified location.
     *
     * @param image      BufferedImage
     * @param targetFile filePath with fileName
     */
    private static void saveBufferedImage(BufferedImage image, File targetFile) {
        try {
            ImageIO.write(image, "png", targetFile);
        } catch (final FileNotFoundException ex) {
            LoggerFactory.getLogger(UITestUtils.class).warn(
                    ("Screenshot file could not be written to file system: " + ex.toString()));
        } catch (final IOException ioe) {
            LoggerFactory.getLogger(UITestUtils.class).warn(ioe.toString());
        }
    }

    /**
     * Save page source to file.
     *
     * @param pageSource       page source.
     * @param sourceTargetFile target file.
     */
    private static void savePageSource(final String pageSource, final File sourceTargetFile) {
        try {
            final FileOutputStream fos = new FileOutputStream(sourceTargetFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos, "UTF-8");
            outputStreamWriter.write(pageSource);
            outputStreamWriter.close();
        } catch (final FileNotFoundException ex) {
            LoggerFactory.getLogger(UITestUtils.class).warn(
                    ("PageSource file could not be written to file system: " + ex.toString()));
        } catch (final IOException ioe) {
            LoggerFactory.getLogger(UITestUtils.class).warn(ioe.toString());
        }
    }

    public static void takeScreenshot(ScreenRegion screenRegion) {
        if (screenRegion != null) {
            LOGGER.info("Taking screenshot from desktop");
            final ScreenLocation upperLeftCorner = screenRegion.getUpperLeftCorner();
            final ScreenLocation lowerRightCorner = screenRegion.getLowerRightCorner();
            final BufferedImage screenshot = screenRegion.getScreen().getScreenshot(upperLeftCorner.getX(), upperLeftCorner.getY(), lowerRightCorner.getX(), lowerRightCorner.getY());

            final String filename = "Desktop_" + FILES_DATE_FORMAT.format(new Date()) + ".png";
            final File targetFile = new File(Report.SCREENSHOTS_DIRECTORY, filename);
            saveBufferedImage(screenshot, targetFile);

            final MethodContext methodContext = ExecutionContextController.getCurrentMethodContext();
            if (methodContext != null) {
                Screenshot screenshot1 = new Screenshot();
                screenshot1.filename = filename;
                methodContext.screenshots.add(screenshot1);
            }
        } else {
            LOGGER.error("Could not take native screenshot, screen region is missing");
        }
    }

    /**
     * Return the browser download Directory for this session. This contains a uuid which is statically created.
     *
     * @param platform OS the browser will run on
     *
     * @return session based download path.
     */
    public static RemoteDownloadPath getStaticBrowserDownloadDirectory(TestOS platform) {
        String uuid = BROWSER_DOWNLOAD_PATH_UUID;
        String fullPath = eu.tsystems.mms.tic.testframework.constants.RTConstants.getDownloadPathByOS(platform) + uuid;
        return new RemoteDownloadPath(fullPath, uuid);
    }

    /**
     * Return the browser download Directory for this session. This contains a uuid which is created from string parameter.
     *
     * @param platform OS the browser will run on
     *
     * @return session based download path.
     */
    public static RemoteDownloadPath generateBrowserDownloadDirectory(TestOS platform) {
        String uuid = UUID.randomUUID().toString();
        String fullPath = eu.tsystems.mms.tic.testframework.constants.RTConstants.getDownloadPathByOS(platform) + uuid;
        return new RemoteDownloadPath(fullPath, uuid);
    }

    private static GuiElementType guiElementType = null;

    public static void loadGuiElementType() {
        String guiElementTypeProperty = PropertyManager.getProperty(TesterraProperties.GUIELEMENT_TYPE, "sequence");
        try {
            guiElementType = GuiElementType.valueOf(guiElementTypeProperty);
        } catch (IllegalArgumentException e) {
            String errorMessage = "GuiElementType not supported: " + guiElementTypeProperty;
            throw new TesterraSystemException(errorMessage);
        }
    }

    /**
     * checks if gui element type gets supported
     *
     * @return gui element type
     */
    public static GuiElementType getGuiElementType() {
        if (guiElementType == null) {
            loadGuiElementType();
        }
        return guiElementType;
    }

    public static List<Screenshot> takeScreenshotsFromSessions(
            final MethodContext methodContext,
            final Map<String, WebDriver> rawWebDriverInstances,
        final boolean explicitly
    ) {
        List<String> processedWebDriverSessions = new ArrayList<>(1);
        List<Screenshot> screenshots = new LinkedList<>();
        if (rawWebDriverInstances != null) {
            // iterate through driver instances
            for (final String sessionKey : rawWebDriverInstances.keySet()) {
                if (!processedWebDriverSessions.contains(sessionKey)) { // already processed
                    final WebDriver driver = rawWebDriverInstances.get(sessionKey);
                    try {
                        List<Screenshot> screenshotsForSession = pTakeAllScreenshotsForSession(sessionKey, driver);
                        screenshots.addAll(screenshotsForSession);
                    } catch (Exception e) {
                        LOGGER.warn("Could not take screenshot from session: " + sessionKey, e);
                    }
                }
            }
        }

        if (methodContext != null) {
            // which means we have to publish the screenshots
            if (explicitly) {
                screenshots.forEach(screenshot -> screenshot.errorContextId = methodContext.id);
            }

            methodContext.addScreenshots(screenshots);
        }

        return screenshots;
    }

    private static List<Screenshot> pTakeAllScreenshotsForSession(String sessionKey, WebDriver driver) {

        final List<Screenshot> screenshots = new LinkedList<>();

        String originalWindowHandle = null;
        Set<String> windowHandles = null;
        if (driver != null) {
            // get actual window to switch back later
            try {
                originalWindowHandle = driver.getWindowHandle();
            } catch (Exception e) {
                LOGGER.error("Error getting actual window handle from driver", e);
            }
            // get all windows
            if (driver.getWindowHandles().size() > 0) {
                windowHandles = driver.getWindowHandles();
            }
        }

        if (windowHandles != null) {
            for (String windowHandle : windowHandles) {
                // switch to
                try {
                    driver.switchTo().window(windowHandle);
                    Screenshot screenshot = takeScreenshot(driver, originalWindowHandle, sessionKey);
                    if (screenshot != null) {
                        screenshots.add(screenshot);
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to switch to window " + windowHandle + " and take a screenshot", e);
                }
            }

            // switch back to original window handle
            try {
                driver.switchTo().window(originalWindowHandle);
            } catch (Exception e) {
                LOGGER.error("Unable to switch back to original window handle after taking all screenshots", e);
            }
        }

        return screenshots;
    }

    /**
     * initialize properties for a Perf test with default values if PERF_TEST is true.
     * all properties can be overwritten with the values in test.properties
     */
    public static void initializePerfTest() {
        boolean perfTest = PropertyManager.getBooleanProperty(TesterraProperties.PERF_TEST, false);
        if (perfTest) {
            Properties fileProperties = PropertyManager.getFileProperties();
            String value = fileProperties.getProperty(TesterraProperties.PERF_GENERATE_STATISTICS, "true");
            fileProperties.setProperty(TesterraProperties.PERF_GENERATE_STATISTICS, value);

            /* Override initial value of flag */
            boolean isPropertyActive = PropertyManager.getBooleanProperty(TesterraProperties.PERF_GENERATE_STATISTICS);
            Flags.GENERATE_PERF_STATISTICS = isPropertyActive;

            value = fileProperties.getProperty(TesterraProperties.CLOSE_WINDOWS_AFTER_TEST_METHODS, "false");
            fileProperties.setProperty(TesterraProperties.CLOSE_WINDOWS_AFTER_TEST_METHODS, value);

            value = fileProperties.getProperty(TesterraProperties.GUIELEMENT_TYPE, "perf");
            fileProperties.setProperty(TesterraProperties.GUIELEMENT_TYPE, value);

            // in case of perfTest reuse driver instances for dataprovider threads
            value = fileProperties.getProperty(TesterraProperties.REUSE_DATAPROVIDER_DRIVER_BY_THREAD, "true");
            fileProperties.setProperty(TesterraProperties.REUSE_DATAPROVIDER_DRIVER_BY_THREAD, value);

            /* Override initial value of flag */
            isPropertyActive = PropertyManager.getBooleanProperty(TesterraProperties.REUSE_DATAPROVIDER_DRIVER_BY_THREAD);
            Flags.REUSE_DATAPROVIDER_DRIVER_BY_THREAD = isPropertyActive;
        }
    }

    /**
     * Make screenshots from all open browser windows/selenium, webdriver instances.
     *
     * @return ScreenshotPaths.
     */
    public static List<Screenshot> takeScreenshots() {
        return takeScreenshots(true);
    }

    /**
     * Make screenshots from all open browser windows/selenium, webdriver instances.
     *
     * @param publishToReport True for publish directly into report.
     *
     * @return ScreenshotPaths.
     */
    public static List<Screenshot> takeScreenshots(final boolean publishToReport) {
        MethodContext methodContext = null;
        if (publishToReport) {
            methodContext = ExecutionContextController.getCurrentMethodContext();
        }
        return takeScreenshots(methodContext, false);
    }

    /**
     * Take screenshots from all windows and store them into the info container.
     *
     * @param methodContext
     * @return
     * @deprecated This method should not be public
     */
    @Deprecated
    public static List<Screenshot> takeScreenshots(final MethodContext methodContext, boolean explicitlyForThisContext) {
        long threadId = Thread.currentThread().getId();
        List<WebDriver> webDriversFromThread = WebDriverManager.getWebDriversFromThread(threadId);
        Map<String, WebDriver> webDriverSessions = new HashMap<>(webDriversFromThread.size());
        for (WebDriver webDriver : webDriversFromThread) {
            String sessionKey = WebDriverManager.getSessionKeyFrom(webDriver);
            webDriverSessions.put(sessionKey, webDriver);
        }

        if (webDriversFromThread.size() == 0) {
            LOGGER.warn("No webdriver or selenium session found. Could not take screenshot(s).");
        }

        return UITestUtils.takeScreenshotsFromSessions(methodContext, webDriverSessions, explicitlyForThisContext);
    }
}
