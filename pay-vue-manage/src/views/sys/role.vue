<!-- 角色管理 role -->
<template>
  <div>

    <div class="mb20">
      <el-button  v-if="handelAuto('add')"  icon="el-icon-circle-plus" size="small" type="success" @click="m_addForm()">新增</el-button>
    </div>

    <el-table v-loading="m_tableLoading" @selection-change="m_handleSelectionChange" @current-change="m_handleCurrentChange" class="mtable" :stripe="true" tooltip-effect="dark" :highlight-current-row="true" :data="m_list">
      <!-- <el-table-column type="selection" width="55"></el-table-column> -->

      <el-table-column width="150" align="center" prop="roleName" label="角色">
        <template slot-scope="scope">{{scope.row.roleName }}</template>
      </el-table-column>
      <el-table-column align="center" prop="remark" label="权限说明"></el-table-column>

      <el-table-column align="center" label="操作" width="200">
        <template slot-scope="scope">
          <el-button  v-if="handelAuto('auth')"  @click.native="show_permission(scope.row)" type="text">设置权限</el-button>
          <el-button  v-if="handelAuto('del')"  style="color:#ff0000" @click.native="m_oneDel(scope.row)" type="text">删除</el-button>

        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑新增谈框 -->

    <el-dialog :close-on-click-modal="false" v-if="m_show" :title="(m_isadd?'添加':'编辑')" :visible.sync="m_show" width="450px">
      <el-form :model="formData" :rules="m_rules" ref="ruleForm">
        <div class="">
          <el-form-item class="flex-1" label="角色" label-width="80px" prop="roleName">
            <el-input placeholder="请输入" v-model="formData.roleName" auto-complete="off"></el-input>
          </el-form-item>

          <el-form-item class="flex-1" label="说明" label-width="80px" prop="remark">
            <el-input type="textarea" placeholder="请输入" v-model="formData.remark" auto-complete="off"></el-input>
          </el-form-item>

        </div>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <!-- 取 消 -->
        <el-button @click="m_show = false">取 消</el-button>
        <!-- 确 定 -->
        <el-button type="primary" :loading="m_loading" @click.native="sureOption">确 定</el-button>
      </div>
    </el-dialog>
    

      <set-permission  v-model="permission_show"  :data="permission_data" />

   

  </div>
</template>

<script>
import setPermission from './setPermission'
import * as api from '@/api/public'
export default {
  components: { setPermission },
  name: 'role',
  data() {
    return {
      permission_show: false,
      formData: {
        roleName: '', //
        remark: '', // 
      },

      m_rules: {
        roleName: [
          { required: true, message: '请输入', trigger: 'blur' }
        ],
      },

      permission_data:{
        roleId:''
      }




    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      this.getDatalist()

    },

    //获取数据信息
    getDatalist() {
      this.m_tableLoading = true

      api.queryUserRoleListPage().then(res => {
        //console.log(res)
        this.m_list = res.data


      }).finally(() => {
        this.m_tableLoading = false
      })
    },

    addData() {
      this.m_loading = true
      api.addUserRole(this.formData).then(res => {

        this.m_success('添加成功')
        this.m_show = false
        this.getDatalist()
      }).catch(res => {

      }).finally(() => {
        this.m_loading = false
      })
    },





    //删除
    delData(id) {
      let m_delId = id
      this.m_loading = true

      api.delUserRole({ id: m_delId }).then(res => {
        // 删除成功
        this.m_loading = false
        this.m_success('删除成功')
        this.getDatalist()
      }).catch(res => {
        console.log(res)
        this.m_loading = false
      })
    },

    show_permission(row){
      this.permission_data.roleId =  row.id
      console.log(this.permission_data)

      this.permission_show = true

 
     
    },







  },

}

</script>
<style lang='scss' scoped>
</style>