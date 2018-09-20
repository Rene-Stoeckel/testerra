package eu.tsystems.mms.tic.testframework.report.pageobjects.dashboard.modules;

import eu.tsystems.mms.tic.testframework.exceptions.FennecRuntimeException;
import eu.tsystems.mms.tic.testframework.pageobjects.Check;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.report.model.TestResultHelper;
import eu.tsystems.mms.tic.testframework.report.abstracts.AbstractFramePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DashboardModuleMethodChart extends AbstractFramePage {

    @Check
    public final GuiElement methodChart = new GuiElement(this.driver, By.id("detailsView"), mainFrame);

    //method chart elements
    public final GuiElement methodChartRepairedFailsIndication = new GuiElement(this.driver, By.xpath("//a[contains(@href, 'test_TestStatePassed2')]//div[@class='skipped']"), mainFrame);
    public final GuiElement methodChartFailedRetried1 = new GuiElement(this.driver, By.xpath("//tr[@class='header broken']/following-sibling::tr[1]"), mainFrame);
    public final GuiElement methodChartFailedRetried2 = new GuiElement(this.driver, By.xpath("//tr[@class='header broken']/following-sibling::tr[2]"), mainFrame);
    public final GuiElement methodChartFailedRetried3 = new GuiElement(this.driver, By.xpath("//tr[@class='header broken']/following-sibling::tr[3]"), mainFrame);
    public final GuiElement methodChartFailedRetried4 = new GuiElement(this.driver, By.xpath("//tr[@class='header broken']/following-sibling::tr[4]"), mainFrame);
    public final GuiElement methodChartSuccessfulRetried = new GuiElement(this.driver, By.xpath("//tr[@class='header passed']/following-sibling::tr[1]"), mainFrame);
    public final GuiElement methodChartTable = methodChart.getSubElement(By.xpath("./table[@class='textleft resultsTable']"));
    public final GuiElement methodChartFailedMethodsTable = methodChart.getSubElement(By.className("filterFailed"));
    public final GuiElement methodChartPassedMethodsTable = methodChart.getSubElement(By.className("filterPassed"));
    public final GuiElement methodChartSkippedMethodsTable = methodChart.getSubElement(By.className("filterSkipped"));

    public DashboardModuleMethodChart(WebDriver driver) {
        super(driver);
    }

    /**
     * Returns all currently displayed test methods.
     *
     * @return a List of GuiElements containing displayed test methods
     */
    public List<GuiElement> getCurrentMethods() {
        List<GuiElement> methods = new LinkedList<>();
        int methodCount = methodChartTable.getSubElement(By.xpath(".//a[contains(@href, 'methods')]")).getNumberOfFoundElements();
        for (int i = 1; i <= methodCount; i++) {
            methods.add(methodChartTable.getSubElement(By.xpath("(.//a[contains(@href, 'methods')])[" + i + "]")));
        }
        return methods;
    }

    /**
     * Returns the method chart GuiElement by a given method name.
     *
     * @param methodName the name of a method
     * @return a method chart GuiElement
     */
    public GuiElement getMethodChartElementRowByMethodName(String methodName) {
        GuiElement methodElement = new GuiElement(driver, By.linkText(methodName), mainFrame);
        methodElement.setName("methodElementFor_" + methodName);
        return methodElement.getSubElement(By.xpath("./../.."));
    }

    /**
     * Checks the display status of a method chart by a given test result.
     *
     * @param testResult representing Failed, Passed or Skipped
     */
    public void assertMethodChartIsDisplayedForTestResult(TestResultHelper.TestResult testResult) {
        switch (testResult) {
            case FAILED:
                methodChartFailedMethodsTable.asserts().assertIsDisplayed();
                break;
            case PASSED:
                methodChartPassedMethodsTable.asserts().assertIsDisplayed();
                break;
            case SKIPPED:
                methodChartSkippedMethodsTable.asserts().assertIsDisplayed();
                break;
            default:
                throw new FennecRuntimeException("Method not implemented for TestResult: " + testResult);
        }
    }

    /**
     * Returns the method quantity for a method chart by a given test result.
     *
     * @param testResult representing Failed, Passed or Skipped
     * @return the method count
     */
    public int getNumberMethodsInMethodChartForTestResult(TestResultHelper.TestResult testResult) {
        switch (testResult) {
            case FAILED:
                return methodChartFailedMethodsTable.findElements(By.tagName("tr")).size() - 1;
            case PASSED:
                return methodChartPassedMethodsTable.findElements(By.tagName("tr")).size() - 1;
            case SKIPPED:
                return methodChartSkippedMethodsTable.findElements(By.tagName("tr")).size() - 1;
            default:
                throw new FennecRuntimeException("Method not implemented for TestResult: " + testResult);
        }
    }

    public List<String> getDisplayedMethodNamesForMethod(String methodName) {
        List<WebElement> displayedMethodTableEntries = methodChartFailedMethodsTable.findElements(By.tagName("a"));
        List<String> displayedMethodNames = new ArrayList<>();
        for (WebElement displayedMethod : displayedMethodTableEntries) {
            // TODO does not work
            String displayedMethodName = displayedMethod.getText();
            if (displayedMethodName.contains(methodName)) {
                displayedMethodNames.add(displayedMethodName);
            }
        }
        return displayedMethodNames;
    }
}
