import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

import org.junit.Test;

import play.mvc.Content;
import play.mvc.Result;

/**
 * 
 * Simple (JUnit) tests that can call all parts of a play app. If you are
 * interested in mocking a whole application, see the wiki for more details.
 * 
 */
public class ApplicationTest {

	/**
	 * Begin route tests
	 */

	@Test
	public void testRootRoute() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Result result = route(fakeRequest(GET, "/"));
				assertThat(result).isNotNull();
			}
		});

	}

	/**
	 * End route tests
	 */

	@Test
	public void simpleCheck() {
		int a = 1 + 1;
		assertThat(a).isEqualTo(2);
	}

	@Test
	public void renderTemplate() {
		Content html = views.html.index
				.render("Your new application is ready.");
		assertThat(contentType(html)).isEqualTo("text/html");
		assertThat(contentAsString(html)).contains(
				"Your new application is ready.");
	}

}
