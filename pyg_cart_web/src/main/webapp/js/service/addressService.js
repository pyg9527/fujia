app.service('addressService',function ($http) {
    this.findListByUserId=function () {
       return $http.get('/address/findListByUserId.do')
    }
})