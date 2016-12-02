export function init () {

	paypal.Button.render({
				
		env: 'sandbox', // Specify 'production' for the prod environment

		// Called when user clicks Paypal button.
		payment: function (resolve, reject) {
			console.log('Setting up payment.')
		},

		// Called when user finishes with Paypal interface (approves payment).
		onAuthorize: function (data, actions) {
			console.log('Authorised.');
	   }
			
	}, '#paypal-button');

};
