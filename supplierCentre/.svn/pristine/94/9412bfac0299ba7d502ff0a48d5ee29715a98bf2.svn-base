/***
 * 分页插件信息
 */
(function($){
	M.paginator = function(target){
		 var tables = target ? $(target) : $(".page-list-table");
		 if(tables.length > 0){
			 $(tables).each(function(){
				 var table = this;
				 var lazyLoad = $(table).attr('lazyLoad') || false; //是否立即加载数据，默认为false
                 if(lazyLoad == 'true') {
                     return;
                 }
				 var url = $(table).attr('url') || '';               //数据请求的url
				 var pageSize = parseInt($(table).attr('pageSize') || 15);      //每页展示的记录条数，默认15条
				 var pages = ($(table).attr("pages") || '50,100,200').split(',');	//可选的每页显示记录数量
	             var templateId = $(table).attr('template') || '';        //渲染模板id
	             var visiblePages = parseInt($(table).attr('visiblePages') || 10);   //分页导航条展示页数，默认10页
	             var params = JSON.parse($(table).attr('params') || '{}'); //额外的过滤参数
	             var nullMsg = $(table).attr('nullMsg') || '没有查询到相关记录!'; //未查询到数据时的提示信息
	             var isNullMsg = $(table).attr('isNullMsg') || 'true';  //是否显示暂无数据信息
	             var onLoadSuccess = $(table).attr('onLoadSuccess'); //分页加载成功后，调用事件
	             if($(table).next(".my_pagination").length > 0){
	            	 $(table).next(".my_pagination").remove();
	             }
	             var page = $('<div class="page my_pagination"><span class="calendar">共有<em class="page_total_em">0</em>条记录</span>' + 
	            		 '<span><select></select></span>' + 
	            		 '<span class="pagination"></span><a class="page_info" href="javascript:void(0);">0/0</a>' + 
	            		 '<span><input type="number" class="page-number" /><input type="submit" class="page-go" value="GO" /></span>' + 
	            		 '</div>').insertAfter(table);
	             //渲染可选的每页显示记录下拉框
	             if(pages && pages.length > 0){
	            	 var selHtml = '', hasPageSize = true;
	            	 for(var i = 0;i < pages.length;i++){
	            		 var value = pages[i];
	            		 selHtml += '<option value="' + value + '">' + value + '</option>';
	            		 if(hasPageSize && value == pageSize){
	            			 hasPageSize = false;
	            		 }
	            	 }
	            	 if(hasPageSize){
	            		 //说明没有用户默认的分页数，加入select下拉框中
	            		 selHtml = '<option value="' + pageSize + '">' + pageSize + '</option>' + selHtml; 
	            	 }
	            	 $(page).find('select').html(selHtml);
	             }
	             var pagination = $(page).find(".pagination").first(); //分页插件
	             var page_info = $(page).find(".page_info");	//分布显示的当前面/总页面信息
	             var pageTarget = $(pagination).jqPaginator({
	                 totalPages: 1,
	                 visiblePages: visiblePages,
	                 currentPage: 1,
	                 pageSize: pageSize,
	                 activeClass: 'sel',
	                 first: '<a href="javascript: void(0);">首页</a>',
	                 last: '<a href="javascript: void(0);">尾页</a>',
	                 prev: '<a href="javascript: void(0);">上一页</a>',
	                 next: '<a href="javascript: void(0);">下一页</a>',
	                 page: '<a href="javascript: void(0);" page="{{page}}">{{page}}</a>',
	                 onPageChange: function (pageNum, type) {    //点击页码时更新数据
	                	 $.extend(params, {pageNum: pageNum, token: token, pageSize: pageSize});
	                	 loadData(params, type);
	                 }
	             });
	             
	             //跳转到指定页面，并验证用户输入页数
	             $(page).find(".page-go").click(function(){
	            	try{
	            		var value = parseInt($(page).find("input[type='number']").val() || "0");
	            		var jqPaginator = pageTarget.data("jqPaginator");
	            		if(value > 0 && value <= parseInt(jqPaginator.options.totalPages)){
	            			jqPaginator.options.currentPage = value;
	            			jqPaginator.render();
	            			jqPaginator.fireEvent(value, null);
	            		} else{
	            			M.alert('请输入有效页数！');
	            		}
	            	} catch(e){}
	             });
	             
	             //选择每页显示多少条记录
	             $(page).find('select').change(function(){
	            	 var value = parseInt(this.value);
	            	 var jqPaginator = pageTarget.data("jqPaginator");
	            	 jqPaginator.options.pageSize = value;
	            	 jqPaginator.options.currentPage = 1;
	            	 pageSize = value;
	            	 jqPaginator.init();
	             });
	             
	             //加载数据
	             function loadData(params, type){
	            	 var load = layer.load();
	            	 M.post(url, params, function(data) {
	            		 var page = data.data;
                    	 if(page){
                    		 if(type == 'init'){  //如果是初始化操作，将相关信息设置到分页插件中
                        		 var totalPages = page.pages;
                        		 if(totalPages != 0) {
                        			 $(pagination).jqPaginator('option', {
                        				 totalPages: page.pages
                        			 });
                        		 } else{
                        			 if(isNullMsg == 'true'){
                        				 layer.msg(nullMsg);
                        			 }
                        			 if($(table).next(".my_pagination").length > 0){
                    	            	 $(table).next(".my_pagination").remove();
                    	             }
                        		 }
                        		 $(table).next(".my_pagination").find(".page_total_em").text(page.total || 0);
                        	 }
                             var pageData = {};
                             pageData.list = page.result;
                             pageData.list.pageNum = params.pageNum;        // 将当前页码和每页显示条数传递到页面，用于有些地方的序号展示
                             pageData.list.pageSize = params.pageSize;
                             var pageHtml = template(templateId, pageData);
                             $(table).html(pageHtml);
                             
                             //更改显示页面
                             $(page_info).text(page.pageNum + "/" + page.pages);
                             layer.close(load);
                             //分页数据显示成功后，调用相关方法
                             if(M.pageLoadSuccess){
                            	 M.pageLoadSuccess();
                             }
                             if(onLoadSuccess){
                            	 eval(onLoadSuccess)();
                             }
                    	 } else{
                    		 layer.close(load);
                    		 M.alert('系统异常');
                    	 }
                     }, function(data){
                    	 layer.close(load);
                    	 M.alert(data.msg || '系统异常');
                     });
	             }
			 });
		 }
	}
	
	$(function(){
		M.paginator();
	});
})(jQuery)