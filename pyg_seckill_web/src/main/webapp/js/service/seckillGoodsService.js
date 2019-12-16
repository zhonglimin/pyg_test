//服务层
app.service('seckillGoodsService',function($http){

    //查询秒杀商品列表
    this.findList = function () {
        return $http.get('../seckillGoods/findList.do');
    }


    //根据id查询秒杀商品信息
    this.loadSeckillGoods = function (id) {
        return $http.get('../seckillGoods/findOneFromRedis.do?id=' + id);
    }


    //提交秒杀订单
    this.submitOrder = function (id) {
        return $http.get('../seckillOrder/submitOrder.do?id=' + id);
    }
});