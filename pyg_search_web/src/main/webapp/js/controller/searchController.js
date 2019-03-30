app.controller("searchController",function ($scope,searchService) {

    $scope.searchMap={keywords:"三星",spec:{},sort:'ASC'};

    $scope.search=function () {
        $scope.searchMap.pageNum=$scope.paginationConf.currentPage;
        $scope.searchMap.pageSize=$scope.paginationConf.itemsPerPage;
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.list=response.content;
               // alert(JSON.stringify($scope.list))
                $scope.paginationConf.totalItems=response.total;
            }
        )
    }


    $scope.addFilterCondition=function (key, value) {
        if(  key=='category' ||key=='brand' ||key=='price'){
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();
    }

    $scope.sortCondition=function (value) {
        $scope.searchMap.sort=value;
        $scope.search();
    }


    //清除搜索条件的
    $scope.removeSearchItem=function (key) {
        if(  key=='category' ||key=='brand' ||key=='price'){
            delete $scope.searchMap[key]; //delete是前端删除map中的key和value的方法
        }else{
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,  //当前页
        totalItems: 10,  //总记录数
        itemsPerPage: 10,  //每页记录数
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.search();//刷新
        }
    };
});

//添加过滤器，实现能将标签输出，要不然高亮显示的标签原样输出
app.filter("trustHtml",['$sce',function($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}])