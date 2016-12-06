package controllers

import play.api.mvc.{Controller, Action}
import play.api.mvc.Results.BadRequest
import play.api.libs.json.{Json, JsSuccess, JsError}
import okhttp3.{OkHttpClient, FormBody, Request, Response}
import com.netaporter.uri.Uri.parseQuery
import configuration.Config

class Paypal extends Controller {

	case class Token (token: String)

	implicit val tokenWrites = Json.writes[Token]
	implicit val tokenReads = Json.reads[Token]

	def NVPRequest (params: Map[String, String]) = {

		val client = new OkHttpClient()
		val requestBody = new FormBody.Builder()
			.add("USER", Config.paypalUser)
			.add("PWD", Config.paypalPassword)
			.add("SIGNATURE", Config.paypalSignature)
			.add("VERSION", Config.paypalNVPVersion)

		for ((param, value) <- params) requestBody.add(param, value)

		val request = new Request.Builder()
			.url(Config.paypalSandboxUrl)
			.post(requestBody.build())
			.build()

		client.newCall(request).execute()

	}

	def retrieveNVPParam (response: Response, paramName: String) = {

		val responseBody = response.body().string()
		val queryParams = parseQuery(responseBody)
		queryParams.paramMap.get(paramName).get(0)

	}

	def tokenJsonResponse (response: Response) = {

		val token = Token(retrieveNVPParam(response, "TOKEN"))
		Json.toJson(token)

	}

	def setupPayment = Action {

		val paymentParams = Map(
			"METHOD" -> "SetExpressCheckout",
			"PAYMENTREQUEST_0_PAYMENTACTION" -> "SALE",
			"PAYMENTREQUEST_0_AMT" -> "4.50",
			"PAYMENTREQUEST_0_CURRENCYCODE" -> "GBP",
			"RETURNURL" -> "http://localhost:9000/create-agreement",
			"CANCELURL" -> "http://localhost:9000/cancel",
			"BILLINGTYPE" -> "MerchantInitiatedBilling")

		val response = NVPRequest(paymentParams)
		Ok(tokenJsonResponse(response))

	}

	def createAgreement = Action { request =>

		val tokenFromJson = request.body.asJson match {
			case Some(json) => Json.fromJson[Token](json)
			case _ => None
		}

		tokenFromJson match {

			case JsSuccess(token: Token, _) => {

				val client = new OkHttpClient()
				val requestBody = new FormBody.Builder()
					.add("USER", Config.paypalUser)
					.add("PWD", Config.paypalPassword)
					.add("SIGNATURE", Config.paypalSignature)
					.add("VERSION", Config.paypalNVPVersion)
					.add("METHOD", "CreateBillingAgreement")
					.add("TOKEN", token.token)
					.build()

				val request = new Request.Builder()
					.url(Config.paypalSandboxUrl)
					.post(requestBody)
					.build()

				val response = client.newCall(request).execute()
				val baid = retrieveNVPParam(response, "BILLINGAGREEMENTID")
				println(baid)
				Ok
			}

			case e: JsError => {
				println("Errors: " + JsError.toJson(e).toString())
				BadRequest
			}

			case _ => BadRequest

		}

	}

}
