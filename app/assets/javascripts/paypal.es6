export function init () {

	paypal.Button.render({
				
		env: 'sandbox', // Specify 'production' for the prod environment

		// Called when user clicks Paypal button.
		payment: function (resolve, reject) {

			const SETUP_PAYMENT_URL = '/setup-payment?amount=4.50&currency=GBP';

			paypal.request.post(SETUP_PAYMENT_URL)
				.then(data => {
					resolve(data.token);
				})
				.catch(err => {
					reject(err);
				});

		},

		// Called when user finishes with Paypal interface (approves payment).
		onAuthorize: function (data, actions) {
			console.log('Authorised.');
	   }
			
	}, '#paypal-button');

};
