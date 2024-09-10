import Vue from 'vue'
import Router from 'vue-router'
import _import from './_import'
import payUrl from '../views/payUrl'
import qrcode from '../views/qrcode'

Vue.use(Router)

export default new Router({
  //mode: 'history',
  routes: [
    { path: '/', component: _import('index') },
    { path: '/qrcode', component: _import('qrcode') },
    { path: '/404', component: _import('errorPage/404') },
    { path: '/401', component: _import('errorPage/401') },
    // { path: '/eth', component: _import('eth/index')},
    // { path: '/trx', component: _import('tron/index')},

    { path: '/home', component: _import('home') },
 

    {
      path: '/payUrl',
      component: payUrl,
      beforeRouteEnter: (to, from, next) => {
        document.title = '订单支付';
        next()
      }
    },


  ]
})
