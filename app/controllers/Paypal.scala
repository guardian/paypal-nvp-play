package controllers

import play.api.mvc.{Controller, Action}
import play.api.mvc.Results.BadRequest
import play.api.libs.json.{Json, JsSuccess, JsError}
import okhttp3.{OkHttpClient, FormBody, Request, Response}
import com.netaporter.uri.Uri.parseQuery
import configuration.Config

class Paypal extends Controller {

	case class Token (token: String)
	case class NVPParam (name: String, value: String)

	implicit val tokenWrites = Json.writes[Token]
	implicit val tokenReads = Json.reads[Token]

	private val defaultNVPParams = List(
		NVPParam("USER", Config.paypalUser),
		NVPParam("PWD", Config.paypalPassword),
		NVPParam("SIGNATURE", Config.paypalSignature),
		NVPParam("VERSION", Config.paypalNVPVersion))

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

	def createAgreement = Action { request =>

		val tokenFromJson = request.body.asJson match {
			case Some(json) => Json.fromJson[Token](json)
			case _ => None
		}

		tokenFromJson match {

			case JsSuccess(token: Token, _) => {

				val agreementParams = List(
					NVPParam("METHOD", "CreateBillingAgreement"),
					NVPParam("TOKEN", token.token))

				val response = NVPRequest(agreementParams)
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
