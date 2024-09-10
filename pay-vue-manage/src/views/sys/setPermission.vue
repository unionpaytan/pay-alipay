<!-- 设置权限 setPermission -->
<template>
  <div>
    <el-dialog :append-to-body="true" :close-on-click-modal="false" title="设置权限" :visible.sync="Show" width="700px">
      <div>
        <el-tree v-loading="m_tableLoading" :data="dataTree" show-checkbox default-expand-all node-key="id" ref="tree" highlight-current :props="props">
        </el-tree>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="Show = false">关 闭</el-button>
        <el-button type="primary" @click="handelSetMenu">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as api from '@/api/public'
export default {
  name: 'setPermission',
  props: {
    value: {},
    data: {
      required: true,
    }
  },

  data() {
    return {
      dataTree: [],
      m_tableLoading: false,
      Show: this.show,
      btnLoding: false,

      props: {
        label: 'menuName',
        children: 'childrenList'

      },
    }
  },
  methods: {
    //初始化方法
    init() {
      this.getDataList()
    },


    getDataList() {
      this.m_tableLoading = true
      this.dataTree = []
      api.queryPermissionInfoList().then(res => {
        this.dataTree = res.data
        this.queryRolePermission()



      }).catch(err => {
        this.m_tableLoading = false

      })

    },


    queryRolePermission() {
      console.log(this.data, 222)
      api.queryRolePermission(this.data).then(res => {
        console.log(res)
        let permissionList = res.data.permissionList
        this.$refs.tree.setCheckedKeys(permissionList);
        this.m_tableLoading = false
      }).catch(err => {
        this.m_tableLoading = false

      })
    },







    handelSetMenu() {
      let permissionValues = this.$refs.tree.getCheckedKeys().join(',')
      //console.log(permissionValues)
      this.m_loading = true
      api.sitiRolePermission({
        permissionValues,
        roleId: this.data.roleId
      }).then(res => {
        this.m_loading = false
        this.$emit('handelSubmit')
        this.Show = false
      }).catch(err => {
        this.m_loading = false

      })





    },
  },
  watch: {
    Show(v) {
      if (v) {
        this.init()
      }
      this.$emit('input', v)
    },
    value(v) {
      this.Show = v
    }
  },
  filters: {

  }
}

</script>
<style lang='scss' scoped>
</style>