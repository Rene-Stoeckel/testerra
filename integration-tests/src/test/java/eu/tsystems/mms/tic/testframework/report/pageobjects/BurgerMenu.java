package eu.tsystems.mms.tic.testframework.report.pageobjects;

import eu.tsystems.mms.tic.testframework.pageobjects.Check;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.factory.PageFactory;
import eu.tsystems.mms.tic.testframework.report.pageobjects.abstracts.AbstractReportPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * This class represents the Page Object for the expandable menu in the right upper corner of a TesterraReportPage
 */
public class BurgerMenu extends AbstractReportPage {

    @Check
    private GuiElement exitPointsLink = new GuiElement(this.getWebDriver(), By.id("ExitPoints"), mainFrame);
    private GuiElement logsLink = new GuiElement(this.getWebDriver(), By.id("Logs"), mainFrame);
    private GuiElement timingsLink = new GuiElement(this.getWebDriver(), By.id("Timings"), mainFrame);
    private GuiElement monitorLink = new GuiElement(this.getWebDriver(), By.id("JVM Monitor"), mainFrame);



    /**
     * Constructor called bei PageFactory
     *
     * @param driver Webdriver to use for this Page
     */
    public BurgerMenu(WebDriver driver) {
        super(driver);
    }

    /**
     * Method to navigate to the ExitPointsPage
     *
     * @return
     */
    public ExitPointsPage openExitPointsPage() {
        exitPointsLink = exitPointsLink.getSubElement(By.xpath("./a"));
        exitPointsLink.click();
        return PageFactory.create(ExitPointsPage.class, this.getWebDriver());
    }

    /**
     * Method to navigate to the LogsPage
     *
     * @return
     */
    public LogsPage openLogsPage() {
        logsLink = logsLink.getSubElement(By.xpath("./a"));
        logsLink.click();
        return PageFactory.create(LogsPage.class, this.getWebDriver());
    }

    /**
     * Method to navigate to the TimingsPage
     *
     * @return
     */
    public TimingsPage openTimingsPage() {
        timingsLink = timingsLink.getSubElement(By.xpath("./a"));
        timingsLink.click();
        return PageFactory.create(TimingsPage.class, this.getWebDriver());
    }

    /**
     * Method to navigate to the MonitorPage
     *
     * @return
     */
    public MonitorPage openMonitorPage() {
        monitorLink = monitorLink.getSubElement(By.xpath("./a"));
        monitorLink.click();
        return PageFactory.create(MonitorPage.class, this.getWebDriver());
    }


}