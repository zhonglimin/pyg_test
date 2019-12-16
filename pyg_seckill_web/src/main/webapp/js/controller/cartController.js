//控制层
app.controller('cartController' ,function($scope   ,cartService){


    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;


                //计算总金额和总数量
                $scope.total = {'totalNum':0,'totalMoney':0.00};
                for(var i=0;i<$scope.cartList.length;i++){
                   var cart =  $scope.cartList[i];
                   for(var j=0;j<cart.orderItemList.length;j++){
                       var orderItem = cart.orderItemList[j];
                       $scope.total.totalNum += orderItem.num;
                       $scope.total.totalMoney += orderItem.totalFee;
                   }
                }
            }
        );
    }
    
    //添加商品到购物车列表
    $scope.addGoodsToCartList = function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if(response.success){
                    //页面重新加载
                    $scope.findCartList();

                }else {
                    alert(response.message);
                }
            }
        );
    }


    //查询地址列表
    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList = response;


                //默认地址的处理
                for(var i=0;i<$scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault == '1'){
                        $scope.selectedAddress = $scope.addressList[i];
                        return ;
                    }
                }
            }
        );
    }

    //记录当前选中地址的方法
    $scope.selectAddress=function (address) {
        $scope.selectedAddress = address;
    }

    //判断是否应该被选中
    $scope.isSelected = function (address) {
        if($scope.selectedAddress == address){
            return true;
        }
        return false;
    }


    //存放支付方式和地址的变量
    $scope.order = {'paymentType':'1'};
    
    $scope.changeType = function (type) {
        $scope.order.paymentType = type;
    }
    
    
    //提交订单
    $scope.submitOrder=function () {
        //传递参数： 地址和支付方式
        $scope.order.receiver = $scope.selectedAddress.contact;
        $scope.order.receiverMobile = $scope.selectedAddress.mobile;
        $scope.order.receiverAreaName = $scope.selectedAddress.address;

        //有什么响应： Result
        cartService.submitOrder($scope.order).success(
            function (response) {
                if(response.success){
                    //跳转到支付页面
                    if($scope.order.paymentType == '1'){
                        location.href = "pay.html";
                    }else {
                        location.href = "paysuccess.html";
                    }
                }else{
                    alert(response.message);
                }
            }
        );

    }
});