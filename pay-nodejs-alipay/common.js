 
var COMMON = {};

/**
 *
 * 日期转 timestamp taskStartTime
 */

 COMMON.getDatetime = function () {
    var now = new Date();
    var year = now.getFullYear();
    var month = (now.getMonth() + 1).toString();
    month = month.length == 1 ? "0" + month : month;
    var day = now.getDate().toString();
    day = day.length == 1 ? "0" + day : day;
    var hour = now.getHours().toString();
    hour = hour.length == 1 ? "0" + hour : hour;
    var min = now.getMinutes().toString();
    //log(hour.length);
    min = min.length == 1 ? "0" + min : min;
    var sec = now.getSeconds().toString();
    sec = sec.length == 1 ? "0" + sec : sec;
    var datetimeFormat = year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
    return datetimeFormat;
}

COMMON.randomIntFromInterval = function(min, max) { // min and max included 
    return Math.floor(Math.random() * (max - min + 1) + min)
}

//导出通用对象
module.exports = COMMON;