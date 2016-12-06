package controllers

import play.api.mvc.{Controller, Action}
import play.api.mvc.Results.BadRequest
import play.api.libs.json.{Json, JsSuccess, JsError}
import okhttp3.{OkHttpClient, FormBody, Request}
import com.netaporter.uri.Uri.parseQuery
import configuration.Config

class Paypal extends Controller {

	case class Token (token: String)

	implicit val tokenWrites = Json.writes[Token]
	implicit val tokenReads = Json.reads[Token]

	def retrieveToken (queryString: String) = {

		val queryParams = parseQuery(queryString)
		val token = Token(queryParams.paramMap.get("TOKEN").get(0))
		Json.toJson(token)

	}

	def retrieveBaid (queryString: String) = {

		val queryParams = parseQuery(queryString)
		queryParams.paramMap.get("BILLINGAGREEMENTID").get(0)

	}

	def setupPayment = Action {

		val client = new OkHttpClient()
		val requestBody = new FormBody.Builder()
			.add("USER", Config.paypalUser)
			.add("PWD", Config.paypalPassword)
			.add("SIGNATURE", Config.paypalSignature)
			.add("VERSION", Config.paypalNVPVersion)
			.add("METHOD", "SetExpressCheckout")
			.add("PAYMENTREQUEST_0_PAYMENTACTION", "SALE")
			.add("PAYMENTREQUEST_0_AMT", "4.50")
			.add("PAYMENTREQUEST_0_CURRENCYCODE", "GBP")
			.add("RETURNURL", "http://localhost:9000/create-agreement")
			.add("CANCELURL", "http://localhost:9000/cancel")
			.add("BILLINGTYPE", "MerchantInitiatedBilling")
			.build()

		val request = new Request.Builder()
			.url(Config.paypalSandboxUrl)
			.post(requestBody)
			.build()

		val response = client.newCall(request).execute()
		Ok(retrieveToken(response.body().string()))

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
				val baid = retrieveBaid(response.body().string())
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
