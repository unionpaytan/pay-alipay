import router from './'
import store from '../store'
import addRoutes from './authority/addRouter'
//路由拦截
let registerRouteFresh = true
router.beforeEach((to, from, next) => {
    next()
})

router.onReady(function() { // 刷新之后挂载路由会被清除  需要重新挂载
  let routerQuery = sessionStorage.routerQuery&&JSON.parse(sessionStorage.routerQuery) || ''
  if(routerQuery){
    addRoutes(routerQuery.key,routerQuery.name)
  }
})