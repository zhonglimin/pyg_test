//定义控制器
app.controller('itemController',function($scope,$http){
	
	
	//数量加减操作
	$scope.num = 1;
	$scope.changeNum = function(num){
		
		$scope.num = $scope.num + num;
		if($scope.num < 1){
			$scope.num = 1;
		}
	}
	
	//规格的选择
	//$scope.specification = {'网络':'移动4G','机身内存':'32G'};
	$scope.specification = {};
	
	//选中规格的时候，改变变量的值
	$scope.selectSpec = function(key,value){
		$scope.specification[key] = value;
		
		
		//根据选中的规格，去查找skuList 中spec,更改页面的标题和价格
		for(var i=0;i < skuList.length;i++){
			if(matchObject($scope.specification,skuList[i].spec)){
				//改变sku
				$scope.sku = skuList[i];
				return ;
			}
		}
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的	
		
		
	}
	
	
	//比较两个json对象是否相等
	matchObject = function(map1,map2){
		for(var k in map1){
			if(map1[k] != map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k] != map1[k]){
				return false;
			}
		}
		
		return true;
	}
	
	//判断应不应该被选中
	$scope.isSelectedSpec = function(key,value){
		if($scope.specification[key] == value){
			return true;	
		}
		return false;
	}
	
	//读取默认的sku
	$scope.loadSku = function(){
		$scope.sku = skuList[0];
		//默认规格的选中(深度克隆)
		$scope.specification =  JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	
	
	//添加商品到购物车
	$scope.addGoodsToCartList=function(){
		// alert("sku的id" + $scope.sku.id + " 数量" + $scope.num);
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+ $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
        	function (response) {
				if(response.success){
					location.href = "http://localhost:9107";
				}else{
					alert(response.message);
				}
            }
		);
	}
	
	
});