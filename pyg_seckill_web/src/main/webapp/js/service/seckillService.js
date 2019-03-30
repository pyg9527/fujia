app.service('seckillService',function($http){
	this.findSeckillList = function(){
		return $http.get('seckillGoods/findSeckillGoods.do');
	}
	
	this.findOne = function(id){
		return $http.get('seckillGoods/findOne.do?id='+id);
	}
	
	this.saveSeckillOrder = function(id){
		return $http.get('seckillGoods/saveSeckillOrder.do?id='+id);
	}
})