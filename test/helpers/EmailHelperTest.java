package helpers;

import org.junit.Test;

public class EmailHelperTest {

	@Test
	public void testSendSimpleEmail() {
		EmailHelper.sendSimpleEmail("Test", "test@test.com",
				"This is a test mail");
	}
}
