export default {
    development: {
        host: '/api'
    },
    /**
     * 多一个目录 是为了反向代理目录用 https://www.xyz.com/api 反向代理到 http://localhost:7377
     * http://localhost:7377 为 java -jar 生成的包地址
     * jrebel执行:nohup java "-agentpath:/root/jrebel/lib/libjrebel64.so" -Drebel.remoting_plugin=true -jar bootpay-mngweb.jar &
     * /root/jrebel/lib/ Jrebel的安装地址 
     * */
    production: {
        host: '/api'
    },
    sit:{
        host: '/'
    }
}