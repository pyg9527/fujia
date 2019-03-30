app.controller('payController',function ($scope,payService) {
    $scope.createNative=function () {
        payService.createNative().success(
            function (response) {
                $scope.out_trade_no = response.out_trade_no; //支付单号
                $scope.total_fee = response.total_fee; //总金额
                    var qr = window.qr = new QRious({
                        element: document.getElementById('qrious'),
                        size: 250,
                        value: response.code_url,
                        level:'H'
                    })
            }
        )
    }

    $scope.queryPayStatus=function () {
        payService.queryPayStatus($scope.out_trade_no).success(
            function (response) {
                if(response.success){
                    location.href='paysuccess.html';
                }else {
                    if(response.message=="timeout"){
                        $scope.createNative(); //如果超时，重新生成一个二维码。。如果关闭页面了，没有j是，就没法加载了。
                    }else {
                        location.href='payfail.html';
                    }
                }
            }
        )

    }
})