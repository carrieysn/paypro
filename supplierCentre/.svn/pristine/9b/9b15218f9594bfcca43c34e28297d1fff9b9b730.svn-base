   $(function(){
		 //初始化地区选择
		M.area(".areaContainer");
		//初始化图片显示
		//initPicInfo();
		//上传事件
		bindUploadEvent();
		//初始化上传控件
		initUpload();
		
		var validateForm = $('#presupplier_form').Validform({
			tiptype : 3,
			datatype:{
				"comparePrice": function(gets,obj,curform,regxp){
					if(regxp.money.test(gets)){
						var market_price_el = $("#presupplier_form input[name='market_price']");
						var market_price = market_price_el.val();
						var discount_price_el =  $("#presupplier_form input[name='settled_price']")
						var discount_price = discount_price_el.val();
						if(market_price && discount_price){
							if(parseFloat(gets) < 0){
								return '请输入正确的金额！';
							}
							if(parseFloat(market_price) < parseFloat(discount_price)){
								return false;
							}else{
								market_price_el.removeClass("Validform_error");
								market_price_el.nextAll(".Validform_checktip").removeClass("Validform_wrong");
								discount_price_el.removeClass("Validform_error");
								discount_price_el.nextAll(".Validform_checktip").removeClass("Validform_wrong");
							}
						}
						return;
					}
					return "请输入正确的金额！";
				},
				"minNum": function(gets,obj,curform,regxp){
					if(regxp.n.test(gets)){
						if(parseInt(gets) < 1){
							return "数量不能少于一！";
						}
						return true;
					}
					return "请填写数字！";
				}
				
			}
		});
	
		  $('.btn-refer').on('click',function(){
			  if(validateForm.check(false)){
			  //验证成功、将上传的图片数据进行格式化
				var picInfoArray = new Array();
				console.log($("#pic_td .img-pro:visible"));
				//产品图片
				$("#pic_td .img-pro").each(function(){
					var img = {};
					img.path_id = $(this).find('img').attr("doc_id");
					img.title = $(this).find('.ms').text();
					if(img.path_id){
						picInfoArray.push(img);
					}
				});
				if(picInfoArray.length < 1){
					M.alert('请至少上传一张产品图片');
					return false;
				} else{
					$("#pic_info").val(JSON.stringify(picInfoArray));
				}
				var min_buy_qty = parseInt($("#presupplier_form input[name='min_buy_qty']").val() || "0");	//起订量
				var stock_qty = parseInt($("#presu" +
						"pplier_form input[name='stock_qty']").val() || "0");	//库存
				if(stock_qty < min_buy_qty){
					layer.confirm("库存量少于起订量，是否确定推荐该商品？",{icon: 3}, function(index){
						layer.close(index);
						publish();
					});
				} else{
					layer.confirm("是否确认推荐该商品？", function(index){
						layer.close(index);
						publish();
					});
				}
			  }
	     });
			
			//推荐商品信息
			function publish(){
				formatDeliveryArea();
				var loadIdContract = layer.load();
				var data = M.formatElement("#presupplier_form");
				M.post(ctx + '/presupplier/save', data, function(result){
				 //成功
				 layer.closeAll();
				    $('.process-rel .tab-containe').hide();
					$('.abs-top4').show();
					$('.step-list').hide();
				
				}, function(result){
					layer.close(loadIdContract);
					M.alert(result.msg || '发布失败!');
				});
				//格式化注册地址 
				function formatDeliveryArea(){
					var areaArray = new Array();
					$(".areaContainer").each(function(){
						var _this = this;
						var district = $(this).find(".bind-district").val();
						var city = $(this).find(".bind-city").val();
						var province = $(this).find(".bind-province").val();
						var areaCode = district || city || province;
						if(areaCode){
							areaArray.push(areaCode);
						}
					});
					$("#area_id").val(areaArray.join(",") || "100000");  //默认为全国
					return false;
				}
				
			}
			
			/**绑定图片上传事件**/
			function bindUploadEvent(){
				$(".add-img").not("#upload_img_div .add-img").bind("click", function(){
					var target = this;
					var parent = $(target).closest('span');
					layer.open({
					    type: 1,
					    title: '添加图片',
					    btn: ['确定', '取消'],
					    shadeClose: true,
					    shade: 0.8,
					    shadeClose: false,
					    area: ['480px', '410px'],
					    content: $("#upload_img_div"),
					    yes: function(index){
					    	var desc1 = $("#upload-img-desc").val() ;
							var doc_id = $('#upload_img_div .img-pro img').attr("doc_id");
							if(doc_id){
								//关闭层
								layer.close(index);
								//添加图片信息
								parent.find('.img-pro').show().find('img').attr('doc_id', doc_id).attr("src", ctx + "/file/preview.do?doc_id=" + doc_id);
								parent.find('.img-pro .ms').text(desc1);
								$(target).hide();
							} else{
								M.alert('请上传图片！');
							}
					    },
						cancel: function(index){
					    	layer.close(index);
					    },
					    end: function(){  //关闭层事件
					    	//还原之前样式
					    	$('#upload_img_div .icon-sc').trigger('click');
					    	$('#upload-img-desc').val('');
					    }
					}); 
				});
				
				//移除图片事件
				$('.icon-sc').on('click', function(){
					var parent = $(this).closest('.img-pro').hide();
					parent.find('img').removeAttr('doc_id').removeAttr("src");
					parent.find('.ms').text('');
					parent.next('label').find('.add-img').show().next("div").show();
				});
			}
			
			/**初始化上传控件**/
			function initUpload(){
				$('#upload_img_div .add-img').each(function(){
					var imgUpload = this;
					var uploader = new plupload.Uploader({
						runtimes: 'gears,html5,html4,flash,silverlight',
						browse_button : imgUpload, 	//触发文件选择对话框的按钮，为那个元素id
						url : ctx + '/file/upload.do', //服务器端的上传页面地址
						flash_swf_url : 'js/Moxie.swf', 		//swf文件，当需要使用swf方式进行上传时需要配置该参数
						silverlight_xap_url : 'js/Moxie.xap', 	//silverlight文件，当需要使用silverlight方式进行上传时需要配置该参数
						file_data_name: 'up_load_file',
						filters : {
							max_file_size : '200kb',
							mime_types: [
								{title : "Image files", extensions : "jpg,png"}
							]
						},
						init: {
							FilesAdded: function(up, files) {
								up.start();
							},
							UploadProgress: function(up, file) {
								$('#'+file.id).find('p').text(file.percent + '%');
							},
							FileUploaded: function(up, file, responseObject) {
								var data = JSON.parse(responseObject.response)	// 上传接口返回的数据
								var doc_id = data.data;
								$("#upload_img_div .img-pro").show();
								$("#upload_img_div .img-pro img").attr("doc_id", doc_id).attr("src", ctx + "/file/preview.do?doc_id=" + doc_id);
								$(imgUpload).hide().next("div").hide();
							}
						}
					});
					uploader.init();
				});
			}
		
   });