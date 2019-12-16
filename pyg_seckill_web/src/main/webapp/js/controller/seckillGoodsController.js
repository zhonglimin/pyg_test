//控制层
app.controller('seckillGoodsController' ,function($scope   ,$location,$interval,seckillGoodsService){


    //查询秒杀商品列表
    $scope.findList=function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.seckillGoodsList = response;

                //处理进度条
                /*for(var i=0;i< $scope.seckillGoodsList.length;i++){
                    var seckillGoods = $scope.seckillGoodsList[i];
                    var pecent = ((seckillGoods.num - seckillGoods.stockCount ) / seckillGoods.num ) * 100;
                    $scope.seckillGoodsList[i].pecent = pecent;
                }*/

            }
        );
    }


    //展示商品详情信息
    $scope.loadSeckillGoods = function () {
       var id =  $location.search()['id'];
        seckillGoodsService.loadSeckillGoods(id).success(
            function (response) {
                $scope.seckillGoods = response;



                //计算剩余xx天xx小时xx分钟xx秒
              allSeconds =  Math.floor(( new Date($scope.seckillGoods.endTime).getTime() - new Date().getTime() ) / 1000);
                // $scope.second = 10;
                var time  =  $interval(
                    function () {
                        if(allSeconds > 0){
                            allSeconds = allSeconds - 1;

                            //折算成xx天xx小时xx分钟xx秒
                           $scope.timeTitle =  convertTime(allSeconds);
                        }else{
                            //关闭定时器
                            $interval.cancel(time);
                        }

                    },1000
                );
            }
        );
    }


    //秒---------折算成xx天xx小时xx分钟xx秒
    convertTime=function (allSecond) {
       var day = Math.floor ( allSecond / (24 * 60 * 60) );

       var hour = Math.floor( ( allSecond - (day * 24 * 60 * 60) ) / (60 * 60) );

        var minutes= Math.floor(  (allSecond -day*60*60*24 - hour*60*60) / 60    );//分钟数

        var seconds= allSecond -day*60*60*24 - hour*60*60 -minutes*60; //秒数

        var timeString="";
        if(day>0){
            timeString=day+"天 ";
        }
        return timeString+hour+":"+minutes+":"+seconds;
    }


    //提交秒杀订单
    $scope.submitOrder = function (id) {
        seckillGoodsService.submitOrder(id).success(
            function (response) {
                if(response.success){
                    location.href = "/pay.html";
                }else{
                    alert(response.message);
                }
            }
        );
        
    }
});