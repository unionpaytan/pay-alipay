const axios = require('axios');
const querystring = require('querystring');
const puppeteer = require('puppeteer');
const express = require("express");
const bodyParser = require("body-parser");//默认只接收 100 K
const app = express();
app.use(bodyParser.json({limit: '5mb'}));
app.use(bodyParser.urlencoded({limit: '5mb', extended: true}))
//app.use(express.static("public"));
app.use('./uploads', express.static('uploads')); //设置静态文件目录为 uploads 文件夹
const port = 30003;
app.listen(port, function() {
    console.log("Server started on por " + port);
});

app.get("/",async(req,res)=>{
   return res.send("200");
})

app.post("/queryAlipay", async (req, res) => { 
  const requestBody = req.body;
  const businessType = requestBody.businessType;
  const alipayUrl = requestBody.alipayUrl;
  const cookie = requestBody.cookie;
  const token = requestBody.token;
  const uid = requestBody.uid;
  const startTime = requestBody.startTime;
  const endTime = requestBody.endTime; 
 
  let result = await doPostDataToServer(businessType,alipayUrl,cookie,token,uid,startTime,endTime)
  const resData = result; 
  return res.send(resData);  
}) 
 
async function doPostDataToServer(businessType,alipayUrl,cookie,token,uid,startTime,endTime){

  let postData =  {
      "ctoken": token, 
      "_output_charset": "utf-8", 
      "billUserId": uid, //订单状态
      "entityFilterType":1,
      "tradeFrom": "ALL",
      "targetTradeOwner": "USERID", //运行结束的时间 ｜｜图片保存名称以 taskEndtime 条件
      "pageNum": 1, //备注
      "pageSize": 100,
      "startTime" : startTime,
      "endTime" : endTime,
      "status":businessType == "1" ? "WAIT_SELLER_SEND" : "SUCCESS",
      "sortType":0,
      "_input_charset":"gbk", 
     };  
 
   const headerConfig = {
      headers: {
        "Host":"mbillexprod.alipay.com",
        'Accept': 'application/json', 
        "Accept-Encoding":"gzip, deflate, br",
        "Accept-Language":"zh-CN,zh;q=0.9",
        "Connection":"keep-alive",
        "Content-Type":"application/x-www-form-urlencoded; charset=UTF-8;",
        "Cookie":cookie,
        "Origin":"https://b.alipay.com",
        "Referer":"https://b.alipay.com/",
        "User-Agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36", 
      }
  };
   let res = await axios.post(alipayUrl,postData,headerConfig)
    .then(function (response) {
      console.log(">>SAVE FINAL RESULT TO SEVER");
      console.log(response.data);
      return response.data;
    })
    .catch(function (error) {
      console.log("err:FAIL TO SAVE RESULT TO JAVA SERVER");
      console.log(error);
      return error;
    });
    return res;

}