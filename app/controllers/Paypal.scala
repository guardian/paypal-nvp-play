package controllers

import play.api.mvc.{Controller}

class Paypal extends Controller {

	def setupPayment (amount: String,
		currency: String,
		returnUrl: String,
		cancelUrl: String) = TODO

	def createAgreement (token: String) = TODO

}
