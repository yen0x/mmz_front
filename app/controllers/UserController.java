package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import models.User;

public class UserController extends Controller {
	
	@Transactional
	public static Result joinAsGuest(){
		ObjectNode result = Json.newObject();
		User guest = User.createGuest();
		if(null != guest){
			result.put("user", guest.username);
			result.put("password", guest.password);
		}
		return ok(result);
		
	}

}
