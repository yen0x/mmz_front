package controllers;

import helpers.EmailHelper;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.signup;
import models.User;

public class UserController extends Controller {
	

	static Form<User> userForm = Form.form(User.class);

	@Transactional
	public static Result joinAsGuest() {
		ObjectNode result = Json.newObject();
		User guest = User.createGuest();
		if (null != guest) {
			result.put("user", guest.username);
			result.put("password", guest.password);
		}
		return ok(result);

	}

	@Transactional
	public static Result newUser() throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {

		Form<User> filledForm = userForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(signup.render(filledForm));
		} else {
			User user = User.create(filledForm.get());
			String message ="Bienvenue sur MyMovieQuiz,\r\n \r\nVous pouvez désormais vous connecter";
			message+=" avec le nom d'utilisateur " + user.username + ".\r\n \r\n";
			message+="A bientôt dans les salons Mymoz !";
			EmailHelper.sendSimpleEmail("Inscription Mymoviequiz", user.email, message);
			return redirect(routes.Application.index("signupOK"));
		}
	}
	

	public static Result signup() {
		return ok(signup.render(userForm));
	}
}
