//定义模块
var app=angular.module('pyg',[]);

//过滤器
app.filter('trustHtml',['$sce',function ($sce) {

    //对html做安全信任策略
    return function (title) {
        return $sce.trustAsHtml(title);
    }
}]);