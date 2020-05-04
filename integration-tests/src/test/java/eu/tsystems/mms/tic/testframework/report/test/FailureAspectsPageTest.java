package eu.tsystems.mms.tic.testframework.report.test;

import eu.tsystems.mms.tic.testframework.annotations.TestContext;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraRuntimeException;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.abstracts.AbstractFailurePointsPage;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.abstracts.AbstractResultTableFailureEntry;
import eu.tsystems.mms.tic.testframework.report.general.AbstractReportFailuresTest;
import eu.tsystems.mms.tic.testframework.report.general.ReportDirectory;
import eu.tsystems.mms.tic.testframework.report.general.SystemTestsGroup;
import eu.tsystems.mms.tic.testframework.report.pageobjetcs.FailureAspectsPage;
import eu.tsystems.mms.tic.testframework.report.workflows.GeneralWorkflow;
import eu.tsystems.mms.tic.testframework.report.model.FailureAspectEntry;
import eu.tsystems.mms.tic.testframework.report.model.ResultTableFailureType;
import eu.tsystems.mms.tic.testframework.report.model.TestReportTwoFailureAspects;
import eu.tsystems.mms.tic.testframework.report.model.TestReportTwoNumbers;
import eu.tsystems.mms.tic.testframework.report.model.TestResultHelper;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@TestContext(name = "View-FailureAspects")
public class FailureAspectsPageTest extends AbstractReportFailuresTest {

    @BeforeMethod(alwaysRun = true)
    @Override
    public void initTestObjects() {
        this.failurePointType = ResultTableFailureType.FAILURE_ASPECT;
        this.reportFilter = SystemTestsGroup.SYSTEMTESTSFILTER2;;
        this.failurePointEntryTestObjects = getExpectedFailurePointEntries();
    }

    @Override
    protected int getNumberOfExpectedFailurePointsForReport() {
        switch (reportFilter) {
            case SystemTestsGroup.SYSTEMTESTSFILTER2:
                return new TestReportTwoNumbers().getFailureAspects();
            default:
                throw new TesterraRuntimeException("Not implemented for Report: " + reportFilter);
        }
    }

    @Override
    protected int getNumberOfExpectedFailurePointsForTestResult(TestResultHelper.TestResultFailurePointEntryType entryType) {
        int counter = 0;
        switch (reportFilter) {
            case SystemTestsGroup.SYSTEMTESTSFILTER2:
                for (FailureAspectEntry failureAspect : TestReportTwoFailureAspects.getAllFailureAspectEntryTestObjects()) {
                    if (failureAspect.getFailurePointEntryType().equals(entryType)) {
                        counter++;
                    }
                }
                return counter;
            default:
                throw new TesterraRuntimeException("Not implemented for Report: " + reportFilter);
        }
    }

    @Override
    protected List<? extends AbstractResultTableFailureEntry> getExpectedFailurePointEntries() {
        switch (reportFilter) {
            case SystemTestsGroup.SYSTEMTESTSFILTER2:
                return TestReportTwoFailureAspects.getAllFailureAspectEntryTestObjects();
            default:
                throw new TesterraRuntimeException("Not implemented for Report: " + reportFilter);
        }
    }
    @Override
    protected void checkExpectedFailedMarkWorkflow(boolean intoReport) {
        final int failedNotIntoReportPosition = 4;
        final int failedIntoReportPosition = 6;
        final int position;
        if (intoReport) {
            position = failedIntoReportPosition;
        } else {
            position = failedNotIntoReportPosition;
        }
        final AbstractResultTableFailureEntry failedEntry = failurePointEntryTestObjects.get(position - 1);
        AbstractFailurePointsPage failurePointsPage = openFailuresPointsPage(ReportDirectory.REPORT_DIRECTORY_2);
        failurePointsPage.assertExpectedFailsReportMark(failedEntry, intoReport);
    }

    @Override
    protected AbstractFailurePointsPage openFailuresPointsPage(ReportDirectory reportDirectory) {
        return GeneralWorkflow.doOpenBrowserAndReportFailureAspectsPage(WebDriverManager.getWebDriver(), PropertyManager.getProperty(reportDirectory.getReportDirectory()));
    }

    @Test(groups = {SystemTestsGroup.SYSTEMTESTSFILTER2})
    public void testT09_checkInfoMessageOnRelatedFailureAspect(){
        FailureAspectsPage failureAspectsPage = GeneralWorkflow.doOpenBrowserAndReportFailureAspectsPage(
                WebDriverManager.getWebDriver(),
                PropertyManager.getProperty(ReportDirectory.REPORT_DIRECTORY_2.getReportDirectory())
        );
        final AbstractResultTableFailureEntry failedEntry = failurePointEntryTestObjects.get(5);
        failureAspectsPage.assertDescriptionsForFailurePointIsCorrect(failedEntry);
    }

}
