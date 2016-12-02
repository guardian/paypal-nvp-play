module.exports = {
	entry: './app/assets/javascripts/main.es6',
	output: {
		filename: 'public/javascripts/bundle.js'
	},
	module: {
		loaders: [
			{
				test: /\.es6$/,
                exclude: /node_modules/,
                loader: 'babel-loader',
                query: {
                    presets: ['es2015']
                }
            }
		]
	}
}
