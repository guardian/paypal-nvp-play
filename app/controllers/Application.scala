package controllers

import play.api.mvc.{Controller, Action}

class Application extends Controller {

	def checkout = Action {
		Ok("Hello world")
	}

}
