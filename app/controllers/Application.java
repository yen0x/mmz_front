package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.client;
import views.html.about;
import views.html.index;

public class Application extends Controller {
	
	public static Result index(String alert) {
		return ok(index.render(alert));
	}

	public static Result game() {
		return ok(client.render());
	}
	
	public static Result about() {
		return ok(about.render());
	}
	
}
