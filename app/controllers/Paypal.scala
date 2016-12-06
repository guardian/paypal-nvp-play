package controllers

import play.api.mvc.{Controller, Action}
import play.api.libs.json.Json
import okhttp3.{OkHttpClient, FormBody, Request}
import com.netaporter.uri.Uri.parseQuery
import configuration.Config

class Paypal extends Controller {

	case class Token (token: String)

	implicit val tokenWrites = Json.writes[Token]

	def retrieveToken (queryString: String) = {

		val queryParams = parseQuery(queryString)
		val token = Token(queryParams.paramMap.get("TOKEN").get(0))
		Json.toJson(token)

	}

	def setupPayment (amount: String, currency: String) = Action {

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
		Ok(retrieveToken(response.body().string()))

	}

	def createAgreement (token: String) = TODO

}
