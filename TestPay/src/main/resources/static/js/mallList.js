	var dataSet = "merchantId=11163&amp;pageIndex=1&amp;pageSize=20&amp;typeId=1";
    $.ajax({
        type    : 'POST',                //数据提交方式
        url     : 'list',   //数据提交路径
        async   : 'true', // true   // 是否支持异步刷新 默认为 true
        data    : dataSet, // 需要提交的数据
        contentType : 'application/x-www-form-urlencoded', // 客端户端发送给服务器的数据类型(待确定)
        //请求成功后回调函数
        success : function(data, status, xhr){
        	var strInfo = "<tr>"+"<td>"+"商品名称"+"</td>"+"<td>"+"商品图片"+"</td>"+"<td>"+"商品价格"+"</td>"+"</tr>";        	
        	var shopDatas =  JSON.parse(data).data;
        	for(var i=0;i<shopDatas.length;i++)
        	{
        		var shopName = "<td>"+shopDatas[i].scratchCardName+"</td>";
        		var shopImg  = "<td>"+"<img src="+shopDatas[i].imgUrl+"/>"+"</td>";
        		var shopPrice= "<td>"+shopDatas[i].scratchCardPrice+"</td>"
        		strInfo = "<tr>"+strInfo +shopName+shopImg+shopPrice+"</tr>";
        	}
        	strInfo = "<table>"+strInfo+"</table>"
        	document.write(strInfo);
        },
        //请求失败后回调函数
        error   : function(error){
            console.log("error", error);
            responseError(error);
        }
    });
