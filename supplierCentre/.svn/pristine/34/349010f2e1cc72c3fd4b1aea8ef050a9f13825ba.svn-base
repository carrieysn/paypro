<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/layout/common.jsp" %>
<%
	session.setAttribute("ctx", request.getContextPath());
	session.setAttribute("static_resource_server", "//mps-static.meitianhui.com/product");	//静态资源访问地址
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>每天惠商家中心-领了么商品推荐</title>   
</head>

<body>
	<div class="header">
		<a href="${ctx}/" class="logo">商家中心</a>
		<ul class="nav">			
		<!-- 	<li><a href="javascript:void(0);">供应商登录</a></li> -->
		</ul>
	</div>
	
	<div class="process-wrap process-js1">
		<div class="process-rel">
			<div class="step-wrap">
				<div class="step">
					<ul class="step-list">
						<li><i>1</i>商家资料</li>
						<li class="${empty preSupplier ? 'sel':'' }"><i>2</i>产品资料</li>
						<li class="sel"><i>3</i>合作方案</li>
					</ul>
				</div>
			</div>
			 <form id="presupplier_form" onsubmit="return false;">
			 <div class="tab-containe abs-top1" style="display: ${empty preSupplier ? '':'none'}">
				<div class="tab-wrap">
					<table class="tab-info">
						<tr>
							<td align="right"><em>*&nbsp;</em>店铺名称</td>
							<td><input type="text" placeholder="输入店铺名称,长度在【1-25】之间！" class="k430" name="supplier_name" value="${preSupplier.supplier_name}" nullmsg="请输入店铺名称！" dataType="*1-25" errormsg="店铺名称的长度在【1-25】之间！"></td>
						</tr>
						<tr>
							<td align="right" valign="top"><em>*&nbsp;</em>注册地址</td>
							<td class="areaContainer">
		                         <select class="bind-province k130"></select>
		                         <select class="bind-city k130"></select>
		                         <select class="bind-district k130" name="area_id"></select>
		                         <input type="hidden" class="_path_" value="${empty preSupplier.path ? _path_ : preSupplier.path}">
	                        </td>
						</tr>
						<tr>
							<td align="right"><em>*</em></td>
							<td><input type="text" placeholder="输入详细地址"  name="address" value="${preSupplier.address}" class="k430" datatype="*1-500" nullmsg="请输入详细地址！" errormsg="详细地址的长度在【1-500】之间！"></td>
						</tr>
						<tr>
							<td align="right"><em>*&nbsp;</em>联系人</td>
							<td><input type="text" placeholder="输入具体联系人,长度在【1-30】之间！" name="contact_person" value="${preSupplier.contact_person}" class="k430" datatype="*1-30" nullmsg="请输入联系人！" errormsg="联系人的长度在【1-30】之间！"></td>
						</tr>
						<tr>
							<td align="right"><em>*&nbsp;</em>手机号</td>
							<td><input type="text" placeholder="输入联系人手机号" name="contact_tel" value="${preSupplier.contact_tel}" class="k430" datatype="m"  nullmsg="请输入手机号！" errormsg="请输入正确手机号！"></td>
						</tr>
						<tr>
							<td align="right">介绍人</td>
							<td><input type="text" placeholder="输入介绍人" name="introducer"  value="${preSupplier.introducer}" class="k430" datatype="*1-30"  errormsg="介绍人的长度在【1-30】之间！" ignore="ignore" ></td>
						</tr>
							<tr>
							<td align="right"><input type="checkbox" value="ty" id="isagree" onclick="agreeClick()" checked/></td>
							<td>我已仔细阅读<a href="javascript:void(0);" style="color:#0096dd;"  onclick="protocolShow()">“商家合作协议”</a></td>
						</tr>
						<%-- <tr>
							<td align="right">介绍人手机号</td>
							<td><input type="text" placeholder="输入介绍人电话" name="introducerTel"  value="${preSupplier.introducerTel}" class="k430" datatype="m" errormsg="请输入正确介绍人手机号！" ignore="ignore" ></td>
						</tr> --%>
						<tr>
							<td></td>
							<td><a href="javascript:void(0);" class="btn-x btn btn-next1" onclick="btnNext(1);">下一步</a></td>
						</tr>
					</table>
				</div>
			</div>
			<div class="tab-containe abs-top2" style="display: ${empty preSupplier ? 'none':''}">
				<div class="tab-wrap">
					<table class="tab-info">
						<tr>
							<td align="right" width="120"><em>*&nbsp;</em>产品名称</td>
							<td><input type="text" placeholder="请输入8-20个字！" onkeydown="checktitle(this);"  name="title" class="k430" datatype="*8-20" nullmsg="请输入产品名称！" errormsg="请输入8-20个字！"></td>
						</tr>
						<tr>
							<td align="right" width="120"><em>*&nbsp;</em>产品链接</td>
							<td><input type="text" placeholder="输入产品链接,长度在【1-255】之间！" name="pic_url" class="k430" datatype="validateLink" nullmsg="请输入产品链接！" errormsg="产品链接的长度在【1-255】之间！"></td>
						</tr>
						<tr>
							<td align="right" valign="top" width="120"><em>*&nbsp;</em>产品描述</td>
							<td><textarea  placeholder="请输入20-120个字！"  onkeydown="checkdesc(this);" name="desc1" class="k430" datatype="*20-120" nullmsg="请输入产品描述！" errormsg="请输入20-120个字！"></textarea></td>
						</tr>
						<tr>
							<td align="right" width="120"><em>*&nbsp;</em>产品图片</td>
							<td>
							    <input type="hidden" name="pic_info" id="pic_info">
								<button class="add-imgbtn" style="height: 35px; width: 80px; background:#dddddd;border:none;float:left;margin-right:9px;">上传图片</button> 
								<p style="color:#FF5842;font-size:12px;width:440px;">推荐图片尺寸：800X800像素，只能上传200kb以内jpg,png格式的图片，最多上传5张图片</p>
							</td>
						</tr>
						<tr id="imgshowdiv" style="display: none;">
						    <td align="right"></td>
						    <td>
						    	<div id="upload_img_div" >
									<p style="height:130px;" class="conp">
										<label class="img-pro">
											<a class="img"><img src=""><i class="icon-sc"></i></a>
										</label>
									</p>
				              	</div>
						    </td>
						</tr>
						<tr>
							<td align="right"><em>*&nbsp;</em>类目</td>
							<td><select class="k430" name="category" datatype="*" nullmsg="请选择类目！" >
							<option value="">选择类目</option>
							            <c:forEach items="${appEnumList}" var="appEnum" varStatus="enumStatus">
												<c:forEach items="${appEnum.app_display_area_list}" var="displayArea" varStatus="status">
													<option value="${displayArea.display_area}">${displayArea.display_area}</option>
												</c:forEach>
										</c:forEach>
							</select></td>
						</tr>
						<tr>
							<td align="right"><em>*&nbsp;</em>规格</td>
							<td><input type="text" placeholder="如：300ml或2米高" name="specification"  class="k430" datatype="*1-40" nullmsg="请输入规格！" errormsg="产品规格的长度在【1-40】之间！"></td>
						</tr>
						<tr>
							<td align="right"><em>*&nbsp;</em>库存</td>
							<td><input type="text" placeholder="输入产品库存,至少两位数" name="stock_qty" class="k430"  datatype="n2-9" nullmsg="请输入库存量！" errormsg="请输入正确库存量！"></td>
						</tr>
						<!-- <tr>
							<td align="right">厂家</td>
							<td><input type="text" placeholder="输入产品生产厂家"  name="manufacturer" class="k430"  datatype="*1-200" errormsg="生产厂家的长度在【1-200】之间！" ignore="ignore"></td>
						</tr> -->
						<tr>
							<td align="right">标签</td>
							<td><input type="text" placeholder="多个标签请用“空格” 隔开"  name="label" class="k430"  datatype="*1-200" errormsg="标签的长度在【1-200】之间！" ignore="ignore" onkeyup="value=value.replace(/[!@#$%^&*]/,'') "></td>
						</tr>
						<tr>
							<td></td>
							<td><a href="javascript:void(0);" class="btn-s btn btn-Previous2">上一步</a><a href="javascript:void(0);" class="btn-x btn btn-next2" id="addBut" onclick="btnNext(2);">下一步</a></td>
						</tr>
					</table>
				</div>
			</div>
			<div class="tab-containe abs-top3" style="display: none;">
				<div class="tab-wrap">
					<table class="tab-info">
						<tr>
							<td align="right"><em>*&nbsp;</em>市场价</td>
							<td><input type="text" placeholder="输入市场价,最多输入两位小数" name="market_price" class="k430" datatype="maxPrice" nullmsg="请输入市场价！" errormsg="市场价在1~9999999之间!"  onkeyup="validateNum(this)"></td>
						</tr>
						
						<tr>
							<td align="right">结算价</td>
							<td><input type="text" placeholder="输入结算价,最多输入两位小数" name="settled_price" class="k430" datatype="setPrice"  onkeyup="validateNum(this)" errormsg="结算价在1~9999999之间！" ignore="ignore"></td>
						</tr>
						<tr>
							<td align="right"><em>*&nbsp;</em>优惠价</td>
							<td><input type="text" placeholder="输入优惠价,最多输入两位小数" name="discount_price" class="k430" datatype="minPrice" nullmsg="请输入优惠价！" errormsg="优惠价在1~9999999之间!"  onkeyup="validateNum(this)" ></td>
						</tr>
						<!-- <tr>
							<td align="right">服务费</td>
							<td><input type="text" placeholder="输入服务费,最多输入两位小数" name="service_fee" class="k430" datatype="comparePrice"  onkeyup="validateNum(this)" ignore="ignore"></td>
						</tr> -->
						<tr>
							<td align="right"><em>*&nbsp;</em>起订</td>
							<td><input type="text" placeholder="输入起订数量"  name="min_buy_qty" class="k430" datatype="minNum" nullmsg="请输入起订数量！" errormsg="起订量在1~9999999999 之间!" ></td>
						</tr>
						<tr>
							<td align="right">限购</td>
							<td><input type="text" placeholder="输入限购数量" name="max_buy_qty"  class="k430" datatype="maxNum" errormsg="限购量在1~9999999999 之间!"  ignore="ignore" ></td>
						</tr>
						<!-- <tr>
							<td align="right">开卖时间</td>
							<td><input type="text"  class="k430" name="valid_thru"  onfocus="WdatePicker({dateFmt:'yyyy-MM-dd',minDate:'%y-%M-%d',readOnly:true})"/></td>
						</tr> -->
						<tr>
							<td align="right" valign="top">备注</td>
							<td><textarea   placeholder="备注的长度在【1-500】之间！" name="remark" class="k430" datatype="*1-500" nullmsg="请输入备注！" errormsg="备注的长度在【1-500】之间！" ignore="ignore"></textarea></td>
						</tr>
						<!-- <tr>
							<td align="right">配送范围</td>
							<td><input type="text" placeholder="多个配送区域请用逗号“，”隔开，默认 '全国 '" name="delivery_area" class="k430" datatype="*1-200" errormsg="配送范围的长度在【1-200】之间！" ignore="ignore" ></td>
						</tr> -->
						<tr>
							<td></td>
							<td><a href="javascript:void(0);" class="btn-s btn btn-Previous3">上一步</a><a href="javascript:void(0);" class="btn-x btn btn-refer"  onclick="btnNext(3);" >提交</a></td>
						</tr>
					</table>
				</div>
			</div>
			</form>
			<div class="tab-containe abs-top4" style="display: none;">
				<div class="tab-wrap">
					<div class="submit-bor">
					    <div class="ft18" style="color:#333333">商品活动码：HD<span style="font-size:18px;" id="activecode"></span></div>
						<div class="ft40">领了么 · 推荐商品，提交成功！</div>
						<div class="ft18">工作人员将在1-3个工作日对该产品进行审核，请耐心等待！
	您可以在首页或商家报名列表页面领了么广告上点击“查看按钮”查看审核状态！</div>
						<div class="tab-info"><a class="btn-s btn" href="${ctx}/">返回首页</a><a class="btn-x btn" id="goOnRecomm" >继续推荐</a></div>
					</div>
				</div>
			</div>
		</div>
	</div>
	                <!-- 弹框 -->
					<div id="upload_img_div" class="tankuang-bor" style="display: none;">
					    <p style="color:#FF5842;font-size:14px;">推荐图片尺寸：800X800像素，只能上传200kb以内jpg,png格式的图片</p>
						<p style="height:160px;">
							<label class="img-pro" style="display:none;">
								<a class="img"><img src=""><i class="icon-sc"></i></a>
							</label>
							<label>
								<a class="add-img" style="position: relative; z-index: 1; display: inline-block;"></a>
							</label>
						</p>
						<p style="height:100px;"><textarea id="upload-img-desc" placeholder="您可以对照片进行描述"></textarea></p>
					</div>
<%@ include file="/common/layout/footer.jsp"%>
<script type="text/javascript" src="${static_resource_server}/common/third-part/plupload/plupload.full.min.js"></script>
	<script type="text/javascript" src="${ctx}/common/static/js/areaSelector.js"></script>
	<script type="text/javascript" src="${ctx}/common/static/js/thelper.js"></script>
	<script type="text/javascript">
		var goods_display_area = '${goods.display_area}', goods_pic_info = '${goods.pic_info}', goods_pic_detail_info = '${goods.pic_detail_info}';
	</script>
    <script type="text/javascript" src="${ctx}/common/static/goods/js/addGood.js"></script>

<script>
	$(function(){
		var oHeight = $('.abs-top1').height();
	    /* var supplier_id = '${preSupplier.supplier_id}';
		if(supplier_id){
			var oHeight2 = $('.abs-top2').height();
			$('.process-wrap').css('height',oHeight2+220);
		} */
		$('.btn-next1').css('background','#e95613');
		$('.btn-next1').css('borderColor','#e95613');
		/* $('.btn-next1').click(function(){
		    $('.process-rel .tab-containe').hide();
			$('.abs-top2').show();
			$('.step-list li').eq(1).removeClass('sel');
		}) 
		 $('.btn-next2').click(function(){
			$('.process-rel .tab-containe').hide();
			$('.abs-top3').show();
			$('.step-list li').eq(2).removeClass('sel');
		}) */
		$('.btn-Previous2').click(function(){
			$('.process-rel .tab-containe').hide();
			$('.abs-top1').show();
			$('.step-list li').eq(1).addClass('sel');
		})
		$('.btn-Previous3').click(function(){
			$('.process-rel .tab-containe').hide();
			$('.abs-top2').show();
			$('.step-list li').eq(2).addClass('sel');			
		})
		
		$(".tab-info .add-img").eq(2).css('margin-right',0);
		$(".input-container").css('width','440px');

	})

</script>