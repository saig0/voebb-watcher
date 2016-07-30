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

public class FirstTest {

	public static void main(String[] args) {

		BorrorStatusChecker checker = new BorrorStatusChecker();

		BorrorStatus status = checker.check("978-3-95590-020-5", "Spandau: Hauptbibliothek Spandau");
		System.out.println(status);

		try {
			// not found
			checker.check("978-3-95590-020-6", "Spandau: Hauptbibliothek Spandau");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// more than one found
			checker.check("Essen für Sieger", "Spandau: Hauptbibliothek Spandau");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
