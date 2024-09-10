<!-- coinChannelWithdrawFlowList 通道流水 -->
<template>
  <div>
    <div class="flex flex-m tc topbox">
       <div class="flex-1 bor">
        <div>总手续费<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{sumMap.sumPayerTotal.toFixed(2) || 0.00}}</div>
      </div>

      <div class="flex-1 bor">
        <div>总代收手续费 %<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{sumMap.sumPayerRateFee || 0.00}}</div>
      </div>

      <div class="flex-1 bor">
        <div>总代收单笔<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{sumMap.sumPayerSingleFee || 0.00}}</div>
      </div>


      <div class="flex-1 bor">
        <div>总下发手续费 %<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{sumMap.sumPayerWithdrawRateFee || 0.00}}</div>
      </div>

      <div class="flex-1 bor">
        <div>总下发单笔<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{sumMap.sumPayerWithdrawSingleFee || 0.00}}</div>
      </div>

      <div class="flex-1">
        <div>总充值手续费<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{sumMap.sumRechargeFee || 0}}</div>
      </div>  

      <!-- <div class="flex-1">
        <div>总剩余手续费<span style="font-size:12px;">(USDT)</span></div>
        <div class="num">{{sumMap.sumPayerRemainFee || 0}}</div>
      </div> -->
    </div>

    <!-- * 
         * @搜索栏
         *
    -->
    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr20">
        <span class="w80">商户编号:</span>
        <el-input
          v-model="search_data.merchantId"
          size="medium"
          class="w180"
          clearable
          placeholder="商户编号"
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
      <div class="flex flex-m mr20">
        <span class="w100">通道流水号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.channelWithdrawNo"
          clearable
          placeholder="通道流水号"
        ></el-input>
      </div>

       <div class="flex flex-m mr20">
        <span class="w80">通道编号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.channelCode"
          clearable
          placeholder="通道编号"
        ></el-input>
      </div> 
     
    </div>

    <div class="mb20 flex flex-m searchBox">
     <div class="flex flex-m mr20">
      <span class="w90">选择日期:</span>
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
            v-for="item in partnerFlowTypeList"
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

      
      <el-table-column align="center" label="交易时间" width="140">
        <template slot-scope="scope">
          <span>{{scope.row.createTime | time }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="merchantId" label="商户编号"></el-table-column>
      <el-table-column align="center" prop="withdrawNo" label="平台订单号"></el-table-column>
      <el-table-column align="center" prop="channelWithdrawNo" label="通道流水号"></el-table-column>
      <el-table-column align="center" prop="channelCode" label="通道编号" width="180">
         <template slot-scope="scope">
           <span>{{scope.row.channelName}}</span><br>
          <span>{{scope.row.channelCode}}</span>
        </template>
      </el-table-column> 
      
      <el-table-column align="center" prop="flowType" label="流水类型" width="90">
        <template slot-scope="scope">
          <span>{{flowTypeText(scope.row.flowType,scope.row.payType)}}</span>
        </template>
      </el-table-column> 

      <el-table-column align="center" prop="amt" label="手续费" width="100">
        <template slot-scope="scope">
          <span :class="scope.row.flowType | flowTypeClass">{{scope.row.amt}}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="remark" label="备注" fixed="right" ></el-table-column>
      <el-table-column align="center" prop="managerName" label="操作员" width="100"></el-table-column>

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
  name: "coinChannelWithdrawFlowList",
  data() {
    return {
      time: [],
      buttonLoding:false,
      search_data: {
        channelCode: "", //商户编号
        channelName: "", //通道名称
        flowType: "", //
        rechargeType: "", //
        beginTime: "", //开始时间
        endTime: "", //结束时间
        remark: "" //通道标识
      },
      sumMap: {
        sumPayerTotal:0.0,//总手续费 =  总代收+下发手续费 + 总单笔手续费
        sumPayerRateFee: 0.0, 
        sumPayerSingleFee:0.0,
        sumPayerWithdrawRateFee: 0.0, //总下发手续费
        sumPayerWithdrawSingleFee: 0.0, //总单笔手续费
        sumPayerRemainFee: 0.0 //  剩余手续费
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
        .queryCoinChannelFlowPage(data)
        .then(res => {
          //console.log(res)
          this.m_list = res.data.pages.records;
          this.m_page.total = res.data.pages.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
        //获取手续费
      this.getSumPayerFee(this.search_data);
    },

    //获取手续费汇总
    getSumPayerFee(data) { 
      const that = this;
      this.buttonLoding = true;
      this.m_api
        .querySumCoinPayer(data)
        .then(res => { 
           this.sumMap = res.data; 
           that.sumMap.sumPayerTotal = parseFloat(that.sumMap.sumPayerRateFee) + 
           parseFloat(that.sumMap.sumPayerSingleFee) + 
           parseFloat(that.sumMap.sumPayerWithdrawRateFee) + 
           parseFloat(that.sumMap.sumPayerWithdrawSingleFee)
        })
        .finally(() => {
           this.buttonLoding = false; 
        });
    },

     flowTypeText(flowType,payType){
      if (payType == '0') {
        //代付
        if (flowType == '0') {
                        return '充值'
                    }
                    if (flowType == '1') {
                        return '冲正'
                    }
                    return '代付'
      }else {

        if (flowType == '0') {
                        return '充值'
                    }
                    if (flowType == '1') {
                        return '代收'
                    }
                    return '代付'

      }
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