package eu.tsystems.mms.tic.testframework.report.workflows;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.factory.PageFactory;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.ClassesDetailsPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.ClassesPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.DashboardPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.ExitPointsPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.FailureAspectsPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.MethodDetailsPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.MethodScreenshotPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.MethodStackPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.MethodStepsPage;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class GeneralWorkflow {

    protected static final Logger LOGGER = LoggerFactory.getLogger(GeneralWorkflow.class);

    public static URI getURIForReport(String reportDirectory) {
        LOGGER.debug("Calling getURIForReport method for report directory " + reportDirectory);
        try {
            return new URI(String.format("%s/%s/%s", WebDriverManager.getBaseURL(), reportDirectory, PropertyManager.getProperty("fileName")));
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to create report url", e);
        }
        return null;
    }

    /**
     * Opens the DashboardPage of the  Report for the defined Report.
     *
     * @param reportDirectory defines the REPORT_DIRECTORY for the page to open
     * @return Dashboard Page
     */
    public static DashboardPage doOpenBrowserAndReportDashboardPage(WebDriver webDriver, String reportDirectory) {
        LOGGER.debug("Open the dashboard page for the report directory " + reportDirectory);
        webDriver.navigate().to(getURIForReport(reportDirectory).toString());
        return PageFactory.create(DashboardPage.class, webDriver);
    }


    /**
     * Opens the ReportClassesPage of the  Report for the defined Report.
     *
     * @param reportDirectory defines the REPORT_DIRECTORY for the page to open
     * @return ReportClassesPage
     */
    public static ClassesPage doOpenBrowserAndReportClassesPage(WebDriver webDriver, String reportDirectory) {
        LOGGER.debug("Open the classes page for the report directory " + reportDirectory);
        DashboardPage dashboardPage = doOpenBrowserAndReportDashboardPage(webDriver, reportDirectory);
        ClassesPage classesPage = dashboardPage.goToClasses();
        return classesPage;
    }

    /**
     * Opens the ReportClassesPage of the  Report for the defined Report.
     *
     * @param reportDirectory        defines the REPORT_DIRECTORY for the page to open
     * @param testUnderTestClassName defines the class which details shall be shown
     * @return ReportClassesPage
     */
    public static ClassesDetailsPage doOpenBrowserAndReportClassesDetailsPage(WebDriver webDriver, String reportDirectory, String testUnderTestClassName) {
        LOGGER.debug("Open the classes detail page for the report directory " + reportDirectory + " and testundertest class " + testUnderTestClassName);
        DashboardPage dashboardPage = doOpenBrowserAndReportDashboardPage(webDriver, reportDirectory);
        ClassesPage classesPage = dashboardPage.goToClasses();
        ClassesDetailsPage classesDetailsPage = classesPage.gotoClassesDetailsPageForClass(testUnderTestClassName);
        return classesDetailsPage;
    }

    /**
     * Opens the ExitPointsPage of the  Report for the defined Report.
     *
     * @param reportDirectory defines the REPORT_DIRECTORY for the page to open
     * @return ExitPointsPage
     */
    public static ExitPointsPage doOpenBrowserAndReportExitPointsPage(WebDriver webDriver, String reportDirectory) {
        LOGGER.debug("Open the exit points page for the report directory " + reportDirectory);
        DashboardPage dashboardPage = doOpenBrowserAndReportDashboardPage(webDriver, reportDirectory);

        ExitPointsPage exitPointsPage = dashboardPage.goToExitPoints();
        return exitPointsPage;
    }

    public static MethodDetailsPage doOpenReportMethodDetailsPage(DashboardPage dashboardPage, GuiElement method) {
        return dashboardPage.clickMethodDetail(method);
    }

    public static MethodScreenshotPage doOpenReportMethodScreenshotPage(MethodDetailsPage methodDetailsPage) {
        return methodDetailsPage.clickScreenShotTab();
    }

    public static MethodStackPage doOpenReportStracktracePage(MethodDetailsPage methodDetailsPage) {
        return methodDetailsPage.clickStackTab();
    }

    public static MethodStepsPage doOpenReportStepsPage(MethodDetailsPage methodDetailsPage) {
        return methodDetailsPage.clickStepsTab();
    }

    public static FailureAspectsPage doOpenBrowserAndReportFailureAspectsPage(WebDriver webDriver, String reportDirectory) {
        DashboardPage dashboardPage = GeneralWorkflow.doOpenBrowserAndReportDashboardPage(webDriver, reportDirectory);
        return dashboardPage.goToFailureAspects();
    }

    public static MethodDetailsPage doOpenBrowserAndReportMethodDetailsPage(WebDriver webDriver, String reportDirectory, String className, String methodName) {
        DashboardPage dashboardPage = GeneralWorkflow.doOpenBrowserAndReportDashboardPage(webDriver, reportDirectory);
        dashboardPage.goToClasses().gotoClassesDetailsPageForClass(className).getDetailsLinkByMethodName(methodName).click();
        return PageFactory.create(MethodDetailsPage.class, webDriver);
    }

    public static MethodDetailsPage doOpenBrowserAndReportMethodDetailsPageWithTag(WebDriver webDriver, String reportDirectory, Class reportClass, String methodTagName) {
        DashboardPage dashboardPage = GeneralWorkflow.doOpenBrowserAndReportDashboardPage(webDriver, reportDirectory);
        dashboardPage.goToClasses().gotoClassesDetailsPageForClass(reportClass.getSimpleName()).getDetailsLinkByMethodNameWithTag(methodTagName).click();
        return PageFactory.create(MethodDetailsPage.class, webDriver);
    }
}