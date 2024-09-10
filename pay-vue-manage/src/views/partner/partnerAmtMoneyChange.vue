<!--partnerAmtMoneyChange 外包跑量账变明细 -->
<template>
  <div> 
 <div class="flex flex-m tc topbox">
      <div class="flex-1 bor">
        <div>总代付成功交易金额<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{parseFloat(countMap.sumMerchantWithdrawAmt).toFixed(2) || 0.00}}</div>
      </div> 
      <div class="flex-1">
        <div>总代收成功交易金额<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{parseFloat(countMap.sumMerchantDepositAmt).toFixed(2) || 0.00}}</div>
      </div> 
  </div>
    <!-- * 
         * @搜索栏
         *
    -->
    
    <div class="flex flex-m mb20">
      <div class="flex flex-m mr10">
        <span class="w80">外包名称:</span>
        <el-input
          v-model="search_data.merchantName"
          size="medium"
          class="w210"
          clearable
          placeholder="外包名称"
        ></el-input>
      </div>

       <div class="flex flex-m mr20">
        <span class="w80">外包编号:</span>
        <el-input
          v-model="search_data.merchantId"
          size="medium"
          class="w180"
          clearable
          placeholder="外包编号"
          style="margin-left:-5px;"
        ></el-input>
      </div> 

      <div class="flex flex-m mr20">
        <span class="w90">平台流水号:</span>
        <el-input
          v-model="search_data.withdrawNo"
          size="medium"
          class="w220"
          clearable
          placeholder="平台流水号"
        ></el-input>
      </div> 

     <div class="flex flex-m">
        <span class="tc w50">备注:</span>
        <el-input
          size="medium"
          class="w150"
          v-model="search_data.remark"
          clearable
          placeholder="备注"
        ></el-input>
      </div>
      
    </div>

    <div class="mb20 flex flex-m mr20">
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

   <!--<div class="flex flex-m mr20">
        <span class="w100">流水类型:</span>
        <el-select class="w120" size="medium" v-model="search_data.flowType" placeholder="请选择">
          <el-option label="请选择类型" value></el-option>
          <el-option
            v-for="item in flowTypeList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
    </div>-->

    

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
      <el-table-column align="center" prop="createTime" label="交易时间" width="140"></el-table-column>
       <el-table-column class="w180" align="center" prop="merchantName" label="外包名称"  width="120"></el-table-column>
      <el-table-column class="w180" align="center" prop="merchantId" label="外包编号"  width="129"></el-table-column> 
      <el-table-column align="center" prop="withdrawNo" label="平台流水号" width="129"></el-table-column>

    <el-table-column class="w100" align="center" prop="payType" label="类别" width="80">
        <template slot-scope="scope">
          <span :class="scope.row.flowType | flowTypeClass">{{scope.row.payType | payTypeFilter }}</span>
        </template>
      </el-table-column>
  
      
      <!-- <el-table-column align="center" prop="amtTotal" label="总成功金额" width="109"></el-table-column> -->
      <el-table-column align="center" prop="amt" label="变动金额" width="109"></el-table-column>

      <el-table-column align="center" prop="amtBefore" label="账变前" width="109"></el-table-column>
      <el-table-column align="center" prop="amtNow" label="账变后跑量" width="109"></el-table-column>

       
      <el-table-column align="center" prop="remark" label="备注"   fixed="right"></el-table-column>
     

      <!-- <el-table-column align="center" prop="merchantWithdrawNo" label="商户流水号"></el-table-column>

      <el-table-column align="center" prop="fee" label="手续费"></el-table-column>
      <el-table-column align="center" prop="creditAmt" label="结算金额"></el-table-column>
      <el-table-column align="center" prop="remark" label="备注"></el-table-column>
      <el-table-column align="center" prop="managerName" label="操作人"></el-table-column>-->

      <!--  状态-->
      <!-- <el-table-column align="center" label="通道状态">
        <template slot-scope="scope">
          <span
            :class="scope.row.channelStatus=='0'?'green':'red'"
          >{{scope.row.channelStatus | status}}</span>
        </template>
      </el-table-column>-->

      <!-- <el-table-column align="center" label="操作">
        <template slot-scope="scope">
          <el-button @click.native="m_editForm(scope.row)" type="text">编辑</el-button>
          <el-button style="color:#ff0000" @click.native="m_oneDel(scope.row)" type="text">删除</el-button>
        </template>
      </el-table-column>-->
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
  name: "partnerAmtMoneyChange",
  data() {
    return {
      time: [],
      search_data: {
        flowType: "", //
        payType:"",
        merchantId: "", //商户ID
        merchantName: "", //商户名称
        beginTime: "", //开始时间
        endTime: "", //结束时间
        channelCode: "", //通道编号
        label: "" //渠道标识
      },

       countMap: {
          sumMerchantWithdrawAmt:0, 
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
        .queryPartnerAmtMoneyChangePage(data)
        .then(res => {
          //console.log(res)
          this.m_list = res.data.pages.records;
          this.m_page.total = res.data.pages.total;
          this.countMap = res.data.countMap; 
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
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
</style>