app.controller("loginController",function ($scope,loginService) {

    $scope.showLoginName=function () {
        loginService.getName().success(
            function (response) {
                $scope.user=response;
            }
        )
    }
})