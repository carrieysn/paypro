//加载layer扩展方法
layer.config({
    extend: 'extend/layer.ext.js'
});

/**字符串截取**/
if(!String.prototype.startsWith){
	String.prototype.startsWith=function(pattern){  
		return this.indexOf(pattern) === 0;
	}  
}

//art-template模板扩展
//日期格式化
template.helper('dateFormat', function (date, format) {
    date = new Date(date);
    var map = {
        "M": date.getMonth() + 1, //月份 
        "d": date.getDate(), //日 
        "h": date.getHours(), //小时 
        "m": date.getMinutes(), //分 
        "s": date.getSeconds(), //秒 
        "q": Math.floor((date.getMonth() + 3) / 3), //季度 
        "S": date.getMilliseconds() //毫秒 
    };
    format = format.replace(/([yMdhmsqS])+/g, function(all, t){
        var v = map[t];
        if(v !== undefined){
            if(all.length > 1){
                v = '0' + v;
                v = v.substr(v.length-2);
            }
            return v;
        }
        else if(t === 'y'){
            return (date.getFullYear() + '').substr(4 - all.length);
        }
        return all;
    });
    return format;
});

//格式化小数方法
template.helper('formatNumber', function (value) {
	return parseFloat(value || '0').toFixed(2);
});

/**将数据中的json字符串解析出来**/
template.helper('parseJson', function (data, key) {
	if(data[key]){
		data[key] = JSON.parse(data[key]);
	} else{
		data[key] = {};
	}
    return;
});

var M = {
	
	/***
	 * 页面统一弹出框
	 * @param msg 提示信息
	 * @param fn 加调方法
	 */
	alert: function(msg, fn){
		fn = $.isFunction(fn) ? fn : function(){};
		layer.msg(msg, {
			time : 2000
		}, fn);
	},
	
	/***
	 * 将数字格式化为金钱
	 * @param s 数字
	 * @param n 保留的小数位
	 */
	fmoney: function(s, n) { 
		n = n > 0 && n <= 20 ? n : 0; 
		//需要对负数特殊处理
		var startChar = '';
		if(parseFloat(s) < 0){
			s = parseFloat(s) * -1;
			startChar = '-';
		}
		s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + ""; 
		var l = s.split(".")[0].split("").reverse(), r = s.split(".")[1]; 
		t = ""; 
		for (i = 0; i < l.length; i++) { 
		t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : ""); 
		} 
		return startChar + t.split("").reverse().join("") + (n == 0 ? "" : "." + r); 
	},
	
	/***
	 * 日期格式化
	 * @param date 日期
	 * @param format 格式化字符串
	 */
	dateFormat: function(date, format){
		 date = new Date(date);
	     var map = {
	        "M": date.getMonth() + 1, //月份 
	        "d": date.getDate(), //日 
	        "h": date.getHours(), //小时 
	        "m": date.getMinutes(), //分 
	        "s": date.getSeconds(), //秒 
	        "q": Math.floor((date.getMonth() + 3) / 3), //季度 
	        "S": date.getMilliseconds() //毫秒 
	     };
	     format = format.replace(/([yMdhmsqS])+/g, function(all, t){
	        var v = map[t];
	        if(v !== undefined){
	            if(all.length > 1){
	                v = '0' + v;
	                v = v.substr(v.length-2);
	            }
	            return v;
	        }
	        else if(t === 'y'){
	            return (date.getFullYear() + '').substr(4 - all.length);
	        }
	        return all;
	     });
	     return format;
	},
	
	/**
	 * 把form表单的元素转换成属性与值对应
	 * @param eleForm 表单元素
	 **/
	formatElement:function(eleForm){
		var beanForm = $(eleForm);
		var eleArray = beanForm.serializeArray(); 
		if(eleArray != ''){
			var eleObj = {};
			for (var i = 0; i < eleArray.length; i++) { 
				eleObj[eleArray[i].name] = $.trim(eleArray[i].value);
			}
			return eleObj;
		}
		return {};
	},
	
	/****
	 * 加载列表数据
	 */
	loadTableData: function(table, params){
		var url = $(table).attr('url'), templateId = $(table).attr('template') || '', params = params || {};
		var loading = layer.load();
		M.post(url, params, function(result){
			layer.close(loading);
			var html = template(templateId, {list: result.data});
			$(table).html(html);
			$(table).data('isLoad',true);
		}, function(result){
			layer.close(loading);
			M.alert(result.msg || '加载列表失败！');
		});
	},
	
	/**
	 * 阻止事件冒泡
	 * 
	 * @param eve 事件对象
	 */
	stopEvent:function(eve) {
    	var ev = window.event ||eve;
        if (ev && ev.stopPropagation) {
            //W3C取消冒泡事件
            ev.stopPropagation();
        } else {
            //IE取消冒泡事件
        	ev.cancelBubble = true;
        }
    },
	
	/**
	 * 公用的ajax请求方式 
	 * @param url
	 * @param params
	 * @param succussFn
	 * @param errorFn
	 */
	post: function(url, params, succussFn, errorFn){
		 $.ajax({
	        type: 'POST',
	        url: url,
	        data: params,
	        success: function(data) {
	            if (data.status == '1') {   //成功
	            	if($.isFunction(succussFn)){
	            		succussFn(data);
	            	}
	            } else if (data.status == '-1') {   //需要登录
	            	M.alert('请刷新页面后重新登录操作！');
	            	//window.location.href = '/portal/login?return_url=' + data.msg;
	            } else {
	                if(errorFn) {
	                	if($.isFunction(errorFn)){
	                		errorFn(data);
	                	}
	            	}
	            }
	        },
	        error: function(data) {
	        	if(!(data && data.readyState == '0' && data.status == '0')){	//解决火狐狂刷页面请求出错问题
	        		var errData = {
	        				status: '0',
	        				msg: data.msg
	        		}
	        		if(errorFn) {
	        			if($.isFunction(errorFn)){
	        				errorFn(errData);
	        			}
	        		}
	        	}
	        }
	    });
	}
}