app.service('cartService',function ($http) {
    this.findCartListFromRedis=function () {
        return $http.get('/cart/findCartListFromRedis.do')
    }


    this.addItemToCartList=function (itemId,num) {
        return $http.get('/cart/addItemToCartList.do?itemId='+itemId+'&num='+num);
    }
})