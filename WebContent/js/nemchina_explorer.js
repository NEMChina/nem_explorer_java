//nemesis block timeStamp
var NEM_EPOCH = Date.UTC(2015, 2, 29, 0, 6, 25, 0);
var HOST = "http://127.0.0.1:8080/explorer";
//query block list
function getBlockList($scope, $http){
	$http.get(HOST+"/blockList?page="+$scope.page).success(function(response) {
		if(response==null)
			return;
		var list = new Array();
		var item = {};
		for(x in response){
			if(response[x]==null)
				continue;
			item = {};
			item.hash = response[x].hash;
			item.height = response[x].height;
			item.timeStamp = new Date(response[x].timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
			item.txAmount = response[x].txAmount;
			item.txFee = response[x].txFee;
			item.harvester = response[x].harvester;
			item.txes = response[x].txes;
			list.push(item);
		}
		$scope.blockList = list;
	});
}
//query transaction list
function getTXList($scope, $http){
	$http.get(HOST+"/txList?page="+$scope.page).success(function(response) {
		if(response==null)
			return;
		var list = new Array();
		var item = {};
		for(x in response){
			if(response[x]==null)
				continue;
			item = {};
			item.no = response[x].no;
			item.hash = response[x].hash;
			item.height = response[x].height;
			item.timeStamp = new Date(response[x].timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
			item.amount = response[x].amount;
			item.fee = response[x].fee;
			item.sender = response[x].sender;
			item.recipient = response[x].recipient;
			item.signature = response[x].signature;
			item.type = response[x].type;
			list.push(item);
		}
		$scope.txList = list;
	});
}
//query transaction detail
function getTXDetail($scope, $http, height, signature, hash, searchFlag){
	var url = "";
	if(searchFlag){
		url = HOST+"/txDetail?hash="+hash;
	} else {
		url = HOST+"/txDetail?height="+height+"&signature="+signature;
	}
	$http.get(url).success(function(response) {
		if(response==null)
			return;
		var transaction = response;
		if(transaction==null || transaction.timeStamp==null){
			$scope.items = [{label: "查找不到指定的交易信息", content: ""}];
			return;
		}
		var items = new Array();
		var content = "";
		if(height==null){
			height = transaction.height;
		}
		items.push({label: "哈希值", content: hash});
		if(transaction.type==257){ //Initiating a transfer transaction
			items.push({label: "创建时间", content: new Date(transaction.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss")});
			items.push({label: "交易类型", content: "转移交易"});
			items.push({label: "发送方", content: transaction.sender});
			items.push({label: "接收方", content: transaction.recipient});
			items.push({label: "数量", content: transaction.amount});
			items.push({label: "手续费", content: transaction.fee});
			items.push({label: "区块编号", content: height});
			if(transaction.messageType==2){
				items.push({label: "消息(加密)", content: transaction.messageContent});
			} else {
				items.push({label: "消息", content: transaction.messageContent});
			}
		} else if(transaction.type==2049){ //Initiating a importance transfer transaction
			items.push({label: "创建时间", content: new Date(transaction.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss")});
			items.push({label: "交易类型", content: "重要值转移交易"});
			items.push({label: "发送方", content: transaction.sender});
			items.push({label: "接收方", content: transaction.remoteAccount});
			items.push({label: "手续费", content: transaction.fee});
		} else if(transaction.type==4097){ //Converting an account to a multisig account
			items.push({label: "创建时间", content: new Date(transaction.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss")});
			items.push({label: "交易类型", content: "转换为多重签名帐户交易"});
			items.push({label: "发送方", content: transaction.sender});
			items.push({label: "手续费", content: transaction.fee});
			if(transaction.modifications!=null){
				items.push({label: "联署人列表：", content: ""});
				for(i in transaction.modifications){
					var cosignatory = transaction.modifications[i]
					items.push({label: "", content: cosignatory.cosignatoryAccount});
				}
			}
		} else if(transaction.type==4098){ //Cosigning multisig transaction
			
		} else if(transaction.type==4100){ //Initiating a multisig transaction; Adding and removing cosignatories
			items.push({label: "创建时间", content: new Date(transaction.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss")});
			if(transaction.otherTrans!=null && transaction.otherTrans.modifications!=null){
				items.push({label: "交易类型", content: "多重签名帐户联署人变更"});
			} else {
				items.push({label: "交易类型", content: "多重签名交易"});
			}
			items.push({label: "发送方", content: transaction.sender});
			items.push({label: "手续费", content: transaction.fee});
			if(transaction.otherTrans!=null){
				items.push({label: "内层信息", content: ""});
				items.push({label: "", content: "创建时间：" + new Date(transaction.otherTrans.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss")});
				items.push({label: "", content: "发送方：" + transaction.otherTrans.sender});
				items.push({label: "", content: "接收方：" + transaction.otherTrans.recipient});
				items.push({label: "", content: "手续费：" + transaction.otherTrans.fee});
				if(transaction.otherTrans.modifications!=null){
					var modifications = transaction.otherTrans.modifications;
					for(x in modifications){
						if(modifications[x].modificationType==1){ //add cosignatory
							items.push({label: "   增加联署人 - ", content: modifications[x].cosignatoryAccount});
						} else if(modifications[x].modificationType==1){ //delete cosignatory
							items.push({label: "   移除联署人 - ", content: modifications[x].cosignatoryAccount});
						}
					}
				}
			}
			if(transaction.signatures!=null && transaction.signatures.length>0){
				items.push({label: "联署信息", content: ""});
				var signatures = transaction.signatures;
				for(x in signatures){
					var tDate = new Date(signatures[x].timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
					items.push({label: "   ", content: "(" + tDate + ") " + signatures[x].sender});
				}
			}
		} else if(transaction.type==8193){ //Provisioning a namespace
			
		} else if(transaction.type==16385){ //Creating a mosaic definition
			
		} else if(transaction.type==16385){ //Changing the mosaic supply
			
		}
		$scope.items = items;
	});
}
//app
var app = angular.module('content', []);
//nav
app.controller('navCtrl', function($location, $scope) {
	$scope.go = function(page){
		window.location.href = page;
	};
	$scope.search = function(){
		var searchContent = $.trim($("#searchInput").val());
		var reg_block = /^\d+$/;
		var reg_tx= /^\w{64}$/;
		var reg_account= /^\w{40}$/;
		var reg_account2= /^\w{6}-\w{6}-\w{6}-\w{6}-\w{6}-\w{6}-\w{4}$/;
		if(searchContent==null || searchContent==""){
			$("#warningContent").html("请填写 [区块编号] 、[交易哈希值] 或 [帐户地址]");
			$("#warning").attr("class", "alert alert-warning");
			$("#warning").show();
		} else if(!reg_block.test(searchContent) 
				&& !reg_tx.test(searchContent) 
				&& !reg_account.test(searchContent) 
				&& !reg_account2.test(searchContent)){
			$("#warningContent").html("请正确填写 [区块编号] 、[交易哈希值] 或 [帐户地址]");
			$("#warning").attr("class", "alert alert-warning");
			$("#warning").show();
		} else {
			if(reg_block.test(searchContent)){
				window.open("s_block.html?height="+searchContent);
			} else if(reg_tx.test(searchContent)){
				window.open("s_tx.html?hash="+searchContent);
			} else if(reg_account.test(searchContent) || reg_account2.test(searchContent)){
				window.open("s_account.html?account="+searchContent);
			} 
		}
	};
});
//load block list on the page
app.controller('blockListCtrl', function($scope, $http) {
	$scope.page = 1;
	$scope.showBlockTransactionsFlag = false;
	getBlockList($scope, $http);
	//load transactions in block
	$scope.showBlockTransactions = function(txes, $event){
		//just skip the action when click from <a>
		if($event!=null && $event.target!=null && $event.target.localName=="a"){
			return;
		}
		var txArr = [];
		var tx = {};
		for(i in txes){
			if(txes[i]!=null && txes[i].tx!=null){
				tx = {};
				tx.hash = txes[i].hash;
				tx.time = new Date(txes[i].tx.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
				tx.amount = txes[i].tx.amount;
				tx.fee = txes[i].tx.fee;
				tx.sender = txes[i].tx.signerAccount;
				tx.recipient = txes[i].tx.recipient;
				tx.height = txes[i].tx.height;
				tx.signature = txes[i].tx.signature;
				txArr.push(tx);
			}
		}
		if(txArr.length>0){
			$scope.showBlockTransactionsFlag = true;
		} else {
			$scope.showBlockTransactionsFlag = false;
		}
		$scope.txList = txArr;
	};
	//load transaction detail
	$scope.showTransaction = function(height, signature, hash, $event){
		//just skip the action when click from <a>
		if($event!=null && $event.target!=null && $event.target.className.indexOf("noDetail")!=-1){
			return;
		}
		$("#txDetail").modal("show");
		getTXDetail($scope, $http, height, signature, hash, false);
	};
	$scope.previousPage = function(){
		if($scope.page>1){
			$scope.page--;
			getBlockList($scope, $http);
			$scope.showBlockTransactionsFlag = false;
		}
	};
	$scope.nextPage = function(){
		$scope.page++;
		getBlockList($scope, $http);
		$scope.showBlockTransactionsFlag = false;
	};
});

//load tx list on the page
app.controller('txListCtrl', function($scope, $http) {
	$scope.page = 1;
	getTXList($scope, $http);
	//load transaction detail
	$scope.showTransaction = function(height, signature, hash, $event){
		//just skip the action when click from <a>
		if($event!=null && $event.target!=null && $event.target.className.indexOf("noDetail")!=-1){
			return;
		}
		$("#txDetail").modal("show");
		getTXDetail($scope, $http, height, signature, hash, false);
	};
	$scope.previousPage = function(){
		if($scope.page>1){
			$scope.page--;
			getTXList($scope, $http);
		}
	};
	$scope.nextPage = function(){
		$scope.page++;
		getTXList($scope, $http);
	};
});

//load account list
app.controller('accountListCtrl', function($scope, $http) {
	$scope.selectOptions = [
        {"key": "Top100 - 帐户余额排行", "value": 1},
        {"key": "Top100 - 收获排行", "value": 2}
	];
	$scope.select = $scope.selectOptions[0];
	$scope.changeSelectOption = function(){
		if($scope.select.value==1){
			getAccountList($scope, $http);
		} else if($scope.select.value==2) {
			getHarvesterList($scope, $http);
		}
	}
	$scope.changeSelectOption();
});

//query account list
function getAccountList($scope, $http){
	$http.get(HOST+"/accountList").success(function(response) {
		if(response==null)
			return;
		var list = new Array();
		var item = {};
		for(x in response){
			if(response[x]==null)
				continue;
			item = {};
			item.account = response[x].account;
			item.balance = response[x].balance;
			item.importance = response[x].importance;
			item.timeStamp = new Date(response[x].timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
			list.push(item);
		}
		$scope.accountList = list;
	});
}

//query harvester list
function getHarvesterList($scope, $http){
	$http.get(HOST+"/harvesterList").success(function(response) {
		if(response==null)
			return;
		var list = new Array();
		var item = {};
		for(x in response){
			if(response[x]==null)
				continue;
			item = {};
			item.account = response[x].account;
			item.importance = response[x].importance;
			item.blocks = response[x].blocks;
			item.fees = response[x].fees;
			item.lastBlock = response[x].lastBlock;
			list.push(item);
		}
		$scope.harvesterList = list;
	});
}

//load node list
app.controller('nodeListCtrl', function($scope, $http) {
	getNodeList($scope, $http);
});

//query node list
function getNodeList($scope, $http){
	$http.get(HOST+"/nodeList").success(function(response) {
		if(response==null)
			return;
		var list = new Array();
		var item = {};
		for(x in response){
			if(response[x]==null)
				continue;
			item = {};
			item.host = response[x].host;
			item.name = XBBCODE.process({
				text: response[x].name,
				removeMisalignedTags: true,
				addInLineBreaks: false
			}).html;
			item.version = response[x].version;
			item.superNodeID = response[x].superNodeID;
			list.push(item);
		}
		$scope.nodeList = list;
	});
}

//query supernode payout list
function getSuperNodePayoutList($scope, $http){
	if($scope.select.value!=null){
		$http.get(HOST+"/supernodePayoutList?round="+$scope.select.value).success(function(response) {
			if(response==null)
				return;
			var list = new Array();
			var item = {};
			for(x in response){
				if(response[x]==null)
					continue;
				item = {};
				item.round = (response[x].round-3) + "-" + response[x].round;
				var sender = "<a href='s_account.html?account="+response[x].sender+"' target='_blank'>"+response[x].sender+"</a>";
				var recipient = "<a href='s_account.html?account="+response[x].recipient+"' target='_blank'>"+response[x].recipient+"</a>";
				item.senderAndRecipient = sender + "<br/>" + recipient;
				item.amount = response[x].amount;
				item.fee = response[x].fee;
				item.timeStamp = new Date(response[x].timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
				list.push(item);
			}
			$scope.payoutList = list;
		});
	}
}

//load supernode payout list
app.controller('payoutListCtrl', function($scope, $http) {
	$scope.changeSelectOption = function(){
		getSuperNodePayoutList($scope, $http);
	}
	//supdernode payout round
	$http.get(HOST+"/supernodePayoutRoundList").success(function(response) {
		if(response==null)
			return;
		var list = new Array();
		var item = {};
		for(x in response){
			if(response[x]==null)
				continue;
			item = {};
			item.key = response[x].key;
			item.value = response[x].value;
			list.push(item);
		}
		$scope.selectOptions = list;
		$scope.select = $scope.selectOptions[0];
		$scope.changeSelectOption();
	});
});

//search block
app.controller('searchBlockCtrl', function($scope, $http, $location) {
	var absUrl = $location.absUrl();
	if(absUrl==null){
		return;
	}
	var reg = /height=([0-9]+)/;
	if(absUrl.match(reg).length==2){
		var height = absUrl.match(reg)[1];
		$http.get(HOST+"/blockDetail?height="+height).success(function(response) {
			if(response==null || response.timeStamp==null){
				$scope.blockItems = [{label: "查找不到指定的区块信息", content: ""}];
				return;
			}
			response.timeStamp = new Date(response.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
			//load block detail
			var list = [
			    {label: "编号", content: response.height},
			    {label: "创建时间", content: response.timeStamp},
			    {label: "区块难度", content: response.difficulty},
			    {label: "交易数", content: response.txAmount},
			    {label: "手续费", content: response.txFee},
			    {label: "收获者", content: response.signer},
			    {label: "区块哈希值", content: response.hash}
			];
			$scope.blockItems = list;
			//load tx list
			if(response.txes==null){
				return;
			}
			var txArr = [];
			var tx = {};
			for(i in response.txes){
				if(response.txes[i]!=null && response.txes[i].tx!=null){
					tx = {};
					tx.hash = response.txes[i].hash;
					tx.time = new Date(response.txes[i].tx.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
					tx.amount = response.txes[i].tx.amount;
					tx.fee = response.txes[i].tx.fee;
					tx.sender = response.txes[i].tx.signerAccount;
					tx.recipient = response.txes[i].tx.recipient;
					tx.height = response.txes[i].tx.height;
					tx.signature = response.txes[i].tx.signature;
					txArr.push(tx);
				}
			}
			if(txArr.length>0){
				$scope.showBlockTransactionsFlag = true;
			} else {
				$scope.showBlockTransactionsFlag = false;
			}
			$scope.txList = txArr;
		});
	}
	//load transaction detail
	$scope.showTransaction = function(height, signature, hash, $event){
		//just skip the action when click from <a>
		if($event!=null && $event.target!=null && $event.target.className.indexOf("noDetail")!=-1){
			return;
		}
		$("#txDetail").modal("show");
		getTXDetail($scope, $http, height, signature, hash, false);
	};
});

//search transaction
app.controller('searchTransactionCtrl', function($scope, $http, $location) {
	var absUrl = $location.absUrl();
	if(absUrl==null){
		return;
	}
	var reg = /hash=(\w{64})/;
	if(absUrl.match(reg).length==2){
		var hash = absUrl.match(reg)[1];
		getTXDetail($scope, $http, null, null, hash, true);
	}
});

//search account
app.controller('searchAccountCtrl', function($scope, $http, $location) {
	var absUrl = $location.absUrl();
	if(absUrl==null){
		return;
	}
	var reg = /account=(\w{40}|\w{46})/;
	if(absUrl.match(reg).length==2){
		var account = absUrl.match(reg)[1];
		$http.get(HOST+"/accountDetail?account="+account).success(function(response) {
			if(response==null || response.account==null){
				alert(11);
				$scope.accountItems = [{label: "查找不到指定的帐户信息", content: ""}];
				return;
			}
			//load account detail
			var list = [];
			list.push({label: "地址", content: response.account});
			list.push({label: "公钥", content: response.publicKey});
			list.push({label: "余额", content: response.balance});
			list.push({label: "重要值", content: response.importance});
			if(response.timeStamp!=null){
				list.push({label: "最后变动时间", content: new Date(response.timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss")});
			}
			if(response.remoteStatus!=null && response.remoteStatus=="ACTIVE"){
				list.push({label: "远程收获", content: "已开启"});
			} else {
				list.push({label: "远程收获", content: "未开启"});
			}
			if(response.blocks!=null){
				list.push({label: "收获区块数", content: response.blocks});
			}
			if(response.blocks!=null){
				list.push({label: "收获手续费", content: response.fees});
			}
			if(response.cosignatories!=null && response.cosignatories!=""){
				list.push({label: "多重签名帐户", content: "是"});
				list.push({label: "联署人", content: response.cosignatories});
			}
			$scope.accountItems = list;
			//load tx list
			if(response.txes==null){
				return;
			}
			var txList = [];
			var tx = {};
			for(i in response.txes){
				if(response.txes[i]!=null){
					tx = {};
					tx.hash = response.txes[i].hash;
					if(response.txes[i].timeStamp!=null){
						tx.time = new Date(response.txes[i].timeStamp*1000 + NEM_EPOCH).format("yyyy-MM-dd hh:mm:ss");
					} else {
						tx.time = "";
					}
					tx.amount = response.txes[i].amount;
					tx.fee = response.txes[i].fee;
					tx.sender = response.txes[i].sender;
					tx.recipient = response.txes[i].recipient;
					tx.height = response.txes[i].height;
					tx.signature = response.txes[i].signature;
					txList.push(tx);
				}
			}
			$scope.txList = txList;
		});
	}
	//load transaction detail
	$scope.showTransaction = function(height, signature, hash, $event){
		//just skip the action when click from <a>
		if($event!=null && $event.target!=null && $event.target.localName=="a"){
			return;
		}
		$("#txDetail").modal("show");
		getTXDetail($scope, $http, height, signature, hash, false);
	};
});

//date format
Date.prototype.format = function(fmt) {
	var o = {
		"M+" : this.getMonth()+1,
		"d+" : this.getDate(),
		"h+" : this.getHours(),
		"m+" : this.getMinutes(),
		"s+" : this.getSeconds(),
		"q+" : Math.floor((this.getMonth()+3)/3)
	}; 
	if(/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
	for(var k in o) 
		if(new RegExp("("+ k +")").test(fmt)) 
	fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); 
	return fmt; 
}

app.filter('to_trusted', ['$sce', function ($sce) {
	return function (text) {
	    return $sce.trustAsHtml(text);
	};
}]);
