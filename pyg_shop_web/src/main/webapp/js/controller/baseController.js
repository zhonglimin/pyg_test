app.controller('baseController',function ($scope) {

    $scope.paginationConf={
        currentPage: 1,
        totalItems: 0,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    };

    //刷新页面
    $scope.reloadList=function(){
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds=[];

    $scope.updateSelection=function ($event,id) {
        if($event.target.checked){//勾选添加元素
            $scope.selectIds.push(id);
        }else{//取消勾选
            var index=$scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }

    }

    //[{"id":43,"text":"容量"},{"id":44,"text":"香型"},{"id":45,"text":"酒精度"}]

    //容量，香型，酒精度
    $scope.jsonToString=function (jsonString,key) {
        var value="";
        var json = JSON.parse(jsonString);
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=",";
            }
            value+= json[i][key];
        }
        return value;
    }


})