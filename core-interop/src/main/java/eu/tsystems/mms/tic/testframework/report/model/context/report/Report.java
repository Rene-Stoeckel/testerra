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
package eu.tsystems.mms.tic.testframework.report.model.context.report;

import eu.tsystems.mms.tic.testframework.common.IProperties;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraRuntimeException;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraSystemException;
import eu.tsystems.mms.tic.testframework.report.model.context.Screenshot;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Report {

    private static final Logger LOGGER = LoggerFactory.getLogger(Report.class);

    public static final File REPORT_DIRECTORY;

    public static final String FRAMES_FOLDER_NAME = "frames";
    public static final String METHODS_FOLDER_NAME = "methods";
    public static final String SCREENSHOTS_FOLDER_NAME = "screenshots";
    public static final String VIDEO_FOLDER_NAME = "videos";
    public static final String XML_FOLDER_NAME = "xml";

    public enum Properties implements IProperties {
        BASE_DIR("dir", "testerra-report"),
        SCREENSHOTS_PREVIEW("screenshots.preview", true),
        NAME("name", null),
        ACTIVATE_SOURCES("activate.sources", true),
        SCREENSHOTTER_ACTIVE("screenshotter.active", true),
        SCREENSHOT_ON_PAGELOAD("screenshot.on.pageload", false),
        SCREENCASTER_ACTIVE("screencaster.active", false),
        ;
        private final String property;
        private Object defaultValue;

        Properties(String property, Object defaultValue) {
            this.property = property;
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() {
            return String.format("tt.report.%s",property);
        }
        @Override
        public IProperties newDefault(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        @Override
        public Double asDouble() {
            return PropertyManager.parser.getDoubleProperty(toString(), defaultValue);
        }
        @Override
        public Long asLong() {
            return PropertyManager.parser.getLongProperty(toString(), defaultValue);
        }
        @Override
        public Boolean asBool() {
            return PropertyManager.parser.getBooleanProperty(toString(), defaultValue);
        }
        @Override
        public String asString() {
            return PropertyManager.parser.getProperty(toString(), defaultValue);
        }
    }

    static {
        /*
        Initialize report directory
         */
        final String relativeReportDir = Report.Properties.BASE_DIR.asString();
        REPORT_DIRECTORY = new File(relativeReportDir);

        // cleanup
        try {
            FileUtils.deleteDirectory(REPORT_DIRECTORY);
        } catch (IOException e) {
            throw new TesterraRuntimeException("Could not clean report dir.", e);
        }
        if (!REPORT_DIRECTORY.mkdirs()) {
            throw new TesterraSystemException(
                    "Error cleaning report dir: " + REPORT_DIRECTORY +
                            "\nCheck consoles or other directory and file accesses for locks.");
        }
    }

    public static final File FRAMES_DIRECTORY = new File(REPORT_DIRECTORY, FRAMES_FOLDER_NAME);
    public static final File METHODS_DIRECTORY = new File(FRAMES_DIRECTORY, METHODS_FOLDER_NAME);
    public static final File SCREENSHOTS_DIRECTORY = new File(REPORT_DIRECTORY, SCREENSHOTS_FOLDER_NAME);
    public static final File VIDEO_DIRECTORY = new File(REPORT_DIRECTORY, VIDEO_FOLDER_NAME);
    public static final File XML_DIRECTORY = new File(REPORT_DIRECTORY, XML_FOLDER_NAME);

    static {
        /*
        Initialize report sub directories
         */
        FRAMES_DIRECTORY.mkdirs();
        METHODS_DIRECTORY.mkdirs();
        SCREENSHOTS_DIRECTORY.mkdirs();
        VIDEO_DIRECTORY.mkdirs();
        XML_DIRECTORY.mkdirs();
    }

    public enum Mode {
        COPY,
        MOVE
    }

    /**
     * Adds a screenshot to the report
     * @param screenshotFile The screenshot file
     * @param screenshotSourceFileOrNull The source code of the screenshot origin
     * @param mode
     * @return Screenshot instance
     */
    public static Screenshot provideScreenshot(
        File screenshotFile,
        File screenshotSourceFileOrNull,
        Mode mode
    ) throws IOException {
        if (!screenshotFile.exists()) {
            LOGGER.error("Cannot provide screenshot: " + screenshotFile + " does not exist");
            return null;
        }
        if (screenshotSourceFileOrNull != null && !screenshotSourceFileOrNull.exists()) {
            LOGGER.warn("Cannot provide screenshot source: " + screenshotSourceFileOrNull + " does not exist");
            screenshotSourceFileOrNull = null;
        }

        final Screenshot screenshot = new Screenshot();

        /*
        provide screenshot
         */
        screenshot.filename = UUID.randomUUID() + "." + FilenameUtils.getExtension(screenshotFile.getName());
        screenshot.meta().put(Screenshot.Meta.FILE_NAME.toString(), screenshotFile.getName());
        final File targetScreenshotFile = new File(SCREENSHOTS_DIRECTORY, screenshot.filename);
        switch (mode) {
            case COPY:
                FileUtils.copyFile(screenshotFile, targetScreenshotFile, true);
                break;
            case MOVE:
                FileUtils.moveFile(screenshotFile, targetScreenshotFile);
                break;
        }


        /*
        provide source
         */
        if (screenshotSourceFileOrNull != null) {
            screenshot.sourceFilename = screenshot.filename + ".html";
            screenshot.meta().put(Screenshot.Meta.SOURCE_FILE_NAME.toString(), screenshotSourceFileOrNull.getName());
            final File targetSourceFile = new File(SCREENSHOTS_DIRECTORY, screenshot.sourceFilename);
            switch (mode) {
                case COPY:
                    FileUtils.copyFile(screenshotSourceFileOrNull, targetSourceFile, true);
                    break;
                case MOVE:
                    FileUtils.moveFile(screenshotSourceFileOrNull, targetSourceFile);
                    break;
            }

        }

        LOGGER.info("Provided screenshot " + screenshotFile + " as " + targetScreenshotFile);

        return screenshot;
    }

    public static Video provideVideo(
        File file,
        Mode mode
    ) throws IOException {
        if (!file.exists()) {
            LOGGER.error("Cannot provide video: " + file + " does not exist");
            return null;
        }

        final String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getName());

        File targetFile = new File(VIDEO_DIRECTORY, fileName);
        switch (mode) {
            case COPY:
                FileUtils.copyFile(file, targetFile, true);
                break;
            case MOVE:
                FileUtils.moveFile(file, targetFile);
                break;
        }

        Video video = new Video();
        video.filename = fileName;

        LOGGER.info("Provided video " + file + " as " + targetFile);

        return video;
    }

}
