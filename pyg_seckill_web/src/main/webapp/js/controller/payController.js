//控制层
app.controller('payController' ,function($scope   ,$location,payService){


    //获取预支付url，生成二维码
    $scope.createNative = function () {

        payService.createNative().success(
            function (response) {
                //订单号，总金额
                $scope.out_trade_no = response.out_trade_no;
                $scope.total_fee = (response.total_fee /100 ).toFixed(2);


                //预支付url
                var qr = window.qr = new QRious({
                    element: document.getElementById('18ewm'),
                    size: 250,
                    level:'H',
                    value: response.code_url
                });


                //立即查询订单的支付状态
                queryOrderStatus($scope.out_trade_no);

            }
        );
    }


    queryOrderStatus = function (out_trade_no) {
        payService.queryOrderStatus(out_trade_no).success(
            function (response) {
                if(response.success){
                    //支付成功，跳转到支付成功页面
                    location.href = "paysuccess.html#?total_fee=" + $scope.total_fee;
                }else{
                    //支付失败，跳转到支付失败页面
                    if(response.message == 'PAY_TIME_OUT'){
                        //超时
                       /* alert("您的支付已超时,请重新支付");
                         $scope.createNative();*/
                       location.href = "paytimeout.html";
                    }else {
                        location.href = "payfail.html";
                    }
                }
            }
        );
    }


    //显示支付金额
    $scope.getMoney = function () {
        $scope.money = $location.search()['total_fee'];
    }

});