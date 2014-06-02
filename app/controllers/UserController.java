package controllers;

import helpers.EmailHelper;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.data.DynamicForm;
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
		//TODO add session connection
		ObjectNode result = Json.newObject();
		User guest = User.createGuest();
		if (null != guest) {
			result.put("type", "guest login");
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
			User user = filledForm.get().create();
			String message ="Bienvenue sur MyMovieQuiz,\r\n \r\nTu peux désormais te connecter";
			message+=" avec le nom d'utilisateur " + user.username + ".\r\n \r\n";
			message+="A bientôt dans les salons Mymoz !";
			EmailHelper.sendSimpleEmail("Inscription Mymoviequiz", user.email, message);
			flash("success", "Votre compte a &eacute;t&eacute; cr&eacute;&eacute; ! <a href='"+routes.Application.game()+"' class='alert-link'>Jouez en cliquant ici !</a>");
			return redirect(routes.Application.index());
		}
	}
	

	public static Result signup() {
		return ok(signup.render(userForm));
	}
	
	@Transactional
	public static Result signin(){
		DynamicForm requestData = Form.form().bindFromRequest();
        String username = requestData.get("username");
        String password = requestData.get("password");
		User user = User.findByUsername(username);
		boolean authenticated = false;
		try {
			if(null != user){
				authenticated = user.authenticate(password);
			}
		} catch (NoSuchAlgorithmException e) {
			Logger.error("NoSuchAlgorithmException");
		} catch (InvalidKeySpecException e) {
			Logger.error("InvalidKeySpecException");
		}
		
		if(authenticated == true){
			session().clear();
			session("username", user.username);
			session("status", user.statut);
		}
		
		ObjectNode result = Json.newObject();
		result.put("type", "authentication");
		result.put("authenticated", authenticated);
	
		return ok(result);
	}
}
