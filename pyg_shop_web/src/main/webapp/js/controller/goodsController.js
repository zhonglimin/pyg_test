 //控制层 
app.controller('goodsController' ,function($scope,$controller ,$location,typeTemplateService,itemCatService,uploadService  ,goodsService){
	
	$controller('baseController',{$scope:$scope});//继承

	$scope.status=['未审核','审核通过','审核未通过','已关闭'];

	$scope.itemCatList=[];

	$scope.findItemCatList=function () {
        itemCatService.findAll().success(
        	function (response) {//分类列表
        		for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id ]= response[i].name;
				}

            }
		)
    }


	//获取第一级分类列表
	$scope.findItemCat1List=function(){
        itemCatService.findByParentId(0).success(
        	function (response) {
				$scope.itemCat1List=response;
            }
		)
	}


	//监测变量，如果变量发生变化了，就会执行对应的方法
	//1被监测的变量 2function(新值，旧值) 变量变化，方法就会执行
	//监测第一级分类id的变化，如果变化，根据newValue查询第二级的分类列表
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		if(newValue!=undefined){
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List=response;
                    //清空第三级列表
                    //$scope.itemCat3List=null;
                }
            )
		}

    });
	//监测第二级分类id的变化，如果变化，根据newValue查询第三级的分类列表
	$scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        if(newValue!=undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                }
            )
        }
    });
	//监测第三级分类id的变化，如果变化，根据newValue查询分类对象，对象中有typeId
	$scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        if(newValue!=undefined) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
            )
        }
    });
	//监测模板id的变化，如果变化，根据newValue查询模板对象，对象中有品牌数据
	$scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
        if(newValue!=undefined){
        	//获取品牌数据
			typeTemplateService.findOne(newValue).success(
				function (response) {//模板对象
					$scope.brandList=JSON.parse(response.brandIds);//[{"id":46,"text":"五粮液"},{"id":47,"text":"茅台"},{"id":48,"text":"牛栏山"},{"id":28,"text":"农夫山泉"}]
				}
			)
			//获取规格数据
            typeTemplateService.findSpecListByTypeId(newValue).success(
            	function (response) {
					$scope.specList=response;
                }
			)
        }
    });


	//上传
	$scope.uploadFile=function () {
        uploadService.uploadFile().success(
        	function (response) {
                if(response.success){
                    //上传
					$scope.image_entity.url=response.message;
                }else{
                    alert(response.message);
                }
            }
		)
    }

	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
        var id=$location.search()["id"];
        if(id!=undefined){
            goodsService.findOne(id).success(
                function(response){
                    $scope.entity= response;
                    editor.html( $scope.entity.goodsDesc.introduction);
                    $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                    $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

                    for(var i=0;i<$scope.entity.itemList.length;i++){
                        $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
                    }
                }
            );
        }
	}

	//判断规格选项名称在勾选结果中是否存在，如果存在返回true，否则返回false  name规格名称 value规格选项名称
    $scope.isChecked=function (name,value) {
        var specItems=$scope.entity.goodsDesc.specificationItems;//勾选结果
        var object=searchObjectByKey(specItems,name,'attributeName');
        if(object!=null){
        	if(object.attributeValue.indexOf(value)>=0){
        		return true;
			}else{
                return false;
			}
		}else{
        	return false;
		}
    }

    $scope.entity={goods:{isEnableSpec:'1'},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};//商品组合实体类  只定义一次

	$scope.createItemList=function () {
        var specItems=$scope.entity.goodsDesc.specificationItems;//勾选结果
        $scope.entity.itemList=[{spec:{},price:0,num:9999,status:'1',isDefault:'0'}];
		//[{"attributeName":"网络","attributeValue":["移动4G","联通3G"]},{"attributeName":"机身内存","attributeValue":["32G","64G","128G"]},{"attributeName":"颜色","attributeValue":["黑色","土豪金"]}]
		for(var i=0;i<specItems.length;i++){
			//上一次的列表和本一次的规格，生成新的列表
            $scope.entity.itemList=addColumn($scope.entity.itemList,specItems[i].attributeName,specItems[i].attributeValue);
		}
    }
	//list上一次的列表  name 规格名称用来生成列表，key-value中的key  values 本次的规格数据
    addColumn=function (list,name,values) {
		var newList=[];
		for(var i=0;i<list.length;i++){
			var oldRow=list[i];
			for(var j=0;j<values.length;j++){
				var newRow=JSON.parse(JSON.stringify(oldRow));
                newRow.spec[name]=values[j];
                newList.push(newRow);
			}
		}
		return newList;
    }






	//勾选结果$scope.entity.goodsDesc.specificationItems
	//勾选结果结构[{"attributeName":"电视屏幕尺寸","attributeValue":["41英寸","42英寸"]},{"attributeName":"颜色","attributeValue":["黑色","中国红"]}]
	//当前勾选的规格，如果在勾选结果中存在，就向规格选项数组中添加元素，如果不存在，就向勾选结果列表中添加对象
	//1name 规格名称 2value 选项名称
	$scope.updateSpecAttribute=function ($event,name,value) {
        var specItems=$scope.entity.goodsDesc.specificationItems;
        var object = searchObjectByKey(specItems,name,'attributeName');
        if(object!=null){//该规格被勾选过，添加元素
			if($event.target.checked){
                object.attributeValue.push(value);
			}else{//取消勾选，移除元素
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);
                if(object.attributeValue.length==0){//数组中没有元素了，将整个对象从勾选结果中移除
                    specItems.splice(specItems.indexOf(object),1);
				}
			}

		}else{//规格没被勾选过，添加对象
            specItems.push({"attributeName":name,"attributeValue":[value]});
		}
    }

	//判断规格名称在勾选结果中是否存在，存在的话，返回一个对象，如果不存在返回null
	//1list 勾选结构， 2name 规格名称
	searchObjectByKey=function(list,name,key){
		for(var i = 0;i<list.length;i++){
			if(list[i][key]==name){
				return list[i];
			}
		}
		return null;
	}



	//定义图片对象
	//$scope.image_entity={};

	//将图片对象添加到goodsDesc表itemImages数组中
	$scope.add_image_entity=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
        //清空文件上传项的内容
		document.getElementById("file").value="";
    }

    $scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }


	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象
        $scope.entity.goodsDesc.introduction=editor.html();//将editor中的内容赋值给goodsDesc表中的introduction字段
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//推荐方式，跳转到列表页
					//清空页面
                    $scope.entity={goods:{},goodsDesc:{itemImages:[]},itemList:[]};//商品组合实体类 和上面的保存一致
					editor.html('');//清空editor
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	
