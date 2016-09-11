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
package org.camunda.bpm.watch;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.watch.voebb.BorrorState;
import org.camunda.bpm.watch.voebb.BorrorStateChecker;
import org.camunda.bpm.watch.voebb.MultipleResultsFoundException;
import org.camunda.bpm.watch.voebb.NoResultFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BorrorStateCheckerTest {

	private BorrorStateChecker checker;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void init() {
		checker = new BorrorStateChecker();
	}
	
	@Test
	public void getState() {
		BorrorState state = checker.check("978-3-95590-020-5", "Spandau: Hauptbibliothek Spandau");
		
		assertThat(state).isNotNull();
		assertThat(state.getTitle()).contains("Essen für Sieger");
		assertThat(state.getLibrary()).isEqualTo("Spandau: Hauptbibliothek Spandau");
	}
	
	@Test
	public void noResult() {
		thrown.expect(NoResultFoundException.class);
		
		checker.check("978-3-95590-020-6", "Spandau: Hauptbibliothek Spandau");
	}
	
	@Test
	public void multipleResults() {
		thrown.expect(MultipleResultsFoundException.class);
		
		checker.check("Essen für Sieger", "Spandau: Hauptbibliothek Spandau");
	}

}
