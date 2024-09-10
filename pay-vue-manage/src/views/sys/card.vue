<!-- 号段管理  people-->
<template>
    <div>

        <div class="mb20">
            <el-button v-if="handelAuto('add')" icon="el-icon-circle-plus" size="small" type="success"
                       @click="adm_addForm()">新增
            </el-button>
        </div>

        <el-table v-loading="m_tableLoading" @selection-change="m_handleSelectionChange"
                  @current-change="m_handleCurrentChange" class="mtable" :stripe="true" tooltip-effect="dark"
                  :highlight-current-row="true" :data="m_list">
            <!-- <el-table-column type="selection" width="55"></el-table-column> -->

            <el-table-column align="center" prop="bankName" label="银行名称"></el-table-column>
            <el-table-column align="center" prop="mechanism" label="银行联行号"></el-table-column>
            <el-table-column align="center" prop="cardLength" label="卡号长度"></el-table-column>
            <el-table-column align="center" prop="cardHead" label="头6位"></el-table-column>
            <el-table-column align="center" prop="cardType" label="银行卡类型"></el-table-column> 
            <el-table-column align="center" prop="userId" label="操作人"></el-table-column> 
           
            <el-table-column align="center" label="操作">
                <template slot-scope="scope">
                   
                    <el-button v-if="handelAuto('del')" style="color:#ff0000" @click.native="m_oneDel(scope.row)"
                               type="text">删除
                    </el-button> 

                </template>
            </el-table-column>
        </el-table>

        <!-- 编辑新增管理员框 -->

        <el-dialog :close-on-click-modal="false" v-if="m_show" :title="(m_isadd?'添加':'编辑')" :visible.sync="m_show"
                   width="450px" style="margin-top:-100px;">
            <el-form :model="formData" :rules="m_rules" ref="ruleForm">
                <div class="">
                    <el-form-item class="flex-1" label="银行名称" label-width="100px" prop="bankName">
                        <el-input placeholder="请输入" v-model="formData.bankName" auto-complete="off"></el-input>
                    </el-form-item>  
                    <el-form-item  class="flex-1" label="银行联行号" label-width="100px" prop="  ">
                        <el-input placeholder="请务必输入正确的银行联行号" v-model="formData.mechanism" auto-complete="off"></el-input>
                    </el-form-item>

                    <el-form-item class="flex-1" label="卡号长度" label-width="100px" prop="cardLength">
                        <el-input type="number" placeholder="16或19" v-model="formData.cardLength" auto-complete="off"></el-input>
                    </el-form-item>  

                     <el-form-item class="flex-1" label="头6位" label-width="100px" prop="cardHead">
                        <el-input type="number" placeholder="请输入" v-model="formData.cardHead" auto-complete="off"></el-input>
                    </el-form-item>

                     <el-form-item class="flex-1" label="银行卡类型" label-width="100px" prop="cardType">
                        <el-input placeholder="借记卡或贷记卡" v-model="formData.cardType" auto-complete="off"></el-input>
                    </el-form-item> 

                </div>

            </el-form>
            <div slot="footer" class="dialog-footer" style="margin-top:-50px;">
                <!-- 取 消 -->
                <el-button @click="m_show = false">取 消</el-button>
                <!-- 确 定 -->
                <el-button type="primary" :loading="m_loading" @click.native="sureOption">确 定</el-button>
            </div>
        </el-dialog>

        <Paging class="mt20 mb30" :pageIndex="m_page.page" :pageSize="m_page.rows" :pageTotal="m_page.total"
                @changeSize="m_changesize" @changeIndex="m_changeindex"></Paging>
    </div>
</template>

<script>
    import qrcode from 'qrcode'
    import * as api from '@/api/public'

    export default {
        name: 'card',
        data() {
            return {

                search_data: {},

                formData: {
                    id: '',
                    bankName: '', //用户名称
                    mechanism: '', // 登陆账号
                    cardLength: '', // 邮箱
                    cardHeadLength: '', // 密码
                    cardType: '', // 角色ID
                    userId: '',//google key 
                    tableMerchantId:'',

                },

                m_rules: {
                    bankName: [
                        {required: true, message: '请输入', trigger: 'blur'}
                    ],
                    mechanism: [
                        {required: true, message: '请输入', trigger: 'blur'}
                    ],
                    cardLength: [
                        {required: true, message: '请输入', trigger: 'blur'}
                    ],
                    cardHead: [
                        {required: true, message: '请输入', trigger: 'blur'}
                    ],
                    cardType: [
                        {required: true, message: '请输入', trigger: 'blur'}
                    ],


                },  

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

                let userData = sessionStorage.userData && JSON.parse(sessionStorage.userData) || {}  
                this.search_data.tableMerchantId = userData.merchantId || 0;
                this.m_tableLoading = true
                let data = Object.assign(this.search_data, this.m_page);
                api.getSysCardList(data).then(res => {
                    //console.log(res)
                    this.m_list = res.data.pages.records
                    this.m_page.total = res.data.pages.total

                }).finally(() => {
                    this.m_tableLoading = false
                })
            },

            addData() {
                this.m_loading = true

                let userData = sessionStorage.userData && JSON.parse(sessionStorage.userData) || {}  

                this.formData.tableMerchantId = userData.merchantId || 0;

                api.addSystemCard(this.formData).then(res => {
                    this.m_page.page = 1
                    this.m_success('添加成功')
                    this.m_show = false
                    this.getDatalist()
                }).catch(res => {

                }).finally(() => {
                    this.m_loading = false
                })
            },


            //编辑
            editData() {
                this.m_loading = true
                let from = this._dep(this.formData)
                from.merchantName = this.formData.userName
                api.updateSysUser(from).then(res => {
                    // 修改成功
                    this.m_success('修改成功')
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
               
                let userData = sessionStorage.userData && JSON.parse(sessionStorage.userData) || {}  
                let tableMerchantId = userData.merchantId || 0;

                api.delSysCard({id: m_delId,tableMerchantId:tableMerchantId}).then(res => {
                    // 删除成功
                    this.m_loading = false
                    this.m_success('删除成功')
                    this.getDatalist()
                }).catch(res => {
                    console.log(res)
                    this.m_loading = false
                })
            },
    
            adm_addForm() {  
                this.m_isadd = true //m_isadd?'添加':'编辑'
                this.formData = JSON.parse(JSON.stringify(this._formData))
                this.m_show = true
            }, 

        },
        filters: {
            f_status(v) {
                if (v == 0) {
                    return '禁用'
                }
                if (v == 1) {
                    return '启用'
                }
            }
        }
    }

</script>
<style lang='scss' scoped>
</style>