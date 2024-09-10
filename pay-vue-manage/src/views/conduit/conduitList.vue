<!-- conduitList 代付通道列表-->
<template>
  <div>
    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr20">
        <span class="w80">通道编号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.channelCode"
          clearable
          placeholder="请输入通道编号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w80">通道名称:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.channelName"
          clearable
          placeholder="请输入通道名称"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w80">通道状态:</span>
        <el-select class="w180" size="medium" v-model="search_data.channelStatus" placeholder="请选择">
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in statusList"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>
      <el-button class size="small" icon="el-icon-search" @click.native="m_search" type="primary">搜索</el-button>
    </div>

    <div class="mb20">
      <el-button
        v-if="handelAuto('add')"
        icon="el-icon-circle-plus"
        size="small"
        type="primary"
        @click="m_addForm()"
      >新增代付通道</el-button>
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

      <el-table-column align="center" prop="channelCode" label="通道编号" width="170"></el-table-column>
      <el-table-column align="center" prop="channelName" label="通道名称"></el-table-column>
      <el-table-column align="center" prop="rechargeCost" label="成本费率">
        <template slot-scope="scope">{{scope.row.rechargeCost }}%</template>
      </el-table-column>

      <el-table-column align="center" prop="rechargeCost" label="充值成本(按笔)">
        <template slot-scope="scope">{{scope.row.singleRechargeCost }}元/笔</template>
      </el-table-column>
      <el-table-column align="center" prop="singleCost" label="代付单笔成本">
        <template slot-scope="scope">{{scope.row.singleCost }}元/笔</template>
      </el-table-column>

      <el-table-column align="center" prop="singleCost" label="手续费结算方式">
        <template slot-scope="scope">{{scope.row.channelFeeType | channelFeeTypeFilter}}</template>
      </el-table-column>

      <!--  状态-->
      <el-table-column align="center" label="通道状态">
        <template slot-scope="scope">
          <span
            :class="scope.row.channelStatus=='1'?'green':'red'"
          >{{scope.row.channelStatus | chlStatus}}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作">
        <template slot-scope="scope">
          <!--  编辑 -->
          <div>
          <el-button v-if="handelAuto('edit')" @click.native="m_editForm(scope.row)" type="text">编辑</el-button>
          </div>
          <!-- 删除 -->
          <div>
          <el-button
            v-if="handelAuto('del')"
            style="color:#ff0000"
            @click.native="handelDel(scope.row)"
            type="text"
          >删除</el-button>
           </div>
          <div>
            <el-button
              v-if="handelAuto('status')"
              @click.native="handel_modifyChannelStatus(scope.row)"
              type="text"
            >
              <span
                :class="scope.row.channelStatus=='0'?'green':'red'"
              >{{scope.row.channelStatus=='0'?'启用':'禁用'}}通道</span>
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑新增谈框 -->

    <el-dialog
      :close-on-click-modal="false"
      v-if="m_show"
      :title="(m_isadd?'添加':'编辑')+'代付通道'"
      :visible.sync="m_show"
      width="800px"
    >
      <el-form :model="formData" :rules="m_rules" ref="ruleForm" label-width="120px">
        <div class="flex flex-m">
          <el-form-item class="flex-1" label="通道名称" prop="channelName">
            <el-input
              :disabled="!m_isadd"
              class="w200"
              placeholder="请输入三方代付通道名称"
              v-model="formData.channelName"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <el-form-item class="flex-1" label="通道编号" prop="channelCode">
            <el-input
              :disabled="!m_isadd"
              class="w200"
              placeholder="请输入三方代付通道编号"
              v-model="formData.channelCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>
        </div>

        <div class="flex flex-m">
          <el-form-item class="flex-1" label="代付时间">
            <el-time-picker
              class="w200"
              value-format="HH:mm:ss"
              is-range
              v-model="times"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              placeholder="选择时间范围"
            ></el-time-picker>
          </el-form-item>

          <el-form-item class="flex-1" label="手续费结算" prop="channelFeeType">
            <el-select
              class="w200"
              size="medium"
              v-model="formData.channelFeeType"
              placeholder="请选择"
            >
              <el-option label="请选择结算方式" value></el-option>
              <el-option
                v-for="item in channelFeeList"
                :key="item.value"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
        </div>

        <div class="flex flex-m">
          <el-form-item class="flex-1" label="平台成本费率" prop="rechargeCost">
            <el-input
              class="w200"
              type="number"
              @mousewheel.native.prevent
              placeholder="如0.36%，填：0.36"
              v-model="formData.rechargeCost"
              auto-complete="off"
            ></el-input>
            <span class="ml5">%</span>
          </el-form-item>

          <el-form-item class="flex-1" label="单日限额" prop="dayQuota">
            <el-input
              class="w200"
              type="number"
              @mousewheel.native.prevent
              placeholder="如 10000000"
              v-model="formData.dayQuota"
              auto-complete="off"
            ></el-input>
            <span class="ml5">元</span>
          </el-form-item>
        </div>

        <div class="flex flex-m">
          <el-form-item class="flex-1" label="充值成本(按笔)" prop="singleRechargeCost">
            <el-input
              class="w200"
              type="number"
              @mousewheel.native.prevent
              placeholder="设为0 充值不收取"
              v-model="formData.singleRechargeCost"
              auto-complete="off"
            ></el-input>
            <span class="ml5">元/笔</span>
          </el-form-item>

          <el-form-item class="flex-1" label="单笔最低" prop="singleMinimum">
            <el-input
              class="w200"
              placeholder="单笔最低"
              type="number"
              @mousewheel.native.prevent
              v-model="formData.singleMinimum"
              auto-complete="off"
            ></el-input>
          </el-form-item>
        </div>

        <div class="flex flex-m">
          <el-form-item class="flex-1" label="代付单笔成本" prop="singleCost">
            <el-input
              class="w200"
              type="number"
              @mousewheel.native.prevent
              placeholder="如0.01元/笔，填：0.01"
              v-model="formData.singleCost"
              auto-complete="off"
            ></el-input>
            <span class="ml5">元/笔</span>
          </el-form-item>

          <el-form-item class="flex-1" label="单笔最高" prop="singleHighest">
            <el-input
              class="w200"
              type="number"
              @mousewheel.native.prevent
              placeholder="单笔最高"
              v-model="formData.singleHighest"
              auto-complete="off"
            ></el-input>
          </el-form-item>
        </div>

        <!-- <el-form-item label-width="80px" label="备注">
          <el-input style="width:550px" type="textarea" v-model="formData.remark"></el-input>
        </el-form-item>-->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="m_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button type="primary" :loading="m_loading" @click.native="sureOption">确 定</el-button>
      </div>
    </el-dialog>

     <!-- GOOGLE密钥 删除通道  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除通道"
      :visible.sync="merchant_show_del"
      width="500px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="通道名称" label-width="110px">
            <span>{{merchant_data.channelName}}</span>
          </el-form-item>

          <el-form-item label="通道编号" label-width="110px">
            <span>{{merchant_data.channelCode}}</span>
          </el-form-item>

          <el-form-item label="Google验证码" label-width="110px" prop="googleCode">
            <el-input
              type="number"
              placeholder="输入6位谷歌Google验证码"
              v-model="merchant_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="merchant_show_del = false">关 闭</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="merchant_loding"
          @click.native="delData(merchant_data)"
        >确 定</el-button>
      </div>
    </el-dialog>


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
export default {
  name: "conduitList",
  data() {
    return {
      times: ["00:00:00", "23:59:59"],
      search_data: {
        channelCode: "", //通道编号
        channelStatus: "", //通道状态
        channelName: "" //通道名称
      }, 
    
      formData: {
        channelName: "", //通道名称
        channelCode: "", // 通道编号
        rechargeCost: "", // 充值费率 百分比
        singleCost: "", // 单笔手续费  元
        singleMinimum: "", // 最低提现金额
        singleHighest: "", // 最高提现金额
        channelFeeType: "2", //通道手续费结算方式
        beginWithdrawTime: "", // 提现开始时间  HH:mm:ss 格式
        endWithdrawTime: "", // 最迟提现时间  HH:mm:ss 格式
        remark: "",

        singleRechargeCost: "", //充值成本(按笔) 元
        dayQuota: "" //单日限额 元
      },

      merchant_loding: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      merchant_show_del: false,

      merchant_data:{
        channelName: "", //通道名称
        channelCode: "", // 通道编号
        googleCode:"",
        tableMerchantId:"",
      },

      m_rules: {
        channelName: [{ required: true, message: "请输入", trigger: "blur" }],
        channelCode: [{ required: true, message: "请输入", trigger: "blur" }],
        rechargeCost: [{ required: true, message: "请输入", trigger: "blur" }],
        channelFeeType: [
          { required: true, message: "请选择", trigger: "blur" }
        ],
        singleCost: [{ required: true, message: "请输入", trigger: "blur" }],
        singleMinimum: [{ required: true, message: "请输入", trigger: "blur" }],
        singleHighest: [{ required: true, message: "请输入", trigger: "blur" }],
        singleRechargeCost: [
          { required: true, message: "请输入", trigger: "blur" }
        ],
        dayQuota: [{ required: true, message: "请输入", trigger: "blur" }]
      }
    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this.getDatalist(); 
    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true;
      let data = Object.assign(this.search_data, this.m_page);
      data.payType = 0;
      this.m_api
        .queryChannelInfoPage(data)
        .then(res => {
          //console.log(res)
          this.m_list = res.data.records;
          this.m_page.total = res.data.total;
        })
        .finally(() => {
          this.m_tableLoading = false;
        });
    },

    addData() {
      this.m_loading = true;
      this.formData.beginWithdrawTime = this.times[0];
      this.formData.endWithdrawTime = this.times[1];
      this.formData.payType = 0;
      this.m_api
        .addChannelInfo(this.formData)
        .then(res => {
          this.m_page.page = 1;
          this.m_success("添加成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch(res => {})
        .finally(() => {
          this.m_loading = false;
        });
    },

    m_editForm(row) {
      console.log(row);
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_isadd = false;
      this.m_editData = JSON.parse(JSON.stringify(row));

      this.formData = this.m_utils.coverObj(this.formData, row);
      this.times[0] = this.formData.beginWithdrawTime;
      this.times[1] = this.formData.endWithdrawTime;
      this.formData.id = row.id;
      this.m_show = true;
    },

    //编辑
    editData() {
      this.m_loading = true;
      this.formData.beginWithdrawTime = this.times[0];
      this.formData.endWithdrawTime = this.times[1];
      let from = this._dep(this.formData);
      from.id = this.m_editData.id;
      this.m_api
        .modifyChannelInfo(from)
        .then(res => {
          // 修改成功
          this.m_success("修改成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch(res => {})
        .finally(() => {
          this.m_loading = false;
        });
    },

    //删除
    delData(v) {
      
      this.m_loading = true;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      this.m_api
        .delChannelInfo({
        channelCode: v.channelCode,
        tableMerchantId: tableMerchantId,
        googleCode:v.googleCode })
        .then(res => {
          // 删除成功
          this.m_loading = false;
          this.m_success("删除成功");
          this.getDatalist();
           this.merchant_show_del = false;
        })
        .catch(res => {
          this.m_loading = false;
        });
    },

    //删除通道
    handelDel(row) {
      this.merchant_data.channelName = row.channelName;
      this.merchant_data.channelCode = row.channelCode;
      this.merchant_data.googleCode = "";
      this.d_merchant_loding = false;
      this.merchant_show_del = true;
    },

    handel_modifyChannelStatus(row) {
      let channelStatus = row.channelStatus;
      let txt = "";
      let d = {
        id: row.id,
        channelStatus: ""
      };
      if (channelStatus == 0) {
        //已禁用
        txt = "启用";
        d.channelStatus = 1;
      } else {
        txt = "禁用"; 
        d.channelStatus = 0;
      }
      let msgData = {
        msg: `是否确定 ${txt} 通道 ${row.channelName} ？`
      };
      this.m_confirm(msgData).then(res => {
        if (res) {
          this.modifyChannelStatus(d);
        }
      });
    },

    //
    modifyChannelStatus(data) {
      this.m_api.modifyChannelStatus(data).then(res => {
        //操作成功
        this.m_success("操作成功");
        this.getDatalist();
      });
    }
  }
};
</script>
<style lang='scss' scoped>
.searchBox {
  flex-wrap: wrap;
  flex-flow:flex-wrap;
}
</style>