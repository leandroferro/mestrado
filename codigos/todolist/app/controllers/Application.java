package controllers;

import models.Task;
import play.data.Form;
import play.libs.Comet;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	static Form<Task> taskForm = form(Task.class);

	public static Result index() {
		Comet comet = new Comet("console.log") {
		    public void onConnected() {
		      sendMessage("kiki");
		      sendMessage("foo");
		      sendMessage("bar");
		      close();
		    }
		  };
		  
		  return ok(comet);
	}

	public static Result tasks() {
		return ok(views.html.index.render(Task.all(), taskForm));
	}

	public static Result newTask() {
		Form<Task> filledForm = taskForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.index.render(Task.all(), filledForm));
		} else {
			Task.create(filledForm.get());
			return redirect(routes.Application.tasks());
		}
	}

	public static Result deleteTask(Long id) {
		Task.delete(id);
		return redirect(routes.Application.tasks());
	}

}