package models;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Function0;
import base.AbstractDatabaseTest;

public class UserTest extends AbstractDatabaseTest {

	@Test
	public void testGuest() throws Throwable {
		JPA.withTransaction(new Function0<Void>() {
			public Void apply() {
				User guest = User.createGuest();
				assertThat(guest).describedAs("The guest should not be null")
						.isNotNull();
				assertThat(guest.id).describedAs(
						"The guest id should be greater than 0").isGreaterThan(
						0);

				boolean result = User.deleteGuest(guest.id);
				assertThat(result).describedAs(
						"The result should be true, guest not deleted")
						.isTrue();

				return null;
			}
		});
	}

	@Test
	public void testSequence() throws Throwable {
		JPA.withTransaction(new Function0<Void>() {
			public Void apply() {
				int userId = User.getNextval();
				assertThat(userId).describedAs("sequence result error, null")
						.isNotNull();
				assertThat(userId).describedAs("sequence result error, <0")
						.isGreaterThan(0);
				return null;
			}
		});
	}

	@Test
	public void testFindByUsername() throws Throwable {
		JPA.withTransaction(new Function0<Void>() {
			public Void apply() {
				User user = new User("toto", "toto", "toto@test.com", "u");
				user.save();
				User user2 = User.findByUsername(user.username);
				assertThat(user2).describedAs("user is null").isNotNull();
				assertThat(user2.username).describedAs("username is wrong")
						.isEqualTo(user.username);
				assertThat(user2.id).describedAs("user differs from base user")
						.isEqualTo(user.id);
				user.delete();
				return null;
			}
		});
	}
}
