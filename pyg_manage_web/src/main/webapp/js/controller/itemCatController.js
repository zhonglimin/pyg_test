 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
	
	$scope.grade=1;//默认是第一级
	
	$scope.setGrade=function (value) {
        $scope.grade=value;
        alert($scope.grade);
    }


    $scope.entity_1={};//记录选择的第一级分类对象
    $scope.entity_2={};//记录选择的第二级分类对象
	//记录用户行为的方法
    $scope.selectList=function(entity){
        if($scope.grade==1){//当前页面是第一级分类列表，用户没有任何行为
            $scope.entity_1=null;//记录选择的第一级分类对象
            $scope.entity_2=null;//记录选择的第二级分类对象
        }
        if($scope.grade==2){//级别为2，用户肯定选择了某个第一级分类对象
            $scope.entity_1=entity;
        	$scope.entity_2=null;
        }
        if($scope.grade==3){//级别为3，用户肯定选择了某个第二级分类对象
            $scope.entity_2=entity;
        }
        $scope.findByParentId(entity.id);
    }


    //根据父id查询子级列表
	$scope.findByParentId=function (parentId) {
		itemCatService.findByParentId(parentId).success(
			function (response) {
				$scope.list=response;
            }
		)
    }


	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
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
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	
