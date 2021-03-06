<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
            <form id="preSupplierEdit_form">
            <input type="hidden" name="supplier_id" id="supplier_id" value="${preSupplier.supplier_id}">
			<div class="tab-containe abs-top">
				<div class="tab-wrap clearfix">
					<div class="pro-title clearfix">
						商品资料 
					</div>
					<div class="pro-wrap clearfix">
						<div class="pro-text-wrap pro-edit-wrap">
							<table class="pro-table">
								<tr>
									<th><em>*</em>商品名称： </th>
									<td><input type="text" value="${preSupplier.title}" placeholder="请输入商品名称，8~20个字"  name="title"  datatype="*8-20" nullmsg="请输入商品名称！" errormsg="商品名称8~20个字！" maxlength="20">
										<!-- <i class="tips clearfix"><img src="./images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i> -->
									</td>
								</tr>
								<tr>
									<th><em>*</em>类目：</th>
									<td>
									  <select class="inputk30" name="category" datatype="*" nullmsg="请选择类目！" >
							            <c:forEach items="${appEnumList}" var="appEnum" varStatus="enumStatus">
											<c:forEach items="${appEnum.app_display_area_list}" var="displayArea" varStatus="status">
												<option value="${displayArea.display_area}" <c:if test="${displayArea.display_area eq preSupplier.category}">selected</c:if>>${displayArea.display_area}</option>
											</c:forEach>
										</c:forEach>
							          </select>
									</td>
								</tr>
								<tr>
									<th><em>*</em>规格：</th>
									<td><input type="text" value="${preSupplier.specification}" name="specification" datatype="*1-20" nullmsg="请输入规格！" errormsg="商品规格1~20个字符之间！"><i class="tips clearfix" style="display:none;"/><img src="${ctx}/common/static/images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i></td>
								</tr>
								<tr>
									<th><em>*</em>库存：</th>
									<td><input type="text" value="${preSupplier.stock_qty}" name="stock_qty" datatype="stocknum" nullmsg="请输入库存量！" errormsg="请输入正确库存,库存最小值为100！"  maxlength="9"><i class="tips clearfix" style="display:none;"><img src="${ctx}/common/static/images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i></td>
								</tr>
								<tr>
									<th>标签：</th>
									<td class="tags"><input type="text" value="${preSupplier.label}" name="label" datatype="*1-200" errormsg="标签1~200个字符之间！" ignore="ignore" onkeydown="value=value.replace(/[!@#$%^&*]/,'') "/></td>
								</tr>
								<tr>
									<th><em>*</em>当前售价：</th>
									<td class=""><input type="text" value="${preSupplier.market_price}" name="market_price"  datatype="maxPrice" nullmsg="请输入当前售价！" onkeydown="validateNum(this)" errormsg="当前售价在1~9999999之间！" maxlength="7"><i class="tips clearfix" style="display:none;"><img src="./images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i></td>
								</tr>
								<tr>
									<th><em>*</em>结算佣金：</th>
									<td class=""><input type="text" value="${preSupplier.settled_price}" name="settled_price"  datatype="setPrice"  nullmsg="请输入结算佣金！" onkeydown="validateNum(this)" errormsg="结算佣金在1~9999999之间！" maxlength="7"><i class="tips clearfix" style="display:none;"><img src="./images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i></td>
								</tr>
								<tr>
									<th><em>*</em>商品链接：</th>
									<td class="links"><input type="text" value="${preSupplier.pic_url}" id="prelink" name="pic_url" datatype="validateLink" nullmsg="请输入商品链接！" errormsg="商品链接1~255个字符之间！" maxlength="255"><i class="tips clearfix" style="display:none;"><img src="./images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i></td>
								</tr>
								<tr>
									<th style="vertical-align:top;">商品描述：</th>
									<td><textarea rows="2"  name="desc1" datatype="*20-120" nullmsg="请输入商品描述！" errormsg="商品描述20-120个字！">${preSupplier.desc1}</textarea><i class="tips clearfix" style="display:none;"><img src="./images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i></td>
								</tr>
								<tr>
									<th style="vertical-align:top;">备注：</th>
									<td><textarea  rows="2" value="${preSupplier.remark}" name="remark"  datatype="*1-500" nullmsg="请输入备注！" errormsg="备注不超过500个字！" ignore="ignore"></textarea><i class="tips clearfix" style="display:none;"><img src="./images/tips.png" alt="">错误提示信息，请研发根据产品提供字段修改！</i></td>
								</tr>
								<tr>
									<th style="vertical-align:top;">商品图片：</th>
									<td class="pics big-pics">
									    <input type="hidden" name="pic_info" id="pic_info">
										<div class="upload-btn clearfix"><a href="javascript:void(0);" class="add-imgbtn">上传图片</a>
										<p style="color:#999999;font-size:12px;width:440px;">上传尺寸800x800，大小不超过200KB的JPG、PNG格式图片，最多5张</p>
										</div>
										<div class="god-img-list" id="editimglist">
										<c:if test="${picInfoList.size() > 0}">
										    <c:forEach items="${picInfoList}" var="picInfo">
										         <div class="gods-imgs" id="img-pro${picInfo.path_id}"><img src="${ctx}/file/preview.do?doc_id=${picInfo.path_id}" doc_id="${picInfo.path_id}" height="100" width="100" class="editimgshow"><i class="del" ><a href="javascript:void(0);"  onclick="delimg('${picInfo.path_id}')"><img src="${ctx}/common/static/images/icon-del.png"></a></i></div>
											</c:forEach>
									    </c:if>
				                        </div>
									</td>
								</tr>
								<tr>
									<th style="vertical-align:top;"><em>*</em>商品详情：</th>
									<td class="pics">
									    <input type="hidden" name="pic_detail_info" id="pic_detail_info">
										<div class="upload-btn clearfix">
										<a href="javascript:void(0);" class="add-imgbtn">上传图片</a>
										<p style="color:#999999;font-size:12px;width:440px;">上传宽750-800，高度不限，大小不超过200KB的JPG、PNG格式图片，最多10张</p>
										</div>
										 <div class="god-img-list" id="editimgdetaillist">
											<c:if test="${picDetailInfoList.size() > 0}">
											    <c:forEach items="${picDetailInfoList}" var="picDetailInfo">
												   <div class="gods-imgs" id="img-pro${picDetailInfo.path_id}"><img src="${ctx}/file/preview.do?doc_id=${picDetailInfo.path_id}" doc_id="${picDetailInfo.path_id}" height="100" width="100" class="editimgshow"><i class="del"><a href="javascript:void(0);" onclick="delimg('${picDetailInfo.path_id}')"><img src="${ctx}/common/static/images/icon-del.png"></a></i></div>
												</c:forEach>
										    </c:if>
			              	             </div>
									</td>
								</tr>
							</table>
						</div>
					</div>
				</div>
				<div class="top20 tab-wrap clearfix">
					<div class="pro-title clearfix">商家资料</div>
					<div class="pro-text-wrap fl">
						<div class="wuliu-wrap clearfix">
						<table class="pro-table">
							<tr>
								<th>店铺名称：</th>
								<td class="">${preSupplier.supplier_name}</td>
							</tr>
							<tr>
								<th>联系人名：</th>
								<td class="">${preSupplier.contact_person}</td>
							</tr>
							<tr>
								<th style="width:120px;">联系人手机号：</th>
								<td class="">${preSupplier.contact_tel}</td>
							</tr>
							<tr>
								<th>介绍人：</th>
								<td>${preSupplier.introducer}</td>
							</tr>
							<tr>
								<th>注册地址：</th>
								<td>${preSupplier.detailPath}</td>
							</tr>
						</table>
						</div>
					</div>
				</div>
				<div class="bottom-btn pro-edit-btn clearfix">
					<a href="${ctx}/presupplier/detail?supplier_id=${preSupplier.supplier_id}" id="md-qx">取消</a><a href="javascript:void(0);" class="ok" id="editPreBut">保存</a>
				</div>
			</div>
          </form> 
<script type="text/javascript">
$(function(){
	
	 //初始化地区选择
	M.area(".areaContainer");
	//初始化图片显示
	//initPicInfo();
	//初始化上传控件
	initUpload();

	/**初始化上传控件**/
	function initUpload(){
		$('.add-imgbtn').each(function(){
			var imgUpload = this;
			var uploader = new plupload.Uploader({
				runtimes: 'gears,html5,html4,flash,silverlight',
				browse_button : imgUpload, 	//触发文件选择对话框的按钮，为那个元素id
				url : ctx + '/file/upload.do', //服务器端的上传页面地址
				flash_swf_url : 'js/Moxie.swf', 		//swf文件，当需要使用swf方式进行上传时需要配置该参数
				silverlight_xap_url : 'js/Moxie.xap', 	//silverlight文件，当需要使用silverlight方式进行上传时需要配置该参数
				file_data_name: 'up_load_file',
				unique_names: false,
				filters : {
					max_file_size: '200kb',
					mime_types: [
						{title : "Image files", extensions : "jpg,png"}
					]
				},
				init: {
					FilesAdded: function(up, files) {
					 var loadLen = files.length;
					 var idname = $(imgUpload).parent().parent().find('.god-img-list').attr('id');
					 var len = $(imgUpload).parent().parent().find('.god-img-list').find('.gods-imgs').length;
					 if(idname == 'editimglist'){
							if(loadLen + len >5){
								M.alert("上传商品图片 最多5张!");
								for(var i=0;i<files.length;i++){
									up.removeFile(files[i]);
								}
								return;
							}else{
							up.start();} 
					 }
					 if(idname == 'editimgdetaillist'){
							if(loadLen + len >10){
								M.alert("上传商品详情 最多10张!");
								for(var i=0;i<files.length;i++){
									up.removeFile(files[i]);
								}
								return;
							}else{
							up.start();} 
					 }
					},
					UploadProgress: function(up, file) {
						$('#'+file.id).find('p').text(file.percent + '%');
					},
					Error: function(up,err){
						if(err.code == '-600'){
							var idname = $(imgUpload).parent().parent().find('.god-img-list').attr('id');
							if(idname == 'editimglist'){
								layer.msg("上传尺寸800x800,大小不超过200kb的jpg、png图片!", {time: 3000});
							}
							if(idname == 'editimgdetaillist'){
								layer.msg("建议上传小于200kb宽度为750-800的jpg、png图片!", {time: 3000});
							}
						}
					},
					FileUploaded: function(up, file, responseObject) {
						var data = JSON.parse(responseObject.response)	// 上传接口返回的数据
						var doc_id = data.data;
						var idname = $(imgUpload).parent().parent().find('.god-img-list').attr('id');
						var len = $(imgUpload).parent().parent().find('.god-img-list').find('.gods-imgs').length;
						if(idname == 'editimglist'){
							if(len < 5){
                                $("#editimglist").append('<div class="gods-imgs" id="img-pro'+doc_id+'"><img src='+ctx +'/file/preview.do?doc_id='+ doc_id+' doc_id='+doc_id+'  height="100" width="100" class="editimgshow"><i class="del" ><a href="javascript:void(0);"  onclick="delimg(\''+doc_id+'\')"><img src="${ctx}/common/static/images/icon-del.png"></a></i></div>');
							}
						}
						if(idname == 'editimgdetaillist'){
							if(len < 10){
								$("#editimgdetaillist").append('<div class="gods-imgs" id="img-pro'+doc_id+'"><img src='+ctx +'/file/preview.do?doc_id='+ doc_id+' doc_id='+doc_id+' height="100" width="100" class="editimgshow"><i class="del"><a href="javascript:void(0);" onclick="delimg(\''+doc_id+'\')"><img src="${ctx}/common/static/images/icon-del.png"></a></i></div>');
							}
						}
						
					}
				}
			});
			uploader.init(); 
		});
	}
	
	var validateForm = $('#preSupplierEdit_form').Validform({
		tiptype : 3,
		datatype:{
			"validateLink":function(gets,obj,curform,regxp){
				var flag = true;
				if(gets.length>255||gets.length<1){
					obj.attr('errormsg','商品链接1~255个字符之间！');
					flag = false;
				}
				var pic_url = $("#preSupplierEdit_form input[name='pic_url']").val();
				var supplier_id = $("#supplier_id").val();
				var getinfo = $.trim(pic_url);
				 $.ajax({
				        type: 'POST',
				        url: ctx + '/presupplier/valRecomm',
				        data: {'pic_url':getinfo,'supplier_id':supplier_id},
				        async:false,
				        success: function(result) {
				            if (result.status == '1') {   //成功
				            	 obj.attr('errormsg','此商品链接已存在！');
								 flag = false;
				            }
				        }
				     });
				return flag;
			},
			"stocknum":function(gets,obj,curform,regxp){
	        	var str=/^[1-9]{1}\d{2,8}$/;
				if(str.test(gets)){return true;}
				return false;
	        },
			"maxPrice": function(gets,obj,curform,regxp){
				if(regxp.money.test(gets)){
					if(parseFloat(gets) < 0 || parseFloat(gets) > 9999999 ){
						obj.attr('errormsg','当前售价在1~9999999之间！');
						return false;
					}
					return true;
				}
				return false;
			},
			"setPrice": function(gets,obj,curform,regxp){
				if(regxp.money.test(gets)){
					var market_price_el = $("#preSupplierEdit_form input[name='market_price']");
					var market_price = market_price_el.val();
					var settled_price_el =  $("#preSupplierEdit_form input[name='settled_price']")
					var settled_price = settled_price_el.val();
					if(settled_price){
						if(parseFloat(gets) < 0 || parseFloat(gets) > 9999999 ){
							return false;
						}
						if(parseFloat(market_price) < parseFloat(settled_price)){
							obj.attr('errormsg','结算佣金不能大于当前售价！');
							settled_price_el.nextAll(".Validform_checktip").html(obj.attr('errormsg'));
							settled_price_el.nextAll(".Validform_checktip").removeClass("Validform_right").addClass("Validform_wrong");
							return false;
						}else{
							settled_price_el.nextAll(".Validform_checktip").removeClass("Validform_wrong");
							return true;
						} 
					}else{
						obj.attr('errormsg','');
						return true;
					}
					return true;
				}
				return false;
			}
		}
	});
	
	
	$('#editPreBut').on('click',function(){
		  if(validateForm.check(false)){
		  //验证成功、将上传的图片数据进行格式化
			 var picInfoArray = new Array();
			//产品图片
			$("#editimglist .gods-imgs").each(function(){
				var img = {};
				img.path_id = $(this).find('.editimgshow').attr("doc_id");
				img.title = $(this).find('.ms').text();
				if(img.path_id){
					picInfoArray.push(img);
				}
			});
			if(picInfoArray.length < 1){
				M.alert('请至少上传一张商品图片');
				return false;
			} else{
				$("#pic_info").val(JSON.stringify(picInfoArray));
			} 
			
			//产品详情
			var picdetailArray = new Array();
			$("#editimgdetaillist .gods-imgs").each(function(){
				var img = {};
				img.path_id = $(this).find('.editimgshow').attr("doc_id");
				img.title = "";
				if(img.path_id){
					picdetailArray.push(img);
				}
			});
			
			if(picdetailArray.length < 1){
				M.alert('请至少上传一张商品详情图片');
				return false;
			} else{
				$("#pic_detail_info").val(JSON.stringify(picdetailArray));
			} 
			layer.confirm("是否确认提交？", function(index){
				layer.close(index);
				publish();
			});
			
		  }
   });
		
		//修改 商品信息
		function publish(){
			formatDeliveryArea();
			var loadIdContract = layer.load();
			var data = M.formatElement("#preSupplierEdit_form");
			M.post(ctx + '/presupplier/edit', data, function(result){
			 //成功
			 layer.closeAll();
			 var supplier_id = result.supplier_id;
			 M.alert("操作成功！");
			 window.location.href = '${ctx}/presupplier/detail?supplier_id='+supplier_id;
			}, function(result){
				layer.close(loadIdContract);
				M.alert(result.msg || '提交失败!');
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
				return false;
			}
			
		}
	
	
	
});

function validateNum(obj){
	  obj.value = obj.value.replace(/[^\d.]/g,"");  //清除“数字”和“.”以外的字符   
	  obj.value = obj.value.replace(/\.{2,}/g,"."); //只保留第一个. 清除多余的   
	  obj.value = obj.value.replace(".","$#$").replace(/\./g,"").replace("$#$",".");  
	  obj.value = obj.value.replace(/^(\-)*(\d+)\.(\d\d).*$/,'$1$2.$3');//只能输入两个小数   
	  if(obj.value.indexOf(".")< 0 && obj.value !=""){//以上已经过滤，此处控制的是如果没有小数点，首位不能为类似于 01、02的金额  
	   obj.value= parseFloat(obj.value);  
	  }  
}

function delimg(id){
	var parent = $('#img-pro'+id);
	parent.remove();
}

</script>