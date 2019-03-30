app.controller("itemController",function($scope,$http){

    $scope.num=1;
    $scope.spec={};
    $scope.addNum=function(value){
        $scope.num+=value;
        if($scope.num<1){
            $scope.num=1
        }
    }

    //用户选择规格
    $scope.selectSpec=function(key,value){
        $scope.spec[key]=value;
        searchSku();

    }
    //判断某规格是否被选中
    $scope.isSelected=function(key,value){
        if($scope.spec[key]==value){
            return true;
        }else{
            return false;
        }
    }
    //默认勾选
    $scope.sku={} ;//将itemList中的数据存进去
    $scope.loadSku=function(){
        $scope.sku=skuList[0];
        //在深克隆，脱离关系，不然勾选时spec改变skuList中的默认也改变
        $scope.spec= JSON.parse(JSON.stringify($scope.sku.spec)) ;
    }//修改itemController.js
    //查询
    searchSku=function(){
        for(var i=0;i< skuList.length;i++  ){
            if( matchObject (skuList[i].spec, $scope.spec) ){
                $scope.sku=skuList[i];
                return;
            }
        }
        $scope.sku={id:0,title:'--------',price:0};//如果没有匹配的，则设置默认值
    }

    //匹配两个对象是否相等
    matchObject=function(map1,map2){
        for(var k in map1){
            if(map1[k]!=map2[k]	){
                return false;
            }
        }
        for(var k in map2){
            if(map1[k]!=map2[k]	){
                return false;
            }
        }
        return true;
    }
    //添加购物车跳转
    $scope.addItemToCartList=function () {
    	$http.get('http://localhost:9107/cart/addItemToCartList.do?itemId='+$scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
    		function (response) {
				if(response.success){
					location.href="http://localhost:9107"
				}else {
					alert(response.message)
				}
            }
		)
    }
});