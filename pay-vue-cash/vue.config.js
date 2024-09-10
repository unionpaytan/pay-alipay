console.log("当前环境:" +process.env.NODE_ENV + ",URL:" + process.env.VUE_APP_SERVER_URL)
//console.log("CONTRACT ADDR:" + process.env.VUE_APP_CONTRACT_ADDRESS);
module.exports = {
    outputDir: process.env.outputDir,
    assetsDir: 'static',
    indexPath: 'index.html',
    publicPath: '/',
    //https:false,
    devServer: {
        proxy: {
             '/api': { //api 替换为 target ==>> http://localhost:8080/api/
                target: process.env.VUE_APP_SERVER_URL, //对应自己的接口
                changeOrigin: true,
                ws: true, //ws
                pathRewrite: {
                    '/api': ''
                }
             }
        }
    },
    //打包时不生成 .map文件 避免看到源码
    productionSourceMap:false,
}