package controllers

import play.api.mvc.{Controller, Action}
import okhttp3.{OkHttpClient, FormBody, Request}
import configuration.Config

class Paypal extends Controller {

	def setupPayment (amount: String,
		currency: String) = Action {

		val client = new OkHttpClient()
		val requestBody = new FormBody.Builder()
			.add("USER", Config.paypalUser)
			.add("PWD", Config.paypalPassword)
			.add("SIGNATURE", Config.paypalSignature)
			.add("VERSION", Config.paypalNVPVersion)
			.add("METHOD", "SetExpressCheckout")
			.add("PAYMENTREQUEST_0_PAYMENTACTION", "SALE")
			.add("PAYMENTREQUEST_0_AMT", amount)
			.add("PAYMENTREQUEST_0_CURRENCYCODE", currency)
			.add("RETURNURL", "http://localhost:5000/create-agreement")
			.add("CANCELURL", "http://localhost:5000/cancel")
			.add("BILLINGTYPE", "MerchantInitiatedBilling")
			.build()

		val request = new Request.Builder()
			.url(Config.paypalSandboxUrl)
			.post(requestBody)
			.build()

		val response = client.newCall(request).execute()
		Ok(response.body())

	}

	def createAgreement (token: String) = TODO

}
