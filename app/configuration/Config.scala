package configuration

object Config {

	val paypalSandboxUrl = "https://api-3t.sandbox.paypal.com/nvp"
	val paypalUser = sys.env.get("PAYPAL_SANDBOX_USER")
	val paypalPassword = sys.env.get("PAYPAL_SANDBOX_PWD")
	val paypalSignature = sys.env.get("PAYPAL_SANDBOX_SIG")

}
