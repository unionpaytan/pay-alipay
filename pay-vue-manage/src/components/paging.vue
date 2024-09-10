<template>
    <div class="Paging" style="overflow:hidden">
        <el-col :span="3" style="textAlign:right;paddingRight:10px;">
            <span class="demonstration">每页条数</span>
        </el-col>
        <el-col :span="16">
            <el-pagination
            @size-change="changeSize"
            @current-change="changeIndex"
            :current-page="pageIndex"
            :page-sizes="[3,10,20, 50, 100, 200]"
            :page-size="pageSize"
            layout="sizes, prev, pager, next , total , jumper"
            :total="pageTotal*1">
            </el-pagination>
        </el-col>
    </div>
</template>

<script>
export default {
    name:'Paging',
    props: {
        pageIndex: {
            
        },
        pageSize: {
           
        },
        pageTotal: {
          
        }
    },
    data(){
        return {

        }
    },
    methods:{
        changeSize(val){
            this.$emit('changeSize',val)
        },
        changeIndex(val){
            this.$emit('changeIndex',val)
        }
    }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
  .Paging{
    .demonstration{    
        font-size: 14px;
        color: #8492a6;
        line-height: 32px;
        text-align:right;
    }
  }
</style>