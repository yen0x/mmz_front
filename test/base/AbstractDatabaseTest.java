package base;


import org.junit.After;
import org.junit.Before;

import play.test.FakeApplication;
import play.test.Helpers;

public abstract class AbstractDatabaseTest {

	protected FakeApplication app;
		
		@Before
		public void setUp() {
			app = Helpers.fakeApplication();
			Helpers.start(app);
		}
		
		@After
		public void tearDown(){
			Helpers.stop(app);
		}

}
