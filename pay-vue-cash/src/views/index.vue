<template>
  <div class="container">
    <v-alert color="#e21735" dark icon="mdi-material-design" border="right">
      <v-row align="center">
        <v-col class="grow">{{$t('lang.数字货币充值')}}</v-col> 
      </v-row>
    </v-alert>  

    <!-- 充值金额 -->
    <v-alert outlined color="purple">
      <div class="text-h6" style="line-height: 180%">
        {{$t('lang.充值金额')}}<span style="font-size: 12px"> (RMB)</span><br />
      </div>
      <v-row align="center">

        <v-col>
          <v-card
            @click="clickMoney(100)"
            :class="money100 == 100 ? 'money-color' : 'money-no-color'"
            elevation="2"
            >
            <div class="text_number">100</div>
            <div class="text_symbol">RMB</div>
            </v-card>
        </v-col>

        <v-col>
          <v-card
            @click="clickMoney(200)"
            :class="money200 == 200 ? 'money-color' : 'money-no-color'"
            elevation="2"
            >
            <div class="text_number">200</div>
            <div class="text_symbol">RMB</div>
            </v-card>
        </v-col>

        <v-col>
          <v-card
            @click="clickMoney(300)"
            :class="money300 == 300 ? 'money-color' : 'money-no-color'"
            elevation="2"
            >
            <div class="text_number">300</div>
            <div class="text_symbol">RMB</div>
            </v-card>
        </v-col>

        <v-col>
          <v-card
            @click="clickMoney(500)"
            :class="money500 == 500 ? 'money-color' : 'money-no-color'"
            elevation="2"
            >
            <div class="text_number">500</div>
            <div class="text_symbol">RMB</div>
            </v-card>
        </v-col>
        <v-col>
          <v-card
            @click="clickMoney(800)"
            :class="money800 == 800 ? 'money-color' : 'money-no-color'"
            elevation="2"
            >
            <div class="text_number">800</div>
            <div class="text_symbol">RMB</div>
            </v-card>
        </v-col>
          <v-col>
          <v-card
            @click="clickMoney(1000)"
            :class="money1000 == 1000 ? 'money-color' : 'money-no-color'"
            elevation="2"
            >
            <div class="text_number">1000</div>
            <div class="text_symbol">RMB</div>
            </v-card>
        </v-col>  

      </v-row>

      <v-row align="center">
        <v-col>
          <v-card
            @click="clickMoney(10)"
            :class="money10 == 10 ? 'money-color' : 'money-no-color'"
            elevation="2"
            style="width:100%"
            >
            <div class="text_number">10</div>
            <div class="text_symbol">RMB</div>
            </v-card>
        </v-col> 
      </v-row>

      <!-- <div
        class="text-h6"
        style="
          color: #c51162;
          line-height: 120%;
          margin-top: 30px;
          font-size: 12px;
        "
      >
        {{$t('lang.优惠立减')}}：0.01 - 0.30元<br />
      </div> -->
      <div
        class="text-h6"
        style="line-height: 130%; margin-top: 10px; font-size: 12px"
      >
        {{$t('lang.充值金额')}}：¥{{ responseData.amt }}<br />
      </div>
      <!-- <div
        class="text-h6"
        style="line-height: 130%; margin-top: 10px; font-size: 12px"
      >
        {{$t('lang.应支付')}}(RMB) = ¥{{ responseData.amt }}<br />
      </div> -->
    </v-alert>
        <!-- 通道 -->
    <v-alert outlined color="purple">
      <div class="text-h6" style="line-height: 180%">通道<span style="font-size: 12px"></span><br />
      </div>
     <v-row align="center">
        <v-col>
          <v-card
            @click="clickChannelType(8001)"
            :class="this.merchantChannelCode == 8001 ? 'money-color' : 'money-no-color'">
            <div class="text_number">个码</div>
            <div class="text_symbol">Person</div>
            </v-card>
        </v-col>
         <v-col>
          <v-card
            @click="clickChannelType(8003)"
            :class="this.merchantChannelCode == 8003 ? 'money-color' : 'money-no-color'">
            <div class="text_number">周转码</div>
            <div class="text_symbol">Business</div>
            </v-card>
        </v-col> 
     </v-row>
    </v-alert>
    <!-- 充值类别 -->
    <v-alert outlined color="purple">
      <div class="text-h6" style="line-height: 180%">充值类别<span style="font-size: 12px"></span><br />
      </div>
     <v-row align="center">
        <v-col>
          <v-card
            @click="clickCoinType(0)"
            :class="this.coinType == 0 ? 'money-color' : 'money-no-color'">
            <div class="text_number">微信</div>
            <div class="text_symbol">Wechat</div>
            </v-card>
        </v-col>
         <v-col>
          <v-card
            @click="clickCoinType(1)"
            :class="this.coinType == 1 ? 'money-color' : 'money-no-color'">
            <div class="text_number">支付宝</div>
            <div class="text_symbol">Alipay</div>
            </v-card>
        </v-col>
         <v-col>
          <v-card
            @click="clickCoinType(2)"
            :class="this.coinType == 2 ? 'money-color' : 'money-no-color'">
            <div class="text_number">聚合码</div>
            <div class="text_symbol">Unionpay</div>
            </v-card>
        </v-col>
     </v-row>
    </v-alert>
    <!-- 立即充值 -->
    <div class="my-2">
      <v-btn
        v-if="clickButton == 0"
        @click="clickRecharge()"
        style="margin-left: 0px; font-size: 16px; width: 100%; height: 50px"
        color="#e21735"
        dark
        >{{$t('lang.立即充值')}}</v-btn
      >

      <v-btn
        v-if="clickButton == 1"
        style="margin-left: 0px; font-size: 16px; width: 100%; height: 50px"
        color="#c0c0c0"
        dark
        >{{$t('lang.立即充值')}}</v-btn
      >
    </div> 

    <!-- style="overflow:auto;margin:auto;width:93%;padding:15px;top:15px;border-radius: 5px;position:fixed;background-color:#c0c0c0;" -->
     <div
          v-if="video_show" 
          style="height: 100%; overflow:scroll;width:105%;margin-left:-20px;padding:15px;top:0px;border-radius: 5px;position:fixed;background-color:rgba(0,0,0,0.3);"
        >
      <div v-loading="v_loding">
        <div class="row">
          <div class="col-3"></div>
          <div class="col-2"></div>
          <div class="col-3"></div> 
        </div> 
        </div>
      </div>
        
    <!-- 订单生成中 -->
    <el-dialog
      v-if="recharge_show"
      :title="showDialogTitle()"
      :visible.sync="recharge_show" 
      style="margin-top:-50px;"
    >
      <div v-loading="d_loding">
        <div v-if="!d_loding">
          <div class="row" style="margin-top: -30px">
            <div class="col-12">
              {{$t('lang.订单号')}}:{{ responseData.merchantWithdrawNo }}
            </div>  
          </div>

          <div class="row" style="margin-top: -20px">
            <div class="col-12">{{$t('lang.订单金额')}}:{{ responseData.amt }} 元</div>
          </div>
      
           <!-- <div class="row" style="margin-top: -20px">
            <div class="col-12">{{$t('lang.优惠立减')}}:{{ responseData.amtBonusText }} 元</div>
          </div>  -->

          <div class="row">
           <div class="col-12 center" style="color: #e21735;font-size:16px;">请截图保存，并用{{coinTypeTxt}}<br>扫一扫支付</div>
          </div>

          <div class="row" style="margin-top: -20px">
            <div class="col-12 center">
              <img class="authurl" :src="payerAddrUrl" alt />
            </div>
          </div>

          <!-- <div class="row"  @click="toastMessage(responseData.payerAddr + ' copied', 'default')"
          >
            <div
              class="col-12 center"
              v-clipboard:copy="responseData.payerAddr"
            >
              <span style="color: #e21735">{{ responseData.payerAddr }}</span
              ><img
                src="../assets/copy.png"
                style="width: 13px; margin-left: 3px"
              />
            </div>
          </div> -->

         <!-- 收款人名称 部份以***代替 -->
          <div class="row">
            <div class="col-12 center">
                {{ responseData.payerName }}
            </div>
          </div>  

           <div class="row" 
            @click="toastMessage('¥' + responseData.amtPay + ' copied', 'default')"
          >
            <div
              class="col-12 center"
              v-clipboard:copy="responseData.amtPay"
            >
              应支付金额：<span style="color: #e21735;font-size:20px;">¥{{ responseData.amtPay }}</span
              ><img
                src="../assets/copy.png"
                style="width: 13px; margin-left: 3px"
              />
            </div>
          </div> 

         <div class="row">
            <div class="col-12 center"> 
              <el-button type="primary" :loading="loading" @click.native="handleScanPay()">直接启动支付宝付款</el-button></div>
          </div> 

          <div class="row">
            <div class="col-12 center">
                {{$t('lang.订单剩余时间')}}: {{ seconds_to_minsec(payTime) }}
            </div>
          </div>

          <div class="row">
            <div class="col-12 center">
             <span  style="overflow-wrap:break-word;">  
              <span style="text-align:center">
                请支付金额 <span style="color: #e21735;">¥{{ responseData.amtPay }}</span><br>
                <br>注意:应支付金额必须为¥{{ responseData.amtPay }},否则无法入账。</span>
             </span> 
            </div>
          </div>

          <!-- <div class="row">
            <div class="col-12">
              <v-btn
                @click="onClickConnect()"
                style="
                  margin-top: -20px;
                  font-size: 16px;
                  width: 100%;
                  height: 50px;
                "
                color="#C51162"
                dark
                >{{$t('lang.合约快捷付款')}}</v-btn
              >
            </div>
          </div> -->
        </div>
      </div>
    </el-dialog>
    

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
  name: "index",
  data() {
    return {
      merchantChannelCode:'8003', //支付宝扫码
      loading:false,
      d_loding:false,
      v_loding:false,
      video_show: false,
      title:"聚合系统",
      isTrc20: 0, //是否选中
      isErc20: 0,
      coinType: 1,
      coinTypeTxt:"",
      // amtRmb: 100, //要支付的金额 rmb 
      money100: 100,
      money200: 0,
      money300: 0,
      money500: 0, 
      money800: 0,
      money1000: 0,
      money1500: 0,
      money2000: 0,
      money3000: 0,
      money5000: 0,
      money6000: 0,
      money8000: 0,
      money10: 0,
      
      clickButton: 0, 
    
      d_loding: false,
      recharge_show: false, 

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
        moneyType:0,//0-rmb
        merchantWithdrawNo: "",
        withdrawNo:"",
        amt: 100,
        amtBonus: 0,
        amtBonusText:0,
        amtPay: 0,
        coinType:1,
        usdtLive: 0, 
        payerAddr: "",
        payerName: "",
        amtUsdt:"",
        channelCode:"",
      },
      payerAddrUrl: "",
      tcqrcodeUrl:"",
      payTime: 60 * 3, //多长时间后订单无效
      intervalTime: {}, //倒计时
      intervalMonitor: {}, //监听是否成功
    };
  },
  beforeCreate() { 
    document.querySelector("body").style.height = "820px";
    document.querySelector("body").style.background = "#f5f5f5";
  },
  created() { 
    document.title = this.title;
    this.init();
    this.initLocal();//国际化 
  },
  mounted() {},
  watch: {
    payTime: function () {
      if (this.payTime < 1 && this.payTime > -1) {
        console.log(this.payTime);
        this.recharge_show = false;
        clearInterval(this.intervalTime);
        clearInterval(this.intervalMonitor);
        this.toastMessage(this.$i18n.t('lang.付款失败'), "error");
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
    init() {
       console.log("init");
      this.responseData.amtBonus = 0;//this.getRandomFloat(1, 30)/100;
      //console.log("init this.responseData.amtBonus " + this.responseData.amtBonus);
      //this.amtBounsText = "0.01 - 0.30"; //修改为3位小数？
      this.amtBounsText = "0"; //修改为3位小数？
      this.intervalBonus();
      this.ABI =[];
      this.handleTCQrcode();
    },
    handleTCQrcode(){
          const that = this; 
          const tempImageUrl = process.env.VUE_APP_UPLOAD_URL + "/uploads/20231220150134e07a59b1a21148579dff1288d0025340.png"; 
          tcqrcode.decodeFromUrl(tempImageUrl).then(res=>{
             console.log("decode from url - ",res);
             that.tcqrcodeUrl = res;
          }); 

    },
    initLocal(){
      var lng = navigator.languages;
      if (lng[0].indexOf('en-US') > -1 || lng[0].indexOf('en') > -1){
          this.$i18n.locale = 'EN';
          this.title = "聚合系统"
      }else {
          this.$i18n.locale = 'CN';
          this.title = "立即支付"
      }
      console.log('ual - ' + lng); 
    },
    changeLocal(v){
      this.$i18n.locale = v;
    },

    intervalBonus() {
      const that = this;
      setInterval(function () {
         that.responseData.amtBonus = 0;//that.getRandomFloat(1,30) / 100; 
      }, 1000 * 1);
    },

    async clickRecharge() {
      const that = this;
      if (this.coinType == '0') {
        this.coinTypeTxt = '微信';
      }else if (this.coinType == '1'){
        this.coinTypeTxt = '支付宝';
      }else {
        this.coinTypeTxt = '云闪付';
      }
      that.payTime =  60 * 10; //60 * 10分钟
      clearInterval(this.intervalTime); //重设倒计时
      clearInterval(this.intervalMonitor); //
      that.recharge_show = true;
      that.d_loding = true;
      let batchList = {
        amt: that.responseData.amt, 
        amtBonus: Math.abs(that.responseData.amtBonus),
        amtPay: (Number(that.responseData.amt) - Number(Math.abs(that.responseData.amtBonus))).toFixed(2), //玩家应付款的金额
      };
      this.formData.merchantId = process.env.VUE_APP_MERCHANT_ID;

      let merchantWithdrawNo = "C" + new Date().getTime() + ""; //商户订单号

      let data =
        "merchantChannelCode=" + that.merchantChannelCode + 
        "&merchantWithdrawNo=" +
        merchantWithdrawNo +
        "&coinType=" +
        that.coinType +
        "&merchantId=" +
        this.formData.merchantId +
        "&batchList=" +
        JSON.stringify(batchList);
      try {
        let res = await this.fetchUrl("/deposit/merchantDeposit", data, "post");
        //console.log("res - " + JSON.stringify(res));
        if (res != null) {
          that.d_loding = false;
        }
        if (res.code == "0000") {
          if (res.data !="" && res.data.payerUrl != "") {
             window.location.href = res.data.payerUrl;
          }else {
            this.toastMessage("支付链接生成有误", "error");
          }
        
          that.recharge_show = false;
        } else {
          this.toastMessage(res.message, "error");
          that.recharge_show = false;
        }
      } catch (error) {
        
        this.toastMessage(this.$i18n.t('lang.网络异常')+error, "error");
        setTimeout(function () {
          that.recharge_show = false;
        }, 2000);
      }
    },
    handleScanPay(){
      const that = this;
      this.loading = true;
      if (that.tcqrcodeUrl == "") {
         this.toastMessage("支付宝启动失败,请用支付宝扫码支付", "error");
      }else {
        window.location.href = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + that.tcqrcodeUrl;
        setTimeout(()=>{
            that.loading = false;
        },2000)
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
           // console.log(JSON.stringify(data.data));
            if (data != null && data.data.withdrawStatus == "1") {
              clearInterval(that.intervalMonitor);
            
              that.toastMessage(that.$i18n.t('lang.支付成功'), "success");
              that.recharge_show = false;
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
 
    //选择金额
    clickMoney(v) { 
      this.money100 = v;
      this.money200 = v;
      this.money300 = v;
      this.money500 = v;
      this.money800 = v;
      this.money1000 = v;

      this.money1500 = v; 
      this.money2000 = v;
      this.money3000 = v;

      this.money5000 = v; 
      this.money6000 = v;
      this.money8000 = v;

      this.money10 = v;  
      this.responseData.amt = v;
    },
    clickCoinType(v){
      this.coinType = v;
    },

    clickChannelType(v){
      this.merchantChannelCode = v;
    },
    getRandomFloat(min, max) {
      return Math.floor(Math.random() * (max - min)) + min;
    },
  
    showDialogTitle() {
      if (this.d_loding) {
        return this.$i18n.t('lang.订单生成中');
      } else {
        return this.$i18n.t('lang.待付款');
      }
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

    //二维码 得通过GCASH软件生成
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

    showVideo() { 
      const that = this;
      this.v_loding = true;
      this.video_show = true; 
      setTimeout(function(){
         that.v_loding = false;
      },1000);
    },

    closeVideo() {
      this.video_show = false; 
    },

    /*** end  */

 
  },
};
</script>
<style scoped>
.frame_size {
  height:55px;
  width:100px;
}
.text_number {
  font-size:18px;
}
.text_symbol {
  font-size:12px;
  margin-top:3px;
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

.money-no-color{
  height:55px;
  width:80px;
  text-align: center;
  padding-top: 5px;
}

.money-color{
  height:55px;
  width:80px;
  /* background-color: rgb(31, 173, 126); */
  background-color:#791726;
  color: #ffffff; 
  text-align: center;
  padding-top: 5px;
}
.el-dialog {
  margin-top: -15px;
  width: 90%;
  border-radius: 5px;
}
.authurl {
  width: 191px;
}
.center {
  text-align: center;
}
</style> 
<style>
@media screen and (max-width: 414px) {
  .el-dialog {
    margin-top: -15px;
    width: 90%;
    border-radius: 5px;
  }
}
</style>