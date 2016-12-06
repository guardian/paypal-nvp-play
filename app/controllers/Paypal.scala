package controllers

import play.api.mvc.{Controller, Action}
import play.api.mvc.Results.BadRequest
import play.api.libs.json.{Json, JsSuccess, JsError}
import okhttp3.{OkHttpClient, FormBody, Request, Response}
import com.netaporter.uri.Uri.parseQuery
import configuration.Config

class Paypal extends Controller {

	// Payment token used to tie Paypal requests together.
	case class Token (token: String)
	// A parameter in a Paypal NVP request.
	case class NVPParam (name: String, value: String)

	// Json writers.
	implicit val tokenWrites = Json.writes[Token]
	implicit val tokenReads = Json.reads[Token]

	// The parameters sent with every NVP request.
	private val defaultNVPParams = List(
		NVPParam("USER", Config.paypalUser),
		NVPParam("PWD", Config.paypalPassword),
		NVPParam("SIGNATURE", Config.paypalSignature),
		NVPParam("VERSION", Config.paypalNVPVersion))

	// Takes a series of parameters, send a request to Paypal, returns response.
	def NVPRequest (params: List[NVPParam]) = {

		val client = new OkHttpClient()
		val reqBody = new FormBody.Builder()

		defaultNVPParams.foreach(param => reqBody.add(param.name, param.value))
		params.foreach(param => reqBody.add(param.name, param.value))

		val request = new Request.Builder()
			.url(Config.paypalSandboxUrl)
			.post(reqBody.build())
			.build()

		client.newCall(request).execute()

	}

	// Takes an NVP response and retrieves a given parameter as a string.
	def retrieveNVPParam (response: Response, paramName: String) = {

		val responseBody = response.body().string()
		val queryParams = parseQuery(responseBody)
		queryParams.paramMap.get(paramName).get(0)

	}

	// Retrieves a payment token from an NVP response, and wraps it in JSON for
	// sending back to the client.
	def tokenJsonResponse (response: Response) = {

		val token = Token(retrieveNVPParam(response, "TOKEN"))
		Json.toJson(token)

	}

	// Sends a request to Paypal to create billing agreement and returns BAID.
	def retrieveBaid (token: Token) = {

		val agreementParams = List(
			NVPParam("METHOD", "CreateBillingAgreement"),
			NVPParam("TOKEN", token.token))

		val response = NVPRequest(agreementParams)
		retrieveNVPParam(response, "BILLINGAGREEMENTID")

	}

	// Sets up a payment by contacting Paypal, returns the token as JSON.
	def setupPayment = Action {

		val paymentParams = List(
			NVPParam("METHOD", "SetExpressCheckout"),
			NVPParam("PAYMENTREQUEST_0_PAYMENTACTION", "SALE"),
			NVPParam("PAYMENTREQUEST_0_AMT", "4.50"),
			NVPParam("PAYMENTREQUEST_0_CURRENCYCODE", "GBP"),
			NVPParam("RETURNURL", "http://localhost:9000/create-agreement"),
			NVPParam("CANCELURL", "http://localhost:9000/cancel"),
			NVPParam("BILLINGTYPE", "MerchantInitiatedBilling"))

		val response = NVPRequest(paymentParams)
		Ok(tokenJsonResponse(response))

	}

	// Creates a billing agreement using a payment token.
	def createAgreement = Action { request =>

		request.body.asJson.map { json =>

			Json.fromJson[Token](json) match {
				case JsSuccess(token: Token, _) => Ok(retrieveBaid(token))
				case e: JsError => BadRequest(JsError.toJson(e).toString)
			}

		}.getOrElse(BadRequest)

	}

}
