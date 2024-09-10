<!-- cardChannelFlowList 通道流水 -->
<template>
  <div>
    <div class="flex flex-m tc topbox">
       <div class="flex-1 bor">
        <div>总手续费</div>
        <div class="num">{{parseFloat(parseFloat(sumMap.sumCardPayerWithdrawRateFee) + parseFloat(sumMap.sumCardPayerWithdrawFee)).toFixed(2) || 0.00}}</div>
      </div>

      <div class="flex-1 bor">
        <div>总下发手续费</div>
        <div class="num">{{sumMap.sumCardPayerWithdrawRateFee || 0.00}}</div>
      </div>

      <div class="flex-1 bor">
        <div>总单笔手续费</div>
        <div class="num">{{sumMap.sumCardPayerWithdrawFee || 0.00}}</div>
      </div>

      <div class="flex-1">
        <div>总剩余手续费</div>
        <div class="num">{{sumMap.sumCardPayerRemainFee || 0}}</div>
      </div>
    </div>

    <!-- * 
         * @搜索栏
         *
    -->
    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr20">
        <span class="w100">平台订单号:</span>
        <el-input
          v-model="search_data.cardWithdrawNo"
          size="medium"
          class="w230"
          clearable
          placeholder="平台订单号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w100">通道流水号:</span>
        <el-input
          size="medium"
          class="w280"
          v-model="search_data.channelWithdrawNo"
          clearable
          placeholder="通道流水号"
        ></el-input>
      </div>

       <div class="flex flex-m mr20">
        <span class="w80">通道编号:</span>
        <el-input
          size="medium"
          class="w230"
          v-model="search_data.cardChannelCode"
          clearable
          placeholder="通道编号"
        ></el-input>
      </div>


      <!-- 
      <div class="flex flex-m mr20">
        <span class="w80">充值类别:</span>
        <el-select class="w120" size="medium" v-model="search_data.rechargeType" placeholder="请选择">
          <el-option label="请选择类别" value></el-option>
          <el-option
            v-for="item in rechargeTypeList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>-->
     
    </div>

    <div class="mb20 flex flex-m searchBox">
     <div class="flex flex-m mr20">
      <span class="w90 mr10">选择日期:</span>
      <el-date-picker
        v-model="time"
        type="daterange"
        align="right"
        unlink-panels
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        class="w230"
      ></el-date-picker>
  </div>

      <div class="flex flex-m mr20">
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
      </div>

       <div class="flex flex-m mr20">
        <span class="tc w60">备注:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.remark"
          clearable
          placeholder="备注"
        ></el-input>
      </div>

      <el-button
        class="ml20"
        size="small"
        icon="el-icon-search"
        @click.native="m_search"
        type="primary"
        :loading="buttonLoding"
      >搜索</el-button>
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

      <!-- <el-table-column align="center" prop="cardWithdrawNo" label="银行卡流水号"></el-table-column> -->
      <el-table-column align="center" label="交易时间" width="140">
        <template slot-scope="scope">
          <span>{{scope.row.createTime | time }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="cardWithdrawNo" label="平台订单号"></el-table-column>
      <el-table-column align="center" prop="channelWithdrawNo" label="通道流水号"></el-table-column>
      <el-table-column align="center" prop="cardChannelCode" label="通道编号" width="180"></el-table-column>
      <el-table-column align="center" prop="chlName" label="通道名称" width="118"></el-table-column>
      <el-table-column align="center" prop="flowType" label="流水类型" width="90">
        <template slot-scope="scope">
          <span>{{scope.row.flowType | flowTypeFilter }}</span>
        </template>
      </el-table-column>

      <!-- <el-table-column align="center" prop="flowType" label="充值类别">
        <template slot-scope="scope">
          <span>{{scope.row.rechargeType | rechargeTypeFilter }}</span>
        </template>
      </el-table-column>-->

      <el-table-column align="center" prop="amt" label="金额" width="100">
        <template slot-scope="scope">
          <span :class="scope.row.flowType | flowTypeClass">{{scope.row.amt}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="remark" label="备注" width="150"></el-table-column>
      <el-table-column align="center" prop="managerName" label="操作人" width="100"></el-table-column>

      <!-- <el-table-column align="center" prop="merchantWithdrawNo" label="商户订单号"></el-table-column>

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
  name: "cardChannelFlowList",
  data() {
    return {
      time: [],
      buttonLoding:false,
      search_data: {
        cardChannelCode: "", //商户编号
        chlName: "", //通道名称
        flowType: "", //
        rechargeType: "", //
        beginTime: "", //开始时间
        endTime: "", //结束时间
        remark: "" //通道标识
      },
      sumMap: {
        sumCardPayerFee: 0.0, //总手续费 =  总下发手续费 + 总单笔手续费
        sumCardPayerWithdrawRateFee: 0.0, //总下发手续费
        sumCardPayerWithdrawFee: 0.0, //总单笔手续费
        sumCardPayerRemainFee: 0.0 //  剩余手续费
      }
    };
  },
  created() {
    this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")];
    this.init();
  },
  methods: {
    init() {
      this.getDatalist();
    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true;
      this.search_data.beginTime = this.time[0] + " 00:00:00" || "";
      this.search_data.endTime = this.time[1] + " 23:59:59" || "";
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId;

      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryCardChannelFlowPage(data)
        .then(res => {
          //console.log(res)
          this.m_list = res.data.records;
          this.m_page.total = res.data.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
        //获取手续费
      this.getCardPayerFee(this.search_data);

    },

    //获取手续费汇总
    getCardPayerFee(data) {
     // console.log("获取手续费汇总");
     // console.log(JSON.stringify(data)); 
      this.buttonLoding = true;
      this.m_api
        .querySumCoinPayer(data)
        .then(res => {
          // console.log(res.data);
           this.sumMap = res.data;
        })
        .finally(() => {
           this.buttonLoding = false;
         // this.m_tableLoading = false;
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
  width:100%;
}
.bor {
  border-right: 1px solid #999;
}
.dia_box {
  font-size: 14px;
  line-height: 28px;
}
.num {
  color: rgba(0, 0, 0, 0.847058823529412);
  font-size: 24px;
}
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
}
</style>