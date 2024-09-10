<!-- menu菜单 -->
<template>
  <div>
    <el-button @click="append()" class="mt20 ml20" size="small" type="success">添加</el-button>
    <div class="m_box">
      <el-tree
        v-loading="loading"
        :props="props"
        :data="dataTree"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
      >
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span>
            {{ node.label }}
            <el-tag class="ml5" type="success" size="mini" v-if="data.isMenu==3">按钮</el-tag>
          </span>
          <span>
            <el-button v-if="data.isMenu!=3" type="text" size="mini" @click="() => append(data)">添加</el-button>
            <el-button type="text" size="mini" @click="() => update(node, data)">编辑</el-button>
            <el-button type="text" size="mini" @click="() => handelDel(node, data)">删除</el-button>
          </span>
        </span>
      </el-tree>
    </div>

    <!-- 编辑新增谈框 -->
    <el-dialog
      :close-on-click-modal="false"
      v-if="m_show"
      :title="(m_isadd?'添加':'编辑')+handelMenuName"
      :visible.sync="m_show"
      width="500px"
    >
      <el-form :model="formData" :rules="m_rules" ref="ruleForm">
        <el-form-item class="flex-1" label="资源名称" label-width="80px" prop="menuName">
          <el-input placeholder="请输入" v-model="formData.menuName" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="资源路径" label-width="80px" prop="menuUrl">
          <el-input placeholder="请输入" v-model="formData.menuUrl" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="权限标识" label-width="80px" prop="identifier">
          <el-input placeholder="请输入" v-model="formData.identifier" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item class="flex-1" label="菜单类型" label-width="80px" prop="isMenu">
          <el-radio-group v-model="formData.isMenu">
            <el-radio label="1">一级菜单</el-radio>
            <el-radio label="2">二级菜单</el-radio>
            <el-radio label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="m_show = false">取 消</el-button>
        <el-button type="primary" :loading="btnLoding" @click.native="sureOption">确 定</el-button>
      </div>
    </el-dialog>

    <!-- GOOGLE密钥系统用户  -->
    <el-dialog
      v-if="merchant_show_del"
      title="删除菜单"
      :visible.sync="merchant_show_del"
      width="500px"
    >
      <div v-loading="d_merchant_loding">
        <el-form :model="merchant_data">
          <el-form-item label="菜单名称" label-width="110px">
            <span>{{merchant_data.menuName}}</span>
          </el-form-item>

          <el-form-item label="菜单ID" label-width="110px">
            <span>{{merchant_data.id}}</span>
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
          @click.native="remove(merchant_data)"
        >确 定</el-button>
      </div>
    </el-dialog>
    
  </div>
</template>

<script>
import * as utils from "@/utils/index";
import * as api from "@/api/public";
export default {
  name: "menu",
  data() {
    return {
      dataTree: [],
      loading: false,
      btnLoding: false,

      formData: {
        id: "", //
        menuName: "", // 资源名称
        menuUrl: "", //资源路径
        identifier: "", //权限标识
        isMenu: "", //1:一级菜单  2:二级菜单  3:按钮
        pMenuId: "" //上级资源id
      },

      merchant_data: {
        id: "", //
        menuName: "",
        googleCode: ""
      },

      merchant_loding: false,
      d_merchant_loding: false,
      merchant_show_google: false, //google
      merchant_show_del: false,

      handelMenuName: "", //操作菜单名

      props: {
        label: "menuName",
        children: "childrenList"
      },
      m_rules: {}
    };
  },
  created() {
    this.init();
  },
  methods: {
    //初始化方法
    init() {
      this.getDataList();
    },

    getDataList() {
      this.loading = true;
      api
        .queryPermissionInfoList()
        .then(res => {
          console.log(res);
          this.dataTree = res.data;
          this.loading = false;
        })
        .catch(err => {
          this.loading = false;
        });
    },

    append(data) {
      // console.log(data)
      this.formData = this._dep(this._formData);
      if (data) {
        this.handelMenuName = data.menuName;
        this.formData.isMenu = data.isMenu;
        this.formData.pMenuId = data.id;
        this.formData.isMenu = this.formData.isMenu * 1 + 1 + "";
      } else {
        //一级菜单
        this.formData.isMenu = "1";
        this.handelMenuName = "根目录";
      }
      this.handelMenuName = " " + this.handelMenuName + " 下权限";
      this.m_isadd = true;
      this.m_show = true;
    },

    //删除用户
    handelDel(node, row) {
      this.merchant_data.id = row.id;
      this.merchant_data.menuName = row.menuName;
      this.d_merchant_loding = false;
      this.merchant_show_del = true;
    },

    remove(data) {
      let userData =
        (sessionStorage.userData && JSON.parse(sessionStorage.userData)) || {};
      let tableMerchantId = userData.merchantId || 0;

      api.delPermissionInfo({
          id: data.id,
          tableMerchantId: tableMerchantId,
          googleCode: data.googleCode
        })
        .then(res => {
          this.m_success("删除成功");
          this.getDataList();
          this.merchant_show_del = false;
        });
      //console.log(node, data)
      // this.$confirm(`此操作将永久删除${data.menuName}, 是否继续?`, '提示', {
      //   confirmButtonText: '确认',
      //   cancelButtonText: '取消',
      //   type: 'warning'
      // }).then(() => {

      // }).catch(() => {
      //   //取消删除
      // })
    },

    update(node, data) {
      this.m_isadd = false;
      this.handelMenuName = data.menuName;
      this.formData = this._dep(this._formData);
      this.formData = utils.coverObj(this.formData, data);
      this.m_show = true;
    },

    sureOption() {
      if (this.m_isadd) {
        //新增
        this.btnLoding = true;
        api
          .addPermissionInfo(this.formData)
          .then(res => {
            this.btnLoding = false;
            this.m_success("操作成功");
            this.m_show = false;
            this.getDataList();
          })
          .catch(err => {
            this.btnLoding = false;
          });
      } else {
        // 修改
        this.btnLoding = true;
        api
          .modifyPermissionInfo(this.formData)
          .then(res => {
            this.btnLoding = false;
            this.m_success("操作成功");
            this.m_show = false;
            this.getDataList();
          })
          .catch(err => {
            this.btnLoding = false;
          });
      }
    }
  }
};
</script>
<style lang='scss' scoped>
.m_box {
  width: 400px;
  padding: 10px;
  border: 1px solid #eee;
  margin: 20px;
}
</style>

<style lang='scss' >
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
</style>