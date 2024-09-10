<template>
  <div id="app">
    <router-view/>
  </div>
</template>
<script>
 
  export default {
    name: 'App',
    components: {},
    setup(){ // 系统创建完成
      let that = this;
      console.log("=setup");
      return {
 
      };
    },
    mounted() { // 组件初始化完成
      let that = this;
      console.log("=mounted");
      //console.log(that.$refs.page_must.); // 访问子组件
    },
    updated() { // 视图更新
      let that = this;
      console.log("=updated");
    },
    unmounted() {
      let that = this;
      console.log("=unmounted");
    },
    methods: {
 
    }
  }
</script> 
<style lang="scss"> 
#app{
   width: 100%;
   height: 100%;
}
</style>
