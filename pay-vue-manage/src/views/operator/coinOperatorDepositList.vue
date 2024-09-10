<!-- coinAgentDepositList coinOperatorDepositList -->

<template>
  <div>
 
   <div class="flex flex-m tc topbox searchBox">
      <div class="flex-1 bor">
        <div>代收总交易金额<span style="font-size:12px;">(元)</span></div>
        <div class="num">{{ count.sumCoinPayerAmt || 0.0 }}</div>
      </div> 

      <div class="flex-1">
        <div>总交易笔数</div>
        <div class="num">{{ count.countCoinPayerWithdraw || 0 }}</div>
      </div> 

        <!-- <div class="flex-3">
        <div>挂码情况<span style="font-size:12px;"></span></div>
        <div class="num">
            <div class="num" style="font-size:16px;line-height:170%;">
             300<span style="font-size:13px;" :class="countMap.count300 < 5 ? (countMap.count300 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count300 || 0}}]</span> 
             400<span style="font-size:13px;" :class="countMap.count400 < 5 ? (countMap.count400 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count400 || 0}}]</span> 
             500<span style="font-size:13px;" :class="countMap.count500 < 5 ? (countMap.count500 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count500 || 0}}]</span> 
             600<span style="font-size:13px;" :class="countMap.count600 < 5 ? (countMap.count600 < 1 ? 'gray' : 'red'): 'green'">[{{countMap.count600 || 0}}]</span> 
             700<span style="font-size:13px;" :class="countMap.count700 < 5 ? (countMap.count700 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count700 || 0}}]</span> 
             800<span style="font-size:13px;" :class="countMap.count800 < 5 ? (countMap.count800 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count800 || 0}}]</span> 
             900<span style="font-size:13px;" :class="countMap.count900 < 5 ? (countMap.count900 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count900 || 0}}]</span> 
            1000<span style="font-size:13px;" :class="countMap.count1000 < 5 ? (countMap.count1000 < 1 ? 'gray' : 'red') : 'green'">[{{countMap.count1000 || 0}}]</span> 
          </div>  
        </div>
      </div> -->
 
    </div>
   <!-- * 
         * @搜索栏
         *
    -->
     <div class="flex flex-m mb20 searchBox">

       <div class="flex flex-m mr20">
        <span class="w100">商户订单号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.merchantWithdrawNo"
          clearable
          placeholder="商户订单号"
        ></el-input>
      </div>
         <div class="flex flex-m mr20">
        <span class="w100">平台订单号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.withdrawNo"
          clearable
          placeholder="平台订单号"
        ></el-input>
      </div>
       <div class="flex flex-m mr20">
        <span class="w100">收款人:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerName"
          clearable
          placeholder="收款人"
        ></el-input>
      </div>

       <!-- <div class="flex flex-m mr20">
        <span class="w100">收款人编号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerIdentity"
          clearable
          placeholder="收款人编号"
        ></el-input>
      </div> -->
 
    
       <div class="flex flex-m mr20">
        <span class="w100">三方通道:</span>
        <el-select
          clearable
          class="w180"
          size="medium"
          v-model="search_data.channelCode"
          placeholder="请选择"
        >
          <el-option label="请选择三方代收通道" value></el-option>
          <el-option
            v-for="item in queryCoinChannelInfoList"
            :key="item.channelCode"
            :label="item.channelName"
            :value="item.channelCode"
          ></el-option>
        </el-select>
      </div> 

     <div class="flex flex-m mr20">
        <span class="w100">收款码ID:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerAddr"
          clearable
          placeholder="收款码二维码ID"
        ></el-input>
      </div> 

      <div class="flex flex-m mr20">
        <!-- <span class="w80">订单状态:</span> -->
        <el-select
          class="w150"
          size="medium"
          v-model="search_data.status"
          placeholder="请选择订单状态"
        >
          <el-option label="请选择订单状态" value></el-option>
          <el-option
            v-for="item in payerDepositStatusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div> 

    <!-- <div class="flex flex-m mr20">
         
        <el-select
          class="w120"
          size="medium"
          v-model="search_data.coinType"
          placeholder="收款码类别"
        >
          <el-option label="收款码类别" value></el-option>
          <el-option
            v-for="item in coinTypeList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
    </div> -->

     
      <div>
         <div class="flex flex-m mr20">
        <span class="w60 mr20">日期:</span>
        <el-date-picker
          v-model="time"
          type="daterange"
          align="right"
          unlink-panels
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="margin-left: -10px"
          class="w250"
        ></el-date-picker>
      </div>

      </div>

 <div style="margin-left: 1px" class="flex flex-m mr20">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          :loading="loadingStatus"
          @click.native="m_search"
          type="primary">搜索 {{leftTime}}秒后自动刷新</el-button>
      </div>

     </div>

 
     <el-table
      v-loading="m_tableLoading"
      @selection-change="m_handleSelectionChange"
      @current-change="m_handleCurrentChange"
      class="mtable"
      :stripe="true"
      tooltip-effect="dark"
      :highlight-current-row="true"
      :data="m_list"
    >
      <el-table-column align="center" label="交易时间" width="140">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime | time }}</span>
        </template>
      </el-table-column>
     
      <el-table-column
        align="center"
        prop="merchantWithdrawNo"
        label="商户订单号"
        
      ></el-table-column>
      <el-table-column
        align="center"
        prop="withdrawNo"
        label="平台订单号"
       
      ></el-table-column> 
     

      <el-table-column align="center" prop label="三方通道" width="128">
        <template slot-scope="scope">
          <span>
            {{ scope.row.channelName }} 
          </span>
        </template>
      </el-table-column> 
     
      <el-table-column align="center" prop label="收款人" width="190">
        <template slot-scope="scope">
           {{scope.row.payerName}} 
         <el-button v-clipboard:copy="scope.row.payerName" @click.native="show_copy(scope.row)" type="text"><span style="font-style:italic;font-size:13px;">复制</span></el-button> 
        </template>
      </el-table-column>

     <el-table-column align="center" prop label="收款码ID" width="125">
        <template slot-scope="scope">
         <!-- <img class="authurl" :src="'/uploads/' + scope.row.payerAddr  + '.png'" style="width:50px;" alt /><br> -->
         
          <el-button @click.native="show_receipt(scope.row)" type="text">查看二维码</el-button> <br/>
           <span>{{ scope.row.payerAddr }}</span>
        </template>
      </el-table-column>

       
      <el-table-column align="center" prop label="交易金额" width="88">
        <template slot-scope="scope">
          {{scope.row.amt}} 
        </template>
      </el-table-column>   
  
      <el-table-column align="center" prop="status" label="付款人"  width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.fromAddr}}</span> 
        </template>
      </el-table-column>  

      <el-table-column align="center" prop="status" label="收款状态"  width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.preStatus | preStatusFilter}}</span> 
        </template>
      </el-table-column> 

       <el-table-column
        align="center"
        prop
        label="备注" 
      >
        <template slot-scope="scope">
          <span
            :style="
              scope.row.status == 3 && scope.row.taskType == 2
                ? 'color:#ff0000'
                : ''
            "
            >{{ scope.row.remark }}</span
          >
        </template>
      </el-table-column>


      <el-table-column align="center" prop="status" label="支付状态"  fixed="right"  width="120">
        <template slot-scope="scope">
          <span :style="(scope.row.status == '2' || scope.row.status == '-1') ? 'color:#ff0000' : (scope.row.status == '1' ? 'color:#67C23A' : '')">{{ scope.row.status | depositStatusFilter}}</span>

         
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" width="125" fixed="right">
        <template slot-scope="scope">  
            <div>
              <el-button @click.native="show_detail(scope.row)" type="text"
                >详情</el-button>
            </div>  
           <div>
              <el-button 
                v-if="scope.row.status == '0' || scope.row.status == '-1'"
                 @click.native="handel_withdreawStatus(scope.row,'1')"
                type="success"
                >确认收款</el-button>
            </div> 
             <!-- <div style="margin-top:5px;">
              <el-button 
                v-if="scope.row.status == '0' || scope.row.status == '-1'"
                 @click.native="handel_withdreawStatus(scope.row,'2')"
                type="danger"
                >设为失败</el-button>
            </div>  -->
        </template>
      </el-table-column>
    </el-table>

    <Paging
      class="mt20 mb30"
      :pageIndex="m_page.page"
      :pageSize="m_page.rows"
      :pageTotal="m_page.total"
      @changeSize="m_changesize"
      @changeIndex="m_changeindex"
    ></Paging>


    <el-dialog
      v-if="balance_show"
      title
      :visible.sync="balance_show"
      width="400px"
      style="margin-top: 50px"
    >
      <div v-loading="d_balance_loading">
        <div align="center">{{detail.payerName}}</div>
        <br>
        <img :src="this.receiptImage" width="350" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="balance_show = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 三方代收订单详情 -->
    <el-dialog
      v-if="m_show"
      title="三方代收订单详情"
      :visible.sync="m_show"
      style="margin-top:-50px"
    >
      <div class="dia_box">
        <div>订单状态：<span :class="detail.status | withdrawStatusClass">{{ detail.status | depositStatusFilter }}</span></div>
        <div class="flex flex-m">
          <div class="flex-1">
            交易金额:
            <span class="gray">{{ detail.amt }} 元</span>
          </div> 
        </div>

        <div class="flex flex-m">
          <div class="flex-1">
            应付金额:
            <span class="gray">{{ detail.amtPay }} 元</span>
          </div> 
        </div>

        <div class="flex flex-m">
          <!-- <div class="flex-1">
            入账金额:
            <span class="gray"> {{ detail.amtCredit }} 元</span>
          </div> -->
          <div class="flex-1">
            通道名称：
            <span class="gray">{{ detail.channelName }}</span>
          </div>
        </div>

        <div class="flex flex-m">
          <!-- <div class="flex-1">
            代收手续费：
            <span class="gray">{{ detail.payerRateFee }} 元</span>
          </div> -->
          <div class="flex-1">
            <span class="echartOff">通道编号：</span><span class="gray" style="font-size:13px;">{{ detail.channelCode }}</span>
          </div>
        </div>

        <div class="flex flex-m">
          <!-- <div class="flex-1">
            单笔手续费：
             <span class="gray">{{ detail.payerSingleFee }} 元/笔</span>
           </div>  -->
        </div>

      <div class="flex flex-m">
          <!-- <div class="flex-1">
            优惠立减：
             <span class="gray">{{ detail.amtBonus }} 元/笔</span>
           </div>  -->
        </div> 

        <div class="flex flex-m echartOn">
          <div class="flex-1">
           交易时间：<span class="gray">{{ detail.createTime | time }}</span>
          </div> 
           <!-- <div class="flex-1">
           处理时间：<span class="gray">{{ detail.taskStartTime | time }}</span>
          </div>   -->
           <div class="flex-1">
            完成时间：<span class="gray">{{ detail.taskEndTime | time }}</span>
          </div>  
        </div> 

        <div class="flex flex-m">
          <div class="flex-1">
            平台订单号：
            <span class="gray">{{ detail.withdrawNo }}</span>
          </div>
          <!-- <div class="flex-1">通道编号：<span class=" gray">{{detail.channelCode}}</span></div> -->
          <div class="flex-1 echartOff">
            交易时间：
            <span class="gray">{{ detail.createTime | time }}</span>
          </div>
        </div>

        <div class="flex flex-m">
          <!-- <div class="flex-1">
            上游单号：
            <span class="gray" style="font-size:13px;">{{ detail.channelWithdrawNo }}</span>
          </div> -->
          
          <!-- <div class="flex-1 echartOff">
            处理时间：
            <span class="gray">{{ detail.taskStartTime | time }}</span>
          </div> -->
        </div>

        <div class="flex flex-m">
          <div class="flex-1">
            收款人：
            <span class="gray">{{ detail.payerName }}</span>
          </div>
          
          <div class="flex-1 echartOff">
            完成时间：
            <span class="gray">{{ detail.taskEndTime | time }}</span>
          </div>
        </div>
        <div>
          备注：
          <span class="gray">{{ detail.remark }}</span>
        </div>

        <div>
          收款码：<br>
          <span class="gray">
               <span class="gray">{{ detail.payerAddr }}</span><br>
              <img class="authurl" :src="'/uploads/' + detail.payerAddr + '.png'" style="width:100px;" alt />
          </span>
        </div>  

     
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 确 定 -->
        <el-button type="button"  @click="m_show = false">关闭</el-button> 
      </div>
    </el-dialog> 

      <!-- 审核订单/失败:BEGIN -->
    <el-dialog
      v-if="handel_withdreawStatus_force_dialog"
      :title="forceTitle"
      :visible.sync="handel_withdreawStatus_force_dialog"
      style="margin-top: -100px"
    >
      <div v-loading="loadingStatus">
        <el-form :model="merchant_data" :rules="force_rules" ref="forceForm"> 
           <span>
            <span class="red">警告：请务必核对订单的实际收款状态，再{{forceTitle}}，以免造成损失。</span> 
          </span>

          <el-form-item
            label="平台订单号"
            label-width="130px"
            style="margin-top: 15px; line-height: 80px"
          >
            <span>{{ merchant_data.withdrawNo }}</span>
          </el-form-item>
          <el-form-item label="订单金额" label-width="130px">
            <span>{{ merchant_data.amt }} 元</span>
          </el-form-item>
          <el-form-item label="交易时间" label-width="130px">
            <span>{{ merchant_data.createTime | time }}</span>
          </el-form-item>

         <!-- <el-form-item
            label="google验证码"
            label-width="130px"
            prop="googleCode"
          >
            <el-input
              clearable
              type="number"
              placeholder="请输入"
              v-model="merchant_data.googleCode"
              auto-complete="off" 
            ></el-input>
          </el-form-item> -->
          <el-form-item label="操作员ID" label-width="130px">
            <span>{{
              merchant_data.merchantId.substring(0, 5) +
              "*****" +
              merchant_data.merchantId.substring(
                merchant_data.merchantId.length - 5
              )
            }}</span>
          </el-form-item>
          <el-form-item label="操作时间" label-width="130px">
            <span>{{ merchant_data.nowTime }}</span>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="handel_withdreawStatus_force_dialog = false"
          >关 闭</el-button
        >
        <!-- 确 定 primary -->
        <el-button
            :type="merchant_data.status =='1' ? 'success' : 'danger'"
          :loading="loadingStatus"
          @click.native="handel_withdreawStatus_force_submit(merchant_data)"
          >{{ forceTitle }}</el-button
        >
      </div>
    </el-dialog>
    <!-- 审核订单/失败:END-->


 
  
  </div>
</template>

  
<script>
import moment from "moment"; 
export default { 
  name: "coinOperatorDepositList",
  data() {
    return { 
      leftTime:15,
      refreshTime:"",
      bankCodeDataList: [], //银行列表
      payMerchantInfoList:[],//从商户列表中查出 码商(agentRate == '-1')列表
      time: [],
      search_data: {
        coinType:"",
        withdrawNo: "", //平台提现订单号
        channelWithdrawNo: "", //三方提现订单号
        channelCode: "",
        payerBankCode: "",
        status: "", //订单类型
        payerBankType:"",//自动 -1:手动
        bankName: "", //付款银行
        payerName: "", //付款人
        payerAddr: "", //付款人卡号
        payerMerchantId: [], //卡主 
        acctName: "", //收款人姓名
        acctAddr: "", //钱包地址
        beginTime: "", //开始时间
        endTime: "", //结束时间
        remark: "", //渠道标识
        payerOperatorId:"",
        tableMerchantId:"",
      },

      countMap:{},
      //平台数据
      platform_data: {
        payType:1, //0-付 1-收
        success_loding: false,
        tableMerchantId: "",
        accountBalance: 0,
        beginTime: "",
        endTime: "",
      },

      balance_show: false,
      d_balance_loading: false,
      forceTitle: "",
      handel_withdreawStatus_manual_dialog:false, //人工操作弹出窗口
      handel_withdreawStatus_force_dialog: false, //强制成功、失败的弹出窗口
      handel_withdreawStatus_return_dialog: false, //手动冲正弹出窗口
      loadingStatus: false,
      _merchant_data: {},
      merchant_data: {
        merchantId: "", //商户信息列表中的  商户号 (非登陆后接口返回的)
        googleCode: "",
        withdrawNo: "",
        amt: 0,
        type: 0,
        createTime: "",
        payerBankCode: "",
        merchantWithdrawNo: "",
        status:"",
      },
      sms_loding: false,
      sms_count: 0,
      sms_data: {
        keywords: "无卡,自助,消费,商城,小通,易联,航空,还款,代扣,代付,代收", //关键字以","分隔
      },

      //统计数据
      count: {
        sumCoinPayerAmt:0,
        sumCoinPayerAmtCredit:0,
        countCoinPayerWithdraw: 0,
        sumCoinPayerWithdrawRateFee: 0, 
        sumCoinPayerWithdrawFee: 0,
      },
      queryCoinChannelInfoList: [],
      queryMerchantIdList:[],//卡主列表数组
      detail: {
        status:0,
        remark:"",
        createTime:"",
        taskStartTime:"",

      },
      receiptImage: "",

      force_rules: { 
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },
  created() {
    this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")]; 
    this.init(); 
  },
  mounted() {
    // 绑定enter事件
    this.enterKeyup();
  },
  destroyed() {
    // 销毁enter事件
    this.enterKeyupDestroyed();
    clearInterval(this.refreshTime);
    console.log("clear refreshTime");
  },
  methods: {
    init() {  
      this.m_search();
    }, 

    enterKey(event) {
      const code = event.keyCode;
      //console.log(code);
      if (code == 108 || code == 13 || code == 27) {
        if (
          !this.m_show &&
          !this.handel_withdreawStatus_force_dialog &&
          !this.balance_show
        ) {
          this.m_search();
        }
      }
    },
    enterKeyupDestroyed() {
      document.removeEventListener("keyup", this.enterKey);
    },
    enterKeyup() {
      document.addEventListener("keyup", this.enterKey);
    },
    m_search() {
      this.queryCoinChannelInfo(); 
      // this.queryPayMerchantInfoList();
      this.getDatalist(1);
      this.intervalRefreshTime();
    }, 

 intervalRefreshTime() {
      const that = this;
      clearInterval(this.refreshTime); 
      this.leftTime = 15;
      this.refreshTime = setInterval(() => {
        this.leftTime = this.leftTime - 1;
        if (this.leftTime <= 0) {
          that.leftTime = 15;
          that.getDatalist(); 
        }
        
      }, 1000 * 1);
    },

   //获取码商列表
    queryPayMerchantInfoList() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryPayMerchantInfoList(
        {
          "agentRate":"-1",
          "tableMerchantId":userData.merchantId,
        }
        ).then((res) => {
        this.payMerchantInfoList = res.data;
      });
    }, 

     
    //每20分钟检查同步一次跑量成功交易金额
    intervalQuerySuccessOrder() {
      setInterval(() => {
        this.querySuccessOrder();
      }, 1000 * 60 * 20);
    },

    //获取交易数据信息
    getDatalist(page) {
      const that = this;
      this.m_tableLoading = true;
      this.loadingStatus = true;
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;
      //this.search_data.payerMerchantId = userData.merchantId;//码商
      this.search_data.payerOperatorId = userData.merchantId;//码商操作员
      if (page != null) {
        this.m_page.page = 1;
      }

      let data = Object.assign(this.search_data, this.m_page);

      this.m_api
        .queryCoinPayerDepositPage(data)
        .then((res) => {
          this.count.sumCoinPayerAmt   = res.data.sumCoinPayerAmt || 0;
          this.count.sumCoinPayerAmtCredit   = res.data.sumCoinPayerAmtCredit || 0;
          this.count.countCoinPayerWithdraw = res.data.countCoinPayerWithdraw || 0;

          this.count.sumCoinPayerWithdrawRateFee =  res.data.sumCoinPayerWithdrawRateFee || 0;
          this.count.sumCoinPayerWithdrawFee = res.data.sumCoinPayerWithdrawFee || 0;

          this.m_list = res.data.pages.records;
          this.m_page.total = res.data.pages.total; 
        })
        .finally(() => {
          this.m_tableLoading = false;
          this.loadingStatus = false;
        });

        //总挂码情况
        this.getCoinHolderCountMap(this.search_data);
        this.queryNewOrder();
    },

    //总挂码情况
    getCoinHolderCountMap(data) {
      // let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      // this.search_data.tableMerchantId = userData.merchantId;
      
      this.m_api.getCoinHolderCountMap(data).then((res) => {
        this.countMap = res.data || 0;
      });
    },

    //获取新订单
    queryNewOrder(){
         let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
         if (userData.accountType == 'merchant' && (userData.agentRate == "-3" || userData.agentRate == "-2" || userData.agentRate == "-1")) {
            //console.log("卡主来单提醒");
            this.m_api.queryNewOrder({ 
              tableMerchantId: userData.merchantId, 
            })
            .then((res) => { 
              if (res != null && res !== 'undefined' && res.data.newOrder == "1"){
                console.log("有新订单");
                let audioUrl = "/neworder.mp3"; //public路径
                var audio = new Audio(audioUrl);
                audio.currentTime = 0;
                audio.play();
              } 

            })
            .catch((error) => { 
                 
            }).finally(()=>{
              
            });
         }else {
           //console.log("no music");
          
         }
         
    },


 //获取实时成功交易总金额
    querySuccessOrder() {
      var date = new Date();
      var month = (date.getMonth() + 1).toString();
      var year = date.getFullYear();
      month = month.length == 1 ? "0" + month : month;

      //var beginTime = year + "-" + month + "-" + "01" + " 00:00:00"; //月初时间
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.platform_data.tableMerchantId = userData.merchantId;
      this.platform_data.beginTime = year + "-" + month + "-" + "01" + " 00:00:00"; //月初时间
      this.platform_data.endTime = moment(date).format("YYYY-MM-DD HH:mm:ss"); //当前时间
      this.platform_data.payType = 1;
      console.log(
        this.platform_data.beginTime + " - " + this.platform_data.endTime
      );

      this.m_api.querySuccessOrder(this.platform_data).then((res) => {
        this.platform_data.accountBalance =
          res.data.accountBalance == "" || res.data.accountBalance == null
            ? 0
            : res.data.accountBalance;
        this.platform_data.success_loding = true;
      });
      //
    },


    /**
     * 强制冲正 弹出窗
     */
    handel_withdreawStatus_return_showup(row) {
      this.forceTitle = "强制冲正";
      this.handel_withdreawStatus_return_dialog = true;
      this.loadingStatus = false; //进度条
      let userData =  (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.merchant_data.merchantId = userData.merchantId;
      this.merchant_data.withdrawNo = row.withdrawNo;
      this.merchant_data.amt = row.amt;
      this.merchant_data.createTime = row.createTime;
      this.merchant_data.googleCode = "";
      this.merchant_data.nowTime = moment(new Date()).format(
        "YYYY-MM-DD HH:mm:ss"
      );
    },

  /**
     * 人工订单审核、失败 弹出窗
     */
    handel_withdreawStatus_manual_showup(row, type) {
      //console.log("type:" + type);
      this.forceTitle = type == 1 ? "人工订单审核" : "人工设为失败";
      this.handel_withdreawStatus_manual_dialog = true;
      this.loadingStatus = false; //进度条
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.merchant_data.merchantId = userData.merchantId;
      this.merchant_data.withdrawNo = row.withdrawNo;
      this.merchant_data.amt = row.amt;
      this.merchant_data.type = type;
      this.merchant_data.createTime = row.createTime;
      this.merchant_data.googleCode = "";
      this.merchant_data.payerBankCode = row.payerBankCode;
      this.merchant_data.nowTime = moment(new Date()).format(
        "YYYY-MM-DD HH:mm:ss"
      );
    },
 
     handel_withdreawStatus(row,status) {
      this.handel_withdreawStatus_force_dialog = true; 
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      
      this.merchant_data.withdrawNo = row.withdrawNo;
      this.forceTitle = status == '1' ? "确认收款" : '设为失败'
      this.merchant_data.amt = row.amt;
      this.merchant_data.status = status;
      this.merchant_data.createTime = row.createTime;
      this.merchant_data.merchantId =userData.merchantId;
      this.merchant_data.nowTime = moment(new Date()).format(
        "YYYY-MM-DD HH:mm:ss"
      ); 
      this.merchant_data.googleCode = "";
    },

    handel_withdreawStatus_force_submit(data){ 
       this.m_loading = true;
       this.loadingStatus = true;
       let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
        if (data.status == '1') {

           this.m_api
          .setDepositStatusSuccess({
            googleCode:data.googleCode,
            withdrawNo: data.withdrawNo,
            tableMerchantId: userData.merchantId,
          })
          .then((response) => {
            this.m_tableLoading = false;
            if (response.code == '0000') {
               this.m_tableLoading = true;
              this.m_success("[" + response.data.withdrawNo + "]已操作完成");
              this.handel_withdreawStatus_force_dialog = false;
              this.getDatalist();
            } else { 
              this.m_error(response);
            }
          
          })
          .catch((error) => {
            this.m_tableLoading = false; 
          }).finally(()=>{ 
            this.m_loading = false;
            this.loadingStatus = false;
          }); 

        }else if (data.status == '2'){

            this.m_api.setDepositStatusFail({
            googleCode:data.googleCode,
            withdrawNo: data.withdrawNo,
            tableMerchantId: userData.merchantId,
          })
          .then((response) => {
            this.m_tableLoading = false;
            if (response.code == '0000') {
               this.m_tableLoading = true;
              this.m_success("[" + response.data.withdrawNo + "]已操作完成");
              this.handel_withdreawStatus_force_dialog = false;
              this.getDatalist();
            } else { 
              this.m_error(response);
            }
          
          })
          .catch((error) => {
            this.m_tableLoading = false; 
          }).finally(()=>{ 
            this.m_loading = false;
            this.loadingStatus = false;
          }); 

        }
       

    },



    //copy
     show_copy(row){
     this.m_success("[" + row.payerName + "]已复制");
    },

    //凭证
    show_receipt(row) {
      this.detail.payerName = row.payerName;
      this.balance_show = true;
      this.receiptImage = '/uploads/' + row.payerAddr  + '.png';
      // this.receiptImage = "https://30pay.info/uploads/20240112104722905097b12af24698a67b7776ed861ef1.png";
    },

    show_detail(row) {
      this.detail = row;
      this.m_show = true;
    },
  
 


    //获取三方代收通道
    queryCoinChannelInfo() {
      this.m_api.queryCoinChannelDepositInfo({}).then((res) => {
        this.queryCoinChannelInfoList = res.data;
      });
    },


    handel_upload_excel() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;
      let url = `${
        this.m_host
      }/fileDownload/coinPayerWithdrawListExportExcel?${this.m_getQuery(
        this.search_data
      )}`;
      console.log(url);
      this.upload_excel(url);
    }, 
 
    copyto(v){
      this.$message({message: v + " 已复制到剪贴板",type: 'warning'});
    }, 

  },
};

document.body.onkeydown = function (event) {
  var e = window.event || event;

  if (e.preventDefault && event.keyCode == 32) {
    e.preventDefault();
  } else {
    window.event.returnValue = true;
  }
};
</script>
<style lang='scss' scoped>
.topbox {
  height: 80px;
  border: 1px solid #999;
  border-radius: 5px;
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.447058823529412);
  width: 100%;
}
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
}

.num {
  color: rgba(0, 0, 0, 0.847058823529412);
  font-size: 24px;
}

.bor {
  border-right: 1px solid #999;
}

.dia_box {
  font-size: 14px;
  line-height: 28px;
}

.font_gray {
  font-size: 14px;
  color: #999;
}



@media screen and (max-width: 2155px) {
.echartOn {
    display:none;
}

.buttonClass {
    width:60px;
    height:30px;
    margin-left:5px;
    vertical-align: top;
    padding: 5px 2px -5px 5px;
    font-size:14px;
    text-align: left;
    display:none;
}

} 

@media screen and (max-width: 414px) {
  .echartOff {
    display:none;
  }
  .echartOn {
    display:inline-block;
    flex-direction:column;
  }
  .buttonClass {
    width:60px;
    height:30px;
    margin-left:5px;
    vertical-align: top;
    padding: 5px 2px -5px 5px;
    font-size:14px;
    text-align: left;
    display:inline;
  }
} 
</style>

<style lang='scss'>
.el-dialog {
    width:625px;
    margin-top:-5px;
}
@media screen and (max-width: 414px) {
  .el-dialog {
     width:355px;
     margin-top:-15px;
  } 
  .dialog-footer {
    margin-top:-38px;
  }
}
</style>

