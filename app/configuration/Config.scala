package configuration

object Config {

	val paypalSandboxUrl = "https://api-3t.sandbox.paypal.com/nvp"
	val paypalUser = sys.env.get("PAYPAL_SANDBOX_USER").get
	val paypalPassword = sys.env.get("PAYPAL_SANDBOX_PWD").get
	val paypalSignature = sys.env.get("PAYPAL_SANDBOX_SIG").get
	val paypalNVPVersion = "124.0"

}
