<!-- 商户自助充值订单  card merchantSelfRechargeList -->
<template>
  <div>
    <!-- <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>总可提现余额</div>
        <div class="num">{{ countMap.sumPayerAmt || 0.0 }}</div>
      </div>

      <div class="flex-1">
        <div>单钱包单笔最高可提现</div>
        <div class="num">{{ highestAmt || 0.0 }}</div>
      </div>
    </div> -->

    <!-- * 
         * @搜索栏
         *
    -->
    <div class="flex flex-m mb20 searchBox">

      <div class="flex flex-m mr20">
        <span class="w60">订单号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.withdrawNo"
          clearable
          placeholder="订单号"
        ></el-input>
      </div>

      
      <div class="flex flex-m mr20">
        <span class="w60">持卡人:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerName"
          clearable
          placeholder="持卡人"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w60">卡号:</span>
        <el-input
          size="medium"
          class="w200"
          v-model="search_data.payerCardNo"
          clearable
          placeholder="卡号"
        ></el-input>
      </div>

     <div class="flex flex-m mr20">
        <span class="w60">日期:</span>
            <el-date-picker
                    v-model="time"
                    type="daterange"
                    align="right"
                    unlink-panels
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format='yyyy-MM-dd'
                    class="w250"
            >
            </el-date-picker>
      </div>  

      <!-- <div class="flex flex-m mr20">
        <span class="w60">卡状态:</span>
        <el-select
          class="w160"
          size="medium"
          v-model="search_data.payerCardStatus"
          placeholder="请选择"
        >
          <el-option label="请选择银行卡状态" value></el-option>
          <el-option
            v-for="item in payerCardStatusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div> -->

      <div class="flex flex-m mr20">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          @click.native="m_search"
          type="primary"
          >搜索</el-button
        >
      </div>
    </div>

    <!-- * 
         * @列表
         *
    -->
    <el-table
      :data="m_list"
      v-loading="m_tableLoading"
      @selection-change="m_handleSelectionChange"
      @current-change="m_handleCurrentChange"
      class="mtable"
      :stripe="true"
      tooltip-effect="dark"
      :highlight-current-row="true"
    >
<el-table-column align="center" prop="createTime" label="交易时间" width="90"></el-table-column>
      <el-table-column align="center" prop="withdrawNo" label="订单号"></el-table-column>
        <el-table-column align="center" prop="amt" label="充值金额"></el-table-column>
      <el-table-column
        align="center"
        prop="bankName"
        label="银行名称"
        width="w128"
      >
        <template slot-scope="scope">
          <span>{{ scope.row.bankName }}</span > 
        </template>
      </el-table-column> 

       
      <el-table-column
        align="center"
        prop="payerName"
        label="持卡人"
        width="128"
      >
        <template slot-scope="scope">
          <span>{{ scope.row.payerName }}</span
          ><br />
          <span
            style="font-size: 12px; font-style: italic"
            :class="scope.row.payerOnline | payerOnlineStatusClass"
            v-if="
              scope.row.payerBankType == -1 && scope.row.payerCardStatus == 2
            "
            >{{ scope.row.payerOnline | payerOnlineFilter }}</span
          ><br />
           
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        prop="payerCardNo"
        label="卡号"
        width="155"
      >
        <template slot-scope="scope">
          <span>{{ scope.row.payerCardNo }}</span
          >
           
        </template>
      </el-table-column> 

      <el-table-column
        align="center"
        prop="cardChannelCode"
        label="通道编号"
        width="w138"
      >
        <template slot-scope="scope">
          <span style="height: 295px"
            >{{ scope.row.cardChannelName }}<br />{{
              scope.row.cardChannelCode
            }}</span
          >
        </template>
      </el-table-column>

      <el-table-column align="center" prop="remark" label="备注"></el-table-column>

      <!--  状态-->
      <el-table-column align="center" label="订单状态" width="80" fixed="right">
        <template slot-scope="scope">  
          <span :class="scope.row.status | rechargeStatusClass">{{
            scope.row.status | rechargeStatusFilter
          }}</span>
        </template>
      </el-table-column>

         <el-table-column align="center" label="操作" width="80" fixed="right">
        <template slot-scope="scope">  
          <div>
              <el-button @click.native="show_detail(scope.row)" type="text"
                >详情</el-button>
            </div> 

            <div>
              <el-button
                style="color: #67c23a"
                v-if="scope.row.status == 0"
                @click.native="handel_recharge_status(scope.row,'1')"
                type="text"
                >确认充值</el-button
              >
            </div>

            <div>
              <el-button
                style="color: #ff0000"
                v-if="scope.row.status == 0"
                @click.native="handel_recharge_status(scope.row, '-1')"
                type="text"
                >驳回</el-button
              >
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

   <!-- 代付钱包自助充值详情 -->
    <el-dialog
      v-if="m_show"
      title="代付钱包自助充值详情"
      :visible.sync="m_show"
      style="margin-top: -10px"
    >
      <div class="dia_box">
        <div class="flex flex-m">
          <div class="flex-1">
            通道名称：
            <span class="gray">{{ detail.cardChannelName }}</span>

            <span class="gray" style="margin-left: 8px">{{
              detail.cardChannelCode
            }}</span>
          </div>
        </div>

        <div class="flex-1">
          卡状态：
          <span :class="detail.payerCardStatus | payerCardStatusClass">{{
            detail.payerCardStatus | payerCardStatusFilter
          }}</span>
          <span
            style="font-size: 12px; font-style: italic; margin-left: 5px"
            :class="detail.payerOnline | payerOnlineStatusClass"
            v-if="detail.payerBankType == -1 && detail.payerCardStatus == 2"
            >({{ detail.payerOnline | payerOnlineFilter }})</span
          >
        </div>

        <div class="flex-1">
          开户行所在地：
          <span class="gray">{{ detail.bankArea }}</span>
        </div>

        <div class="flex-1">
          开户行：
          <span class="gray">{{ detail.bankCode | payerBankCodeFilter }}</span>
           
        </div>
        <div class="flex flex-m">
          <div class="flex-1" style="margin-top: 5px">
            持卡人：
            <span class="gray">{{ detail.payerName }}</span>
             
          </div>
        </div>
        <div style="margin-top: 5px">
          卡号：
          <span class="gray">{{ detail.payerCardNo }}</span> 
        </div> 
         

        <div style="margin-top: 5px">
          订单号码：
          <span class="gray">{{ detail.withdrawNo }}</span> 
        </div> 

        <div style="margin-top: 5px">
          充值金额：
          <span class="gray">{{ detail.amt }}</span> 
        </div> 

        <div style="margin-top: 5px">
          订单状态：
          <span :class="detail.status | rechargeStatusClass">{{
            detail.status | rechargeStatusFilter
          }}</span>
        </div> 
        
        
        <div style="margin-top: 5px">
          交易时间：
          <span class="gray">{{ detail.createTime }}</span> 
        </div> 

       
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 确 定 -->
        <el-button type="button" @click="m_show = false">关闭</el-button>
        <el-button type="primary" v-if="detail.status == 0" @click.native="handel_recharge_status(detail,'1')">确认充值</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script> 
import moment from "moment"; 
export default {
  name: "cardMerchantSelfRechargeList",
  data() {
    return {
      time: [],
      bankCodeDataList: [], //银行列表
      cardChannelCodeList: [], //通道列表

      search_data: {
        withdrawNo:"",
        cardChannelCode: "", //通道编号 
        payerName: "", 
        payerCardNo: "", // 手机号 
        remark: "", 
        merchantId: "",
      },
      highestAmt: 0,
      //预先 加载 渲染
      countMap: {
        sumPayerAmt: 0.0,
        countProcessingNum: 0,
      }, 

      balance_show: false,
      d_balance_loading: false,
      balance_loading: false,

      _balance_data: {},

      balance_data: {
        merchantId: "", //管理员id
        cardChannelCode: "",
        cardChannelName: "",
        bankCode: "",
        bankName: "",
        remark: "", //备注
        chlRate: "", //费率
        chlSingle: "", //单下发手续费
        googleCode: "", //google验证码
      },
      m_show_recharge: false,
      merchant_show: false,
      agent_show: false, //代理费率
      merchant_loding: false,
      d_merchant_loding: false,
      _channel_data: {},

      channel_data: {
        chlContact: "", //联系人
        chlName: "", //通道名称
        cardChannelCode: "", //商户信息列表中的  商户号 (非登陆后接口返回的)
        channelCode: "", //通道编号
      },
      detail: {
        amt: "", //充值金额
      },
      m_rules: {
        amt: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },
  created() {
     this.time = [moment().format('YYYY-MM-DD'),moment().format('YYYY-MM-DD')]
     this.init()
  },
  methods: {
    init() { 
      this.getDatalist(); //获取通道列表数据信息
    }, 

    //获取通道列表数据信息
    getDatalist() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || ""; 
      this.m_tableLoading = true;
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryPayMerchantSelfRechargePage(data)
        .then((res) => {
          this.m_list = res.data.pages.records; //记录
          this.m_page.total = res.data.pages.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
          this.m_checkData = [];
        }); 
      
    }, 

      /**
     * TODO: 设为成功
     * */
    handel_recharge_status(row,type) {
      var msg = "";
      if (type == '1'){
        msg=  { msg: `确认充值[${row.withdrawNo}]?` };
      }else {
        msg = { msg: `驳回[${row.withdrawNo}]?` };
      }
      
      var isConfirm = this.m_confirm(msg).then((res) => {
        let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
          // console.log(userData.merchantName);
          // return;
        //如果点击确认键
        if (res) {
          this.m_tableLoading = true;
          this.m_api.setMerchantSelfRechargeStatus({
              withdrawNo: row.withdrawNo,
              tableMerchantId: userData.merchantId,
              status:type,
            })
            .then((res) => {
              if (res != null && res !== 'undefined'){
                var txt = res.data.isMerchantRecharge == 1 ? "[" + row.withdrawNo + "]已确认充值" : "已驳回[" + row.withdrawNo + "]"
                if (res.data.isMerchantRecharge == 1){
                    this.m_success(txt);
                }else {
                    this.m_error(txt);
                } 
              } 
             
            })
            .catch((error) => {
                 this.m_error(error);
            }).finally(()=>{
              this.m_tableLoading = false;
               this.m_show = false;
              this.getDatalist();
            });
        }
      });
    },


    show_detail(row) {
      this.detail = row;
      this.m_show = true;
    },

  
  },
};
</script>

<style lang='scss' scoped>
.span {
  width: 120px;
}
.topbox {
  height: 80px;
  border: 1px solid #999;
  border-radius: 5px;
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.447058823529412);
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
.searchBox {
  flex-wrap: wrap;
  flex-flow: flex-wrap;
}
@media screen and (max-width: 2155px) {
  .echartOn {
    display: none;
  }

  .buttonClass {
    width: 60px;
    height: 30px;
    margin-left: 5px;
    vertical-align: top;
    padding: 5px 2px -5px 5px;
    font-size: 14px;
    text-align: left;
    display: none;
  }

  .buttonShowClass {
    width: 60px;
    height: 30px;
    margin-left: 5px;
    vertical-align: top;
    padding: 5px 2px -5px 5px;
    font-size: 14px;
    text-align: left;
  }
}

@media screen and (max-width: 414px) {
  .buttonClass {
    width: 60px;
    height: 30px;
    margin-left: 5px;
    vertical-align: top;
    padding: 5px 2px -5px 5px;
    font-size: 14px;
    text-align: left;
    display: inline;
  }
  .dialog-footer {
    margin-top: -15px;
  }
}
</style>

<style lang='scss'>
.el-dialog {
  width: 625px;
  margin-top: -5px;
}
@media screen and (max-width: 414px) {
  .el-dialog {
    width: 355px;
    margin-top: -15px;
  }
  .dialog-footer {
    margin-top: -15px;
  }
}
</style>