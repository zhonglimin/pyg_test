//定义控制器
app.controller('brandController',function($scope,$controller,brandService){

   //控制器继承 1被继承的控制器的名称 2{}
    $controller('baseController',{$scope:$scope});

    //查询所有品牌列表数据
    $scope.findAll=function(){
        brandService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }
    //分页查询
    $scope.findPage=function(page,rows){
        brandService.findPage(page,rows).success(
            function(response){//分页封装实体类
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//将总记录数给分页组件
            }
        )
    }

    //$scope.entity={};//品牌对象

    //添加品牌
    $scope.save=function () {
        var methodObject;
        if($scope.entity.id!=null){//修改
            methodObject = brandService.update($scope.entity);
        }else{
            methodObject = brandService.add($scope.entity);
        }
        methodObject.success(
            function (response) {//操作结果 {success:true,message:'操作成功'}
                if(response.success){//成功
                    //重新查询
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }

            }
        )
    }

    //回显
    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function(response){//品牌对象
                $scope.entity=response;
            }
        )
    }

    //批量删除
    $scope.dele=function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){//成功
                    //重新查询
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
                $scope.selectIds=[];
            }
        )
    }

    $scope.searchEntity={};//封装查询条件
    //分页条件查询
    $scope.search=function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//将总记录数给分页组件
            }
        )
    }
});