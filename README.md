# Paypal NVP Play

A simple Play app for communicating with Paypal's NVP API. Designed to produce billing agreement ids (BAIDs) for use with subscription systems like Zuora.

## Install

Requires `sbt` and `node` installed. Clone the repo, and then run:

```
npm install
```

## Build and Run

To build the assets:

```
npm run build
```

The app is built against scala `2.11.8` and Play `2.5.10`, and requires your Paypal sandbox credentials to be available in environment variables, as follows:

```
export PAYPAL_SANDBOX_USER=<sandbox_user>
export PAYPAL_SANDBOX_PWD=<sandbox_password>
export PAYPAL_SANDBOX_SIG=<sandbox_signature>
```

Then, to run it, launch `sbt` and type `run`, access on `localhost:9000`.