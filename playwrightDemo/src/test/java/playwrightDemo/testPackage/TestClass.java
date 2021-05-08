package playwrightDemo.testPackage;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import org.testng.annotations.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.IsVisibleOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import com.shaft.tools.io.ReportManager;
import com.shaft.tools.io.ReportManagerHelper;
import com.shaft.validation.Assertions;
import com.shaft.validation.Assertions.AssertionComparisonType;
import com.shaft.validation.Assertions.AssertionType;

public class TestClass {
	@Test
	public void testMethod() {
		////Creating the basic drivers
		
		//playwright handles the initial setup
		var playwright = Playwright.create();
		//browser opens the browser session
		var browser = playwright.chromium().launch(new LaunchOptions().setHeadless(false));
		//sets the context, a sub-browser like an in-private browsing session
		var context = browser.newContext(new Browser.NewContextOptions()
				  .setRecordVideoDir(Paths.get("videos/")));
		//the actual tab/page that will be used for browser actions
		var page = context.newPage();

		////Browser Actions
		page.navigate("https://www.google.com/ncr", new Page.NavigateOptions()
				  .setWaitUntil(WaitUntilState.NETWORKIDLE).setTimeout(20000));
		ReportManager.logDiscrete(page.title());
		
		///Screenshots
		var screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
		ReportManagerHelper.attach("Screenshot", "playwright.png", new ByteArrayInputStream(screenshot));

		//Sample using configurable timeouts and basic element identification
		boolean isGoogleLogoDisplayed = page.isVisible("xpath=//img[@alt='Google']",
				new IsVisibleOptions().setTimeout(20000));
		Assertions.assertTrue(isGoogleLogoDisplayed,
				"Checking to see if the google logo is displayed using playwright");

		//Sample using findElements equivalent to verify element identifiers
		page.waitForSelector("xpath=//input[@name='q']", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setState(WaitForSelectorState.VISIBLE).setTimeout(20000));
		var elementsList = page.querySelectorAll("xpath=//input[@name='q']");
		if (elementsList.size()==1) {
			elementsList.get(0).fill("SHAFT_Engine");
			elementsList.get(0).press("Enter");
		}

		var searchResultText = page.textContent("xpath=(//h3[@class='LC20lb DKV0Md'])[1]");
		Assertions.assertEquals("MohabMohie", searchResultText, AssertionComparisonType.CONTAINS,
				AssertionType.POSITIVE, "Checking to see if the first search result contains 'MohabMohie'");
		screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
		ReportManagerHelper.attach("Screenshot", "playwright.png", new ByteArrayInputStream(screenshot));

		var videoPath = page.video().path().toString();
		context.close();
		playwright.close();
		try {
			ReportManagerHelper.attach("Video Recording", "playwright.mp4", new FileInputStream(videoPath));
		} catch (FileNotFoundException e) {
			ReportManagerHelper.log(e);
		}
	}
}
