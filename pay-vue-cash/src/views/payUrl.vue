<template>
  <div class="container">
    <v-alert color="#008de6" dark icon="mdi-material-design" border="right">
      <v-row align="center">
        <v-col class="grow"> {{ title }}</v-col>  
      </v-row>
    </v-alert>
    <!-- QRCODE订单 -->
    <div 
       v-if="recharge_show" 
       v-loading="d_loding"
       style="margin-top: 2px">
      <div>
        <div v-if="!d_loding">   
           <div class="row"> 
            <!-- <div class="col-12 center" style="color: #e21735;font-size:16px;">请截图保存，并用{{coinType == "0" ? "微信" : (coinType == "1" ? "支付宝" : "云闪付")}}<br>扫一扫支付</div> -->
             <div class="col-12 center" @click="toastMessage('¥' + responseData.amtPay + ' 已复制', 'default')">
                <div v-clipboard:copy="responseData.amtPay"><span style="color: #e21735;font-size:20px;">¥{{ responseData.amtPay }}</span><img src="../assets/copy.png" style="width: 13px; margin-left: 3px"/><span style="color:#575757">复制</span></div>
            </div>
            <div class="col-12 center">
              {{ $t("lang.订单号") }}:{{ responseData.merchantWithdrawNo }}
              <!-- <br>
              {{ $t("lang.订单金额") }}:{{ responseData.amt }} 元 -->
              <br>剩余支付时间: <span style="color: #e21735;font-size:16px;">{{ seconds_to_minsec(payTime) }}</span>
            </div>
            </div>
         <div class="row" style="overflow-y: auto;max-height:310px;overflow-x: hidden;width: 100%;"> 
            <!-- <div class="col-12 center" style="color: #e21735;font-size:16px;">请截图保存，并用{{coinType == "0" ? "微信" : (coinType == "1" ? "支付宝" : "云闪付")}}<br>扫一扫支付</div> -->
             <div class="col-12 center">
              <img class="authurl" :src="payerAddrUrl" alt /></div> 
          </div>

          <div class="row">
             <div class="col-12 center"  @click="toastMessage('' + responseData.payerName + ' 已复制', 'default')">
               <span style="color: #e21735;font-size:16px;">{{ responseData.payerName }}</span><div v-clipboard:copy="responseData.payerName"><img src="../assets/copy.png" style="width: 13px; margin-left: 3px;c="><span style="color:#575757">复制</span></div>
            </div>
          </div>   

         <div class="row">
            <div class="col-12 center"> 
              <el-button v-if="coinType == 1" type="primary" :loading="loading" @click.native="handleScanPay()">直接启动支付宝付款</el-button>
          </div>  
         </div>  

           <div class="row"> 
                 <div v-if="businessType == '0'" class="col-12 center" style="margin-top:5px;"> 
                   <el-button disabled type="warning" :loading="c_loading" @click.native="handleConfirm()">已完成付款</el-button><br>
                    <span style="color: #e21735;font-size:14px;">{{confirmTxt}}</span>
                </div>  
          
          <div class="col-12 center" style="margin-top:-14px;">
          <span  style="overflow-wrap:break-word;">  
              <span style="line-height:150%;">  
                <br>
                <br>支付金额必须为¥{{ responseData.amtPay }},否则无法入账
                <br>每个付款码只能付款一次，请不要重复支付
                <br>请截图保存后用{{coinType == "0" ? "微信" : (coinType == "1" ? "支付宝" : "云闪付")}}扫码付款
                <br><span v-if="coinType == '1'">或者直接启动支付宝进行付款</span>
                <br><span style="color:'#ff0000'">付款完成后请点击“已完成付款”按钮</span>
                 <br><span style="color:'#ff0000'">付款后若5分钟内没到账,请截图并提供给客服核实</span>
                 <br> <br>
                </span>
             </span> 
            </div>
            </div> 
         
 

            




            <v-alert color="#008de6" dark border="right">
              <v-row align="center"> 
                <span style="font-size:11px;margin-top:4px;">&copy; Powered by {{coinType == "0" ? "WECHAT" : (coinType == "1" ? "ALIPAY" : "UNIONPAY")}}</span>
               </v-row>
             </v-alert>
           </div>
      </div>
    </div>
 
 <!-- Modal --> 
 <!-- <el-dialog 
      :visible.sync="dialogVisible" 
      @close="handleDialogClose"
      :before-close="handleBeforeClose"
      :show-close="false"
      width="90%"
    >
      <el-input clearable v-model="fromAddr" placeholder="正确填写付款人姓名会更快到账"></el-input>
      <div slot="footer" class="dialog-footer"> 
        <el-button type="primary" @click="handleFromAddr">确认</el-button>
      </div>
 </el-dialog>   -->
  </div>
</template>
<script>
import Vue from "vue";
import Vuetify from "vuetify";
import VueToast from "vue-toast-notification";
import "vue-toast-notification/dist/theme-sugar.css";
import qrcode from "qrcode";
import tcqrcode from "tc-qrcode";

export default {
  name: "payUrl",
  data() {
    return { 
      businessType:"",
      dialogVisible: false,
      inputText: '',
      merchantChannelCode:'8001', //支付宝扫码
      loading:false,
      tcqrcodeUrl:"",
      v_loding:false,
   
      title: "订单支付",
      isTrc20: 0, //是否选中
      isErc20: 0,
      coinType: 0,
      selectedChannelCode:"",//通道号
      amtRmb: 100, //要支付的金额 rmb
      money8: 0,
      money100: 100,
      money200: 0,
      money500: 0,
      money2000: 0,
      money5000: 0,
      money16000: 0,
      amtBounsText: 0, //兑换成美金后随机立减的数字 以区分 5*60秒 内不同的订单
      amtBouns: 0,
      clickButton: 0,
      usdtLive: 6.48, //汇率
      amtUsdt: 0,
      d_loding: false,
      c_loading:false,
      recharge_show: false,
      amt: 0, //玩家应付的实际金额
      confirmTxt:"",
      isPaid:false,
      tron: "",
      eth: "",
      userAddress: "",
      surroundings: "",
      linkId: "",
      ABI: {},

      formData: {
        merchantId: "", //登录后返回的商户编号
        batchList: [], //参考提现接口batchList
      },

      responseData: {
        coinType: 0,
        moneyType: 0,
        merchantWithdrawNo: "",
        amt: 0,
        amtRmb: 0,
        usdtLive: 0,
        amtBonus: 0,
        payerAddr: "",
        payerName: "",
        timestamp: "",
        orderAmt: 0,  
      },
      fromAddr:"",
      payerAddrUrl: "",
      payTime: 0, //多长时间后订单无效
      intervalTime: {}, //倒计时
      intervalMonitor: {}, //监听是否成功
      payUrl: "",
      isEng:false,
    };
  },
  beforeCreate() {
    document.title = "订单支付";
    document.querySelector("body").style.height = "820px";
    document.querySelector("body").style.background = "#f5f5f5";
  },
  created() {
    this.initLocal(); //国际化
    this.init(); 
  },
  mounted() {},
  watch: {
    payTime: function () {
      if (this.payTime < 1 && this.payTime > -1) {
        console.log(this.payTime);
        this.recharge_show = false;
        clearInterval(this.intervalTime);
        clearInterval(this.intervalMonitor);
        this.title = this.$i18n.t("lang.付款失败");
        this.toastMessage(this.$i18n.t("lang.付款失败"), "error");
      }
    },


    recharge_show: function () {
      if (this.recharge_show == false) {
        clearInterval(this.intervalMonitor);
        clearInterval(this.intervalTime);
      }
    },
  },
  methods: { 
    hasAtLeastTwoChinese(inputString) {
      const chineseRegex = /[\u4e00-\u9fa5]/g;
      const matches = inputString.match(chineseRegex);
      return matches && matches.length >= 2;
    },
    handleDialogClose() {
      // 在这里可以执行关闭弹窗的逻辑
      this.dialogVisible = false;
    },
     handleBeforeClose(done) {
      // 在这里通过条件判断来阻止关闭
      // 例如，以下是一个简单的例子，禁止点击右上角关闭按钮关闭弹窗
      done(false);
      this.toastMessage("请点击确认按钮","error");
    },
    async handleFromAddr() {
       const that = this; 
      // 处理输入文本的逻辑
      console.log('Input text:', this.fromAddr); 
      const isValid = this.hasAtLeastTwoChinese(this.fromAddr);
      console.log("isValid",isValid);
      if (!isValid) {
        //  that.toastMessage("请正确填写付款人姓名","error");
        //  return;
      }
   
      let data = "fromAddr="+this.fromAddr+"&merchantWithdrawNo=" + this.responseData.merchantWithdrawNo;
      let res = await this.fetchUrl("/deposit/payerConfirmFromAddr", data, "post");
      // 关闭弹窗
      this.dialogVisible = false;
    },
    init() {
      this.payUrl = this.getUrlQueryString();
      //console.log("payUrl - " + this.payUrl);
      this.getPayUrl(this.payUrl); 
      this.intervalBouns();
    },
    initLocal() {
      var lng = navigator.languages;
      console.log(lng);
      if (lng[0].indexOf("en-US") > -1 || lng[0].indexOf("en") > -1) {
        this.$i18n.locale = "EN";
        this.title = "订单支付";
        this.isEng = true;
      } else {
        this.$i18n.locale = "CN";
        this.title = "订单支付";
        this.isEng = false;
      }
      //this.$i18n.locale = 'CN';
      //this.title = "订单支付"
      console.log("language - " + lng);
    },

    handleScanPay(){  
      const that = this;
      this.loading = true;
      if (that.tcqrcodeUrl == "") {
         this.toastMessage("支付宝启动失败,请用支付宝扫码支付", "error");
          setTimeout(()=>{
            that.loading = false;
        },3000)
      }else {
       // window.location.href = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + that.tcqrcodeUrl;
         window.location.href = that.tcqrcodeUrl;
        setTimeout(()=>{
            that.loading = false;
        },3000)
      }
    
    },

   async handleConfirm(){
     const that = this;
     this.c_loading = true;
      let data = "merchantWithdrawNo=" + this.responseData.merchantWithdrawNo;
      let res = await this.fetchUrl("/deposit/payerConfirmPaid", data, "post");
      if (res != null) {
        if (res.code == "0000") {
           this.confirmTxt = "感谢付款，正在核实款项";
           this.isPaid = true;
        }else {
          this.confirmTxt = res.message;
        }
         setTimeout(()=>{
            that.c_loading = false;
        },200)
      }
    },

     changeLocal(v){
      this.$i18n.locale = v;
    },

    intervalBouns() {
        this.responseData.amtBonus =  (this.getRandomFloat(101, 599) / 10000).toFixed(2);
        console.log("amtBonus - ",this.responseData.amtBonus); 
    },

   getRandomFloat(min, max) {
      return Math.floor(Math.random() * (max - min)) + min;
    },

    //TODO: 在验证支付链接 verifyPayUrl 的时候 获取用户的IP
    async getPayUrl(url) {
      const that = this;
      let data = "url=" + url;
      let res = await this.fetchUrl("/deposit/verifyPayUrl", data, "post");
      console.log("payInfo - " + JSON.stringify(res));
      if (res.code != "0000") {
        this.recharge_show = false;
       
        this.title = res.message;
        this.dialogVisible = false;
        clearInterval(that.intervalMonitor);
        clearInterval(that.intervalTime);
        //that.payerAddrUrl = "https://uploads.30pay.info/uploads/qrcode/13118354708.jpg";//
      } else {
        //payInfo - {"code":"0000","message":"成功","data":
       
        this.recharge_show = true;
        clearInterval(that.intervalMonitor); //reset
        clearInterval(that.intervalTime);

        let payInfo = res.data.payInfo;
        let payInfoArray = payInfo.split(":"); 
        let payerName =  payInfoArray[7] != null ? payInfoArray[7] : ""; //收款人名称
        var arr = payerName.split("");
        var newPayerName = "";
        for (var i=0;i<arr.length;i++) {
            // if (i > 1 && i < arr.length - 2){
            //     newPayerName =newPayerName + "*";
            // }
            // else {
            //     newPayerName =newPayerName + arr[i];
            // }
             newPayerName = newPayerName + arr[i];
            
        }
        //收款钱包地址:商户单号:平台订单号:订单金额:优惠立减:实付金额:timestamp:收款人名称
        that.responseData.merchantWithdrawNo = payInfoArray[1];
        //that.responseData.amtBonus = payInfoArray[4]; //优惠立减 
        that.responseData.amtPay = payInfoArray[5]; //redis:amtpay
        that.responseData.payerAddr = payInfoArray[0];
        that.responseData.amt = payInfoArray[3]; 
        that.responseData.payerName = newPayerName;
        that.responseData.businessType = payInfoArray[10]; 

        const totalSeconds = 60 * 10; //10分钟过期 

        //固码的方式 而不是由GCASH账号生成二维码
        //that.generateQrcode(that.responseData.payerAddr);
        console.log("that.responseData.payerAddr" + that.responseData.payerAddr);
        //that.payerAddrUrl = "https://uploads.30pay.info/uploads/qrcode/" + that.responseData.payerAddr + ".jpg";//13118354708.jpg
        //that.payerAddrUrl = process.env.VUE_APP_UPLOAD_URL + "/uploads/" + that.responseData.payerAddr + '.png';
        that.payerAddrUrl = process.env.VUE_APP_UPLOAD_URL + "/" + that.responseData.payerAddr + '.png';
        console.log("qrcode url - ",that.payerAddrUrl); 
        const tempImageUrl = that.payerAddrUrl;
        tcqrcode.decodeFromUrl(tempImageUrl).then(res=>{
            console.log("decode from url - ",res);
           // if (res.indexOf("alipays://platformapi/startapp?appId") > -1){
            if (res.indexOf("https://qr.alipay.com/") > -1){
                that.tcqrcodeUrl = res;
            }else {
                that.tcqrcodeUrl = "";
            }
           
        }); 

        that.monitorSuccess(payInfoArray[2]);
        that.responseData.timestamp = payInfoArray[6]; //REDIS的时间戳
        that.responseData.coinType = payInfoArray[8];//coinType
        that.coinType = payInfoArray[8];
        that.selectedChannelCode = payInfoArray[9] || ""; //zzm U1713775996149215734
        var timestamp = new Date().getTime(); //当前客服端的时间戳 改为服务器时间

        let serverTimeRes = await this.fetchUrl("/deposit/getServerTime", "", "post");
        if (serverTimeRes != null) {
          console.log("server - local " + (serverTimeRes.data.serverTime - timestamp) / 1000);
          timestamp = serverTimeRes.data.serverTime;
        } 
        //console.log("redis - " + that.responseData.timestamp + " , now " + timestamp);
        var seconds = (
          (timestamp - that.responseData.timestamp) /
          1000
        ).toFixed(0); //时间差的秒数
        console.log("seconds - " + seconds);
        that.payTime = totalSeconds - seconds; //剩余的秒数

        if (that.payTime < 1) {
          that.recharge_show = false;
         
          that.title = that.$i18n.t("lang.支付链接已过期");
          clearInterval(that.intervalMonitor);
          clearInterval(that.intervalTime);
          return;
        }
        that.intervalPayTime();
      }
    },

    intervalPayTime() {
      const that = this;
      this.intervalTime = setInterval(function () {
        that.payTime--;
      }, 1000 * 1);
    },
    /**
     * 监听是否付款成功
     * 数据库的订单状态为成功 则为成功
     * FIXME:待升级为查询缓存 缓解服务器压力
     */
    async monitorSuccess(withdrawNo) {
      const that = this;
      this.intervalMonitor = setInterval(function () {
        //每隔1秒查询服务器订单是否成功
        console.log("MONITOR SUCCESS -- ");
        let data = "withdrawNo=" + withdrawNo;

        let url = "/deposit/monitorSuccess";
        let method = "post";

        fetch(process.env.VUE_APP_SERVER_URL + url, {
          method: method,
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: data,
        })
          .then((res) => {
            return res.json();
          })
          .then((data) => {
            if (data != null && data.data.withdrawStatus == "1") {
              clearInterval(that.intervalMonitor);
              //console.log(JSON.stringify(data));
              that.toastMessage(that.$i18n.t("lang.支付成功"), "success");
              that.recharge_show = false;
              that.title = that.$i18n.t("lang.支付成功");
            }
          })
          .catch(function (err) {
            console.log(err);
            that.d_loding = false;
            return "";
          });
      }, 1000 * 3); //3秒监听一次
    },

    toastMessage(msg, type) {
      Vue.$toast.open({
        message: msg,
        type: type,
        position: "top",
        duration: 2000,
      });
    }, 

    async fetchUrl(url, data, method) {
      const that = this;
      return fetch(process.env.VUE_APP_SERVER_URL + url, {
        method: method,
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: data,
      })
        .then((res) => {
          return res.json();
        })
        .then((data) => {
          return data;
        })
        .catch(function (err) {
          console.log(err);
          that.d_loding = false;
          return "";
        });
    },

    generateQrcode(url) {
      var opts = {
        errorCorrectionLevel: "H",
        type: "image/jpeg",
        rendererOpts: {
          quality: 1,
        },
      };
      let that = this;
      qrcode.toDataURL(url, opts, function (err, url) {
        if (err) throw err;
        that.payerAddrUrl = url;
      });
    }, 
    /**
     * 将秒转换为 分:秒
     * s int 秒数
     */

    seconds_to_minsec(s) {
      //计算分钟
      //算法：将秒数除以60，然后下舍入，既得到分钟数
      var h;
      h = Math.floor(s / 60);
      //计算秒
      //算法：取得秒%60的余数，既得到秒数
      s = s % 60;
      //将变量转换为字符串
      h += "";
      s += "";
      //如果只有一位数，前面增加一个0
      h = h.length == 1 ? "0" + h : h;
      s = s.length == 1 ? "0" + s : s;
      return h + ":" + s;
    }, 
  

    //获取URL
    getUrlQueryString(urls) {
      urls = urls || window.location.href;
      return urls;
    },

    showVideo() { 
      const that = this;
      this.v_loding = true;
      
      setTimeout(function(){
         that.v_loding = false;
      },1000);
    },

    closeVideo() {
     
    },

  },
};
</script>

<style>
.el-dialog {
  width:90%;
} 
.kuang {
  height: 50px;
  text-align: center;
}
.kuang-no-color {
  color: #000;
  padding-top: 14px;
}
.kuang-color {
  background-color: rgb(31, 173, 126);
  color: #fff;
  padding-top: 14px;
}
.text13 {
  font-size: 13px;
}

.money-no-color {
  height: 30px;
  text-align: center;
  padding-top: 5px;
}

.money-color {
  background-color: rgb(31, 173, 126);
  color: #ffffff;
  height: 30px;
  text-align: center;
  padding-top: 5px;
}

.authurl {
  width: 191px;
}
.center {
  text-align: center;
}
.modal {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
  }

  .modal-content {
    background-color: #fff;
    margin: 15% auto;
    padding: 20px;
    max-width: 400px;
    text-align: center;
    position: relative;
  }

  .close {
    position: absolute;
    top: 0;
    right: 0;
    padding: 10px;
    cursor: pointer;
  }
  .col-12 {
    margin-top:-10px;
  }
</style>  