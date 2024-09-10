<!-- 码商统计报表  cardMerchantReport-->
<template>
  <div>
     <div class="flex flex-m tc topbox">
      <!-- <div class="flex-1 bor">
        <div>码商总人数<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.masangTotal || 0 }}</div>
      </div> 
       <div class="flex-1">
        <div>码商总余额<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.sumAccountBalance || 0 }}</div>
      </div>  -->
       <div class="flex-1 bor">
        <div>交易总金额<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.totalSumAmt || 0 }}</div>
      </div> 
       <div class="flex-1 bor">
        <div>交易总分润<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.totalSumProfit || 0 }}</div>
      </div> 
       <div class="flex-1 bor">
        <div>交易总笔数<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.totalCountNum || 0 }}</div>
      </div> 
        <div class="flex-1 bor">
        <div>总成功金额<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.totalSuccessSumAmt || 0 }}</div>
      </div> 
       <div class="flex-1 bor">
        <div>总成功笔数<span style="font-size:12px;"></span></div>
        <div class="num">{{ countMap.totalSuccessCountNum || 0 }}</div>
      </div> 
       <div class="flex-1">
        <div>总成功率<span style="font-size:12px;"></span></div>
        <div class="num" v-if="countMap.totalCountNum >0">{{ parseFloat(parseFloat(countMap.totalSuccessCountNum/ countMap.totalCountNum) * 100 ).toFixed(2) || 0 }}%</div>
         <div class="num" v-if="countMap.totalCountNum == 0">0%</div>
      </div> 
    </div>

    <!-- * 
         * @搜索栏
         *
    --> 
       <div class="flex flex-m mb20 searchBox">
   

      <div class="flex flex-m mr20">
        <span class="w195 mr10">码商名称:</span>
        <el-input
          size="medium" 
          v-model="search_data.merchantName"
          clearable
          placeholder="码商名称"
          class="w150"
        ></el-input>
      </div>


      <div class="flex flex-m mr20">
        <span class="w195 mr10">码商编号:</span>
        <el-input
          size="medium" 
          v-model="search_data.merchantId"
          clearable
          placeholder="码商编号"
          class="w150"
        ></el-input>
      </div>

<div class="flex flex-m mr20">
      <span class="w60">日期:</span>
      <el-date-picker
        v-model="time"
        type="daterange"
        unlink-panels
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        class="w250"
      ></el-date-picker>
  </div>


         <div class="flex flex-m mr50">
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

     <!-- <el-table-column align="center" prop="createTime" label="创建时间"></el-table-column> -->
      <el-table-column label="序号" type="index" width="60" align="center">
        <template slot-scope="scope">
          <span>{{ (m_page.page - 1) * m_page.rows + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
     <el-table-column align="center" prop="" label="码商名称/编号">
          <template slot-scope="scope">
          <span>{{scope.row.merchantName}}</span><br><span>{{scope.row.merchantId}}</span>
          <br>
           <span style="font-style:italic;color:#c0c0c0;font-size:13px;">权重:{{scope.row.agentWeight}}</span>
        </template>
     </el-table-column>
    
      <el-table-column align="center" prop="sumAmt" label="交易总金额"></el-table-column>
       <el-table-column align="center" prop="sumProfit" label="交易总分润"></el-table-column>
      <el-table-column align="center" prop="countNum" label="交易总订单"></el-table-column> 
      <el-table-column align="center" prop="sumSuccessAmt" label="总成功金额"></el-table-column>
      <el-table-column align="center" prop="countSuccessNum" label="总成功订单"></el-table-column> 
      <el-table-column align="center" prop label="成功率">
         <template slot-scope="scope">
           <span v-if="scope.row.countNum > 0">{{ parseFloat(parseFloat(scope.row.countSuccessNum/ scope.row.countNum) * 100 ).toFixed(2) || 0 }}%</span>
           <span v-if="scope.row.countNum == 0">0.00%</span>
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
  </div>
</template>

<script>
import qrcode from "qrcode";
import * as api from "@/api/public";
import moment from "moment";
export default {
  name: "cardMerchantReport",
  data() {
    return {
      time: [],
      search_data: {
        beginTime: "", //开始时间
        endTime: "", //结束时间
      },
      countMap:{},
      formData: {
        id: "",
        userName: "", //用户名称
        loginName: "", // 登陆账号
        maillbox: "", // 邮箱
        passwrod: "", // 密码
        roleId: "", // 角色ID
        googleKey: "", //google key
        googleUrl: "",
        authurl: "",
        tableMerchantId: "",
        googleCode: "",
        emailCode: "", //邮箱code
        agentWeight:"9",
        accountBalance:10000,
      },
      authurl: "",
      //邮箱
      sendNum: 0,
      time: null,
      get_loading: false,

      merchant_data: {
        userName: "",
        merchantId: "",
        googleCode: "",
        emailCode: "",
      },

      merchant_loding: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      merchant_show_del: false,
      merchant_show_status: false,
      m_rules: {
        userName: [{ required: true, message: "请输入", trigger: "blur" }],
        loginName: [{ required: true, message: "请输入", trigger: "blur" }],
        maillbox: [{ required: true, message: "请输入", trigger: "blur" }],
        passwrod: [{ required: true, message: "请输入", trigger: "blur" }],
        roleId: [{ required: true, message: "请输入", trigger: "change" }],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
        emailCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      merchantGoogleForm_rules: { 
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
        emailCode: [{ required: true, message: "请输入", trigger: "blur" }]
      },


      roleList: [], //角色列表
    };
  },
  created() {
     this.time = [moment().format("YYYY-MM-DD"), moment().format("YYYY-MM-DD")];
    this.userData =
      (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
    this.init();
  },
  methods: {
    init() {
      this.getDatalist();
      //this.queryUserRoleListPage();
    },   
    //码商:获取数据信息
    getDatalist() {
      
      this.search_data.beginTime = this.time[0] || "";
      this.search_data.endTime = this.time[1] || "";

      this.m_tableLoading = true;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId || 0;
      let data = Object.assign(this.search_data, this.m_page);
      //码商列表
      api
        .queryMerchantWholeSellerReport(data)
        .then((res) => {
          this.m_list = res.data.data.records;
          this.m_page.total = res.data.data.total;
           
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
        //统计订单 queryCoinHolderDepositList
       api.queryCoinHolderDepositList(data)
        .then((res) => { 
         this.countMap = res.data; 
        })
        .finally(() => {
         // this.m_tableLoading = false;
        });
    },  
 
  },
  filters: {
    f_status(v) {
      if (v == 0) {
        return "禁用";
      }
      if (v == 1) {
        return "启用";
      }
    },
  },
};
</script>
<style lang='scss' scoped>
 .authurl {
   height: 180px;
 }
</style>
<style lang='scss'>
.el-dialog {
    width:425px;
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
.remark {
  width:60px;
}

@media screen and (max-width: 414px) {
  .searchBox_bankcard {
    flex-wrap: wrap; 
    flex-direction:column;
    align-items: flex-start;
    
  } 
  .remark {
    width:80px;
  }
}
</style>
