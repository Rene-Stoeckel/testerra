package eu.tsystems.mms.tic.testframework.report.abstracts;


import eu.tsystems.mms.tic.testframework.pageobjects.Check;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.factory.PageFactory;
import eu.tsystems.mms.tic.testframework.report.pageobjects.BurgerMenu;
import eu.tsystems.mms.tic.testframework.report.pageobjects.ClassesPage;
import eu.tsystems.mms.tic.testframework.report.pageobjects.DashboardPage;
import eu.tsystems.mms.tic.testframework.report.pageobjects.ExitPointsPage;
import eu.tsystems.mms.tic.testframework.report.pageobjects.FailureAspectsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Created by jlma on 26.10.2016.
 */
public abstract class AbstractReportPage extends AbstractFramePage {

    @Check
    private GuiElement burgerMenu = new GuiElement(this.getWebDriver(), By.id("menuToggle"), mainFrame);

    private GuiElement dashBoardLink = new GuiElement(this.getWebDriver(), By.linkText("DASHBOARD"), mainFrame);
    private GuiElement classesLink = new GuiElement(this.getWebDriver(), By.partialLinkText("CLASSES"), mainFrame);
    private GuiElement failureAspectsLink = new GuiElement(this.getWebDriver(), By.partialLinkText("FAILURE ASPECTS"), mainFrame);
    private GuiElement threadsLink = new GuiElement(this.getWebDriver(), By.partialLinkText("THREADS"), mainFrame);
    private GuiElement stateChangesLink = new GuiElement(this.getWebDriver(), By.partialLinkText("STATE CHANGES"), mainFrame);

    /**
     * Constructor called bei PageFactory
     *
     * @param driver Webdriver to use for this Page
     */
    public AbstractReportPage(WebDriver driver) {
        super(driver);
    }

    public DashboardPage goToDashboard() throws InterruptedException {
        this.dashBoardLink.click();
        return PageFactory.create(DashboardPage.class, this.getWebDriver());
    }

    public ClassesPage goToClasses() {
        this.classesLink.click();
        return PageFactory.create(ClassesPage.class, this.getWebDriver());
    }

    public ExitPointsPage goToExitPoints() {
        return this.openBurgerMenu().openExitPointsPage();
    }

    public FailureAspectsPage goToFailureAspects() {
        this.failureAspectsLink.click();
        return PageFactory.create(FailureAspectsPage.class, this.getWebDriver());
    }

    public BurgerMenu openBurgerMenu() {
        GuiElement burgerMenuButton = burgerMenu.getSubElement(By.xpath(".//input"));
        burgerMenuButton.setName("burgerMenuButton");
        burgerMenuButton.click();
        return PageFactory.create(BurgerMenu.class, this.getWebDriver());
    }
}
