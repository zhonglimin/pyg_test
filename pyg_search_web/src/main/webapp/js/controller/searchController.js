 //控制层 
app.controller('searchController' ,function($scope,$location,searchService){

    $scope.searchMap = {'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortField':'','sort':''};

    //首页和搜索页的对接
    $scope.loadKeywords = function () {
        var keywords = $location.search()['keywords'];
        if(keywords != null){
            $scope.searchMap.keywords = keywords;
            $scope.search();
        }
    }


	//搜索的方法
	$scope.search=function () {
	    //处理字符串类型的页码
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);

        searchService.search($scope.searchMap).success(
        	function (response) {
                $scope.resultMap = response;


                $scope.startDot = false;
                $scope.lastDot = false;


                var startPage = 1;
                var endPage = $scope.resultMap.totalPages;

                //计算开始页码和结束页码----根据当前页计算
                if($scope.resultMap.totalPages > 5){
                    if($scope.searchMap.pageNo < 3){
                        //显示前5页数据
                        endPage = 5;
                        $scope.lastDot = true;
                    }else if($scope.searchMap.pageNo > ($scope.resultMap.totalPages - 2)){
                        //显示最后5页数据
                        startPage = $scope.resultMap.totalPages - 4;
                        $scope.startDot = true;
                    }else{
                        //正常的处理方案
                        startPage = $scope.searchMap.pageNo - 2;
                        endPage = $scope.searchMap.pageNo + 2;

                        $scope.startDot = true;
                        $scope.lastDot = true;

                    }
                }



                //构建分页标签
                $scope.pageList = [];
                for(var i=startPage;i <= endPage;i++){
                    $scope.pageList.push(i);
                }
            }
		)
    }



    //添加搜索选项的方法
    $scope.addSearchItem = function (key,value) {

	    if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = value;
        }else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    //删除搜索选项的方法
    $scope.deleteSearchItem = function (key) {

        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = '';
        }else {
           delete $scope.searchMap.spec[key];
        }

        $scope.search();
    }

    $scope.queryByPage = function (page) {
	    if(page < 1 || page > $scope.resultMap.totalPages){
            return ;
        }
        $scope.searchMap.pageNo = page;

	    //触发新的的查询
        $scope.search();
    }

    //是否是首页
    $scope.isTopPage = function () {
        if($scope.searchMap.pageNo == 1){
            return true;
        }

        return false;
    }



    //是否是尾页
    $scope.isEndPage = function () {
        if($scope.searchMap.pageNo == $scope.resultMap.totalPages){
            return true;
        }

        return false;
    }

    
    //排序
    $scope.sortSearch = function (sortField,sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;

        //触发搜索
        $scope.search();
    }


    //判断品牌是否隐藏
    $scope.isKeywordsContainsBrand = function () {
        var brandList = $scope.resultMap.brandList;

        var keywords = $scope.searchMap.keywords;
        for(var i=0;i<brandList.length;i++){
            if(keywords.indexOf(brandList[i].text) >= 0){
                return true;
            }
        }

        return false;
    }
});	
