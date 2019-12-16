//服务层
app.service('cartService',function($http){

    //查询购物车列表
    this.findCartList = function () {
        return $http.get('../cart/findCartList.do');
    }

    //添加商品到购物车列表
    this.addGoodsToCartList = function (itemId,num) {
        return $http.get('../cart/addGoodsToCartList.do?itemId='+itemId+'&num=' + num);
    }


    //查询地址列表
    this.findAddressList = function () {
        return $http.get('../address/findAddressListByUser.do');
    }


    //提交订单
    this.submitOrder = function (order) {
        return $http.post('../order/add.do',order);
    }
});