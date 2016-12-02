package controllers

import play.api.mvc.{Controller, Action}

class Paypal extends Controller {

	def setupPayment (amount: String,
		currency: String,
		returnUrl: String,
		cancelUrl: String) = Action {

		Ok("Setup Payment")

	}

	def createAgreement (token: String) = Action {
		Ok("Create Agreement")
	}

}
