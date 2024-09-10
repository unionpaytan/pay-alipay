<!-- coinDepositChannelList -->
<template>
  <div>
    <!-- <div class="flex flex-m tc topbox">
      <div class="flex-1">
        <div>总剩余手续费(USDT)</div>
        <div  class="num">{{parseFloat(countMap.amt).toFixed(2) || 0.00}}</div>
      </div>
    </div> -->

    <!-- * 
         * @搜索栏
         *
    -->
    <div class="flex flex-m mb20 searchBox">
      <div class="flex flex-m mr20">
        <span class="w120">代收通道编号:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.channelCode"
          clearable
          placeholder="三方代收通道编号"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w120">代收通道名称:</span>
        <el-input
          size="medium"
          class="w180"
          v-model="search_data.channelName"
          clearable
          placeholder="三方代收通道名称"
        ></el-input>
      </div>
      <div class="flex flex-m mr20">
        <span class="w120">代收通道状态:</span>
        <el-select
          class="w180"
          size="medium"
          v-model="search_data.status"
          placeholder="请选择"
        >
          <el-option label="请选择状态" value></el-option>
          <el-option
            v-for="item in channelStatusType"
            :key="item.value"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
      </div>

      <div class="flex flex-m mr20">
        <el-button
          class
          size="small"
          icon="el-icon-search"
          @click.native="m_search"
          type="primary"
          >搜索</el-button
        >
      </div>
    </div>

    <!-- * 
         * @操作栏
         *
    -->
    <div class="mb20">
      <el-button
        v-if="handelAuto('add')"
        icon="el-icon-circle-plus"
        size="small"
        type="success"
        @click="mer_addForm()"
        >新增代收通道</el-button
      >
      <!-- <el-button icon="el-icon-delete" type="danger" size="small" @click="delMore">删除</el-button> -->
      <el-button
        v-if="handelAuto('status')"
        @click="batchModifyCoinWithdrawChannelInfoStatus(1)"
        icon="el-icon-circle-check"
        size="small"
        type="primary"
        >启用</el-button
      >
      <el-button
        v-if="handelAuto('status')"
        @click="batchModifyCoinWithdrawChannelInfoStatus(0)"
        icon="el-icon-circle-close"
        size="small"
        type="warning"
        >禁用</el-button
      >
    </div>

    <!-- * 
         * @列表
         *
    -->
    <el-table
      :data="m_list"
      v-loading="m_tableLoading"
      @selection-change="m_handleSelectionChange"
      @current-change="m_handleCurrentChange"
      class="mtable"
      :stripe="true"
      tooltip-effect="dark"
      :highlight-current-row="true"
    >
      <!-- prop 数据库里的字段 -->
      <el-table-column type="selection" width="46"></el-table-column>
      <el-table-column
        align="center"
        prop="channelCode"
        label="通道编号"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="channelName"
        label="通道名称"
      ></el-table-column>
      <el-table-column align="center" prop label="通道费率 %">
        <template slot-scope="scope">
          <span>{{ scope.row.channelRate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        prop="channelSingle"
        label="单笔手续费(元/笔)"
        width="160"
      ></el-table-column>

      <el-table-column
        align="center"
        prop="coinNum"
        label="收款码数量"
      ></el-table-column>
 
     <!-- <el-table-column align="center" pro label="PESO汇率">
         <template slot-scope="scope">
          <span>{{scope.row.usdtLiveFixed | usdtLiveFixedFilter}}</span>
          <span v-if="scope.row.usdtLiveFixed == '1'"><br>{{scope.row.usdtLive}}</span>
        </template>
      </el-table-column> -->

      <!--  状态-->
      <el-table-column align="center" label="状态" width="110">
        <template slot-scope="scope">
          <span :class="scope.row.status == '1' ? 'green' : 'red'">{{
            scope.row.status | coinStatus
          }}</span>
        </template>
      </el-table-column>

       <el-table-column
        align="center"
        prop="remark"
        label="标识"
      ></el-table-column>

      <el-table-column align="center" label="操作" fixed="right">
        <template slot-scope="scope">
          <div>
            <el-button
              v-if="handelAuto('edit')"
              @click.native="m_editForm(scope.row)"
              type="text"
              >编辑</el-button
            >
          </div>
          <!-- <div>
            <el-button 
              v-if="handelAuto('edit')"
              @click.native="handelBalance(scope.row)"
              type="text"
            >手续费充值</el-button>
          </div> -->
          <div>
            <el-button
              v-if="handelAuto('del')"
              style="color: #ff0000"
              @click.native="handelDel(scope.row)"
              type="text"
              >删除</el-button
            >
          </div>
          <div>
            <el-button
              v-if="handelAuto('edit')"
              @click.native="handel_modifyChannelStatus(scope.row)"
              type="text"
            >
              <span :class="scope.row.status == '0' ? 'green' : 'red'"
                >{{ scope.row.status == "1" ? "禁用" : "启用" }}通道</span
              >
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- * 
         * @新增/编辑 弹框
         *
    -->
    <el-dialog
      :close-on-click-modal="false"
      v-if="m_show"
      :title="(m_isadd ? '添加' : '编辑') + '代收通道'"
      :visible.sync="m_show"
      style="margin-top: -100px"
    >
      <el-form :model="formData" :rules="m_rules" ref="ruleForm">
        <el-form-item
          class="flex-1"
          label="通道名称"
          label-width="100px"
          clearable
          prop="channelName"
        >
          <!-- :disabled="!m_isadd" -->
          <el-input
            placeholder="代收通道名称"
            clearable
            v-model="formData.channelName"
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item
          class="flex-1"
          label="代收费率 %"
          label-width="100px"
          clearable
          prop="channelRate"
        >
          <el-input
            placeholder="商户代收汇率若为1.0%,填:1.0"
            v-model="formData.channelRate"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item
          class="flex-1"
          label="单笔手续费"
          label-width="100px"
          clearable
          prop="channelSingle"
        >
          <el-input
            placeholder="商户手续费若为 0.1USDT,填:0.1 设为0则不收取"
            v-model="formData.channelSingle"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

     <el-form-item
          class="flex-1"
          label="汇率采集方式"
          label-width="100px"
          clearable
          prop="usdtLiveFixed"
        >
         <el-select
          class="w210"
          size="medium"
          v-model="formData.usdtLiveFixed"
          placeholder="请选择PESO汇率采集方式"
        >
          <el-option label="请选择PESO汇率采集方式" value></el-option>
          <el-option
            v-for="item in usdtLiveFixedType"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          ></el-option>
        </el-select>
        </el-form-item>

        <el-form-item
          class="flex-1"
          label="PESO汇率"
          label-width="100px"
          clearable
          prop="usdtLive"
          v-if="formData.usdtLiveFixed == '1'"
        >
          <el-input
            placeholder="6.48"
            v-model="formData.usdtLive"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>


        <el-form-item
          class="flex-1"
          label="通道标识"
          label-width="100px"
          clearable
          prop="remark"
        >
          <el-input
            placeholder="通道标识"
            v-model="formData.remark"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item
          class="flex-1"
          label="联系人"
          label-width="100px"
          clearable
          prop="contact"
        >
          <el-input
            placeholder="联系人"
            v-model="formData.contact"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item
          class="flex-1"
          label="邮箱"
          label-width="100px"
          clearable
          prop="email"
        >
          <el-input
            placeholder="邮箱"
            v-model="formData.email"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>

        <el-form-item
          class="flex-1"
          label="手机号"
          label-width="100px"
          prop="phone"
        >
          <el-input
            placeholder="手机号"
            type="number"
            @mousewheel.native.prevent
            v-model="formData.phone"
            clearable
            auto-complete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" style="margin-top: -50px">
        <!-- 取 消 -->
        <el-button @click="m_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="m_loading"
          @click.native="sureOption"
          >确 定</el-button
        >
      </div>
    </el-dialog> 

    <!-- 手续费充值  -->
    <el-dialog v-if="balance_show" title="手续费充值" :visible.sync="balance_show">
      <div v-loading="d_balance_loading">
        <el-form :model="balance_data" :rules="balanceForm_rules" ref="balanceForm">
          <div class="flex flex-m">
            <el-form-item class="flex-1" label="通道编号" label-width="120px">
              <span>{{balance_data.cardChannelCode}}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m">
            <el-form-item class="flex-1" label="通道名称" label-width="120px">
              <span>{{balance_data.chlName}}</span>
            </el-form-item>
          </div>

          <div class="flex flex-m">
            <el-form-item class="flex-1" label="手续费余额" label-width="120px">
              <span class="red">{{balance_data.chlFeeChargeAmtRemain}}元</span>
            </el-form-item>
          </div>

          <el-form-item label="充值金额" label-width="120px" prop="chlFeeChargeAmt">
            <div class="flex flex-m">
              <el-input
                @mousewheel.native.prevent
                type="number"
                style="width:110px;"
                placeholder="充值金额"
                v-model="balance_data.chlFeeChargeAmt"
                auto-complete="off"
              ></el-input>
              <div class="red ml15 fs18 w100">{{balance_data.chlFeeChargeAmt | convertCurrency}}</div>
            </div>
          </el-form-item>

          <el-form-item label="google验证码" label-width="120px" prop="googleCode">
            <el-input placeholder="请输入" v-model="balance_data.googleCode" auto-complete="off"></el-input>
          </el-form-item>

          <el-form-item label-width="120px" label="备注">
            <el-input style type="textarea" v-model="balance_data.remark"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="balance_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button type="primary" :loading="balance_loading" @click.native="balanceSubmit">确 定</el-button>
      </div>
    </el-dialog>

    <!-- GOOGLE密钥 删除通道  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除通道"
      :visible.sync="merchant_show_del"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="通道名称" label-width="110px">
            <span>{{ merchant_data.channelName }}</span>
          </el-form-item>

          <el-form-item label="通道编号" label-width="110px">
            <span>{{ merchant_data.channelCode }}</span>
          </el-form-item>

          <el-form-item
            label="Google验证码"
            label-width="110px"
            prop="googleCode"
          >
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
          >确 定</el-button
        >
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
import qrcode from "qrcode";
import moment from "moment";

export default {
  name: "coinWithdrawChannelList",
  data() {
    return {
      search_data: {
        channelName: "", //通道编号
        channelCode: "", //商户状态
        contact: "", //通道名称
        email: "", // 邮箱
        phone: "", // 手机号
        status: "",
      },

      countMap: {
        amt: 0.0,
      },

      highestAmt: 0,

      formData: {
        usdtLive:6.48,
        usdtLiveFixed:'0',
        channelRate: "", //下发费率
        channelSingle: "", //下发单笔
        mintFee: "",
        channelName: "", //通道名称
        email: "", // 邮箱
        phone: "", // 手机号
        contact: "", // 联系人
        remark: "",
      },

      merchant_loding: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      merchant_show_del: false,

      merchant_data: {
        channelName: "", //通道名称
        channelCode: "", // 通道编号
        googleCode: "",
        tableMerchantId: "",
      },

      balance_show: false,
      d_balance_loading: false,
      balance_loading: false,
      balance_info: {},
      _balance_data: {},
      balance_data: {
        merchantId: "", //管理员id
        contact: "",
        channelCode: "", //登陆后接口返回的商户ID
        channelName: "", //商户列表中 商户名称
        amt: "", //手续费余额
        rechargeAmt: "", //充值金额
        remark: "", //备注
        googleCode: "", //google验证码
      },

      merchant_show: false,
      agent_show: false, //代理费率
      merchant_loding: false,
      d_merchant_loding: false,
      _channel_data: {},

      channel_data: {
        contact: "", //联系人
        channelName: "", //通道名称
        channelCode: "", //通道编号
      },

      m_rules: {
        channelRate: [{ required: true, message: "请输入", trigger: "blur" }],
        channelSingle: [{ required: true, message: "请输入", trigger: "blur" }],
        channelName: [{ required: true, message: "请输入", trigger: "blur" }],
      },

      //手续费 rules
      balanceForm_rules: {
        amt: [{ required: true, message: "请输入", trigger: "blur" }],
        googleCode: [{ required: true, message: "请输入", trigger: "blur" }],
      },
    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this._balance_data = this._dep(this.balance_data);
      this._channel_data = this._dep(this.channel_data);
      this.getDatalist(); //获取通道列表数据信息
    },

    //获取通道列表数据信息
    getDatalist() {
      //console.log("查询" + JSON.stringify(this.search_data))
      this.m_tableLoading = true;
      let data = Object.assign(this.search_data, this.m_page);
      this.m_api
        .queryCoinDepositChannelList(data)
        .then((res) => {
          this.m_list = res.data.pages.records; //记录
          this.m_page.total = res.data.pages.total;

          //总余额
          this.countMap = res.data.countMap;
        })
        .finally(() => {
          this.m_tableLoading = false;
          this.m_checkData = [];
        });
    },

    //提交服务器 加入数据
    addData() {
      this.m_loading = true;
      this.m_api
        .registerCoinDepositChannel(this.formData)
        .then((res) => {
          this.m_page.page = 1;
          this.m_success("添加通道成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch((res) => {})
        .finally(() => {
          this.m_loading = false;
        });
    },

    /***
     *
     * form click
     *
     */
    //m_addForm 会自动overwrited index.js的方法 click
    mer_addForm() {
      let channelName =
        this.formData.channelName != null ? this.formData.channelName : "";
      this.m_isadd = true; //m_isadd?'添加':'编辑'
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_show = true;
    },

    //编辑通道overwrited index.js method
    m_editForm(row) {
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_isadd = false;
      this.m_editData = JSON.parse(JSON.stringify(row));
      this.formData = this.m_utils.coverObj(this.formData, row);
      this.formData.channelCode = row.channelCode;
      this.m_show = true;
    },

    //编辑 send to server
    editData() {
      this.m_loading = true;
      let form = this._dep(this.formData);
      form.id = this.m_editData.id;
      this.m_api
        .editCoinDepositChannel(form)
        .then((res) => {
          // 修改成功
          this.m_success("修改成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch((res) => {})
        .finally(() => {
          this.m_loading = false;
        });
    },

    //删除
    delData(v = {}) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      this.m_loading = true;
      this.m_api
        .delCoinDepositChannel({
          channelCode: v.channelCode,
          tableMerchantId: tableMerchantId,
          googleCode: v.googleCode,
        })
        .then((res) => {
          // 删除成功
          this.m_loading = false;
          this.m_success("删除成功");
          this.getDatalist();
          this.merchant_show_del = false;
        })
        .catch((res) => {
          this.m_loading = false;
        });
    },

    //参数多条
    delMore() {
      let m_checkData = this.m_checkData;
      let len = m_checkData.length;
      if (!len) {
        this.m_warning("请勾选数据");
        return;
      }
      this.$confirm(`此操作将永久删除${len}个商户, 是否继续?`, "提示", {
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          let ids = m_checkData.map((i) => i.channelCode).join(",");
          console.log(ids);
          // this.delData(row.id, row)
        })
        .catch(() => {
          //取消删除
        });
    },

    handel_modifyChannelStatus(row) {
      let status = row.status;
      let txt = "";
      let data = {
        channelCode: row.channelCode,
        status: "",
      };
      if (status == 0) {
        txt = "启用";
        data.status = 1;
      } else {
        txt = "禁用";
        //要启用
        data.status = 0;
      }
      let msgData = {
        msg: `是否确定 ${txt} 通道 ${row.channelName} ？`,
      };
      this.m_confirm(msgData).then((res) => {
        if (res) {
          this.modifyCoinDepositChannelStatus(data);
        }
      });
    },

    //返回通道内已分配卡的最高余额
    gettingHighestAmtBychannelCode(v) {
      //异步操作
      this.m_api
        .gettingHighestAmtBychannelCode({ channelCode: v })
        .then((res) => {
          console.log(res.data);
          return res.data;
        });
    },

    modifyCoinWithdrawChannelStatus(data) {
      this.m_api.modifyCoinWithdrawChannelStatus(data).then((res) => {
        //操作成功
        this.m_success("操作成功");
        this.getDatalist();
      });
    },

    //批量修改状态 status 0 启用 1禁用
    batchModifyCoinWithdrawChannelInfoStatus(status) {
      let m_checkData = this.m_checkData;
      let len = m_checkData.length;
      if (!len) {
        this.m_warning("请勾选数据");
        return;
      }
      let ids = m_checkData.map((item) => item.channelCode).join(",");
      let txt = status == "1" ? "启用" : "禁用";
      let form = {
        status,
        channelCodes: ids, //channelCode 列表
      };

      this.m_confirm({ msg: `是否确 批量 ${txt} 通道` }).then((res) => {
        if (res) {
          this.m_api
            .batchModifyCoinWithdrawChannelInfoStatus(form)
            .then((res) => {
              this.m_success("操作成功");
              this.getDatalist();
            });
        }
      });
    },

    handelBalance(row) {
      let userData = (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.balance_data = this._dep(this._balance_data);
      this.balance_data.channelName = row.channelName;
      this.balance_data.amt = row.amt; //当前手续费
      this.balance_data.channelCode = row.channelCode;
      this.balance_data.contact = row.contact;

      this.balance_data.merchantId = userData.merchantId; //cookie中读取操作管理员的商户id

      this.balance_show = true;
      //this.d_balance_loading = true;
      // this.m_api
      //   .queryMerchantChannel({ channelCode: row.channelCode })
      //   .then(res => {
      //     console.log(res);
      //     this.balance_info = this._dep(res.data);
      //     this.d_balance_loading = false;
      //   })
      //   .catch(err => {
      //     this.d_balance_loading = false;
      //   });
    },

    balanceSubmit() {
      this.$refs["balanceForm"].validate((valid) => {
        if (valid) {
          this.balance_loading = true;
          this.m_api
            .coinChannelRecharge(this.balance_data)
            .then((res) => {
              this.getDatalist();
              this.balance_loading = false;
              this.m_success(
                "手续费充值【" +
                  parseFloat(this.balance_data.rechargeAmt).toFixed(2) +
                  "】成功,请核对充值金额并检查【手续费充值流水】"
              );
              // this.m_success('请二次核对充值金额【'+this.balance_data.chlFeeChargeAmt+'】');
              this.balance_show = false;
            })
            .catch((res) => {
              this.m_error("手续费充值失败,请检查【手续费充值流水】");
              this.balance_loading = false;
            });
        }
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
  },
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
  flex-flow: flex-wrap;
}
</style>
<style lang='scss'>
.el-dialog {
  width: 525px;
  margin-top: -5px;
}
@media screen and (max-width: 414px) {
  .el-dialog {
    width: 355px;
    margin-top: -15px;
  }
  .dialog-footer {
    margin-top: -38px;
  }
}
</style>