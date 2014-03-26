package controllers;

import static org.fest.assertions.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import models.User;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Function0;
import base.AbstractDatabaseTest;

public class UserControllerTest extends AbstractDatabaseTest {

	@Test
	public void authenticate() throws Throwable {

		JPA.withTransaction(new Function0<Void>() {
			public Void apply() throws NoSuchAlgorithmException,
					InvalidKeySpecException, UnsupportedEncodingException {
				User user = new User("username", "password", "test@test.com",
						"u").create();

				assertThat(user.authenticate("password")).isTrue();
				user.erase();

				return null;
			}
		});
	}
}
