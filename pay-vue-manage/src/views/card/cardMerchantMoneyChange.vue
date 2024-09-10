<!--sys flowing 商户资金账变明细 -->
<template>
  <div>
    <div class="flex flex-m mb20 searchBox"> 

      <div class="flex flex-m mr20">
        <span class="w80">码商名称:</span>
        <el-select
          clearable
          class="w180"
          size="medium"
          v-model="search_data.merchantId"
          placeholder="请选择" 
        >
          <el-option label="请选择码商" value></el-option>
          <el-option
            v-for="item in payMerchantInfoList"
            :key="item.merchantId"
            :label="item.merchantName + ' ('+item.merchantId+')'"
            :value="item.merchantId"
          ></el-option>
        </el-select>
      </div> 

       <!-- <div class="flex flex-m mr20">
        <span class="w100">商户名称:</span>
        <el-input
          v-model="search_data.merchantName"
          size="medium"
          class="w180"
          clearable
          placeholder="商户名称"
          style="margin-left:-5px;"
        ></el-input>
      </div> -->

      <div class="flex flex-m mr20">
        <span class="w100">商户订单号:</span>
        <el-input
          v-model="search_data.merchantWithdrawNo"
          size="medium"
          class="w180"
          clearable
          placeholder="商户订单号"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w100">平台订单号:</span>
        <el-input
          v-model="search_data.withdrawNo"
          size="medium"
          class="w180"
          clearable
          placeholder="平台订单号"
        ></el-input>
      </div> 
      
    </div>

    <div class="mb20 flex flex-m mr20 searchBox">
         <div class="flex flex-m mr15">
      <span class="w80">选择日期:</span>
      <el-date-picker
        v-model="time"
        type="daterange"
        align="right"
        unlink-panels
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        class="w240"
      ></el-date-picker>
         </div>
      <!-- <div class="flex flex-m mr20 searchBox">
        <span class="w60">通道:</span>
        <el-select class="w180" size="medium" v-model="search_data.channelCode" placeholder="请选择">
          <el-option label="请选择通道" value></el-option>
          <el-option
            v-for="item in s_queryChannelInfoList"
            :key="item.CHANNEL_CODE"
            :label="(item.PAY_TYPE == '0' ? '代付 - ' : '代收 - ') + item.CHANNEL_NAME"
            :value="item.CHANNEL_CODE"
          ></el-option>
        </el-select>
      </div> -->

   <!-- <div class="flex flex-m mr20">
        <span class="w100">流水类型:</span>
        <el-select class="w120" size="medium" v-model="search_data.flowType" placeholder="请选择">
          <el-option label="请选择类型" value></el-option>
          <el-option
            v-for="item in partnerFlowTypeList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div> -->

      <div class="flex flex-m">
        <span class="tc w40">备注:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.remark"
          clearable
          placeholder="增减、扣除"
        ></el-input>
      </div>

      <el-button
        class="ml20"
        size="small"
        icon="el-icon-search"
        @click.native="m_search"
        type="primary"
      >搜索 (ENTER回车键或ESC键)</el-button>
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
      <el-table-column align="center" prop="createTime" label="交易时间" width="90"></el-table-column>
       <el-table-column class="w180" align="center" prop="merchantName" label="码商名称/编号"  width="131">
          <template slot-scope="scope">
          <span>{{scope.row.merchantName}}</span><br>
          <span>{{scope.row.merchantId}}</span>
        </template>
       </el-table-column>
     
      <el-table-column align="center" prop="merchantWithdrawNo" label="商户订单号" ></el-table-column>
      <el-table-column align="center" prop="withdrawNo" label="平台订单号"></el-table-column> 
 
      <el-table-column align="center" prop="creditAmt" label="账变金额">
        <template slot-scope="scope">
          <span>{{scope.row.amt}}</span>
        </template>
      </el-table-column> 
      <el-table-column align="center" prop="amtBefore" label="账变前" width="109"></el-table-column>
      <el-table-column align="center" prop="amtNow" label="账变后" width="109"></el-table-column>

 
      <el-table-column align="center" prop="remark" label="备注" width="118" fixed="right"></el-table-column>
     

 
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
  name: "cardMerchantMoneyChange",
  data() {
    return {
      time: [],
      payMerchantInfoList:[],//从商户列表中查出 码商(agentRate == '-1')列表
       payerCodePriceList:[
        {code:300,price:300},
        {code:400,price:400},
        {code:500,price:500},
        {code:600,price:600},
        {code:700,price:700},
        {code:800,price:800},
        {code:900,price:900},
        {code:1000,price:1000},
      ],
      search_data: {
        flowType: "", //
        merchantId: "", //商户ID
        merchantName: "", //商户名称
        beginTime: "", //开始时间
        endTime: "", //结束时间
        channelCode: "", //通道编号
        label: "" //渠道标识
      },

      s_queryChannelInfoList: []
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
  },
  methods: {
    init() {
      this.m_search(); 
    },
    enterKey(event) {
      const code = event.keyCode; 
      //console.log(code);
      if (code == 108 || code == 13 || code == 27) { 
          this.m_search(); 
      }
    },
    enterKeyupDestroyed() {
      document.removeEventListener("keyup", this.enterKey);
    },
    enterKeyup() {
      document.addEventListener("keyup", this.enterKey);
    },
    m_search() { 
      this.getDatalist();
      this.s_queryChannelInfo();
      this.queryPayMerchantInfoList();//码商列表
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

    //获取通道搜索
    s_queryChannelInfo() {
      this.m_api.queryChannelInfo({}).then(res => {
        console.log(res);
        this.s_queryChannelInfoList = res.data;
      });
    },

    //获取数据信息
    getDatalist() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.m_tableLoading = true;
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";
      this.search_data.tableMerchantId = userData.merchantId;

      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryCardMerchantMoneyChangePage(data)
        .then(res => {
          //console.log(res)
          this.m_list = res.data.records;
          this.m_page.total = res.data.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
    },

  
  }
};
</script>
<style lang='scss' scoped>
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
}
</style>