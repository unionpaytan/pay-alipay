export default function(){
    setHtmlFontSize()
    //这里是页面尺寸发生变化时执行
    window.addEventListener('resize', setHtmlFontSize);
    function setHtmlFontSize() {
        // 1. 获取当前的屏幕的宽度
        var windowWidth = document.documentElement.offsetWidth;
        // 2. 以标准的375屏幕 根元素100px的标准  需要3.75rem占满整屏的值 作为参照点
        var htmlFontSize = windowWidth / 3.75;
        //给最大值 超过了200px默认为200px
        if(htmlFontSize > 200){
            htmlFontSize = 200;
        }
        // 3. 把当前根元素的字体大小设置给当前html的fontSize 注意要带单位
        document.querySelector('html').style.fontSize = htmlFontSize + 'px';
    }
}