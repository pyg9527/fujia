app.service("contentService",function ($http) {
    this.findByCategoryID=function (categoryId) {
        return $http.get('content/findByCategoryId.do?categoryId='+categoryId)

    }
})