app.controller('cartController', function ($scope, cartService,addressService,orderService) {

    /**
     * 获取购物车列表
     */
    $scope.findCartListFromRedis = function () {
        cartService.findCartListFromRedis().success(
            function (response) {
                $scope.cartList = response;
                sum();
            }
        )
    }

    /**
     * 商品添加购物车列表
     * @param itemId
     * @param num
     */
    $scope.addItemToCartList = function (itemId, num) {
        cartService.addItemToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    //刷新购物车页面
                    $scope.findCartListFromRedis();
                } else {
                    alert(response.message);
                }
            }
        )
    }
//    求合计金额和数量,在查询出购物车的地方调用它
    sum = function () {
        $scope.totalNum = 0;
        $scope.totalMoney = 0;
        for (var i = 0; i < $scope.cartList.length; i++) {
            var cart = $scope.cartList[i];
            for (var j = 0; j < cart.orderItemList.length; j++) {
                $scope.totalMoney += cart.orderItemList[j].totalFee;
                $scope.totalNum += cart.orderItemList[j].num;
            }
        }
    }
    $scope.findAddressList=function () {
        addressService.findListByUserId().success(
            function (response) {
                $scope.addressList=response;
                for(var i=0;i<response.length;i++){
                     if(response[i].isDefault=='1'){
                         $scope.address=response[i];
                     }
                }
            }
        )

    }

    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    $scope.isSelect=function (address) {
        if($scope.address==address){
            return true;
        }
        return false;
    }

    $scope.order={paymentType:'1'} //初始化一个订单对象，用来封装参数
    $scope.selectpaymentType=function (value) {
        alert(value);
        $scope.order.paymentType=value;

    }

    //创建订单
    $scope.createOrder=function () {
        //获取收件人的电话
        $scope.order.receiverMobile=$scope.address.mobile;
        $scope.order.receiverAreaName=$scope.address.address;
        $scope.order.receiver=$scope.address.contact;
        orderService.add($scope.order).success(
            function (response) {
                if(response.success){
                    location.href = "pay.html";  //如果订单生成成功跳转支付页面
                }else{
                    alert(response.message);
                }

            }
        )

    }
})