package models;

import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.w3c.dom.Document;

import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.WS;
import play.libs.WS.WSRequestHolder;
import play.libs.XPath;

import com.play4jpa.jpa.models.Finder;
import com.play4jpa.jpa.models.Model;
import com.play4jpa.jpa.query.Query;

@Entity
@Table(name = "users")
@SequenceGenerator(name = "user_gen", sequenceName = "users_id_seq", allocationSize = 1)
public class User extends Model<User> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
	public int id;
	public String username;
	public String password;
	public String email;
	public String statut = "u";

	public static Finder<String, User> find = new Finder<>(String.class,
			User.class);

	public static Query<User> query() {
		return find.query();
	}

	public User() {
		this.username = "guest";
		this.password = "guest";
		this.statut = "g";
		this.email = "guest@test.com";
	}

	public User(String username, String password, String email, String statut) {
		this.username = username;
		this.password = password;
		this.email = email;
		if (null != statut)
			this.statut = statut;
	}

	public static User createGuest() {
		// TODO securize password generation
		User guest = new User("guest", "guest",
				"", "g");
		guest.save();

		guest.username = guest.username+guest.id;
		guest.password = guest.password+guest.id;

		WSRequestHolder wsreqHolder = WS.url(Play.application().configuration()
				.getString("openfire.url"));
		wsreqHolder.setQueryParameter("type", "add");
		wsreqHolder.setQueryParameter("secret", Play.application()
				.configuration().getString("openfire.key"));
		wsreqHolder.setQueryParameter("username", guest.username);
		wsreqHolder.setQueryParameter("password", guest.password);
		wsreqHolder.setQueryParameter("email", guest.email);
		F.Promise<WS.Response> openfireResult = wsreqHolder.get();
		WS.Response response = openfireResult.get(3000);
		Document xmlResponse = response.asXml();
		if (XPath.selectText("result", xmlResponse).equals("ok")) {
			guest.update();
			return guest;
		} else {
			guest.delete();
			Logger.error(XPath.selectText("error", xmlResponse));
			return null;
		}
	}

	public static boolean deleteGuest(int userId) {

		User guest = query().eq("id", userId).eq("statut", "g").findUnique();
		if (null == guest) {
			Logger.error("guest not found for deletion");
			return false;
		}

		WSRequestHolder wsreqHolder = WS.url(Play.application().configuration()
				.getString("openfire.url"));
		wsreqHolder.setQueryParameter("type", "delete");
		wsreqHolder.setQueryParameter("secret", Play.application()
				.configuration().getString("openfire.key"));
		wsreqHolder.setQueryParameter("username", guest.username);
		F.Promise<WS.Response> openfireResult = wsreqHolder.get();
		WS.Response response = openfireResult.get(3000);

		Document xmlResponse = response.asXml();
		if (XPath.selectText("result", xmlResponse).equals("ok")) {
			guest.delete();
			return true;
		} else {
			Logger.error(XPath.selectText("error", xmlResponse));
			return false;
		}
	}

	public static int getNextval() {
		javax.persistence.Query q = JPA.em().createNativeQuery(
				"select nextval('users_id_seq')");
		return ((BigInteger) q.getSingleResult()).intValue();
	}
}
