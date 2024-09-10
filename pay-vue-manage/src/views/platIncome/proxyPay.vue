<!-- 通道收益 proxyPay-->
<template>
  <div>
    <div class="flex flex-m mb20">
      <div class="flex flex-m mr20">
        <span class="w80">通道编号:</span>
        <el-input size="medium" class="w180" v-model="search_data.channelCode" clearable placeholder="请输入通道编号"></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w80">通道名称:</span>
        <el-input size="medium" class="w180" v-model="search_data.channelName" clearable placeholder="请输入通道名称"></el-input>
      </div>

      <el-button class size="small" icon="el-icon-search" @click.native="m_search" type="primary">搜索</el-button>
    </div>

    <el-table v-loading="m_tableLoading" @selection-change="m_handleSelectionChange" @current-change="m_handleCurrentChange" class="mtable" :stripe="true" tooltip-effect="dark" :highlight-current-row="true" :data="m_list">
      <el-table-column align="center" prop="channelCode" label="通道编号"></el-table-column>
      <el-table-column align="center" prop="channelName" label="通道名称"></el-table-column>

      <el-table-column align="center" prop="platformAccountBalance" label="可提现金额">
        <template slot-scope="scope"><span class="red">{{scope.row.platformAccountBalance }}</span> </template>
      </el-table-column>

      <!-- <el-table-column align="center" label="操作">
        <template slot-scope="scope"> 
          <el-button v-if="handelAuto('withdrawal')" style="color:#ff0000" @click.native="showDialog(scope.row)" type="text">立即提现</el-button>
        </template>
      </el-table-column> -->

    </el-table>

    <!-- 编辑新增谈框 -->

    <el-dialog :close-on-click-modal="false" v-if="m_show" title="提现" :visible.sync="m_show" width="1000px">
      <div class="dia_box">
        <div class="flex flex-m">
          <div class="flex-1">通道编号：<span class="gray">{{detail.channelCode}}</span></div>
          <div class="flex-1">代付限额：<span class="gray">{{detail.singleMinimum}} - {{detail.singleHighest}}</span></div>
        </div>

        <div class="flex flex-m">
          <div class="flex-1">通道名称：<span class="gray">{{detail.channelName}}</span></div>
          <div class="flex-1">代付时间：<span class="gray">{{detail.beginWithdrawTime}} - {{detail.endWithdrawTime}}</span></div>
        </div>

        <div class="flex flex-m">
          <div class="flex-1">提现余额：<span class="red fs20">{{detail.platformAccountBalance}}</span></div>
        </div>

      </div>

      <el-tabs v-model="activeName">
        <el-tab-pane label="单卡多笔" name="1"></el-tab-pane>
        <el-tab-pane label="多卡多笔" name="2"></el-tab-pane>

      </el-tabs>

      <div>
        <el-form :model="f_data" :rules="m_rules" ref="ruleForm">
          <el-form-item v-if="activeName==1" label="收款人" label-width="80px" prop="acctName">
            <el-input class="w300" placeholder="请输入" v-model="f_data.acctName" auto-complete="off" clearable></el-input>
          </el-form-item>

          <el-form-item v-if="activeName==1" label="钱包地址" label-width="80px" prop="acctAddr">
            <el-input class="w300" clearable placeholder="请输入" v-model="f_data.acctAddr" auto-complete="off"></el-input>
          </el-form-item>

          <el-form-item v-if="activeName==1" label="身份证号" label-width="80px">
            <el-input class="w300" clearable placeholder="非必填" v-model="f_data.idCard" auto-complete="off"></el-input>
          </el-form-item>

          <el-form-item v-if="activeName==1" label="手机号" label-width="80px">
            <el-input class="w300" clearable placeholder="非必填" v-model="f_data.bankePhone" auto-complete="off"></el-input>
          </el-form-item>

          <el-table v-show="activeName==1" :data="batchList_1" border style="width: 100%">
            <el-table-column align="center" type="index" width="50" label="序号"> </el-table-column>
            <el-table-column align="center" prop="date" label="代付金额">
              <template slot-scope="scope">
                <el-input @mousewheel.native.prevent type="number" v-model="scope.row.amt" placeholder="请输入代付金额"></el-input>
              </template>
            </el-table-column>
          </el-table>

          <el-table v-show="activeName==2" :data="batchList_2" border style="width: 100%">
            <el-table-column align="center" type="index" width="50" label="序号"> </el-table-column>

            <el-table-column align="center" prop="date" width="100px" label="收款人">
              <template slot-scope="scope">
                <el-input v-model="scope.row.acctName" placeholder="请输入"></el-input>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="date" label="钱包地址">
              <template slot-scope="scope">
                <el-input @mousewheel.native.prevent type="number" v-model="scope.row.acctAddr" placeholder="请输入"></el-input>
              </template>
            </el-table-column>
            <el-table-column align="center" prop="date" width="170px" label="代付金额">
              <template slot-scope="scope">
                <el-input @mousewheel.native.prevent type="number" v-model="scope.row.amt" placeholder="请输入代付金额"></el-input>
              </template>
            </el-table-column>

            <el-table-column align="center" prop="date" label="身份证号">
              <template slot-scope="scope">
                <el-input v-model="scope.row.idCard" placeholder="非必填"></el-input>
              </template>
            </el-table-column>

            <el-table-column align="center" prop="date" label="手机号">
              <template slot-scope="scope">
                <el-input @mousewheel.native.prevent type="number" v-model="scope.row.bankePhone" placeholder="非必填"></el-input>
              </template>
            </el-table-column>
          </el-table>

          <el-form-item class="mt20" label="验证码" label-width="80px" prop="googleCode">
            <el-input class="w300" clearable placeholder="打开谷歌验证器查看验证码" v-model="f_data.googleCode" auto-complete="off"></el-input>
          </el-form-item>

        </el-form>
      </div>

      <!-- <div class="red mt10">说明：最多提交5笔代付请求</div> -->
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="m_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button type="primary" :loading="m_loading" @click.native="handel_platformWithdraw">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="提示" :visible.sync="warning_show" width="650px" :close-on-press-escape="false" :close-on-click-modal="false">
      <h3 class="flex flex-m"><i style="color:#E6A23C;font-size:28px" class="el-icon-warning mr20"></i><span style="color: #606266 !important;" > 网络异常，请重新登录系统后查询下操作的订单状态是否成功，以免重复出款，谢谢！</span></h3>
      <span slot="footer" class="dialog-footer">

        <el-button type="primary" @click="handeWarn">确 定</el-button>
      </span>
    </el-dialog>

    <Paging class="mt20 mb30" :pageIndex="m_page.page" :pageSize="m_page.rows" :pageTotal="m_page.total" @changeSize="m_changesize" @changeIndex="m_changeindex"></Paging>
  </div>
</template>

<script>
const MAX_NUM = 10
export default {
  name: 'proxyPay',
  data() {
    return {
      activeName: '1', //1单卡多笔  2多卡多笔
      d_loading: false,
      search_data: {
        channelCode: '', //通道编号
        channelName: '', //通道名称
      },
      m_show: false,
      detail: {}, //详细信息
      batchList_1: [], //单卡
      batchList_2: [], //多卡
      m_loading: false,
      batchList_item: {
        amt: '', //金额
        acctName: '', //姓名
        acctAddr: '', //钱包地址
        idCard: '', //身份证
        bankePhone: '', //手机号
      },
      formData: {
        channelCode: '', //通道编号
        merchantId: '',//登录后返回的商户编号 
        batchList: [], //参考提现接口batchList
        googleCode: '',//谷歌验证码
      },

      f_data: {
        acctName: '',//姓名
        acctAddr:'',//钱包地址
        googleCode: '',//谷歌验证码
        idCard: '', //身份证
        bankePhone: '', //手机号

      },
      _f_data: {},


      m_rules: {
        acctName: [
          { required: true, message: '请输入', trigger: 'blur' }
        ],
        acctAddr: [
          { required: true, message: '请输入', trigger: 'blur' }
        ],
        googleCode: [
          { required: true, message: '请输入', trigger: 'blur' }
        ],
      },

      warning_show: false


    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      this._f_data = this._dep(this.f_data)
      this.getDatalist()

    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api.platformBalance(data).then(res => {
        this.m_list = res.data.records
        this.m_page.total = res.data.total
      }).finally(() => {
        this.m_tableLoading = false
      })
    },


    showDialog(item) {

      //  this.d_loading = true
      this.detail = this._dep(item)
      // let search = {
      //  channelCode:item.channelCode,
      //  rows:10,
      //  page:1
      // }
      // this.m_api.queryChannelInfoPage(search).then(res=>{
      //   console.log(res)
      //   let records = res.data.records
      //   if(records.length){
      //      this.detail = this._dep(records[0]) 
      //   }else{
      //       this.detail = this._dep({}) 
      //   }  
      // }).finally(()=>{
      //    this.d_loading = false
      // })

      this.batchList_1 = new Array(MAX_NUM).fill().map(i => this._dep(this.batchList_item))
      this.batchList_2 = new Array(MAX_NUM).fill().map(i => this._dep(this.batchList_item))
      this.f_data = this._dep(this._f_data)
      this.m_show = true



    },

    handel_platformWithdraw() {
      this.$refs['ruleForm'].validate((valid) => {
        let userData = sessionStorage.userData && JSON.parse(sessionStorage.userData) || {}
        this.formData.merchantId = userData.merchantId
        this.formData.channelCode = this.detail.channelCode
        this.formData.googleCode = this.f_data.googleCode
        if (valid) {
          if (this.activeName == 1) {
            //1单卡多笔
            //2多卡多笔
            let batchList = this.batchList_1.filter((i) => { return i.amt }).map(i => {
              i.acctName = this.f_data.acctName
              i.acctAddr = this.f_data.acctAddr
              i.idCard = this.f_data.idCard
              i.bankePhone = this.f_data.bankePhone
              return i
            })
            this.formData.batchList = JSON.stringify(batchList)
            if (!this.formData.batchList.length) {
              this.m_error('请填写提现信息')
              return
            }

          } else if (this.activeName == 2) {
            let batchList = this.batchList_2.filter((i) => {
              return JSON.stringify(i) != JSON.stringify(this.batchList_item)
            })
            this.formData.batchList = JSON.stringify(batchList)
            if (!this.formData.batchList.length) {
              this.m_error('请填写提现信息')
              return
            }

          }



          this.platformWithdraw()

        }
      })


    },

    platformWithdraw() {

      this.m_loading = true
      this.m_api.platformWithdraw(this.formData).then(res => {
        this.m_loading = false
        this.m_success('操作成功')
        this.m_show = false
        this.getDatalist()
      }).catch(err => {
        this.m_loading = false
        if (!err.response&&err.request) {
          //系统错误
          this.warning_show = true

        }

      })

    },


    handeWarn() {

      this.warning_show = false,
      this.$router.push('/login')
      sessionStorage.clear()
      setTimeout(() => {
        window.location.reload()
      }, 300)

    },








  }
}

</script>
<style lang='scss' scoped>
.dia_box {
  font-size: 14px;
  .flex {
    margin-bottom: 10px;
  }
}
</style>