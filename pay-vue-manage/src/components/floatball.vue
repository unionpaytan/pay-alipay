<template>
  <transition>
    <div
      ref="dragIcon"
      class="dragIcon"
      @click="$parent.isCollapse = !$parent.isCollapse"
      @touchstart.stop="handleTouchStart"
      @touchmove.prevent.stop="handleTouchMove($event)"
      @touchend.stop="handleTouchEnd"
      :style="{
        left: left + 'px',
        top: top + 'px',
        width: itemWidth + 'px',
        height: itemHeight + 'px',
      }"
      v-text="text"
      v-if="isShow"
    > {{ text }}</div>
  </transition>
</template>

<script>
export default {
  props: {
    text: {
      type: String,
      default: "球",
    },
    itemWidth: {
      type: Number,
      default: 40,
    },
    itemHeight: {
      type: Number,
      default: 40,
    },
  },
  data() {
    return {
      left: 0,
      top: 0,
      startToMove: false,
      isShow: true,
      timer: null,
      currentTop: null,
      clientW: document.documentElement.clientWidth, //视口宽
      clientH: document.documentElement.clientHeight, //视口高
    };
  },
  created() {
    //this.left = (this.clientW - this.itemWidth - 30)
    //this.left = 120;
    this.left = 30
    this.top = this.clientH / 2 - this.itemHeight / 2  + 140;
    //this.init();
  },
  mounted() {
    this.bindScrollEvent();

  },
  beforeDestroy() {
    // 记得销毁一些全局的的事件
    this.removeScrollEvent();
  },
  methods: { 
    showText(v) {
      console.log(v);
    },
    handleTouchStart() {
      this.startToMove = true;
      this.$refs.dragIcon.style.transition = "none";
    },
    handleTouchMove(e) {
      const clientX = e.targetTouches[0].clientX; //手指相对视口的x
      const clientY = e.targetTouches[0].clientY; //手指相对视口的y
      const isInScreen =
        clientX <= this.clientW &&
        clientX >= 0 &&
        clientY <= this.clientH &&
        clientY >= 0;
      if (this.startToMove && e.targetTouches.length === 1) {
        if (isInScreen) {
          this.left = clientX - this.itemWidth / 2;
          this.top = clientY - this.itemHeight / 2;
        }
      }
    },
    handleTouchEnd() {
      if (this.left < this.clientW / 2) {
        this.left = 30; //不让贴边 所以设置30没设置0
        this.handleIconY();
      } else {
        this.left = this.clientW - this.itemWidth - 30; //不让贴边 所以减30
        this.handleIconY();
      }
      this.$refs.dragIcon.style.transition = "all .3s";
    },
    handleIconY() {
      if (this.top < 0) {
        this.top = 30; //不上帖上边所以设置为30 没设置0
      } else if (this.top + this.itemHeight > this.clientH) {
        this.top = this.clientH - this.itemHeight - 30; //不让帖下边所以减30
      }
    },
    bindScrollEvent() {
      window.addEventListener("scroll", this.handleScrollStart);
    },
    removeScrollEvent() {
      window.removeEventListener("scroll", this.handleScrollStart);
    },
    handleScrollStart() {
      this.isShow = false;
      this.timer && clearTimeout(this.timer);
      this.timer = setTimeout(() => {
        this.handleScrollEnd();
      }, 100);
      this.currentTop =
        document.documentElement.scrollTop || document.body.scrollTop;
    },
    handleScrollEnd() {
      this.scrollTop =
        document.documentElement.scrollTop || document.body.scrollTop;
      // 判断是否停止滚动的条件
      if (this.scrollTop == this.currentTop) {
        this.isShow = true;
      }
    },
  },
  watch: {
    isCollapse(v) {
      this.$parent.sidebarWidth = v ? "0" : "200px" + " !important";
      this.$parent.menuShow = v ? "none" : "";
      console.log(v);
      var sidebar = document.getElementById('sidebar');
      console.log("sidebar",sidebar)
       // 显示侧边栏
      // sidebar.style.display = 'block'; 
      // 隐藏侧边栏
    
    },
  },
};
</script>

<style scoped>
.dragIcon {
  position: fixed;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #42b983;
  line-height: 40px;
  text-align: center;
  color: #fff;
  z-index: 999999;
}
.v-enter {
  opacity: 1;
}
.v-leave-to {
  opacity: 0;
}
.v-enter-active,
.v-leave-active {
  transition: opacity 0.3s;
}
@media screen and (max-width: 2000px) {
  .dragIcon {
      display:none;
  }
}
@media screen and (max-width: 414px) {
  .dragIcon {
      display:inline;
  }
}
</style>