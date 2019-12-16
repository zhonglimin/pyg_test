//服务层
app.service('payService',function($http){

    //获取预支付url的方法
    this.createNative = function () {
        return $http.get('../pay/createNative.do');
    }


    //查询订单的支付状态
    this.queryOrderStatus = function (out_trade_no) {
        return $http.get('../pay/queryOrderStatus.do?out_trade_no=' + out_trade_no);
    }

});