package org.camunda.bpm.watch.voebb;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.Select;

public class SeleniumBorrorStateChecker {

	private final WebDriver driver = new HtmlUnitDriver(true);

	public BorrorState check(String text, String library) throws NoResultFoundException, MultipleResultsFoundException {
		
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

		if(hasNoResults()) {
			return BorrorState.notItemFound(library);		
		} else if (hasMultipleResults()) {
			return BorrorState.multipleItemsFound(library);
		}
		
		element = driver.findElement(By.xpath("(//input[@value = 'Verf�gbarkeit / Bestellen'])[1]"));
		element.click();

		String title = driver.findElement(By.xpath("//p//*[contains(text(), 'Titel:')]")).getText();
		title = title.substring("Titel:".length()).trim();
		
		List<WebElement> elements = driver.findElements(By.xpath("//table[thead/tr/th/text()='Bibliothek']/tbody/tr"));
		if (elements.isEmpty()) {
			throw new RuntimeException("found no borrow state for library '" + library + "'");
		}
	
		BorrorState state = null;
		
		for (int i = 1; i <= elements.size(); i++) {
			state = getBorrorState(i, title, library);
			
			if (state.isAvailableForBorrow()) {
				return state;
			}
		}
		
		return state;
	}
	
	private BorrorState getBorrorState(int row, String title, String library) {
		
		String path = "//table[thead/tr/th/text()='Bibliothek']/tbody/tr[" + row + "]/td";

		String signature = driver.findElement(By.xpath(path + "[2]")).getText();
		String status = driver.findElement(By.xpath(path + "[3]")).getText();
		String location = driver.findElement(By.xpath(path + "[4]")).getText();
		String note = driver.findElement(By.xpath(path + "[5]")).getText();

		return new BorrorState(title, library, signature, status, location, note);		
	}

	private boolean hasNoResults() {
		List<WebElement> elements = driver
				.findElements(By.xpath("//h1[contains(text(), 'Ihre Suche erzielte keinen Treffer.')]"));
		return !elements.isEmpty();
	}

	private boolean hasMultipleResults() {
		List<WebElement> elements = driver
				.findElements(By.xpath("//p[@id='breadcrumb']//span[contains(text(),'Trefferliste')]"));
		return !elements.isEmpty();
	}

}
