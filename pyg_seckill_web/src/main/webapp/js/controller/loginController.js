//控制层
app.controller('loginController' ,function($scope   ,loginService){


    //获取当前登录名的方法
    $scope.getLoginUser=function () {
        loginService.getLoginUser().success(
            function (response) {
                $scope.loginUser = response.username;
            }
        );
    }
});