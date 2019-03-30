//控制层
app.controller('goodsController', function ($scope, $controller, itemCatService, goodsService, typeTemplateService, uploadService, $location) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        id = $location.search()['id'];
        // console.log(id)
        // alert(id);
        if (id != null) {
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    // $scope.entity.goodsDesc.introduction=editor.html(); 这是取值
                    editor.html($scope.entity.goodsDesc.introduction);//获取商品描述
                    //将图片，规格，自定义属性转换成json串
                    $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                    $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                    for(var i=0;i< $scope.entity.itemList.length;i++){
                        $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec)
                    }
                }
            )
        }

    };

    $scope.checkAttributeValue=function(specName,optionName){
        var items= $scope.entity.goodsDesc.specificationItems;
        var object= searchObjectByKey(items,'attributeName',specName);
        if(object==null){
            return false;
        }else{
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else{
                return false;
            }
        }
    }


    //保存
    $scope.save = function () {
        $scope.entity.goodsDesc.introduction = editor.html();//富文本编辑器的取值
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    alert(response.message);
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //显示一级商品分类

    $scope.findByParentId = function (pid) {
        // alert(id)
        itemCatService.findByParentId(pid).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        )

    }

//显示商品二级分类，用到$watch
    $scope.$watch('entity.goods.category1Id', function (newvalue, oldvalue) {

        /* itemCatService.findByParentId(0).success(
             function (response) {
                 $scope.itemCat1List=response;
             }
         )
 */
        itemCatService.findByParentId(newvalue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        )
    })

    //显示商品三级分类，用到$watch
    $scope.$watch('entity.goods.category2Id', function (newvalue, oldvalue) {
        itemCatService.findByParentId(newvalue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        )
    })
    //显示模板版本号 ,并将模板id，text，options等查询出。
    $scope.$watch('entity.goods.category3Id', function (newvalue, oldvalue) {
        itemCatService.findOne(newvalue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        )
    })

    //初始化
    $scope.entity = {goodsDesc: {customAttributeItems: []}};
//	$scope.entity.goodsDesc.customAttributeItems=response.customAttributeItems;
//[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
    $scope.$watch('entity.goods.typeTemplateId', function (newvalue, oldvalue) {
        typeTemplateService.findOne(newvalue).success(
            function (response) {
                $scope.typeTemplate = response;
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                if ($location.search()['id'] == null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }
            }
        );
        typeTemplateService.findSpecList(newvalue).success(
            function (response) {
                $scope.specList = response
            }
        )
    })

    $scope.image_entity = {};
    //[{"color":"红色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINKADo__AAjlKdWCzvg874.jpg"},]
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.image_entity.url = response.message;
                } else {
                    alert(response.message)
                }
            }
        )
    }

    //对图片属性初始化
    $scope.entity = {goodsDesc: {customAttributeItems: [], itemImages: [], specificationItems: []}};
    $scope.addImage = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }
    $scope.deleImage = function ($index) {
        $scope.entity.goodsDesc.itemImages.splice($index, 1);
    }
    searchObjectByKey = function (list, key, value) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == value) {
                return list[i];
            }

        }
        return null;
    };
    //"updateSpecAttribute($event,spec.text,option.optionName)
    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            //判断选择状态
            if ($event.target.checked) {
                object.attributeValue.push(value)
            } else {
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                if (object.attributeValue.length < 1) {
                    // $scope.entity.goodsDesc.specificationItems={}; 不能将整个赋成空值，因为可能有多个 规格
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1)
                }
            }
        } else {
            // $scope.entity.goodsDesc.specificationItems={"attributeName":"","attributeValue":[]}; 变成了创建空对象，没有对其进行赋值
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }


    //创建出封装对象的itemList
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];// 初始
        var items = $scope.entity.goodsDesc.specificationItems; //用户勾选的规格&规格选项
        for (var i = 0; i < items.length; i++) {   //循环用户所有的规格
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }

    //list = [ {spec : {},price : 0,num : 99999,status : '0',isDefault : '0'} ];  columnName=规格名称  ;conlumnValues = 规格选项集合
    addColumn = function (list, columnName, conlumnValues) {
        var newList = [];// 新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];  //获取出当前行的内容 {spec:{},price:'0.01',num:'99999',status:'0',isDefault:'0'}
            for (var j = 0; j < conlumnValues.length; j++) {//循环attributeValue数组的内容
                var newRow = JSON.parse(JSON.stringify(oldRow));// 深克隆,根据attributeValue的数量
                //相当于key value，给他赋值
                newRow.spec[columnName] = conlumnValues[j];//{spec:{"网络制式":"移动4G"},price:'0.01',num:'99999',status:'0',isDefault:'0'}
                newList.push(newRow);
            }
        }
        return newList;
    }
    $scope.status = ['草稿', '未审核', '审核通过', '审核未通过', '关闭商家', '上架', '下架'];
    $scope.itemCatList = [];
    //查询所有分类数据
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        )
    }
    //提交审核
    $scope.updateStatus = function (status) {
        goodsService.updateStatus($scope.selectIds, status).success(
            function (response) {
                if (response.success) {
                    //刷新列表
                    $scope.reloadList();
                    $scope.selectIds = []; //将多选ids清空
                } else {
                    alert(response.message);
                }
            }
        )
    }
});	
