<!-- cardPayerSmsRechargeFlowList 银行短信充值流水 -->

<template>
  <div>
    <!-- <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>总充值金额</div>
        <div class="num">{{count.sumPayerAmt || 0.00}}</div>
      </div>

      <div class="flex-1 bor">
        <div>总入帐金额</div>
        <div class="num">{{count.sumPayerCreditAmt || 0.00}}</div>
      </div>

        <div class="flex-1 bor">
        <div>当日充值金额</div>
        <div class="num">{{countMapBySearch.sumPayerAmt || 0.00}}</div>
      </div>

      <div class="flex-1">
        <div>当日入帐金额</div>
        <div class="num">{{countMapBySearch.sumPayerCreditAmt || 0.00}}</div>
      </div>

    </div>-->

    <!-- * 
         * @搜索栏
         *
    -->

    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr20" style="margin-left:10px">
        <span class="w60">手机号:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.payerAddr"
          clearable
          placeholder="手机号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20" style="margin-left:10px">
        <span class="w80">收款账号:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.payerName"
          clearable
          placeholder="收款账号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20" style="margin-left:10px">
        <span class="w60">金额:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.amount"
          clearable
          placeholder="金额"
        ></el-input>
      </div>
       <div class="flex flex-m mr20" style="margin-left:10px">
        <span class="w100">银联订单号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.orderId"
          clearable
          placeholder="银联订单号"
        ></el-input>
      </div>

      <!-- <div class="flex flex-m mr20">
        <span class="w40">银行:</span>
        <el-select
          class="w120"
          size="medium"
          v-model="search_data.payerBankCode"
          placeholder="请选择银行"
        >
          <el-option label="请选择银行" value></el-option>
          <el-option v-for="item in bankList" :key="item.value" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </div> -->
      <!-- <div class="flex flex-m mr20">
        <span class="w40">短信:</span>
        <el-select class="w90" size="medium" v-model="search_data.smsType" placeholder="请选择">
          <el-option label="类型" value></el-option>
          <el-option
            v-for="item in smsTypeList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div> --> 
  

      <!-- <el-button
        v-if="handelAuto('export')"
        class="mr20"
        size="small"
        icon="el-icon-document"
        @click="handel_upload_excel"
        type="primary"
      >导出</el-button>-->
    </div>

    <div class="flex flex-m mb20 searchBox">
            <span class="w50 mr10" style="margin-left:10px">日期:</span>
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

      <!-- <div class="flex flex-m mr20" style="margin-left:10px">
        <span class="w50">备注:</span>
        <el-input
          size="medium"
          class="w120"
          v-model="search_data.remark"
          clearable
          placeholder="支付成功"
        ></el-input>
      </div> -->


    <div style="margin-left:10px" class="flex flex-m mr20">
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

      <!-- <el-table-column align="center" prop label="银行名称" width="120">
        <template slot-scope="scope">
          <span>{{scope.row.smsNumber | smsNumberFilter}}</span>
        </template>
      </el-table-column> -->
      <el-table-column align="center" prop="payerAddr" label="手机号" width="150">
         <template slot-scope="scope">
          <span>{{scope.row.payerAddr}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="payerName" label="收款账号"></el-table-column>
     
      <el-table-column align="center" prop="orderId" label="上游订单号"></el-table-column>
       <el-table-column align="center" prop="amount" label="金额"></el-table-column>
      <el-table-column align="center" label="收款时间" width="150">
        <template slot-scope="scope">
          <span>{{scope.row.payTime }}</span>
        </template>
      </el-table-column>
       <el-table-column align="center" prop="remark" label="备注"></el-table-column>
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
  name: "appRecordList",
  data() {
    return {
      time: [],
      search_data: {
        deviceId:"",
        orderId:"",
        payerName: "",
        payerAddr: "", //商户名称
        beginTime: "", //开始时间
        endTime: "", //结束时间
        remark: "", //渠道标识
        tableMerchantId: "",  
        amount:"",
      },

      count: {}, //统计数据
      countMapBySearch: {}, //须预先渲染
      queryChannelInfoList: [],
      detail: {}
    };
  },
  created() {
    this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")];
    //console.log(this.time)
    this.init();
  },
  methods: {
    init() {
      this.getDatalist();
      //this.queryChannelInfo();
    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true;
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";

      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;

      let data = { ...this.search_data, ...this.m_page };
      this.m_api
        .queryAppRecordList(data)
        .then(res => {
          //console.log(res)
          this.m_list = res.data.records;
          //this.count = res.data.countInfo || {};
          //this.countMapBySearch = res.data.countMapBySearch || {};
          this.m_page.total = res.data.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
    },

    //获取
    queryChannelInfo() {
      this.m_api.queryChannelInfo({}).then(res => {
        //console.log(res)
        this.queryChannelInfoList = res.data;
      });
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