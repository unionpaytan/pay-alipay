<!-- cardMerchantRechargeOrder 卡主银行卡充值流水 -->

<template>
  <div>
    <div class="flex flex-m tc topbox">
      <!-- <div class="flex-1 bor">
        <div>总充值金额</div>
        <div class="num">{{count.sumPayerAmt || 0.00}}</div>
      </div>

      <div class="flex-1 bor">
        <div>总入帐金额</div>
        <div class="num">{{count.sumPayerCreditAmt || 0.00}}</div>
      </div> -->

      <div class="flex-1 bor">
        <div>总充值金额</div>
        <div class="num">{{countMapBySearch.sumPayerAmt || 0.00}}</div>
      </div>

      <div class="flex-1">
        <div>总入帐金额</div>
        <div class="num">{{countMapBySearch.sumPayerCreditAmt || 0.00}}</div>
      </div>

    </div>

    <!-- * 
         * @搜索栏
         *
    -->

    <div class="flex flex-m mb20 searchBox">
      
        <div class="flex flex-m mr20">
      
       <span class="w80">流水号:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.cardWithdrawNo"
          clearable
          placeholder="银行卡流水号"
        ></el-input>
     </div> 

     <div class="flex flex-m mr20">
        <span class="w80">持卡人:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.payerName"
          clearable
          placeholder="持卡人"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w80">卡号:</span>
        <el-input
          size="medium"
          class="w220"
          v-model="search_data.payerCardNo"
          clearable
          placeholder="卡号"
        ></el-input>
      </div>
       <div class="flex flex-m mr20">
        <span class="w80">备注:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.remark"
          clearable
          placeholder=""
        ></el-input>
      </div>

    </div> 
  

    <div class="flex flex-m mb20 searchBox">

      <span class="w80">银行名称:</span>
        <el-select
          class="w180"
          size="medium"
          v-model="search_data.payerBankCode"
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

        <span class="w80">所属卡主:</span>
        <el-select
          class="w180"
          size="medium"
          v-model="search_data.payerMerchantId"
          placeholder="请选择卡主"
        >
          <el-option label="请选择卡主" value></el-option>
          <el-option
            v-for="item in queryMerchantIdList"
            :key="item.merchantId"
            :label="item.merchantId + '('+ item.merchantName+')'"
            :value="item.merchantId"
          ></el-option>
        </el-select>

     <span class="w80">三方通道:</span>
        <el-select
          class="w180"
          size="medium"
          v-model="search_data.cardChannelCode"
          placeholder="请选择三方通道"
        >
          <el-option label="请选择三方通道" value></el-option>
          <el-option
            v-for="item in queryCardChannelInfoList"
            :key="item.channelName"
            :label="item.channelName"
            :value="item.cardChannelCode"
          ></el-option>
        </el-select>


      <span class="w80">日期:</span>
      <el-date-picker
        v-model="time"
        type="daterange"
        align="right"
        unlink-panels
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        class="w250"
      ></el-date-picker> 
      

      <div class="flex flex-m mr20">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          @click.native="m_search"
          type="primary"
        >搜索</el-button>
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
      <!-- <el-table-column type="selection" width="55"></el-table-column> -->

      <el-table-column align="center" prop="cardChannelCode" label="通道编号" width="180"></el-table-column>
      <el-table-column align="center" prop="cardWithdrawNo" label="银行卡流水号" width="150"></el-table-column>
       <el-table-column align="center" prop="bankName" label="银行名称"></el-table-column>
      <el-table-column align="center" prop="payerName" label="持卡人"></el-table-column> 
       <el-table-column align="center" prop="payerCardNo" label="卡号" width="158"></el-table-column> 
      <el-table-column align="center" prop="amt" label="充值金额"></el-table-column>
      <el-table-column align="center" prop="creditAmt" label="入帐金额"></el-table-column> 
      <el-table-column align="center" prop="remark" label="备注" width="200"></el-table-column>

      <el-table-column align="center" label="充值时间" width="90">
        <template slot-scope="scope">
          <span>{{scope.row.createTime | time }}</span>
        </template>
      </el-table-column>

      <!-- <el-table-column align="center" prop="managerName" label="操作人"></el-table-column> -->
    </el-table>

    <Paging
      class="mt20 mb30"
      :pageIndex="m_page.page"
      :pageSize="m_page.rows"
      :pageTotal="m_page.total"
      @changeSize="m_changesize"
      @changeIndex="m_changeindex"
    ></Paging>
  </div>
</template>

<script>
import moment from "moment";
export default {
  name: "cardMerchantRechargeOrder",
  data() {
    return {
      time: [],
      search_data: {
        cardChannelCode: "", //商户ID
        flowType: "0", //流水类型
        bankName:"",
        payerName:"",
        payerCardNo: "", //商户名称
        payerCardType:"0",
        beginTime: "", //开始时间
        endTime: "", //结束时间 
        remark: "", //渠道标识
        tableMerchantId:"",
        payerMerchantId: "", //卡主
        payerBankType:"-1",//自动 -1:手动
      },

      count: {}, //统计数据
      countMapBySearch:{}, //须预先渲染
      detail: {},
      queryCardChannelInfoList: [],
      queryMerchantIdList:[],//卡主列表数组
      bankCodeDataList: [], //银行列表
    };
  },
  created() {
    this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")];
    this.init();
  },
  methods: {
    init() {
      this.getDatalist();
      this.queryCardChannelInfo(); //三方通道
      this.getBankCodeListFromServer();//获取银行卡列表
      this.queryMerchantIdListInfo(); //获取卡主列表数据信息
    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true;
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";
      
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId
      
      let data = { ...this.search_data, ...this.m_page };
      this.m_api
        .queryPayerFlowPageRechargeCount(data)
        .then(res => { 
          this.m_list = res.data.pages.records;
          this.count = res.data.countInfo || {};
          this.countMapBySearch = res.data.countMapBySearch || {};
          this.m_page.total = res.data.pages.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
    },

   //获取三方通道
    queryCardChannelInfo() {
      this.m_api.queryCardChannelInfo({}).then((res) => {
        //console.log(res)
        this.queryCardChannelInfoList = res.data;
      });
    },

        //获取卡主数组列表
    queryMerchantIdListInfo() {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_api.queryMerchantIdListInfo({
        status:0,
        agentRate:'-1',
        tableMerchantId:userData.merchantId,
        rows:99999,
        page:1,
      }).then((res) => {
        this.queryMerchantIdList = res.data.pages.records;
      });
    }, 

        //获取银行列表
    getBankCodeListFromServer() {
      this.m_api.querybankCodeDataList({
        payerBankType:"-1",
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

    handel_upload_excel() {
      let user =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let url =
        `${this.m_host}/fileDownload/rechargeExportExcel?${this.m_getQuery(
          this.search_data
        )}` +
        "&tableMerchantId=" +
        "";
      console.log(url);
      this.upload_excel(url);
    }
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
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
}
</style>