/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.voebb;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class FirstTest {

  public static void main(String[] args) {
    // Create a new instance of the html unit driver
    // Notice that the remainder of the code relies on the interface,
    // not the implementation.
    WebDriver driver = new HtmlUnitDriver(); // new FirefoxDriver();

    // And now use this to visit Google
    driver.get("https://www.voebb.de/");

    // Find the text input element by its name
    WebElement element = driver.findElement(By.xpath("//span[text() = 'Suche']/parent::a"));
    element.click();

    element = driver.findElement(By.xpath("//span[text() = 'Freie Suche']/parent::label/following-sibling::input"));

    String title = "Die Stadt der träumenden Bücher : ein Roman aus Zamonien";

    // Enter something to search for
    element.sendKeys(title);

    // Now submit the form. WebDriver will find the form for us from the element
    // element.submit();
    element = driver.findElement(By.xpath("//input[@value = 'Suche starten']"));
    element.click();

    List<WebElement> elements = driver.findElements(By.xpath("//a[text()='" + title + "']"));
    if (elements.size() == 1) {
      // click
      System.out.println("found");

    } else if (elements.size() > 1) {
      // more elements
      System.out.println("found too much");

    } else {
      // no element
      System.out.println("not found");
    }

    //Close the browser
    driver.quit();
  }

}
