package jp.yorozuya.hiroumi.seino;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Wait;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) {
		System.setProperty("webdriver.chrome.driver", "./driver/chromedriver");
		WebDriver driver = new ChromeDriver();

//		driver.get("https://www.amazon.com/Amazon-Alexa-Things-to-Try/dp/B01JH8EV8W/ref=sr_1_1?s=digital-skills&ie=UTF8&qid=1512671002&sr=1-1&refinements=p_72%3A2661618011");
		String listPageUrl = "https://www.amazon.com/s/ref=lp_13727921011_nr_p_72_0?fst=as%3Aoff&rh=n%3A13727921011%2Cp_72%3A2661618011&bbn=13727921011&ie=UTF8&qid=1512633306&rnid=2661617011";

		do {
			HashMap<String, Object> listResult = getListPage(driver, listPageUrl);
			listPageUrl = (String) listResult.get("next");
			ArrayList<String> detailPageUrls = (ArrayList<String>) listResult.get("details");

			for(int i = 0; i < detailPageUrls.size(); i++) {
				String row = getDetailPage(driver, detailPageUrls.get(i));

				writeToFile(row);
			}
		} while(!listPageUrl.equals(""));

        driver.close();
	}

	public static void writeToFile(String row) {
		File file = new File("result.csv");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(pw != null) {
			pw.println(row);
		}

		pw.close();
	}

	public static HashMap<String, Object> getListPage(WebDriver driver, String url) {
		try {
			Thread.sleep((4 - (new Random()).nextInt(3))*1000);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		driver.get(url);
		ArrayList<String> urls = new ArrayList<String>();

		Wait<WebDriver> wait = new WebDriverWait(driver, 90);
		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>(){
			public Boolean apply(WebDriver driver){
				return driver.findElements(By.className("celwidget")).size() == 16;
			}
		};
		try {
			wait.until(condition);
		} catch (TimeoutException e) {}

		ArrayList<WebElement> listItems = (ArrayList<WebElement>) driver.findElements(By.className("celwidget"));
		for (int i = 0; i < listItems.size(); i++) {
			final WebElement item = listItems.get(i);
			WebElement titleItem = item.findElement(By.tagName("h2"));
			String detailURL = titleItem.findElement(By.xpath("./..")).getAttribute("href");
			urls.add(detailURL);
		}

		String nextPageHREF = "";

		ExpectedCondition<WebElement> nextPageLinkCondition = new ExpectedCondition<WebElement>() {
			public WebElement apply(WebDriver driver) {
				return driver.findElement(By.id("pagnNextLink"));
			}
		};

		WebElement nextPage = null;
		try {
			nextPage = wait.until(nextPageLinkCondition);
		} catch (TimeoutException e) {
			nextPageHREF = "";
		}

		HashMap<String, Object> result = new HashMap<String, Object>();

		if(nextPage != null) {
			nextPageHREF = nextPage.getAttribute("href");
		}

		result.put("next", nextPageHREF);
		result.put("details", urls);

		return result;
	}

	public static String getDetailPage(WebDriver driver, String url) {
		try {
			Thread.sleep((4 - (new Random()).nextInt(3))*1000);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		driver.get(url);
		WebElement title = driver.findElement(By.tagName("h1"));
		String titleText = title.getText();
		WebElement review = driver.findElement(By.cssSelector("span.a-size-small.a2s-reviews>a.a-link-normal.a2s-link>span.a-size-small.a-color-link.a2s-review-star-count"));
		String reviewText = review.getText();
		WebElement star = driver.findElement(By.cssSelector("span.arp-rating-out-of-text"));
		String starText = star.getText().split(" out")[0];
		String resultString = "\"" + titleText + "\",\"" + starText + "\",\"" + reviewText + "\"";
		return resultString;
	}

}
