module.exports={
    data(){
        return{
            page:{
                index:1,
                size:20,
                total:0
            }
        }
    },
    methods:{
        changesize(val){                                 //改变分页数量
            this.page.size=val
            this.page.index=1
            try{
                this.getList()
            }catch(res){
                
            }
        },
        changeindex(val){                                //改变当前页
            this.page.index=val
            try{
                this.getList()
            }catch(res){
                
            }
        }
    }
}
