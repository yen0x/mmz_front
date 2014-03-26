package helpers;

import org.junit.Test;
import base.AbstractDatabaseTest;

public class EmailHelperTest extends AbstractDatabaseTest{

	@Test
	public void testSendSimpleEmail() {
		EmailHelper.sendSimpleEmail("Test", "test@test.com",
				"This is a test mail");
	}
}
