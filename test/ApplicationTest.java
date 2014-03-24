import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.GET;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;

import javax.persistence.EntityManager;

import models.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Function0;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

/**
 * 
 * Simple (JUnit) tests that can call all parts of a play app. If you are
 * interested in mocking a whole application, see the wiki for more details.
 * 
 */
public class ApplicationTest {

	protected EntityManager em;
	protected FakeApplication app;
	
	@Before
	public void setUp() {
		app = fakeApplication();
		Helpers.start(app);
	}
	
	@After
	public void tearDown(){
		Helpers.stop(app);
	}

	/**
	 * Begin route tests
	 */

	//@Test
	public void testRootRoute() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Result result = route(fakeRequest(GET, "/site/"));
				assertThat(result).isNotNull();
			}
		});

	}

	/**
	 * End route tests
	 */

	//@Test
	public void simpleCheck() {
		int a = 1 + 1;
		assertThat(a).isEqualTo(2);
	}

	@Test
	public void createAndRetrieveUser() throws Throwable {
		JPA.withTransaction(new Function0<Void>() {
			public Void apply() {
				User newUser = new User("mymoz", "mymoz", "mymoz@mymoviequiz.com", null);
				newUser.save();
				User user = User.query().eq("username", "mymoz").findUnique();
				assertThat(user).isNotNull();
				assertThat(user.email).isEqualTo("mymoz@mymoviequiz.com");
				newUser.delete();
				user = User.query().eq("username", "mymoz").findUnique();
				assertThat(user).isNull();
				return null;
			}
		});
	}

}
