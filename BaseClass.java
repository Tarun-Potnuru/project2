package baseClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
/*hello every one*/
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import utilities.DateUtils;
import utilities.ExtentReportManager;
import utilities.ReadExcelDataFile;

public class BaseClass {

	public WebDriver driver;
	public DesiredCapabilities capability;
	public Properties prop;
	public ExtentReports report = ExtentReportManager.getReportInstance();
	public ExtentTest logger;
	public boolean acceptNextAlert = true;
	SoftAssert softAssert = new SoftAssert();

	/********************* Invoke Browser *********************/
	public void invokeBrowser(String browserName) {

		readProp();

		if (prop.getProperty("useGrid").equalsIgnoreCase("false")) {
			invokeWithoutGrid(browserName);
		} else {
			invokeWithGrid(browserName);
		}

		driver.manage().timeouts().implicitlyWait(240, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.manage().timeouts().pageLoadTimeout(240, TimeUnit.SECONDS);

	}

	/********************* Read configProperties file *********************/
	public void readProp() {

		if (prop == null) {
			prop = new Properties();
			try {
				FileInputStream file = new FileInputStream(System.getProperty("user.dir")
						+ "//src//test//resources//ObjectRepository//projectConfig.properties");
				prop.load(file);
			} catch (Exception e) {
				reportFail(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/*********************
	 * Invoke Browser without Selenium Grid
	 *********************/
	public void invokeWithoutGrid(String browserName) {
		try {
			if (browserName.equalsIgnoreCase("chrome")) {
				ChromeOptions co = new ChromeOptions();
				co.addArguments("--disable-infobars");
				co.addArguments("--disable-notifications");
				System.setProperty("webdriver.chrome.driver",
						System.getProperty("user.dir") + "\\src\\test\\resources\\Drivers\\chromedriver.exe");
				driver = new ChromeDriver(co);
			} else if (browserName.equalsIgnoreCase("firefox")) {
				FirefoxOptions fo = new FirefoxOptions();
				fo.addPreference("dom.webnotifications.enabled", false);
				System.setProperty("webdriver.gecko.driver",
						System.getProperty("user.dir") + "\\src\\test\\resources\\Drivers\\geckodriver.exe");
				driver = new FirefoxDriver(fo);
			} else if (browserName.equalsIgnoreCase("opera")) {
				OperaOptions oo = new OperaOptions();
				oo.addArguments("--disable-blink-features=AutomationControlled");
				System.setProperty("webdriver.opera.driver",
						System.getProperty("user.dir") + "\\src\\test\\resources\\Drivers\\operadriver.exe");
				driver = new OperaDriver();
			} else {
				System.setProperty("webdriver.edge.driver",
						System.getProperty("user.dir") + "\\src\\test\\resources\\Drivers\\msedgedriver.exe");
				driver = new EdgeDriver();
			}
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/********************* Invoke Browser with Selenium Grid *********************/
	@SuppressWarnings("deprecation")
	public void invokeWithGrid(String browserName) {
		String nodeURL = "http://localhost:4444/wd/hub";

		try {
			if (browserName.equalsIgnoreCase("chrome")) {
				capability = DesiredCapabilities.chrome();
			} else if (browserName.equalsIgnoreCase("firefox")) {
				capability = DesiredCapabilities.firefox();
			} else if (browserName.equalsIgnoreCase("opera")) {
				capability = DesiredCapabilities.opera();
			} else {
				capability = DesiredCapabilities.edge();
			}

			driver = new RemoteWebDriver(new URL(nodeURL), capability);

		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/********************* Open URL *********************/
	public void openURL(String websiteURLKey) {
		try {
			driver.get(prop.getProperty(websiteURLKey));
			reportPass(websiteURLKey + " Identified Successfully");
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/********************* Close Browser *********************/
	public void tearDown() {
		driver.close();
	}

	/********************* Quit Browser *********************/
	public void quitBrowser() {
		driver.quit();
	}

	/********************* Enter Text *********************/
	public void enterText(String pathKey, String data) {
		try {
			getElement(pathKey).clear();
			waitLoad(1);
			getElement(pathKey).sendKeys(data);
			reportPass(data + " - Entered successfully in locator Element : " + pathKey);
			waitLoad(2);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/********************* Get Text *********************/
	public String getAvailableText(String pathKey) {
		String text = null;
		try {
			text = getElement(pathKey).getText();
			reportPass("Text available in the locator Element " + pathKey + " : " + text);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
		return text;
	}

	public String getValue(String pathKey) {
		String text = null;
		try {
			text = getElement(pathKey).getAttribute("value");
			reportPass("Text available in the locator Element " + pathKey + " : " + text);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
		return text;
	}

	/********************* Click Element *********************/
	public void elementClick(String pathKey) {
		try {
			getElement(pathKey).click();
			reportPass(pathKey + " : Element Clicked Successfully");
			waitLoad(2);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/****************** Java Script Alert Functions ***********************/
	public void acceptAlert() {
		try {
			Alert alert = driver.switchTo().alert();
			alert.accept();
			reportPass("Page Alert Accepted");
			waitLoad(1);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}

	}

	public void cancelAlert() {
		try {
			Alert alert = driver.switchTo().alert();
			alert.dismiss();
			reportPass("Page Alert Rejected");
			waitLoad(1);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}

	}

	public void getAlertText() {
		String text = null;
		try {
			Alert alert = driver.switchTo().alert();
			text = alert.getText();
			reportPass("Alert Message : " + text);
			waitLoad(1);
			acceptAlert();
		} catch (Exception e) {
			reportFail(e.getMessage());
		}

	}

	/********************* Window Handle Functions *********************/
	public String getHandle() {
		String text = null;
		try {
			text = driver.getWindowHandle();
			reportPass("Current Window Handle : " + text);
			waitLoad(1);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
		return text;
	}

	public Set<String> getHandles() {
		Set<String> handles = null;
		try {
			handles = driver.getWindowHandles();
			reportPass("Received all Window Handles");
			waitLoad(1);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
		return handles;
	}

	public void switchToHandle(String handle) {
		try {
			driver.switchTo().window(handle);
			reportPass("Switched Window to : " + handle);
			waitLoad(2);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/********************* Identify Element *********************/
	public WebElement getElement(String locatorKey) {
		WebElement element = null;

		try {
			if (locatorKey.endsWith("_Id")) {
				element = driver.findElement(By.id(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_Xpath")) {
				element = driver.findElement(By.xpath(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_ClassName")) {
				element = driver.findElement(By.className(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_CSS")) {
				element = driver.findElement(By.cssSelector(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_LinkText")) {
				element = driver.findElement(By.linkText(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_PartialLinkText")) {
				element = driver.findElement(By.partialLinkText(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_Name")) {
				element = driver.findElement(By.name(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else {
				reportFail("Failing the Testcase, Invalid Locator " + locatorKey);
			}
		} catch (Exception e) {
			// Fail the TestCase and Report the error
			reportFail(e.getMessage());
			e.printStackTrace();
		}
		return element;
	}

	public List<WebElement> getElements(String locatorKey) {
		List<WebElement> elements = null;

		try {
			if (locatorKey.endsWith("_Id")) {
				elements = driver.findElements(By.id(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_Xpath")) {
				elements = driver.findElements(By.xpath(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_ClassName")) {
				elements = driver.findElements(By.className(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_CSS")) {
				elements = driver.findElements(By.cssSelector(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_LinkText")) {
				elements = driver.findElements(By.linkText(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_PartialLinkText")) {
				elements = driver.findElements(By.partialLinkText(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else if (locatorKey.endsWith("_Name")) {
				elements = driver.findElements(By.name(prop.getProperty(locatorKey)));
				logger.log(Status.INFO, "Locator Identified : " + locatorKey);
			} else {
				reportFail("Failing the Testcase, Invalid Locator " + locatorKey);
			}
		} catch (Exception e) {
			// Fail the TestCase and Report the error
			reportFail(e.getMessage());
			e.printStackTrace();
		}
		return elements;
	}

	/****************** Verify Element ***********************/
	public boolean isElementPresent(String locatorKey) {
		try {
			if (getElement(locatorKey).isDisplayed()) {
				reportPass(locatorKey + " : Element is Displayed");
				return true;
			}
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
		return false;
	}

	public boolean isElementSelected(String locatorKey) {
		try {
			if (getElement(locatorKey).isSelected()) {
				reportPass(locatorKey + " : Element is Selected");
				return true;
			}
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
		return false;
	}

	public boolean isElementEnabled(String locatorKey) {
		try {
			if (getElement(locatorKey).isEnabled()) {
				reportPass(locatorKey + " : Element is Enabled");
				return true;
			}
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
		return false;
	}

	public void verifyPageTitle(String pageTitle) {
		try {
			String actualTite = driver.getTitle();
			logger.log(Status.INFO, "Actual Title is : " + actualTite);
			logger.log(Status.INFO, "Expected Title is : " + pageTitle);
			Assert.assertEquals(actualTite, pageTitle);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/****************** Assertion Functions ***********************/
	public void assertTrue(boolean flag) {
		softAssert.assertTrue(flag);
	}

	public void assertfalse(boolean flag) {
		softAssert.assertFalse(flag);
	}

	public void assertequals(String actual, String expected) {
		try {
			logger.log(Status.INFO, "Assertion : Actual is -" + actual + " And Expacted is - " + expected);
			softAssert.assertEquals(actual, expected);
		} catch (Exception e) {
			reportFail(e.getMessage());
		}

	}

	/********************* Reporting Functions *********************/
	public void reportFail(String reportString) {
		logger.log(Status.FAIL, reportString);
		takeScreenShotOnFailure();
		Assert.fail(reportString);
	}

	public void reportPass(String reportString) {
		logger.log(Status.PASS, reportString);
		System.out.println(reportString);
	}

	@AfterMethod
	public void afterTest() {
		softAssert.assertAll();
		driver.quit();
	}

	/****************** Capture Screen Shot ***********************/
	public void takeScreenShotOnFailure() {
		TakesScreenshot takeSS = (TakesScreenshot) driver;
		File sourceFile = takeSS.getScreenshotAs(OutputType.FILE);

		File destFile = new File(System.getProperty("user.dir") + "//Test Results//Failure Screenshots//"
				+ DateUtils.getTimeStamp() + ".png");
		try {
			FileUtils.copyFile(sourceFile, destFile);
			logger.addScreenCaptureFromPath(System.getProperty("user.dir") + "//Test Results//Failure Screenshots//"
					+ DateUtils.getTimeStamp() + ".png");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/***************** Wait Functions in Framework *****************/
	public void waitForPageLoad() {
		JavascriptExecutor js = (JavascriptExecutor) driver;

		int i = 0;
		while (i != 180) {
			String pageState = (String) js.executeScript("return document.readyState;");
			if (pageState.equals("complete")) {
				break;
			} else {
				waitLoad(1);
			}
		}

		waitLoad(2);

		i = 0;
		while (i != 180) {
			Boolean jsState = (Boolean) js.executeScript("return window.jQuery != undefined && jQuery.active == 0;");
			if (jsState) {
				break;
			} else {
				waitLoad(1);
			}
		}
	}

	public void waitLoad(int i) {
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void waitUntil(String locatorKey) {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfAllElements(getElement(locatorKey)));
		logger.log(Status.INFO, "Waiting for the All Elements to load in " + locatorKey);
		waitLoad(2);
	}

	/***************** Navigations *****************/
	public void back() {
		try {
			driver.navigate().back();
			logger.log(Status.INFO, "Moved back to previous page");
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	public void forward() {
		try {
			driver.navigate().forward();
			logger.log(Status.INFO, "Moved forward to next page");
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	public void refresh() {
		try {
			driver.navigate().refresh();
			logger.log(Status.INFO, "Refreshed the current page");
		} catch (Exception e) {
			reportFail(e.getMessage());
		}
	}

	/***************** Read Data *****************/
	public String[] getData() {
		String data[] = ReadExcelDataFile.getCellData("Invalid_Details");
		return data;
	}


