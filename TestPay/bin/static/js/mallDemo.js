var dataSet = "merchantId=11163&amp;pageIndex=1&amp;pageSize=20&amp;typeId=1";
$.ajax({
    type: 'POST',                //数据提交方式
    url: 'list',   //数据提交路径
    async: 'true', // true   // 是否支持异步刷新 默认为 true
    data: dataSet, // 需要提交的数据
    contentType: 'application/x-www-form-urlencoded', // 客端户端发送给服务器的数据类型(待确定)
    //请求成功后回调函数
    success: function (data, status, xhr) {
        var strInfo = '';
        var shopDatas = JSON.parse(data).data;
        for (var i = 0; i < shopDatas.length; i++) {
            
            var shopName = "<h6>名称：" + shopDatas[i].scratchCardName + "</h6>";
            var shopImg ="<div class='mallDetailed'>"+  "<a class='mallLink'>"+ "<img  src=" + shopDatas[i].imgUrl + "/>";
            var shopPrice = "<h6>价格：" + shopDatas[i].scratchCardPrice + "</h6>" + "</a>"+ "</div>";
            strInfo = strInfo + shopImg + shopName + shopPrice;
        }
        strInfo = strInfo ;
//        href='http://ww.baidu.com'
        document.getElementById('mallList').innerHTML=strInfo;
    },
    //请求失败后回调函数
    error: function (error) {
        console.log("error", error);
        responseError(error);
    }
});
