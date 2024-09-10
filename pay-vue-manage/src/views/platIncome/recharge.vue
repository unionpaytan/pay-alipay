<!-- 提现明细  recharge-->
<template>
  <div>
    <div class="flex flex-m mb20">
      <div class="flex flex-m mr20">
        <span class="w80">收款人:</span>
        <el-input
          size="medium"
          class="w140"
          clearable
           v-model="search_data.acctName"
          placeholder="请输入"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w80">钱包地址:</span>
        <el-input
          size="medium"
          class="w220"
          v-model="search_data.acctAddr"
          clearable
          placeholder="请输入"
        ></el-input>
      </div>

       <div class="flex flex-m mr20">
        <span class="w80">通道编号:</span>
        <el-input
          size="medium"
          class="w220"
          v-model="search_data.channelCode"
          clearable
          placeholder="请输入"
        ></el-input>
      </div>

      <div class="flex flex-m mr20">
        <span class="w80">提现状态:</span>
        <el-select class="w180" size="medium" v-model="search_data.withdrawStatus" placeholder="请选择">
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in withdrawStatusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>
      
    </div>

    <div class="mb20">
      <span class="mr10">选择日期:</span>
         <el-date-picker
        v-model="time"
        type="daterange"
        align="right"
        unlink-panels
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format='yyyy-MM-dd'
       >
      </el-date-picker>

      <el-button class="ml20" size="small" icon="el-icon-search" @click.native="m_search" type="primary">搜索</el-button>
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
       <el-table-column align="center" prop="date" label="创建时间">
         <template slot-scope="scope">
          <span >{{scope.row.createTime | time }}</span>
        </template>
       </el-table-column>
       <el-table-column align="center" prop="withdrawNo" label="平台订单号"></el-table-column>
       <el-table-column align="center" prop="amt" label="提现金额"></el-table-column>
       <el-table-column align="center" prop="acctName" label="收款人"></el-table-column>
       <el-table-column align="center" prop="acctAddr" label="钱包地址"></el-table-column>
       <el-table-column align="center" prop="withdrawStatus" label="提现状态">
          <template slot-scope="scope">
           <span >{{scope.row.withdrawStatus | withdrawStatus }}</span>
          </template>
       </el-table-column>
       <el-table-column align="center" prop="channelCode" label="通道编号"></el-table-column>
       <el-table-column align="center" prop="channelName" label="通道名称"></el-table-column>
       <el-table-column align="center" prop="merchantName" label="操作人"></el-table-column>
     
      

      <!--  状态-->
      <!-- <el-table-column align="center" label="通道状态">
        <template slot-scope="scope">
          <span
            :class="scope.row.channelStatus=='0'?'green':'red'"
          >{{scope.row.channelStatus | status}}</span>
        </template>
      </el-table-column> -->

      <!-- <el-table-column align="center" label="操作">
        <template slot-scope="scope">
          <el-button @click.native="m_editForm(scope.row)" type="text">编辑</el-button>
          <el-button style="color:#ff0000" @click.native="m_oneDel(scope.row)" type="text">删除</el-button>
        </template>
      </el-table-column> -->
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
import moment from 'moment'
export default {
  name: 'recharge',
  data() {
    return {
      time:[],
      search_data: {
        acctName:'' , //收款人
        merchantId: '', //商户ID
        withdrawNo:'', //平台订单号
        withdrawStatus:'',//提现状态  //0 未知 1  成功 2  失败 3处理中
        requestType: 'PLATFORM', //订单来源 订单提交来源  API:接口提交,WEB:后台提交 ,PLATFORM 平台提现
        acctAddr:'', //钱包地址
        bankName:'', //银行名称
       
        merchantWithdrawNo:"",//商户订单号
        beginTime: '', //开始时间
        endTime: '', //结束时间
        channelCode: '', //通道编号  
      },

      

   


    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
        this.time = [moment().format('YYYY-MM-DD'), moment().format('YYYY-MM-DD')]
      this.getDatalist()
    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true
      this.search_data.beginTime = this.time[0] || ''
        this.search_data.endTime = this.time[1] || ''
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api.queryWithdrawPage(data).then(res => {
        //console.log(res)
        this.m_list = res.data.pages.records
        this.m_page.total = res.data.pages.total

      }).finally(() => {
        this.m_tableLoading = false
      })
    },

   

  




  }
}

</script>
<style lang='scss' scoped>
</style>