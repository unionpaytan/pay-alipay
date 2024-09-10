<!-- 主页-->
<template>
  <div style="background-color:#ffffff">
    <div class="flex mm_box">
      <div class="b_left mr15">
        <div class="b_left_top bor mb15">
          <img class="t_img" :src="imgSrc" alt />
          <h3 class="tc">{{ userData.merchantName }}</h3>
          <h4 class="tc" style="padding:10px;">{{ userData.merchantId}}</h4>
          <div class="t_font">
            <div>登录帐户：{{ userData.loginName }}</div> 
            <div>绑定邮箱：{{ userData.maillbox }}</div>
            <div>上一次登录：{{ userData.lastLoginTime }}</div>
            <div class="g1 mt10">
              <span v-if="isAdmin != 1 && userData.agentRate != '-1' &&  userData.agentRate != '-2' &&  userData.agentRate != '-3' " class="red" style="font-size:15px;">实时余额(元) : {{ accountBalance }} </span> 
            </div>
          </div>  
        </div>
        <div class="b_left_bootm bor">
          <div class="bl_title">安全设置</div>
          <div class="bl_box">
            <div class="bl_item flex flex-m flex-b">
              <div class="b1">谷歌验证</div>
              <div class v-if="userData.isbindGoogleAuth == 0">
                <el-button @click="show_AuthUrl(true)" type="text"
                  >绑定</el-button
                >
              </div>
              <div v-if="userData.isbindGoogleAuth == 1">
                <el-button @click="show_AuthUrl(false)" type="text">
                  <span class="red">解绑</span>
                </el-button>
              </div>
            </div>
            <div class="g1" v-if="userData.isbindGoogleAuth == 0">
              当前未开通谷歌证验
            </div>
            <div class="r1 mt5">验证节点：WEB提现/接口密钥</div>

            <div>
              <div class="bl_item flex flex-m flex-b top_bor">
                <div class="b1">登录密码</div>
                <div class>
                  <el-button @click="showUpdataPwd" type="text">修改</el-button>
                </div>
              </div>
              <div class="g1">6-16位字母、数字或符号相结合</div>
            </div>
          </div>
        </div>
      </div>
     <div v-if="userData.accountType == 'admin'"  class="flex-1 b-right echartOff">
         <!-- 手续费充值 --> 
        <div class="b_right_top mb15 bor" style="display:flex;flex-direction:row"> 
          <div>
            <div class="flex flex-m mb20">
                  <span class="w380">平台手续费余额(元)</span> 
              </div>
              <div class="r1 mt20" style="font-size: 36px">
                    {{ platformFee }}
                  </div>
              <el-button  @click="handleShowAccountFee"  type="primary" class="mt30">手续费充值</el-button>
          </div>
          <div>
            <div class="flex flex-m mb20">
                  <span class="w380">平台手续费费率</span> 
              </div>
              <div class="r1 mt20" style="font-size: 36px">
                    {{ platformRate }}
                  </div>
            
          </div>

        </div> 
         <!-- 手续费列表 -->
         <div class="b_right_bootm mb15 bor">
    <div style="height: 460px;overflow-y:scroll;overflow: auto;">    
     <el-table
      v-loading="m_tableLoading"
      @selection-change="m_handleSelectionChange"
      @current-change="m_handleCurrentChange"
      class="mtable"
      :stripe="true"
      tooltip-effect="dark"
      :highlight-current-row="true"
      style="max-width:800px;"
      :data="m_list"
    >
      <el-table-column align="center" prop="createTime" label="订单时间" width="150"></el-table-column> 
     
      <el-table-column align="center" prop="withdrawNo" label="订单号"></el-table-column> 
 
      <el-table-column align="center" prop="amt" label="人民币">
        <template slot-scope="scope">
          <span>{{scope.row.amt}}</span>
        </template>
      </el-table-column> 
      <el-table-column align="center" prop="amtUsdt" label="USDT"></el-table-column>  
       <el-table-column align="center" prop="amtRate" label="汇率"></el-table-column>  
       <el-table-column align="center" prop="amt" label="支付状态">
        <template slot-scope="scope">
          <span>{{scope.row.status | rechargeStatusFilter}}</span>
        </template>
      </el-table-column> 
      <el-table-column align="center" prop="remark" label="备注"  fixed="right"></el-table-column>
     

 
    </el-table>
    </div>
    <Paging
      class="mt20 mb30"
      :pageIndex="m_page.page"
      :pageSize="m_page.rows"
      :pageTotal="m_page.total"
      @changeSize="m_changesize"
      @changeIndex="m_changeindex"
    ></Paging>

         </div>
     </div>
      <!-- 商户 -->
      <div v-if="1==2" class="flex-1 b-right echartOff">
        <div class="b_right_top mb15 bor">
          <div class="flex flex-m mb20">
            <div class="g1 mt10" style="width: 200px">
              <span class="w380">平台账户余额(元)</span>
              <div class="r1 mt20" style="font-size: 36px">
                {{ accountBalance }}
              </div>
                <div>
           
          </div>

            </div>

            <div
              class="g1 mt10"
              style="width: 200px"
              v-show="userData.agentRate == '0'"
            >
              <span class="w380">单钱包单笔最高可提现(元)</span>
              <div class="r1 mt20" style="font-size: 36px">
                {{ accountHighestBalance || 0 }}
              </div>
            </div>

            <div class="g1 mt10" v-show="userData.agentRate == '0'">
              <span class="w380">手续费余额(元)</span>
              <div class="r1 mt20" style="font-size: 36px">
                {{ accountChannelFeeBalance || 0 }}
              </div>
            </div>
          </div>

          <div>
            <el-button
              v-if="userData.agentRate == '0'"
              @click="$router.push('/index/merchantWEB')"
              type="primary"
              class="mt30"
              >发起WEB代付</el-button
            >
          </div>
        </div>
        <div class="b_right_bootm bor" v-show="userData.agentRate == '0'">
          <div class="mt5" style="font-size: 18px">近7天数据</div>
          <div v-loading="m_tableLoading">
            <el-table
              :data="queryMerchantWtihdrawCountList"
              border
              class="mt20 tc"
              style="width: 100%"
            >
              <el-table-column align="center" prop="date" label="日期">
                <template slot-scope="scope">
                  <span>{{ scope.row.date | time }}</span>
                </template>
              </el-table-column>
              <el-table-column
                align="center"
                prop="successAmt"
                label="交易金额（成功）"
              ></el-table-column>
              <el-table-column
                align="center"
                prop="successPen"
                label="交易笔数（成功）"
              ></el-table-column>
            </el-table>
          </div>
        </div>
      </div>

      <!-- 管理员 统计报表:代付 temp off -->
      <div  class="flex-1 b-right echartOff" v-show="userData.accountType == 'admin-off'">
        <!-- 当周 -->
        <div class="b_right_top mb15 bor">
          <div class="Grid">
            <div class="Grid-cell">
              <div class="Grid">
                <div class="Grid-cell g1 mt10">
                  <span class="w380">当周下发总笔数</span>
                </div>
                <div class="Grid-cell g1 mt10">
                  <span class="w380">当周下发总额(元)</span>
                </div>
              </div>
              <div class="Grid">
                <div class="Grid-cell">
                  <span class="r1 mt20" style="font-size: 32px">{{
                    weekBalance.withdrawNum || 0
                  }}</span>
                </div>
                <div class="Grid-cell">
                  <span class="r1 mt20" style="font-size: 32px">{{
                    weekBalance.withdrawAmt || 0
                  }}</span>
                </div>
              </div>
              <div class="Grid">
                <div class="Grid-cell">&nbsp;</div>
              </div>

              <div class="Grid">
                <div class="Grid-cell g1 mt10">
                  <span class="w380">当周充值总额(元)</span>
                </div>
                <div class="Grid-cell g1 mt10">
                  <span class="w380">当周手续费总额(元)</span>
                </div>
              </div>
              <div class="Grid">
                <div class="Grid-cell">
                  <span class="r1 mt20" style="font-size: 32px">{{
                    weekBalance.withdrawRechargeAmt || 0
                  }}</span>
                </div>
                <div class="Grid-cell">
                  <span class="r1 mt20" style="font-size: 32px">{{
                    weekBalance.withdrawFeeAmt || 0
                  }}</span>
                </div>
              </div>
            </div>
            <div class="Grid-cell">
              <!-- pie 饼图 -->
              <div class="Grid">
                <div class="Grid-cell">
                  <ve-histogram
                    height="270px"
                    :data="pieChartData"
                    :settings="pieChartSettings"
                  ></ve-histogram>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 水晶图表 -->
        <div class="b_right_bootm bor">
          <span class="g1 mt10"></span>
          <el-date-picker
            v-model="dateFormat"
            align="right"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            picker-options="shortcuts"
            style="width: 140px"
          ></el-date-picker>
          <el-button
            class="ml20"
            size="small"
            icon="el-icon-search"
            :loading="crystal_loding"
            @click.native="m_search"
            type="primary"
            >查询</el-button
          >

          <ve-line
            :settings="chartSettings"
            :data="chartData"
            :v-show="false"
          ></ve-line>
        </div>
      </div>

    </div>

    <el-dialog
      :title="(this.isBind ? '绑定' : '解绑') + '谷歌验证'"
      :visible.sync="AuthUrl_show" 
    >
      <div v-loading="authurl_lodng">
        <div v-if="this.isBind">
          <div class="tc">
            手机打开Google Authenticator（谷歌身份验证器），扫码以下二维码
          </div>
          <img class="authurl" :src="authurl" alt />
          <div class="tc">谷歌验证码</div>
        </div>

        <div v-if="!this.isBind" class="tc red">
          提示：谷歌验证器解绑后无法再自助绑定，请联系客服人员重新绑定
        </div>
        <div class="tc mt20">
          <el-input
            v-model="googleCode"
            placeholder="请输入谷歌验证码"
          ></el-input>
        </div>

        <div class="mt30" v-if="this.isBind">
          <div>谷歌验证器下载说明：</div>
          <div class="ml30 mt10 mb10">
            安卓手机：应用商店搜索"身份证验证器","条码扫描器"下载；
          </div>
          <div class="ml30">
            苹果手机：苹果商店搜索"Google Authenticator"下载；
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="AuthUrl_show = false">取 消</el-button>
        <el-button
          :disabled="!googleCode.length"
          :loading="bindLoding"
          type="primary"
          @click="modifyGoogleAuthStatus"
          >确 定</el-button
        >
      </span>
    </el-dialog>

    <el-dialog
      title="修改密码"
      v-if="updataPwdShow"
      :visible.sync="updataPwdShow"
       
    >
      <div>
        <el-form
          :model="updataPwd"
          :rules="rules2"
          ref="ruleForm2"
          label-width="100px"
        >
          <el-form-item label="新密码" prop="password">
            <el-input
              class="passwordClass"
              type="password"
              v-model="updataPwd.password"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="re_password">
            <el-input
              class="passwordClass"
              type="password"
              v-model="updataPwd.re_password"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="谷歌验证码" prop="googleCode">
            <el-input class="w200" v-model="updataPwd.googleCode"></el-input>
            <!-- <el-button type="primary" class="ml20" @click="sendCode" :disabled="sendNum!=0" :loading="get_loading">
              <span v-if="sendNum!=0">{{sendNum}}秒后重新</span>获取<span v-if="sendNum==0">验证码</span>
           </el-button>  -->
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="updataPwdShow = false">取 消</el-button>
        <el-button
          :loading="updataPwdLoading"
          type="primary"
          @click="confirmModifyPwd"
          >确 定</el-button
        >
      </span>
    </el-dialog>

    <el-dialog
      title="手续费充值"
      v-if="showAccountFee"
      :visible.sync="showAccountFee">
      <div>
        <el-form
          :model="accountFeeObj"
          :rules="rules2"
          ref="ruleForm2"
          label-width="100px"
        >
          <el-form-item label="充值金额" prop="amt">
            <el-input
              class="passwordClass"
              type="number"
              placeholder="人民币"
              v-model="accountFeeObj.amt"
              autocomplete="off"
            ></el-input>
          </el-form-item>

          <el-form-item label="USDT" prop="amtUsdt">
            <el-input
              readonly
              class="passwordClass"
              type="number"
              placeholder="USDT"
              v-model="accountFeeObj.amtUsdt"
              autocomplete="off"
            ></el-input>
          </el-form-item>

           <el-form-item label="" prop="">
           换算汇率约为 {{accountFeeObj.amtRate}} ,汇率由 <a href="https://www.okx.com/zh-hans/p2p-markets/cny/buy-usdt" target="_blank">OKX</a> 实时提供
           </el-form-item> 
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="showAccountFee = false">取 消</el-button>
        <el-button
          :loading="accountFeeLoading"
          type="primary"
          @click="handleAccountFeeRecharge"
          >提交订单</el-button
        >
      </span>
    </el-dialog>

     <el-dialog
      title="支付手续费"
      v-if="dialogShowAccountQrcode"
      :visible.sync="dialogShowAccountQrcode">
      <div class="dialogRecharge">
           <div  style="font-size:18px;">【USDT-TRC20】</div>
           <div>订单号：{{accountFeeObj.withdrawNo}}，订单金额: {{accountFeeObj.amtUsdt}} USDT</div>
           <div>换算汇率约为 {{accountFeeObj.amtRate}} ,汇率由 <a href="https://www.okx.com/zh-hans/p2p-markets/cny/buy-usdt" target="_blank">OKX</a> 实时提供</div>
            <div><el-button  @click="handleCopyAmtUsdt" type="primary">复制金额</el-button></div>
             <div><img :src="authurl"></div>
             <div>{{trcAddress}}</div>
            <div  v-clipboard:copy="accountFeeObj.trcAddress"><el-button @click="handleCopyTrcAddress" type="primary">复制地址</el-button></div>
             <div>
               剩余支付时间: <span style="color: #e21735;font-size:16px;">{{ seconds_to_minsec(payTime) }}</span>
             </div>
              <div style="font-size:18px;margin-top:10px;">【注意事项】</div>
              <div>
                <ul>
                  <li>此地址仅授受USDDT-TRC20充值，不支持其它资产充值;</li>
                  <li>不可重复支付、修改金额、否则不到账且无法追回;</li>
                  <li>请在订单有效时间转账，超过时间会导致充值不到账;</li>
                  <li>若充值后不到账请联系系统提供方客服;</li>
                </ul>
            </div>
      </div> 
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogShowAccountQrcode = false">关闭</el-button> 
      </span>
    </el-dialog>


  </div>
</template>

<script>
import qrcode from "qrcode";
import moment from "moment";

export default {
  name: "homepage",
   watch: {
    'accountFeeObj.amt'(newValue) {
      // 假设汇率为 1 RMB = 0.15 USDT
      if (newValue < 10 && this.accountFeeObj.amtRate) {
        this.accountFeeObj.amtUsdt = "";
      }else if (newValue >= 10 && this.accountFeeObj.amtRate) {
          this.accountFeeObj.amtUsdt = parseFloat(newValue / this.accountFeeObj.amtRate).toFixed(4);
      }else {
        this.accountFeeObj.amtUsdt = "";
      }
     
    },
    dialogShowAccountQrcode:function(){
        // console.log(this.dialogShowAccountQrcode);
        if (!this.dialogShowAccountQrcode) {
           this.payTime = 60 * 10;
           clearInterval(this.intervalTime);
           clearInterval(this.intervalMonitorSuccess);
        }
    },
    payTime: function () {
        // console.log(this.payTime);
        if (this.payTime < 1 && this.payTime > -1) {
          this.dialogShowAccountQrcode = false; 
          clearInterval(this.intervalTime);
          clearInterval(this.intervalMonitorSuccess);
      
          this.m_error("支付失败");
          that.getDatalist(1);
        } 
    }

  },
  data() {
   
   
    var validatePass = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入密码"));
      } else {
        if (this.updataPwd.re_password !== "") {
          this.$refs.updataPwd.validateField("re_password");
        }
        callback();
      }
    };
    var validatePass2 = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请再次输入密码"));
      } else if (value !== this.updataPwd.password) {
        callback(new Error("两次输入密码不一致!"));
      } else {
        callback();
      }
    };

    this.chartSettings = {
      labelMap: {
        日成功订单笔数: "日成功订单笔数",
        日成功订单金额: "日成功订单金额",
      },
      legendName: {
        日成功订单笔数: "日成功订单笔数",
        日成功订单金额: "日成功订单金额",
      },
      area: true,
      axisSite: { right: ["日成功订单金额"] },
      yAxisName: ["笔数", "金额(元)"],
      metrics: ["日成功订单笔数", "日成功订单金额"],
    };

    this.pieChartSettings = {
      axisSite: { right: ["周成功订单金额"] },
      yAxisName: ["笔数", "金额(元)"],
      yAxis: {
        offset: -100,
      },
    };

    return {
      partnerDepositList:[],
      payTime: 60 * 10, //多长时间后订单无效
      intervalTime: {}, //倒计时
      intervalMonitorSuccess: {}, //监听是否成功
      trcAddress:"TRHkmDQRMYk3o3Zqd5PKKrwuk7XqjN49Up",
      imgSrc:"",
      isAdmin:0,//非管理员
      //水晶图表
      chartData: {
        columns: ["时间", "日成功订单笔数", "日成功订单金额"],
        rows: [],
      },
      //pie饼图
      pieChartData: {
        columns: ["时间", "周成功订单笔数", "周成功订单金额"],
        rows: [],
      },
      dateFormat: this.queryDateFormat(),
      crystal_loding: false,
      userData: {},
      weekBalance: {
        withdrawNum: 0,
        withdrawAmt: 0,
        withdrawRechargeAmt: 0,
        withdrawFeeAmt: 0,
      },
      AuthUrl_show: false,
      authurl_lodng: false,
      authurl: "",
      isBind: false,
      googleCode: "", //
      bindLoding: false,
      merchantId: "",
      accountFeeObj:{
        amt:"",
        amtUsdt:"",
        amtRate:"",
        rateText:"",
        withdrawNo:"",
        merchantId:process.env.VUE_APP_MERCHANT_PLATFORM_ID,
      },
      updataPwdShow: false, 
      showAccountFee:false,//提交订单
      dialogShowAccountQrcode:false,
      accountFeeLoading:false,
      updataPwdLoading: false,
      _updataPwd: {},
      updataPwd: {
        password: "",
        re_password: "",
        googleCode: "",
      },

      sendNum: 0,
      time: null,
      get_loading: false,
      platformFee:0,
      platformRate:0,
      accountBalance: "0",
      accountHighestBalance: 0, //单笔可下
      accountChannelFeeBalance: 0, //手续费余额
      dayCount: 0,
      dayAmt: 0.0,
      //代理对象
      payMerchantInfo: {},
      queryMerchantWtihdrawCountList: [],

      rules2: {
        password: [
          { required: true, validator: validatePass, trigger: "blur" },
        ],
        re_password: [
          { required: true, validator: validatePass2, trigger: "blur" },
        ],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },

  created() {
    this.userData =
      (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
    this.merchantId = this.userData.merchantId;
    this._updataPwd = this._dep(this.updataPwd);
    //console.log("userData" + this.userData.accountType);
    if (this.userData.accountType == "merchant") {
       this.isAdmin = 0;
       this.queryMerchantBalance(); //余额
      //this.queryMerchantHighestBalance(); //最高余额
      //this.queryMerchantAccountChannelFeeBalance();
   
    } else if (this.userData.accountType == "admin") {
      this.isAdmin = 1;
      this.getDatalist(1); //下发列表
      //this.queryWeekBalance();
      //this.queryDateFormat(); //日期先执行
    }
    this.init();
  },
  mounted() {},
  methods: {
    init() {
      this.m_search();
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.imgSrc = '/upload/avatar.png';//'/uploads/avatar.png';
      this.queryPlatformBalance();//查找手续费
    },
    async queryOkexTradeInfo(){
        let data = {
          userType:'all',//c2c是 all || 大宗交易是 blockTrade
        }
        let res = await this.m_api.queryOkexTradeInfo(data);
        if (res.code == "0000") {
            let infoArray = res.data;
            let infoObj = infoArray[2];
            this.accountFeeObj.amtRate = infoObj.price;
            // this.accountFeeObj.rateText = "换算汇率约为 "+ infoObj.price + ",汇率由 OKX 实时提供"
            console.log("infoObj",infoObj);
        }
    },
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

    queryDateFormat() {
      var date = new Date();
      var month = (date.getMonth() + 1).toString();
      month = month.length == 1 ? "0" + month : month;
      var day = date.getDate().toString();
      day = day.length == 1 ? "0" + day : day;
      this.dateFormat = date.getFullYear() + "-" + month + "-" + day;
      console.log(this.dateFormat);
    },

    queryCrystalData() {
      var crystalData = {
        date: this.dateFormat,
        merchantId: this.userData.merchantId,
      };
      this.m_api.queryCrystalData(crystalData).then((res) => {
        this.crystal_loding = false;
        this.chartData.rows = res.data.rows;
        this.dataEmpty = false;
        this.dayCount = 0;
        this.dayAmt = 0.0;

        res.data.rows.forEach((item) => {
          console.log(item.日成功订单金额);
          this.dayCount += parseInt(item.日成功订单笔数);
          this.dayAmt = parseFloat(
            parseFloat(this.dayAmt) + parseFloat(item.日成功订单金额)
          ).toFixed(2);
        });

        this.chartSettings = {
          labelMap: {
            日成功订单笔数: "日成功订单笔数",
            日成功订单金额: "日成功订单金额",
          },
          legendName: {
            日成功订单笔数: "日成功订单笔数(" + this.dayCount + ")",
            日成功订单金额: "日成功订单金额(" + this.dayAmt + ")",
          },
          area: true,
          axisSite: { right: ["日成功订单金额"] },
          yAxisName: ["笔数", "金额(元)"],
          metrics: ["日成功订单笔数", "日成功订单金额"],
        };
      });
    },
    show_AuthUrl(v) {
      this.googleCode = "";
      this.isBind = !!v;
      this.AuthUrl_show = true;
      if (v) {
        this.getGoogleAuthUrl();
      }
    },

    getGoogleAuthUrl() {
      let merchantId = this.merchantId;
      this.authurl_lodng = true;
      this.m_api
        .getGoogleAuthUrl({ merchantId })
        .then((res) => {
          console.log(res);
          this.authurl_lodng = false;
          let authurl = res.data.googleAuthUrl;
          // console.log(authurl)
          var opts = {
            errorCorrectionLevel: "H",
            type: "image/jpeg",
            rendererOpts: {
              quality: 1,
            },
          };
          let that = this;
          qrcode.toDataURL(authurl, opts, function (err, url) {
            if (err) throw err;
            that.authurl = url;
          });
        })
        .catch((err) => {
          console.log(err);
          this.authurl_lodng = false;
        });
    },

    modifyGoogleAuthStatus() {
      let type = this.isBind ? "bind" : "cancel";
      let data = {
        type,
        googleCode: this.googleCode,
        merchantId: this.merchantId,
      };
      this.bindLoding = true;
      this.m_api
        .modifyGoogleAuthStatus(data)
        .then((res) => {
          console.log(res);
          this.userData.isbindGoogleAuth = this.isBind ? "1" : "0";
          sessionStorage.userData = JSON.stringify(this.userData);
          let txt = this.isBind ? "绑定" : "解绑";
          this.m_success(txt + "成功");
          this.AuthUrl_show = false;
          this.bindLoding = false;
        })
        .catch((err) => {
          this.bindLoding = false;
        });
    },

    showUpdataPwd() {
      this.updataPwd = this._dep(this._updataPwd);
      this.updataPwdShow = true;
    },

    handleShowAccountFee() { 
      this.accountFeeLoading = false;
      this.accountFeeObj.amt = "";
      this.showAccountFee = true;
      this.dialogShowAccountQrcode = false;
      this.queryOkexTradeInfo();
      clearInterval(this.intervalTime);
    
    },

    handleAccountFeeRecharge() {  
       const that =this;
       this.accountFeeLoading = true;
       
       this.m_api.partnerRecharge(this.accountFeeObj).then(res=>{
            that.intervalPayTime()
           
            that.accountFeeLoading = false; 
            that.showAccountFee = false; 
            if (res.code == "0000") {
                that.accountFeeObj.withdrawNo = res.data.withdrawNo;
                console.log(res.data.withdrawNo);
                that.dialogShowAccountQrcode = true;
                that.generateQrcode(that.trcAddress);
                that.monitorSuccess(res.data.withdrawNo);
                that.getDatalist(1);
            }

       });
    },

     intervalPayTime() {
      const that = this;
      this.intervalTime = setInterval(function () {
        that.payTime--;
      }, 1000 * 1);
    },

    monitorSuccess(withdrawNo){
      const that = this;
      this.intervalMonitorSuccess = setInterval(function () {
        //每隔1秒查询服务器订单是否成功
        console.log("MONITOR SUCCESS -- "); 
        let data = {"withdrawNo":withdrawNo}
        this.m_api.queryPartnerMonitorSucess(data).then(res=>{
            if (res.code == "0000") {
                 clearInterval(that.intervalTime);
                 clearInterval(that.intervalMonitorSuccess);
                 that.dialogShowAccountQrcode = false; 
                 that.m_success("支付成功");
                 that.getDatalist(1);
            }
        });
      }, 1000 * 3); //3秒监听一次
    },

    sendCode() {
      this.get_loading = true;
      this.m_api
        .modifyPwd({ loginName: this.userData.loginName })
        .then((res) => {
          this.get_loading = false;
          this.m_success("已将验证码发送到你到邮箱:" + res.data.mail);
          this.sendCodnTime();
          return res;
        })
        .catch((res) => {
          this.get_loading = false;
        });
    },

    sendCodnTime() {
      this.time && clearInterval(this.time);
      this.sendNum = 60;
      this.time = setInterval(() => {
        this.sendNum = this.sendNum - 1;
        if (this.sendNum <= 0) {
          this.sendNum = 0;
          clearInterval(this.time);
        }
      }, 1000);
    },

    confirmModifyPwd() {
      this.updataPwdLoading = true;
      let form = {
        loginName: this.userData.loginName,
        password: this.updataPwd.password,
        googleCode: this.updataPwd.googleCode,
      };
      this.m_api
        .confirmModifyPwd(form)
        .then((res) => {
          console.log(res);
          // this.m_success('修改成功')

          this.updataPwdLoading = false;
          this.updataPwdShow = false;
          this.sendNum = 0;

          this.$alert("修改密码成功，请重新登录!", "提示", {
            confirmButtonText: "确定",
            callback: (action) => {
              sessionStorage.clear();
              this.$router.push("/login");
              setTimeout(() => {
                window.location.reload();
              }, 300);
            },
          });
        })
        .catch((err) => {
          this.updataPwdLoading = false;
        });
    },

    queryPlatformBalance() {
      this.m_api
        .queryPlatformBalance({ merchantId: process.env.VUE_APP_MERCHANT_PLATFORM_ID })
        .then((res) => {
          this.platformFee = res.data.platformFee;
          this.platformRate = res.data.platformRate * 100 + "%";
        });
    },

    //查询商户余额
    queryMerchantBalance() {
      this.m_api
        .queryMerchantBalance({ merchantId: this.userData.merchantId })
        .then((res) => {
          this.accountBalance = res.data.accountBalance;
        });
    },

    //最高的银行卡余额
    queryMerchantHighestBalance() {
      this.m_api
        .queryMerchantHighestBalance({ merchantId: this.userData.merchantId })
        .then((res) => {
          //操作成功
          this.accountHighestBalance = res.data;
        });
    },

    //手续费余额
    queryMerchantAccountChannelFeeBalance() {
      this.m_api
        .queryMerchantAccountChannelFeeBalance({
          merchantId: this.userData.merchantId,
        })
        .then((res) => {
          //操作成功
          this.accountChannelFeeBalance = res.data;
        });
    },

    queryWeekBalance() {
      //周的第一天
      //周的最后一天
      //管理员ID
      var date = new Date(); // 本周一的日期
      date.setDate(date.getDate() - date.getDay() + 1);
      var begin =
        date.getFullYear() +
        "-" +
        (date.getMonth() + 1) +
        "-" +
        date.getDate() +
        " 00:00:00"; // 本周日的日期

      date.setDate(date.getDate() + 6);

      var end =
        date.getFullYear() +
        "-" +
        (date.getMonth() + 1) +
        "-" +
        date.getDate() +
        " 23:59:59";

      //console.log("begin:%s,end:%s", begin, end);
      var curDateArr = getWeekStartAndEnd(0);
      var curWeekLastDay = curDateArr.pop();
      var curWeekFirstDay = curDateArr.pop();

      console.log("start:" + curWeekFirstDay + ",End:" + curWeekLastDay);
      var endTime = curWeekLastDay + " " + "23:59:59";
      var beginTime = curWeekFirstDay + " " + "00:00:00";

      this.m_api
        .queryWeekBalance({
          beginTime: beginTime,
          endTime: endTime,
          merchantId: this.userData.merchantId,
        })
        .then((res) => {
          //操作成功
          this.weekBalance = res.data.weekBalance;
        });

      this.m_api
        .queryWeekBalanceCrystalData({
          beginTime: beginTime,
          endTime: endTime,
          merchantId: this.userData.merchantId,
        })
        .then((res) => {
          //操作成功
          this.pieChartData.rows = res.data.rows;
        });
    },

    //获取数据信息
    getDatalist(page) {
      this.m_tableLoading = true;
      let search_data = {
        merchantId: process.env.VUE_APP_MERCHANT_PLATFORM_ID,
        beginTime: moment()
          .subtract("days", 360) //6
          .format("YYYY-MM-DD"),
        endTime: moment().format("YYYY-MM-DD"),
        rows: "20",
        page: page,
      };
      if (page == 1) {
        this.m_page.page = page;
      }
        let data = Object.assign(search_data, this.m_page);
       this.m_api
        .queryPartnerDepositList(data)
        .then((res) => {
          //console.log(res)
          this.m_list = res.data.pages.records;
          this.m_page.total = res.data.pages.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
    },

    handleCopyAmtUsdt(){
       navigator.clipboard.writeText(this.accountFeeObj.amtUsdt).then(() => {
         this.m_success(this.accountFeeObj.amtUsdt + " 金额已复制")
       }).catch(err => {
         console.error('Failed to copy text: ', err);
         this.m_error('Failed to copy text: ', err)
      });
      
    },

    handleCopyTrcAddress(){
        // console.log("address")
        // this.m_success(this.trcAddress + " 地址已复制")
         navigator.clipboard.writeText(this.trcAddress).then(() => {
         this.m_success(this.trcAddress + " 金额已复制")
       }).catch(err => {
         console.error('Failed to copy text: ', err);
         this.m_error('Failed to copy text: ', err)
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
        that.authurl = url;
      });
    },

    m_search() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      if (userData.accountType == "admin") {
        this.crystal_loding = true;
        // this.queryCrystalData();
      }
    },
  },
};

function getCurrentDate(date) {
  var year = "";
  var month = "";
  var day = "";
  var now = date;
  year = "" + now.getFullYear();
  if (now.getMonth() + 1 < 10) {
    month = "0" + (now.getMonth() + 1);
  } else {
    month = "" + (now.getMonth() + 1);
  }
  if (now.getDate() < 10) {
    day = "0" + now.getDate();
  } else {
    day = "" + now.getDate();
  }
  return year + "-" + month + "-" + day;
}

function getWeekStartAndEnd(AddWeekCount) {
  //起止日期数组
  var startStop = new Array();
  //一天的毫秒数
  var millisecond = 1000 * 60 * 60 * 24;
  //获取当前时间
  var currentDate = new Date();
  //相对于当前日期AddWeekCount个周的日期
  currentDate = new Date(
    currentDate.getTime() + millisecond * 7 * AddWeekCount
  );
  //返回date是一周中的某一天
  var week = currentDate.getDay();
  //返回date是一个月中的某一天
  var month = currentDate.getDate();
  //减去的天数
  var minusDay = week != 0 ? week - 1 : 6;
  //获得当前周的第一天
  var currentWeekFirstDay = new Date(
    currentDate.getTime() - millisecond * minusDay
  );
  //获得当前周的最后一天
  var currentWeekLastDay = new Date(
    currentWeekFirstDay.getTime() + millisecond * 6
  );
  //添加至数组
  startStop.push(getCurrentDate(currentWeekFirstDay));
  startStop.push(getCurrentDate(currentWeekLastDay));

  return startStop;
}
</script>
<style lang='scss' scoped>
.authurl {
  width: 260px;
  height: 260px;
  display: block;
  margin: 10px auto;
  border-radius: 10px;
}

.b_left {
  width: 360px;
}

.mm_box {
  box-sizing: border-box;
}

.bor {
  border: 1px solid #eee;
  border-radius: 5px;
  padding: 10px;
}

.t_img {
  display: block;
  width: auto;
  height: 105px;
  margin: 20px auto;
  border-radius: 10px;
}

.t_font {
  margin-top: 20px;
  font-weight: 400;
  font-style: normal;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.647058823529412);
  text-align: center;
  line-height: 30px;
}

.bl_title {
  font-weight: 400;
  font-style: normal;
  font-size: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.b1 {
  font-size: 14px;

  color: rgba(0, 0, 0, 0.847058823529412);
  font-weight: 650;
}
.g1 {
  font-weight: 400;
  color: rgba(0, 0, 0, 0.447058823529412);
  font-size: 14px;
  line-height: 22px;
}

.r1 {
  font-weight: 400;
  font-style: normal;
  color: #ff6600;
  font-size: 14px;
}

.top_bor {
  margin-top: 10px;
  border-top: 1px solid #eee;
}
.Grid {
  display: flex;
}

.Grid-cell {
  flex: 1;
}

@media screen and (max-width: 2500px) {
  /*当屏幕尺寸小于600px时，应用下面的CSS样式*/
  .b_left_top {
    box-sizing: border-box;

    height: 360px;
    width: 360px;
  }

  .b_left_bootm {
    box-sizing: border-box;

    height: 401px;
    width: 360px;
  }

  .b_right_top {
    box-sizing: border-box;
    width: 100%;
    height: 222px;
  }

  .b_right_bootm {
    box-sizing: border-box;
    width: 100%;
    height: 539px;
  }
   .passwordClass {
    width:300px;
  }
}

@media screen and (max-width: 414px) {
   /*当屏幕尺寸小于600px时，应用下面的CSS样式*/
  .b_left_top {
    box-sizing: border-box; 
    width: 330px;
    height: 360px;
  }

  .b_left_bootm {
    box-sizing: border-box; 
    height: 301px;
    width: 330px;
  }

  .b_right_top {
    box-sizing: border-box;
    width: 100%;
    height: 222px;
  }

  .b_right_bootm {
    box-sizing: border-box;
    width: 100%;
    height: 339px;
  }
  .echartOff {
    display: none;
  }
  .passwordClass {
    width:200px;
  }
}
@media screen and (max-width: 375px) {
  /*当屏幕尺寸小于600px时，应用下面的CSS样式*/
  .b_left_top {
    box-sizing: border-box; 
    width: 330px;
    height: 360px;
  }

  .b_left_bootm {
    box-sizing: border-box; 
    height: 301px;
    width: 330px;
  }

  .b_right_top {
    box-sizing: border-box;
    width: 100%;
    height: 222px;
  }

  .b_right_bootm {
    box-sizing: border-box;
    width: 100%;
    height: 339px;
  }
  .echartOff {
    display: none;
  }
}

@media screen and (max-width: 320px) {
   .b_left_top {
    box-sizing: border-box; 
    width: 330px;
    height: 360px;
  }

  .b_left_bootm {
    box-sizing: border-box; 
    height: 301px;
    width: 330px;
  }

  .b_right_top {
    box-sizing: border-box;
    width: 100%;
    height: 222px;
  }

  .b_right_bootm {
    box-sizing: border-box;
    width: 100%;
    height: 339px;
  }
  .echartOff {
    display: none;
  }
}
</style>
<style lang='scss'>
.el-dialog {
    width:525px;
    margin-top:-5px;
}
.dialogRecharge {
   line-height: 30px;
}
@media screen and (max-width: 414px) {
  .el-dialog {
     width:355px;
     margin-top:-15px;
  } 
  .dialog-footer {
    margin-top:-38px;
  }
  .passwordClass {
    width:200px;
  }
}
</style>