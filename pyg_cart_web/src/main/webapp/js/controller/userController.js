 //控制层 
app.controller('userController' ,function($scope   ,userService){
	

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=userService.update( $scope.entity ); //修改  
		}else{
			serviceObject=userService.add( $scope.entity  );//增加 
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
		userService.dele( $scope.selectIds ).success(
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
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	
	//用户注册
	$scope.reg=function () {
		//1.两次密码是否一致
        if($scope.entity.password!=$scope.password)  {
            alert("两次输入的密码不一致，请重新输入");
            return ;
        }
        //2. 用户名是否可用

		//3. 手机号是否合法

		if($scope.smscode == null || $scope.smscode == ''){
			alert("请输入验证码");
			return;
		}

		userService.add($scope.entity,$scope.smscode).success(
			function (response) {
				alert(response.message);
            }
		);
    }

    //发送短信验证码的按钮
	$scope.sendCheckCode = function () {

		//1. 校验手机号

		userService.sendCheckCode($scope.entity.phone).success(
			function (response) {
				alert(response.message);
            }
		);

		
    }
    
});	
