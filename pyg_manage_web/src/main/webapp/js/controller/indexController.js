app.controller('indexController',function ($scope,loginService) {
    //获取用户名
    $scope.loginName=function () {
        loginService.loginName().success(
            function (response) {//{'loginName':'dasheng'}
                $scope.loginName=response.loginName;
            }
        );
    }
})