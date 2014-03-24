package models;

import helpers.PasswordEncryptionHelper;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.w3c.dom.Document;

import play.Logger;
import play.Play;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
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
	@Required(message="Pseudo requis")
	public String username;
	@Required(message="Mot de  passe requis")
	public String password;
	@Required(message="E-mail requis")
	@Email(message="Format incorrect")
	public String email;
	public String statut = "u";
	public byte[] salt;

	public static Finder<String, User> find = new Finder<>(String.class,
			User.class);

	public static Query<User> query() {
		return find.query();
	}

	public User() {
	}

	public User(String username, String password, String email, String statut) {
		this.username = username;
		this.password = password;
		this.email = email;
		if (null != statut)
			this.statut = statut;
	}
	
	public static User create(User user) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		byte [] salt = PasswordEncryptionHelper.generateSalt();
		user.salt = salt;
		
		byte[] password = PasswordEncryptionHelper.getEncryptedPassword(user.password, user.salt);
		user.password = PasswordEncryptionHelper.byteToBase64(password);
		user.save();
		User.createOnXMPPServer(user);
		return user;
	}

	public static User createGuest() {
		// TODO securize password generation
		User guest = new User("guest", "guest",
				"guest@guest.com", "g");
		guest.save();

		guest.username = guest.username+guest.id;
		guest.password = guest.password+guest.id;

		guest.update();

		return User.createOnXMPPServer(guest);
		
	}
	
	/**
	 * sends a GET request to the xmpp server for a user creation
	 * @param user
	 * @return
	 */
	public static User createOnXMPPServer(User user){
		WSRequestHolder wsreqHolder = WS.url(Play.application().configuration()
				.getString("openfire.url"));
		wsreqHolder.setQueryParameter("type", "add");
		wsreqHolder.setQueryParameter("secret", Play.application()
				.configuration().getString("openfire.key"));
		wsreqHolder.setQueryParameter("username", user.username);
		wsreqHolder.setQueryParameter("password", user.password);
		wsreqHolder.setQueryParameter("email", user.email);
		F.Promise<WS.Response> openfireResult = wsreqHolder.get();
		WS.Response response = openfireResult.get(3000);
		Document xmlResponse = response.asXml();
		if (XPath.selectText("result", xmlResponse).equals("ok")) {
			return user;
		} else {
			user.delete();
			Logger.error(XPath.selectText("error", xmlResponse));
			return null;
		}
	}
	//TODO separate into deleteUser and deleteFrom XMPP
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
	
	public String validate(){
		if(findByUsername(username) != null){
			return "Ce pseudonyme est déjà pris par un autre joueur";
		}else if(findByEmail(email) != null){
			return "Un compte est déjà créé avec cette adresse e-mail";
		}
		return null;
	}
	
	public static User findByUsername(String username){
		return query().eq("username", username).findUnique();
	}
	
	public static User findByEmail(String email){
		return query().eq("email", email).findUnique();
	}
	
}
