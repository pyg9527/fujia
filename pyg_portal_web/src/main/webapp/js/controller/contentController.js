app.controller("contentController",function ($scope,contentService) {
    //集合初始化
    $scope.contentList=[];
    $scope.findByCategoryId=function (id) {
        contentService.findByCategoryID(id).success(
            function (response) {
                $scope.contentList[id]=response;
                // alert(JSON.stringify($scope.contentList[id]))
            }
        )
    }

})