package de.voebb;

import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;

public class BorrorStatusChecker {

	private final WebDriver driver = new HtmlUnitDriver(true);

	public BorrorStatus check(String text, String library) throws NoSuchElementException, IllegalArgumentException {
		
		driver.get("https://www.voebb.de/");

		WebElement element = driver.findElement(By.xpath("//span[text() = 'Suche']/parent::a"));
		element.click();

		element = driver
				.findElement(By.xpath("//span[text() = 'oder Bibliothek']/parent::label/following-sibling::select"));
		new Select(element).selectByVisibleText(library);

		element = driver.findElement(By.xpath("//span[text() = 'Freie Suche']/parent::label/following-sibling::input"));
		element.sendKeys(text);

		element = driver.findElement(By.xpath("//input[@value = 'Suche starten']"));
		element.click();

		if (foundNoResults()) {
			
			throw new NoSuchElementException("found no result for '" + text + "' at '" + library + "'");

		} else if (foundMoreThanOneResult()) {
			
			throw new IllegalArgumentException("found multiple results for '" + text + "' at '" + library + "'");

		} else {
			element = driver.findElement(By.xpath("(//input[@value = 'Verfügbarkeit / Bestellen'])[1]"));
			element.click();

			String row = "//td[contains(text(),'" + library + "')]/following-sibling::td";

			String status = driver.findElement(By.xpath(row + "[2]")).getText();
			String location = driver.findElement(By.xpath(row + "[3]")).getText();
			String note = driver.findElement(By.xpath(row + "[4]")).getText();

			return new BorrorStatus(status, location, note);
		}
	}

	private boolean foundNoResults() {
		List<WebElement> elements = driver
				.findElements(By.xpath("//h1[contains(text(), 'Ihre Suche erzielte keinen Treffer.')]"));
		return !elements.isEmpty();
	}

	private boolean foundMoreThanOneResult() {
		List<WebElement> elements = driver
				.findElements(By.xpath("//p[@id='breadcrumb']//span[contains(text(),'Trefferliste')]"));
		return !elements.isEmpty();
	}

}
