import Vuex from 'vuex'
import Vue from 'vue'
import axios from 'axios'
import * as api from '@/api/public'

Vue.use(Vuex)

let state = {
    loading: false,
    routerList: sessionStorage['routerList'] ? JSON.parse(sessionStorage['routerList']) : [],
    viewTagList: sessionStorage['viewTagList'] ? JSON.parse(sessionStorage['viewTagList']) : [], //tab标签
    isLogin: sessionStorage['isLogin'] ? sessionStorage['isLogin'] : 0,
}

let actions = {

    setLoading({ commit }, info) {
        commit('SET_LOADING', info)
    },
    setRouterList({ commit }, info) {
        commit('SET_ROUTER_LIST', info)
    },
    setViewTagList({ commit }, info) {
        commit('SET_VIEW_TAG_LIST', info)
    },

    setIsLogin({ commit }, info) {
        return new Promise(resolve => {
            commit('SET_IS_LOGIN', info)
            resolve()
        })
    },


}

let mutations = {
    SET_LOADING(state, info) {
        state.loading = info
    },
 
    SET_ROUTER_LIST(state, info) {
        sessionStorage['routerList'] = JSON.stringify(info)
        state.routerList = info
    },
    SET_VIEW_TAG_LIST(state, info) {
        sessionStorage['viewTagList'] = JSON.stringify(info)
        state.viewTagList = info
    },
    SET_IS_LOGIN(state, info) {
        sessionStorage['isLogin'] = info
        state.isLogin = info
    },

}
let getters = {
    loading: state => state.loading,
    routerList: state => state.routerList,
    viewTagList: state => state.viewTagList,
    isLogin: state => state.isLogin,
    

   
}

export default new Vuex.Store({
    state,
    actions,
    mutations,
    getters
})