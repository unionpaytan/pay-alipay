import Vue from 'vue'
import Router from 'vue-router'

import _import from './_import'

Vue.use(Router)

export default new Router({
  routes: [
    { path: '/', redirect: '/login'},
    { path: '/qrcode', component: _import('login/qrcode')},
    { path: '/amazon',component: _import('login/amazon')},
    { path: '/404', component: _import('errorPage/404')},
    { path: '/401', component: _import('errorPage/401')},
    { path: '/login',name: 'login', component: _import('login/index')},
    
    
  ]
})
