<!-- coinAgentWithdrawList 三方代付订单列表 -->

<template>
  <div>
    <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>总交易金额</div>
        <div class="num">{{ count.sumCardPayerWithdraw || 0.0 }}</div>
      </div>

      <div class="flex-1">
        <div>总交易笔数</div>
        <div class="num">{{ count.countCardPayerWithdraw || 0 }}</div>
      </div>
    </div> 

    <!-- * 
         * @搜索栏
         *
    --> 

    <div class="flex flex-m mb20 searchBox"> 

   <div class="flex flex-m mr20">
        <span class="w100">银行名称:</span>
        <el-select
          class="w150"
          size="medium"
          v-model="search_data.coinType"
          placeholder="请选择银行"
        >
          <el-option label="请选择银行" value></el-option>
          <el-option
            v-for="item in bankCodeDataList"
            :key="item.bankCode"
            :label="item.bankName + '(' + bankTypeFilter(item.bankType) + ')'"
            :value="item.bankCode"
          ></el-option>
        </el-select>
   </div>

      <div class="flex flex-m mr10">
        <span class="w100">商户订单号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.merchantWithdrawNo"
          clearable
          placeholder="商户订单号"
        ></el-input>
      </div>

      <div class="flex flex-m mr10">
        <span class="w100">平台订单号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.withdrawNo"
          clearable
          placeholder="平台订单号"
        ></el-input>
      </div>

      <div class="flex flex-m mr10">
        <span class="w109">通道流水号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.channelWithdrawNo"
          clearable
          placeholder="通道流水号"
        ></el-input>
      </div> 
    </div> 

    <div class="flex flex-m mb20 searchBox">  
      <div class="flex flex-m mr20 ">
        <span class="w60">付款人:</span>
        <el-input
          size="medium"
          class="w110"
          v-model="search_data.payerName"
          clearable
          placeholder="付款人"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w60">钱包地址:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerAddr"
          clearable
          placeholder="钱包地址"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w60">收款人:</span>
        <el-input
          size="medium"
          class="w110"
          v-model="search_data.acctName"
          clearable
          placeholder="收款人"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w60">钱包地址:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.acctAddr"
          clearable
          placeholder="收款人卡号"
        ></el-input>
      </div>
    </div>

    <div class="flex flex-m mb20 searchBox">
      <div>
        <span class="w201">选择日期:</span>
        <el-date-picker
          v-model="time"
          type="daterange"
          align="right"
          unlink-panels
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd" 
          class="w260"
        ></el-date-picker>
      </div> 

      <div class="flex flex-m mr20">
        <span class="w70">订单状态:</span>
        <el-select
          class="w120"
          size="medium"
          v-model="search_data.status"
          placeholder="请选择状态"
        >
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in payerWithdrawStatusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>

      <div class="flex flex-m mr20">
        <span class="w70">备 注:</span>
        <el-input
          size="medium"
          class="w120"
          v-model="search_data.remark"
          clearable
          placeholder="备注"
        ></el-input>
      </div>

      <div  class="flex flex-m mr20">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          :loading="loadingStatus"
          @click.native="m_search"
          type="primary"
          >搜索 (ENTER回车键或ESC键)</el-button
        >
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
      <el-table-column align="center" label="交易时间" width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime | time }}</span>
        </template>
      </el-table-column>
    
      <el-table-column
        align="center"
        prop="merchantWithdrawNo"
        label="商户订单号"
        width="99"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="withdrawNo"
        label="平台订单号"
         width="99"
      ></el-table-column> 
       
      <el-table-column align="center" prop label="持卡人">
        <template slot-scope="scope">
          <span>
            {{ scope.row.payerName }}
            <br />
          </span>
          <span>{{ scope.row.payerAddr }}<br /></span>
          <span style="color: #999;font-style: italic;">{{
            scope.row.coinType | coinTypeFilter
          }}</span>
        </template>
      </el-table-column>
      <!-- <el-table-column align="center" prop="" label="卡号" width="95"></el-table-column> -->
      <el-table-column align="center" prop label="交易金额" width="78">
        <template slot-scope="scope">
          <span :class="scope.row.status | withdrawStatusClass">{{
            scope.row.amt
          }}</span>
        </template>
      </el-table-column>

      
 
      <el-table-column align="center" prop label="收款人">
        <template slot-scope="scope">
          <span>
            {{ scope.row.acctName }}
            <br />
          </span>
          <span>{{ scope.row.acctAddr }} <br /></span>
          <span style="color: #999;font-style: italic;">{{ scope.row.receiverBank }} <br /></span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop label="三方通道" width="115">
        <template slot-scope="scope">
          <span>
            {{ scope.row.channelName }}
            <br />
            {{ scope.row.channelCode }}
          </span>
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        prop
        label="备注"
        v-if="this.platform_data.accountBalance > -5000"
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

      

      <el-table-column
        align="center"
        prop="withdrawStatus"
        label="提现状态"
        fixed="right"
        width="90"
        v-if="this.platform_data.accountBalance > -3000"
      >
        <template slot-scope="scope">
          <span :style="scope.row.status == 2 ? 'color:#ff0000' : ''">{{
            scope.row.status | payerWithdrawStatusListFilter
          }}</span> 
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        label="操作"
        fixed="right"
        v-if="this.platform_data.accountBalance > -1"
      >
        <template slot-scope="scope"> 

          <div v-if="scope.row.payerBankType == -1">
            <!-- 卡主[{{ scope.row.payerMerchantId }}]管理 <br /> -->
            <div>
              <el-button @click.native="show_detail(scope.row)" type="text"
                >详情</el-button>
            </div> 
            <div>
              <el-button
                style="color: #ff0000"
                v-if="scope.row.status == 0 || scope.row.status == 3 || scope.row.status == -1"
                @click.native="handel_withdreawStatus_manual_showup(scope.row, 2)"
                type="text"
                >人工失败</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #67c23a"
                v-if="scope.row.status == 0 || scope.row.status == 3 || scope.row.status == -1"
                @click.native="handel_withdreawStatus_success(scope.row)"
                type="text"
                >人工成功</el-button
              >
            </div>
          </div>
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

    <!-- 三方订单详情 -->
    <el-dialog
      v-if="m_show"
      title="订单详情"
      :visible.sync="m_show"
      style="margin-top:-100px"
    >
      <div class="dia_box">
        <div class>订单状态：{{ detail.status | withdrawStatusFilter }}</div>
        <!-- <div class><el-button type="primary" class="buttonClass"  v-if="detail.status != 1 && detail.status != 2" v-clipboard:copy="detail.amt">复制</el-button></div> -->
        <div class="flex flex-m">
          <div class="flex-1">
           通道名称：
            <span class="gray">{{ detail.channelName }}</span>
          
            <span class="gray" style="margin-left:8px;">{{ detail.channelCode }}</span>
          </div>
        </div> 
       
        <div class="flex flex-m echartOn">
          <div class="flex-1">
           交易时间：<span class="gray">{{ detail.createTime | time }}</span>
          </div> 
           <div class="flex-1">
           处理时间：<span class="gray">{{ detail.taskStartTime | time }}</span>
          </div>  
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
          <div class="flex-1">
            银行卡流水号：
            <span class="gray" style="font-size:13px;">{{ detail.channelWithdrawNo }}</span>
          </div>
          <!-- <div class="flex-1">通道名称：<span class=" gray">{{detail.channelName}}</span></div> -->
          <div class="flex-1 echartOff">
            处理时间：
            <span class="gray">{{ detail.taskStartTime | time }}</span>
          </div>
        </div>
         <div class="flex-1 echartOn">
            持卡人开户行：
            <span class="gray">{{ detail.coinType | coinTypeFilter }}</span>
          </div>
        <div class="flex flex-m">
           
          <div class="flex-1">
            持卡人：
            <span class="gray">{{ detail.payerName }}</span>
          </div> 
          <div class="flex-1 echartOff">
            完成时间：
            <span class="gray">{{ detail.taskEndTime | time }}</span>
          </div>
        </div>
        <div>
          卡号：
          <span class="gray">{{ detail.payerAddr }}</span>
        </div> 

         <div>
          收款人开户行：
          <span class="gray">{{ detail.receiverBank }}</span>
            <el-button type="primary" class="buttonClass" v-if="detail.status != 1 && detail.status != 2" v-clipboard:copy="detail.receiverBank">复制</el-button>
        
        </div>

        <div>
          收款人：
          <span class="gray">{{ detail.acctName }}</span>
          <el-button type="primary" class="buttonClass" v-if="detail.status != 1 && detail.status != 2"  v-clipboard:copy="detail.acctName" >复制</el-button>

        </div>
        <div>
          收款人卡号：
          <span class="gray">{{ detail.acctAddr }}</span>
             <el-button type="primary" class="buttonClass"  v-if="detail.status != 1 && detail.status != 2" v-clipboard:copy="detail.acctAddr">复制</el-button>
        
        </div>

        <div>
          订单金额：
          <span class="gray">{{ detail.amt }}</span>
             <el-button type="primary" class="buttonClass"  v-if="detail.status != 1 && detail.status != 2" v-clipboard:copy="detail.amt">复制</el-button> 
        </div>
        

        <div>
          备注：
          <span class="gray">{{ detail.remark }}</span>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 确 定 -->
        <el-button type="button" @click="m_show = false">关闭</el-button>
        <!-- <el-button type="button" v-if="detail.status == 0"  @click="handel_withdreawStatus_process(detail)">去处理</el-button> -->
        <el-button type="primary" v-if="detail.status != 1 && detail.status !=2"  @click="handel_withdreawStatus_success(detail)">人工设为成功</el-button>
      
      </div>
    </el-dialog>


    <!-- 人工成功/失败:BEGIN -->
    <el-dialog
      v-if="handel_withdreawStatus_manual_dialog"
      :title="forceTitle"
      :visible.sync="handel_withdreawStatus_manual_dialog"
      style="margin-top: -100px"
    >
      <div v-loading="loadingStatus">
        <el-form :model="merchant_data" :rules="force_rules" ref="forceForm">
          <span>
            <span class="red">警告:</span>鉴于{{
              forceTitle
            }}乃人工操作，请核对是否已打款，再修改订单状态。
            <br />
          </span>

          <el-form-item
            label="平台订单号"
            label-width="130px"
            style="margin-top: 15px; line-height: 80px"
          >
            <span>{{ merchant_data.withdrawNo }}</span>
          </el-form-item>
          <el-form-item label="订单金额" label-width="130px">
            <span>{{ merchant_data.amt }}</span>
          </el-form-item>
          <el-form-item label="交易时间" label-width="130px">
            <span>{{ merchant_data.createTime | time }}</span>
          </el-form-item>

          <el-form-item
            label="google验证码"
            label-width="130px"
            prop="googleCode"
          >
            <el-input
              type="number"
              placeholder="请输入"
              v-model="merchant_data.googleCode"
              auto-complete="off"
              class="w190"
            ></el-input>
          </el-form-item>
          <el-form-item label="卡主" label-width="130px">
            <span>{{merchant_data.merchantName}} ({{
              merchant_data.merchantId.substring(0, 5) +
              "*****" +
              merchant_data.merchantId.substring(
                merchant_data.merchantId.length - 5
              )
            }})</span>
          </el-form-item>
          <el-form-item label="操作时间" label-width="130px">
            <span>{{ merchant_data.nowTime }}</span>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="handel_withdreawStatus_manual_dialog = false"
          >关 闭</el-button
        >
        <!-- 确 定 primary -->
        <el-button
          :type="merchant_data.type == 1 ? 'primary' : 'danger'"
          :loading="loadingStatus"
          @click.native="handel_withdreawStatus_manual(merchant_data)"
          >{{ forceTitle }}</el-button
        >
      </div>
    </el-dialog>
    <!-- 人工成功/失败:END--> 
  </div>
</template>

  
<script>
import moment from "moment";
export default {
  name: "coinAgentWithdrawList",
  data() {
    return {
      bankCodeDataList: [], //银行列表
      time: [],
      search_data: {
        withdrawNo: "", //平台提现订单号
        channelWithdrawNo: "", //三方提现订单号
        channelCode: "",
        coinType: "",
        withdrawStatus: "", //订单类型
        payerBankType:"-1",//自动 -1:手动
        bankName: "", //付款银行
        payerName: "", //付款人
        payerMerchantId: "", //卡主
        payerAddr: "", //付款人卡号
        acctName: "", //收款人姓名
        acctAddr: "", //收款人卡号
        beginTime: "", //开始时间
        endTime: "", //结束时间
        remark: "", //渠道标识
        tableMerchantId: "",
        merchantId:"",
      },

      //平台数据
      platform_data: {
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
        coinType: "",
        merchantWithdrawNo: "",
        merchantName:"",//卡主名称
      }, 
      //统计数据
      count: {
        sumCardPayerWithdraw: 0,
        countCardPayerWithdraw: 0,
        sumCardPayerWithdrawRateFee: 0,
        sumCardPayerWithdrawFee: 0,
      },
      queryCardChannelInfoList: [],
      queryMerchantIdList:[],//卡主列表数组
      detail: {
        taskStartTime:"",
        taskEndTime:"",
      },
      receiptImage: "",

      force_rules: {
        //apiKey: [{ required: true, message: "请输入", trigger: "blur" }],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },
  created() {
    this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")];
    //console.log(this.time)
    this.init();
  },
  mounted() {
    // 绑定enter事件
    this.enterKeyup();
  },
  destroyed() {
    // 销毁enter事件
    this.enterKeyupDestroyed();
  },
  methods: {
    init() {
       this.getDatalist();
      this.getBankCodeListFromServer();
    },

    enterKey(event) {
      const code = event.keyCode;
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
      this.getDatalist(1);
      this.getBankCodeListFromServer();
    },
 

    //获取交易数据信息
    getDatalist(page) {
      this.m_tableLoading = true;
      this.loadingStatus = true;
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.merchantId = userData.merchantId;
      this.search_data.payerMerchantId = userData.merchantId;
      if (page != null) {
        this.m_page.page = 1;
      }

      let data = Object.assign(this.search_data, this.m_page);

      this.m_api.queryCardAgentWithdrawList(data)
        .then((res) => {
          this.count.sumCardPayerWithdraw = res.data.sumCardPayerWithdraw || 0;
          this.count.countCardPayerWithdraw = res.data.countCardPayerWithdraw || 0;  
          this.m_list = res.data.pages.records;
          this.m_page.total = res.data.pages.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
          this.loadingStatus = false;
        });
    },

 

  /**
     * 人工设为成功、失败 弹出窗
     */
    handel_withdreawStatus_manual_showup(row, type) {
      //console.log("type:" + type);
      this.forceTitle = type == 1 ? "人工设为成功" : "人工设为失败";
      this.handel_withdreawStatus_manual_dialog = true;
      this.loadingStatus = false; //进度条
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.merchant_data.merchantId = userData.merchantId;
      this.merchant_data.withdrawNo = row.withdrawNo;
      this.merchant_data.amt = row.amt;
      this.merchant_data.type = type;
      this.merchant_data.createTime = row.createTime;
      this.merchant_data.googleCode = "";
      this.merchant_data.coinType = row.coinType;
      this.merchant_data.merchantName = row.merchantName;
      this.merchant_data.nowTime = moment(new Date()).format(
        "YYYY-MM-DD HH:mm:ss"
      );
    },
   
    /**
     * 人工设为成功、失败
     */
    handel_withdreawStatus_manual(row) {
       this.$refs["forceForm"].validate((valid) => {
        if (valid) {
          this.loadingStatus = true;
          //(JSON.stringify(row));
          if (row.type == 1) {
            this.m_api
              .setWithdrawStatusByManual({
                googleCode: row.googleCode,
                withdrawNo: row.withdrawNo,
                merchantId: row.merchantId,
                withdrawStatus:1,//1成功
              })
              .then((response) => { 
                this.m_success( "[" + response.data.withdrawNo + "]已人工设为成功");  
              })
              .catch((error) => { 
                this.error( "订单状态修改有误" ); 
              }).finally(()=>{
                this.handel_withdreawStatus_manual_dialog = false;
                this.m_tableLoading = false;
                this.loadingStatus = false; 
                this.getDatalist();
              });
          } else if (row.type == 2) {
            this.m_api.setWithdrawStatusByManual({
                googleCode: row.googleCode,
                withdrawNo: row.withdrawNo,
                merchantId: row.merchantId,
                withdrawStatus:2,//2-失败
              })
              .then((response) => { 
                  this.m_success( "[" + response.data.withdrawNo + "]已人工设为失败,交易金额[" + response.data.amt + "]已退回" ); 
              })
              .catch((error) => { 
                this.error( "订单状态修改有误" ); 
              }).finally(()=>{
                 this.handel_withdreawStatus_manual_dialog = false;
                 this.loadingStatus = false;
                  this.m_tableLoading = false;
                 this.getDatalist();
              });
          }
        }
      });
    },
 
  
   handel_withdreawStatus_process(row){
      let msg = { msg: `是否去处理订单[${row.withdrawNo}]` };
      this.m_confirm(msg).then((res)=>{
        let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
        //如果点击确认键
        if (res) {
          this.m_api.setWithdrawStatusProcess({
              withdrawNo: row.withdrawNo,
              merchantId: userData.merchantId,
              withdrawStatus:3,//3 处理中
            })
            .then((res) => { 
              if (res != null && res !== 'undefined'){
                this.m_success("[" + res.data.withdrawNo + "]已为[处理中]状态,请复制收款人信息并打款"); 
                this.detail.status = res.data.status;
                this.detail.remark = "处理中";
                this.detail.taskStartTime = res.data.taskStartTime;
              } 
            })
            .catch((error) => { 
                this.m_error(error);
               //this.m_success("[" + row.withdrawNo + "]已为[处理中]状态,请复制收款人信息并打款"); 
            }).finally(()=>{
              this.getDatalist();
              //this.detail.status = 3;
              //this.detail.remark = "处理中" 
            });
        } 
        
      });
    },

  /**
     * TODO: 设为成功
     * */
    handel_withdreawStatus_success(row) {
      
      let msg = { msg: `请打款后,再把订单[${row.withdrawNo}]设为成功` };
      var isConfirm = this.m_confirm(msg).then((res) => {
        let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
          // console.log(userData.merchantName);
          // return;
        //如果点击确认键
        if (res) {
          this.m_tableLoading = true;
          this.m_api
            .setWithdrawStatusSuccessByManual({
              withdrawNo: row.withdrawNo,
              merchantId: userData.merchantId,
              withdrawStatus:1,//1-成功
            })
            .then((res) => {
              if (res != null && res !== 'undefined'){
                this.m_success("[" + row.withdrawNo + "]已为[成功]状态"); 
                this.detail.status = res.data.status;
                this.detail.remark = res.data.remark;
                this.detail.taskEndTime = res.data.taskEndTime;
              } 
             
            })
            .catch((error) => {
             
            }).finally(()=>{
             this.m_tableLoading = false;
              this.getDatalist();
            });
        }
      });
    },


    show_detail(row) {
      this.detail = row;
      this.m_show = true;
    },

    
    
    //获取银行列表
    getBankCodeListFromServer() {
      this.m_api.querybankCodeDataList({
        payerBankType:-1,
      }).then((res) => {
        this.bankCodeDataList = res.data;
      });
    }, 
    

    bankTypeFilter(v) {
      if (v == "0") {
        return "自动";
      } else if (v == "-1") {
        return "人工";
      }
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
  width:100%;
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
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
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