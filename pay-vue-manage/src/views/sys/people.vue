<!-- 用户管理  people-->
<template>
  <div>
    <div class="mb20">
      <el-button
        v-if="handelAuto('add')"
        icon="el-icon-circle-plus"
        size="small"
        type="success"
        @click="adm_addForm()"
        >新增</el-button
      >
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

      <el-table-column
        align="center"
        prop="userName"
        label="用户名"
      ></el-table-column>
      <el-table-column
        align="center"
        prop="loginName"
        label="登录帐户"
      ></el-table-column>
      <el-table-column align="center" prop="roleName" label="角色">
        <template slot-scope="scope">{{ scope.row.roleName }}</template>
      </el-table-column>

      <el-table-column align="center" label="帐户状态">
        <template slot-scope="scope">
          <span :class="scope.row.status == '0' ? 'green' : 'red'">{{
            scope.row.status | status
          }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作">
        <template slot-scope="scope">
          <el-button
            v-if="handelAuto('edit')"
            @click.native="adm_editForm(scope.row)"
            type="text"
            >编辑</el-button
          >
          <el-button
            v-if="handelAuto('del')"
            style="color: #ff0000"
            @click.native="handelDel(scope.row)"
            type="text"
            >删除</el-button
          >
          <el-button
            v-if="handelAuto('status')"
            @click.native="handel_updateUserStatus(scope.row)"
            type="text"
          >
            <span :class="scope.row.status == '1' ? 'green' : 'red'">{{
              scope.row.status | f_status
            }}</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑/新增管理员框 -->
    <el-dialog
      :close-on-click-modal="false"
      v-if="m_show"
      :title="m_isadd ? '添加' : '编辑'"
      :visible.sync="m_show"
      width="450px"
      style="margin-top: -100px"
    >
      <el-form :model="formData" :rules="m_rules" ref="ruleForm">
        <div class>
          <el-form-item
            class="flex-1"
            label="用户名称"
            label-width="95px"
            prop="userName"
          >
            <el-input
              placeholder="请输入"
              v-model="formData.userName"
              auto-complete="off"
            ></el-input>
          </el-form-item>
          <el-form-item
            class="flex-1"
            label="角色"
            label-width="95px"
            prop="roleId"
          >
            <el-select v-model="formData.roleId" placeholder="请选择">
              <el-option
                v-for="item in roleList"
                :key="item.id"
                :label="item.roleName"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>

          <el-form-item
            v-if="m_isadd"
            class="flex-1"
            label="登陆账号"
            label-width="95px"
            prop="loginName"
          >
            <el-input
              placeholder="请输入"
              v-model="formData.loginName"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <el-form-item
            class="flex-1"
            label="邮箱"
            label-width="95px"
            prop="maillbox"
          >
            <el-input
              placeholder="请输入"
              v-model="formData.maillbox"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <el-form-item
            v-if="m_isadd"
            class="flex-1"
            label="密码"
            label-width="95px"
            prop="passwrod"
          >
            <el-input
              placeholder="请输入"
              v-model="formData.passwrod"
              auto-complete="off"
              type="password"
            ></el-input>
          </el-form-item>

          <el-form-item
            v-if="m_isadd"
            class="flex-1"
            label="谷歌密钥"
            label-width="95px"
            prop="googleKey"
          >
            <el-input
              readonly
              placeholder="google谷歌密钥"
              type="text"
              v-model="formData.googleKey"
              clearable
              auto-complete="off"
            ></el-input>
            <img class="authurl" :src="authurl" alt />
          </el-form-item>
          <el-form-item label="谷歌验证码" label-width="95px" prop="googleCode">
            <el-input
              type="number"
              clearable
              placeholder="谷歌Google验证码"
              v-model="formData.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="95px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="formData.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode(m_isadd)"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item> -->
          <!-- 邮箱二次验证:END -->
        </div>
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

    <!-- GOOGLE 删除系统用户  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除系统用户"
      :visible.sync="merchant_show_del"
      width="500px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="系统用户名称" label-width="110px">
            <span>{{ merchant_data.userName }}</span>
          </el-form-item>

          <el-form-item label="系统用户编号" label-width="110px">
            <span>{{ merchant_data.merchantId }}</span>
          </el-form-item>

          <el-form-item
            label="Google验证码"
            label-width="110px"
            prop="googleCode"
          >
            <el-input
              type="number"
              clearable
              placeholder="输入6位谷歌Google验证码"
              v-model="merchant_data.googleCode"
              auto-complete="off"
            ></el-input>
          </el-form-item>

          <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="110px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="merchant_data.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode('adminDel')"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item> -->
          <!-- 邮箱二次验证:END -->
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

    <!-- GOOGLE 修改系统用户状态  -->
    <el-dialog
      v-if="merchant_show_status"
      title="修改系统用户状态"
      :visible.sync="merchant_show_status"
      width="500px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="系统用户名称" label-width="110px">
            <span>{{ merchant_data.userName }}</span>
          </el-form-item>

          <el-form-item label="系统用户编号" label-width="110px">
            <span>{{ merchant_data.merchantId }}</span>
          </el-form-item>

          <el-form-item label="操作" label-width="110px">
            <span>{{ merchant_data.statusText }}</span>
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

          <!-- 邮箱二次验证:START -->
          <!-- <el-form-item label="邮箱验证码" label-width="110px" prop="emailCode">
            <el-input
              type="text"
              clearable
              placeholder="邮箱验证码"
              v-model="merchant_data.emailCode"
              auto-complete="off"
              class="w150"
            ></el-input>
            <el-button
              type="primary"
              class="ml20"
              @click="sendCode('adminStatus')"
              :disabled="sendNum != 0"
              :loading="get_loading"
            >
              <span v-if="sendNum != 0">{{ sendNum }}秒后重新</span>获取<span
                v-if="sendNum == 0"
                >验证码</span
              >
            </el-button>
          </el-form-item> -->
          <!-- 邮箱二次验证:END -->
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="merchant_show_status = false">关 闭</el-button>
        <!-- 确 定 -->
        <el-button
          type="primary"
          :loading="merchant_loding"
          @click.native="updateUserStatus(merchant_data)"
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
import * as api from "@/api/public";

export default {
  name: "people",
  data() {
    return {
      search_data: {},

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

      roleList: [], //角色列表
    };
  },
  created() {
    this.userData =
      (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
    this.init();
  },
  methods: {
    init() {
      this.getDatalist();
      this.queryUserRoleListPage();
    },

    //邮箱v
    sendCode(v) {
      this.get_loading = true;
      var codeType = v ? "adminAdd" : "adminEdit";
      codeType = v == "adminDel" ? "adminDel" : codeType;
      codeType = v == "adminStatus" ? "adminStatus" : codeType;
      console.log("Email Code:" + codeType);
      this.m_api
        .sendEmailCode({
          loginName: this.userData.loginName,
          codeType: codeType,
        })
        .then((res) => {
          this.get_loading = false;
          this.m_success("已将验证码发送到邮箱:" + res.data.mail);
          this.intervalCodeTime();
          //return res;
        })
        .catch((res) => {
          this.get_loading = false;
        });
    },

    intervalCodeTime() {
      this.time && clearInterval(this.time);
      this.sendNum = 60; //60秒可重发 不可太频繁
      this.time = setInterval(() => {
        this.sendNum = this.sendNum - 1;
        if (this.sendNum <= 0) {
          this.sendNum = 0;
          clearInterval(this.time);
        }
      }, 1000);
    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.search_data.tableMerchantId = userData.merchantId || 0;
      let data = Object.assign(this.search_data, this.m_page);
      api
        .getSysUserList(data)
        .then((res) => {
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

      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId || 0;
      api
        .addSystemUser(this.formData)
        .then((res) => {
          this.m_page.page = 1;
          this.m_success("添加成功");
          this.m_show = false;
          this.getDatalist();
        })
        .catch((res) => {})
        .finally(() => {
          this.m_loading = false;
        });
    },

    //编辑
    editData() {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      this.formData.tableMerchantId = userData.merchantId || 0;

      this.m_loading = true;
      let from = this._dep(this.formData);
      from.merchantName = this.formData.userName;
      api
        .updateSysUser(from)
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
    delData(v) {
      this.m_loading = true;
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      api
        .delSysUser({
          merchantId: v.merchantId,
          tableMerchantId: tableMerchantId,
          googleCode: v.googleCode,
          emailCode: v.emailCode, //邮箱
        })
        .then((res) => {
          // 删除成功
          this.m_loading = false;
          this.m_success("删除成功");
          this.getDatalist();
          this.merchant_show_del = false;
        })
        .catch((res) => {
          console.log(res);
          this.m_loading = false;
          this.merchant_show_del = false;
        });
    },

    handel_updateUserStatus(row) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;
      this.merchant_data.tableMerchantId = tableMerchantId;
      this.merchant_data.status = row.status == 0 ? 1 : 0;
      this.merchant_data.userName = row.userName;
      this.merchant_data.merchantId = row.merchantId;
      this.merchant_data.googleCode = "";
      this.d_merchant_loding = false;
      this.merchant_show_status = true;
      this.merchant_data.statusText = row.status == 0 ? "禁用" : "启用";
      // let txt = "";
      // let d = {
      //   id: row.id,
      //   status: ""
      // };
      // if (status == 0) {
      //   //用禁用
      //   txt = "禁用";
      //   d.status = 1;
      // } else {
      //   txt = "禁用";
      //   //要启用
      //   d.status = 0;
      // }
      // let msgData = {
      //   msg: `是否确定 ${txt} 用户 ${row.userName} ？`
      // };
      // this.m_confirm(msgData).then(res => {
      //   if (res) {
      //     this.updateUserStatus(d);
      //   }
      // });
    },

    //
    updateUserStatus(data) {
      api.updateUserStatus(data).then((res) => {
        //操作成功
        this.merchant_show_status = false;
        this.m_success("操作成功");
        this.getDatalist();
      });
    },

    queryUserRoleListPage() {
      api.queryUserRoleListPage().then((res) => {
        this.roleList = res.data;
      });
    },

    adm_addForm() {
      //console.log("新增ADMIN");
      let loginName =
        this.formData.loginName != null ? this.formData.loginName : "";
      this.getNewGoogleAuthUrl(loginName);
      this.m_isadd = true; //m_isadd?'添加':'编辑'
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_show = true;
    },

    //编辑用户overwrited index.js method
    adm_editForm(row) {
      //console.log("编辑ADMIN" + row.merchantId);
      this.formData = JSON.parse(JSON.stringify(this._formData));
      this.m_isadd = false;
      // 编辑关闭GOOGLE
      this.m_editData = JSON.parse(JSON.stringify(row));
      this.formData = this.m_utils.coverObj(this.formData, row);
      this.formData.tableMerchantId = row.merchantId;
      this.m_show = true;
    },

    // 新增google key
    getNewGoogleAuthUrl(v) {
      this.m_api
        .getNewGoogleAuthUrl({ v })
        .then((res) => {
          let authKey = res.data.googleKey;
          let authurl = res.data.googleAuthUrl;
          this.formData.googleKey = authKey;
          this.formData.googleUrl = authurl;
          //console.log("googleKey:" + authKey);
          var opts = {
            errorCorrectionLevel: "H",
            type: "image/jpeg",
            rendererOpts: {
              quality: 1,
            },
          };
          let that = this;
          qrcode.toDataURL(authurl, opts, function (err, url) {
            if (err) throw err;
            that.authurl = url;
          });
        })
        .catch((err) => {
          //console.log(err);
        });
    },

    //删除用户
    handelDel(row) {
      this.merchant_data.userName = row.userName;
      this.merchant_data.merchantId = row.merchantId;
      this.merchant_data.googleCode = "";
      this.merchant_data.emailCode = "";
      this.d_merchant_loding = false;
      this.merchant_show_del = true;
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
</style>